package com.kidsroutine.core.model

/**
 * Age-Adaptive Game Configuration.
 * Defines difficulty parameters for each game type at each age group.
 * Used by GameRenderer and game blocks to dynamically configure gameplay.
 */

data class GameDifficultyConfig(
    val gameType: GameType = GameType.NONE,
    val ageGroup: AgeGroup = AgeGroup.EXPLORER,
    val timeLimit: Int = 0,              // seconds, 0 = no limit
    val gridSize: Int = 6,               // for memory: total cards
    val targetScore: Int = 3,            // correct answers to win
    val contentComplexity: String = "BASIC",  // BASIC, MEDIUM, ADVANCED, EXPERT
    val hintAvailable: Boolean = true,
    val bonusXpMultiplier: Float = 1.0f  // XP bonus for harder age groups
)

/**
 * Factory for creating age-appropriate game configs.
 */
object GameConfigFactory {

    /** Get game configuration for a specific game type and age group */
    fun configFor(gameType: GameType, ageGroup: AgeGroup): GameDifficultyConfig {
        val base = GameDifficultyConfig(gameType = gameType, ageGroup = ageGroup)
        return when (gameType) {
            GameType.MEMORY_GAME -> memoryConfig(ageGroup, base)
            GameType.SPEED_GAME -> speedConfig(ageGroup, base)
            GameType.LOGIC_GAME -> logicConfig(ageGroup, base)
            GameType.WORD_SCRAMBLE -> wordConfig(ageGroup, base)
            GameType.CODE_CRACK -> codeConfig(ageGroup, base)
            GameType.BUDGET_BOSS -> budgetConfig(ageGroup, base)
            else -> base
        }
    }

    private fun memoryConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(gridSize = 6, timeLimit = 0, contentComplexity = "EMOJI", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(gridSize = 10, timeLimit = 60, contentComplexity = "EMOJI_THEMED", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(gridSize = 12, timeLimit = 45, contentComplexity = "FLAGS_SYMBOLS", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(gridSize = 14, timeLimit = 40, contentComplexity = "PAIRED_ASSOCIATION", bonusXpMultiplier = 2.0f)
    }

    private fun speedConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(timeLimit = 7, targetScore = 3, contentComplexity = "COLORS", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(timeLimit = 5, targetScore = 3, contentComplexity = "COLORS", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(timeLimit = 4, targetScore = 5, contentComplexity = "MULTI_STEP", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(timeLimit = 3, targetScore = 7, contentComplexity = "REFLEX_DECISION", bonusXpMultiplier = 2.0f)
    }

    private fun logicConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(targetScore = 3, contentComplexity = "ADDITION", bonusXpMultiplier = 1.0f, hintAvailable = true)
        AgeGroup.EXPLORER -> base.copy(targetScore = 3, contentComplexity = "ARITHMETIC", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(targetScore = 3, contentComplexity = "ALGEBRA_DIVISION", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(targetScore = 3, contentComplexity = "PERCENTAGES_EXPONENTS", bonusXpMultiplier = 2.0f)
    }

    private fun wordConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(contentComplexity = "3_LETTERS", hintAvailable = true, bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(contentComplexity = "5_LETTERS", hintAvailable = true, bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(contentComplexity = "8_LETTERS", hintAvailable = true, bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(contentComplexity = "12_LETTERS", hintAvailable = false, bonusXpMultiplier = 2.0f)
    }

    private fun codeConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(contentComplexity = "PATTERNS", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(contentComplexity = "PATTERNS_NUMBERS", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(contentComplexity = "BASIC_CODE", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(contentComplexity = "ALGORITHMS", bonusXpMultiplier = 2.0f)
    }

    private fun budgetConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(contentComplexity = "COUNTING", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(contentComplexity = "SIMPLE_BUDGET", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(contentComplexity = "MEAL_PLANNING", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(contentComplexity = "REAL_WORLD", bonusXpMultiplier = 2.0f)
    }

    /** Get all available games for a user's age group, with configs */
    fun allGamesForAgeGroup(ageGroup: AgeGroup): List<GameDifficultyConfig> {
        return GameType.forAgeGroup(ageGroup).map { configFor(it, ageGroup) }
    }
}
