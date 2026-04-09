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
    fun `immediateXpBonus returns 200 for jackpot`() {
        val result = SpinWheelResult(reward = SpinRewardType.XP_JACKPOT)
        assertEquals(200, engine.immediateXpBonus(result))
    }

    @Test
    fun `immediateXpBonus returns 50 for medium XP boost`() {
        val result = SpinWheelResult(reward = SpinRewardType.XP_BOOST_MEDIUM)
        assertEquals(50, engine.immediateXpBonus(result))
    }

    @Test
    fun `immediateXpBonus returns 10 for mini XP boost`() {
        val result = SpinWheelResult(reward = SpinRewardType.XP_BOOST_MINI)
        assertEquals(10, engine.immediateXpBonus(result))
    }

    @Test
    fun `immediateXpBonus returns 5 for tiny XP boost`() {
        val result = SpinWheelResult(reward = SpinRewardType.XP_BOOST_TINY)
        assertEquals(5, engine.immediateXpBonus(result))
    }

    @Test
    fun `immediateXpBonus returns 0 for FREE_SPIN`() {
        val result = SpinWheelResult(reward = SpinRewardType.FREE_SPIN)
        assertEquals(0, engine.immediateXpBonus(result))
    }

    @Test
    fun `immediateXpBonus returns 0 for NOTHING`() {
        val result = SpinWheelResult(reward = SpinRewardType.NOTHING)
        assertEquals(0, engine.immediateXpBonus(result))
    }

    // ── SpinRewardType probabilities ────────────────────────────────────

    @Test
    fun `spin reward probabilities sum to 1`() {
        val total = SpinRewardType.entries.sumOf { it.probability.toDouble() }
        assertEquals(1.0, total, 0.01)
    }
}
