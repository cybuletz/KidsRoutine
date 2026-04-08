package com.kidsroutine.core.engine.pet_engine

import com.kidsroutine.core.model.PetEvolutionStage
import com.kidsroutine.core.model.PetModel
import com.kidsroutine.core.model.PetMood
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pet Engine — manages companion pet stats, feeding, evolution, and mood.
 * Pet happiness decays daily; completing tasks "feeds" the pet.
 */
@Singleton
class PetEngine @Inject constructor() {

    /**
     * Feed the pet when a task is completed.
     * Increases happiness and energy, increments feed count.
     */
    fun feedPet(pet: PetModel, xpEarned: Int): PetModel {
        val happinessGain = (xpEarned / 5).coerceIn(5, 20)
        val energyGain = (xpEarned / 10).coerceIn(2, 10)
        return pet.copy(
            happiness = (pet.happiness + happinessGain).coerceIn(0, 100),
            energy = (pet.energy + energyGain).coerceIn(0, 100),
            totalFed = pet.totalFed + 1,
            lastFedAt = System.currentTimeMillis(),
            lastInteractedAt = System.currentTimeMillis()
        )
    }

    /**
     * Apply daily decay to pet stats.
     * Called once per day when the app opens.
     * Neglected pets lose happiness faster.
     */
    fun applyDailyDecay(pet: PetModel, daysInactive: Int = 0): PetModel {
        val happinessDecay = when {
            daysInactive == 0 -> 5     // normal daily decay
            daysInactive == 1 -> 10    // missed 1 day
            daysInactive <= 3 -> 15    // missed 2-3 days
            else -> 25                  // absent 4+ days
        }
        val energyDecay = when {
            daysInactive == 0 -> 3
            daysInactive <= 3 -> 8
            else -> 15
        }
        return pet.copy(
            happiness = (pet.happiness - happinessDecay).coerceIn(0, 100),
            energy = (pet.energy - energyDecay).coerceIn(0, 100),
            daysAlive = pet.daysAlive + 1
        )
    }

    /**
     * Interact with pet (petting, playing — non-task interaction).
     * Small happiness boost, resets interaction timer.
     */
    fun interactWithPet(pet: PetModel): PetModel {
        return pet.copy(
            happiness = (pet.happiness + 3).coerceIn(0, 100),
            lastInteractedAt = System.currentTimeMillis()
        )
    }

    /**
     * Apply streak bonus to pet.
     * Streak days increase energy, long streaks give happiness boost.
     */
    fun applyStreakBonus(pet: PetModel, streakDays: Int): PetModel {
        val energyBonus = when {
            streakDays >= 30 -> 15
            streakDays >= 7 -> 10
            streakDays >= 3 -> 5
            else -> 2
        }
        val happinessBonus = if (streakDays >= 7) 5 else 0
        return pet.copy(
            happiness = (pet.happiness + happinessBonus).coerceIn(0, 100),
            energy = (pet.energy + energyBonus).coerceIn(0, 100),
            longestHappyStreak = maxOf(pet.longestHappyStreak, streakDays)
        )
    }

    /**
     * Check and apply evolution if eligible.
     * Returns evolved pet or unchanged pet.
     */
    fun checkEvolution(pet: PetModel, userLevel: Int): PetModel {
        if (!pet.canEvolve(userLevel)) return pet
        val nextStage = PetEvolutionStage.entries.getOrNull(pet.stage.ordinal + 1) ?: return pet
        return pet.copy(
            stage = nextStage,
            happiness = 100,   // evolution celebration!
            energy = 100
        )
    }

    /**
     * Apply avatar style bonus.
     * When user equips premium avatar items, pet style increases.
     */
    fun updateStyle(pet: PetModel, premiumItemCount: Int): PetModel {
        val styleValue = (premiumItemCount * 5).coerceIn(0, 100)
        return pet.copy(style = styleValue)
    }

    /**
     * Get recommended action text for pet state.
     */
    fun getRecommendedAction(pet: PetModel): String = when (pet.mood) {
        PetMood.ECSTATIC -> "Your pet is thriving! Keep it up!"
        PetMood.HAPPY -> "Your pet is happy! A task will make them ecstatic!"
        PetMood.CONTENT -> "Complete a task to boost your pet's mood!"
        PetMood.BORED -> "Your pet is bored... They need a task from you!"
        PetMood.SAD -> "Your pet misses you! Complete a task to cheer them up!"
        PetMood.SLEEPING -> "Your pet fell asleep waiting... Wake them with a task!"
    }
}
