package com.kidsroutine.core.engine

import com.kidsroutine.core.model.Season
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class SeasonalThemeManagerTest {

    private val manager = SeasonalThemeManager()

    // ── getActiveTheme returns active theme ─────────────────────────

    @Test
    fun `getActiveTheme returns active theme`() {
        val theme = manager.getActiveTheme()
        assertTrue(theme.isActive)
    }

    @Test
    fun `getActiveTheme has non-empty display name`() {
        val theme = manager.getActiveTheme()
        assertTrue(theme.displayName.isNotEmpty())
    }

    @Test
    fun `getActiveTheme has non-empty emoji`() {
        val theme = manager.getActiveTheme()
        assertTrue(theme.emoji.isNotEmpty())
    }

    // ── currentSeason returns a valid season ────────────────────────

    @Test
    fun `currentSeason returns non-NONE`() {
        val season = manager.currentSeason()
        assertNotEquals(Season.NONE, season)
    }

    @Test
    fun `currentSeason matches getActiveTheme season`() {
        assertEquals(manager.getActiveTheme().season, manager.currentSeason())
    }

    // ── Seasonal theme correctness based on current date ────────────
    // These tests validate the current month (April = Spring)

    @Test
    fun `April returns SPRING theme`() {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        if (month in 3..5) {
            assertEquals(Season.SPRING, manager.currentSeason())
        }
    }

    // ── Theme structure ─────────────────────────────────────────────

    @Test
    fun `active theme has confetti emojis`() {
        val theme = manager.getActiveTheme()
        assertTrue(theme.confettiEmojis.isNotEmpty())
    }

    // ── Season enum ─────────────────────────────────────────────────

    @Test
    fun `Season enum has 7 entries`() {
        assertEquals(7, Season.entries.size)
    }

    @Test
    fun `Season includes NONE`() {
        assertTrue(Season.entries.contains(Season.NONE))
    }

    @Test
    fun `Season includes HALLOWEEN and CHRISTMAS specials`() {
        assertTrue(Season.entries.contains(Season.HALLOWEEN))
        assertTrue(Season.entries.contains(Season.CHRISTMAS))
    }
}
