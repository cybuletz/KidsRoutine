package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class SeasonalThemeModelTest {

    // ── Season enum ─────────────────────────────────────────────────

    @Test
    fun `Season has 7 entries`() {
        assertEquals(7, Season.entries.size)
    }

    @Test
    fun `Season includes all expected values`() {
        val names = Season.entries.map { it.name }
        assertTrue(names.contains("SPRING"))
        assertTrue(names.contains("SUMMER"))
        assertTrue(names.contains("AUTUMN"))
        assertTrue(names.contains("WINTER"))
        assertTrue(names.contains("HALLOWEEN"))
        assertTrue(names.contains("CHRISTMAS"))
        assertTrue(names.contains("NONE"))
    }

    // ── SeasonalTheme defaults ──────────────────────────────────────

    @Test
    fun `default season is NONE`() {
        val theme = SeasonalTheme()
        assertEquals(Season.NONE, theme.season)
    }

    @Test
    fun `default displayName is Classic`() {
        val theme = SeasonalTheme()
        assertEquals("Classic", theme.displayName)
    }

    @Test
    fun `default isActive is false`() {
        val theme = SeasonalTheme()
        assertFalse(theme.isActive)
    }

    @Test
    fun `default confettiEmojis is not empty`() {
        val theme = SeasonalTheme()
        assertTrue(theme.confettiEmojis.isNotEmpty())
    }

    // ── SeasonalThemes object ───────────────────────────────────────

    @Test
    fun `NONE theme has Classic displayName`() {
        assertEquals("Classic", SeasonalThemes.NONE.displayName)
    }

    @Test
    fun `SPRING theme has Spring displayName`() {
        assertEquals("Spring", SeasonalThemes.SPRING.displayName)
    }

    @Test
    fun `SUMMER theme has Summer displayName`() {
        assertEquals("Summer", SeasonalThemes.SUMMER.displayName)
    }

    @Test
    fun `AUTUMN theme has Autumn displayName`() {
        assertEquals("Autumn", SeasonalThemes.AUTUMN.displayName)
    }

    @Test
    fun `WINTER theme has Winter displayName`() {
        assertEquals("Winter", SeasonalThemes.WINTER.displayName)
    }

    @Test
    fun `HALLOWEEN theme has Halloween displayName`() {
        assertEquals("Halloween", SeasonalThemes.HALLOWEEN.displayName)
    }

    @Test
    fun `CHRISTMAS theme has Christmas displayName`() {
        assertEquals("Christmas", SeasonalThemes.CHRISTMAS.displayName)
    }

    @Test
    fun `all themes have non-empty emoji`() {
        val themes = listOf(
            SeasonalThemes.NONE, SeasonalThemes.SPRING, SeasonalThemes.SUMMER,
            SeasonalThemes.AUTUMN, SeasonalThemes.WINTER,
            SeasonalThemes.HALLOWEEN, SeasonalThemes.CHRISTMAS
        )
        themes.forEach { assertTrue("${it.displayName} emoji", it.emoji.isNotEmpty()) }
    }

    @Test
    fun `all themes have non-empty confetti emojis`() {
        val themes = listOf(
            SeasonalThemes.SPRING, SeasonalThemes.SUMMER,
            SeasonalThemes.AUTUMN, SeasonalThemes.WINTER,
            SeasonalThemes.HALLOWEEN, SeasonalThemes.CHRISTMAS
        )
        themes.forEach {
            assertTrue("${it.displayName} confetti", it.confettiEmojis.isNotEmpty())
        }
    }

    @Test
    fun `all seasonal themes have non-empty bannerText`() {
        val themes = listOf(
            SeasonalThemes.SPRING, SeasonalThemes.SUMMER,
            SeasonalThemes.AUTUMN, SeasonalThemes.WINTER,
            SeasonalThemes.HALLOWEEN, SeasonalThemes.CHRISTMAS
        )
        themes.forEach {
            assertTrue("${it.displayName} banner", it.bannerText.isNotEmpty())
        }
    }

    @Test
    fun `NONE theme has empty bannerText`() {
        assertEquals("", SeasonalThemes.NONE.bannerText)
    }

    @Test
    fun `each season maps to correct theme`() {
        assertEquals(Season.SPRING, SeasonalThemes.SPRING.season)
        assertEquals(Season.SUMMER, SeasonalThemes.SUMMER.season)
        assertEquals(Season.AUTUMN, SeasonalThemes.AUTUMN.season)
        assertEquals(Season.WINTER, SeasonalThemes.WINTER.season)
        assertEquals(Season.HALLOWEEN, SeasonalThemes.HALLOWEEN.season)
        assertEquals(Season.CHRISTMAS, SeasonalThemes.CHRISTMAS.season)
    }

    // ── FamilyModel defaults ────────────────────────────────────────

    @Test
    fun `FamilyModel default memberIds is empty`() {
        val family = FamilyModel()
        assertTrue(family.memberIds.isEmpty())
    }

    @Test
    fun `FamilyModel default familyXp is 0`() {
        val family = FamilyModel()
        assertEquals(0, family.familyXp)
    }

    @Test
    fun `FamilyModel default familyStreak is 0`() {
        val family = FamilyModel()
        assertEquals(0, family.familyStreak)
    }

    @Test
    fun `FamilyModel stores all fields`() {
        val family = FamilyModel(
            familyId = "f1",
            familyName = "Smith Family",
            memberIds = listOf("u1", "u2", "u3"),
            familyXp = 5000,
            familyStreak = 14,
            inviteCode = "ABC123"
        )
        assertEquals("f1", family.familyId)
        assertEquals("Smith Family", family.familyName)
        assertEquals(3, family.memberIds.size)
        assertEquals(5000, family.familyXp)
        assertEquals("ABC123", family.inviteCode)
    }

    // ── MomentModel defaults ────────────────────────────────────────

    @Test
    fun `MomentModel default emoji is camera`() {
        val moment = MomentModel()
        assertEquals("📸", moment.emoji)
    }

    @Test
    fun `MomentModel default reactions is empty`() {
        val moment = MomentModel()
        assertTrue(moment.reactions.isEmpty())
    }
}
