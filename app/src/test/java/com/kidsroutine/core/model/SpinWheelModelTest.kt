package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class SpinWheelModelTest {

    // ── SpinRewardType probabilities ────────────────────────────────

    @Test
    fun `probabilities sum to 1_0`() {
        val sum = SpinRewardType.entries.sumOf { it.probability.toDouble() }
        assertEquals(1.0, sum, 0.01)
    }

    @Test
    fun `all rewards have non-empty display names`() {
        SpinRewardType.entries.forEach {
            assertTrue("${it.name} should have display name", it.displayName.isNotEmpty())
        }
    }

    @Test
    fun `all rewards have non-empty emojis`() {
        SpinRewardType.entries.forEach {
            assertTrue("${it.name} should have emoji", it.emoji.isNotEmpty())
        }
    }

    @Test
    fun `randomReward returns a valid type`() {
        repeat(50) {
            val reward = SpinRewardType.randomReward()
            assertTrue(reward in SpinRewardType.entries)
        }
    }

    @Test
    fun `XP_JACKPOT has lowest probability`() {
        val jackpot = SpinRewardType.XP_JACKPOT
        SpinRewardType.entries.filter { it != jackpot }.forEach { other ->
            assertTrue(
                "XP_JACKPOT (${jackpot.probability}) should be <= ${other.name} (${other.probability})",
                jackpot.probability <= other.probability
            )
        }
    }

    @Test
    fun `XP_BOOST_BIG has lower probability than XP_BOOST_SMALL`() {
        assertTrue(SpinRewardType.XP_BOOST_BIG.probability < SpinRewardType.XP_BOOST_SMALL.probability)
    }

    @Test
    fun `all XP rewards have positive xpValue`() {
        val xpRewards = SpinRewardType.entries.filter { it.name.startsWith("XP_") }
        xpRewards.forEach { reward ->
            assertTrue("${reward.name} should have positive xpValue", reward.xpValue > 0)
        }
    }

    @Test
    fun `FREE_SPIN and NOTHING have zero xpValue`() {
        assertEquals(0, SpinRewardType.FREE_SPIN.xpValue)
        assertEquals(0, SpinRewardType.NOTHING.xpValue)
    }

    @Test
    fun `there are 8 reward types`() {
        assertEquals(8, SpinRewardType.entries.size)
    }

    // ── DailySpinState.canSpin ──────────────────────────────────────

    @Test
    fun `canSpin true when no spins used`() {
        val state = DailySpinState(spinsUsed = 0, maxSpins = 1)
        assertTrue(state.canSpin)
    }

    @Test
    fun `canSpin false when spins exhausted`() {
        val state = DailySpinState(spinsUsed = 1, maxSpins = 1)
        assertFalse(state.canSpin)
    }

    @Test
    fun `canSpin true for premium user with 1 spin used`() {
        val state = DailySpinState(spinsUsed = 1, maxSpins = 2)
        assertTrue(state.canSpin)
    }

    @Test
    fun `canSpin false for premium user with 2 spins used`() {
        val state = DailySpinState(spinsUsed = 2, maxSpins = 2)
        assertFalse(state.canSpin)
    }

    // ── SpinWheelResult defaults ────────────────────────────────────

    @Test
    fun `default spin result is NOTHING`() {
        val result = SpinWheelResult()
        assertEquals(SpinRewardType.NOTHING, result.reward)
    }

    @Test
    fun `default spin number is 1`() {
        val result = SpinWheelResult()
        assertEquals(1, result.spinNumber)
    }

    // ── DailySpinState defaults ─────────────────────────────────────

    @Test
    fun `default maxSpins is 1`() {
        val state = DailySpinState()
        assertEquals(1, state.maxSpins)
    }
}
