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
    val totalPlayed: Int = 0,    // total play interactions
    val totalTrained: Int = 0,   // total training sessions
    val totalGroomed: Int = 0,   // total grooming sessions
    val totalAdventures: Int = 0,// total adventures completed
    val totalNaps: Int = 0,      // total nap sessions
    val totalTreats: Int = 0,    // total treats given
    val totalTreasureHunts: Int = 0, // total treasure hunts completed
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

    /** Whether pet can evolve to next stage based on user level and pet stats */
    fun canEvolve(userLevel: Int): Boolean {
        val nextStage = PetEvolutionStage.entries.getOrNull(stage.ordinal + 1) ?: return false
        if (userLevel < nextStage.requiredLevel) return false
        return when (nextStage) {
            PetEvolutionStage.EGG -> false
            PetEvolutionStage.BABY -> true  // just level 1
            PetEvolutionStage.JUVENILE -> careLevel >= 20
            PetEvolutionStage.ADULT -> careLevel >= 45 && happiness >= 40
            PetEvolutionStage.MAJESTIC -> careLevel >= 70 && style >= 25
        }
    }

    /** Get the stat requirements for the next evolution stage (for UI display) */
    fun nextEvolutionRequirements(): EvolutionRequirements? {
        val nextStage = PetEvolutionStage.entries.getOrNull(stage.ordinal + 1) ?: return null
        return when (nextStage) {
            PetEvolutionStage.EGG -> null
            PetEvolutionStage.BABY -> EvolutionRequirements(nextStage, level = 1)
            PetEvolutionStage.JUVENILE -> EvolutionRequirements(nextStage, level = 5, careLevel = 20)
            PetEvolutionStage.ADULT -> EvolutionRequirements(nextStage, level = 15, careLevel = 45, happiness = 40)
            PetEvolutionStage.MAJESTIC -> EvolutionRequirements(nextStage, level = 30, careLevel = 70, style = 25)
        }
    }

    /** Total number of all activities performed with this pet */
    val totalActivities: Int
        get() = totalFed + totalPlayed + totalTrained + totalGroomed +
                totalAdventures + totalNaps + totalTreats + totalTreasureHunts

    /** Care level (0–100) based on overall activity engagement */
    val careLevel: Int
        get() {
            // Each activity type contributes up to ~12.5 points (100 / 8 activities)
            // Diminishing returns: first few of each type count more
            fun activityScore(count: Int, maxScore: Float = 12.5f): Float {
                if (count <= 0) return 0f
                // ln(count+1) / ln(20) gives diminishing curve, capped at 1.0
                val ratio = (kotlin.math.ln((count + 1).toFloat()) / kotlin.math.ln(20f)).coerceAtMost(1f)
                return ratio * maxScore
            }
            return (activityScore(totalFed) + activityScore(totalPlayed) +
                    activityScore(totalTrained) + activityScore(totalGroomed) +
                    activityScore(totalAdventures) + activityScore(totalNaps) +
                    activityScore(totalTreats) + activityScore(totalTreasureHunts))
                .toInt().coerceIn(0, 100)
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

data class PetAccessory(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val xpCost: Int,
    val category: PetAccessoryCategory,
    val happinessBoost: Int = 0,
    val energyBoost: Int = 0,
    /** Duration in minutes for consumable items (0 = permanent accessory) */
    val durationMinutes: Int = 0
) {
    /** Whether this is a permanent accessory (not a consumable) */
    val isPermanent: Boolean get() = durationMinutes == 0
}

enum class PetAccessoryCategory(val emoji: String, val label: String) {
    HAT("🎩", "Hats"),
    COLLAR("📿", "Collars"),
    TOY("🧸", "Toys"),
    BED("🛏️", "Beds"),
    SNACK("🍪", "Treats")
}

data class EvolutionRequirements(
    val stage: PetEvolutionStage,
    val level: Int,
    val careLevel: Int = 0,
    val happiness: Int = 0,
    val style: Int = 0
)

/**
 * Milestone system — each activity type has repeating tiers.
 * Reaching a tier grants a title and contributes to style.
 */
data class MilestoneInfo(
    val tierName: String,
    val rewardDescription: String
)

object MilestoneData {
    /** Titles for each milestone tier (0-based index) */
    private val FEEDING_TITLES = listOf("Snack Helper 🍖", "Kitchen Apprentice 🍳", "Chef's Assistant 👨‍🍳", "Gourmet Chef ⭐", "Master Feeder 🏆")
    private val PLAYING_TITLES = listOf("Playmate 🎾", "Fun Partner 🎮", "Best Friend 💛", "Joy Master 🎪", "Play Champion 🏆")
    private val TRAINING_TITLES = listOf("Student 📖", "Apprentice 🎓", "Coach 🏅", "Trainer Pro 💪", "Grand Master 🏆")
    private val GROOMING_TITLES = listOf("Tidy Helper 🧹", "Spa Assistant 🧴", "Style Scout ✂️", "Grooming Star 💅", "Fashionista 🏆")
    private val ADVENTURE_TITLES = listOf("Explorer 🗺️", "Pathfinder 🧭", "Trailblazer 🏔️", "Adventurer Elite 🌟", "Legend 🏆")
    private val NAP_TITLES = listOf("Rest Buddy 😴", "Dream Helper 🌙", "Sleep Guardian ☁️", "Nap Master 💫", "Dream Weaver 🏆")
    private val TREAT_TITLES = listOf("Cookie Giver 🍪", "Treat Maker 🧁", "Snack Expert 🎂", "Treat Master 🍰", "Pastry Legend 🏆")
    private val TREASURE_TITLES = listOf("Treasure Scout 🔍", "Treasure Hunter 🗝️", "Gold Finder 💎", "Treasure Master 👑", "Legendary Hunter 🏆")

    fun getTierTitle(activityKey: String, tier: Int): String {
        val titles = when (activityKey) {
            "feeding" -> FEEDING_TITLES
            "playing" -> PLAYING_TITLES
            "training" -> TRAINING_TITLES
            "grooming" -> GROOMING_TITLES
            "adventures" -> ADVENTURE_TITLES
            "naps" -> NAP_TITLES
            "treats" -> TREAT_TITLES
            "treasure_hunts" -> TREASURE_TITLES
            else -> return "⭐ Tier $tier"
        }
        if (tier < 1) return "⭐ Tier $tier"
        return titles.getOrElse(tier - 1) { titles.lastOrNull() ?: "⭐ Tier $tier" }
    }

    fun getNextMilestoneReward(activityKey: String, currentCount: Int, milestone: Int): MilestoneInfo {
        val nextTier = (currentCount / milestone) + 1
        val nextMilestone = nextTier * milestone
        val title = getTierTitle(activityKey, nextTier)
        return MilestoneInfo(
            tierName = title,
            rewardDescription = "At $nextMilestone: Earn \"$title\" + ✨ Style boost!"
        )
    }
}
