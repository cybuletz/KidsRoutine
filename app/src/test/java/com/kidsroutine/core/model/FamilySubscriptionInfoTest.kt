package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class FamilySubscriptionInfoTest {

    // ── Default values ─────────────────────────────────────────────

    @Test
    fun `default planType is FREE`() {
        val info = FamilySubscriptionInfo()
        assertEquals(PlanType.FREE, info.planType)
    }

    @Test
    fun `default billingParentId is empty`() {
        val info = FamilySubscriptionInfo()
        assertEquals("", info.billingParentId)
    }

    @Test
    fun `default updatedAt is zero`() {
        val info = FamilySubscriptionInfo()
        assertEquals(0L, info.updatedAt)
    }

    // ── Custom values ──────────────────────────────────────────────

    @Test
    fun `planType PRO is stored`() {
        val info = FamilySubscriptionInfo(planType = PlanType.PRO)
        assertEquals(PlanType.PRO, info.planType)
    }

    @Test
    fun `planType PREMIUM is stored`() {
        val info = FamilySubscriptionInfo(planType = PlanType.PREMIUM)
        assertEquals(PlanType.PREMIUM, info.planType)
    }

    @Test
    fun `custom billingParentId is stored`() {
        val info = FamilySubscriptionInfo(billingParentId = "parent_123")
        assertEquals("parent_123", info.billingParentId)
    }

    @Test
    fun `custom updatedAt is stored`() {
        val info = FamilySubscriptionInfo(updatedAt = 1_700_000_000L)
        assertEquals(1_700_000_000L, info.updatedAt)
    }

    // ── Equality and copy ──────────────────────────────────────────

    @Test
    fun `equality for identical instances`() {
        val a = FamilySubscriptionInfo(PlanType.PRO, "p1", 100L)
        val b = FamilySubscriptionInfo(PlanType.PRO, "p1", 100L)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `inequality when planType differs`() {
        val a = FamilySubscriptionInfo(planType = PlanType.FREE)
        val b = FamilySubscriptionInfo(planType = PlanType.PREMIUM)
        assertNotEquals(a, b)
    }

    @Test
    fun `copy changes only specified field`() {
        val original = FamilySubscriptionInfo(PlanType.FREE, "p1", 100L)
        val copied = original.copy(planType = PlanType.PREMIUM)
        assertEquals(PlanType.PREMIUM, copied.planType)
        assertEquals("p1", copied.billingParentId)
        assertEquals(100L, copied.updatedAt)
    }
}
