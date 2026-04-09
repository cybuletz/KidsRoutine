package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class GameTypeTest {

    // ── forAgeGroup ─────────────────────────────────────────────────

    @Test
    fun `SPROUT gets core games`() {
        val games = GameType.forAgeGroup(AgeGroup.SPROUT)
        assertTrue(games.contains(GameType.MEMORY_GAME))
        assertTrue(games.contains(GameType.SPEED_GAME))
        assertTrue(games.contains(GameType.LOGIC_GAME))
        assertTrue(games.contains(GameType.PATTERN_MATCH))
        assertTrue(games.contains(GameType.COUNTING_GAME))
        assertTrue(games.contains(GameType.SHAPE_SORT))
    }

    @Test
    fun `SPROUT does not get EXPLORER games`() {
        val games = GameType.forAgeGroup(AgeGroup.SPROUT)
        assertFalse(games.contains(GameType.WORD_SCRAMBLE))
        assertFalse(games.contains(GameType.TRIVIA))
        assertFalse(games.contains(GameType.SEQUENCE))
    }

    @Test
    fun `EXPLORER gets SPROUT plus own games`() {
        val games = GameType.forAgeGroup(AgeGroup.EXPLORER)
        // SPROUT games
        assertTrue(games.contains(GameType.MEMORY_GAME))
        assertTrue(games.contains(GameType.SHAPE_SORT))
        // EXPLORER games
        assertTrue(games.contains(GameType.WORD_SCRAMBLE))
        assertTrue(games.contains(GameType.TRIVIA))
        assertTrue(games.contains(GameType.SEQUENCE))
    }

    @Test
    fun `TRAILBLAZER gets all up to own tier`() {
        val games = GameType.forAgeGroup(AgeGroup.TRAILBLAZER)
        assertTrue(games.contains(GameType.MEMORY_GAME))
        assertTrue(games.contains(GameType.WORD_SCRAMBLE))
        assertTrue(games.contains(GameType.QUICK_THINK))
        assertTrue(games.contains(GameType.CODE_CRACK))
        assertTrue(games.contains(GameType.TYPING_SPEED))
        assertTrue(games.contains(GameType.FACT_FICTION))
    }

    @Test
    fun `TRAILBLAZER does not get LEGEND games`() {
        val games = GameType.forAgeGroup(AgeGroup.TRAILBLAZER)
        assertFalse(games.contains(GameType.BUDGET_BOSS))
        assertFalse(games.contains(GameType.TIME_ARCHITECT))
        assertFalse(games.contains(GameType.DEBATE_PROMPT))
    }

    @Test
    fun `LEGEND gets all games`() {
        val games = GameType.forAgeGroup(AgeGroup.LEGEND)
        // Should include ALL non-NONE games
        val allNonNone = GameType.entries.filter { it != GameType.NONE }
        assertEquals(allNonNone.size, games.size)
    }

    @Test
    fun `forAgeGroup never includes NONE`() {
        AgeGroup.entries.forEach { ageGroup ->
            val games = GameType.forAgeGroup(ageGroup)
            assertFalse("NONE should never be in forAgeGroup for $ageGroup",
                games.contains(GameType.NONE))
        }
    }

    // ── newForAgeGroup ──────────────────────────────────────────────

    @Test
    fun `newForAgeGroup SPROUT returns SPROUT-minAge games`() {
        val newGames = GameType.newForAgeGroup(AgeGroup.SPROUT)
        assertTrue(newGames.contains(GameType.MEMORY_GAME))
        assertTrue(newGames.contains(GameType.SPEED_GAME))
        assertTrue(newGames.contains(GameType.LOGIC_GAME))
        assertTrue(newGames.contains(GameType.PATTERN_MATCH))
        assertTrue(newGames.contains(GameType.COUNTING_GAME))
        assertTrue(newGames.contains(GameType.SHAPE_SORT))
    }

    @Test
    fun `newForAgeGroup EXPLORER returns only EXPLORER-minAge games`() {
        val newGames = GameType.newForAgeGroup(AgeGroup.EXPLORER)
        assertTrue(newGames.contains(GameType.WORD_SCRAMBLE))
        assertTrue(newGames.contains(GameType.TRIVIA))
        assertTrue(newGames.contains(GameType.SEQUENCE))
        assertFalse(newGames.contains(GameType.MEMORY_GAME))
    }

    @Test
    fun `newForAgeGroup LEGEND returns LEGEND-tier games`() {
        val newGames = GameType.newForAgeGroup(AgeGroup.LEGEND)
        assertTrue(newGames.contains(GameType.BUDGET_BOSS))
        assertTrue(newGames.contains(GameType.TIME_ARCHITECT))
        assertTrue(newGames.contains(GameType.DEBATE_PROMPT))
        assertFalse(newGames.contains(GameType.CODE_CRACK))
    }

    // ── enum properties ─────────────────────────────────────────────

    @Test
    fun `all non-NONE games have non-empty displayName`() {
        GameType.entries.filter { it != GameType.NONE }.forEach {
            assertTrue("${it.name} should have displayName", it.displayName.isNotEmpty())
        }
    }

    @Test
    fun `all non-NONE games have non-empty emoji`() {
        GameType.entries.filter { it != GameType.NONE }.forEach {
            assertTrue("${it.name} should have emoji", it.emoji.isNotEmpty())
        }
    }

    @Test
    fun `all non-NONE games have non-empty description`() {
        GameType.entries.filter { it != GameType.NONE }.forEach {
            assertTrue("${it.name} should have description", it.description.isNotEmpty())
        }
    }

    @Test
    fun `GameType has 18 entries`() {
        assertEquals(18, GameType.entries.size)
    }

    @Test
    fun `games are progressively unlocked across age groups`() {
        val sproutCount      = GameType.forAgeGroup(AgeGroup.SPROUT).size
        val explorerCount    = GameType.forAgeGroup(AgeGroup.EXPLORER).size
        val trailblazerCount = GameType.forAgeGroup(AgeGroup.TRAILBLAZER).size
        val legendCount      = GameType.forAgeGroup(AgeGroup.LEGEND).size
        assertTrue(sproutCount < explorerCount)
        assertTrue(explorerCount < trailblazerCount)
        assertTrue(trailblazerCount < legendCount)
    }
}
