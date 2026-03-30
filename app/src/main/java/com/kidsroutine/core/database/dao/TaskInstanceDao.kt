package com.kidsroutine.core.database.dao

import androidx.room.*
import com.kidsroutine.core.database.entity.TaskInstanceEntity
import com.kidsroutine.core.database.entity.TaskProgressEntity
import com.kidsroutine.core.model.TaskInstance
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskInstanceDao {

    @Query("SELECT * FROM task_instances WHERE familyId = :familyId AND userId = :userId AND assignedDate = :date AND status = 'PENDING'")
    fun getTasksForDate(familyId: String, userId: String, date: String): Flow<List<TaskInstanceEntity>>

    @Query("SELECT COUNT(*) FROM task_instances WHERE familyId = :familyId AND userId = :userId AND assignedDate = :date")
    suspend fun countTasksForDate(familyId: String, userId: String, date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskInstanceEntity>)

    @Query("DELETE FROM task_instances WHERE assignedDate < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)

    @Query("DELETE FROM task_instances WHERE familyId = :familyId AND userId = :userId AND assignedDate = :date")
    suspend fun deleteTasksForDate(familyId: String, userId: String, date: String)

    @Query("UPDATE task_instances SET status = :status, completedAt = :completedAt WHERE instanceId = :instanceId")
    suspend fun updateStatus(instanceId: String, status: String, completedAt: Long)

    @Query("SELECT instanceId FROM task_instances WHERE familyId = :familyId AND userId = :userId AND assignedDate = :date")
    suspend fun getExistingInstanceIds(familyId: String, userId: String, date: String): List<String>

    @Query("SELECT * FROM task_instances WHERE familyId = :familyId AND userId = :userId AND assignedDate = :date")
    fun getAllTasksForDate(familyId: String, userId: String, date: String): Flow<List<TaskInstanceEntity>>

    @Query("DELETE FROM task_instances WHERE familyId = :familyId AND userId = :userId AND instanceId = :instanceId")
    suspend fun deleteByInstanceId(familyId: String, userId: String, instanceId: String)

    // Mark as completed INSTEAD of delete
    @Query("UPDATE task_instances SET status = 'COMPLETED', completedAt = :timestamp WHERE familyId = :familyId AND userId = :userId AND instanceId = :instanceId")
    suspend fun markCompleted(familyId: String, userId: String, instanceId: String, timestamp: Long)

    @Query("DELETE FROM task_instances WHERE userId = :userId AND assignedDate = :date")
    suspend fun deleteAllForUserAndDate(userId: String, date: String)

    @Query("DELETE FROM task_instances WHERE userId = :userId AND assignedDate = :date")
    suspend fun deleteAllForDate(userId: String, date: String)

    // Atomic delete + insert transaction
    @Transaction
    suspend fun deleteAndInsertForUserAndDate(
        userId: String,
        date: String,
        newEntities: List<TaskInstanceEntity>
    ) {
        deleteAllForUserAndDate(userId, date)
        insertAll(newEntities)
    }
}