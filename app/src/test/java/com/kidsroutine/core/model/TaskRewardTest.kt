package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class TaskRewardTest {

    // ── defaults ────────────────────────────────────────────────────

    @Test
    fun `default xp is 0`() {
        assertEquals(0, TaskReward().xp)
    }

    @Test
    fun `default bonusConditions is empty`() {
        assertTrue(TaskReward().bonusConditions.isEmpty())
    }

    // ── custom values ───────────────────────────────────────────────

    @Test
    fun `stores xp`() {
        assertEquals(42, TaskReward(xp = 42).xp)
    }

    @Test
    fun `stores bonusConditions`() {
        val reward = TaskReward(bonusConditions = listOf("streak_3", "first_try"))
        assertEquals(2, reward.bonusConditions.size)
        assertEquals("streak_3", reward.bonusConditions[0])
    }

    @Test
    fun `stores both fields`() {
        val reward = TaskReward(xp = 100, bonusConditions = listOf("fast"))
        assertEquals(100, reward.xp)
        assertEquals(1, reward.bonusConditions.size)
    }

    // ── equality / copy ─────────────────────────────────────────────

    @Test
    fun `data class equality`() {
        val a = TaskReward(xp = 10, bonusConditions = listOf("b"))
        val b = TaskReward(xp = 10, bonusConditions = listOf("b"))
        assertEquals(a, b)
    }

    @Test
    fun `data class inequality`() {
        val a = TaskReward(xp = 10)
        val b = TaskReward(xp = 20)
        assertNotEquals(a, b)
    }

    @Test
    fun `copy updates xp`() {
        val orig = TaskReward(xp = 10)
        val updated = orig.copy(xp = 50)
        assertEquals(50, updated.xp)
        assertTrue(updated.bonusConditions.isEmpty())
    }
}
