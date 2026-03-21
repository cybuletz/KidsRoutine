package com.kidsroutine.core.database.dao

import androidx.room.*
import com.kidsroutine.core.database.entity.TaskInstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskInstanceDao {

    @Query("SELECT * FROM task_instances WHERE userId = :userId AND assignedDate = :date")
    fun getTasksForDate(userId: String, date: String): Flow<List<TaskInstanceEntity>>

    @Query("SELECT COUNT(*) FROM task_instances WHERE userId = :userId AND assignedDate = :date")
    suspend fun countTasksForDate(userId: String, date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskInstanceEntity>)

    @Query("DELETE FROM task_instances WHERE assignedDate < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)

    @Query("DELETE FROM task_instances WHERE userId = :userId AND assignedDate = :date")
    suspend fun deleteTasksForDate(userId: String, date: String)
}
