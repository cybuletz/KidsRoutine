package com.kidsroutine.feature.daily.data

import com.kidsroutine.core.model.DailyStateModel
import com.kidsroutine.core.model.TaskInstance
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.TaskTemplate
import kotlinx.coroutines.flow.Flow

interface DailyRepository {
    fun observeDailyState(userId: String, date: String): Flow<DailyStateModel>
    suspend fun saveDailyTasks(userId: String, date: String, tasks: List<TaskInstance>)
    suspend fun hasTasksForDate(userId: String, date: String): Boolean
    suspend fun fetchTaskTemplatesFromFirestore(familyId: String): List<TaskTemplate>
    suspend fun getAssignedTasks(userId: String, familyId: String): List<TaskModel>
    suspend fun mergeAssignedTasks(userId: String, date: String, newTasks: List<TaskInstance>)
    suspend fun deleteTaskInstance(userId: String, instanceId: String)
}