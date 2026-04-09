package com.kidsroutine.core.model

import kotlin.random.Random

/**
 * Daily Spin Wheel / Mystery Bonus system.
 * Variable reward schedule — every segment on the wheel delivers exactly what it promises.
 * Once per day after first task completion.
 */

enum class SpinRewardType(
    val displayName: String,
    val emoji: String,
    val xpValue: Int,            // actual XP awarded (0 for non-XP rewards)
    val probability: Float       // sum must = 1.0
) {
    XP_JACKPOT(
        displayName = "+200 XP",
        emoji = "🌟",
        xpValue = 200,
        probability = 0.02f
    ),
    XP_BOOST_BIG(
        displayName = "+100 XP",
        emoji = "⚡",
        xpValue = 100,
        probability = 0.05f
    ),
    XP_BOOST_MEDIUM(
        displayName = "+50 XP",
        emoji = "🔥",
        xpValue = 50,
        probability = 0.10f
    ),
    XP_BOOST_SMALL(
        displayName = "+25 XP",
        emoji = "✨",
        xpValue = 25,
        probability = 0.18f
    ),
    XP_BOOST_MINI(
        displayName = "+10 XP",
        emoji = "💫",
        xpValue = 10,
        probability = 0.22f
    ),
    XP_BOOST_TINY(
        displayName = "+5 XP",
        emoji = "🍀",
        xpValue = 5,
        probability = 0.15f
    ),
    FREE_SPIN(
        displayName = "Free Spin!",
        emoji = "🎰",
        xpValue = 0,              // XP refund handled in ViewModel
        probability = 0.10f
    ),
    NOTHING(
        displayName = "Try Again!",
        emoji = "💨",
        xpValue = 0,
        probability = 0.18f
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
    val results: List<SpinWheelResult> = emptyList()
) {
    val canSpin: Boolean get() = spinsUsed < maxSpins
}
