package com.kidsroutine.core.engine.progression_engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StreakCalculatorTest {

    private lateinit var calculator: StreakCalculator

    @Before
    fun setUp() {
        calculator = StreakCalculator()
    }

    // ── computeStreak ───────────────────────────────────────────────

    @Test
    fun `same day returns current streak unchanged`() {
        assertEquals(5, calculator.computeStreak(5, "2026-04-09", "2026-04-09"))
    }

    @Test
    fun `consecutive day increments streak by 1`() {
        assertEquals(6, calculator.computeStreak(5, "2026-04-08", "2026-04-09"))
    }

    @Test
    fun `2 day gap resets streak to 1`() {
        assertEquals(1, calculator.computeStreak(10, "2026-04-07", "2026-04-09"))
    }

    @Test
    fun `3 day gap resets streak to 1`() {
        assertEquals(1, calculator.computeStreak(10, "2026-04-06", "2026-04-09"))
    }

    @Test
    fun `blank lastActiveDate returns 1`() {
        assertEquals(1, calculator.computeStreak(5, "", "2026-04-09"))
    }

    @Test
    fun `first day ever returns 1`() {
        assertEquals(1, calculator.computeStreak(0, "", "2026-04-09"))
    }

    // ── shield logic ────────────────────────────────────────────────

    @Test
    fun `shield protects streak on 1 missed day gap`() {
        // daysDiff == 2 (skipped exactly 1 day) + shield active → preserve streak
        assertEquals(5, calculator.computeStreak(5, "2026-04-07", "2026-04-09", shieldActive = true))
    }

    @Test
    fun `shield does not protect on 2+ missed days`() {
        // daysDiff == 3 → too many days, shield doesn't help
        assertEquals(1, calculator.computeStreak(5, "2026-04-06", "2026-04-09", shieldActive = true))
    }

    @Test
    fun `shield does not change same-day behavior`() {
        assertEquals(5, calculator.computeStreak(5, "2026-04-09", "2026-04-09", shieldActive = true))
    }

    @Test
    fun `shield does not change consecutive behavior`() {
        assertEquals(6, calculator.computeStreak(5, "2026-04-08", "2026-04-09", shieldActive = true))
    }

    @Test
    fun `no shield means 1 missed day resets streak`() {
        assertEquals(1, calculator.computeStreak(5, "2026-04-07", "2026-04-09", shieldActive = false))
    }

    // ── shouldConsumeShield ────────────────────────────────────────

    @Test
    fun `shouldConsumeShield returns true when gap is exactly 1 missed day with shield`() {
        assertTrue(calculator.shouldConsumeShield("2026-04-07", "2026-04-09", shieldActive = true))
    }

    @Test
    fun `shouldConsumeShield returns false when shield is inactive`() {
        assertFalse(calculator.shouldConsumeShield("2026-04-07", "2026-04-09", shieldActive = false))
    }

    @Test
    fun `shouldConsumeShield returns false when gap is larger than 1 missed day`() {
        assertFalse(calculator.shouldConsumeShield("2026-04-06", "2026-04-09", shieldActive = true))
    }

    @Test
    fun `shouldConsumeShield returns false for consecutive days`() {
        assertFalse(calculator.shouldConsumeShield("2026-04-08", "2026-04-09", shieldActive = true))
    }

    @Test
    fun `shouldConsumeShield returns false for blank lastActiveDate`() {
        assertFalse(calculator.shouldConsumeShield("", "2026-04-09", shieldActive = true))
    }
}
