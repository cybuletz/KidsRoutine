package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class DailyStateModelTest {

    // ── completionPercent ───────────────────────────────────────────

    @Test
    fun `completionPercent at zero tasks`() {
        val state = DailyStateModel(completedCount = 0, totalTasksAssigned = 0)
        assertEquals(0f, state.completionPercent, 0.01f)
    }

    @Test
    fun `completionPercent at half`() {
        val state = DailyStateModel(completedCount = 2, totalTasksAssigned = 4)
        assertEquals(0.5f, state.completionPercent, 0.01f)
    }

    @Test
    fun `completionPercent at 100 percent`() {
        val state = DailyStateModel(completedCount = 5, totalTasksAssigned = 5)
        assertEquals(1.0f, state.completionPercent, 0.01f)
    }

    @Test
    fun `completionPercent partial`() {
        val state = DailyStateModel(completedCount = 3, totalTasksAssigned = 5)
        assertEquals(0.6f, state.completionPercent, 0.01f)
    }

    // ── Default values ──────────────────────────────────────────────

    @Test
    fun `default isGenerated is false`() {
        val state = DailyStateModel()
        assertFalse(state.isGenerated)
    }

    @Test
    fun `default tasks list is empty`() {
        val state = DailyStateModel()
        assertTrue(state.tasks.isEmpty())
    }

    @Test
    fun `default totalXpEarned is 0`() {
        val state = DailyStateModel()
        assertEquals(0, state.totalXpEarned)
    }
}
