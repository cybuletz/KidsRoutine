package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class AgeGroupTest {

    // ── fromAge ─────────────────────────────────────────────────────

    @Test
    fun `fromAge returns SPROUT for ages 4-7`() {
        assertEquals(AgeGroup.SPROUT, AgeGroup.fromAge(4))
        assertEquals(AgeGroup.SPROUT, AgeGroup.fromAge(7))
    }

    @Test
    fun `fromAge returns EXPLORER for ages 8-12`() {
        assertEquals(AgeGroup.EXPLORER, AgeGroup.fromAge(8))
        assertEquals(AgeGroup.EXPLORER, AgeGroup.fromAge(12))
    }

    @Test
    fun `fromAge returns TRAILBLAZER for ages 13-16`() {
        assertEquals(AgeGroup.TRAILBLAZER, AgeGroup.fromAge(13))
        assertEquals(AgeGroup.TRAILBLAZER, AgeGroup.fromAge(16))
    }

    @Test
    fun `fromAge returns LEGEND for ages 17+`() {
        assertEquals(AgeGroup.LEGEND, AgeGroup.fromAge(17))
        assertEquals(AgeGroup.LEGEND, AgeGroup.fromAge(30))
    }

    @Test
    fun `fromAge returns SPROUT for very young`() {
        assertEquals(AgeGroup.SPROUT, AgeGroup.fromAge(3))
    }

    // ── isTeenMode ──────────────────────────────────────────────────

    @Test
    fun `SPROUT is not teen mode`() {
        assertFalse(AgeGroup.SPROUT.isTeenMode)
    }

    @Test
    fun `EXPLORER is not teen mode`() {
        assertFalse(AgeGroup.EXPLORER.isTeenMode)
    }

    @Test
    fun `TRAILBLAZER is teen mode`() {
        assertTrue(AgeGroup.TRAILBLAZER.isTeenMode)
    }

    @Test
    fun `LEGEND is teen mode`() {
        assertTrue(AgeGroup.LEGEND.isTeenMode)
    }

    // ── prefersDarkTheme ────────────────────────────────────────────

    @Test
    fun `young groups prefer light theme`() {
        assertFalse(AgeGroup.SPROUT.prefersDarkTheme)
        assertFalse(AgeGroup.EXPLORER.prefersDarkTheme)
    }

    @Test
    fun `teen groups prefer dark theme`() {
        assertTrue(AgeGroup.TRAILBLAZER.prefersDarkTheme)
        assertTrue(AgeGroup.LEGEND.prefersDarkTheme)
    }

    // ── autonomyLevel ───────────────────────────────────────────────

    @Test
    fun `SPROUT has autonomy 0`() {
        assertEquals(0, AgeGroup.SPROUT.autonomyLevel)
    }

    @Test
    fun `EXPLORER has autonomy 1`() {
        assertEquals(1, AgeGroup.EXPLORER.autonomyLevel)
    }

    @Test
    fun `TRAILBLAZER has autonomy 2`() {
        assertEquals(2, AgeGroup.TRAILBLAZER.autonomyLevel)
    }

    @Test
    fun `LEGEND has autonomy 3`() {
        assertEquals(3, AgeGroup.LEGEND.autonomyLevel)
    }

    // ── enum properties ─────────────────────────────────────────────

    @Test
    fun `all age groups have non-empty display names`() {
        AgeGroup.entries.forEach {
            assertTrue(it.displayName.isNotEmpty())
        }
    }

    @Test
    fun `all age groups have non-empty emojis`() {
        AgeGroup.entries.forEach {
            assertTrue(it.emoji.isNotEmpty())
        }
    }

    @Test
    fun `age ranges are contiguous`() {
        // SPROUT 4-7, EXPLORER 8-12, TRAILBLAZER 13-16, LEGEND 17-99
        assertEquals(AgeGroup.SPROUT.maxAge + 1, AgeGroup.EXPLORER.minAge)
        assertEquals(AgeGroup.EXPLORER.maxAge + 1, AgeGroup.TRAILBLAZER.minAge)
        assertEquals(AgeGroup.TRAILBLAZER.maxAge + 1, AgeGroup.LEGEND.minAge)
    }
}
