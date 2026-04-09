package com.kidsroutine.navigation

import org.junit.Assert.*
import org.junit.Test

class RoutesTest {

    // ── Route helper functions ──────────────────────────────────────

    @Test
    fun `execution builds correct route`() {
        assertEquals("execution/task123", Routes.execution("task123"))
    }

    @Test
    fun `challengeDetail builds correct route`() {
        assertEquals("challenge_detail/ch1", Routes.challengeDetail("ch1"))
    }

    @Test
    fun `parentChallengeDetail builds correct route`() {
        assertEquals("parent_challenge_detail/ch2", Routes.parentChallengeDetail("ch2"))
    }

    @Test
    fun `selectChildren builds correct route`() {
        assertEquals("select_children/t1", Routes.selectChildren("t1"))
    }

    // ── Route constants are not empty ───────────────────────────────

    @Test
    fun `DAILY route is not empty`() {
        assertTrue(Routes.DAILY.isNotEmpty())
    }

    @Test
    fun `WORLD route is not empty`() {
        assertTrue(Routes.WORLD.isNotEmpty())
    }

    @Test
    fun `REWARDS route is not empty`() {
        assertTrue(Routes.REWARDS.isNotEmpty())
    }

    @Test
    fun `PET route is not empty`() {
        assertTrue(Routes.PET.isNotEmpty())
    }

    @Test
    fun `BOSS_BATTLE route is not empty`() {
        assertTrue(Routes.BOSS_BATTLE.isNotEmpty())
    }

    @Test
    fun `SPIN_WHEEL route is not empty`() {
        assertTrue(Routes.SPIN_WHEEL.isNotEmpty())
    }

    // ── Route templates contain placeholders ────────────────────────

    @Test
    fun `EXECUTION contains taskId placeholder`() {
        assertTrue(Routes.EXECUTION.contains("{taskId}"))
    }

    @Test
    fun `CHALLENGE_DETAIL contains challengeId placeholder`() {
        assertTrue(Routes.CHALLENGE_DETAIL.contains("{challengeId}"))
    }

    @Test
    fun `PARENT_CHALLENGE_DETAIL contains challengeId placeholder`() {
        assertTrue(Routes.PARENT_CHALLENGE_DETAIL.contains("{challengeId}"))
    }

    // ── Parent routes ───────────────────────────────────────────────

    @Test
    fun `PARENT_DASHBOARD route is not empty`() {
        assertTrue(Routes.PARENT_DASHBOARD.isNotEmpty())
    }

    @Test
    fun `GENERATION route is not empty`() {
        assertTrue(Routes.GENERATION.isNotEmpty())
    }

    @Test
    fun `DAILY_PLAN route is not empty`() {
        assertTrue(Routes.DAILY_PLAN.isNotEmpty())
    }

    @Test
    fun `WEEKLY_PLAN route is not empty`() {
        assertTrue(Routes.WEEKLY_PLAN.isNotEmpty())
    }

    @Test
    fun `UPGRADE route is not empty`() {
        assertTrue(Routes.UPGRADE.isNotEmpty())
    }

    // ── Community routes ────────────────────────────────────────────

    @Test
    fun `MARKETPLACE route is not empty`() {
        assertTrue(Routes.MARKETPLACE.isNotEmpty())
    }

    @Test
    fun `PUBLISH route is not empty`() {
        assertTrue(Routes.PUBLISH.isNotEmpty())
    }

    // ── Parent nav bar tab routes ───────────────────────────────────

    @Test
    fun `PARENT_HOME route is not empty`() {
        assertTrue(Routes.PARENT_HOME.isNotEmpty())
    }

    @Test
    fun `PARENT_TASKS_TAB route is not empty`() {
        assertTrue(Routes.PARENT_TASKS_TAB.isNotEmpty())
    }

    @Test
    fun `PARENT_FAMILY_TAB route is not empty`() {
        assertTrue(Routes.PARENT_FAMILY_TAB.isNotEmpty())
    }

    @Test
    fun `PARENT_DISCOVER_TAB route is not empty`() {
        assertTrue(Routes.PARENT_DISCOVER_TAB.isNotEmpty())
    }

    @Test
    fun `PARENT_SETTINGS_TAB route is not empty`() {
        assertTrue(Routes.PARENT_SETTINGS_TAB.isNotEmpty())
    }
}
