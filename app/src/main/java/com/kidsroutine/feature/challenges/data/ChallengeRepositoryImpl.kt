package com.kidsroutine.feature.challenges.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChallengeRepository {

    override suspend fun createChallenge(challenge: ChallengeModel): ChallengeModel {
        try {
            Log.d("ChallengeRepository", "Creating challenge: ${challenge.title}")

            val ref = if (challenge.familyId.isNotEmpty()) {
                firestore
                    .collection("families")
                    .document(challenge.familyId)
                    .collection("challenges")
            } else {
                firestore.collection("challenges")
            }

            ref.document(challenge.challengeId).set(mapOf(
                "challengeId" to challenge.challengeId,
                "title" to challenge.title,
                "description" to challenge.description,
                "type" to challenge.type.name,
                "category" to challenge.category.name,
                "difficulty" to challenge.difficulty.name,
                "duration" to challenge.duration,
                "frequency" to challenge.frequency.name,
                "targetDaysPerWeek" to challenge.targetDaysPerWeek,
                "dailyXpReward" to challenge.dailyXpReward,
                "completionBonusXp" to challenge.completionBonusXp,
                "streakBonusXp" to challenge.streakBonusXp,
                "createdBy" to challenge.createdBy.name,
                "familyId" to challenge.familyId,
                "isCoOp" to challenge.isCoOp,
                "isActive" to challenge.isActive,
                "createdAt" to System.currentTimeMillis()
            )).await()

            Log.d("ChallengeRepository", "Challenge created: ${challenge.challengeId}")
            return challenge
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "Error creating challenge", e)
            throw e
        }
    }

    override suspend fun getChallenge(challengeId: String): ChallengeModel? {
        return try {
            Log.d("ChallengeRepository", "Fetching challenge: $challengeId")

            // Try system challenges first
            var doc = firestore.collection("challenges").document(challengeId).get().await()

            if (!doc.exists()) {
                // Try all family challenges (brute force for now - optimize later with proper querying)
                val familiesSnap = firestore.collection("families").get().await()
                for (familyDoc in familiesSnap.documents) {
                    val familyId = familyDoc.id
                    doc = firestore
                        .collection("families")
                        .document(familyId)
                        .collection("challenges")
                        .document(challengeId)
                        .get()
                        .await()
                    if (doc.exists()) break
                }
            }

            if (doc.exists()) {
                doc.toChallengeModel()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "Error getting challenge", e)
            null
        }
    }

    override suspend fun getFamilyChallenges(familyId: String): List<ChallengeModel> {
        return try {
            Log.d("ChallengeRepository", "Fetching challenges for family: $familyId")

            val snapshot = firestore
                .collection("families")
                .document(familyId)
                .collection("challenges")
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val challenges = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toChallengeModel()
                } catch (e: Exception) {
                    Log.e("ChallengeRepository", "Error parsing challenge", e)
                    null
                }
            }

            Log.d("ChallengeRepository", "Fetched ${challenges.size} family challenges")
            challenges
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "Error fetching family challenges", e)
            emptyList()
        }
    }

    override suspend fun getSystemChallenges(): List<ChallengeModel> {
        return try {
            Log.d("ChallengeRepository", "Fetching system challenges")

            val snapshot = firestore
                .collection("challenges")
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val challenges = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toChallengeModel()
                } catch (e: Exception) {
                    Log.e("ChallengeRepository", "Error parsing challenge", e)
                    null
                }
            }

            Log.d("ChallengeRepository", "Fetched ${challenges.size} system challenges")
            challenges
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "Error fetching system challenges", e)
            emptyList()
        }
    }

    override suspend fun startChallenge(userId: String, challengeId: String): ChallengeProgress {
        return try {
            Log.d("ChallengeRepository", "Starting challenge: $challengeId for user: $userId")

            val today = com.kidsroutine.core.common.util.DateUtils.todayString()

            val progress = ChallengeProgress(
                challengeId = challengeId,
                userId = userId,
                currentDay = 1,
                totalDays = 1,
                completedDays = 0,
                currentStreak = 0,
                successRate = 0f,
                dailyProgress = emptyMap(),
                status = ChallengeStatus.ACTIVE,
                startDate = today,
                endDate = today,
                lastCompletedDate = ""
            )

            firestore
                .collection("users")
                .document(userId)
                .collection("challenge_progress")
                .document(challengeId)
                .set(mapOf(
                    "challengeId" to challengeId,
                    "userId" to userId,
                    "currentDay" to progress.currentDay,
                    "totalDays" to progress.totalDays,
                    "completedDays" to progress.completedDays,
                    "currentStreak" to progress.currentStreak,
                    "successRate" to progress.successRate,
                    "dailyProgress" to progress.dailyProgress,
                    "status" to progress.status.name,
                    "startDate" to progress.startDate,
                    "endDate" to progress.endDate,
                    "lastCompletedDate" to progress.lastCompletedDate
                ))
                .await()

            Log.d("ChallengeRepository", "Challenge started: $challengeId")
            progress
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "Error starting challenge", e)
            throw e
        }
    }

    override suspend fun getActiveChallenges(userId: String): List<ChallengeProgress> {
        return try {
            Log.d("ChallengeRepository", "Fetching active challenges for user: $userId")

            val snapshot = firestore
                .collection("users")
                .document(userId)
                .collection("challenge_progress")
                .whereEqualTo("status", ChallengeStatus.ACTIVE.name)
                .get()
                .await()

            val challenges = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toChallengeProgress()
                } catch (e: Exception) {
                    Log.e("ChallengeRepository", "Error parsing progress", e)
                    null
                }
            }

            Log.d("ChallengeRepository", "Fetched ${challenges.size} active challenges")
            challenges
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "Error fetching active challenges", e)
            emptyList()
        }
    }

    override suspend fun getChallengeProgress(
        userId: String,
        challengeId: String
    ): ChallengeProgress? {
        return try {
            Log.d("ChallengeRepository", "Fetching progress for challenge: $challengeId")

            val doc = firestore
                .collection("users")
                .document(userId)
                .collection("challenge_progress")
                .document(challengeId)
                .get()
                .await()

            if (doc.exists()) {
                doc.toChallengeProgress()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "Error getting challenge progress", e)
            null
        }
    }

    override suspend fun updateChallengeProgress(progress: ChallengeProgress) {
        try {
            Log.d("ChallengeRepository", "Updating challenge progress: ${progress.challengeId}")

            firestore
                .collection("users")
                .document(progress.userId)
                .collection("challenge_progress")
                .document(progress.challengeId)
                .update(mapOf(
                    "currentDay" to progress.currentDay,
                    "totalDays" to progress.totalDays,
                    "completedDays" to progress.completedDays,
                    "currentStreak" to progress.currentStreak,
                    "successRate" to progress.successRate,
                    "dailyProgress" to progress.dailyProgress,
                    "status" to progress.status.name,
                    "lastCompletedDate" to progress.lastCompletedDate
                ))
                .await()

            Log.d("ChallengeRepository", "Challenge progress updated")
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "Error updating challenge progress", e)
            throw e
        }
    }

    override fun observeActiveChallenges(userId: String): Flow<List<ChallengeProgress>> = flow {
        try {
            firestore
                .collection("users")
                .document(userId)
                .collection("challenge_progress")
                .whereEqualTo("status", ChallengeStatus.ACTIVE.name)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChallengeRepository", "Error observing challenges", error)
                        return@addSnapshotListener
                    }

                    val challenges = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toChallengeProgress()
                        } catch (e: Exception) {
                            Log.e("ChallengeRepository", "Error parsing progress", e)
                            null
                        }
                    } ?: emptyList()

                    Log.d("ChallengeRepository", "Emitting ${challenges.size} active challenges")
                }
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "Error observing challenges", e)
        }
    }

    // Helper extensions
    private fun com.google.firebase.firestore.DocumentSnapshot.toChallengeModel(): ChallengeModel? {
        val data = this.data ?: return null
        return ChallengeModel(
            challengeId = data["challengeId"] as? String ?: "",
            title = data["title"] as? String ?: "",
            description = data["description"] as? String ?: "",
            type = try {
                ChallengeType.valueOf(data["type"] as? String ?: "DAILY_HABIT")
            } catch (e: Exception) {
                ChallengeType.DAILY_HABIT
            },
            category = try {
                TaskCategory.valueOf(data["category"] as? String ?: "HEALTH")
            } catch (e: Exception) {
                TaskCategory.HEALTH
            },
            difficulty = try {
                DifficultyLevel.valueOf(data["difficulty"] as? String ?: "EASY")
            } catch (e: Exception) {
                DifficultyLevel.EASY
            },
            duration = (data["duration"] as? Long)?.toInt() ?: 7,
            frequency = try {
                ChallengeFrequency.valueOf(data["frequency"] as? String ?: "DAILY")
            } catch (e: Exception) {
                ChallengeFrequency.DAILY
            },
            targetDaysPerWeek = (data["targetDaysPerWeek"] as? Long)?.toInt() ?: 7,
            dailyXpReward = (data["dailyXpReward"] as? Long)?.toInt() ?: 10,
            completionBonusXp = (data["completionBonusXp"] as? Long)?.toInt() ?: 50,
            streakBonusXp = (data["streakBonusXp"] as? Long)?.toInt() ?: 5,
            createdBy = try {
                TaskCreator.valueOf(data["createdBy"] as? String ?: "SYSTEM")
            } catch (e: Exception) {
                TaskCreator.SYSTEM
            },
            familyId = data["familyId"] as? String ?: "",
            isCoOp = data["isCoOp"] as? Boolean ?: false,
            isActive = data["isActive"] as? Boolean ?: true,
            createdAt = (data["createdAt"] as? Long) ?: 0L
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toChallengeProgress(): ChallengeProgress? {
        val data = this.data ?: return null
        return ChallengeProgress(
            challengeId = data["challengeId"] as? String ?: "",
            userId = data["userId"] as? String ?: "",
            currentDay = (data["currentDay"] as? Long)?.toInt() ?: 1,
            totalDays = (data["totalDays"] as? Long)?.toInt() ?: 1,
            completedDays = (data["completedDays"] as? Long)?.toInt() ?: 0,
            currentStreak = (data["currentStreak"] as? Long)?.toInt() ?: 0,
            successRate = (data["successRate"] as? Double)?.toFloat() ?: 0f,
            dailyProgress = @Suppress("UNCHECKED_CAST")
            (data["dailyProgress"] as? Map<String, Boolean>) ?: emptyMap(),
            status = try {
                ChallengeStatus.valueOf(data["status"] as? String ?: "ACTIVE")
            } catch (e: Exception) {
                ChallengeStatus.ACTIVE
            },
            startDate = data["startDate"] as? String ?: "",
            endDate = data["endDate"] as? String ?: "",
            lastCompletedDate = data["lastCompletedDate"] as? String ?: ""
        )
    }
}