package com.kidsroutine.feature.generation.data

import org.junit.Assert.*
import org.junit.Test

class WeeklyPlanModelsTest {

    // ── WeekTheme enum ──────────────────────────────────────────────

    @Test
    fun `WeekTheme has 4 entries`() {
        assertEquals(4, WeekTheme.entries.size)
    }

    @Test
    fun `WeekTheme all have non-empty emojis`() {
        WeekTheme.entries.forEach {
            assertTrue("${it.name} emoji", it.emoji.isNotEmpty())
        }
    }

    @Test
    fun `WeekTheme all have non-empty labels`() {
        WeekTheme.entries.forEach {
            assertTrue("${it.name} label", it.label.isNotEmpty())
        }
    }

    @Test
    fun `WeekTheme all have non-empty descriptions`() {
        WeekTheme.entries.forEach {
            assertTrue("${it.name} description", it.description.isNotEmpty())
        }
    }

    @Test
    fun `WeekTheme includes ADVENTURE DISCIPLINE CREATIVITY WELLNESS`() {
        val names = WeekTheme.entries.map { it.name }
        assertTrue(names.contains("ADVENTURE"))
        assertTrue(names.contains("DISCIPLINE"))
        assertTrue(names.contains("CREATIVITY"))
        assertTrue(names.contains("WELLNESS"))
    }

    // ── WeeklyTask ──────────────────────────────────────────────────

    @Test
    fun `weekly task requiresCoop defaults to false`() {
        val task = WeeklyTask(
            childName = "", title = "", description = "",
            estimatedDurationSec = 0, category = "", difficulty = "",
            xpReward = 0, type = ""
        )
        assertFalse(task.requiresCoop)
    }

    @Test
    fun `weekly task stores all fields`() {
        val task = WeeklyTask(
            childName = "Alice",
            title = "Study Math",
            description = "Practice multiplication",
            estimatedDurationSec = 1800,
            category = "LEARNING",
            difficulty = "MEDIUM",
            xpReward = 25,
            type = "LEARNING",
            requiresCoop = true
        )
        assertEquals("Alice", task.childName)
        assertEquals("Study Math", task.title)
        assertEquals(1800, task.estimatedDurationSec)
        assertTrue(task.requiresCoop)
    }

    // ── WeeklyDayPlan ───────────────────────────────────────────────

    @Test
    fun `day plan stores name and tasks`() {
        val tasks = listOf(
            WeeklyTask("A", "T1", "", 0, "", "", 0, ""),
            WeeklyTask("B", "T2", "", 0, "", "", 0, "")
        )
        val dayPlan = WeeklyDayPlan(
            dayName = "Monday",
            dayEmoji = "🌅",
            tasks = tasks
        )
        assertEquals("Monday", dayPlan.dayName)
        assertEquals("🌅", dayPlan.dayEmoji)
        assertEquals(2, dayPlan.tasks.size)
    }

    // ── GeneratedWeeklyPlan ─────────────────────────────────────────

    @Test
    fun `weekly plan stores theme and total XP`() {
        val plan = GeneratedWeeklyPlan(
            weekTheme = "Adventure",
            totalFamilyXp = 500,
            days = emptyList()
        )
        assertEquals("Adventure", plan.weekTheme)
        assertEquals(500, plan.totalFamilyXp)
        assertTrue(plan.days.isEmpty())
    }

    // ── WeeklyPlanResponse ──────────────────────────────────────────

    @Test
    fun `response stores success and quota`() {
        val response = WeeklyPlanResponse(
            success = true,
            weeklyPlan = GeneratedWeeklyPlan("Theme", 100, emptyList()),
            cached = false,
            quotaRemaining = 2
        )
        assertTrue(response.success)
        assertNotNull(response.weeklyPlan)
        assertEquals(2, response.quotaRemaining)
    }

    @Test
    fun `failed response has null plan`() {
        val response = WeeklyPlanResponse(
            success = false,
            weeklyPlan = null,
            cached = false,
            quotaRemaining = 0
        )
        assertFalse(response.success)
        assertNull(response.weeklyPlan)
    }
}
