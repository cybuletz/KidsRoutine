package com.kidsroutine.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_instances")
data class TaskInstanceEntity(
    @PrimaryKey val instanceId: String,
    val templateId: String,
    val taskJson: String,
    val assignedDate: String,
    val userId: String,
    val familyId: String,  // ✅ NEW: Required for Firestore path scoping
    val injectedByChallengeId: String? = null,
    val status: String = "PENDING",
    val completedAt: Long = 0L
)