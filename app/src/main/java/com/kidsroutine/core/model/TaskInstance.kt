package com.kidsroutine.core.model

data class TaskInstance(
    val instanceId: String = "",
    val templateId: String = "",
    val task: TaskModel = TaskModel(),
    val resolvedValues: Map<String, Any> = emptyMap(),
    val assignedDate: String = "",   // yyyy-MM-dd
    val userId: String = "",
    val injectedByChallengeId: String? = null,  // null = regular task
    val status: TaskStatus = TaskStatus.PENDING,
    val completedAt: Long = 0L       // epoch ms — set when status → COMPLETED
)
