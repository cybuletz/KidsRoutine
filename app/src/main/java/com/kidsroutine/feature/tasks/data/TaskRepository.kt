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

            // ✅ SINGLE WRITE: Family-scoped ONLY
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
            val batch = firestore.batch()

            // ✅ SINGLE UPDATE: Family-scoped ONLY
            val familyTaskRef = firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .document(task.id)
            batch.set(familyTaskRef, task)

            // Touch all assignments to trigger client refresh
            val assignments = firestore
                .collection("taskAssignments")
                .whereEqualTo("taskId", task.id)
                .get()
                .await()

            Log.d("TaskRepository", "Found ${assignments.size()} assignments for task ${task.id}")

            for (doc in assignments.documents) {
                batch.update(doc.reference, mapOf(
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ))
            }

            batch.commit().await()
            Log.d("TaskRepository", "✅ Task updated successfully + ${assignments.size()} assignments touched")
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error updating task", e)
            throw e
        }
    }

    suspend fun deleteTask(familyId: String, taskId: String) {
        try {
            Log.d("TaskRepository", "Deleting task: $taskId")
            val batch = firestore.batch()

            // ✅ DELETE ONLY from family-scoped (not global)
            val familyTaskRef = firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .document(taskId)
            batch.delete(familyTaskRef)

            // Delete all assignments
            val assignments = firestore
                .collection("taskAssignments")
                .whereEqualTo("taskId", taskId)
                .get()
                .await()

            Log.d("TaskRepository", "Found ${assignments.size()} assignments for taskId=$taskId")

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