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
            GameType.MEMORY_GAME    -> memoryConfig(ageGroup, base)
            GameType.SPEED_GAME     -> speedConfig(ageGroup, base)
            GameType.LOGIC_GAME     -> logicConfig(ageGroup, base)
            GameType.PATTERN_MATCH  -> patternConfig(ageGroup, base)
            GameType.COUNTING_GAME  -> countingConfig(ageGroup, base)
            GameType.SHAPE_SORT     -> shapeSortConfig(ageGroup, base)
            GameType.WORD_SCRAMBLE  -> wordConfig(ageGroup, base)
            GameType.TRIVIA         -> triviaConfig(ageGroup, base)
            GameType.SEQUENCE       -> sequenceConfig(ageGroup, base)
            GameType.QUICK_THINK    -> quickThinkConfig(ageGroup, base)
            GameType.CODE_CRACK     -> codeConfig(ageGroup, base)
            GameType.ESTIMATION     -> estimationConfig(ageGroup, base)
            GameType.TYPING_SPEED   -> typingConfig(ageGroup, base)
            GameType.FACT_FICTION    -> factFictionConfig(ageGroup, base)
            GameType.BUDGET_BOSS    -> budgetConfig(ageGroup, base)
            GameType.TIME_ARCHITECT -> timeArchitectConfig(ageGroup, base)
            GameType.DEBATE_PROMPT  -> debateConfig(ageGroup, base)
            GameType.NONE           -> base
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

    private fun patternConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(targetScore = 3, contentComplexity = "EMOJI_PATTERNS", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(targetScore = 3, contentComplexity = "NUMBER_PATTERNS", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(targetScore = 3, contentComplexity = "COMPLEX_SEQUENCES", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(targetScore = 3, contentComplexity = "ADVANCED_SEQUENCES", bonusXpMultiplier = 2.0f)
    }

    private fun countingConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(targetScore = 3, gridSize = 5, contentComplexity = "SIMPLE_COUNT", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(targetScore = 3, gridSize = 15, contentComplexity = "MIXED_COUNT", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(targetScore = 3, contentComplexity = "SKIP_COUNTING", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(targetScore = 3, contentComplexity = "ESTIMATION_COUNT", bonusXpMultiplier = 2.0f)
    }

    private fun shapeSortConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(gridSize = 5, contentComplexity = "2D_SHAPES", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(gridSize = 6, contentComplexity = "MULTI_PROPERTY", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(gridSize = 7, contentComplexity = "3D_SHAPES", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(gridSize = 7, contentComplexity = "GEOMETRY", bonusXpMultiplier = 2.0f)
    }

    private fun triviaConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(targetScore = 2, timeLimit = 20, contentComplexity = "BASIC", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(targetScore = 2, timeLimit = 15, contentComplexity = "GENERAL", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(targetScore = 2, timeLimit = 15, contentComplexity = "SCIENCE_HISTORY", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(targetScore = 2, timeLimit = 12, contentComplexity = "ADVANCED", bonusXpMultiplier = 2.0f)
    }

    private fun sequenceConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(targetScore = 3, contentComplexity = "COUNTING_SEQ", bonusXpMultiplier = 1.0f, hintAvailable = true)
        AgeGroup.EXPLORER -> base.copy(targetScore = 3, contentComplexity = "ARITHMETIC_SEQ", bonusXpMultiplier = 1.2f, hintAvailable = true)
        AgeGroup.TRAILBLAZER -> base.copy(targetScore = 3, contentComplexity = "GEOMETRIC_SEQ", bonusXpMultiplier = 1.5f, hintAvailable = true)
        AgeGroup.LEGEND -> base.copy(targetScore = 3, contentComplexity = "POLYNOMIAL_SEQ", bonusXpMultiplier = 2.0f, hintAvailable = false)
    }

    private fun quickThinkConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(targetScore = 2, timeLimit = 20, contentComplexity = "BASIC", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(targetScore = 2, timeLimit = 15, contentComplexity = "GENERAL", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(targetScore = 2, timeLimit = 15, contentComplexity = "SCIENCE_LITERATURE", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(targetScore = 2, timeLimit = 12, contentComplexity = "CALCULUS_ECONOMICS", bonusXpMultiplier = 2.0f)
    }

    private fun estimationConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(targetScore = 3, contentComplexity = "OBJECT_COUNT", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(targetScore = 3, contentComplexity = "MEASUREMENTS", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(targetScore = 3, contentComplexity = "REAL_WORLD_QTY", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(targetScore = 3, contentComplexity = "FERMI_ESTIMATION", bonusXpMultiplier = 2.0f)
    }

    private fun typingConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(targetScore = 5, timeLimit = 30, contentComplexity = "LETTERS", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(targetScore = 5, timeLimit = 30, contentComplexity = "SHORT_WORDS", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(targetScore = 20, timeLimit = 30, contentComplexity = "SENTENCES", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(targetScore = 30, timeLimit = 30, contentComplexity = "CODE_SNIPPETS", bonusXpMultiplier = 2.0f)
    }

    private fun factFictionConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(targetScore = 3, contentComplexity = "BASIC_FACTS", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(targetScore = 3, contentComplexity = "SCIENCE_FACTS", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(targetScore = 3, contentComplexity = "COMMON_MYTHS", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(targetScore = 3, contentComplexity = "CRITICAL_THINKING", bonusXpMultiplier = 2.0f)
    }

    private fun timeArchitectConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(gridSize = 3, contentComplexity = "TIME_ORDER", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(gridSize = 5, contentComplexity = "TIME_SLOTS", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(gridSize = 7, contentComplexity = "DEPENDENCIES", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(gridSize = 8, contentComplexity = "OPTIMIZATION", bonusXpMultiplier = 2.0f)
    }

    private fun debateConfig(ageGroup: AgeGroup, base: GameDifficultyConfig) = when (ageGroup) {
        AgeGroup.SPROUT -> base.copy(contentComplexity = "PICTURE_CHOICE", bonusXpMultiplier = 1.0f)
        AgeGroup.EXPLORER -> base.copy(contentComplexity = "MORAL_DILEMMA", bonusXpMultiplier = 1.2f)
        AgeGroup.TRAILBLAZER -> base.copy(contentComplexity = "ARGUMENT_BUILD", bonusXpMultiplier = 1.5f)
        AgeGroup.LEGEND -> base.copy(contentComplexity = "ETHICAL_ESSAY", bonusXpMultiplier = 2.0f)
    }

    /** Get all available games for a user's age group, with configs */
    fun allGamesForAgeGroup(ageGroup: AgeGroup): List<GameDifficultyConfig> {
        return GameType.forAgeGroup(ageGroup).map { configFor(it, ageGroup) }
    }
}
