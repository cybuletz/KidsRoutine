package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class PetModelTest {

    // ── mood ────────────────────────────────────────────────────────

    @Test
    fun `mood ECSTATIC when happiness 90+`() {
        val pet = PetModel(happiness = 95)
        assertEquals(PetMood.ECSTATIC, pet.mood)
    }

    @Test
    fun `mood HAPPY when happiness 70-89`() {
        val pet = PetModel(happiness = 75)
        assertEquals(PetMood.HAPPY, pet.mood)
    }

    @Test
    fun `mood CONTENT when happiness 50-69`() {
        val pet = PetModel(happiness = 55)
        assertEquals(PetMood.CONTENT, pet.mood)
    }

    @Test
    fun `mood BORED when happiness 30-49`() {
        val pet = PetModel(happiness = 35)
        assertEquals(PetMood.BORED, pet.mood)
    }

    @Test
    fun `mood SAD when happiness 10-29`() {
        val pet = PetModel(happiness = 15)
        assertEquals(PetMood.SAD, pet.mood)
    }

    @Test
    fun `mood SLEEPING when happiness below 10`() {
        val pet = PetModel(happiness = 5)
        assertEquals(PetMood.SLEEPING, pet.mood)
    }

    @Test
    fun `mood boundary at exactly 90`() {
        val pet = PetModel(happiness = 90)
        assertEquals(PetMood.ECSTATIC, pet.mood)
    }

    @Test
    fun `mood boundary at exactly 70`() {
        val pet = PetModel(happiness = 70)
        assertEquals(PetMood.HAPPY, pet.mood)
    }

    @Test
    fun `mood boundary at exactly 50`() {
        val pet = PetModel(happiness = 50)
        assertEquals(PetMood.CONTENT, pet.mood)
    }

    // ── canEvolve ───────────────────────────────────────────────────

    @Test
    fun `EGG can evolve to BABY at level 1`() {
        val pet = PetModel(stage = PetEvolutionStage.EGG)
        assertTrue(pet.canEvolve(1))
    }

    @Test
    fun `EGG cannot evolve at level 0`() {
        val pet = PetModel(stage = PetEvolutionStage.EGG)
        assertFalse(pet.canEvolve(0))
    }

    @Test
    fun `BABY can evolve to JUVENILE at level 5 with sufficient care`() {
        val pet = PetModel(
            stage = PetEvolutionStage.BABY,
            totalFed = 10, totalPlayed = 10, totalTrained = 5,
            totalGroomed = 5, totalAdventures = 3, totalNaps = 5,
            totalTreats = 3, totalTreasureHunts = 2
        )
        assertTrue("careLevel should be >= 20", pet.careLevel >= 20)
        assertTrue(pet.canEvolve(5))
    }

    @Test
    fun `BABY cannot evolve at level 4`() {
        val pet = PetModel(stage = PetEvolutionStage.BABY)
        assertFalse(pet.canEvolve(4))
    }

    @Test
    fun `BABY cannot evolve without sufficient careLevel`() {
        val pet = PetModel(stage = PetEvolutionStage.BABY)  // careLevel = 0
        assertFalse(pet.canEvolve(5))
    }

    @Test
    fun `JUVENILE can evolve to ADULT at level 15 with stats`() {
        val pet = PetModel(
            stage = PetEvolutionStage.JUVENILE,
            happiness = 50,
            totalFed = 20, totalPlayed = 20, totalTrained = 10,
            totalGroomed = 10, totalAdventures = 8, totalNaps = 10,
            totalTreats = 8, totalTreasureHunts = 5
        )
        assertTrue("careLevel should be >= 45", pet.careLevel >= 45)
        assertTrue(pet.canEvolve(15))
    }

    @Test
    fun `ADULT can evolve to MAJESTIC at level 30 with stats`() {
        val pet = PetModel(
            stage = PetEvolutionStage.ADULT,
            style = 30,
            totalFed = 30, totalPlayed = 30, totalTrained = 20,
            totalGroomed = 20, totalAdventures = 15, totalNaps = 20,
            totalTreats = 15, totalTreasureHunts = 10
        )
        assertTrue("careLevel should be >= 70", pet.careLevel >= 70)
        assertTrue(pet.canEvolve(30))
    }

    @Test
    fun `MAJESTIC cannot evolve further`() {
        val pet = PetModel(stage = PetEvolutionStage.MAJESTIC)
        assertFalse(pet.canEvolve(99))
    }

    // ── displayEmoji ────────────────────────────────────────────────

    @Test
    fun `EGG displays egg emoji`() {
        val pet = PetModel(species = PetSpecies.DRAGON, stage = PetEvolutionStage.EGG)
        assertEquals("🥚", pet.displayEmoji)
    }

    @Test
    fun `BABY displays species emoji`() {
        val pet = PetModel(species = PetSpecies.DRAGON, stage = PetEvolutionStage.BABY)
        assertEquals("🐲", pet.displayEmoji)
    }

    @Test
    fun `MAJESTIC displays crown + species emoji`() {
        val pet = PetModel(species = PetSpecies.WOLF, stage = PetEvolutionStage.MAJESTIC)
        assertEquals("👑🐺", pet.displayEmoji)
    }

    @Test
    fun `PHOENIX EGG displays fire emoji`() {
        val pet = PetModel(species = PetSpecies.PHOENIX, stage = PetEvolutionStage.EGG)
        assertEquals("🔥", pet.displayEmoji)
    }

    // ── PetSpecies ──────────────────────────────────────────────────

    @Test
    fun `all species have 6 options`() {
        assertEquals(6, PetSpecies.entries.size)
    }

    @Test
    fun `all species have non-empty display names`() {
        PetSpecies.entries.forEach {
            assertTrue(it.displayName.isNotEmpty())
        }
    }

    // ── PetEvolutionStage ───────────────────────────────────────────

    @Test
    fun `evolution stages are ordered by level`() {
        val stages = PetEvolutionStage.entries
        for (i in 0 until stages.size - 1) {
            assertTrue("${stages[i].name} should require <= level than ${stages[i+1].name}",
                stages[i].requiredLevel <= stages[i+1].requiredLevel)
        }
    }

    @Test
    fun `there are 5 evolution stages`() {
        assertEquals(5, PetEvolutionStage.entries.size)
    }

    // ── PetMood ─────────────────────────────────────────────────────

    @Test
    fun `all moods have non-empty emojis`() {
        PetMood.entries.forEach {
            assertTrue(it.emoji.isNotEmpty())
        }
    }

    // ── totalActivities ────────────────────────────────────────────

    @Test
    fun `totalActivities sums all activity counters`() {
        val pet = PetModel(
            totalFed = 5, totalPlayed = 3, totalTrained = 2,
            totalGroomed = 4, totalAdventures = 1, totalNaps = 6,
            totalTreats = 2, totalTreasureHunts = 1
        )
        assertEquals(24, pet.totalActivities)
    }

    @Test
    fun `totalActivities is 0 for default pet`() {
        assertEquals(0, PetModel().totalActivities)
    }

    // ── careLevel ───────────────────────────────────────────────────

    @Test
    fun `careLevel is 0 for default pet`() {
        assertEquals(0, PetModel().careLevel)
    }

    @Test
    fun `careLevel increases with activities`() {
        val pet = PetModel(
            totalFed = 10, totalPlayed = 10, totalTrained = 10,
            totalGroomed = 10, totalAdventures = 10, totalNaps = 10,
            totalTreats = 10, totalTreasureHunts = 10
        )
        assertTrue(pet.careLevel > 0)
    }

    @Test
    fun `careLevel is clamped to 100`() {
        val pet = PetModel(
            totalFed = 9999, totalPlayed = 9999, totalTrained = 9999,
            totalGroomed = 9999, totalAdventures = 9999, totalNaps = 9999,
            totalTreats = 9999, totalTreasureHunts = 9999
        )
        assertTrue(pet.careLevel <= 100)
    }

    // ── nextEvolutionRequirements ────────────────────────────────────

    @Test
    fun `nextEvolutionRequirements returns null for MAJESTIC`() {
        val pet = PetModel(stage = PetEvolutionStage.MAJESTIC)
        assertNull(pet.nextEvolutionRequirements())
    }

    @Test
    fun `nextEvolutionRequirements returns correct for BABY`() {
        val pet = PetModel(stage = PetEvolutionStage.BABY)
        val req = pet.nextEvolutionRequirements()
        assertNotNull(req)
        assertEquals(PetEvolutionStage.JUVENILE, req!!.stage)
        assertEquals(5, req.level)
        assertEquals(20, req.careLevel)
    }

    @Test
    fun `nextEvolutionRequirements ADULT requires happiness`() {
        val pet = PetModel(stage = PetEvolutionStage.JUVENILE)
        val req = pet.nextEvolutionRequirements()
        assertNotNull(req)
        assertEquals(40, req!!.happiness)
    }

    // ── MilestoneData ────────────────────────────────────────────────

    @Test
    fun `MilestoneData returns valid tier title`() {
        val title = MilestoneData.getTierTitle("feeding", 1)
        assertTrue(title.isNotEmpty())
    }

    @Test
    fun `MilestoneData next milestone info is descriptive`() {
        val info = MilestoneData.getNextMilestoneReward("playing", 0, 30)
        assertTrue(info.rewardDescription.contains("30"))
        assertTrue(info.tierName.isNotEmpty())
    }
}
