package com.kidsroutine.core.engine.league_engine

import com.kidsroutine.core.model.AgeGroup
import com.kidsroutine.core.model.League
import com.kidsroutine.core.model.LeagueEntry
import com.kidsroutine.core.model.LeagueStanding
import javax.inject.Inject
import javax.inject.Singleton

/**
 * League Engine — manages weekly league cycles,
 * promotion/relegation logic, and standing calculations.
 */
@Singleton
class LeagueEngine @Inject constructor() {

    /**
     * Process end-of-week standings and determine promotions/demotions.
     * @param entries all entries in the league for the week, sorted by weeklyXp desc
     * @param league the current league being processed
     * @return updated entries with isPromoting/isDemoting flags
     */
    fun processWeekEnd(
        entries: List<LeagueEntry>,
        league: League
    ): List<LeagueEntry> {
        if (entries.isEmpty()) return entries

        val sorted = entries.sortedByDescending { it.weeklyXp }
        val ranked = sorted.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }

        return ranked.map { entry ->
            val isPromoting = league.nextLeague != null &&
                    entry.rank <= league.promotionSlots &&
                    entry.weeklyXp >= league.minXpForPromotion

            val isDemoting = league.previousLeague != null &&
                    entry.rank > (ranked.size - league.demotionSlots)

            entry.copy(
                isPromoting = isPromoting,
                isDemoting = isDemoting
            )
        }
    }

    /**
     * Determine initial league placement for a new user.
     * All users start in Bronze.
     */
    fun initialLeague(): League = League.BRONZE

    /**
     * Apply promotion: move user to next league.
     */
    fun promote(currentLeague: League): League =
        currentLeague.nextLeague ?: currentLeague

    /**
     * Apply demotion: move user to previous league.
     */
    fun demote(currentLeague: League): League =
        currentLeague.previousLeague ?: currentLeague

    /**
     * Calculate the XP threshold for promotion in the current context.
     * Based on current standings, returns the XP needed to reach promotion zone.
     */
    fun xpForPromotion(standing: LeagueStanding): Int {
        if (standing.entries.isEmpty()) return 0
        val sorted = standing.entries.sortedByDescending { it.weeklyXp }
        val promotionCutoff = sorted.getOrNull(standing.league.promotionSlots - 1)
        return promotionCutoff?.weeklyXp ?: standing.league.minXpForPromotion
    }

    /**
     * Calculate the XP threshold below which demotion occurs.
     */
    fun xpForSafety(standing: LeagueStanding): Int {
        if (standing.entries.isEmpty()) return 0
        val sorted = standing.entries.sortedByDescending { it.weeklyXp }
        val safetyIndex = (sorted.size - standing.league.demotionSlots).coerceAtLeast(0)
        return sorted.getOrNull(safetyIndex)?.weeklyXp ?: 0
    }

    /**
     * Check if user is in danger zone (bottom N positions).
     */
    fun isInDangerZone(userRank: Int, totalEntries: Int, league: League): Boolean {
        if (league.demotionSlots == 0) return false
        return userRank > (totalEntries - league.demotionSlots)
    }

    /**
     * Check if user is in promotion zone (top N positions).
     */
    fun isInPromotionZone(userRank: Int, league: League): Boolean {
        if (league.promotionSlots == 0) return false
        return userRank <= league.promotionSlots
    }

    /**
     * Create league standing for display.
     */
    fun createStanding(
        league: League,
        week: String,
        ageGroup: AgeGroup,
        entries: List<LeagueEntry>,
        currentUserId: String,
        daysRemaining: Int
    ): LeagueStanding {
        val sorted = entries.sortedByDescending { it.weeklyXp }
        val ranked = sorted.mapIndexed { index, entry ->
            entry.copy(
                rank = index + 1,
                isCurrentUser = entry.userId == currentUserId
            )
        }
        val userRank = ranked.firstOrNull { it.isCurrentUser }?.rank ?: 0

        return LeagueStanding(
            league = league,
            week = week,
            ageGroup = ageGroup,
            entries = ranked,
            userRank = userRank,
            promotionThresholdXp = ranked.getOrNull(league.promotionSlots - 1)?.weeklyXp ?: league.minXpForPromotion,
            demotionThresholdXp = if (league.demotionSlots > 0 && ranked.size > league.demotionSlots)
                ranked[ranked.size - league.demotionSlots].weeklyXp else 0,
            daysRemaining = daysRemaining
        )
    }
}
