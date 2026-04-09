package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class TaskCategoryTest {

    @Test
    fun `has 10 entries`() {
        assertEquals(10, TaskCategory.entries.size)
    }

    @Test
    fun `includes all expected categories`() {
        val names = TaskCategory.entries.map { it.name }
        assertTrue(names.contains("MORNING_ROUTINE"))
        assertTrue(names.contains("HEALTH"))
        assertTrue(names.contains("LEARNING"))
        assertTrue(names.contains("CREATIVITY"))
        assertTrue(names.contains("SOCIAL"))
        assertTrue(names.contains("FAMILY"))
        assertTrue(names.contains("CHORES"))
        assertTrue(names.contains("OUTDOOR"))
        assertTrue(names.contains("SLEEP"))
        assertTrue(names.contains("SCREEN_TIME"))
    }

    @Test
    fun `valueOf round-trips`() {
        for (cat in TaskCategory.entries) {
            assertEquals(cat, TaskCategory.valueOf(cat.name))
        }
    }

    @Test
    fun `MORNING_ROUTINE is first`() {
        assertEquals(0, TaskCategory.MORNING_ROUTINE.ordinal)
    }

    @Test
    fun `SCREEN_TIME is last`() {
        assertEquals(9, TaskCategory.SCREEN_TIME.ordinal)
    }
}
