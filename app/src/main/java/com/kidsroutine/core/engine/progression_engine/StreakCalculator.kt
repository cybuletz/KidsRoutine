package com.kidsroutine.core.engine.progression_engine

import android.util.Log
import com.kidsroutine.core.common.util.DateUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakCalculator @Inject constructor() {

    /**
     * Returns new streak count given the last active date and today's date.
     * Streak continues if lastDate was yesterday. Breaks if more than 1 day gap.
     * If shieldActive = true and daysDiff == 2, streak is protected for exactly 1 missed day.
     */
    fun computeStreak(
        currentStreak: Int,
        lastActiveDateStr: String,
        todayStr: String,
        shieldActive: Boolean = false   // ← NEW optional parameter; default false = no change to callers
    ): Int {
        if (lastActiveDateStr.isBlank()) return 1
        val last     = DateUtils.parseDate(lastActiveDateStr)
        val today    = DateUtils.parseDate(todayStr)
        val daysDiff = DateUtils.daysBetween(last, today)

        return when {
            daysDiff == 0 -> currentStreak                         // same day, no change
            daysDiff == 1 -> currentStreak + 1                     // consecutive — extend
            daysDiff == 2 && shieldActive -> {                     // ← NEW: shield absorbs 1 missed day
                Log.d("StreakCalculator", "🛡️ Streak shield activated! Streak preserved at $currentStreak")
                currentStreak                                       // hold — don't increment, don't reset
            }
            else          -> 1                                     // gap — reset
        }
    }

    /**
     * Checks whether a streak shield should be consumed.
     * Returns true if the shield was active AND a gap of exactly 1 day occurred.
     * Call this to know when to remove the shield from Firestore.
     */
    fun shouldConsumeShield(
        lastActiveDateStr: String,
        todayStr: String,
        shieldActive: Boolean
    ): Boolean {
        if (!shieldActive || lastActiveDateStr.isBlank()) return false
        val last     = DateUtils.parseDate(lastActiveDateStr)
        val today    = DateUtils.parseDate(todayStr)
        val daysDiff = DateUtils.daysBetween(last, today)
        return daysDiff == 2   // exactly 1 missed day = shield consumed
    }
}