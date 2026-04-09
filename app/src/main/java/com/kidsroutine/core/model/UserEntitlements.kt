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
    val maxChildren: Int = 2,                          // Max children per family
    val parentControlsEnabled: Boolean = false,        // Can parent configure Fun Zone / difficulty
    val xpBankEnabled: Boolean = false,                // Can parent lend XP
    val customDifficultyEnabled: Boolean = false,      // Can parent configure quest difficulty tiers
    val updatedAt: Long = 0L,

    // ── AI Trial Prompts (configurable) ────────────────────────────
    // FREE users get a limited number of trial prompts for premium AI features
    // so they can experience the value before upgrading. Configurable via Firestore.
    val aiTrialChallengePrompts: Int = DEFAULT_TRIAL_CHALLENGE_PROMPTS,
    val aiTrialPlanPrompts: Int = DEFAULT_TRIAL_PLAN_PROMPTS,
    val aiTrialWeeklyPlanPrompts: Int = DEFAULT_TRIAL_WEEKLY_PLAN_PROMPTS
) {
    fun canGenerateTasks()        = aiTasksPerDay > 0
    fun canGenerateChallenges()   = aiChallengesPerDay > 0 || (planType == PlanType.FREE && aiTrialChallengePrompts > 0)
    fun canGenerateDailyPlan()    = aiPlansPerDay > 0 || (planType == PlanType.FREE && aiTrialPlanPrompts > 0)
    fun canGenerateWeeklyPlan()   = aiWeeklyPlansPerMonth > 0 || (planType == PlanType.FREE && aiTrialWeeklyPlanPrompts > 0)
    fun hasFeature(key: String)   = unlockedFeatures.contains(key)

    /** Whether this is a trial-only access (no full quota, only trial prompts) */
    fun isTrialAccess(feature: String): Boolean = planType == PlanType.FREE && when (feature) {
        "challenges"   -> aiChallengesPerDay == 0 && aiTrialChallengePrompts > 0
        "daily_plan"   -> aiPlansPerDay == 0 && aiTrialPlanPrompts > 0
        "weekly_plan"  -> aiWeeklyPlansPerMonth == 0 && aiTrialWeeklyPlanPrompts > 0
        else           -> false
    }

    /** Check if a specific Fun Zone feature is available in this billing tier */
    fun hasFunZoneFeature(key: String): Boolean = when (planType) {
        PlanType.FREE    -> key in FREE_FUN_ZONE_FEATURES
        PlanType.PRO     -> key in PRO_FUN_ZONE_FEATURES
        PlanType.PREMIUM -> true  // Premium gets everything
    }

    /** Returns the required plan for a Fun Zone feature (for display when locked) */
    fun requiredPlanForFeature(key: String): PlanType? {
        if (hasFunZoneFeature(key)) return null // Already unlocked
        return when (key) {
            in PRO_FUN_ZONE_FEATURES -> PlanType.PRO
            else -> PlanType.PRO  // Default to PRO for any premium feature
        }
    }

    companion object {
        /** Configurable trial prompt defaults — FREE users get a taste of premium AI */
        const val DEFAULT_TRIAL_CHALLENGE_PROMPTS = 2
        const val DEFAULT_TRIAL_PLAN_PROMPTS = 2
        const val DEFAULT_TRIAL_WEEKLY_PLAN_PROMPTS = 1

        /** Fun Zone features available per billing tier */
        val FREE_FUN_ZONE_FEATURES = setOf(
            "pet", "daily_spin"
        )
        val PRO_FUN_ZONE_FEATURES = setOf(
            "pet", "daily_spin",
            "boss_battle", "story_arcs", "events", "skill_tree", "rituals"
        )
        /** All possible Fun Zone feature keys */
        val ALL_FUN_ZONE_FEATURE_KEYS = setOf(
            "pet", "daily_spin", "rituals",
            "boss_battle", "story_arcs", "events", "skill_tree",
            "wallet"
        )
        // PREMIUM = all features
    }
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
        userId                     = userId,
        planType                   = PlanType.FREE,
        aiTasksPerDay              = 3,
        aiChallengesPerDay         = 0,
        aiPlansPerDay              = 0,
        aiWeeklyPlansPerMonth      = 0,
        unlockedFeatures           = emptyList(),
        maxChildren                = 2,
        parentControlsEnabled      = false,
        xpBankEnabled              = false,
        customDifficultyEnabled    = false,
        aiTrialChallengePrompts    = UserEntitlements.DEFAULT_TRIAL_CHALLENGE_PROMPTS,
        aiTrialPlanPrompts         = UserEntitlements.DEFAULT_TRIAL_PLAN_PROMPTS,
        aiTrialWeeklyPlanPrompts   = UserEntitlements.DEFAULT_TRIAL_WEEKLY_PLAN_PROMPTS
    )
    PlanType.PRO -> UserEntitlements(
        userId                     = userId,
        planType                   = PlanType.PRO,
        aiTasksPerDay              = 20,
        aiChallengesPerDay         = 5,
        aiPlansPerDay              = 3,
        aiWeeklyPlansPerMonth      = 4,
        unlockedFeatures           = listOf("world_map", "lootbox", "daily_plan", "weekly_plan"),
        maxChildren                = 5,
        parentControlsEnabled      = true,
        xpBankEnabled              = true,
        customDifficultyEnabled    = true,
        aiTrialChallengePrompts    = 0,  // Not needed — full access
        aiTrialPlanPrompts         = 0,
        aiTrialWeeklyPlanPrompts   = 0
    )
    PlanType.PREMIUM -> UserEntitlements(
        userId                     = userId,
        planType                   = PlanType.PREMIUM,
        aiTasksPerDay              = 999,
        aiChallengesPerDay         = 50,
        aiPlansPerDay              = 10,
        aiWeeklyPlansPerMonth      = 30,
        unlockedFeatures           = listOf("world_map", "lootbox", "daily_plan", "weekly_plan", "story_tasks", "seasonal_themes"),
        maxChildren                = 20,
        parentControlsEnabled      = true,
        xpBankEnabled              = true,
        customDifficultyEnabled    = true,
        aiTrialChallengePrompts    = 0,  // Not needed — full access
        aiTrialPlanPrompts         = 0,
        aiTrialWeeklyPlanPrompts   = 0
    )
}