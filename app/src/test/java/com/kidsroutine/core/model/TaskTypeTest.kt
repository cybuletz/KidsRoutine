package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class TaskTypeTest {

    @Test
    fun `has 8 entries`() {
        assertEquals(8, TaskType.entries.size)
    }

    @Test
    fun `includes all expected types`() {
        val names = TaskType.entries.map { it.name }
        assertTrue(names.contains("LOGIC"))
        assertTrue(names.contains("REAL_LIFE"))
        assertTrue(names.contains("CREATIVE"))
        assertTrue(names.contains("LEARNING"))
        assertTrue(names.contains("EMOTIONAL"))
        assertTrue(names.contains("CO_OP"))
        assertTrue(names.contains("SOCIAL"))
        assertTrue(names.contains("STORY"))
    }

    @Test
    fun `valueOf round-trips`() {
        for (t in TaskType.entries) {
            assertEquals(t, TaskType.valueOf(t.name))
        }
    }

    @Test
    fun `LOGIC is first`() {
        assertEquals(0, TaskType.LOGIC.ordinal)
    }

    @Test
    fun `STORY is last`() {
        assertEquals(7, TaskType.STORY.ordinal)
    }
}
