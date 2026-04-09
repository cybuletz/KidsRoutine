package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class RooMascotTest {

    // ── RooExpression enum ──────────────────────────────────────────

    @Test
    fun `RooExpression has 10 entries`() {
        assertEquals(10, RooExpression.entries.size)
    }

    @Test
    fun `includes all expected expressions`() {
        val names = RooExpression.entries.map { it.name }
        assertTrue(names.contains("HAPPY"))
        assertTrue(names.contains("EXCITED"))
        assertTrue(names.contains("PROUD"))
        assertTrue(names.contains("WORRIED"))
        assertTrue(names.contains("SAD"))
        assertTrue(names.contains("SLEEPY"))
        assertTrue(names.contains("SARCASTIC"))
        assertTrue(names.contains("CELEBRATING"))
        assertTrue(names.contains("ENCOURAGING"))
        assertTrue(names.contains("THINKING"))
    }

    @Test
    fun `all expressions have non-empty emoji`() {
        RooExpression.entries.forEach {
            assertTrue("${it.name} should have emoji", it.emoji.isNotEmpty())
        }
    }

    @Test
    fun `all expressions have non-empty description`() {
        RooExpression.entries.forEach {
            assertTrue("${it.name} should have description", it.description.isNotEmpty())
        }
    }

    @Test
    fun `all emojis contain kangaroo`() {
        RooExpression.entries.forEach {
            assertTrue("${it.name} emoji should contain kangaroo", it.emoji.contains("🦘"))
        }
    }

    @Test
    fun `HAPPY emoji is kangaroo smile`() {
        assertEquals("🦘😊", RooExpression.HAPPY.emoji)
    }

    @Test
    fun `SARCASTIC emoji is kangaroo smirk`() {
        assertEquals("🦘😏", RooExpression.SARCASTIC.emoji)
    }

    @Test
    fun `CELEBRATING description mentions confetti`() {
        assertTrue(RooExpression.CELEBRATING.description.lowercase().contains("confetti"))
    }

    // ── RooState defaults ───────────────────────────────────────────

    @Test
    fun `default expression is HAPPY`() {
        val state = RooState()
        assertEquals(RooExpression.HAPPY, state.currentExpression)
    }

    @Test
    fun `default seasonal outfit is NONE`() {
        val state = RooState()
        assertEquals(Season.NONE, state.seasonalOutfit)
    }

    @Test
    fun `default age group personality is EXPLORER`() {
        val state = RooState()
        assertEquals(AgeGroup.EXPLORER, state.ageGroupPersonality)
    }

    @Test
    fun `displayName is always Roo`() {
        val state = RooState()
        assertEquals("Roo", state.displayName)
    }

    // ── personalityTone ─────────────────────────────────────────────

    @Test
    fun `SPROUT personalityTone is playful`() {
        val state = RooState(ageGroupPersonality = AgeGroup.SPROUT)
        assertEquals("playful", state.personalityTone)
    }

    @Test
    fun `EXPLORER personalityTone is enthusiastic`() {
        val state = RooState(ageGroupPersonality = AgeGroup.EXPLORER)
        assertEquals("enthusiastic", state.personalityTone)
    }

    @Test
    fun `TRAILBLAZER personalityTone is chill`() {
        val state = RooState(ageGroupPersonality = AgeGroup.TRAILBLAZER)
        assertEquals("chill", state.personalityTone)
    }

    @Test
    fun `LEGEND personalityTone is respected`() {
        val state = RooState(ageGroupPersonality = AgeGroup.LEGEND)
        assertEquals("respected", state.personalityTone)
    }

    // ── RooState construction ───────────────────────────────────────

    @Test
    fun `stores custom expression`() {
        val state = RooState(currentExpression = RooExpression.SAD)
        assertEquals(RooExpression.SAD, state.currentExpression)
    }

    @Test
    fun `stores seasonal outfit`() {
        val state = RooState(seasonalOutfit = Season.HALLOWEEN)
        assertEquals(Season.HALLOWEEN, state.seasonalOutfit)
    }

    @Test
    fun `all age groups produce valid personalityTone`() {
        val validTones = setOf("playful", "enthusiastic", "chill", "respected")
        for (ageGroup in AgeGroup.entries) {
            val tone = RooState(ageGroupPersonality = ageGroup).personalityTone
            assertTrue("$ageGroup should produce valid tone, got: $tone", validTones.contains(tone))
        }
    }

    // ── data class behavior ─────────────────────────────────────────

    @Test
    fun `copy preserves other fields`() {
        val original = RooState(
            currentExpression = RooExpression.PROUD,
            seasonalOutfit = Season.WINTER,
            ageGroupPersonality = AgeGroup.LEGEND
        )
        val copy = original.copy(currentExpression = RooExpression.EXCITED)
        assertEquals(RooExpression.EXCITED, copy.currentExpression)
        assertEquals(Season.WINTER, copy.seasonalOutfit)
        assertEquals(AgeGroup.LEGEND, copy.ageGroupPersonality)
    }

    @Test
    fun `same data equals`() {
        val a = RooState(currentExpression = RooExpression.HAPPY)
        val b = RooState(currentExpression = RooExpression.HAPPY)
        assertEquals(a, b)
    }
}
