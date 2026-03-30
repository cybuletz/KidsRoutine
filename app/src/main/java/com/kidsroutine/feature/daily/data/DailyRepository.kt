package com.kidsroutine.feature.daily.data

import com.kidsroutine.core.model.DailyStateModel
import com.kidsroutine.core.model.TaskInstance
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.TaskTemplate
import kotlinx.coroutines.flow.Flow

interface DailyRepository {
    // ✅ NEW: requires familyId
    suspend fun saveDailyTasks(familyId: String, userId: String, date: String, tasks: List<TaskInstance>)

    // ✅ NEW: requires familyId
    fun observeDailyState(familyId: String, userId: String, date: String): Flow<DailyStateModel>

    // ✅ NEW: requires familyId
    suspend fun hasTasksForDate(familyId: String, userId: String, date: String): Boolean

    // ✅ NEW: requires familyId
    suspend fun mergeAssignedTasks(familyId: String, userId: String, date: String, freshTasks: List<TaskInstance>)

    // ✅ NEW: requires familyId
    suspend fun refreshTasksForDate(familyId: String, userId: String, date: String)
    suspend fun fetchTaskTemplatesFromFirestore(familyId: String): List<TaskTemplate>
    suspend fun getAssignedTasks(userId: String, familyId: String): List<TaskModel>
    // ✅ NEW: requires familyId
    suspend fun deleteTaskInstance(familyId: String, userId: String, instanceId: String)

    suspend fun updateTaskInRoom(familyId: String, userId: String, instanceId: String, updatedTask: TaskModel)

    suspend fun deleteOldCompletedInstances(familyId: String, userId: String, today: String)

    suspend fun replaceTasksForDate(familyId: String, userId: String, date: String, instances: List<TaskInstance>)

}