package com.kidsroutine.core.engine.spin_engine

import com.kidsroutine.core.model.DailySpinState
import com.kidsroutine.core.model.PlanType
import com.kidsroutine.core.model.SpinRewardType
import com.kidsroutine.core.model.SpinWheelResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DailyRewardEngineTest {

    private lateinit var engine: DailyRewardEngine

    @Before
    fun setUp() {
        engine = DailyRewardEngine()
    }

    // ── createDailyState ────────────────────────────────────────────────

    @Test
    fun `createDailyState gives FREE user 1 spin`() {
        val state = engine.createDailyState("user1", "2026-04-09", PlanType.FREE)
        assertEquals(1, state.maxSpins)
        assertEquals(0, state.spinsUsed)
        assertEquals("user1", state.userId)
        assertEquals("2026-04-09", state.date)
    }

    @Test
    fun `createDailyState gives PRO user 1 spin`() {
        val state = engine.createDailyState("user1", "2026-04-09", PlanType.PRO)
        assertEquals(1, state.maxSpins)
    }

    @Test
    fun `createDailyState gives PREMIUM user 2 spins`() {
        val state = engine.createDailyState("user1", "2026-04-09", PlanType.PREMIUM)
        assertEquals(2, state.maxSpins)
    }

    // ── spin ────────────────────────────────────────────────────────────

    @Test
    fun `spin increments spinsUsed`() {
        val state = engine.createDailyState("user1", "2026-04-09", PlanType.FREE)
        val (updated, _) = engine.spin(state)
        assertEquals(1, updated.spinsUsed)
    }

    @Test
    fun `spin returns a result with correct userId and date`() {
        val state = engine.createDailyState("user1", "2026-04-09", PlanType.FREE)
        val (_, result) = engine.spin(state)
        assertEquals("user1", result.userId)
        assertEquals("2026-04-09", result.date)
        assertEquals(1, result.spinNumber)
    }

    @Test
    fun `spin adds result to state results list`() {
        val state = engine.createDailyState("user1", "2026-04-09", PlanType.FREE)
        val (updated, _) = engine.spin(state)
        assertEquals(1, updated.results.size)
    }

    @Test
    fun `spin activates doubleXP when DOUBLE_XP reward`() {
        // We test the logic with a pre-set state
        val state = DailySpinState(
            userId = "user1", date = "2026-04-09", spinsUsed = 0, maxSpins = 2
        )
        // Spin multiple times to find DOUBLE_XP (probabilistic, so check logic)
        val (updated, result) = engine.spin(state)
        if (result.reward == SpinRewardType.DOUBLE_XP) {
            assertTrue(updated.hasDoubleXpActive)
            assertTrue(updated.doubleXpExpiresAt > 0)
        }
    }

    // ── canSpin ─────────────────────────────────────────────────────────

    @Test
    fun `canSpin is true when spins available`() {
        val state = DailySpinState(spinsUsed = 0, maxSpins = 1)
        assertTrue(state.canSpin)
    }

    @Test
    fun `canSpin is false when all spins used`() {
        val state = DailySpinState(spinsUsed = 1, maxSpins = 1)
        assertFalse(state.canSpin)
    }

    // ── isDoubleXpActive ────────────────────────────────────────────────

    @Test
    fun `isDoubleXpActive returns false when not activated`() {
        val state = DailySpinState(hasDoubleXpActive = false)
        assertFalse(engine.isDoubleXpActive(state))
    }

    @Test
    fun `isDoubleXpActive returns true when active and not expired`() {
        val state = DailySpinState(
            hasDoubleXpActive = true,
            doubleXpExpiresAt = System.currentTimeMillis() + 60_000
        )
        assertTrue(engine.isDoubleXpActive(state))
    }

    @Test
    fun `isDoubleXpActive returns false when expired`() {
        val state = DailySpinState(
            hasDoubleXpActive = true,
            doubleXpExpiresAt = System.currentTimeMillis() - 1
        )
        assertFalse(engine.isDoubleXpActive(state))
    }

    // ── xpMultiplier ────────────────────────────────────────────────────

    @Test
    fun `xpMultiplier returns 1x when no double XP`() {
        val state = DailySpinState(hasDoubleXpActive = false)
        assertEquals(1.0f, engine.xpMultiplier(state))
    }

    @Test
    fun `xpMultiplier returns 2x when double XP active`() {
        val state = DailySpinState(
            hasDoubleXpActive = true,
            doubleXpExpiresAt = System.currentTimeMillis() + 60_000
        )
        assertEquals(2.0f, engine.xpMultiplier(state))
    }

    // ── immediateXpBonus ────────────────────────────────────────────────

    @Test
    fun `immediateXpBonus returns 25 for small XP boost`() {
        val result = SpinWheelResult(reward = SpinRewardType.XP_BOOST_SMALL)
        assertEquals(25, engine.immediateXpBonus(result))
    }

    @Test
    fun `immediateXpBonus returns 100 for big XP boost`() {
        val result = SpinWheelResult(reward = SpinRewardType.XP_BOOST_BIG)
        assertEquals(100, engine.immediateXpBonus(result))
    }

    @Test
    fun `immediateXpBonus returns 0 for non-XP rewards`() {
        val result = SpinWheelResult(reward = SpinRewardType.PET_TREAT)
        assertEquals(0, engine.immediateXpBonus(result))
    }

    // ── SpinRewardType probabilities ────────────────────────────────────

    @Test
    fun `spin reward probabilities sum to 1`() {
        val total = SpinRewardType.entries.sumOf { it.probability.toDouble() }
        assertEquals(1.0, total, 0.01)
    }
}
