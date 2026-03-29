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
import kotlinx.coroutines.flow.firstOrNull
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


    // ✅ NEW: requires familyId for family-scoped queries
    override fun observeDailyState(familyId: String, userId: String, date: String): Flow<DailyStateModel> {
        val tasksFlow    = taskInstanceDao.getTasksForDate(familyId, userId, date)         // ← PENDING only
        val allTasksFlow = taskInstanceDao.getAllTasksForDate(familyId, userId, date)      // ← ALL for progress
        val progressFlow = taskProgressDao.getProgressForDate(familyId, userId, date)
            .onStart { emit(emptyList()) }

        return combine(tasksFlow, allTasksFlow, progressFlow) { pendingEntities, allEntities, progressEntities ->
            // ✅ Create PENDING instances for display (progress pills only show pending tasks)
            val instances = pendingEntities.map { entity ->
                val taskModel = json.fromJson(entity.taskJson, TaskModel::class.java)
                TaskInstance(
                    instanceId            = entity.instanceId,
                    templateId            = entity.templateId,
                    task                  = taskModel,
                    assignedDate          = entity.assignedDate,
                    userId                = entity.userId,
                    injectedByChallengeId = entity.injectedByChallengeId,
                    status                = TaskStatus.PENDING,
                    completedAt           = 0L
                )
            }

            // ✅ CRITICAL: Count completed from progressEntities (child's actual accomplishments)
            val completedCount = progressEntities.count { it.status == "COMPLETED" }

            // ✅ CRITICAL: Total assigned = pending tasks that exist + tasks completed today
            val totalTasksAssigned = pendingEntities.size + completedCount

            val totalXp = progressEntities
                .filter { it.status == "COMPLETED" }
                .sumOf { p ->
                    val task = allEntities.find { it.instanceId == p.taskInstanceId }
                        ?.let { json.fromJson(it.taskJson, TaskModel::class.java) }
                    task?.reward?.xp ?: 0
                }

            DailyStateModel(
                date                = date,
                userId              = userId,
                tasks               = instances,
                completedCount      = completedCount,
                totalTasksAssigned  = totalTasksAssigned,  // ✅ ADD THIS
                totalXpEarned       = totalXp,
                isGenerated         = allEntities.isNotEmpty()
            )
        }
    }

    override suspend fun refreshTasksForDate(familyId: String, userId: String, date: String) {
        try {
            Log.d("DailyRepository", "Refreshing tasks for family=$familyId, user=$userId, date=$date")

            // ✅ NEW PATH: /families/{familyId}/users/{userId}/assignments/
            val assignmentsSnapshot = firestore
                .collection("families")
                .document(familyId)
                .collection("users")
                .document(userId)
                .collection("assignments")
                .whereEqualTo("status", "ASSIGNED")
                .get()
                .await()

            val taskIds = assignmentsSnapshot.documents.mapNotNull { it.getString("taskId") }
            val refreshedTasks = mutableListOf<TaskInstance>()

            for (taskId in taskIds) {
                try {
                    // Fetch from family-scoped path
                    val taskDoc = firestore
                        .collection("families").document(familyId)
                        .collection("tasks").document(taskId)
                        .get().await()

                    val taskModel = taskDoc.toObject(TaskModel::class.java)
                    if (taskModel != null && taskModel.title.isNotBlank()) {
                        refreshedTasks.add(
                            TaskInstance(
                                instanceId = "refresh_${taskId}_${System.currentTimeMillis()}",
                                templateId = taskId,
                                task = taskModel,
                                assignedDate = date,
                                userId = userId,
                                injectedByChallengeId = null,
                                status = TaskStatus.PENDING,
                                completedAt = 0L
                            )
                        )
                        Log.d("DailyRepository", "Refreshed task: ${taskModel.title}")
                    }
                } catch (e: Exception) {
                    Log.e("DailyRepository", "Error refreshing task $taskId", e)
                }
            }

            if (refreshedTasks.isNotEmpty()) {
                mergeAssignedTasks(familyId, userId, date, refreshedTasks)
            }
        } catch (e: Exception) {
            Log.e("DailyRepository", "Error refreshing tasks for date", e)
        }
    }

    override suspend fun mergeAssignedTasks(
        familyId: String,
        userId: String,
        date: String,
        newInstances: List<TaskInstance>
    ) {
        try {
            val entitiesToInsert = newInstances.map { instance ->
                TaskInstanceEntity(
                    instanceId = instance.instanceId,
                    templateId = instance.templateId,
                    taskJson = json.toJson(instance.task),
                    assignedDate = instance.assignedDate,
                    userId = userId,
                    familyId = familyId,
                    injectedByChallengeId = instance.injectedByChallengeId,
                    status = instance.status.name,
                    completedAt = instance.completedAt
                )
            }

            // ✅ CHANGED: Use atomic transaction instead of separate delete + insert
            taskInstanceDao.deleteAndInsertForUserAndDate(userId, date, entitiesToInsert)

            Log.d("DailyRepository", "✅ Merged ${newInstances.size} new assigned tasks for family=$familyId, user=$userId")
        } catch (e: Exception) {
            Log.e("DailyRepository", "Error merging assigned tasks", e)
        }
    }

    // ✨ NEW: Delete a task instance
    override suspend fun deleteTaskInstance(familyId: String, userId: String, instanceId: String) {
        try {
            Log.d("DailyRepository", "Deleting task instance: $instanceId for user: $userId in family: $familyId")
            taskInstanceDao.deleteByInstanceId(familyId, userId, instanceId)
            Log.d("DailyRepository", "✅ Task instance deleted: $instanceId")
        } catch (e: Exception) {
            Log.e("DailyRepository", "❌ Error deleting task instance: ${e.message}", e)
        }
    }

    // ✅ NEW: requires familyId for family-scoped saves
    override suspend fun saveDailyTasks(familyId: String, userId: String, date: String, tasks: List<TaskInstance>) {
        try {
            Log.d("DailyRepository", "Saving ${tasks.size} tasks for userId=$userId in family=$familyId on $date")

            val batch = firestore.batch()

            // ✅ CHANGED: Family-scoped path
            val userRef = firestore
                .collection("families").document(familyId)
                .collection("users").document(userId)

            // ✅ Save to Firestore
            for (instance in tasks) {
                val ref = userRef.collection("task_instances").document(instance.instanceId)
                batch.set(ref, mapOf(
                    "instanceId"   to instance.instanceId,
                    "templateId"   to instance.templateId,
                    "taskId"       to instance.task.id,
                    "taskType"     to instance.task.type.name,
                    "title"        to instance.task.title,
                    "date"         to instance.assignedDate,
                    "familyId"     to familyId,
                    "userId"       to userId,
                    "requiresCoop" to instance.task.requiresCoop,
                    "xp"           to instance.task.reward.xp,
                    "injectedByChallengeId" to instance.injectedByChallengeId,
                    "status"       to instance.status.name,
                    "completedAt"  to instance.completedAt
                ))
            }
            batch.commit().await()

            // ✅ Save to Room local DB
            val entitiesToInsert = tasks.map { instance ->
                TaskInstanceEntity(
                    instanceId            = instance.instanceId,
                    templateId            = instance.templateId,
                    taskJson              = json.toJson(instance.task),
                    assignedDate          = instance.assignedDate,
                    userId                = userId,
                    familyId              = familyId,
                    injectedByChallengeId = instance.injectedByChallengeId,
                    status                = instance.status.name,
                    completedAt           = instance.completedAt
                )
            }
            taskInstanceDao.insertAll(entitiesToInsert)

            Log.d("DailyRepository", "✅ Daily tasks saved to Firestore and Room for family=$familyId, user=$userId")
        } catch (e: Exception) {
            Log.e("DailyRepository", "❌ Failed to save daily tasks", e)
            throw e
        }
    }

    // ✅ NEW: requires familyId
    override suspend fun hasTasksForDate(familyId: String, userId: String, date: String): Boolean {
        return taskInstanceDao.countTasksForDate(familyId, userId, date) > 0
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
                    .collection("families")  // ✅ FAMILY-SCOPED
                    .document(familyId)
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