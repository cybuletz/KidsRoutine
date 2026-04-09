package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class RooDialogueTest {

    // ── streakEncouragement ─────────────────────────────────────────

    @Test
    fun `SPROUT streak 30+ includes happy dance`() {
        val msg = RooDialogue.streakEncouragement(30, AgeGroup.SPROUT)
        assertTrue(msg.contains("30"))
        assertTrue(msg.contains("happy dance"))
    }

    @Test
    fun `SPROUT streak 7 includes AMAZING`() {
        val msg = RooDialogue.streakEncouragement(7, AgeGroup.SPROUT)
        assertTrue(msg.contains("AMAZING"))
    }

    @Test
    fun `SPROUT streak 3 includes proud`() {
        val msg = RooDialogue.streakEncouragement(3, AgeGroup.SPROUT)
        assertTrue(msg.contains("proud"))
    }

    @Test
    fun `EXPLORER streak 30+ includes on fire`() {
        val msg = RooDialogue.streakEncouragement(30, AgeGroup.EXPLORER)
        assertTrue(msg.contains("fire"))
    }

    @Test
    fun `TRAILBLAZER streak 7 includes not bad`() {
        val msg = RooDialogue.streakEncouragement(7, AgeGroup.TRAILBLAZER)
        assertTrue(msg.contains("Not bad"))
    }

    @Test
    fun `LEGEND streak 30+ includes consistency`() {
        val msg = RooDialogue.streakEncouragement(30, AgeGroup.LEGEND)
        assertTrue(msg.contains("consecutive"))
    }

    @Test
    fun `streak count appears in all age groups`() {
        for (ageGroup in AgeGroup.entries) {
            val msg = RooDialogue.streakEncouragement(15, ageGroup)
            assertTrue("Streak count should appear for $ageGroup", msg.contains("15"))
        }
    }

    // ── streakAtRisk ────────────────────────────────────────────────

    @Test
    fun `streakAtRisk includes streak count for all groups`() {
        for (ageGroup in AgeGroup.entries) {
            val msg = RooDialogue.streakAtRisk(10, ageGroup)
            assertTrue("Streak count should appear for $ageGroup", msg.contains("10"))
        }
    }

    @Test
    fun `SPROUT streak at risk mentions waiting`() {
        val msg = RooDialogue.streakAtRisk(5, AgeGroup.SPROUT)
        assertTrue(msg.contains("waiting"))
    }

    @Test
    fun `TRAILBLAZER streak at risk has casual tone`() {
        val msg = RooDialogue.streakAtRisk(5, AgeGroup.TRAILBLAZER)
        assertTrue(msg.contains("Your call") || msg.contains("die"))
    }

    // ── comebackMessage ─────────────────────────────────────────────

    @Test
    fun `SPROUT comeback is welcoming`() {
        val msg = RooDialogue.comebackMessage(5, AgeGroup.SPROUT)
        assertTrue(msg.contains("back") || msg.contains("missed"))
    }

    @Test
    fun `TRAILBLAZER comeback is sarcastic`() {
        val msg = RooDialogue.comebackMessage(5, AgeGroup.TRAILBLAZER)
        assertTrue(msg.contains("remembered") || msg.contains("exists"))
    }

    @Test
    fun `comebackMessage non-empty for all groups`() {
        for (ageGroup in AgeGroup.entries) {
            val msg = RooDialogue.comebackMessage(3, ageGroup)
            assertTrue(msg.isNotEmpty())
        }
    }

    // ── petHungry ───────────────────────────────────────────────────

    @Test
    fun `petHungry includes pet name for all groups`() {
        for (ageGroup in AgeGroup.entries) {
            val msg = RooDialogue.petHungry("Rex", ageGroup)
            assertTrue("Pet name should appear for $ageGroup", msg.contains("Rex"))
        }
    }

    // ── almostPromoted ──────────────────────────────────────────────

    @Test
    fun `almostPromoted includes league and XP for all groups`() {
        for (ageGroup in AgeGroup.entries) {
            val msg = RooDialogue.almostPromoted("Gold", 42, ageGroup)
            assertTrue("League should appear for $ageGroup", msg.contains("Gold"))
            assertTrue("XP should appear for $ageGroup", msg.contains("42"))
        }
    }

    // ── dangerZone ──────────────────────────────────────────────────

    @Test
    fun `dangerZone includes league name for all groups`() {
        for (ageGroup in AgeGroup.entries) {
            val msg = RooDialogue.dangerZone("Silver", ageGroup)
            assertTrue("League should appear for $ageGroup", msg.contains("Silver"))
        }
    }

    // ── bossAppeared ────────────────────────────────────────────────

    @Test
    fun `bossAppeared includes boss name for all groups`() {
        for (ageGroup in AgeGroup.entries) {
            val msg = RooDialogue.bossAppeared("Dragon", ageGroup)
            assertTrue("Boss name should appear for $ageGroup", msg.contains("Dragon"))
        }
    }

    // ── randomEncouragement ─────────────────────────────────────────

    @Test
    fun `randomEncouragement returns non-empty for all groups`() {
        for (ageGroup in AgeGroup.entries) {
            val msg = RooDialogue.randomEncouragement(ageGroup)
            assertTrue(msg.isNotEmpty())
        }
    }

    // ── shopGreeting ────────────────────────────────────────────────

    @Test
    fun `shopGreeting non-empty for all groups`() {
        for (ageGroup in AgeGroup.entries) {
            val msg = RooDialogue.shopGreeting(ageGroup)
            assertTrue(msg.isNotEmpty())
        }
    }

    // ── gameIntro ───────────────────────────────────────────────────

    @Test
    fun `gameIntro includes game name for all groups`() {
        for (ageGroup in AgeGroup.entries) {
            val msg = RooDialogue.gameIntro("Memory Match", ageGroup)
            assertTrue("Game name should appear for $ageGroup", msg.contains("Memory Match"))
        }
    }

    // ── RooState ────────────────────────────────────────────────────

    @Test
    fun `RooState displayName is Roo`() {
        val state = RooState()
        assertEquals("Roo", state.displayName)
    }

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

    // ── RooExpression ───────────────────────────────────────────────

    @Test
    fun `RooExpression has 10 entries`() {
        assertEquals(10, RooExpression.entries.size)
    }

    @Test
    fun `all expressions have non-empty emojis`() {
        RooExpression.entries.forEach {
            assertTrue(it.emoji.isNotEmpty())
        }
    }
}
