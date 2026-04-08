package com.kidsroutine.core.model

/**
 * Comeback/Recovery System — "Roo-covery."
 * Don't punish lapsed users, welcome them back.
 * Inspired by Duolingo's resurrection mechanics.
 */

enum class ComebackChallengeType(
    val displayName: String,
    val emoji: String,
    val tasksRequired: Int,
    val streakRecoveryPercent: Float  // % of lost streak restored
) {
    QUICK_RETURN(
        displayName = "Quick Return",
        emoji = "⚡",
        tasksRequired = 1,
        streakRecoveryPercent = 0.25f
    ),
    COMEBACK_KID(
        displayName = "Comeback Kid",
        emoji = "💪",
        tasksRequired = 3,
        streakRecoveryPercent = 0.50f
    ),
    FULL_RECOVERY(
        displayName = "Full Recovery",
        emoji = "🔥",
        tasksRequired = 5,
        streakRecoveryPercent = 0.75f
    )
}

data class ComebackState(
    val userId: String = "",
    val daysAbsent: Int = 0,
    val previousStreak: Int = 0,
    val isActive: Boolean = false,
    val challenge: ComebackChallengeType = ComebackChallengeType.COMEBACK_KID,
    val tasksCompletedToday: Int = 0,
    val streakRecovered: Int = 0,
    val freeShieldGranted: Boolean = false,
    val showWelcomeBack: Boolean = true,

    // What they missed
    val friendUpdates: List<String> = emptyList(),  // "[Friend] reached Gold league!"
    val petStatus: String = "",                      // "Sleeping but safe"
    val eventsActive: List<String> = emptyList()     // active timed events
) {
    /** Whether the comeback challenge is complete */
    val isChallengeComplete: Boolean
        get() = tasksCompletedToday >= challenge.tasksRequired

    /** Streak amount that would be restored */
    val potentialStreakRecovery: Int
        get() = (previousStreak * challenge.streakRecoveryPercent).toInt().coerceAtLeast(1)

    companion object {
        /**
         * Create a ComebackState when user returns after absence.
         * @param daysAbsent how many days since last activity
         * @param previousStreak the streak they had before lapsing
         */
        fun forReturn(
            userId: String,
            daysAbsent: Int,
            previousStreak: Int
        ): ComebackState {
            val challenge = when {
                daysAbsent <= 3 -> ComebackChallengeType.QUICK_RETURN
                daysAbsent <= 7 -> ComebackChallengeType.COMEBACK_KID
                else -> ComebackChallengeType.FULL_RECOVERY
            }
            return ComebackState(
                userId = userId,
                daysAbsent = daysAbsent,
                previousStreak = previousStreak,
                isActive = true,
                challenge = challenge,
                freeShieldGranted = daysAbsent <= 5,
                showWelcomeBack = true
            )
        }
    }
}
