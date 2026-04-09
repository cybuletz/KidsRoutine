package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class TaskCreatorTest {

    @Test
    fun `has 5 entries`() {
        assertEquals(5, TaskCreator.entries.size)
    }

    @Test
    fun `includes all expected creators`() {
        val names = TaskCreator.entries.map { it.name }
        assertTrue(names.contains("SYSTEM"))
        assertTrue(names.contains("PARENT"))
        assertTrue(names.contains("COMMUNITY"))
        assertTrue(names.contains("CHILD"))
        assertTrue(names.contains("AI_GENERATED"))
    }

    @Test
    fun `valueOf round-trips`() {
        for (c in TaskCreator.entries) {
            assertEquals(c, TaskCreator.valueOf(c.name))
        }
    }

    @Test
    fun `SYSTEM is first`() {
        assertEquals(0, TaskCreator.SYSTEM.ordinal)
    }

    @Test
    fun `AI_GENERATED is last`() {
        assertEquals(4, TaskCreator.AI_GENERATED.ordinal)
    }
}
