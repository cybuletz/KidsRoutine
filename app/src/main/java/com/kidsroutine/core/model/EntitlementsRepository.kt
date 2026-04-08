package com.kidsroutine.core.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads and caches user entitlements from Firestore.
 * Collection: user_entitlements/{userId}
 *
 * Falls back to FREE defaults if document doesn't exist.
 * Also reads the existing ai_quotas/{userId} tier field as a fallback
 * so we don't break any user who was already on PRO via quota.
 */
@Singleton
class EntitlementsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // In-memory cache so we don't hit Firestore on every screen
    private val cache = mutableMapOf<String, UserEntitlements>()

    suspend fun getEntitlements(userId: String): UserEntitlements {
        cache[userId]?.let { return it }

        return try {
            // 1. Try user_entitlements collection first
            val doc = firestore
                .collection("user_entitlements")
                .document(userId)
                .get()
                .await()

            if (doc.exists()) {
                val data = doc.data ?: return freeFallback(userId)
                val planType = try {
                    PlanType.valueOf(data["planType"] as? String ?: "FREE")
                } catch (e: Exception) {
                    PlanType.FREE
                }

                @Suppress("UNCHECKED_CAST")
                val defaults = planType.defaultEntitlements(userId)
                val entitlements = UserEntitlements(
                    userId                  = userId,
                    planType                = planType,
                    aiTasksPerDay           = (data["aiTasksPerDay"] as? Number)?.toInt()
                        ?: defaults.aiTasksPerDay,
                    aiChallengesPerDay      = (data["aiChallengesPerDay"] as? Number)?.toInt()
                        ?: defaults.aiChallengesPerDay,
                    aiPlansPerDay           = (data["aiPlansPerDay"] as? Number)?.toInt()
                        ?: defaults.aiPlansPerDay,
                    aiWeeklyPlansPerMonth   = (data["aiWeeklyPlansPerMonth"] as? Number)?.toInt()
                        ?: defaults.aiWeeklyPlansPerMonth,
                    unlockedFeatures        = (data["unlockedFeatures"] as? List<String>) ?: emptyList(),
                    maxChildren             = (data["maxChildren"] as? Number)?.toInt()
                        ?: defaults.maxChildren,
                    parentControlsEnabled   = (data["parentControlsEnabled"] as? Boolean)
                        ?: defaults.parentControlsEnabled,
                    xpBankEnabled           = (data["xpBankEnabled"] as? Boolean)
                        ?: defaults.xpBankEnabled,
                    customDifficultyEnabled = (data["customDifficultyEnabled"] as? Boolean)
                        ?: defaults.customDifficultyEnabled,
                    updatedAt               = (data["updatedAt"] as? Number)?.toLong() ?: 0L
                )
                cache[userId] = entitlements
                Log.d("Entitlements", "Loaded entitlements for $userId: ${entitlements.planType}")
                entitlements
            } else {
                // 2. Fallback: read tier from existing ai_quotas document
                val quotaDoc = firestore
                    .collection("ai_quotas")
                    .document(userId)
                    .get()
                    .await()

                val tierStr = quotaDoc.data?.get("tier") as? String ?: "FREE"
                val planType = try { PlanType.valueOf(tierStr) } catch (e: Exception) { PlanType.FREE }
                val entitlements = planType.defaultEntitlements(userId)
                cache[userId] = entitlements
                Log.d("Entitlements", "No entitlements doc, using quota tier=$tierStr for $userId")
                entitlements
            }
        } catch (e: Exception) {
            Log.e("Entitlements", "Error loading entitlements: ${e.message}")
            freeFallback(userId)
        }
    }

    /** Write entitlements (called from billing module in Batch 8) */
    suspend fun saveEntitlements(entitlements: UserEntitlements) {
        try {
            firestore.collection("user_entitlements")
                .document(entitlements.userId)
                .set(mapOf(
                    "planType"                to entitlements.planType.name,
                    "aiTasksPerDay"           to entitlements.aiTasksPerDay,
                    "aiChallengesPerDay"      to entitlements.aiChallengesPerDay,
                    "aiPlansPerDay"           to entitlements.aiPlansPerDay,
                    "aiWeeklyPlansPerMonth"   to entitlements.aiWeeklyPlansPerMonth,
                    "unlockedFeatures"        to entitlements.unlockedFeatures,
                    "maxChildren"             to entitlements.maxChildren,
                    "parentControlsEnabled"   to entitlements.parentControlsEnabled,
                    "xpBankEnabled"           to entitlements.xpBankEnabled,
                    "customDifficultyEnabled" to entitlements.customDifficultyEnabled,
                    "updatedAt"               to System.currentTimeMillis()
                ))
                .await()
            cache[entitlements.userId] = entitlements
            Log.d("Entitlements", "Saved entitlements for ${entitlements.userId}")
        } catch (e: Exception) {
            Log.e("Entitlements", "Error saving entitlements: ${e.message}")
        }
    }

    fun clearCache(userId: String) {
        cache.remove(userId)
    }

    private fun freeFallback(userId: String): UserEntitlements {
        val e = PlanType.FREE.defaultEntitlements(userId)
        cache[userId] = e
        return e
    }
}