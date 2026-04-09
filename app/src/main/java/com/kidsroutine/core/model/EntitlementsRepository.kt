package com.kidsroutine.core.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads and caches user/family entitlements from Firestore.
 *
 * Resolution chain (first match wins):
 *  1. user_entitlements/{userId}           — purchasing parent's own doc
 *  2. families/{familyId}/subscription     — family-level subscription doc
 *  3. ai_quotas/{userId}.tier              — legacy fallback
 *  4. FREE defaults
 *
 * A single subscription purchased by ANY parent applies to the entire family
 * (all parents + all children). The family-level doc at
 * families/{familyId}/subscription is the canonical source of truth for
 * non-purchasing family members.
 */
@Singleton
class EntitlementsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "Entitlements"
    }

    // In-memory cache keyed by userId — avoids repeated Firestore reads
    private val cache = mutableMapOf<String, UserEntitlements>()

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Load entitlements for [userId].
     * When [familyId] is provided the family-level subscription is checked
     * as a fallback if the user has no personal entitlements doc, so every
     * family member inherits the plan purchased by any parent.
     */
    suspend fun getEntitlements(userId: String, familyId: String = ""): UserEntitlements {
        cache[userId]?.let { return it }

        return try {
            // 1. user_entitlements/{userId}
            val userDoc = firestore
                .collection("user_entitlements")
                .document(userId)
                .get()
                .await()

            if (userDoc.exists()) {
                val entitlements = parseEntitlementsDoc(userId, userDoc.data)
                if (entitlements != null && entitlements.planType != PlanType.FREE) {
                    cache[userId] = entitlements
                    Log.d(TAG, "Loaded user entitlements for $userId: ${entitlements.planType}")
                    return entitlements
                }
                // User doc exists but is FREE — still check family subscription
            }

            // 2. families/{familyId}/subscription  (family-level)
            val resolvedFamilyId = familyId.ifBlank { lookupFamilyId(userId) }
            if (resolvedFamilyId.isNotBlank()) {
                val familyEnt = loadFamilySubscription(userId, resolvedFamilyId)
                if (familyEnt != null && familyEnt.planType != PlanType.FREE) {
                    cache[userId] = familyEnt
                    Log.d(TAG, "Loaded family subscription for $userId via family $resolvedFamilyId: ${familyEnt.planType}")
                    return familyEnt
                }
            }

            // 3. Legacy fallback: ai_quotas/{userId}.tier
            val quotaDoc = firestore
                .collection("ai_quotas")
                .document(userId)
                .get()
                .await()
            val tierStr = quotaDoc.data?.get("tier") as? String ?: "FREE"
            val planType = try { PlanType.valueOf(tierStr) } catch (_: Exception) { PlanType.FREE }
            val entitlements = planType.defaultEntitlements(userId)
            cache[userId] = entitlements
            Log.d(TAG, "Fallback to ai_quotas tier=$tierStr for $userId")

            // Auto-create the entitlements doc so it can be edited in Firestore Console
            ensureEntitlementsDocExists(userId, entitlements)

            entitlements

        } catch (e: Exception) {
            Log.e(TAG, "Error loading entitlements for $userId: ${e.message}")
            freeFallback(userId)
        }
    }

    /**
     * Ensure a user_entitlements document exists in Firestore.
     * Called during fallback so the doc can be manually edited for testing.
     */
    private suspend fun ensureEntitlementsDocExists(userId: String, entitlements: UserEntitlements) {
        try {
            val docRef = firestore.collection("user_entitlements").document(userId)
            val doc = docRef.get().await()
            if (!doc.exists()) {
                docRef.set(entitlementsToMap(entitlements)).await()
                Log.d(TAG, "Auto-created user_entitlements doc for $userId (${entitlements.planType})")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not auto-create entitlements doc for $userId: ${e.message}")
        }
    }

    // ── Write ──────────────────────────────────────────────────────────────

    /** Persist entitlements for the purchasing parent. */
    suspend fun saveEntitlements(entitlements: UserEntitlements) {
        try {
            firestore.collection("user_entitlements")
                .document(entitlements.userId)
                .set(entitlementsToMap(entitlements))
                .await()
            cache[entitlements.userId] = entitlements
            Log.d(TAG, "Saved user entitlements for ${entitlements.userId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving entitlements: ${e.message}")
        }
    }

    /**
     * Write a family-level subscription doc so ALL members inherit the plan.
     * Called alongside [saveEntitlements] when a parent purchases.
     */
    suspend fun saveFamilySubscription(
        familyId: String,
        billingParentId: String,
        planType: PlanType
    ) {
        try {
            firestore.collection("families")
                .document(familyId)
                .collection("subscription")
                .document("current")
                .set(mapOf(
                    "planType"        to planType.name,
                    "billingParentId" to billingParentId,
                    "updatedAt"       to System.currentTimeMillis()
                ))
                .await()
            // Invalidate cache for all family members so they pick up the change
            clearFamilyCache(familyId)
            Log.d(TAG, "Saved family subscription for family=$familyId by parent=$billingParentId → $planType")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving family subscription: ${e.message}")
        }
    }

    /**
     * Load the family subscription metadata (plan + billing parent).
     * Returns null if no family subscription exists.
     */
    suspend fun getFamilySubscriptionInfo(familyId: String): FamilySubscriptionInfo? {
        if (familyId.isBlank()) return null
        return try {
            val doc = firestore.collection("families")
                .document(familyId)
                .collection("subscription")
                .document("current")
                .get()
                .await()
            if (!doc.exists()) return null
            val data = doc.data ?: return null
            val plan = try { PlanType.valueOf(data["planType"] as? String ?: "FREE") } catch (_: Exception) { PlanType.FREE }
            FamilySubscriptionInfo(
                planType        = plan,
                billingParentId = data["billingParentId"] as? String ?: "",
                updatedAt       = (data["updatedAt"] as? Number)?.toLong() ?: 0L
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error reading family subscription: ${e.message}")
            null
        }
    }

    // ── Cache management ───────────────────────────────────────────────────

    fun clearCache(userId: String) {
        cache.remove(userId)
    }

    fun clearAllCache() {
        cache.clear()
    }

    // ── Internal helpers ───────────────────────────────────────────────────

    private fun clearFamilyCache(familyId: String) {
        // We can't easily know all member userIds here, so clear everything
        cache.clear()
    }

    private suspend fun lookupFamilyId(userId: String): String {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            userDoc.data?.get("familyId") as? String ?: ""
        } catch (e: Exception) {
            Log.w(TAG, "Could not look up familyId for $userId: ${e.message}")
            ""
        }
    }

    private suspend fun loadFamilySubscription(userId: String, familyId: String): UserEntitlements? {
        return try {
            val doc = firestore.collection("families")
                .document(familyId)
                .collection("subscription")
                .document("current")
                .get()
                .await()
            if (!doc.exists()) return null
            val data = doc.data ?: return null
            val planStr = data["planType"] as? String ?: "FREE"
            val plan = try { PlanType.valueOf(planStr) } catch (_: Exception) { PlanType.FREE }
            if (plan == PlanType.FREE) return null
            // Return default entitlements for this plan, attributed to the requesting user
            plan.defaultEntitlements(userId)
        } catch (e: Exception) {
            Log.w(TAG, "Could not load family subscription for family=$familyId: ${e.message}")
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseEntitlementsDoc(userId: String, data: Map<String, Any>?): UserEntitlements? {
        if (data == null) return null
        val planType = try {
            PlanType.valueOf(data["planType"] as? String ?: "FREE")
        } catch (_: Exception) {
            PlanType.FREE
        }
        val defaults = planType.defaultEntitlements(userId)
        return UserEntitlements(
            userId                   = userId,
            planType                 = planType,
            aiTasksPerDay            = (data["aiTasksPerDay"] as? Number)?.toInt() ?: defaults.aiTasksPerDay,
            aiChallengesPerDay       = (data["aiChallengesPerDay"] as? Number)?.toInt() ?: defaults.aiChallengesPerDay,
            aiPlansPerDay            = (data["aiPlansPerDay"] as? Number)?.toInt() ?: defaults.aiPlansPerDay,
            aiWeeklyPlansPerMonth    = (data["aiWeeklyPlansPerMonth"] as? Number)?.toInt() ?: defaults.aiWeeklyPlansPerMonth,
            unlockedFeatures         = (data["unlockedFeatures"] as? List<String>) ?: emptyList(),
            maxChildren              = (data["maxChildren"] as? Number)?.toInt() ?: defaults.maxChildren,
            parentControlsEnabled    = (data["parentControlsEnabled"] as? Boolean) ?: defaults.parentControlsEnabled,
            xpBankEnabled            = (data["xpBankEnabled"] as? Boolean) ?: defaults.xpBankEnabled,
            customDifficultyEnabled  = (data["customDifficultyEnabled"] as? Boolean) ?: defaults.customDifficultyEnabled,
            updatedAt                = (data["updatedAt"] as? Number)?.toLong() ?: 0L,
            aiTrialChallengePrompts  = (data["aiTrialChallengePrompts"] as? Number)?.toInt() ?: defaults.aiTrialChallengePrompts,
            aiTrialPlanPrompts       = (data["aiTrialPlanPrompts"] as? Number)?.toInt() ?: defaults.aiTrialPlanPrompts,
            aiTrialWeeklyPlanPrompts = (data["aiTrialWeeklyPlanPrompts"] as? Number)?.toInt() ?: defaults.aiTrialWeeklyPlanPrompts
        )
    }

    private fun entitlementsToMap(e: UserEntitlements) = mapOf(
        "planType"                 to e.planType.name,
        "aiTasksPerDay"            to e.aiTasksPerDay,
        "aiChallengesPerDay"       to e.aiChallengesPerDay,
        "aiPlansPerDay"            to e.aiPlansPerDay,
        "aiWeeklyPlansPerMonth"    to e.aiWeeklyPlansPerMonth,
        "unlockedFeatures"         to e.unlockedFeatures,
        "maxChildren"              to e.maxChildren,
        "parentControlsEnabled"    to e.parentControlsEnabled,
        "xpBankEnabled"            to e.xpBankEnabled,
        "customDifficultyEnabled"  to e.customDifficultyEnabled,
        "aiTrialChallengePrompts"  to e.aiTrialChallengePrompts,
        "aiTrialPlanPrompts"       to e.aiTrialPlanPrompts,
        "aiTrialWeeklyPlanPrompts" to e.aiTrialWeeklyPlanPrompts,
        "updatedAt"                to System.currentTimeMillis()
    )

    private fun freeFallback(userId: String): UserEntitlements {
        val e = PlanType.FREE.defaultEntitlements(userId)
        cache[userId] = e
        return e
    }
}

/**
 * Metadata about the family-level subscription.
 * Used to display "Managed by [parent]" on the UpgradeScreen
 * and to prevent duplicate purchases.
 */
data class FamilySubscriptionInfo(
    val planType: PlanType = PlanType.FREE,
    val billingParentId: String = "",
    val updatedAt: Long = 0L
)