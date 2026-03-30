package com.kidsroutine.feature.daily.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.common.util.DateUtils
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
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import com.google.firebase.firestore.QuerySnapshot

@Singleton
class DailyRepositoryImpl @Inject constructor(
    private val taskInstanceDao: TaskInstanceDao,
    private val taskProgressDao: TaskProgressDao,
    private val firestore: FirebaseFirestore,
    private val json: com.google.gson.Gson
) : DailyRepository {


    override fun observeDailyState(familyId: String, userId: String, date: String): Flow<DailyStateModel> {
        val assignmentsFlow = callbackFlow<QuerySnapshot> {
            val listener = firestore
                .collection("families").document(familyId)
                .collection("users").document(userId)
                .collection("assignments")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        trySend(snapshot)
                    }
                }
            awaitClose { listener.remove() }
        }

        val progressFlow = callbackFlow<QuerySnapshot> {
            val listener = firestore
                .collection("families").document(familyId)
                .collection("users").document(userId)
                .collection("task_progress")
                .whereEqualTo("date", date)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        trySend(snapshot)
                    }
                }
            awaitClose { listener.remove() }
        }

        return combine(assignmentsFlow, progressFlow) { assignmentSnapshot, progressSnapshot ->
            // Get completed task IDs for today
            val completedTaskIds = progressSnapshot.documents
                .mapNotNull { it.getString("templateId") }
                .toSet()

            Log.d("DailyRepository", "Completed today: $completedTaskIds")

            // ✅ Build PENDING tasks from assignments
            val pendingTasks = assignmentSnapshot.documents
                .mapNotNull { doc ->
                    val taskId = doc.getString("taskId") ?: return@mapNotNull null
                    val status = doc.getString("status") ?: return@mapNotNull null

                    // Skip if not ASSIGNED or already completed today
                    if (status != "ASSIGNED" || taskId in completedTaskIds) {
                        return@mapNotNull null
                    }

                    // ✅ Fetch full task from tasks collection
                    try {
                        val taskDoc = firestore
                            .collection("families").document(familyId)
                            .collection("tasks").document(taskId)
                            .get()
                            .await()

                        val taskModel = taskDoc.toObject(TaskModel::class.java)
                        if (taskModel != null) {
                            TaskInstance(
                                instanceId = "assign_${taskId}_${System.currentTimeMillis()}",
                                templateId = taskId,
                                task = taskModel,
                                assignedDate = date,
                                userId = userId,
                                status = TaskStatus.PENDING,
                                completedAt = 0L
                            )
                        } else null
                    } catch (e: Exception) {
                        Log.e("DailyRepository", "Error fetching task $taskId", e)
                        null
                    }
                }

            // ✅ Get completed count (DON'T create TaskInstances for display)
            val completedCount = progressSnapshot.documents.size

            // ✅ Calculate totals
            val totalTasksAssigned = pendingTasks.size + completedCount

            val totalXp = progressSnapshot.documents
                .sumOf { doc ->
                    (doc.getLong("xpGained") ?: 0L).toInt()
                }

            // ✅ Create fake TaskInstances for completed tasks (for pills only, not displayed as cards)
            val completedTasksForPills = progressSnapshot.documents.mapNotNull { doc ->
                val taskInstanceId = doc.getString("taskInstanceId") ?: return@mapNotNull null
                val taskTitle = doc.getString("taskTitle") ?: "Completed Task"
                val completedAt = doc.getLong("completionTime") ?: 0L

                TaskInstance(
                    instanceId = taskInstanceId,
                    templateId = doc.getString("templateId") ?: "",
                    task = TaskModel(title = taskTitle),
                    assignedDate = date,
                    userId = userId,
                    status = TaskStatus.COMPLETED,
                    completedAt = completedAt
                )
            }

            // ✅ Combine: completed FIRST, then pending
            val allTasksForPills = completedTasksForPills + pendingTasks

            DailyStateModel(
                date = date,
                userId = userId,
                tasks = allTasksForPills,  // ✅ All tasks for pills (completed show as filled, pending as empty)
                completedCount = completedCount,
                totalTasksAssigned = totalTasksAssigned,
                totalXpEarned = totalXp,
                isGenerated = false,
                generatedAt = 0L
            )
        }
    }

    override suspend fun deleteOldCompletedInstances(familyId: String, userId: String, today: String) {
        try {
            taskInstanceDao.deleteOlderThan(today)
            Log.d("DailyRepository", "✅ Deleted task instances older than $today")
        } catch (e: Exception) {
            Log.e("DailyRepository", "Error deleting old instances", e)
        }
    }

    override suspend fun replaceTasksForDate(familyId: String, userId: String, date: String, instances: List<TaskInstance>) {
        try {
            // Step 1: Delete all old Room records for this date
            taskInstanceDao.deleteAllForDate(userId, date)
            Log.d("DailyRepository", "✅ Deleted all old tasks for $date")

            // ✅ CRITICAL: Also delete progress records for this date
            // This prevents stale completed task counts
            try {
                taskProgressDao.deleteForDate(userId, date)
                Log.d("DailyRepository", "✅ Deleted task_progress for $date")
            } catch (e: Exception) {
                Log.e("DailyRepository", "⚠️ Error deleting task_progress", e)
            }

            kotlinx.coroutines.delay(100)

            // Step 2: Insert fresh instances from Firestore
            val entities = instances.map { instance ->
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

            taskInstanceDao.insertAll(entities)
            Log.d("DailyRepository", "✅ Inserted ${entities.size} fresh tasks from Firestore for $date")
        } catch (e: Exception) {
            Log.e("DailyRepository", "Error replacing tasks for date", e)
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

    override suspend fun updateTaskInRoom(familyId: String, userId: String, instanceId: String, updatedTask: TaskModel) {
        try {
            val entity = TaskInstanceEntity(
                instanceId = instanceId,
                templateId = "", // dummy
                taskJson = json.toJson(updatedTask),
                assignedDate = DateUtils.todayString(),
                userId = userId,
                familyId = familyId,
                injectedByChallengeId = null,
                status = "PENDING",
                completedAt = 0L
            )
            taskInstanceDao.insertAll(listOf(entity))
            Log.d("DailyRepository", "✅ Task updated in Room: ${updatedTask.title}")
        } catch (e: Exception) {
            Log.e("DailyRepository", "Error updating task in Room", e)
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

            // ✅ CRITICAL FIX: Delete all existing task_instances for this user/date FIRST, then insert new ones atomically
            // This prevents duplicates when mergeAssignedTasks is called multiple times
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