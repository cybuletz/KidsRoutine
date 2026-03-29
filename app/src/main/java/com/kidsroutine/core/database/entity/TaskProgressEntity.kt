package com.kidsroutine.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_progress")
data class TaskProgressEntity(
    @PrimaryKey val taskInstanceId: String,
    val userId: String,
    val familyId: String,  // ✅ NEW: Required for queries
    val date: String,
    val status: String,
    val completionTime: Long?,
    val validationStatus: String,
    val photoUrl: String?,
    val taskTitle: String = "",
    val syncedToFirestore: Boolean = false
)