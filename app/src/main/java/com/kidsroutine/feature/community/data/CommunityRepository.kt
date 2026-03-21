package com.kidsroutine.feature.community.data

import com.kidsroutine.core.model.*
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {

    // ═══════════════════════════════════════════════════════════════════════
    // TASK MARKETPLACE
    // ═══════════════════════════════════════════════════════════════════════
    suspend fun publishTask(task: SharedTask): SharedTask
    suspend fun getApprovedTasks(category: TaskCategory? = null, difficulty: DifficultyLevel? = null, limit: Int = 50): List<SharedTask>
    suspend fun getPendingTasks(limit: Int = 100): List<SharedTask>  // ADD THIS
    suspend fun getTaskById(taskId: String): SharedTask?
    suspend fun importTask(userId: String, taskId: String): TaskTemplate

    // ═══════════════════════════════════════════════════════════════════════
    // CHALLENGE SHARING
    // ═══════════════════════════════════════════════════════════════════════
    suspend fun publishChallenge(challenge: SharedChallenge): SharedChallenge
    suspend fun getApprovedChallenges(category: TaskCategory? = null, difficulty: DifficultyLevel? = null, limit: Int = 50): List<SharedChallenge>
    suspend fun getPendingChallenges(limit: Int = 100): List<SharedChallenge>  // ADD THIS
    suspend fun getChallengeById(challengeId: String): SharedChallenge?
    suspend fun importChallenge(userId: String, challengeId: String): ChallengeModel

    // ═══════════════════════════════════════════════════════════════════════
    // MODERATION
    // ═══════════════════════════════════════════════════════════════════════
    suspend fun approveTask(taskId: String)  // ADD THIS
    suspend fun rejectTask(taskId: String)  // ADD THIS
    suspend fun approveChallenge(challengeId: String)  // ADD THIS
    suspend fun rejectChallenge(challengeId: String)  // ADD THIS
    suspend fun resolveReport(reportId: String, status: String)  // ADD THIS

    // ═══════════════════════════════════════════════════════════════════════
    // RATINGS
    // ═══════════════════════════════════════════════════════════════════════
    suspend fun rateContent(rating: UserRating): UserRating
    suspend fun getUserRating(userId: String, contentId: String): UserRating?
    suspend fun getContentRatings(contentId: String, limit: Int = 10): List<UserRating>

    // ═══════════════════════════════════════════════════════════════════════
    // REPORTING
    // ═══════════════════════════════════════════════════════════════════════
    suspend fun reportContent(report: ContentReport): ContentReport
    suspend fun getPendingReports(limit: Int = 100): List<ContentReport>

    // ═══════════════════════════════════════════════════════════════════════
    // LEADERBOARDS
    // ═══════════════════════════════════════════════════════════════════════
    suspend fun getChildLeaderboard(limit: Int = 100): List<ChildLeaderboardEntry>
    suspend fun getFamilyLeaderboard(limit: Int = 100): List<FamilyLeaderboardEntry>
    suspend fun getChallengeLeaderboard(limit: Int = 100): List<ChallengeLeaderboardEntry>
    fun observeChildLeaderboard(limit: Int = 100): Flow<List<ChildLeaderboardEntry>>
    fun observeFamilyLeaderboard(limit: Int = 100): Flow<List<FamilyLeaderboardEntry>>
}