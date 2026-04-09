package com.kidsroutine.core.engine.league_engine

import com.kidsroutine.core.model.AgeGroup
import com.kidsroutine.core.model.League
import com.kidsroutine.core.model.LeagueEntry
import com.kidsroutine.core.model.LeagueStanding
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LeagueEngineTest {

    private lateinit var engine: LeagueEngine

    @Before
    fun setUp() {
        engine = LeagueEngine()
    }

    private fun createEntry(userId: String, weeklyXp: Int) = LeagueEntry(
        userId = userId,
        displayName = userId,
        weeklyXp = weeklyXp
    )

    // ── initialLeague ───────────────────────────────────────────────

    @Test
    fun `all users start in Bronze`() {
        assertEquals(League.BRONZE, engine.initialLeague())
    }

    // ── promote and demote ──────────────────────────────────────────

    @Test
    fun `promote moves Bronze to Silver`() {
        assertEquals(League.SILVER, engine.promote(League.BRONZE))
    }

    @Test
    fun `promote from Champion stays Champion`() {
        assertEquals(League.CHAMPION, engine.promote(League.CHAMPION))
    }

    @Test
    fun `demote moves Silver to Bronze`() {
        assertEquals(League.BRONZE, engine.demote(League.SILVER))
    }

    @Test
    fun `demote from Bronze stays Bronze`() {
        assertEquals(League.BRONZE, engine.demote(League.BRONZE))
    }

    @Test
    fun `promote goes up sequentially`() {
        var league = League.BRONZE
        league = engine.promote(league)
        assertEquals(League.SILVER, league)
        league = engine.promote(league)
        assertEquals(League.GOLD, league)
    }

    // ── processWeekEnd ──────────────────────────────────────────────

    @Test
    fun `processWeekEnd returns empty list for empty entries`() {
        val result = engine.processWeekEnd(emptyList(), League.SILVER)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `processWeekEnd ranks entries by weeklyXp descending`() {
        val entries = listOf(
            createEntry("low", 50),
            createEntry("high", 200),
            createEntry("mid", 100)
        )
        val result = engine.processWeekEnd(entries, League.SILVER)
        assertEquals("high", result[0].userId)
        assertEquals("mid", result[1].userId)
        assertEquals("low", result[2].userId)
        assertEquals(1, result[0].rank)
        assertEquals(2, result[1].rank)
        assertEquals(3, result[2].rank)
    }

    @Test
    fun `processWeekEnd marks top entries as promoting in Silver`() {
        // Silver has promotionSlots=10, minXpForPromotion=100
        val entries = (1..15).map { createEntry("user$it", it * 20) }
        val result = engine.processWeekEnd(entries, League.SILVER)
        val promoting = result.filter { it.isPromoting }
        assertTrue(promoting.isNotEmpty())
        assertTrue(promoting.all { it.weeklyXp >= League.SILVER.minXpForPromotion })
    }

    @Test
    fun `processWeekEnd does not promote from Champion`() {
        val entries = listOf(createEntry("user1", 999))
        val result = engine.processWeekEnd(entries, League.CHAMPION)
        assertFalse(result[0].isPromoting)
    }

    @Test
    fun `processWeekEnd does not demote from Bronze`() {
        val entries = listOf(createEntry("user1", 5))
        val result = engine.processWeekEnd(entries, League.BRONZE)
        assertFalse(result[0].isDemoting)
    }

    @Test
    fun `processWeekEnd marks bottom entries as demoting in Silver`() {
        // Silver has demotionSlots=5
        val entries = (1..20).map { createEntry("user$it", it * 10) }
        val result = engine.processWeekEnd(entries, League.SILVER)
        val demoting = result.filter { it.isDemoting }
        assertEquals(5, demoting.size)
    }

    // ── isInDangerZone ──────────────────────────────────────────────

    @Test
    fun `isInDangerZone returns true for bottom position in Silver`() {
        // Silver: demotionSlots=5, if rank > (20-5)=15, in danger
        assertTrue(engine.isInDangerZone(userRank = 16, totalEntries = 20, league = League.SILVER))
    }

    @Test
    fun `isInDangerZone returns false for top position`() {
        assertFalse(engine.isInDangerZone(userRank = 1, totalEntries = 20, league = League.SILVER))
    }

    @Test
    fun `isInDangerZone returns false for Bronze`() {
        assertFalse(engine.isInDangerZone(userRank = 20, totalEntries = 20, league = League.BRONZE))
    }

    // ── isInPromotionZone ───────────────────────────────────────────

    @Test
    fun `isInPromotionZone returns true for rank 1`() {
        assertTrue(engine.isInPromotionZone(userRank = 1, league = League.SILVER))
    }

    @Test
    fun `isInPromotionZone returns false for rank beyond slots`() {
        assertFalse(engine.isInPromotionZone(userRank = 11, league = League.SILVER))
    }

    @Test
    fun `isInPromotionZone returns false for Champion`() {
        assertFalse(engine.isInPromotionZone(userRank = 1, league = League.CHAMPION))
    }

    // ── createStanding ──────────────────────────────────────────────

    @Test
    fun `createStanding ranks entries and finds user`() {
        val entries = listOf(
            createEntry("other", 100),
            createEntry("me", 200)
        )
        val standing = engine.createStanding(
            league = League.BRONZE,
            week = "2026-W15",
            ageGroup = AgeGroup.EXPLORER,
            entries = entries,
            currentUserId = "me",
            daysRemaining = 3
        )
        assertEquals(1, standing.userRank) // "me" has most XP
        assertEquals(2, standing.entries.size)
        assertTrue(standing.entries[0].isCurrentUser)
        assertEquals(3, standing.daysRemaining)
    }

    @Test
    fun `createStanding handles user not in entries`() {
        val entries = listOf(createEntry("other", 100))
        val standing = engine.createStanding(
            league = League.BRONZE,
            week = "2026-W15",
            ageGroup = AgeGroup.EXPLORER,
            entries = entries,
            currentUserId = "missing_user",
            daysRemaining = 5
        )
        assertEquals(0, standing.userRank)
    }

    @Test
    fun `createStanding handles empty entries`() {
        val standing = engine.createStanding(
            league = League.GOLD,
            week = "2026-W15",
            ageGroup = AgeGroup.SPROUT,
            entries = emptyList(),
            currentUserId = "me",
            daysRemaining = 7
        )
        assertEquals(0, standing.userRank)
        assertTrue(standing.entries.isEmpty())
    }

    // ── xpForPromotion / xpForSafety ────────────────────────────────

    @Test
    fun `xpForPromotion returns 0 for empty entries`() {
        val standing = LeagueStanding(league = League.SILVER, entries = emptyList())
        assertEquals(0, engine.xpForPromotion(standing))
    }

    @Test
    fun `xpForSafety returns 0 for empty entries`() {
        val standing = LeagueStanding(league = League.SILVER, entries = emptyList())
        assertEquals(0, engine.xpForSafety(standing))
    }
}
