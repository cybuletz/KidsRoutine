package com.kidsroutine.core.ai

import org.junit.Assert.*
import org.junit.Test

class AIModelsTest {

    // ── GenerationType enum ─────────────────────────────────────────

    @Test
    fun `GenerationType has 3 entries`() {
        assertEquals(3, GenerationType.entries.size)
    }

    @Test
    fun `GenerationType includes TASK CHALLENGE CUSTOM`() {
        val names = GenerationType.entries.map { it.name }
        assertTrue(names.contains("TASK"))
        assertTrue(names.contains("CHALLENGE"))
        assertTrue(names.contains("CUSTOM"))
    }

    @Test
    fun `GenerationType valueOf round-trips`() {
        for (g in GenerationType.entries) {
            assertEquals(g, GenerationType.valueOf(g.name))
        }
    }

    // ── Severity enum ───────────────────────────────────────────────

    @Test
    fun `Severity has 3 entries`() {
        assertEquals(3, Severity.entries.size)
    }

    @Test
    fun `Severity includes NONE WARNING CRITICAL`() {
        val names = Severity.entries.map { it.name }
        assertTrue(names.contains("NONE"))
        assertTrue(names.contains("WARNING"))
        assertTrue(names.contains("CRITICAL"))
    }

    @Test
    fun `Severity ordinal order`() {
        assertEquals(0, Severity.NONE.ordinal)
        assertEquals(1, Severity.WARNING.ordinal)
        assertEquals(2, Severity.CRITICAL.ordinal)
    }

    // ── SubscriptionTier enum ───────────────────────────────────────

    @Test
    fun `SubscriptionTier has 3 entries`() {
        assertEquals(3, SubscriptionTier.entries.size)
    }

    @Test
    fun `SubscriptionTier includes FREE PRO PREMIUM`() {
        val names = SubscriptionTier.entries.map { it.name }
        assertTrue(names.contains("FREE"))
        assertTrue(names.contains("PRO"))
        assertTrue(names.contains("PREMIUM"))
    }

    @Test
    fun `SubscriptionTier ordinal order`() {
        assertEquals(0, SubscriptionTier.FREE.ordinal)
        assertEquals(1, SubscriptionTier.PRO.ordinal)
        assertEquals(2, SubscriptionTier.PREMIUM.ordinal)
    }

    // ── ValidationResult data class ─────────────────────────────────

    @Test
    fun `ValidationResult default is safe with no reason`() {
        val r = ValidationResult(isSafe = true)
        assertTrue(r.isSafe)
        assertNull(r.reason)
        assertEquals(Severity.NONE, r.severity)
    }

    @Test
    fun `ValidationResult unsafe with reason`() {
        val r = ValidationResult(isSafe = false, reason = "profanity", severity = Severity.CRITICAL)
        assertFalse(r.isSafe)
        assertEquals("profanity", r.reason)
        assertEquals(Severity.CRITICAL, r.severity)
    }

    @Test
    fun `ValidationResult equality`() {
        val a = ValidationResult(isSafe = true, reason = null, severity = Severity.NONE)
        val b = ValidationResult(isSafe = true, reason = null, severity = Severity.NONE)
        assertEquals(a, b)
    }

    @Test
    fun `ValidationResult inequality`() {
        val a = ValidationResult(isSafe = true)
        val b = ValidationResult(isSafe = false, reason = "bad")
        assertNotEquals(a, b)
    }

    // ── GenerationContext data class ────────────────────────────────

    @Test
    fun `GenerationContext defaults`() {
        val ctx = GenerationContext(userId = "u1", familyId = "f1", childAge = 8)
        assertEquals("u1", ctx.userId)
        assertEquals("f1", ctx.familyId)
        assertEquals(8, ctx.childAge)
        assertTrue(ctx.preferences.isEmpty())
        assertTrue(ctx.recentCompletions.isEmpty())
        assertTrue(ctx.goals.isEmpty())
        assertEquals(SubscriptionTier.FREE, ctx.tier)
    }

    @Test
    fun `GenerationContext stores all fields`() {
        val ctx = GenerationContext(
            userId = "u1",
            familyId = "f1",
            childAge = 12,
            preferences = listOf("logic", "outdoor"),
            recentCompletions = listOf("brush teeth"),
            goals = listOf("sleep on time"),
            tier = SubscriptionTier.PRO
        )
        assertEquals(2, ctx.preferences.size)
        assertEquals(1, ctx.recentCompletions.size)
        assertEquals(1, ctx.goals.size)
        assertEquals(SubscriptionTier.PRO, ctx.tier)
    }

    @Test
    fun `GenerationContext equality`() {
        val a = GenerationContext("u1", "f1", 8)
        val b = GenerationContext("u1", "f1", 8)
        assertEquals(a, b)
    }

    @Test
    fun `GenerationContext copy`() {
        val orig = GenerationContext("u1", "f1", 8)
        val updated = orig.copy(childAge = 10, tier = SubscriptionTier.PREMIUM)
        assertEquals("u1", updated.userId)
        assertEquals(10, updated.childAge)
        assertEquals(SubscriptionTier.PREMIUM, updated.tier)
    }

    // ── QuotaInfo data class ────────────────────────────────────────

    @Test
    fun `QuotaInfo stores fields`() {
        val qi = QuotaInfo(limit = 20, remaining = 15, isExceeded = false)
        assertEquals(20, qi.limit)
        assertEquals(15, qi.remaining)
        assertFalse(qi.isExceeded)
    }

    @Test
    fun `QuotaInfo exceeded`() {
        val qi = QuotaInfo(limit = 1, remaining = 0, isExceeded = true)
        assertTrue(qi.isExceeded)
        assertEquals(0, qi.remaining)
    }

    @Test
    fun `QuotaInfo equality`() {
        val a = QuotaInfo(10, 5, false)
        val b = QuotaInfo(10, 5, false)
        assertEquals(a, b)
    }

    @Test
    fun `QuotaInfo inequality`() {
        val a = QuotaInfo(10, 5, false)
        val b = QuotaInfo(10, 4, false)
        assertNotEquals(a, b)
    }
}
