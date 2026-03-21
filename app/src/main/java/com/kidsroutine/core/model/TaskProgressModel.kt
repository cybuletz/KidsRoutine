package com.kidsroutine.core.model

enum class TaskStatus { PENDING, COMPLETED, FAILED }
enum class ValidationStatus { PENDING, APPROVED, REJECTED }

data class TaskProgressModel(
    val taskInstanceId: String = "",
    val userId: String = "",
    val date: String = "",
    val status: TaskStatus = TaskStatus.PENDING,
    val completionTime: Long? = null,
    val validationStatus: ValidationStatus = ValidationStatus.PENDING,
    val photoUrl: String? = null,
    val taskTitle: String = "",
    val familyId: String = "",
    val syncedToFirestore: Boolean = false
)