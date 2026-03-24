package com.kidsroutine.feature.generation.data

/**
 * A fully AI-generated day plan (5 tasks + metadata).
 * Returned by generateDailyPlanAI Cloud Function.
 */
data class GeneratedDailyPlan(
    val theme: String,
    val totalXp: Int,
    val mood: String,
    val tasks: List<GeneratedPlanTask>
)

data class GeneratedPlanTask(
    val title: String,
    val description: String,
    val estimatedDurationSec: Int,
    val category: String,
    val difficulty: String,
    val xpReward: Int,
    val type: String,
    val timeSlot: String,          // MORNING | AFTERNOON | EVENING
    val requiresCoop: Boolean = false
)

data class DailyPlanResponse(
    val success: Boolean,
    val plan: GeneratedDailyPlan?,
    val cached: Boolean,
    val quotaRemaining: Int
)

enum class DayMood(val emoji: String, val label: String, val description: String) {
    ENERGETIC("⚡", "Energetic",  "High intensity day — go all out!"),
    NORMAL   ("😊", "Normal",     "Balanced mix of everything"),
    CALM     ("🌿", "Calm",       "Easy, creative and relaxed")
}