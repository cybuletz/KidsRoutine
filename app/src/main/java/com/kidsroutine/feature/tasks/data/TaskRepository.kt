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
            firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .document(task.id)
                .set(task)
                .await()
            Log.d("TaskRepository", "Task created successfully")
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

            // Delete from global /tasks collection
            val globalTaskRef = firestore.collection("tasks").document(taskId)
            batch.delete(globalTaskRef)

            // Also delete from family subcollection
            val familyTaskRef = firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .document(taskId)
            batch.delete(familyTaskRef)

            // ✨ FIX: Query ALL assignments with just taskId (no familyId filter)
            val assignments = firestore
                .collection("taskAssignments")
                .whereEqualTo("taskId", taskId)  // ← ONLY filter by taskId
                .get()
                .await()

            Log.d("TaskRepository", "Found ${assignments.size()} assignments to delete for taskId=$taskId")

            for (doc in assignments.documents) {
                Log.d("TaskRepository", "Deleting assignment: ${doc.id}")
                batch.delete(doc.reference)  // ← This triggers notifyTaskDeletion Cloud Function
            }

            batch.commit().await()
            Log.d("TaskRepository", "✅ Task and ${assignments.size()} assignments deleted successfully")
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error deleting task", e)
            throw e
        }
    }
}
