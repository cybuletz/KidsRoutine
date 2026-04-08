package com.kidsroutine.core.model

/**
 * Companion Pet System — emotional retention driver.
 * Each child hatches an egg when they first join,
 * choosing from 6 species. Pet evolves at milestones.
 */

enum class PetSpecies(
    val displayName: String,
    val emoji: String,
    val eggEmoji: String,
    val description: String
) {
    DRAGON("Dragon", "🐲", "🥚", "Fierce and loyal — feeds on courage"),
    WOLF("Wolf", "🐺", "🥚", "Pack leader — grows strong with streaks"),
    PHOENIX("Phoenix", "🦅", "🔥", "Reborn from challenges — never gives up"),
    DOLPHIN("Dolphin", "🐬", "🌊", "Smart and playful — loves learning"),
    UNICORN("Unicorn", "🦄", "✨", "Magical and kind — spreads positivity"),
    ROBOT("Robot", "🤖", "⚙️", "Efficient and clever — masters logic")
}

enum class PetMood(val emoji: String, val description: String) {
    ECSTATIC("🤩", "Over the moon!"),
    HAPPY("😊", "Feeling great!"),
    CONTENT("🙂", "Doing okay"),
    BORED("😐", "Could use some attention"),
    SAD("😢", "Missing you..."),
    SLEEPING("😴", "Zzz... waiting for you")
}

enum class PetEvolutionStage(
    val displayName: String,
    val emoji: String,
    val requiredLevel: Int
) {
    EGG("Egg", "🥚", 0),
    BABY("Baby", "🐣", 1),
    JUVENILE("Juvenile", "🌟", 5),
    ADULT("Adult", "💫", 15),
    MAJESTIC("Majestic", "👑", 30)
}

data class PetModel(
    val petId: String = "",
    val userId: String = "",
    val species: PetSpecies = PetSpecies.DRAGON,
    val name: String = "",
    val stage: PetEvolutionStage = PetEvolutionStage.EGG,

    // Stats (0–100 range)
    val happiness: Int = 80,
    val energy: Int = 80,
    val style: Int = 0,

    // Progression
    val totalFed: Int = 0,       // total task completions feeding pet
    val daysAlive: Int = 0,
    val longestHappyStreak: Int = 0,

    // Timestamps
    val hatchedAt: Long = 0L,
    val lastFedAt: Long = 0L,
    val lastInteractedAt: Long = 0L,

    // Cosmetics
    val accessoryId: String? = null,  // optional pet accessory from shop
    val isPremium: Boolean = false
) {
    /** Current mood based on happiness stat */
    val mood: PetMood
        get() = when {
            happiness >= 90 -> PetMood.ECSTATIC
            happiness >= 70 -> PetMood.HAPPY
            happiness >= 50 -> PetMood.CONTENT
            happiness >= 30 -> PetMood.BORED
            happiness >= 10 -> PetMood.SAD
            else -> PetMood.SLEEPING
        }

    /** Whether pet can evolve to next stage based on user level */
    fun canEvolve(userLevel: Int): Boolean {
        val nextStage = PetEvolutionStage.entries.getOrNull(stage.ordinal + 1)
        return nextStage != null && userLevel >= nextStage.requiredLevel
    }

    /** Display emoji based on species + stage */
    val displayEmoji: String
        get() = when (stage) {
            PetEvolutionStage.EGG -> species.eggEmoji
            PetEvolutionStage.BABY -> species.emoji
            PetEvolutionStage.JUVENILE -> species.emoji
            PetEvolutionStage.ADULT -> species.emoji
            PetEvolutionStage.MAJESTIC -> "👑${species.emoji}"
        }
}
