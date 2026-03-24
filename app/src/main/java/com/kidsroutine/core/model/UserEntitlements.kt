package com.kidsroutine.core.model

/**
 * Represents what a user is entitled to based on their plan.
 * Read from Firestore: user_entitlements/{userId}
 * Default: FREE tier with minimum limits.
 */
data class UserEntitlements(
    val userId: String = "",
    val planType: PlanType = PlanType.FREE,
    val aiTasksPerDay: Int = 3,
    val aiChallengesPerDay: Int = 0,
    val aiPlansPerDay: Int = 0,
    val aiWeeklyPlansPerMonth: Int = 0,
    val unlockedFeatures: List<String> = emptyList(),  // e.g. ["world_map", "lootbox"]
    val updatedAt: Long = 0L
) {
    fun canGenerateTasks()        = aiTasksPerDay > 0
    fun canGenerateChallenges()   = aiChallengesPerDay > 0
    fun canGenerateDailyPlan()    = aiPlansPerDay > 0
    fun canGenerateWeeklyPlan()   = aiWeeklyPlansPerMonth > 0
    fun hasFeature(key: String)   = unlockedFeatures.contains(key)
}

enum class PlanType(val displayName: String, val emoji: String) {
    FREE    ("Free",    "🆓"),
    PRO     ("Pro",     "⭐"),
    PREMIUM ("Premium", "👑")
}

/**
 * Maps PlanType → default entitlements.
 * Used when no Firestore document exists yet for a user.
 */
fun PlanType.defaultEntitlements(userId: String) = when (this) {
    PlanType.FREE -> UserEntitlements(
        userId                = userId,
        planType              = PlanType.FREE,
        aiTasksPerDay         = 3,
        aiChallengesPerDay    = 0,
        aiPlansPerDay         = 0,
        aiWeeklyPlansPerMonth = 0,
        unlockedFeatures      = emptyList()
    )
    PlanType.PRO -> UserEntitlements(
        userId                = userId,
        planType              = PlanType.PRO,
        aiTasksPerDay         = 20,
        aiChallengesPerDay    = 5,
        aiPlansPerDay         = 3,
        aiWeeklyPlansPerMonth = 4,
        unlockedFeatures      = listOf("world_map", "lootbox", "daily_plan", "weekly_plan")
    )
    PlanType.PREMIUM -> UserEntitlements(
        userId                = userId,
        planType              = PlanType.PREMIUM,
        aiTasksPerDay         = 999,
        aiChallengesPerDay    = 50,
        aiPlansPerDay         = 10,
        aiWeeklyPlansPerMonth = 30,
        unlockedFeatures      = listOf("world_map", "lootbox", "daily_plan", "weekly_plan", "story_tasks", "seasonal_themes")
    )
}