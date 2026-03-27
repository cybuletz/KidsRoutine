package com.kidsroutine.core.database.dao

import androidx.room.*
import com.kidsroutine.core.database.entity.TaskInstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskInstanceDao {

    @Query("SELECT * FROM task_instances WHERE userId = :userId AND assignedDate = :date AND status != 'COMPLETED'")
    fun getTasksForDate(userId: String, date: String): Flow<List<TaskInstanceEntity>>

    @Query("SELECT COUNT(*) FROM task_instances WHERE userId = :userId AND assignedDate = :date")
    suspend fun countTasksForDate(userId: String, date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskInstanceEntity>)

    @Query("DELETE FROM task_instances WHERE assignedDate < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)

    @Query("DELETE FROM task_instances WHERE userId = :userId AND assignedDate = :date")
    suspend fun deleteTasksForDate(userId: String, date: String)

    @Query("UPDATE task_instances SET status = :status, completedAt = :completedAt WHERE instanceId = :instanceId")
    suspend fun updateStatus(instanceId: String, status: String, completedAt: Long)

    @Query("SELECT instanceId FROM task_instances WHERE userId = :userId AND assignedDate = :date")
    suspend fun getExistingInstanceIds(userId: String, date: String): List<String>

    @Query("SELECT * FROM task_instances WHERE userId = :userId AND assignedDate = :date")
    fun getAllTasksForDate(userId: String, date: String): Flow<List<TaskInstanceEntity>>

}
