package com.kidsroutine.feature.tasks.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.TaskModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

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
            Log.d("TaskRepository", "Deleting task: $taskId")
            firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .document(taskId)
                .delete()
                .await()
            Log.d("TaskRepository", "Task deleted successfully")
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error deleting task", e)
            throw e
        }
    }

    suspend fun getTaskById(familyId: String, taskId: String): TaskModel? {
        return try {
            Log.d("TaskRepository", "Fetching task: $taskId")
            val doc = firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .document(taskId)
                .get()
                .await()

            doc.toObject(TaskModel::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error fetching task", e)
            null
        }
    }
}