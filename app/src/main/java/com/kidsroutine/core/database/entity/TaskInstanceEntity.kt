package com.kidsroutine.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_instances")
data class TaskInstanceEntity(
    @PrimaryKey val instanceId: String,
    val templateId: String,
    val taskJson: String,          // full TaskModel serialized as JSON
    val assignedDate: String,
    val userId: String,
    val injectedByChallengeId: String? = null
)
