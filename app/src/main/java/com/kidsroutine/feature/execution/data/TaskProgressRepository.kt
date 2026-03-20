package com.kidsroutine.feature.execution.data

import com.kidsroutine.core.model.TaskProgressModel
import kotlinx.coroutines.flow.Flow

interface TaskProgressRepository {
    suspend fun saveProgress(progress: TaskProgressModel)
    suspend fun getProgress(taskInstanceId: String): TaskProgressModel?
    fun observeProgress(userId: String, date: String): Flow<List<TaskProgressModel>>
    suspend fun syncPendingToFirestore()
}
