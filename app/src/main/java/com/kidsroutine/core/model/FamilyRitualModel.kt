package com.kidsroutine.core.model

/**
 * Family Rituals — Move beyond "chores" to meaningful family moments.
 * Differentiation from typical chore apps.
 */

enum class RitualType(val displayName: String, val emoji: String) {
    GRATITUDE_CIRCLE("Gratitude Circle", "🙏"),
    FAMILY_MEETING("Family Meeting", "👨‍👩‍👧‍👦"),
    MEMORY_LANE("Memory Lane", "📸"),
    SHARED_GOAL("Shared Goal", "🎯"),
    CELEBRATION("Celebration", "🎉")
}

enum class RitualFrequency { DAILY, WEEKLY, MONTHLY, ONE_TIME }

data class FamilyRitual(
    val ritualId: String = "",
    val familyId: String = "",
    val type: RitualType = RitualType.GRATITUDE_CIRCLE,
    val title: String = "",
    val description: String = "",
    val frequency: RitualFrequency = RitualFrequency.DAILY,

    // For gratitude circle
    val gratitudeEntries: Map<String, String> = emptyMap(), // userId -> today's gratitude

    // For family meeting
    val meetingDurationMin: Int = 15,
    val agendaItems: List<String> = emptyList(),

    // For shared goal
    val goalTitle: String = "",
    val goalTarget: Int = 0,       // e.g., 3 (cook together 3x)
    val goalProgress: Int = 0,
    val goalUnit: String = "times",

    // XP reward (everyone gets this)
    val completionXp: Int = 25,

    // State
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val lastCompletedAt: Long = 0L,

    // Scheduling
    val scheduledDay: String = "",   // "MONDAY" for weekly
    val scheduledTime: String = ""   // "19:00"
) {
    val goalProgressPercent: Float
        get() = if (goalTarget > 0) (goalProgress.toFloat() / goalTarget).coerceIn(0f, 1f) else 0f
}
