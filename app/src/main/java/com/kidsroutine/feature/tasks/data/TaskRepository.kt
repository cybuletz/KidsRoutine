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

            // ✅ UPDATE family-scoped task
            val familyTaskRef = firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .document(task.id)
            batch.set(familyTaskRef, task)

            // ✅ Get ALL users in this family
            val usersSnapshot = firestore
                .collection("families")
                .document(familyId)
                .collection("users")
                .get()
                .await()

            Log.d("TaskRepository", "Found ${usersSnapshot.size()} users in family")

            var assignmentsTouched = 0

            // ✅ For each user, find and touch their assignments for this task
            for (userDoc in usersSnapshot.documents) {
                val userId = userDoc.id

                val assignmentsSnapshot = firestore
                    .collection("families")
                    .document(familyId)
                    .collection("users")
                    .document(userId)
                    .collection("assignments")
                    .whereEqualTo("taskId", task.id)
                    .get()
                    .await()

                Log.d("TaskRepository", "User $userId has ${assignmentsSnapshot.size()} assignments for this task")

                for (assignmentDoc in assignmentsSnapshot.documents) {
                    batch.update(
                        assignmentDoc.reference,
                        mapOf("updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp())
                    )
                    assignmentsTouched++
                }
            }

            batch.commit().await()
            Log.d("TaskRepository", "✅ Task updated successfully + $assignmentsTouched assignments touched")
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error updating task", e)
            throw e
        }
    }

    suspend fun deleteTask(familyId: String, taskId: String) {
        try {
            Log.d("TaskRepository", "Deleting task: $taskId")
            val batch = firestore.batch()

            // ✅ DELETE from family-scoped tasks
            val familyTaskRef = firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .document(taskId)
            batch.delete(familyTaskRef)

            // ✅ Delete all assignments across all users
            // NEW PATH: /families/{familyId}/users/{userId}/assignments/
            var assignmentsDeleted = 0

            // Get all users in family
            val usersSnapshot = firestore
                .collection("families")
                .document(familyId)
                .collection("users")
                .get()
                .await()

            // For each user, delete their assignments that reference this task
            for (userDoc in usersSnapshot.documents) {
                val userId = userDoc.id
                val assignmentsSnapshot = firestore
                    .collection("families")
                    .document(familyId)
                    .collection("users")
                    .document(userId)
                    .collection("assignments")
                    .whereEqualTo("taskId", taskId)
                    .get()
                    .await()

                for (assignmentDoc in assignmentsSnapshot.documents) {
                    batch.delete(assignmentDoc.reference)
                    assignmentsDeleted++
                    Log.d("TaskRepository", "Deleting assignment: ${assignmentDoc.id}")
                }
            }

            batch.commit().await()
            Log.d("TaskRepository", "✅ Task deleted: $taskId + $assignmentsDeleted assignments deleted")
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error deleting task", e)
            throw e
        }
    }
}