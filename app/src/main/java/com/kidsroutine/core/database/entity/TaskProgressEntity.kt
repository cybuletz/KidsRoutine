package com.kidsroutine.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_progress")
data class TaskProgressEntity(
    @PrimaryKey val taskInstanceId: String,
    val userId: String,
    val date: String,
    val status: String,
    val completionTime: Long?,
    val validationStatus: String,
    val photoUrl: String?,
    val taskTitle: String = "",      // ← ADD THIS
    val familyId: String = "",       // ← ADD THIS
    val syncedToFirestore: Boolean = false
)