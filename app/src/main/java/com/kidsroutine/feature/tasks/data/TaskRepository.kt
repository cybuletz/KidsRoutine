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

            // 1. Delete the task document itself
            val taskRef = firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .document(taskId)
            batch.delete(taskRef)

            // 2. Find and delete all taskAssignments for this task
            val assignments = firestore
                .collection("taskAssignments")
                .whereEqualTo("taskId", taskId)
                .whereEqualTo("familyId", familyId)
                .get()
                .await()

            for (doc in assignments.documents) {
                batch.delete(doc.reference)
            }

            // 3. Commit everything atomically
            batch.commit().await()
            Log.d("TaskRepository", "Task and ${assignments.size()} assignments deleted successfully")
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

    // ════════════════════════════════════════════════════════════════════════
    // REAL-TIME LISTENER FOR CHILD'S ASSIGNED TASKS ← NEW!
    // ════════════════════════════════════════════════════════════════════════
    fun observeChildAssignedTasks(childId: String, familyId: String): Flow<List<TaskModel>> =
        callbackFlow {
            Log.d("TaskRepository", "Starting real-time listener for child: $childId")

            val listener = firestore.collection("taskAssignments")
                .whereEqualTo("childId", childId)
                .whereEqualTo("familyId", familyId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("TaskRepository", "Listener error: ${error.message}")
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        try {
                            val taskIds = snapshot.documents.mapNotNull { doc ->
                                doc.getString("taskId")
                            }

                            Log.d("TaskRepository", "Got ${taskIds.size} task IDs for child")

                            if (taskIds.isEmpty()) {
                                trySend(emptyList()).isSuccess
                                return@addSnapshotListener
                            }

                            // Fetch actual task documents from root "tasks" collection
                            firestore.collection("tasks")
                                .whereIn("id", taskIds)
                                .addSnapshotListener { taskSnapshot, taskError ->
                                    if (taskError != null) {
                                        Log.e("TaskRepository", "Task fetch error: ${taskError.message}")
                                        return@addSnapshotListener
                                    }

                                    if (taskSnapshot != null) {
                                        try {
                                            val tasks = taskSnapshot.documents.mapNotNull { doc ->
                                                try {
                                                    doc.toObject(TaskModel::class.java)?.copy(id = doc.id)
                                                } catch (e: Exception) {
                                                    Log.w("TaskRepository", "Error parsing task", e)
                                                    null
                                                }
                                            }
                                            Log.d("TaskRepository", "Emitting ${tasks.size} tasks")
                                            trySend(tasks).isSuccess
                                        } catch (e: Exception) {
                                            Log.e("TaskRepository", "Error processing tasks: ${e.message}")
                                        }
                                    }
                                }
                        } catch (e: Exception) {
                            Log.e("TaskRepository", "Error processing assignments: ${e.message}")
                        }
                    }
                }

            awaitClose {
                Log.d("TaskRepository", "Closing listener")
                listener.remove()
            }
        }

    // ════════════════════════════════════════════════════════════════════════
    // REAL-TIME LISTENER FOR CHILD'S ASSIGNED CHALLENGES ← NEW!
    // ════════════════════════════════════════════════════════════════════════
    fun observeChildAssignedChallenges(childId: String, familyId: String): Flow<List<com.kidsroutine.feature.generation.data.GeneratedChallenge>> =
        callbackFlow {
            Log.d("TaskRepository", "Starting real-time listener for child challenges: $childId")

            val listener = firestore.collection("challengeAssignments")
                .whereEqualTo("childId", childId)
                .whereEqualTo("familyId", familyId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("TaskRepository", "Challenge listener error: ${error.message}")
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        try {
                            val challengeIds = snapshot.documents.mapNotNull { doc ->
                                doc.getString("challengeId")
                            }

                            Log.d("TaskRepository", "Got ${challengeIds.size} challenge IDs for child")

                            if (challengeIds.isEmpty()) {
                                trySend(emptyList()).isSuccess
                                return@addSnapshotListener
                            }

                            // Fetch actual challenge documents from root "challenges" collection
                            firestore.collection("challenges")
                                .whereIn("id", challengeIds)
                                .addSnapshotListener { challengeSnapshot, challengeError ->
                                    if (challengeError != null) {
                                        Log.e("TaskRepository", "Challenge fetch error: ${challengeError.message}")
                                        return@addSnapshotListener
                                    }

                                    if (challengeSnapshot != null) {
                                        try {
                                            val challenges = challengeSnapshot.documents.mapNotNull { doc ->
                                                try {
                                                    val challenge = doc.toObject(com.kidsroutine.feature.generation.data.GeneratedChallenge::class.java)
                                                    // Don't use .copy(id = doc.id), just return the challenge as-is
                                                    challenge
                                                } catch (e: Exception) {
                                                    Log.w("TaskRepository", "Error parsing challenge", e)
                                                    null
                                                }
                                            }
                                            Log.d("TaskRepository", "Emitting ${challenges.size} challenges")
                                            trySend(challenges).isSuccess
                                        } catch (e: Exception) {
                                            Log.e("TaskRepository", "Error processing challenges: ${e.message}")
                                        }
                                    }
                                }
                        } catch (e: Exception) {
                            Log.e("TaskRepository", "Error processing challenge assignments: ${e.message}")
                        }
                    }
                }

            awaitClose {
                Log.d("TaskRepository", "Closing challenge listener")
                listener.remove()
            }
        }
}