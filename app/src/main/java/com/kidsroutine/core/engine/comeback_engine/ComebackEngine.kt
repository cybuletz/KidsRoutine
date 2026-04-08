package com.kidsroutine.core.engine.comeback_engine

import com.kidsroutine.core.common.util.DateUtils
import com.kidsroutine.core.model.ComebackState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Comeback Engine — "Roo-covery" system.
 * Detects lapsed users and creates gentle comeback paths.
 */
@Singleton
class ComebackEngine @Inject constructor() {

    /**
     * Check if a user qualifies for a comeback flow.
     * Returns null if no comeback needed, or a ComebackState if they've lapsed.
     */
    fun checkForComeback(
        userId: String,
        lastActiveDateStr: String,
        todayStr: String,
        previousStreak: Int
    ): ComebackState? {
        if (lastActiveDateStr.isBlank()) return null

        val lastActive = DateUtils.parseDate(lastActiveDateStr)
        val today = DateUtils.parseDate(todayStr)
        val daysAbsent = DateUtils.daysBetween(lastActive, today)

        // Only trigger comeback for 3+ days absence
        return if (daysAbsent >= 3) {
            ComebackState.forReturn(
                userId = userId,
                daysAbsent = daysAbsent,
                previousStreak = previousStreak
            )
        } else {
            null
        }
    }

    /**
     * Record a task completion during comeback challenge.
     * Returns updated state with progress.
     */
    fun recordTaskCompletion(state: ComebackState): ComebackState {
        if (!state.isActive) return state
        val updatedTasks = state.tasksCompletedToday + 1
        val isComplete = updatedTasks >= state.challenge.tasksRequired
        val streakRecovery = if (isComplete) state.potentialStreakRecovery else 0

        return state.copy(
            tasksCompletedToday = updatedTasks,
            streakRecovered = streakRecovery
        )
    }

    /**
     * Calculate the streak value after comeback recovery.
     */
    fun recoveredStreak(state: ComebackState): Int {
        return if (state.isChallengeComplete) {
            state.potentialStreakRecovery.coerceAtLeast(1)
        } else {
            1  // default: start fresh
        }
    }

    /**
     * Generate "what you missed" summary for the welcome back screen.
     */
    fun generateMissedSummary(
        daysAbsent: Int,
        petName: String?,
        friendUpdates: List<String>
    ): List<String> {
        val summary = mutableListOf<String>()

        if (daysAbsent > 7) {
            summary.add("It's been $daysAbsent days — a lot has happened!")
        } else {
            summary.add("You were away for $daysAbsent days.")
        }

        if (petName != null) {
            summary.add("$petName was sleeping but Roo kept them company 💤")
        }

        friendUpdates.take(3).forEach { summary.add(it) }

        summary.add("Complete your comeback challenge to recover your streak! 💪")

        return summary
    }
}
