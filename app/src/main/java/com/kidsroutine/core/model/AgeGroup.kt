package com.kidsroutine.core.model

/**
 * Age-adaptive grouping system.
 * Each child is assigned an AgeGroup based on their age,
 * which drives UI style, game difficulty, notification tone,
 * and autonomy level.
 */
enum class AgeGroup(
    val displayName: String,
    val emoji: String,
    val minAge: Int,
    val maxAge: Int,
    val description: String
) {
    SPROUT(
        displayName = "Sprout",
        emoji = "🌱",
        minAge = 4,
        maxAge = 7,
        description = "Simple, colorful, lots of encouragement"
    ),
    EXPLORER(
        displayName = "Explorer",
        emoji = "🧭",
        minAge = 8,
        maxAge = 12,
        description = "Expanding challenges, growing independence"
    ),
    TRAILBLAZER(
        displayName = "Trailblazer",
        emoji = "🔥",
        minAge = 13,
        maxAge = 16,
        description = "Real-world relevance, dark mode, autonomy"
    ),
    LEGEND(
        displayName = "Legend",
        emoji = "⚡",
        minAge = 17,
        maxAge = 99,
        description = "Self-managing, strategy, life skills"
    );

    /** Whether this group should use teen/mature UI styling */
    val isTeenMode: Boolean get() = this == TRAILBLAZER || this == LEGEND

    /** Whether this group should default to dark theme */
    val prefersDarkTheme: Boolean get() = isTeenMode

    /** Autonomy level: 0 = parent controls all, 3 = fully self-managed */
    val autonomyLevel: Int
        get() = when (this) {
            SPROUT -> 0       // Parent creates all tasks, validates everything
            EXPLORER -> 1     // Child can propose tasks, self-validate easy
            TRAILBLAZER -> 2  // Child creates own goals, optional parent approval
            LEGEND -> 3       // Self-managing, parent is observer
        }

    companion object {
        /** Determine AgeGroup from age integer */
        fun fromAge(age: Int): AgeGroup = when {
            age <= 7  -> SPROUT
            age <= 12 -> EXPLORER
            age <= 16 -> TRAILBLAZER
            else      -> LEGEND
        }
    }
}
