package com.kidsroutine.core.engine.pet_engine

import com.kidsroutine.core.model.PetEvolutionStage
import com.kidsroutine.core.model.PetModel
import com.kidsroutine.core.model.PetMood
import com.kidsroutine.core.model.PetSpecies
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PetEngineTest {

    private lateinit var engine: PetEngine

    @Before
    fun setUp() {
        engine = PetEngine()
    }

    // ── feedPet ─────────────────────────────────────────────────────────

    @Test
    fun `feedPet increases happiness and energy`() {
        val pet = createPet(happiness = 50, energy = 50)
        val fed = engine.feedPet(pet, xpEarned = 50)
        assertTrue(fed.happiness > 50)
        assertTrue(fed.energy > 50)
    }

    @Test
    fun `feedPet increments totalFed`() {
        val pet = createPet(totalFed = 5)
        val fed = engine.feedPet(pet, xpEarned = 50)
        assertEquals(6, fed.totalFed)
    }

    @Test
    fun `feedPet clamps happiness to 100`() {
        val pet = createPet(happiness = 95)
        val fed = engine.feedPet(pet, xpEarned = 100)
        assertTrue(fed.happiness <= 100)
    }

    @Test
    fun `feedPet clamps energy to 100`() {
        val pet = createPet(energy = 98)
        val fed = engine.feedPet(pet, xpEarned = 100)
        assertTrue(fed.energy <= 100)
    }

    @Test
    fun `feedPet updates lastFedAt timestamp`() {
        val pet = createPet(lastFedAt = 0L)
        val fed = engine.feedPet(pet, xpEarned = 50)
        assertTrue(fed.lastFedAt > 0)
    }

    @Test
    fun `feedPet happiness gain is clamped between 5 and 20`() {
        // Very small XP → minimum gain of 5
        val petLowXp = createPet(happiness = 50)
        val fedLowXp = engine.feedPet(petLowXp, xpEarned = 5)
        assertEquals(55, fedLowXp.happiness) // 5/5 = 1 → coerced to 5

        // Very large XP → maximum gain of 20
        val petHighXp = createPet(happiness = 50)
        val fedHighXp = engine.feedPet(petHighXp, xpEarned = 500)
        assertEquals(70, fedHighXp.happiness) // 500/5 = 100 → coerced to 20
    }

    // ── applyDailyDecay ─────────────────────────────────────────────────

    @Test
    fun `applyDailyDecay reduces happiness by 5 for normal active day`() {
        val pet = createPet(happiness = 80, energy = 80)
        val decayed = engine.applyDailyDecay(pet, daysInactive = 0)
        assertEquals(75, decayed.happiness)
        assertEquals(77, decayed.energy)
    }

    @Test
    fun `applyDailyDecay has higher penalty for 1 missed day`() {
        val pet = createPet(happiness = 80, energy = 80)
        val decayed = engine.applyDailyDecay(pet, daysInactive = 1)
        assertEquals(70, decayed.happiness)
    }

    @Test
    fun `applyDailyDecay has severe penalty for 4+ inactive days`() {
        val pet = createPet(happiness = 80, energy = 80)
        val decayed = engine.applyDailyDecay(pet, daysInactive = 5)
        assertEquals(55, decayed.happiness)
        assertEquals(65, decayed.energy)
    }

    @Test
    fun `applyDailyDecay clamps stats to zero minimum`() {
        val pet = createPet(happiness = 3, energy = 2)
        val decayed = engine.applyDailyDecay(pet, daysInactive = 5)
        assertEquals(0, decayed.happiness)
        assertEquals(0, decayed.energy)
    }

    @Test
    fun `applyDailyDecay increments daysAlive`() {
        val pet = createPet(daysAlive = 10)
        val decayed = engine.applyDailyDecay(pet)
        assertEquals(11, decayed.daysAlive)
    }

    // ── interactWithPet ─────────────────────────────────────────────────

    @Test
    fun `interactWithPet adds 3 happiness`() {
        val pet = createPet(happiness = 50)
        val interacted = engine.interactWithPet(pet)
        assertEquals(53, interacted.happiness)
    }

    @Test
    fun `interactWithPet clamps happiness to 100`() {
        val pet = createPet(happiness = 99)
        val interacted = engine.interactWithPet(pet)
        assertEquals(100, interacted.happiness)
    }

    @Test
    fun `interactWithPet updates lastInteractedAt`() {
        val pet = createPet(lastInteractedAt = 0L)
        val interacted = engine.interactWithPet(pet)
        assertTrue(interacted.lastInteractedAt > 0)
    }

    // ── applyStreakBonus ────────────────────────────────────────────────

    @Test
    fun `applyStreakBonus gives small bonus for short streak`() {
        val pet = createPet(energy = 50)
        val bonused = engine.applyStreakBonus(pet, streakDays = 2)
        assertEquals(52, bonused.energy)
        assertEquals(80, bonused.happiness) // no happiness bonus for short streaks (default is 80)
    }

    @Test
    fun `applyStreakBonus gives medium bonus for 7-day streak`() {
        val pet = createPet(energy = 50, happiness = 50)
        val bonused = engine.applyStreakBonus(pet, streakDays = 7)
        assertEquals(60, bonused.energy)   // +10 energy
        assertEquals(55, bonused.happiness) // +5 happiness
    }

    @Test
    fun `applyStreakBonus gives large bonus for 30+ day streak`() {
        val pet = createPet(energy = 50, happiness = 50)
        val bonused = engine.applyStreakBonus(pet, streakDays = 30)
        assertEquals(65, bonused.energy)   // +15 energy
        assertEquals(55, bonused.happiness) // +5 happiness
    }

    @Test
    fun `applyStreakBonus updates longestHappyStreak`() {
        val pet = createPet(longestHappyStreak = 5)
        val bonused = engine.applyStreakBonus(pet, streakDays = 10)
        assertEquals(10, bonused.longestHappyStreak)
    }

    @Test
    fun `applyStreakBonus does not reduce longestHappyStreak`() {
        val pet = createPet(longestHappyStreak = 20)
        val bonused = engine.applyStreakBonus(pet, streakDays = 5)
        assertEquals(20, bonused.longestHappyStreak)
    }

    // ── checkEvolution ──────────────────────────────────────────────────

    @Test
    fun `checkEvolution evolves EGG to BABY at level 1`() {
        val pet = createPet(stage = PetEvolutionStage.EGG)
        val evolved = engine.checkEvolution(pet, userLevel = 1)
        assertEquals(PetEvolutionStage.BABY, evolved.stage)
    }

    @Test
    fun `checkEvolution does not evolve if level too low`() {
        val pet = createPet(stage = PetEvolutionStage.JUVENILE) // needs level 15 for ADULT
        val result = engine.checkEvolution(pet, userLevel = 10)
        assertEquals(PetEvolutionStage.JUVENILE, result.stage)
    }

    @Test
    fun `checkEvolution sets stats to 100 on evolution`() {
        val pet = createPet(stage = PetEvolutionStage.EGG, happiness = 50, energy = 50)
        val evolved = engine.checkEvolution(pet, userLevel = 1)
        assertEquals(100, evolved.happiness)
        assertEquals(100, evolved.energy)
    }

    @Test
    fun `checkEvolution does not evolve MAJESTIC further`() {
        val pet = createPet(stage = PetEvolutionStage.MAJESTIC)
        val result = engine.checkEvolution(pet, userLevel = 99)
        assertEquals(PetEvolutionStage.MAJESTIC, result.stage)
    }

    // ── getRecommendedAction ────────────────────────────────────────────

    @Test
    fun `getRecommendedAction returns correct text for each mood`() {
        val ecstatic = createPet(happiness = 95)
        assertTrue(engine.getRecommendedAction(ecstatic).contains("thriving"))

        val sad = createPet(happiness = 15)
        assertTrue(engine.getRecommendedAction(sad).contains("misses you"))

        val sleeping = createPet(happiness = 5)
        assertTrue(engine.getRecommendedAction(sleeping).contains("asleep"))
    }

    // ── PetModel.mood ──────────────────────────────────────────────────

    @Test
    fun `pet mood is ECSTATIC at 90+ happiness`() {
        assertEquals(PetMood.ECSTATIC, createPet(happiness = 95).mood)
    }

    @Test
    fun `pet mood is SLEEPING at under 10 happiness`() {
        assertEquals(PetMood.SLEEPING, createPet(happiness = 5).mood)
    }

    // ── napPet ──────────────────────────────────────────────────────────

    @Test
    fun `napPet restores energy significantly`() {
        val pet = createPet(energy = 50)
        val napped = engine.napPet(pet)
        assertEquals(62, napped.energy)  // +12 energy
    }

    @Test
    fun `napPet gives small happiness boost`() {
        val pet = createPet(happiness = 50, energy = 50)
        val napped = engine.napPet(pet)
        assertEquals(52, napped.happiness)  // +2 happiness
    }

    @Test
    fun `napPet clamps energy to 100`() {
        val pet = createPet(energy = 95)
        val napped = engine.napPet(pet)
        assertEquals(100, napped.energy)
    }

    @Test
    fun `napPet updates lastInteractedAt`() {
        val pet = createPet(lastInteractedAt = 0L)
        val napped = engine.napPet(pet)
        assertTrue(napped.lastInteractedAt > 0)
    }

    // ── treatPet ─────────────────────────────────────────────────────────

    @Test
    fun `treatPet increases happiness by 6`() {
        val pet = createPet(happiness = 50)
        val treated = engine.treatPet(pet)
        assertEquals(56, treated.happiness)
    }

    @Test
    fun `treatPet increases energy by 4`() {
        val pet = createPet(energy = 50)
        val treated = engine.treatPet(pet)
        assertEquals(54, treated.energy)
    }

    @Test
    fun `treatPet clamps happiness to 100`() {
        val pet = createPet(happiness = 98)
        val treated = engine.treatPet(pet)
        assertEquals(100, treated.happiness)
    }

    @Test
    fun `treatPet updates lastInteractedAt`() {
        val pet = createPet(lastInteractedAt = 0L)
        val treated = engine.treatPet(pet)
        assertTrue(treated.lastInteractedAt > 0)
    }

    // ── treasureHuntWithPet ──────────────────────────────────────────────

    @Test
    fun `treasureHuntWithPet boosts happiness more when happiness is lower`() {
        val pet = createPet(happiness = 30, energy = 70)
        val hunted = engine.treasureHuntWithPet(pet)
        assertEquals(42, hunted.happiness)  // +12 (lower stat gets bigger boost)
        assertEquals(75, hunted.energy)     // +5
    }

    @Test
    fun `treasureHuntWithPet boosts energy more when energy is lower`() {
        val pet = createPet(happiness = 70, energy = 30)
        val hunted = engine.treasureHuntWithPet(pet)
        assertEquals(78, hunted.happiness)  // +8
        assertEquals(40, hunted.energy)     // +10 (lower stat gets bigger boost)
    }

    @Test
    fun `treasureHuntWithPet equal stats boosts happiness more`() {
        val pet = createPet(happiness = 50, energy = 50)
        val hunted = engine.treasureHuntWithPet(pet)
        assertEquals(62, hunted.happiness)  // +12 (happiness <= energy → boostLower = true)
        assertEquals(55, hunted.energy)     // +5
    }

    @Test
    fun `treasureHuntWithPet clamps stats to 100`() {
        val pet = createPet(happiness = 95, energy = 96)
        val hunted = engine.treasureHuntWithPet(pet)
        assertTrue(hunted.happiness <= 100)
        assertTrue(hunted.energy <= 100)
    }

    @Test
    fun `treasureHuntWithPet updates lastInteractedAt`() {
        val pet = createPet(lastInteractedAt = 0L)
        val hunted = engine.treasureHuntWithPet(pet)
        assertTrue(hunted.lastInteractedAt > 0)
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun createPet(
        happiness: Int = 80,
        energy: Int = 80,
        totalFed: Int = 0,
        daysAlive: Int = 0,
        longestHappyStreak: Int = 0,
        stage: PetEvolutionStage = PetEvolutionStage.BABY,
        lastFedAt: Long = 0L,
        lastInteractedAt: Long = 0L
    ) = PetModel(
        petId = "test_pet",
        userId = "test_user",
        species = PetSpecies.DRAGON,
        name = "TestPet",
        stage = stage,
        happiness = happiness,
        energy = energy,
        totalFed = totalFed,
        daysAlive = daysAlive,
        longestHappyStreak = longestHappyStreak,
        lastFedAt = lastFedAt,
        lastInteractedAt = lastInteractedAt
    )
}
