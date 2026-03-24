package com.kidsroutine.feature.generation.data

data class WeeklyDayPlan(
    val dayName: String,        // "Monday"
    val dayEmoji: String,       // "🌅"
    val tasks: List<WeeklyTask>
)

data class WeeklyTask(
    val childName: String,
    val title: String,
    val description: String,
    val estimatedDurationSec: Int,
    val category: String,
    val difficulty: String,
    val xpReward: Int,
    val type: String,
    val requiresCoop: Boolean = false
)

data class GeneratedWeeklyPlan(
    val weekTheme: String,
    val totalFamilyXp: Int,
    val days: List<WeeklyDayPlan>
)

data class WeeklyPlanResponse(
    val success: Boolean,
    val weeklyPlan: GeneratedWeeklyPlan?,
    val cached: Boolean,
    val quotaRemaining: Int
)

enum class WeekTheme(val emoji: String, val label: String, val description: String) {
    ADVENTURE  ("🌍", "Adventure",   "Exploration & outdoor quests"),
    DISCIPLINE ("⚡", "Discipline",  "Focus, structure & growth"),
    CREATIVITY ("🎨", "Creativity",  "Art, music & imagination"),
    WELLNESS   ("🌿", "Wellness",    "Sleep, calm & healthy habits")
}