package com.kidsroutine.core.model

/**
 * Boss Battle System — Family Co-op Events.
 * Weekly "Family Boss" appears; each completed task deals damage.
 * Inspired by Habitica's party quests for social accountability.
 */

enum class BossType(
    val displayName: String,
    val emoji: String,
    val description: String,
    val baseHp: Int
) {
    HOMEWORK_HYDRA(
        displayName = "Homework Hydra",
        emoji = "🐉",
        description = "A multi-headed beast of procrastination!",
        baseHp = 200
    ),
    CHORE_CHIMERA(
        displayName = "Chore Chimera",
        emoji = "🦁",
        description = "Part mess, part laziness, all trouble!",
        baseHp = 250
    ),
    BEDTIME_BASILISK(
        displayName = "Bedtime Basilisk",
        emoji = "🐍",
        description = "Its gaze turns bedtimes into screen time!",
        baseHp = 180
    ),
    DISTRACTION_DRAGON(
        displayName = "Distraction Dragon",
        emoji = "🐲",
        description = "Breathes fire of endless scrolling!",
        baseHp = 300
    ),
    SCREEN_SPECTER(
        displayName = "Screen Specter",
        emoji = "👻",
        description = "Haunts you with just-one-more-episode!",
        baseHp = 220
    ),
    CHAOS_KRAKEN(
        displayName = "Chaos Kraken",
        emoji = "🦑",
        description = "Tentacles of disorganization everywhere!",
        baseHp = 350
    )
}

data class BossModel(
    val bossId: String = "",
    val familyId: String = "",
    val type: BossType = BossType.HOMEWORK_HYDRA,
    val week: String = "",              // "2026-W15"
    val season: Season = Season.NONE,

    // HP system
    val maxHp: Int = 200,
    val currentHp: Int = 200,

    // Participation
    val damageLog: Map<String, Int> = emptyMap(), // userId -> total damage dealt
    val totalDamage: Int = 0,

    // Rewards
    val victoryXpBonus: Int = 100,
    val victoryLootRarity: LootBoxRarity = LootBoxRarity.RARE,
    val defeatXpPenalty: Int = 20,      // minor XP loss on defeat

    // Status
    val isDefeated: Boolean = false,
    val isExpired: Boolean = false,
    val startedAt: Long = 0L,
    val deadline: Long = 0L             // Sunday midnight
) {
    val hpPercentage: Float get() = if (maxHp > 0) currentHp.toFloat() / maxHp else 0f
    val isAlive: Boolean get() = currentHp > 0 && !isExpired

    /** Calculate damage from a single task completion */
    fun damageForTask(task: TaskModel): Int {
        val baseDamage = when (task.difficulty) {
            DifficultyLevel.EASY -> 5
            DifficultyLevel.MEDIUM -> 10
            DifficultyLevel.HARD -> 20
        }
        val coopBonus = if (task.requiresCoop) 1.5f else 1.0f
        return (baseDamage * coopBonus).toInt()
    }

    /** Calculate boss HP based on family size */
    companion object {
        fun hpForFamily(bossType: BossType, familySize: Int, difficulty: DifficultyLevel): Int {
            val sizeMultiplier = (familySize * 0.75f).coerceAtLeast(1f)
            val diffMultiplier = when (difficulty) {
                DifficultyLevel.EASY -> 0.7f
                DifficultyLevel.MEDIUM -> 1.0f
                DifficultyLevel.HARD -> 1.5f
            }
            return (bossType.baseHp * sizeMultiplier * diffMultiplier).toInt()
        }
    }
}
