package com.kidsroutine.feature.daily.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.database.dao.TaskInstanceDao
import com.kidsroutine.core.database.dao.TaskProgressDao
import com.kidsroutine.core.database.entity.TaskInstanceEntity
import com.kidsroutine.core.model.*
import com.kidsroutine.core.network.safeFirestoreCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.onStart


@Singleton
class DailyRepositoryImpl @Inject constructor(
    private val taskInstanceDao: TaskInstanceDao,
    private val taskProgressDao: TaskProgressDao,
    private val firestore: FirebaseFirestore,
    private val json: com.google.gson.Gson
) : DailyRepository {

    override fun observeDailyState(userId: String, date: String): Flow<DailyStateModel> {
        val tasksFlow    = taskInstanceDao.getTasksForDate(userId, date)
        val progressFlow = taskProgressDao.getProgressForDate(userId, date)
            .onStart { emit(emptyList()) }   // ← guarantee at least one emission so combine() doesn't stall

        return combine(tasksFlow, progressFlow) { taskEntities, progressEntities ->
            val instances = taskEntities.map { entity ->
                val taskModel = json.fromJson(entity.taskJson, TaskModel::class.java)
                TaskInstance(
                    instanceId            = entity.instanceId,
                    templateId            = entity.templateId,
                    task                  = taskModel,
                    assignedDate          = entity.assignedDate,
                    userId                = entity.userId,
                    injectedByChallengeId = entity.injectedByChallengeId,
                    status                = runCatching { TaskStatus.valueOf(entity.status) }.getOrDefault(TaskStatus.PENDING),
                    completedAt           = entity.completedAt
                )
            }
            val completedCount = progressEntities.count { it.status == "COMPLETED" }
            val totalXp = progressEntities
                .filter { it.status == "COMPLETED" }
                .sumOf { p ->
                    val task = taskEntities.find { it.instanceId == p.taskInstanceId }
                        ?.let { json.fromJson(it.taskJson, TaskModel::class.java) }
                    task?.reward?.xp ?: 0
                }
            DailyStateModel(
                date           = date,
                userId         = userId,
                tasks          = instances,
                completedCount = completedCount,
                totalXpEarned  = totalXp,
                isGenerated    = taskEntities.isNotEmpty()
            )
        }
    }

    override suspend fun saveDailyTasks(userId: String, date: String, tasks: List<TaskInstance>) {
        // DELETE existing tasks for this date first
        taskInstanceDao.deleteTasksForDate(userId, date)  // ← ADD THIS

        val entities = tasks.map { instance ->
            TaskInstanceEntity(
                instanceId   = instance.instanceId,
                templateId   = instance.templateId,
                taskJson     = json.toJson(instance.task),
                assignedDate = instance.assignedDate,
                userId       = instance.userId,
                injectedByChallengeId = instance.injectedByChallengeId
            )
        }
        taskInstanceDao.insertAll(entities)
        // Also push to Firestore for cross-device sync
        safeFirestoreCall {
            val batch = firestore.batch()
            tasks.forEach { instance ->
                val ref = firestore
                    .collection("users").document(userId)
                    .collection("daily_tasks").document(instance.instanceId)
                batch.set(ref, mapOf(
                    "instanceId"   to instance.instanceId,
                    "templateId"   to instance.templateId,
                    "taskId"       to instance.task.id,
                    "taskType"     to instance.task.type.name,
                    "title"        to instance.task.title,
                    "date"         to instance.assignedDate,
                    "requiresCoop" to instance.task.requiresCoop,
                    "xp"           to instance.task.reward.xp,
                    "injectedByChallengeId" to instance.injectedByChallengeId
                ))
            }
            batch.commit().await()
        }
    }

    override suspend fun hasTasksForDate(userId: String, date: String): Boolean {
        // Always return false to allow regeneration
        // This ensures parent-assigned tasks are picked up
        return false
    }

    override suspend fun fetchTaskTemplatesFromFirestore(familyId: String): List<TaskTemplate> {
        Log.d("DailyRepository", "fetchTaskTemplatesFromFirestore called with familyId=$familyId")
        return try {
            val systemTemplates = firestore.collection("task_templates")
                .whereEqualTo("isActive", true)
                .get().await()

            Log.d("DailyRepository", "Found ${systemTemplates.documents.size} documents")

            systemTemplates.documents.forEach { doc ->
                Log.d("DailyRepository", "Document ID: ${doc.id}, Data: ${doc.data}")
            }

            systemTemplates.documents.mapNotNull { doc ->
                try {
                    doc.toObject(TaskTemplate::class.java)
                } catch (e: Exception) {
                    Log.e("DailyRepository", "Failed to deserialize doc ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("DailyRepository", "Exception in fetchTaskTemplatesFromFirestore", e)
            emptyList()
        }
    }

    override suspend fun getAssignedTasks(userId: String, familyId: String): List<TaskModel> {
        return try {
            val assignmentsSnapshot = firestore
                .collection("taskAssignments")
                .whereEqualTo("childId", userId)
                .whereEqualTo("status", "ASSIGNED")
                .get()
                .await()

            val taskIds = assignmentsSnapshot.documents.mapNotNull {
                it.getString("taskId")
            }

            taskIds.mapNotNull { taskId ->
                firestore
                    .collection("tasks")
                    .document(taskId)
                    .get()
                    .await()
                    .toObject(TaskModel::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
