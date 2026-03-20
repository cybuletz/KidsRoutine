package com.kidsroutine.core.engine.progression_engine

import com.kidsroutine.core.common.util.DateUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakCalculator @Inject constructor() {

    /**
     * Returns new streak count given the last active date and today's date.
     * Streak continues if lastDate was yesterday. Breaks if more than 1 day gap.
     */
    fun computeStreak(currentStreak: Int, lastActiveDateStr: String, todayStr: String): Int {
        if (lastActiveDateStr.isBlank()) return 1
        val last  = DateUtils.parseDate(lastActiveDateStr)
        val today = DateUtils.parseDate(todayStr)
        val daysDiff = DateUtils.daysBetween(last, today)
        return when (daysDiff) {
            0    -> currentStreak          // same day, no change
            1    -> currentStreak + 1      // consecutive — extend streak
            else -> 1                      // gap — reset but start at 1
        }
    }
}
