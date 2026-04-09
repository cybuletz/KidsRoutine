package com.kidsroutine.core.engine.progression_engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class XpCalculatorTest {

    private lateinit var calculator: XpCalculator

    @Before
    fun setUp() {
        calculator = XpCalculator()
    }

    // ── forTask ─────────────────────────────────────────────────────────

    @Test
    fun `forTask returns base XP for EASY task with no bonuses`() {
        val task = TestTaskFactory.create(xp = 10, difficulty = com.kidsroutine.core.model.DifficultyLevel.EASY)
        val result = calculator.forTask(task)
        assertEquals(10, result) // 10 * 1.0 * 1.0 * 1.0
    }

    @Test
    fun `forTask applies 1_5x multiplier for MEDIUM`() {
        val task = TestTaskFactory.create(xp = 10, difficulty = com.kidsroutine.core.model.DifficultyLevel.MEDIUM)
        val result = calculator.forTask(task)
        assertEquals(15, result) // 10 * 1.5
    }

    @Test
    fun `forTask applies 2x multiplier for HARD`() {
        val task = TestTaskFactory.create(xp = 10, difficulty = com.kidsroutine.core.model.DifficultyLevel.HARD)
        val result = calculator.forTask(task)
        assertEquals(20, result) // 10 * 2.0
    }

    @Test
    fun `forTask applies coop bonus of 1_25x`() {
        val task = TestTaskFactory.create(xp = 10, difficulty = com.kidsroutine.core.model.DifficultyLevel.EASY)
        val result = calculator.forTask(task, isCoop = true)
        assertEquals(12, result) // 10 * 1.0 * 1.25 = 12.5 → 12
    }

    @Test
    fun `forTask applies streak bonus of 1_1x`() {
        val task = TestTaskFactory.create(xp = 10, difficulty = com.kidsroutine.core.model.DifficultyLevel.EASY)
        val result = calculator.forTask(task, isStreakBonus = true)
        assertEquals(11, result) // 10 * 1.0 * 1.0 * 1.1
    }

    @Test
    fun `forTask stacks all multipliers correctly`() {
        val task = TestTaskFactory.create(xp = 20, difficulty = com.kidsroutine.core.model.DifficultyLevel.HARD)
        val result = calculator.forTask(task, isCoop = true, isStreakBonus = true)
        // 20 * 2.0 * 1.25 * 1.1 = 55.0
        assertEquals(55, result)
    }

    @Test
    fun `forTask handles zero base XP`() {
        val task = TestTaskFactory.create(xp = 0, difficulty = com.kidsroutine.core.model.DifficultyLevel.HARD)
        val result = calculator.forTask(task)
        assertEquals(0, result)
    }

    // ── levelForXp ──────────────────────────────────────────────────────

    @Test
    fun `levelForXp returns 1 for 0 XP`() {
        assertEquals(1, calculator.levelForXp(0))
    }

    @Test
    fun `levelForXp returns 1 for 49 XP`() {
        assertEquals(1, calculator.levelForXp(49))
    }

    @Test
    fun `levelForXp returns 2 for 50 XP`() {
        assertEquals(2, calculator.levelForXp(50))
    }

    @Test
    fun `levelForXp returns 3 for 200 XP`() {
        assertEquals(3, calculator.levelForXp(200))
    }

    @Test
    fun `levelForXp uses progressive curve`() {
        // Level 5 requires (4^2)*50 = 800 XP
        assertTrue(calculator.levelForXp(800) >= 5)
    }

    // ── xpToNextLevel ───────────────────────────────────────────────────

    @Test
    fun `xpToNextLevel returns remaining XP to next level`() {
        // Level 1 at 0 XP → next level at 50 XP → remaining = 50
        val remaining = calculator.xpToNextLevel(0)
        assertEquals(50, remaining)
    }

    @Test
    fun `xpToNextLevel is never negative`() {
        val remaining = calculator.xpToNextLevel(9999)
        assertTrue(remaining >= 0)
    }
}
