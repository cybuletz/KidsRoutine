package com.kidsroutine.feature.parent.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.ParentControlSettings
import com.kidsroutine.core.model.XpLoan
import com.kidsroutine.core.model.XpLoanStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParentControlRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ParentControlRepository {

    // ── Parent Control Settings ────────────────────────────────────────────

    override suspend fun getControlSettings(familyId: String, childId: String): ParentControlSettings {
        return try {
            val doc = firestore
                .collection("families").document(familyId)
                .collection("parent_controls").document(childId)
                .get().await()

            if (doc.exists()) {
                val data = doc.data ?: return ParentControlSettings(childId = childId, familyId = familyId)
                mapToControlSettings(data, childId, familyId)
            } else {
                ParentControlSettings(childId = childId, familyId = familyId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading parent controls for $childId", e)
            ParentControlSettings(childId = childId, familyId = familyId)
        }
    }

    override suspend fun saveControlSettings(settings: ParentControlSettings) {
        try {
            firestore
                .collection("families").document(settings.familyId)
                .collection("parent_controls").document(settings.childId)
                .set(mapOf(
                    "childId"               to settings.childId,
                    "familyId"              to settings.familyId,
                    "petEnabled"            to settings.petEnabled,
                    "bossBattleEnabled"     to settings.bossBattleEnabled,
                    "dailySpinEnabled"      to settings.dailySpinEnabled,
                    "storyArcsEnabled"      to settings.storyArcsEnabled,
                    "eventsEnabled"         to settings.eventsEnabled,
                    "skillTreeEnabled"      to settings.skillTreeEnabled,
                    "walletEnabled"         to settings.walletEnabled,
                    "ritualsEnabled"        to settings.ritualsEnabled,
                    "allowedDifficulties"   to settings.allowedDifficulties.map { it.name },
                    "defaultDifficulty"     to settings.defaultDifficulty.name,
                    "xpMultiplierEasy"      to settings.xpMultiplierEasy,
                    "xpMultiplierMedium"    to settings.xpMultiplierMedium,
                    "xpMultiplierHard"      to settings.xpMultiplierHard,
                    "dailyXpEarningCap"     to settings.dailyXpEarningCap,
                    "dailyXpSpendingCap"    to settings.dailyXpSpendingCap,
                    "updatedAt"             to System.currentTimeMillis()
                )).await()
            Log.d(TAG, "Saved parent controls for ${settings.childId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving parent controls", e)
        }
    }

    override fun observeControlSettings(familyId: String, childId: String): Flow<ParentControlSettings> = callbackFlow {
        val ref = firestore
            .collection("families").document(familyId)
            .collection("parent_controls").document(childId)

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing parent controls", error)
                return@addSnapshotListener
            }
            val data = snapshot?.data
            if (data != null) {
                trySend(mapToControlSettings(data, childId, familyId))
            } else {
                trySend(ParentControlSettings(childId = childId, familyId = familyId))
            }
        }
        awaitClose { listener.remove() }
    }

    // ── XP Bank / Loans ────────────────────────────────────────────────────

    override suspend fun createLoan(loan: XpLoan) {
        try {
            val loanId = loan.loanId.ifEmpty { firestore
                .collection("families").document(loan.familyId)
                .collection("xp_loans").document().id }

            val loanWithId = loan.copy(
                loanId = loanId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            firestore
                .collection("families").document(loan.familyId)
                .collection("xp_loans").document(loanId)
                .set(mapOf(
                    "loanId"              to loanWithId.loanId,
                    "familyId"            to loanWithId.familyId,
                    "parentId"            to loanWithId.parentId,
                    "childId"             to loanWithId.childId,
                    "childName"           to loanWithId.childName,
                    "amount"              to loanWithId.amount,
                    "amountRepaid"        to loanWithId.amountRepaid,
                    "repaymentPercentage" to loanWithId.repaymentPercentage,
                    "status"              to loanWithId.status.name,
                    "note"                to loanWithId.note,
                    "createdAt"           to loanWithId.createdAt,
                    "completedAt"         to loanWithId.completedAt,
                    "updatedAt"           to loanWithId.updatedAt
                )).await()

            Log.d(TAG, "Created XP loan: $loanId for child ${loan.childId}, amount=${loan.amount}")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating XP loan", e)
        }
    }

    override suspend fun getActiveLoans(familyId: String, childId: String): List<XpLoan> {
        return try {
            val snapshot = firestore
                .collection("families").document(familyId)
                .collection("xp_loans")
                .whereEqualTo("childId", childId)
                .whereEqualTo("status", XpLoanStatus.ACTIVE.name)
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { mapToXpLoan(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading active loans for $childId", e)
            emptyList()
        }
    }

    override suspend fun getAllFamilyLoans(familyId: String): List<XpLoan> {
        return try {
            val snapshot = firestore
                .collection("families").document(familyId)
                .collection("xp_loans")
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { mapToXpLoan(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading family loans", e)
            emptyList()
        }
    }

    override suspend fun repayLoan(loanId: String, familyId: String, amount: Int) {
        try {
            val loanRef = firestore
                .collection("families").document(familyId)
                .collection("xp_loans").document(loanId)

            val doc = loanRef.get().await()
            if (!doc.exists()) return

            val currentRepaid = (doc.data?.get("amountRepaid") as? Number)?.toInt() ?: 0
            val totalAmount = (doc.data?.get("amount") as? Number)?.toInt() ?: 0
            val newRepaid = (currentRepaid + amount).coerceAtMost(totalAmount)

            val updates = mutableMapOf<String, Any>(
                "amountRepaid" to newRepaid,
                "updatedAt"    to System.currentTimeMillis()
            )

            if (newRepaid >= totalAmount) {
                updates["status"] = XpLoanStatus.COMPLETED.name
                updates["completedAt"] = System.currentTimeMillis()
            }

            loanRef.update(updates).await()
            Log.d(TAG, "Repaid $amount XP on loan $loanId (total repaid: $newRepaid/$totalAmount)")
        } catch (e: Exception) {
            Log.e(TAG, "Error repaying loan", e)
        }
    }

    override suspend fun forgiveLoan(loanId: String, familyId: String) {
        try {
            firestore
                .collection("families").document(familyId)
                .collection("xp_loans").document(loanId)
                .update(mapOf(
                    "status"      to XpLoanStatus.FORGIVEN.name,
                    "completedAt" to System.currentTimeMillis(),
                    "updatedAt"   to System.currentTimeMillis()
                )).await()
            Log.d(TAG, "Forgave loan $loanId")
        } catch (e: Exception) {
            Log.e(TAG, "Error forgiving loan", e)
        }
    }

    override suspend fun cancelLoan(loanId: String, familyId: String) {
        try {
            firestore
                .collection("families").document(familyId)
                .collection("xp_loans").document(loanId)
                .update(mapOf(
                    "status"      to XpLoanStatus.CANCELLED.name,
                    "completedAt" to System.currentTimeMillis(),
                    "updatedAt"   to System.currentTimeMillis()
                )).await()
            Log.d(TAG, "Cancelled loan $loanId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling loan", e)
        }
    }

    override fun observeActiveLoans(familyId: String, childId: String): Flow<List<XpLoan>> = callbackFlow {
        val ref = firestore
            .collection("families").document(familyId)
            .collection("xp_loans")
            .whereEqualTo("childId", childId)
            .whereEqualTo("status", XpLoanStatus.ACTIVE.name)

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing loans", error)
                return@addSnapshotListener
            }
            val loans = snapshot?.documents?.mapNotNull { doc ->
                doc.data?.let { mapToXpLoan(it) }
            } ?: emptyList()
            trySend(loans)
        }
        awaitClose { listener.remove() }
    }

    // ── Mapping helpers ────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun mapToControlSettings(data: Map<String, Any>, childId: String, familyId: String): ParentControlSettings {
        return ParentControlSettings(
            childId             = childId,
            familyId            = familyId,
            petEnabled          = data["petEnabled"] as? Boolean ?: true,
            bossBattleEnabled   = data["bossBattleEnabled"] as? Boolean ?: true,
            dailySpinEnabled    = data["dailySpinEnabled"] as? Boolean ?: true,
            storyArcsEnabled    = data["storyArcsEnabled"] as? Boolean ?: true,
            eventsEnabled       = data["eventsEnabled"] as? Boolean ?: true,
            skillTreeEnabled    = data["skillTreeEnabled"] as? Boolean ?: true,
            walletEnabled       = data["walletEnabled"] as? Boolean ?: true,
            ritualsEnabled      = data["ritualsEnabled"] as? Boolean ?: true,
            allowedDifficulties = (data["allowedDifficulties"] as? List<String>)?.mapNotNull {
                try { DifficultyLevel.valueOf(it) } catch (_: Exception) { null }
            } ?: DifficultyLevel.entries,
            defaultDifficulty   = try {
                DifficultyLevel.valueOf(data["defaultDifficulty"] as? String ?: "MEDIUM")
            } catch (_: Exception) { DifficultyLevel.MEDIUM },
            xpMultiplierEasy    = (data["xpMultiplierEasy"] as? Number)?.toFloat() ?: 1.0f,
            xpMultiplierMedium  = (data["xpMultiplierMedium"] as? Number)?.toFloat() ?: 2.0f,
            xpMultiplierHard    = (data["xpMultiplierHard"] as? Number)?.toFloat() ?: 3.0f,
            dailyXpEarningCap   = (data["dailyXpEarningCap"] as? Number)?.toInt() ?: 0,
            dailyXpSpendingCap  = (data["dailyXpSpendingCap"] as? Number)?.toInt() ?: 0,
            updatedAt           = (data["updatedAt"] as? Number)?.toLong() ?: 0L
        )
    }

    private fun mapToXpLoan(data: Map<String, Any>): XpLoan {
        return XpLoan(
            loanId              = data["loanId"] as? String ?: "",
            familyId            = data["familyId"] as? String ?: "",
            parentId            = data["parentId"] as? String ?: "",
            childId             = data["childId"] as? String ?: "",
            childName           = data["childName"] as? String ?: "",
            amount              = (data["amount"] as? Number)?.toInt() ?: 0,
            amountRepaid        = (data["amountRepaid"] as? Number)?.toInt() ?: 0,
            repaymentPercentage = (data["repaymentPercentage"] as? Number)?.toInt() ?: 20,
            status              = try {
                XpLoanStatus.valueOf(data["status"] as? String ?: "ACTIVE")
            } catch (_: Exception) { XpLoanStatus.ACTIVE },
            note                = data["note"] as? String ?: "",
            createdAt           = (data["createdAt"] as? Number)?.toLong() ?: 0L,
            completedAt         = (data["completedAt"] as? Number)?.toLong() ?: 0L,
            updatedAt           = (data["updatedAt"] as? Number)?.toLong() ?: 0L
        )
    }

    companion object {
        private const val TAG = "ParentControlRepo"
    }
}
