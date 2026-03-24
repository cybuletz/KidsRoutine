package com.kidsroutine.core.model

enum class ContentStatus {
    PENDING,      // Awaiting moderation
    APPROVED,     // Published
    REJECTED,     // Failed moderation
    FLAGGED       // User reported
}

enum class ReportReason {
    INAPPROPRIATE,
    OFFENSIVE,
    SPAM,
    MISLEADING,
    OTHER
}

data class SharedTask(
    val taskId: String = "",
    val title: String = "",
    val description: String = "",
    val category: TaskCategory = TaskCategory.LEARNING,
    val difficulty: DifficultyLevel = DifficultyLevel.EASY,
    val type: TaskType = TaskType.LEARNING,
    val estimatedDurationSec: Int = 300,
    val reward: TaskReward = TaskReward(),

    // Community info
    val createdBy: String = "",          // Parent userId
    val creatorName: String = "",        // Parent displayName
    val familyId: String = "",
    val status: ContentStatus = ContentStatus.PENDING,
    val publishedAt: Long = 0L,
    val usageCount: Int = 0,            // How many families use it

    // Ratings
    val averageRating: Float = 0f,
    val totalRatings: Int = 0,
    val ratingBreakdown: Map<Int, Int> = emptyMap()  // 1-5 star counts
)

data class SharedChallenge(
    val challengeId: String = "",
    val title: String = "",
    val description: String = "",
    val category: TaskCategory = TaskCategory.HEALTH,
    val difficulty: DifficultyLevel = DifficultyLevel.EASY,
    val duration: Int = 7,
    val dailyXpReward: Int = 10,
    val completionBonusXp: Int = 50,
    val streakBonusXp: Int = 5,

    // Community info
    val createdBy: String = "",
    val creatorName: String = "",
    val familyId: String = "",
    val status: ContentStatus = ContentStatus.PENDING,
    val publishedAt: Long = 0L,
    val usageCount: Int = 0,

    // Ratings
    val averageRating: Float = 0f,
    val totalRatings: Int = 0,
    val ratingBreakdown: Map<Int, Int> = emptyMap()
)

data class UserRating(
    val ratingId: String = "",
    val userId: String = "",
    val contentId: String = "",           // taskId or challengeId
    val contentType: String = "",         // "task" or "challenge"
    val rating: Int = 5,                  // 1-5 stars
    val review: String = "",
    val createdAt: Long = 0L
)

data class ContentReport(
    val reportId: String = "report_${System.currentTimeMillis()}_${kotlin.random.Random.nextInt(10000)}",
    val contentId: String = "",
    val contentType: String = "",         // "task" or "challenge"
    val reportedBy: String = "",          // userId
    val reason: ReportReason = ReportReason.OTHER,
    val description: String = "",
    val status: String = "PENDING",       // PENDING, REVIEWED, RESOLVED
    val createdAt: Long = 0L
)

// Leaderboard models
data class ChildLeaderboardEntry(
    val userId: String = "",
    val displayName: String = "",
    val familyId: String = "",
    val avatarUrl: String = "",
    val xp: Int = 0,
    val rank: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val badges: Int = 0
)

data class FamilyLeaderboardEntry(
    val familyId: String = "",
    val familyName: String = "",
    val streak: Int = 0,
    val totalXp: Int = 0,
    val memberCount: Int = 0,
    val rank: Int = 0
)

data class ChallengeLeaderboardEntry(
    val challengeId: String = "",
    val title: String = "",
    val completedByCount: Int = 0,
    val averageCompletionDays: Float = 0f,
    val rank: Int = 0
)