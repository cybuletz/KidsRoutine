package com.kidsroutine.core.engine

import com.kidsroutine.core.model.Season
import com.kidsroutine.core.model.SeasonalTheme
import com.kidsroutine.core.model.SeasonalThemes
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Picks the active seasonal theme based on current date.
 * Inject this wherever you need the theme; it is stateless.
 */
@Singleton
class SeasonalThemeManager @Inject constructor() {

    /** Returns the active theme for today. */
    fun getActiveTheme(): SeasonalTheme {
        val cal   = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1   // 1-based
        val day   = cal.get(Calendar.DAY_OF_MONTH)

        return when {
            // Halloween: Oct 1–31
            month == 10 -> SeasonalThemes.HALLOWEEN
            // Christmas: Dec 1–31
            month == 12 -> SeasonalThemes.CHRISTMAS
            // Winter: Jan + Feb
            month in listOf(1, 2) -> SeasonalThemes.WINTER
            // Spring: Mar–May
            month in 3..5 -> SeasonalThemes.SPRING
            // Summer: Jun–Aug
            month in 6..8 -> SeasonalThemes.SUMMER
            // Autumn: Sep + Nov
            else -> SeasonalThemes.AUTUMN
        }.copy(isActive = true)
    }

    /** Returns the season enum for the current month. */
    fun currentSeason(): Season = getActiveTheme().season
}