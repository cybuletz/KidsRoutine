package com.kidsroutine.core.model

/**
 * All available game types, organized by age-group suitability.
 * GameRenderer dispatches to the appropriate composable based on type + AgeGroup.
 */
enum class GameType(
    val displayName: String,
    val emoji: String,
    val description: String,
    val minAgeGroup: AgeGroup
) {
    NONE("None", "", "No game", AgeGroup.SPROUT),

    // ── Core games (all ages, scale with age group) ────────────────
    MEMORY_GAME("Memory Match", "🧠", "Match pairs of cards", AgeGroup.SPROUT),
    SPEED_GAME("Speed Tap", "⚡", "Tap targets under pressure", AgeGroup.SPROUT),
    LOGIC_GAME("Logic Puzzle", "🧩", "Math and logic challenges", AgeGroup.SPROUT),

    // ── Sprout additions (ages 4-7) ────────────────────────────────
    PATTERN_MATCH("Pattern Match", "🔷", "Complete the pattern sequence", AgeGroup.SPROUT),
    COUNTING_GAME("Counting Fun", "🔢", "Count objects and tap the answer", AgeGroup.SPROUT),
    SHAPE_SORT("Shape Sort", "🟡", "Sort shapes into the right spots", AgeGroup.SPROUT),

    // ── Explorer additions (ages 8-12) ─────────────────────────────
    WORD_SCRAMBLE("Word Scramble", "📝", "Unscramble letters to form words", AgeGroup.EXPLORER),
    TRIVIA("Quick Trivia", "❓", "Answer fun trivia questions", AgeGroup.EXPLORER),
    SEQUENCE("Number Sequence", "🔢", "Find the next number in the pattern", AgeGroup.EXPLORER),

    // ── Trailblazer additions (ages 13-16) ─────────────────────────
    QUICK_THINK("Quick Think", "💡", "Rapid-fire trivia and decisions", AgeGroup.TRAILBLAZER),
    CODE_CRACK("Code Crack", "💻", "Logic puzzles and pattern decoding", AgeGroup.TRAILBLAZER),
    ESTIMATION("Estimation", "📏", "Guess quantities and measurements", AgeGroup.TRAILBLAZER),
    TYPING_SPEED("Typing Speed", "⌨️", "Type words as fast as you can", AgeGroup.TRAILBLAZER),
    FACT_FICTION("Fact or Fiction", "🔍", "Identify true vs false statements", AgeGroup.TRAILBLAZER),

    // ── Legend additions (ages 17+) ────────────────────────────────
    BUDGET_BOSS("Budget Boss", "💰", "Plan a budget under constraints", AgeGroup.LEGEND),
    TIME_ARCHITECT("Time Architect", "⏰", "Optimally schedule a day", AgeGroup.LEGEND),
    DEBATE_PROMPT("Debate Prompt", "🗣️", "Respond to an ethical scenario", AgeGroup.LEGEND);

    companion object {
        /** Get all game types available for a given age group */
        fun forAgeGroup(ageGroup: AgeGroup): List<GameType> =
            entries.filter { it != NONE && it.minAgeGroup.ordinal <= ageGroup.ordinal }

        /** Get new game types unlocked at a specific age group (not available before) */
        fun newForAgeGroup(ageGroup: AgeGroup): List<GameType> =
            entries.filter { it != NONE && it.minAgeGroup == ageGroup }
    }
}