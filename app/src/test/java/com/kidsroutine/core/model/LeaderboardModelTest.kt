package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class LeaderboardModelTest {

    // ── LeaderboardEntry defaults ───────────────────────────────────

    @Test
    fun `default rank is 0`() {
        val entry = LeaderboardEntry()
        assertEquals(0, entry.rank)
    }

    @Test
    fun `default level is 1`() {
        val entry = LeaderboardEntry()
        assertEquals(1, entry.level)
    }

    @Test
    fun `default league is BRONZE`() {
        val entry = LeaderboardEntry()
        assertEquals(League.BRONZE, entry.league)
    }

    @Test
    fun `default ageGroup is EXPLORER`() {
        val entry = LeaderboardEntry()
        assertEquals(AgeGroup.EXPLORER, entry.ageGroup)
    }

    @Test
    fun `default isPromoting is false`() {
        val entry = LeaderboardEntry()
        assertFalse(entry.isPromoting)
    }

    @Test
    fun `default isDemoting is false`() {
        val entry = LeaderboardEntry()
        assertFalse(entry.isDemoting)
    }

    @Test
    fun `entry stores all fields`() {
        val entry = LeaderboardEntry(
            rank = 1,
            userId = "u1",
            displayName = "Alice",
            xp = 1000,
            level = 10,
            weeklyXp = 200,
            league = League.GOLD,
            isPromoting = true
        )
        assertEquals(1, entry.rank)
        assertEquals("Alice", entry.displayName)
        assertEquals(1000, entry.xp)
        assertEquals(League.GOLD, entry.league)
        assertTrue(entry.isPromoting)
    }

    // ── FamilyLeaderboard defaults ──────────────────────────────────

    @Test
    fun `default family leaderboard entries is empty`() {
        val lb = FamilyLeaderboard()
        assertTrue(lb.entries.isEmpty())
    }

    @Test
    fun `default family leaderboard familyXp is 0`() {
        val lb = FamilyLeaderboard()
        assertEquals(0, lb.familyXp)
    }

    @Test
    fun `default family leaderboard rank is 0`() {
        val lb = FamilyLeaderboard()
        assertEquals(0, lb.rank)
    }

    @Test
    fun `default activeBoss is null`() {
        val lb = FamilyLeaderboard()
        assertNull(lb.activeBoss)
    }

    @Test
    fun `family leaderboard stores entries`() {
        val entries = listOf(
            LeaderboardEntry(rank = 1, userId = "u1"),
            LeaderboardEntry(rank = 2, userId = "u2")
        )
        val lb = FamilyLeaderboard(
            familyId = "f1",
            week = "2026-W15",
            entries = entries,
            familyXp = 5000
        )
        assertEquals(2, lb.entries.size)
        assertEquals("2026-W15", lb.week)
        assertEquals(5000, lb.familyXp)
    }
}
