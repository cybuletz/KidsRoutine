package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class FamilyRitualTest {

    // ── goalProgressPercent ─────────────────────────────────────────

    @Test
    fun `goalProgressPercent at zero`() {
        val ritual = FamilyRitual(goalTarget = 10, goalProgress = 0)
        assertEquals(0f, ritual.goalProgressPercent, 0.01f)
    }

    @Test
    fun `goalProgressPercent at 50 percent`() {
        val ritual = FamilyRitual(goalTarget = 10, goalProgress = 5)
        assertEquals(0.5f, ritual.goalProgressPercent, 0.01f)
    }

    @Test
    fun `goalProgressPercent capped at 1_0`() {
        val ritual = FamilyRitual(goalTarget = 5, goalProgress = 10)
        assertEquals(1.0f, ritual.goalProgressPercent, 0.01f)
    }

    @Test
    fun `goalProgressPercent with zero target`() {
        val ritual = FamilyRitual(goalTarget = 0, goalProgress = 5)
        assertEquals(0f, ritual.goalProgressPercent, 0.01f)
    }

    // ── RitualType enum ─────────────────────────────────────────────

    @Test
    fun `RitualType has 5 entries`() {
        assertEquals(5, RitualType.entries.size)
    }

    @Test
    fun `all ritual types have non-empty display names`() {
        RitualType.entries.forEach {
            assertTrue(it.displayName.isNotEmpty())
        }
    }

    @Test
    fun `all ritual types have non-empty emojis`() {
        RitualType.entries.forEach {
            assertTrue(it.emoji.isNotEmpty())
        }
    }

    // ── RitualFrequency enum ────────────────────────────────────────

    @Test
    fun `RitualFrequency has 4 entries`() {
        assertEquals(4, RitualFrequency.entries.size)
    }

    // ── Default values ──────────────────────────────────────────────

    @Test
    fun `default type is GRATITUDE_CIRCLE`() {
        val ritual = FamilyRitual()
        assertEquals(RitualType.GRATITUDE_CIRCLE, ritual.type)
    }

    @Test
    fun `default frequency is DAILY`() {
        val ritual = FamilyRitual()
        assertEquals(RitualFrequency.DAILY, ritual.frequency)
    }

    @Test
    fun `default completionXp is 25`() {
        val ritual = FamilyRitual()
        assertEquals(25, ritual.completionXp)
    }

    @Test
    fun `default meetingDurationMin is 15`() {
        val ritual = FamilyRitual()
        assertEquals(15, ritual.meetingDurationMin)
    }

    @Test
    fun `default isActive is true`() {
        val ritual = FamilyRitual()
        assertTrue(ritual.isActive)
    }
}
