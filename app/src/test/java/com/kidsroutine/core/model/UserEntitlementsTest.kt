package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class UserEntitlementsTest {

    // ── hasFunZoneFeature ───────────────────────────────────────────────

    @Test
    fun `FREE tier has pet access`() {
        val ent = PlanType.FREE.defaultEntitlements("user1")
        assertTrue(ent.hasFunZoneFeature("pet"))
    }

    @Test
    fun `FREE tier has daily_spin access`() {
        val ent = PlanType.FREE.defaultEntitlements("user1")
        assertTrue(ent.hasFunZoneFeature("daily_spin"))
    }

    @Test
    fun `FREE tier does NOT have rituals access`() {
        val ent = PlanType.FREE.defaultEntitlements("user1")
        assertFalse(ent.hasFunZoneFeature("rituals"))
    }

    @Test
    fun `FREE tier does NOT have boss_battle`() {
        val ent = PlanType.FREE.defaultEntitlements("user1")
        assertFalse(ent.hasFunZoneFeature("boss_battle"))
    }

    @Test
    fun `FREE tier does NOT have story_arcs`() {
        val ent = PlanType.FREE.defaultEntitlements("user1")
        assertFalse(ent.hasFunZoneFeature("story_arcs"))
    }

    @Test
    fun `PRO tier has rituals access`() {
        val ent = PlanType.PRO.defaultEntitlements("user1")
        assertTrue(ent.hasFunZoneFeature("rituals"))
    }

    @Test
    fun `PRO tier has boss_battle access`() {
        val ent = PlanType.PRO.defaultEntitlements("user1")
        assertTrue(ent.hasFunZoneFeature("boss_battle"))
    }

    @Test
    fun `PRO tier has all PRO features`() {
        val ent = PlanType.PRO.defaultEntitlements("user1")
        for (key in UserEntitlements.PRO_FUN_ZONE_FEATURES) {
            assertTrue("PRO should have $key", ent.hasFunZoneFeature(key))
        }
    }

    @Test
    fun `PRO tier does NOT have wallet`() {
        val ent = PlanType.PRO.defaultEntitlements("user1")
        assertFalse(ent.hasFunZoneFeature("wallet"))
    }

    @Test
    fun `PREMIUM tier has all features`() {
        val ent = PlanType.PREMIUM.defaultEntitlements("user1")
        for (key in UserEntitlements.ALL_FUN_ZONE_FEATURE_KEYS) {
            assertTrue("PREMIUM should have $key", ent.hasFunZoneFeature(key))
        }
    }

    // ── requiredPlanForFeature ──────────────────────────────────────────

    @Test
    fun `FREE user sees PRO required for boss_battle`() {
        val ent = PlanType.FREE.defaultEntitlements("user1")
        assertEquals(PlanType.PRO, ent.requiredPlanForFeature("boss_battle"))
    }

    @Test
    fun `FREE user sees PRO required for rituals`() {
        val ent = PlanType.FREE.defaultEntitlements("user1")
        assertEquals(PlanType.PRO, ent.requiredPlanForFeature("rituals"))
    }

    @Test
    fun `FREE user sees null (unlocked) for pet`() {
        val ent = PlanType.FREE.defaultEntitlements("user1")
        assertNull(ent.requiredPlanForFeature("pet"))
    }

    @Test
    fun `PRO user sees null (unlocked) for rituals`() {
        val ent = PlanType.PRO.defaultEntitlements("user1")
        assertNull(ent.requiredPlanForFeature("rituals"))
    }

    // ── Default entitlements ────────────────────────────────────────────

    @Test
    fun `FREE default has 3 tasks per day`() {
        val ent = PlanType.FREE.defaultEntitlements("user1")
        assertEquals(3, ent.aiTasksPerDay)
    }

    @Test
    fun `PRO default has parent controls enabled`() {
        val ent = PlanType.PRO.defaultEntitlements("user1")
        assertTrue(ent.parentControlsEnabled)
    }

    @Test
    fun `FREE default has parent controls disabled`() {
        val ent = PlanType.FREE.defaultEntitlements("user1")
        assertFalse(ent.parentControlsEnabled)
    }

    @Test
    fun `PREMIUM default has 999 tasks per day`() {
        val ent = PlanType.PREMIUM.defaultEntitlements("user1")
        assertEquals(999, ent.aiTasksPerDay)
    }

    // ── Trial access ────────────────────────────────────────────────────

    @Test
    fun `FREE user can generate challenges via trial`() {
        val ent = PlanType.FREE.defaultEntitlements("user1")
        assertTrue(ent.canGenerateChallenges())
    }

    @Test
    fun `FREE user has trial access for challenges`() {
        val ent = PlanType.FREE.defaultEntitlements("user1")
        assertTrue(ent.isTrialAccess("challenges"))
    }

    @Test
    fun `PRO user does not have trial access`() {
        val ent = PlanType.PRO.defaultEntitlements("user1")
        assertFalse(ent.isTrialAccess("challenges"))
    }
}
