package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class FeatureFlagsTest {

    @Test
    fun `default feature flags have core features enabled`() {
        val flags = FeatureFlags()
        assertTrue(flags.dailyTasksEnabled)
        assertTrue(flags.challengesEnabled)
        assertTrue(flags.communityEnabled)
        assertTrue(flags.aiGenerationEnabled)
    }

    @Test
    fun `default feature flags have P0 features enabled`() {
        val flags = FeatureFlags()
        assertTrue(flags.ageAdaptiveGamesEnabled)
        assertTrue(flags.companionPetEnabled)
        assertTrue(flags.mascotRooEnabled)
    }

    @Test
    fun `default feature flags have P1 features enabled`() {
        val flags = FeatureFlags()
        assertTrue(flags.leagueSystemEnabled)
        assertTrue(flags.smartNotificationsEnabled)
        assertTrue(flags.darkModeEnabled)
        assertTrue(flags.hapticFeedbackEnabled)
    }

    @Test
    fun `default feature flags have P2 features enabled`() {
        val flags = FeatureFlags()
        assertTrue(flags.bossBattlesEnabled)
        assertTrue(flags.dailySpinWheelEnabled)
        assertTrue(flags.timedEventsEnabled)
        assertTrue(flags.comebackSystemEnabled)
    }

    @Test
    fun `default feature flags have P3 features enabled`() {
        val flags = FeatureFlags()
        assertTrue(flags.skillTreesEnabled)
        assertTrue(flags.familyRitualsEnabled)
        assertTrue(flags.financialLiteracyEnabled)
    }

    @Test
    fun `default feature flags have friend system disabled`() {
        val flags = FeatureFlags()
        assertFalse(flags.friendSystemEnabled)
    }

    @Test
    fun `default feature flags have P4 features disabled`() {
        val flags = FeatureFlags()
        assertFalse(flags.i18nEnabled)
    }

    @Test
    fun `feature flags can be toggled via copy`() {
        val flags = FeatureFlags(companionPetEnabled = false)
        assertFalse(flags.companionPetEnabled)
        // Other flags should still have defaults
        assertTrue(flags.dailyTasksEnabled)
    }

    @Test
    fun `weekly planner is disabled by default`() {
        val flags = FeatureFlags()
        assertFalse(flags.weeklyPlannerEnabled)
    }
}
