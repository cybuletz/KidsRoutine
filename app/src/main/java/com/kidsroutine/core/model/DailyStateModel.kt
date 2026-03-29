package com.kidsroutine.core.model

data class DailyStateModel(
    val date: String = "",
    val userId: String = "",
    val tasks: List<TaskInstance> = emptyList(),
    val completedCount: Int = 0,
    val totalTasksAssigned: Int = 0,  // ✅ ADD THIS
    val totalXpEarned: Int = 0,
    val isGenerated: Boolean = false,
    val generatedAt: Long = 0L
) {
    val completionPercent: Float
        get() = if (totalTasksAssigned == 0) 0f else completedCount.toFloat() / totalTasksAssigned
}