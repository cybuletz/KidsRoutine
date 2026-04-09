package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class ValidationTypeTest {

    @Test
    fun `has 5 entries`() {
        assertEquals(5, ValidationType.entries.size)
    }

    @Test
    fun `includes all expected types`() {
        val names = ValidationType.entries.map { it.name }
        assertTrue(names.contains("AUTO"))
        assertTrue(names.contains("SELF"))
        assertTrue(names.contains("PHOTO_REQUIRED"))
        assertTrue(names.contains("PARENT_REQUIRED"))
        assertTrue(names.contains("HYBRID"))
    }

    @Test
    fun `valueOf round-trips`() {
        for (v in ValidationType.entries) {
            assertEquals(v, ValidationType.valueOf(v.name))
        }
    }

    @Test
    fun `AUTO is first`() {
        assertEquals(0, ValidationType.AUTO.ordinal)
    }

    @Test
    fun `HYBRID is last`() {
        assertEquals(4, ValidationType.HYBRID.ordinal)
    }
}
