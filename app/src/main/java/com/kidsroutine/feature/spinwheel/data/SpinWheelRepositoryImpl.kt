package com.kidsroutine.feature.spinwheel.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.DailySpinState
import com.kidsroutine.core.model.SpinRewardType
import com.kidsroutine.core.model.SpinWheelResult
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpinWheelRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SpinWheelRepository {

    override suspend fun getDailyState(userId: String, date: String): DailySpinState? {
        return try {
            val docId = "${userId}_$date"
            val snapshot = firestore
                .collection(COLLECTION_DAILY_SPINS)
                .document(docId)
                .get()
                .await()

            if (!snapshot.exists()) return null

            val resultsSnapshot = firestore
                .collection(COLLECTION_DAILY_SPINS)
                .document(docId)
                .collection(SUBCOLLECTION_SPIN_HISTORY)
                .get()
                .await()

            val results = resultsSnapshot.documents.mapNotNull { doc ->
                try {
                    SpinWheelResult(
                        resultId = doc.getString("resultId") ?: "",
                        userId = doc.getString("userId") ?: "",
                        reward = runCatching {
                            SpinRewardType.valueOf(doc.getString("reward") ?: "NOTHING")
                        }.getOrDefault(SpinRewardType.NOTHING),
                        date = doc.getString("date") ?: "",
                        spinNumber = (doc.getLong("spinNumber") ?: 1L).toInt(),
                        claimedAt = doc.getLong("claimedAt") ?: 0L
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse spin result: ${doc.id}", e)
                    null
                }
            }

            DailySpinState(
                userId = snapshot.getString("userId") ?: userId,
                date = snapshot.getString("date") ?: date,
                spinsUsed = (snapshot.getLong("spinsUsed") ?: 0L).toInt(),
                maxSpins = (snapshot.getLong("maxSpins") ?: 1L).toInt(),
                results = results,
                hasDoubleXpActive = snapshot.getBoolean("hasDoubleXpActive") ?: false,
                doubleXpExpiresAt = snapshot.getLong("doubleXpExpiresAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading daily spin state for $userId on $date", e)
            null
        }
    }

    override suspend fun saveDailyState(state: DailySpinState) {
        try {
            val docId = "${state.userId}_${state.date}"
            firestore
                .collection(COLLECTION_DAILY_SPINS)
                .document(docId)
                .set(
                    mapOf(
                        "userId" to state.userId,
                        "date" to state.date,
                        "spinsUsed" to state.spinsUsed,
                        "maxSpins" to state.maxSpins,
                        "hasDoubleXpActive" to state.hasDoubleXpActive,
                        "doubleXpExpiresAt" to state.doubleXpExpiresAt
                    )
                )
                .await()
            Log.d(TAG, "Saved daily spin state: $docId")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving daily spin state", e)
            throw e
        }
    }

    override suspend fun saveSpinResult(userId: String, result: SpinWheelResult) {
        try {
            val docId = "${userId}_${result.date}"
            firestore
                .collection(COLLECTION_DAILY_SPINS)
                .document(docId)
                .collection(SUBCOLLECTION_SPIN_HISTORY)
                .document(result.resultId)
                .set(
                    mapOf(
                        "resultId" to result.resultId,
                        "userId" to result.userId,
                        "reward" to result.reward.name,
                        "date" to result.date,
                        "spinNumber" to result.spinNumber,
                        "claimedAt" to result.claimedAt
                    )
                )
                .await()
            Log.d(TAG, "Saved spin result: ${result.resultId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving spin result", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "SpinWheelRepository"
        private const val COLLECTION_DAILY_SPINS = "daily_spins"
        private const val SUBCOLLECTION_SPIN_HISTORY = "spin_history"
    }
}
