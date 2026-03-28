package com.kidsroutine.feature.tasks.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.TaskModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.kidsroutine.feature.generation.data.GeneratedChallenge

class TaskRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getFamilyTasks(familyId: String): List<TaskModel> {
        return try {
            Log.d("TaskRepository", "Fetching tasks for family: $familyId")
            val snapshot = firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .get()
                .await()

            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(TaskModel::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e("TaskRepository", "Error parsing task", e)
                    null
                }
            }
            Log.d("TaskRepository", "Fetched ${tasks.size} tasks")
            tasks
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error fetching tasks", e)
            throw e
        }
    }

    suspend fun createTask(familyId: String, task: TaskModel) {
        try {
            Log.d("TaskRepository", "Creating task: ${task.title}")

            // Save to BOTH locations so it can be found when deleted
            // 1. Family subcollection (for family-level task management)
            firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .document(task.id)
                .set(task)
                .await()

            // 2. Global tasks collection (so assignments can reference it and deletion works)
            firestore
                .collection("tasks")
                .document(task.id)
                .set(mapOf(
                    "id" to task.id,
                    "type" to task.type.name,
                    "title" to task.title,
                    "description" to task.description,
                    "category" to task.category.name,
                    "difficulty" to task.difficulty.name,
                    "estimatedDurationSec" to task.estimatedDurationSec,
                    "reward" to mapOf("xp" to task.reward.xp),
                    "validationType" to task.validationType.name,
                    "requiresParent" to false,
                    "requiresCoop" to task.requiresCoop,
                    "tags" to task.tags,
                    "createdBy" to task.createdBy.name,
                    "interactionBlocks" to emptyList<Map<String, Any>>(),
                    "isActive" to task.isActive,
                    "familyId" to familyId,
                    "gameType" to task.gameType.name,
                ))
                .await()

            Log.d("TaskRepository", "Task created successfully in both locations")
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error creating task", e)
            throw e
        }
    }

    suspend fun updateTask(familyId: String, task: TaskModel) {
        try {
            Log.d("TaskRepository", "Updating task: ${task.id}")
            firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .document(task.id)
                .set(task)
                .await()
            Log.d("TaskRepository", "Task updated successfully")
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error updating task", e)
            throw e
        }
    }

    suspend fun deleteTask(familyId: String, taskId: String) {
        try {
            Log.d("TaskRepository", "Deleting task: $taskId and all its assignments")
            val batch = firestore.batch()

            // Delete from BOTH locations (for both creation paths)
            // Path 1: Global tasks collection (SelectChildrenScreen)
            val globalTaskRef = firestore.collection("tasks").document(taskId)
            batch.delete(globalTaskRef)

            // Path 2: Family subcollection (CreateTaskScreen via TaskRepository)
            val familyTaskRef = firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .document(taskId)
            batch.delete(familyTaskRef)

            // Find ALL assignments by taskId (no familyId filter needed)
            val assignments = firestore
                .collection("taskAssignments")
                .whereEqualTo("taskId", taskId)
                .get()
                .await()

            Log.d("TaskRepository", "Found ${assignments.size()} assignments for taskId=$taskId")

            // Delete each assignment (triggers notifyTaskDeletion Cloud Function)
            for (doc in assignments.documents) {
                Log.d("TaskRepository", "Deleting assignment: ${doc.id}")
                batch.delete(doc.reference)
            }

            batch.commit().await()
            Log.d("TaskRepository", "✅ Task and ${assignments.size()} assignments deleted successfully")
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error deleting task", e)
            throw e
        }
    }
}
