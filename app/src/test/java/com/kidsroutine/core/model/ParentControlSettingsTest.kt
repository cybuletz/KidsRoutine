package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class ParentControlSettingsTest {

    // ── isFunZoneFeatureEnabled ─────────────────────────────────────

    @Test
    fun `default settings enable all features`() {
        val settings = ParentControlSettings()
        for (feature in ParentControlSettings.ALL_FUN_ZONE_FEATURES) {
            assertTrue("${feature.key} should be enabled by default",
                settings.isFunZoneFeatureEnabled(feature.key))
        }
    }

    @Test
    fun `disabling pet returns false`() {
        val settings = ParentControlSettings(petEnabled = false)
        assertFalse(settings.isFunZoneFeatureEnabled("pet"))
    }

    @Test
    fun `disabling boss_battle returns false`() {
        val settings = ParentControlSettings(bossBattleEnabled = false)
        assertFalse(settings.isFunZoneFeatureEnabled("boss_battle"))
    }

    @Test
    fun `disabling daily_spin returns false`() {
        val settings = ParentControlSettings(dailySpinEnabled = false)
        assertFalse(settings.isFunZoneFeatureEnabled("daily_spin"))
    }

    @Test
    fun `disabling story_arcs returns false`() {
        val settings = ParentControlSettings(storyArcsEnabled = false)
        assertFalse(settings.isFunZoneFeatureEnabled("story_arcs"))
    }

    @Test
    fun `disabling events returns false`() {
        val settings = ParentControlSettings(eventsEnabled = false)
        assertFalse(settings.isFunZoneFeatureEnabled("events"))
    }

    @Test
    fun `disabling skill_tree returns false`() {
        val settings = ParentControlSettings(skillTreeEnabled = false)
        assertFalse(settings.isFunZoneFeatureEnabled("skill_tree"))
    }

    @Test
    fun `disabling wallet returns false`() {
        val settings = ParentControlSettings(walletEnabled = false)
        assertFalse(settings.isFunZoneFeatureEnabled("wallet"))
    }

    @Test
    fun `disabling rituals returns false`() {
        val settings = ParentControlSettings(ritualsEnabled = false)
        assertFalse(settings.isFunZoneFeatureEnabled("rituals"))
    }

    @Test
    fun `unknown feature key returns true`() {
        val settings = ParentControlSettings()
        assertTrue(settings.isFunZoneFeatureEnabled("unknown_feature"))
    }

    // ── xpMultiplierFor ─────────────────────────────────────────────

    @Test
    fun `default easy multiplier is 1_0`() {
        val settings = ParentControlSettings()
        assertEquals(1.0f, settings.xpMultiplierFor(DifficultyLevel.EASY), 0.01f)
    }

    @Test
    fun `default medium multiplier is 2_0`() {
        val settings = ParentControlSettings()
        assertEquals(2.0f, settings.xpMultiplierFor(DifficultyLevel.MEDIUM), 0.01f)
    }

    @Test
    fun `default hard multiplier is 3_0`() {
        val settings = ParentControlSettings()
        assertEquals(3.0f, settings.xpMultiplierFor(DifficultyLevel.HARD), 0.01f)
    }

    @Test
    fun `custom multipliers are respected`() {
        val settings = ParentControlSettings(
            xpMultiplierEasy = 0.5f,
            xpMultiplierMedium = 1.5f,
            xpMultiplierHard = 4.0f
        )
        assertEquals(0.5f, settings.xpMultiplierFor(DifficultyLevel.EASY), 0.01f)
        assertEquals(1.5f, settings.xpMultiplierFor(DifficultyLevel.MEDIUM), 0.01f)
        assertEquals(4.0f, settings.xpMultiplierFor(DifficultyLevel.HARD), 0.01f)
    }

    // ── ALL_FUN_ZONE_FEATURES ───────────────────────────────────────

    @Test
    fun `companion has 8 features`() {
        assertEquals(8, ParentControlSettings.ALL_FUN_ZONE_FEATURES.size)
    }

    @Test
    fun `all features have non-empty keys`() {
        ParentControlSettings.ALL_FUN_ZONE_FEATURES.forEach {
            assertTrue("key should not be empty", it.key.isNotEmpty())
        }
    }

    @Test
    fun `all features have non-empty titles`() {
        ParentControlSettings.ALL_FUN_ZONE_FEATURES.forEach {
            assertTrue("title should not be empty for ${it.key}", it.title.isNotEmpty())
        }
    }

    // ── Default field values ─────────────────────────────────────────

    @Test
    fun `default allowedDifficulties contains all levels`() {
        val settings = ParentControlSettings()
        assertEquals(DifficultyLevel.entries.size, settings.allowedDifficulties.size)
    }

    @Test
    fun `default dailyXpEarningCap is unlimited`() {
        val settings = ParentControlSettings()
        assertEquals(0, settings.dailyXpEarningCap)
    }

    @Test
    fun `default dailyXpSpendingCap is unlimited`() {
        val settings = ParentControlSettings()
        assertEquals(0, settings.dailyXpSpendingCap)
    }
}
