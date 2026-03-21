package com.kidsroutine.feature.community.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kidsroutine.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommunityRepository {

    // ═══════════════════════════════════════════════════════════════════════
    // TASK MARKETPLACE
    // ═══════════════════════════════════════════════════════════════════════

    override suspend fun publishTask(task: SharedTask): SharedTask {
        return try {
            Log.d("CommunityRepo", "Publishing task: ${task.title}")

            firestore.collection("marketplace")
                .document("tasks")
                .collection("shared")
                .document(task.taskId)
                .set(mapOf(
                    "taskId" to task.taskId,
                    "title" to task.title,
                    "description" to task.description,
                    "category" to task.category.name,
                    "difficulty" to task.difficulty.name,
                    "type" to task.type.name,
                    "estimatedDurationSec" to task.estimatedDurationSec,
                    "reward" to mapOf(
                        "xp" to task.reward.xp,
                        "bonusConditions" to task.reward.bonusConditions
                    ),
                    "createdBy" to task.createdBy,
                    "creatorName" to task.creatorName,
                    "familyId" to task.familyId,
                    "status" to task.status.name,
                    "publishedAt" to System.currentTimeMillis(),
                    "usageCount" to 0,
                    "averageRating" to 0f,
                    "totalRatings" to 0,
                    "ratingBreakdown" to emptyMap<String, Any>()
                ))
                .await()

            Log.d("CommunityRepo", "Task published successfully")
            task.copy(publishedAt = System.currentTimeMillis(), status = ContentStatus.PENDING)
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error publishing task", e)
            throw e
        }
    }

    override suspend fun getPendingTasks(limit: Int): List<SharedTask> {
        return try {
            Log.d("CommunityRepo", "Fetching pending tasks")

            val snapshot = firestore.collection("marketplace")
                .document("tasks")
                .collection("shared")
                .whereEqualTo("status", ContentStatus.PENDING.name)
                .orderBy("publishedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toSharedTask()
                } catch (e: Exception) {
                    Log.e("CommunityRepo", "Error parsing task", e)
                    null
                }
            }

            Log.d("CommunityRepo", "Fetched ${tasks.size} pending tasks")
            tasks
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error getting pending tasks", e)
            emptyList()
        }
    }

    override suspend fun getPendingChallenges(limit: Int): List<SharedChallenge> {
        return try {
            Log.d("CommunityRepo", "Fetching pending challenges")

            val snapshot = firestore.collection("marketplace")
                .document("challenges")
                .collection("shared")
                .whereEqualTo("status", ContentStatus.PENDING.name)
                .orderBy("publishedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val challenges = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toSharedChallenge()
                } catch (e: Exception) {
                    Log.e("CommunityRepo", "Error parsing challenge", e)
                    null
                }
            }

            Log.d("CommunityRepo", "Fetched ${challenges.size} pending challenges")
            challenges
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error getting pending challenges", e)
            emptyList()
        }
    }

    override suspend fun getApprovedTasks(
        category: TaskCategory?,
        difficulty: DifficultyLevel?,
        limit: Int
    ): List<SharedTask> {
        return try {
            Log.d("CommunityRepo", "Fetching approved tasks")

            var query: Query = firestore.collection("marketplace")
                .document("tasks")
                .collection("shared")
                .whereEqualTo("status", ContentStatus.APPROVED.name)
                .orderBy("usageCount", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (category != null) {
                query = query.whereEqualTo("category", category.name)
            }

            if (difficulty != null) {
                query = query.whereEqualTo("difficulty", difficulty.name)
            }

            val snapshot = query.get().await()
            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toSharedTask()
                } catch (e: Exception) {
                    Log.e("CommunityRepo", "Error parsing task", e)
                    null
                }
            }

            Log.d("CommunityRepo", "Fetched ${tasks.size} approved tasks")
            tasks
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error getting approved tasks", e)
            emptyList()
        }
    }

    override suspend fun getTaskById(taskId: String): SharedTask? {
        return try {
            Log.d("CommunityRepo", "Fetching task: $taskId")

            val doc = firestore.collection("marketplace")
                .document("tasks")
                .collection("shared")
                .document(taskId)
                .get()
                .await()

            if (doc.exists()) {
                doc.toSharedTask()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error getting task", e)
            null
        }
    }

    override suspend fun importTask(userId: String, taskId: String): TaskTemplate {
        return try {
            Log.d("CommunityRepo", "Importing task: $taskId for user: $userId")

            val sharedTask = getTaskById(taskId) ?: throw Exception("Task not found")

            val taskTemplate = TaskTemplate(
                templateId = "imported_$taskId",
                familyId = userId,
                generationParams = emptyMap(),
                baseTask = TaskModel(
                    id = taskId,
                    title = sharedTask.title,
                    description = sharedTask.description,
                    category = sharedTask.category,
                    difficulty = sharedTask.difficulty,
                    type = sharedTask.type,
                    estimatedDurationSec = sharedTask.estimatedDurationSec,
                    reward = sharedTask.reward
                )
            )

            // Save to user's templates
            firestore.collection("families")
                .document(userId)
                .collection("task_templates")
                .document(taskTemplate.templateId)
                .set(taskTemplate)
                .await()

            // Increment usage count
            firestore.collection("marketplace")
                .document("tasks")
                .collection("shared")
                .document(taskId)
                .update("usageCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()

            Log.d("CommunityRepo", "Task imported successfully")
            taskTemplate
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error importing task", e)
            throw e
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CHALLENGE SHARING
    // ═══════════════════════════════════════════════════════════════════════

    override suspend fun publishChallenge(challenge: SharedChallenge): SharedChallenge {
        return try {
            Log.d("CommunityRepo", "Publishing challenge: ${challenge.title}")

            firestore.collection("marketplace")
                .document("challenges")
                .collection("shared")
                .document(challenge.challengeId)
                .set(mapOf(
                    "challengeId" to challenge.challengeId,
                    "title" to challenge.title,
                    "description" to challenge.description,
                    "category" to challenge.category.name,
                    "difficulty" to challenge.difficulty.name,
                    "duration" to challenge.duration,
                    "dailyXpReward" to challenge.dailyXpReward,
                    "completionBonusXp" to challenge.completionBonusXp,
                    "streakBonusXp" to challenge.streakBonusXp,
                    "createdBy" to challenge.createdBy,
                    "creatorName" to challenge.creatorName,
                    "familyId" to challenge.familyId,
                    "status" to challenge.status.name,
                    "publishedAt" to System.currentTimeMillis(),
                    "usageCount" to 0,
                    "averageRating" to 0f,
                    "totalRatings" to 0,
                    "ratingBreakdown" to emptyMap<String, Any>()
                ))
                .await()

            Log.d("CommunityRepo", "Challenge published successfully")
            challenge.copy(publishedAt = System.currentTimeMillis(), status = ContentStatus.PENDING)
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error publishing challenge", e)
            throw e
        }
    }

    override suspend fun getApprovedChallenges(
        category: TaskCategory?,
        difficulty: DifficultyLevel?,
        limit: Int
    ): List<SharedChallenge> {
        return try {
            Log.d("CommunityRepo", "Fetching approved challenges")

            var query: Query = firestore.collection("marketplace")
                .document("challenges")
                .collection("shared")
                .whereEqualTo("status", ContentStatus.APPROVED.name)
                .orderBy("usageCount", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (category != null) {
                query = query.whereEqualTo("category", category.name)
            }

            if (difficulty != null) {
                query = query.whereEqualTo("difficulty", difficulty.name)
            }

            val snapshot = query.get().await()
            val challenges = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toSharedChallenge()
                } catch (e: Exception) {
                    Log.e("CommunityRepo", "Error parsing challenge", e)
                    null
                }
            }

            Log.d("CommunityRepo", "Fetched ${challenges.size} approved challenges")
            challenges
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error getting approved challenges", e)
            emptyList()
        }
    }

    override suspend fun getChallengeById(challengeId: String): SharedChallenge? {
        return try {
            Log.d("CommunityRepo", "Fetching challenge: $challengeId")

            val doc = firestore.collection("marketplace")
                .document("challenges")
                .collection("shared")
                .document(challengeId)
                .get()
                .await()

            if (doc.exists()) {
                doc.toSharedChallenge()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error getting challenge", e)
            null
        }
    }

    override suspend fun importChallenge(userId: String, challengeId: String): ChallengeModel {
        return try {
            Log.d("CommunityRepo", "Importing challenge: $challengeId for user: $userId")

            val sharedChallenge = getChallengeById(challengeId) ?: throw Exception("Challenge not found")

            val challengeModel = ChallengeModel(
                challengeId = "imported_$challengeId",
                title = sharedChallenge.title,
                description = sharedChallenge.description,
                category = sharedChallenge.category,
                difficulty = sharedChallenge.difficulty,
                duration = sharedChallenge.duration,
                dailyXpReward = sharedChallenge.dailyXpReward,
                completionBonusXp = sharedChallenge.completionBonusXp,
                streakBonusXp = sharedChallenge.streakBonusXp,
                createdBy = TaskCreator.PARENT,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )

            // Save to family challenges
            firestore.collection("families")
                .document(userId)
                .collection("challenges")
                .document(challengeModel.challengeId)
                .set(challengeModel)
                .await()

            // Increment usage count
            firestore.collection("marketplace")
                .document("challenges")
                .collection("shared")
                .document(challengeId)
                .update("usageCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()

            Log.d("CommunityRepo", "Challenge imported successfully")
            challengeModel
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error importing challenge", e)
            throw e
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RATINGS
    // ═══════════════════════════════════════════════════════════════════════

    override suspend fun rateContent(rating: UserRating): UserRating {
        return try {
            Log.d("CommunityRepo", "Rating content: ${rating.contentId} with ${rating.rating} stars")

            val ratingId = firestore.collection("ratings").document().id
            val ratingWithId = rating.copy(
                ratingId = ratingId,
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("ratings")
                .document(ratingId)
                .set(mapOf(
                    "ratingId" to ratingWithId.ratingId,
                    "userId" to ratingWithId.userId,
                    "contentId" to ratingWithId.contentId,
                    "contentType" to ratingWithId.contentType,
                    "rating" to ratingWithId.rating,
                    "review" to ratingWithId.review,
                    "createdAt" to ratingWithId.createdAt
                ))
                .await()

            // Update content rating stats
            updateContentRatingStats(rating.contentId, rating.contentType, rating.rating)

            Log.d("CommunityRepo", "Rating saved successfully")
            ratingWithId
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error rating content", e)
            throw e
        }
    }

    override suspend fun getUserRating(userId: String, contentId: String): UserRating? {
        return try {
            val snapshot = firestore.collection("ratings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("contentId", contentId)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                snapshot.documents[0].toUserRating()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error getting user rating", e)
            null
        }
    }

    override suspend fun getContentRatings(contentId: String, limit: Int): List<UserRating> {
        return try {
            val snapshot = firestore.collection("ratings")
                .whereEqualTo("contentId", contentId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toUserRating()
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error getting content ratings", e)
            emptyList()
        }
    }

    private suspend fun updateContentRatingStats(contentId: String, contentType: String, newRating: Int) {
        try {
            val collection = if (contentType == "task") "marketplace/tasks/shared" else "marketplace/challenges/shared"
            val doc = firestore.collection(collection).document(contentId).get().await()

            if (doc.exists()) {
                val currentRating = doc.getDouble("averageRating") ?: 0.0
                val totalRatings = doc.getLong("totalRatings")?.toInt() ?: 0
                val breakdown = @Suppress("UNCHECKED_CAST")
                (doc.get("ratingBreakdown") as? Map<String, Long>) ?: emptyMap()

                val newTotal = totalRatings + 1
                val newAverage = ((currentRating * totalRatings) + newRating) / newTotal
                val newBreakdown = breakdown.toMutableMap()
                newBreakdown[newRating.toString()] = (newBreakdown[newRating.toString()] ?: 0L) + 1L

                firestore.collection(collection)
                    .document(contentId)
                    .update(mapOf(
                        "averageRating" to newAverage,
                        "totalRatings" to newTotal,
                        "ratingBreakdown" to newBreakdown
                    ))
                    .await()
            }
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error updating rating stats", e)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REPORTING
    // ═══════════════════════════════════════════════════════════════════════

    override suspend fun reportContent(report: ContentReport): ContentReport {
        return try {
            Log.d("CommunityRepo", "Reporting content: ${report.contentId}")

            val reportData = mapOf(
                "reportId" to report.reportId,
                "contentId" to report.contentId,
                "contentType" to report.contentType,
                "reportedBy" to report.reportedBy,
                "reason" to report.reason.name,
                "description" to report.description,
                "status" to report.status,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("reports")
                .document(report.reportId)
                .set(reportData)
                .await()

            Log.d("CommunityRepo", "Report saved: ${report.reportId}")
            report
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error reporting content", e)
            throw e
        }
    }

    override suspend fun getPendingReports(limit: Int): List<ContentReport> {
        return try {
            Log.d("CommunityRepo", "Fetching pending reports")

            val snapshot = firestore
                .collection("reports")
                .whereEqualTo("status", "PENDING")
                .limit(limit.toLong())
                .get()
                .await()

            val reports = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toContentReport()
                } catch (e: Exception) {
                    Log.e("CommunityRepo", "Error parsing report", e)
                    null
                }
            }.sortedByDescending { it.createdAt }

            Log.d("CommunityRepo", "Fetched ${reports.size} pending reports")
            reports
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error getting pending reports", e)
            emptyList()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // LEADERBOARDS
    // ═══════════════════════════════════════════════════════════════════════

    override suspend fun getChildLeaderboard(limit: Int): List<ChildLeaderboardEntry> {
        return try {
            Log.d("CommunityRepo", "Fetching child leaderboard")

            val snapshot = firestore.collection("users")
                .orderBy("xp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapIndexed { index, doc ->
                ChildLeaderboardEntry(
                    userId = doc.getString("userId") ?: "",
                    displayName = doc.getString("displayName") ?: "Unknown",
                    familyId = doc.getString("familyId") ?: "",
                    avatarUrl = doc.getString("avatarUrl") ?: "",
                    xp = doc.getLong("xp")?.toInt() ?: 0,
                    rank = index + 1
                )
            }
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error getting child leaderboard", e)
            emptyList()
        }
    }

    override suspend fun getFamilyLeaderboard(limit: Int): List<FamilyLeaderboardEntry> {
        return try {
            Log.d("CommunityRepo", "Fetching family leaderboard")

            val snapshot = firestore.collection("families")
                .orderBy("familyStreak", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapIndexed { index, doc ->
                FamilyLeaderboardEntry(
                    familyId = doc.getString("familyId") ?: "",
                    familyName = doc.getString("familyName") ?: "Unknown Family",
                    streak = doc.getLong("familyStreak")?.toInt() ?: 0,
                    totalXp = doc.getLong("familyXp")?.toInt() ?: 0,
                    memberCount = (doc.get("memberIds") as? List<*>)?.size ?: 0,
                    rank = index + 1
                )
            }
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error getting family leaderboard", e)
            emptyList()
        }
    }

    override suspend fun getWeeklyFamilyLeaderboard(familyId: String): FamilyLeaderboard {
        return try {
            Log.d("LeaderboardRepository", "Getting weekly leaderboard for family: $familyId")

            val weekString = getWeekString()

            // Get family data
            val familyDoc = firestore.collection("families").document(familyId).get().await()
            val memberIds = (familyDoc.data?.get("memberIds") as? List<*>)?.map { it.toString() } ?: emptyList()

            // Get all members' XP
            val entries = mutableListOf<LeaderboardEntry>()
            for ((index, memberId) in memberIds.withIndex()) {
                val userDoc = firestore.collection("users").document(memberId).get().await()
                val userData = userDoc.data ?: continue

                val xp = (userData["xp"] as? Number)?.toInt() ?: 0
                val level = (userData["level"] as? Number)?.toInt() ?: 1
                val displayName = userData["displayName"] as? String ?: "Unknown"
                val avatarUrl = userData["avatarUrl"] as? String ?: ""
                val badges = ((userData["badges"] as? List<*>) ?: emptyList()).size

                entries.add(
                    LeaderboardEntry(
                        rank = index + 1,
                        userId = memberId,
                        displayName = displayName,
                        avatarUrl = avatarUrl,
                        xp = xp,
                        level = level,
                        weeklyXp = xp,
                        badges = badges
                    )
                )
            }

            // Sort by XP (descending)
            entries.sortByDescending { it.xp }

            // Rebuild ranks after sorting
            val rankedEntries = entries.mapIndexed { index, entry ->
                entry.copy(rank = index + 1)
            }

            val leaderboard = FamilyLeaderboard(
                familyId = familyId,
                week = getWeekString(),
                entries = rankedEntries  // ← USE rankedEntries HERE
            )

            Log.d("LeaderboardRepository", "Leaderboard ready with ${entries.size} members")
            leaderboard
        } catch (e: Exception) {
            Log.e("LeaderboardRepository", "Error getting leaderboard", e)
            FamilyLeaderboard(familyId = familyId)
        }
    }

    override fun observeWeeklyFamilyLeaderboard(familyId: String): Flow<FamilyLeaderboard> = flow {
        try {
            Log.d("LeaderboardRepository", "Observing weekly leaderboard for family: $familyId")

            firestore.collection("families").document(familyId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("LeaderboardRepository", "Error observing leaderboard", error)
                        return@addSnapshotListener
                    }

                    snapshot?.data?.let { data ->
                        val memberIds = (data["memberIds"] as? List<*>)?.map { it.toString() } ?: emptyList()

                        // This would need to be async, so in practice you'd use a coroutine here
                        // For now, we'll emit the structure
                        Log.d("LeaderboardRepository", "Leaderboard updated with ${memberIds.size} members")
                    }
                }
        } catch (e: Exception) {
            Log.e("LeaderboardRepository", "Error observing leaderboard", e)
        }
    }

    override suspend fun getChallengeLeaderboard(limit: Int): List<ChallengeLeaderboardEntry> {
        return try {
            Log.d("CommunityRepo", "Fetching challenge leaderboard")

            val snapshot = firestore.collectionGroup("challenge_progress")
                .whereEqualTo("status", "COMPLETED")
                .get()
                .await()

            // Group by challenge and count completions
            val challengeStats = mutableMapOf<String, Pair<Int, MutableList<Int>>>()

            for (doc in snapshot.documents) {
                val challengeId = doc.getString("challengeId") ?: continue
                val totalDays = doc.getLong("totalDays")?.toInt() ?: 0

                challengeStats.getOrPut(challengeId) { Pair(0, mutableListOf()) }.let {
                    it.second.add(totalDays)
                    challengeStats[challengeId] = Pair(it.first + 1, it.second)
                }
            }

            // Get challenge details and create leaderboard entries
            val entries = challengeStats.entries.mapNotNull { (challengeId, stats) ->
                try {
                    val doc = firestore.collectionGroup("challenges")
                        .whereEqualTo("challengeId", challengeId)
                        .limit(1)
                        .get()
                        .await()
                        .documents
                        .firstOrNull() ?: return@mapNotNull null

                    val title = doc.getString("title") ?: "Unknown Challenge"
                    val completedCount = stats.first
                    val averageDays = if (stats.second.isNotEmpty()) {
                        stats.second.average().toFloat()
                    } else {
                        0f
                    }

                    ChallengeLeaderboardEntry(
                        challengeId = challengeId,
                        title = title,
                        completedByCount = completedCount,
                        averageCompletionDays = averageDays,
                        rank = 0
                    )
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.completedByCount }
                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
                .take(limit)

            Log.d("CommunityRepo", "Fetched ${entries.size} challenges")
            entries
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error getting challenge leaderboard", e)
            emptyList()
        }
    }

    override fun observeChildLeaderboard(limit: Int): Flow<List<ChildLeaderboardEntry>> = flow {
        try {
            firestore.collection("users")
                .orderBy("xp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("CommunityRepo", "Error observing child leaderboard", error)
                        return@addSnapshotListener
                    }

                    val entries = snapshot?.documents?.mapIndexed { index, doc ->
                        ChildLeaderboardEntry(
                            userId = doc.getString("userId") ?: "",
                            displayName = doc.getString("displayName") ?: "Unknown",
                            familyId = doc.getString("familyId") ?: "",
                            avatarUrl = doc.getString("avatarUrl") ?: "",
                            xp = doc.getLong("xp")?.toInt() ?: 0,
                            rank = index + 1
                        )
                    } ?: emptyList()
                }
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error observing child leaderboard", e)
        }
    }

    override fun observeFamilyLeaderboard(limit: Int): Flow<List<FamilyLeaderboardEntry>> = flow {
        try {
            firestore.collection("families")
                .orderBy("familyStreak", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("CommunityRepo", "Error observing family leaderboard", error)
                        return@addSnapshotListener
                    }

                    val entries = snapshot?.documents?.mapIndexed { index, doc ->
                        FamilyLeaderboardEntry(
                            familyId = doc.getString("familyId") ?: "",
                            familyName = doc.getString("familyName") ?: "Unknown Family",
                            streak = doc.getLong("familyStreak")?.toInt() ?: 0,
                            totalXp = doc.getLong("familyXp")?.toInt() ?: 0,
                            memberCount = (doc.get("memberIds") as? List<*>)?.size ?: 0,
                            rank = index + 1
                        )
                    } ?: emptyList()
                }
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error observing family leaderboard", e)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPER EXTENSIONS
    // ═══════════════════════════════════════════════════════════════════════

    private fun com.google.firebase.firestore.DocumentSnapshot.toSharedTask(): SharedTask? {
        val data = this.data ?: return null
        return SharedTask(
            taskId = data["taskId"] as? String ?: "",
            title = data["title"] as? String ?: "",
            description = data["description"] as? String ?: "",
            category = try {
                TaskCategory.valueOf(data["category"] as? String ?: "LEARNING")
            } catch (e: Exception) {
                TaskCategory.LEARNING
            },
            difficulty = try {
                DifficultyLevel.valueOf(data["difficulty"] as? String ?: "EASY")
            } catch (e: Exception) {
                DifficultyLevel.EASY
            },
            type = try {
                TaskType.valueOf(data["type"] as? String ?: "LEARNING")
            } catch (e: Exception) {
                TaskType.LEARNING
            },
            estimatedDurationSec = (data["estimatedDurationSec"] as? Long)?.toInt() ?: 300,
            reward = TaskReward(
                xp = ((data["reward"] as? Map<*, *>)?.get("xp") as? Long)?.toInt() ?: 10
            ),
            createdBy = data["createdBy"] as? String ?: "",
            creatorName = data["creatorName"] as? String ?: "",
            familyId = data["familyId"] as? String ?: "",
            status = try {
                ContentStatus.valueOf(data["status"] as? String ?: "PENDING")
            } catch (e: Exception) {
                ContentStatus.PENDING
            },
            publishedAt = data["publishedAt"] as? Long ?: 0L,
            usageCount = (data["usageCount"] as? Long)?.toInt() ?: 0,
            averageRating = (data["averageRating"] as? Double)?.toFloat() ?: 0f,
            totalRatings = (data["totalRatings"] as? Long)?.toInt() ?: 0
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toSharedChallenge(): SharedChallenge? {
        val data = this.data ?: return null
        return SharedChallenge(
            challengeId = data["challengeId"] as? String ?: "",
            title = data["title"] as? String ?: "",
            description = data["description"] as? String ?: "",
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
            dailyXpReward = (data["dailyXpReward"] as? Long)?.toInt() ?: 10,
            completionBonusXp = (data["completionBonusXp"] as? Long)?.toInt() ?: 50,
            streakBonusXp = (data["streakBonusXp"] as? Long)?.toInt() ?: 5,
            createdBy = data["createdBy"] as? String ?: "",
            creatorName = data["creatorName"] as? String ?: "",
            familyId = data["familyId"] as? String ?: "",
            status = try {
                ContentStatus.valueOf(data["status"] as? String ?: "PENDING")
            } catch (e: Exception) {
                ContentStatus.PENDING
            },
            publishedAt = data["publishedAt"] as? Long ?: 0L,
            usageCount = (data["usageCount"] as? Long)?.toInt() ?: 0,
            averageRating = (data["averageRating"] as? Double)?.toFloat() ?: 0f,
            totalRatings = (data["totalRatings"] as? Long)?.toInt() ?: 0
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toUserRating(): UserRating? {
        val data = this.data ?: return null
        return UserRating(
            ratingId = data["ratingId"] as? String ?: "",
            userId = data["userId"] as? String ?: "",
            contentId = data["contentId"] as? String ?: "",
            contentType = data["contentType"] as? String ?: "",
            rating = (data["rating"] as? Long)?.toInt() ?: 5,
            review = data["review"] as? String ?: "",
            createdAt = data["createdAt"] as? Long ?: 0L
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toContentReport(): ContentReport? {
        val data = this.data ?: return null
        return ContentReport(
            reportId = data["reportId"] as? String ?: "",
            contentId = data["contentId"] as? String ?: "",
            contentType = data["contentType"] as? String ?: "",
            reportedBy = data["reportedBy"] as? String ?: "",
            reason = try {
                ReportReason.valueOf(data["reason"] as? String ?: "OTHER")
            } catch (e: Exception) {
                ReportReason.OTHER
            },
            description = data["description"] as? String ?: "",
            status = data["status"] as? String ?: "PENDING",
            createdAt = data["createdAt"] as? Long ?: 0L
        )
    }

    override suspend fun approveTask(taskId: String) {
        try {
            firestore.collection("marketplace")
                .document("tasks")
                .collection("shared")
                .document(taskId)
                .update(mapOf(
                    "status" to ContentStatus.APPROVED.name
                ))
                .await()
            Log.d("CommunityRepo", "Task approved: $taskId")
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error approving task", e)
            throw e
        }
    }

    override suspend fun rejectTask(taskId: String) {
        try {
            firestore.collection("marketplace")
                .document("tasks")
                .collection("shared")
                .document(taskId)
                .update(mapOf(
                    "status" to ContentStatus.REJECTED.name
                ))
                .await()
            Log.d("CommunityRepo", "Task rejected: $taskId")
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error rejecting task", e)
            throw e
        }
    }

    override suspend fun approveChallenge(challengeId: String) {
        try {
            firestore.collection("marketplace")
                .document("challenges")
                .collection("shared")
                .document(challengeId)
                .update(mapOf(
                    "status" to ContentStatus.APPROVED.name
                ))
                .await()
            Log.d("CommunityRepo", "Challenge approved: $challengeId")
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error approving challenge", e)
            throw e
        }
    }

    override suspend fun rejectChallenge(challengeId: String) {
        try {
            firestore.collection("marketplace")
                .document("challenges")
                .collection("shared")
                .document(challengeId)
                .update(mapOf(
                    "status" to ContentStatus.REJECTED.name
                ))
                .await()
            Log.d("CommunityRepo", "Challenge rejected: $challengeId")
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Error rejecting challenge", e)
            throw e
        }
    }

    override suspend fun resolveReport(reportId: String, status: String) {
        try {
            Log.d("CommunityRepo", "About to resolve report: $reportId with status: $status")

            firestore.collection("reports")
                .document(reportId)
                .update(mapOf(
                    "status" to status
                ))
                .await()

            Log.d("CommunityRepo", "✅ Report resolved successfully: $reportId → $status")
        } catch (e: Exception) {
            Log.e("CommunityRepo", "❌ Error resolving report: ${e.message}", e)
            throw e
        }
    }

    private fun getWeekString(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val week = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
        return String.format("%d-W%02d", year, week)
    }
}