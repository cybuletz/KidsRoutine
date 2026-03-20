package com.kidsroutine.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_progress")
data class TaskProgressEntity(
    @PrimaryKey val taskInstanceId: String,
    val userId: String,
    val date: String,
    val status: String,             // PENDING | COMPLETED | FAILED
    val completionTime: Long?,
    val validationStatus: String,   // PENDING | APPROVED | REJECTED
    val photoUrl: String?,
    val syncedToFirestore: Boolean = false
)
