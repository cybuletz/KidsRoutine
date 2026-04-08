package com.kidsroutine.core.engine.event_engine

import com.kidsroutine.core.model.EventProgress
import com.kidsroutine.core.model.TimedEvent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Event Engine — manages timed/seasonal events lifecycle.
 * Handles event activation, progress tracking, and reward distribution.
 */
@Singleton
class EventEngine @Inject constructor() {

    /**
     * Check if a timed event is currently active.
     */
    fun isEventActive(event: TimedEvent, currentTime: Long): Boolean {
        return currentTime in event.startTimestamp..event.endTimestamp
    }

    /**
     * Record task completion towards an active event.
     */
    fun recordTaskCompletion(
        progress: EventProgress,
        event: TimedEvent,
        xpEarned: Int,
        tokensPerTask: Int = 1
    ): EventProgress {
        val updatedTasks = progress.tasksCompleted + 1
        val updatedXp = progress.xpEarned + xpEarned
        val updatedTokens = progress.tokensEarned + tokensPerTask
        val isComplete = updatedTasks >= event.targetTaskCount ||
                (event.targetXp > 0 && updatedXp >= event.targetXp)

        return progress.copy(
            tasksCompleted = updatedTasks,
            xpEarned = updatedXp,
            tokensEarned = updatedTokens,
            isComplete = isComplete,
            lastActivityAt = System.currentTimeMillis()
        )
    }

    /**
     * Spend event tokens in the event shop.
     */
    fun spendTokens(progress: EventProgress, cost: Int): EventProgress? {
        if (progress.tokensAvailable < cost) return null
        return progress.copy(tokensSpent = progress.tokensSpent + cost)
    }

    /**
     * Calculate time remaining for an event (in seconds).
     */
    fun timeRemainingSeconds(event: TimedEvent, currentTime: Long): Long {
        return ((event.endTimestamp - currentTime) / 1000).coerceAtLeast(0)
    }

    /**
     * Calculate completion percentage.
     */
    fun completionPercent(progress: EventProgress, event: TimedEvent): Float {
        val taskPercent = if (event.targetTaskCount > 0)
            progress.tasksCompleted.toFloat() / event.targetTaskCount else 0f
        val xpPercent = if (event.targetXp > 0)
            progress.xpEarned.toFloat() / event.targetXp else 0f
        return maxOf(taskPercent, xpPercent).coerceIn(0f, 1f)
    }

    /**
     * Get XP reward for completing the event.
     */
    fun eventRewardXp(event: TimedEvent, progress: EventProgress): Int {
        return if (progress.isComplete) event.rewardXp else 0
    }
}
