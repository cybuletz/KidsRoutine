package com.kidsroutine.core.model

/**
 * Per-child parent control settings.
 * Stored in Firestore: families/{familyId}/parent_controls/{childId}
 *
 * Controls which Fun Zone features are visible/accessible to each child,
 * quest difficulty configuration, and XP economy settings.
 */
data class ParentControlSettings(
    val childId: String = "",
    val familyId: String = "",

    // ── Fun Zone Visibility Toggles ────────────────────────────────
    // Parents can hide individual Fun Zone features per child
    val petEnabled: Boolean = true,
    val bossBattleEnabled: Boolean = true,
    val dailySpinEnabled: Boolean = true,
    val storyArcsEnabled: Boolean = true,
    val eventsEnabled: Boolean = true,
    val skillTreeEnabled: Boolean = true,
    val walletEnabled: Boolean = true,
    val ritualsEnabled: Boolean = true,

    // ── Quest Difficulty Configuration ──────────────────────────────
    // Parent can set min/max difficulty and XP multiplier overrides
    val allowedDifficulties: List<DifficultyLevel> = DifficultyLevel.entries,
    val defaultDifficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val xpMultiplierEasy: Float = 1.0f,
    val xpMultiplierMedium: Float = 2.0f,
    val xpMultiplierHard: Float = 3.0f,

    // ── XP Economy Controls ────────────────────────────────────────
    val dailyXpEarningCap: Int = 0,       // 0 = unlimited
    val dailyXpSpendingCap: Int = 0,      // 0 = unlimited

    val updatedAt: Long = 0L
) {
    /** Returns true if the given Fun Zone feature key is enabled by parent */
    fun isFunZoneFeatureEnabled(featureKey: String): Boolean = when (featureKey) {
        "pet"        -> petEnabled
        "boss_battle"-> bossBattleEnabled
        "daily_spin" -> dailySpinEnabled
        "story_arcs" -> storyArcsEnabled
        "events"     -> eventsEnabled
        "skill_tree" -> skillTreeEnabled
        "wallet"     -> walletEnabled
        "rituals"    -> ritualsEnabled
        else         -> true
    }

    /** Returns XP multiplier for a given difficulty */
    fun xpMultiplierFor(difficulty: DifficultyLevel): Float = when (difficulty) {
        DifficultyLevel.EASY   -> xpMultiplierEasy
        DifficultyLevel.MEDIUM -> xpMultiplierMedium
        DifficultyLevel.HARD   -> xpMultiplierHard
    }

    companion object {
        /** All Fun Zone feature keys for iteration in UI */
        val ALL_FUN_ZONE_FEATURES = listOf(
            FunZoneFeature("pet",         "🐾", "My Pet",      "Pet companion system"),
            FunZoneFeature("boss_battle", "⚔️", "Boss Battle", "Weekly family boss battles"),
            FunZoneFeature("daily_spin",  "🎡", "Daily Spin",  "Daily reward wheel"),
            FunZoneFeature("story_arcs",  "📖", "Story Arcs",  "Multi-day narrative adventures"),
            FunZoneFeature("events",      "📅", "Events",      "Limited-time seasonal events"),
            FunZoneFeature("skill_tree",  "🌳", "Skill Tree",  "Skill progression trees"),
            FunZoneFeature("wallet",      "💰", "Wallet",      "Financial literacy tools"),
            FunZoneFeature("rituals",     "🙏", "Rituals",     "Family gratitude & bonding")
        )
    }
}

/** Describes a Fun Zone feature for UI rendering */
data class FunZoneFeature(
    val key: String,
    val emoji: String,
    val title: String,
    val description: String
)
