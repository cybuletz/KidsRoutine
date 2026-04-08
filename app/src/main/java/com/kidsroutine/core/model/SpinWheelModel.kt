package com.kidsroutine.core.model

import kotlin.random.Random

/**
 * Daily Spin Wheel / Mystery Bonus system.
 * Variable reward schedule — the most addictive mechanic in gaming.
 * Once per day after first task completion.
 */

enum class SpinRewardType(
    val displayName: String,
    val emoji: String,
    val probability: Float  // sum must = 1.0
) {
    DOUBLE_XP(
        displayName = "2x XP",
        emoji = "⚡",
        probability = 0.15f
    ),
    BONUS_AVATAR_ITEM(
        displayName = "Avatar Item",
        emoji = "👕",
        probability = 0.10f
    ),
    PET_TREAT(
        displayName = "Pet Treat",
        emoji = "🦴",
        probability = 0.15f
    ),
    STREAK_SHIELD(
        displayName = "Streak Shield",
        emoji = "🛡️",
        probability = 0.08f
    ),
    XP_BOOST_SMALL(
        displayName = "+25 XP",
        emoji = "✨",
        probability = 0.20f
    ),
    XP_BOOST_BIG(
        displayName = "+100 XP",
        emoji = "🌟",
        probability = 0.05f
    ),
    LEAGUE_SHIELD(
        displayName = "League Shield",
        emoji = "🏅",
        probability = 0.05f
    ),
    ROO_EMOJI(
        displayName = "Roo Sticker",
        emoji = "🦘",
        probability = 0.12f
    ),
    NOTHING(
        displayName = "Try Again!",
        emoji = "💨",
        probability = 0.10f
    );

    companion object {
        /** Weighted random selection based on probabilities */
        fun randomReward(): SpinRewardType {
            val roll = Random.nextFloat()
            var cumulative = 0f
            for (reward in entries) {
                cumulative += reward.probability
                if (roll <= cumulative) return reward
            }
            return NOTHING
        }
    }
}

data class SpinWheelResult(
    val resultId: String = "",
    val userId: String = "",
    val reward: SpinRewardType = SpinRewardType.NOTHING,
    val date: String = "",         // "2026-04-08"
    val spinNumber: Int = 1,       // 1st or 2nd spin (premium gets 2)
    val claimedAt: Long = 0L
)

data class DailySpinState(
    val userId: String = "",
    val date: String = "",
    val spinsUsed: Int = 0,
    val maxSpins: Int = 1,         // 1 for free, 2 for premium
    val results: List<SpinWheelResult> = emptyList(),
    val hasDoubleXpActive: Boolean = false,
    val doubleXpExpiresAt: Long = 0L
) {
    val canSpin: Boolean get() = spinsUsed < maxSpins
}
