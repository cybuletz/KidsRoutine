package com.kidsroutine.core.engine.challenge_engine

import android.util.Log
import com.kidsroutine.core.model.*
import com.kidsroutine.core.common.util.DateUtils

/**
 * Core challenge (habits) system.
 * Manages multi-day challenge tracking, progression, and task injection.
 */
class ChallengeEngine {

    /**
     * Starts a new challenge for a user.
     */
    fun startChallenge(
        challenge: ChallengeModel,
        userId: String,
        startDate: String = DateUtils.todayString()
    ): ChallengeProgress {
        Log.d("ChallengeEngine", "Starting challenge: ${challenge.title} for user: $userId")

        return ChallengeProgress(
            challengeId = challenge.challengeId,
            userId = userId,
            currentDay = 1,
            totalDays = challenge.duration,
            completedDays = 0,
            currentStreak = 0,
            successRate = 0f,
            dailyProgress = emptyMap(),
            status = ChallengeStatus.ACTIVE,
            startDate = startDate,
            endDate = calculateEndDate(startDate, challenge.duration),
            lastCompletedDate = ""
        )
    }

    /**
     * Records daily progress for a challenge.
     * Returns updated progress + whether challenge is now complete/failed.
     */
    fun recordDailyProgress(
        challenge: ChallengeModel,
        progress: ChallengeProgress,
        completed: Boolean,
        date: String = DateUtils.todayString()
    ): Pair<ChallengeProgress, ChallengeStatus> {
        Log.d("ChallengeEngine", "Recording progress for ${challenge.title}: completed=$completed on $date")

        val updatedDaily = progress.dailyProgress.toMutableMap()
        updatedDaily[date] = completed

        val newCompletedDays = updatedDaily.values.count { it }
        val newStreak = if (completed) progress.currentStreak + 1 else 0
        val newSuccessRate = (newCompletedDays.toFloat() / progress.totalDays) * 100

        // Determine new status
        val newStatus = when {
            newCompletedDays == progress.totalDays -> ChallengeStatus.COMPLETED
            !completed && shouldFailChallenge(challenge, progress, date) -> ChallengeStatus.FAILED
            else -> ChallengeStatus.ACTIVE
        }

        val updatedProgress = progress.copy(
            completedDays = newCompletedDays,
            currentStreak = newStreak,
            successRate = newSuccessRate,
            dailyProgress = updatedDaily,
            status = newStatus,
            currentDay = progress.currentDay + 1,
            lastCompletedDate = if (completed) date else progress.lastCompletedDate
        )

        Log.d("ChallengeEngine", "Updated progress: streak=$newStreak, completed=$newCompletedDays/${progress.totalDays}, status=$newStatus")
        return Pair(updatedProgress, newStatus)
    }

    /**
     * Determines if challenge should fail (streaks > 2 days missing).
     */
    private fun shouldFailChallenge(
        challenge: ChallengeModel,
        progress: ChallengeProgress,
        currentDate: String
    ): Boolean {
        // Challenge fails if user misses 2+ consecutive required days
        if (challenge.frequency == ChallengeFrequency.DAILY) {
            val missedConsecutive = countConsecutiveMissed(progress.dailyProgress, currentDate)
            return missedConsecutive >= 2
        }
        return false
    }

    /**
     * Counts consecutive missed days up to current date.
     */
    private fun countConsecutiveMissed(dailyProgress: Map<String, Boolean>, upToDate: String): Int {
        var count = 0
        var currentDate = upToDate

        while (count < 7) {
            val completed = dailyProgress[currentDate] ?: false
            if (!completed) {
                count++
            } else {
                break
            }
            currentDate = DateUtils.previousDay(currentDate)
        }

        return count
    }

    /**
     * Calculates XP for completing a challenge day.
     */
    fun calculateDailyXp(
        challenge: ChallengeModel,
        progress: ChallengeProgress,
        streakBonus: Boolean = false
    ): Int {
        var xp = challenge.dailyXpReward

        if (streakBonus && progress.currentStreak > 0) {
            xp += (challenge.streakBonusXp * progress.currentStreak)
        }

        return xp
    }

    /**
     * Calculates completion bonus (when challenge is fully completed).
     */
    fun calculateCompletionBonus(challenge: ChallengeModel): Int {
        return challenge.completionBonusXp
    }

    /**
     * Generates the daily task to inject into daily list.
     */
    fun generateDailyTask(
        challenge: ChallengeModel,
        challengeProgress: ChallengeProgress,
        today: String
    ): TaskModel? {
        if (challengeProgress.status != ChallengeStatus.ACTIVE) {
            return null
        }

        // Check if today should have a task (respect frequency)
        if (!shouldHaveTaskToday(challenge, today)) {
            return null
        }

        Log.d("ChallengeEngine", "Generating daily task for challenge: ${challenge.title}")

        return challenge.dailyTaskTemplate.copy(
            id = "challenge_${challenge.challengeId}_${today}",
            title = "${challenge.dailyTaskTemplate.title} (Day ${challengeProgress.currentDay}/${challengeProgress.totalDays})",
            description = "${challenge.dailyTaskTemplate.description}\n🔥 Streak: ${challengeProgress.currentStreak}",
            category = challenge.category,
            difficulty = challenge.difficulty,
            reward = TaskReward(xp = calculateDailyXp(challenge, challengeProgress)),
            requiresParent = challenge.validationType == ValidationType.PARENT_REQUIRED,
            familyId = challenge.familyId
        )
    }

    /**
     * Determines if challenge should have a task today based on frequency.
     */
    private fun shouldHaveTaskToday(challenge: ChallengeModel, date: String): Boolean {
        return when (challenge.frequency) {
            ChallengeFrequency.DAILY -> true
            ChallengeFrequency.CUSTOM_DAYS -> {
                // Could implement day-of-week logic here
                true
            }
        }
    }

    private fun calculateEndDate(startDate: String, days: Int): String {
        var current = startDate
        repeat(days - 1) {
            current = DateUtils.nextDay(current)
        }
        return current
    }
}