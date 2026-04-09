package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class DifficultyLevelTest {

    @Test
    fun `has 3 entries`() {
        assertEquals(3, DifficultyLevel.entries.size)
    }

    @Test
    fun `includes EASY`() {
        assertTrue(DifficultyLevel.entries.map { it.name }.contains("EASY"))
    }

    @Test
    fun `includes MEDIUM`() {
        assertTrue(DifficultyLevel.entries.map { it.name }.contains("MEDIUM"))
    }

    @Test
    fun `includes HARD`() {
        assertTrue(DifficultyLevel.entries.map { it.name }.contains("HARD"))
    }

    @Test
    fun `valueOf round-trips`() {
        for (level in DifficultyLevel.entries) {
            assertEquals(level, DifficultyLevel.valueOf(level.name))
        }
    }

    @Test
    fun `ordinal order is EASY MEDIUM HARD`() {
        assertEquals(0, DifficultyLevel.EASY.ordinal)
        assertEquals(1, DifficultyLevel.MEDIUM.ordinal)
        assertEquals(2, DifficultyLevel.HARD.ordinal)
    }
}
