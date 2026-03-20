package com.kidsroutine.core.model

enum class ChallengeType {
    DAILY_HABIT,           // "Drink water 3 times daily"
    TIME_BOUND,            // "7-day early sleep challenge"
    RESTRICTION,           // "No screens after 8 PM"
    STREAK_BASED           // "30-day consistency challenge"
}

enum class ChallengeFrequency {
    DAILY,
    CUSTOM_DAYS  // specific days like Mon/Wed/Fri
}

enum class ChallengeStatus {
    ACTIVE,
    COMPLETED,
    FAILED,
    PAUSED,
    ARCHIVED
}

data class ChallengeSuccessCondition(
    val type: String,  // "BEFORE_TIME", "COUNT", "BOOLEAN", "DURATION"
    val value: Any,    // 21:00, 3, true, 600 (seconds)
    val unit: String = ""  // "time", "count", "seconds"
)

data class ChallengeProgress(
    val challengeId: String = "",
    val userId: String = "",
    val currentDay: Int = 1,
    val totalDays: Int = 1,
    val completedDays: Int = 0,
    val currentStreak: Int = 0,
    val successRate: Float = 0f,
    val dailyProgress: Map<String, Boolean> = emptyMap(),  // "2024-03-20" -> true/false
    val status: ChallengeStatus = ChallengeStatus.ACTIVE,
    val startDate: String = "",
    val endDate: String = "",
    val lastCompletedDate: String = ""
)

data class ChallengeModel(
    val challengeId: String = "",
    val title: String = "",
    val description: String = "",
    val type: ChallengeType = ChallengeType.DAILY_HABIT,
    val category: TaskCategory = TaskCategory.HEALTH,
    val difficulty: DifficultyLevel = DifficultyLevel.EASY,

    // Duration
    val duration: Int = 7,  // days
    val frequency: ChallengeFrequency = ChallengeFrequency.DAILY,
    val targetDaysPerWeek: Int = 7,

    // Success definition
    val successCondition: ChallengeSuccessCondition = ChallengeSuccessCondition("BOOLEAN", true),
    val validationType: ValidationType = ValidationType.SELF,

    // Rewards
    val dailyXpReward: Int = 10,
    val completionBonusXp: Int = 50,
    val streakBonusXp: Int = 5,  // per day

    // Metadata
    val createdBy: TaskCreator = TaskCreator.SYSTEM,
    val familyId: String = "",
    val parentId: String = "",  // if created by parent
    val childId: String = "",   // if assigned to specific child
    val isCoOp: Boolean = false,
    val relatedTaskId: String = "",  // injects this task daily

    // Injected task template
    val dailyTaskTemplate: TaskModel = TaskModel(),

    // Status
    val isActive: Boolean = true,
    val createdAt: Long = 0L
)