package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class GameConfigFactoryTest {

    // ── configFor returns correct game type and age group ────────────

    @Test
    fun `configFor returns matching gameType and ageGroup`() {
        val config = GameConfigFactory.configFor(GameType.MEMORY_GAME, AgeGroup.SPROUT)
        assertEquals(GameType.MEMORY_GAME, config.gameType)
        assertEquals(AgeGroup.SPROUT, config.ageGroup)
    }

    // ── Memory game configs scale with age ──────────────────────────

    @Test
    fun `memory SPROUT has small grid`() {
        val config = GameConfigFactory.configFor(GameType.MEMORY_GAME, AgeGroup.SPROUT)
        assertEquals(6, config.gridSize)
        assertEquals(0, config.timeLimit)
    }

    @Test
    fun `memory LEGEND has large grid and tight timer`() {
        val config = GameConfigFactory.configFor(GameType.MEMORY_GAME, AgeGroup.LEGEND)
        assertEquals(14, config.gridSize)
        assertTrue(config.timeLimit > 0)
    }

    @Test
    fun `memory grid size increases with age`() {
        val sprout = GameConfigFactory.configFor(GameType.MEMORY_GAME, AgeGroup.SPROUT).gridSize
        val explorer = GameConfigFactory.configFor(GameType.MEMORY_GAME, AgeGroup.EXPLORER).gridSize
        val trail = GameConfigFactory.configFor(GameType.MEMORY_GAME, AgeGroup.TRAILBLAZER).gridSize
        val legend = GameConfigFactory.configFor(GameType.MEMORY_GAME, AgeGroup.LEGEND).gridSize
        assertTrue(sprout < explorer)
        assertTrue(explorer < trail)
        assertTrue(trail < legend)
    }

    // ── Speed game configs ──────────────────────────────────────────

    @Test
    fun `speed SPROUT has longer time limit`() {
        val config = GameConfigFactory.configFor(GameType.SPEED_GAME, AgeGroup.SPROUT)
        assertEquals(7, config.timeLimit)
    }

    @Test
    fun `speed time limit decreases with age`() {
        val sprout = GameConfigFactory.configFor(GameType.SPEED_GAME, AgeGroup.SPROUT).timeLimit
        val legend = GameConfigFactory.configFor(GameType.SPEED_GAME, AgeGroup.LEGEND).timeLimit
        assertTrue(sprout > legend)
    }

    // ── Logic game configs ──────────────────────────────────────────

    @Test
    fun `logic SPROUT has hints available`() {
        val config = GameConfigFactory.configFor(GameType.LOGIC_GAME, AgeGroup.SPROUT)
        assertTrue(config.hintAvailable)
    }

    @Test
    fun `logic SPROUT uses ADDITION complexity`() {
        val config = GameConfigFactory.configFor(GameType.LOGIC_GAME, AgeGroup.SPROUT)
        assertEquals("ADDITION", config.contentComplexity)
    }

    // ── Word scramble configs ───────────────────────────────────────

    @Test
    fun `word SPROUT uses 3 letters`() {
        val config = GameConfigFactory.configFor(GameType.WORD_SCRAMBLE, AgeGroup.SPROUT)
        assertEquals("3_LETTERS", config.contentComplexity)
    }

    @Test
    fun `word LEGEND uses 12 letters and no hints`() {
        val config = GameConfigFactory.configFor(GameType.WORD_SCRAMBLE, AgeGroup.LEGEND)
        assertEquals("12_LETTERS", config.contentComplexity)
        assertFalse(config.hintAvailable)
    }

    // ── bonusXpMultiplier scales with age ────────────────────────────

    @Test
    fun `bonusXpMultiplier increases across age groups for all games`() {
        val gameTypes = listOf(
            GameType.MEMORY_GAME, GameType.SPEED_GAME, GameType.LOGIC_GAME,
            GameType.WORD_SCRAMBLE, GameType.CODE_CRACK, GameType.BUDGET_BOSS
        )
        gameTypes.forEach { game ->
            val sprout = GameConfigFactory.configFor(game, AgeGroup.SPROUT).bonusXpMultiplier
            val legend = GameConfigFactory.configFor(game, AgeGroup.LEGEND).bonusXpMultiplier
            assertTrue("${game.name}: LEGEND ($legend) should have higher multiplier than SPROUT ($sprout)",
                legend > sprout)
        }
    }

    @Test
    fun `SPROUT always has 1_0x multiplier`() {
        val games = listOf(
            GameType.MEMORY_GAME, GameType.SPEED_GAME, GameType.LOGIC_GAME,
            GameType.PATTERN_MATCH, GameType.COUNTING_GAME, GameType.SHAPE_SORT
        )
        games.forEach { game ->
            assertEquals("${game.name} SPROUT should be 1.0",
                1.0f, GameConfigFactory.configFor(game, AgeGroup.SPROUT).bonusXpMultiplier, 0.01f)
        }
    }

    @Test
    fun `LEGEND always has 2_0x multiplier`() {
        val games = listOf(
            GameType.MEMORY_GAME, GameType.SPEED_GAME, GameType.LOGIC_GAME,
            GameType.WORD_SCRAMBLE, GameType.CODE_CRACK
        )
        games.forEach { game ->
            assertEquals("${game.name} LEGEND should be 2.0",
                2.0f, GameConfigFactory.configFor(game, AgeGroup.LEGEND).bonusXpMultiplier, 0.01f)
        }
    }

    // ── NONE game type ──────────────────────────────────────────────

    @Test
    fun `NONE returns base config unchanged`() {
        val config = GameConfigFactory.configFor(GameType.NONE, AgeGroup.EXPLORER)
        assertEquals(GameType.NONE, config.gameType)
        assertEquals(AgeGroup.EXPLORER, config.ageGroup)
    }

    // ── allGamesForAgeGroup ─────────────────────────────────────────

    @Test
    fun `allGamesForAgeGroup returns configs for all available games`() {
        val configs = GameConfigFactory.allGamesForAgeGroup(AgeGroup.SPROUT)
        val expectedCount = GameType.forAgeGroup(AgeGroup.SPROUT).size
        assertEquals(expectedCount, configs.size)
    }

    @Test
    fun `allGamesForAgeGroup LEGEND has most configs`() {
        val sproutConfigs = GameConfigFactory.allGamesForAgeGroup(AgeGroup.SPROUT)
        val legendConfigs = GameConfigFactory.allGamesForAgeGroup(AgeGroup.LEGEND)
        assertTrue(legendConfigs.size > sproutConfigs.size)
    }

    @Test
    fun `allGamesForAgeGroup configs all have correct age group`() {
        val configs = GameConfigFactory.allGamesForAgeGroup(AgeGroup.TRAILBLAZER)
        configs.forEach { config ->
            assertEquals(AgeGroup.TRAILBLAZER, config.ageGroup)
        }
    }

    // ── Code Crack configs ──────────────────────────────────────────

    @Test
    fun `code crack SPROUT uses PATTERNS`() {
        val config = GameConfigFactory.configFor(GameType.CODE_CRACK, AgeGroup.SPROUT)
        assertEquals("PATTERNS", config.contentComplexity)
    }

    @Test
    fun `code crack LEGEND uses ALGORITHMS`() {
        val config = GameConfigFactory.configFor(GameType.CODE_CRACK, AgeGroup.LEGEND)
        assertEquals("ALGORITHMS", config.contentComplexity)
    }

    // ── Budget Boss configs ─────────────────────────────────────────

    @Test
    fun `budget boss SPROUT uses COUNTING`() {
        val config = GameConfigFactory.configFor(GameType.BUDGET_BOSS, AgeGroup.SPROUT)
        assertEquals("COUNTING", config.contentComplexity)
    }

    @Test
    fun `budget boss LEGEND uses REAL_WORLD`() {
        val config = GameConfigFactory.configFor(GameType.BUDGET_BOSS, AgeGroup.LEGEND)
        assertEquals("REAL_WORLD", config.contentComplexity)
    }

    // ── Estimation configs ──────────────────────────────────────────

    @Test
    fun `estimation LEGEND uses FERMI_ESTIMATION`() {
        val config = GameConfigFactory.configFor(GameType.ESTIMATION, AgeGroup.LEGEND)
        assertEquals("FERMI_ESTIMATION", config.contentComplexity)
    }

    // ── Typing Speed configs ────────────────────────────────────────

    @Test
    fun `typing SPROUT targets 5 in 30 sec`() {
        val config = GameConfigFactory.configFor(GameType.TYPING_SPEED, AgeGroup.SPROUT)
        assertEquals(5, config.targetScore)
        assertEquals(30, config.timeLimit)
    }

    @Test
    fun `typing LEGEND targets 30 in 30 sec`() {
        val config = GameConfigFactory.configFor(GameType.TYPING_SPEED, AgeGroup.LEGEND)
        assertEquals(30, config.targetScore)
        assertEquals(30, config.timeLimit)
    }

    // ── Debate Prompt configs ───────────────────────────────────────

    @Test
    fun `debate SPROUT uses PICTURE_CHOICE`() {
        val config = GameConfigFactory.configFor(GameType.DEBATE_PROMPT, AgeGroup.SPROUT)
        assertEquals("PICTURE_CHOICE", config.contentComplexity)
    }

    @Test
    fun `debate LEGEND uses ETHICAL_ESSAY`() {
        val config = GameConfigFactory.configFor(GameType.DEBATE_PROMPT, AgeGroup.LEGEND)
        assertEquals("ETHICAL_ESSAY", config.contentComplexity)
    }

    // ── Sequence configs ────────────────────────────────────────────

    @Test
    fun `sequence LEGEND has no hints`() {
        val config = GameConfigFactory.configFor(GameType.SEQUENCE, AgeGroup.LEGEND)
        assertFalse(config.hintAvailable)
    }

    @Test
    fun `sequence SPROUT has hints`() {
        val config = GameConfigFactory.configFor(GameType.SEQUENCE, AgeGroup.SPROUT)
        assertTrue(config.hintAvailable)
    }
}
