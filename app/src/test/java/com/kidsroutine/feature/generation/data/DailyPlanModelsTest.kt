package com.kidsroutine.feature.generation.data

import org.junit.Assert.*
import org.junit.Test

class DailyPlanModelsTest {

    // ── DayMood enum ────────────────────────────────────────────────

    @Test
    fun `DayMood has 3 entries`() {
        assertEquals(3, DayMood.entries.size)
    }

    @Test
    fun `DayMood all have non-empty emojis`() {
        DayMood.entries.forEach {
            assertTrue("${it.name} emoji", it.emoji.isNotEmpty())
        }
    }

    @Test
    fun `DayMood all have non-empty labels`() {
        DayMood.entries.forEach {
            assertTrue("${it.name} label", it.label.isNotEmpty())
        }
    }

    @Test
    fun `DayMood all have non-empty descriptions`() {
        DayMood.entries.forEach {
            assertTrue("${it.name} description", it.description.isNotEmpty())
        }
    }

    // ── GeneratedPlanTask ───────────────────────────────────────────

    @Test
    fun `plan task stores all fields`() {
        val task = GeneratedPlanTask(
            title = "Morning Yoga",
            description = "10 min stretching",
            estimatedDurationSec = 600,
            category = "HEALTH",
            difficulty = "EASY",
            xpReward = 15,
            type = "REAL_LIFE",
            timeSlot = "MORNING",
            requiresCoop = false
        )
        assertEquals("Morning Yoga", task.title)
        assertEquals(600, task.estimatedDurationSec)
        assertEquals("MORNING", task.timeSlot)
        assertFalse(task.requiresCoop)
    }

    @Test
    fun `plan task requiresCoop defaults to false`() {
        val task = GeneratedPlanTask(
            title = "", description = "", estimatedDurationSec = 0,
            category = "", difficulty = "", xpReward = 0, type = "",
            timeSlot = ""
        )
        assertFalse(task.requiresCoop)
    }

    // ── GeneratedDailyPlan ──────────────────────────────────────────

    @Test
    fun `daily plan stores theme and totalXp`() {
        val plan = GeneratedDailyPlan(
            theme = "Adventure Day",
            totalXp = 150,
            mood = "ENERGETIC",
            tasks = emptyList()
        )
        assertEquals("Adventure Day", plan.theme)
        assertEquals(150, plan.totalXp)
        assertEquals("ENERGETIC", plan.mood)
    }

    // ── DailyPlanResponse ───────────────────────────────────────────

    @Test
    fun `response stores success and quota`() {
        val response = DailyPlanResponse(
            success = true,
            plan = GeneratedDailyPlan("Theme", 100, "NORMAL", emptyList()),
            cached = false,
            quotaRemaining = 3
        )
        assertTrue(response.success)
        assertNotNull(response.plan)
        assertEquals(3, response.quotaRemaining)
        assertFalse(response.cached)
    }

    @Test
    fun `failed response has null plan`() {
        val response = DailyPlanResponse(
            success = false,
            plan = null,
            cached = false,
            quotaRemaining = 0
        )
        assertFalse(response.success)
        assertNull(response.plan)
    }
}
