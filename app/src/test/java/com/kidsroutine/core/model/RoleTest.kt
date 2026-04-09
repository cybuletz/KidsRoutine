package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class RoleTest {

    @Test
    fun `has 2 entries`() {
        assertEquals(2, Role.entries.size)
    }

    @Test
    fun `includes CHILD`() {
        assertTrue(Role.entries.map { it.name }.contains("CHILD"))
    }

    @Test
    fun `includes PARENT`() {
        assertTrue(Role.entries.map { it.name }.contains("PARENT"))
    }

    @Test
    fun `CHILD ordinal is 0`() {
        assertEquals(0, Role.CHILD.ordinal)
    }

    @Test
    fun `PARENT ordinal is 1`() {
        assertEquals(1, Role.PARENT.ordinal)
    }

    @Test
    fun `valueOf round-trips`() {
        for (r in Role.entries) {
            assertEquals(r, Role.valueOf(r.name))
        }
    }
}
