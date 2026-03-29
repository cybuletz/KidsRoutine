package com.kidsroutine.core.database.dao

import androidx.room.*
import com.kidsroutine.core.database.entity.TaskProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskProgressDao {

    @Query("SELECT * FROM task_progress WHERE familyId = :familyId AND userId = :userId AND date = :date")
    fun getProgressForDate(familyId: String, userId: String, date: String): Flow<List<TaskProgressEntity>>

    @Query("SELECT * FROM task_progress WHERE syncedToFirestore = 0")
    suspend fun getUnsynced(): List<TaskProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: TaskProgressEntity)

    @Query("UPDATE task_progress SET syncedToFirestore = 1 WHERE taskInstanceId = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT * FROM task_progress WHERE taskInstanceId = :id")
    suspend fun getById(id: String): TaskProgressEntity?
}