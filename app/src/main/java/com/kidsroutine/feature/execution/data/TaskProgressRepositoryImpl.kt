package com.kidsroutine.feature.execution.data

import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.database.dao.TaskProgressDao
import com.kidsroutine.core.database.entity.TaskProgressEntity
import com.kidsroutine.core.model.*
import com.kidsroutine.core.network.safeFirestoreCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskProgressRepositoryImpl @Inject constructor(
    private val dao: TaskProgressDao,
    private val firestore: FirebaseFirestore
) : TaskProgressRepository {

    override suspend fun saveProgress(progress: TaskProgressModel) {
        dao.upsert(progress.toEntity())
    }

    override suspend fun getProgress(taskInstanceId: String): TaskProgressModel? =
        dao.getById(taskInstanceId)?.toModel()

    override fun observeProgress(userId: String, date: String): Flow<List<TaskProgressModel>> =
        dao.getProgressForDate(userId, date).map { list -> list.map { it.toModel() } }

    override suspend fun syncPendingToFirestore() {
        val unsynced = dao.getUnsynced()
        unsynced.forEach { entity ->
            safeFirestoreCall {
                firestore.collection("task_progress")
                    .document(entity.taskInstanceId)
                    .set(entity.toMap())
                    .await()
                dao.markSynced(entity.taskInstanceId)
            }
        }
    }

    private fun TaskProgressModel.toEntity() = TaskProgressEntity(
        taskInstanceId   = taskInstanceId,
        userId           = userId,
        date             = date,
        status           = status.name,
        completionTime   = completionTime,
        validationStatus = validationStatus.name,
        photoUrl         = photoUrl,
        syncedToFirestore = syncedToFirestore
    )

    private fun TaskProgressEntity.toModel() = TaskProgressModel(
        taskInstanceId   = taskInstanceId,
        userId           = userId,
        date             = date,
        status           = TaskStatus.valueOf(status),
        completionTime   = completionTime,
        validationStatus = ValidationStatus.valueOf(validationStatus),
        photoUrl         = photoUrl,
        syncedToFirestore = syncedToFirestore
    )

    private fun TaskProgressEntity.toMap() = mapOf(
        "taskInstanceId"   to taskInstanceId,
        "userId"           to userId,
        "date"             to date,
        "status"           to status,
        "completionTime"   to completionTime,
        "validationStatus" to validationStatus,
        "photoUrl"         to photoUrl
    )
}
