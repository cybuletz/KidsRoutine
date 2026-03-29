package com.kidsroutine.feature.generation.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.TaskCategory
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.TaskReward
import com.kidsroutine.core.model.TaskType
import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.TaskCreator
import com.kidsroutine.core.model.GameType
import com.kidsroutine.core.model.ValidationType
import com.kidsroutine.core.common.util.DateUtils
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for saving AI-generated tasks and challenges
 */
@Singleton
class TaskSaveRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /**
     * Convert GeneratedTask to TaskModel and save to Firestore
     */
    suspend fun saveGeneratedTask(
        generatedTask: com.kidsroutine.feature.generation.data.GeneratedTask,
        familyId: String,
        createdBy: String = "PARENT_AI"
    ): Result<String> {
        return try {
            Log.d("TaskSaveRepository", "Saving generated task: ${generatedTask.title}")

            // Convert category string to enum
            val category = try {
                TaskCategory.valueOf(generatedTask.category)
            } catch (e: Exception) {
                TaskCategory.LEARNING
            }

            // Convert difficulty string to enum
            val difficulty = try {
                DifficultyLevel.valueOf(generatedTask.difficulty)
            } catch (e: Exception) {
                DifficultyLevel.EASY
            }

            // Convert type string to enum
            val taskType = try {
                TaskType.valueOf(generatedTask.type)
            } catch (e: Exception) {
                TaskType.REAL_LIFE
            }

            // Create TaskModel
            val taskModel = TaskModel(
                id = "task_${System.currentTimeMillis()}_${(Math.random() * 10000).toInt()}",
                type = taskType,
                title = generatedTask.title,
                description = generatedTask.description,
                category = category,
                difficulty = difficulty,
                estimatedDurationSec = generatedTask.estimatedDurationSec,
                gameType = GameType.NONE,
                validationType = ValidationType.SELF,
                reward = TaskReward(xp = generatedTask.xpReward),
                requiresParent = false,
                requiresCoop = false,
                createdBy = TaskCreator.AI_GENERATED,  // ✅ All AI-generated tasks use this
                familyId = familyId,
                isActive = true
            )

            // Save to Firestore
            firestore.collection("taskTemplates")
                .document(taskModel.id)
                .set(taskModel)
                .await()

            Log.d("TaskSaveRepository", "Task saved successfully: ${taskModel.id}")
            Result.success(taskModel.id)
        } catch (e: Exception) {
            Log.e("TaskSaveRepository", "Error saving task: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Add task instance for a specific child today
     */
    suspend fun assignTaskToChild(
        taskId: String,
        childId: String,
        familyId: String
    ): Result<String> {
        return try {
            Log.d("TaskSaveRepository", "Assigning task $taskId to child $childId")

            val today = DateUtils.todayString()

            // Create task instance
            val taskInstance = mapOf(
                "instanceId" to "instance_${System.currentTimeMillis()}_${(Math.random() * 10000).toInt()}",
                "templateId" to taskId,
                "userId" to childId,
                "familyId" to familyId,
                "assignedDate" to today,
                "status" to "PENDING",
                "createdAt" to System.currentTimeMillis()
            )

            val instanceId = taskInstance["instanceId"] as String

            // Save to Firestore
            firestore.collection("taskInstances")
                .document(instanceId)
                .set(taskInstance)
                .await()

            Log.d("TaskSaveRepository", "Task instance created: $instanceId")
            Result.success(instanceId)
        } catch (e: Exception) {
            Log.e("TaskSaveRepository", "Error assigning task: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Save generated task and assign to all children in family
     */
    suspend fun saveAndAssignToFamily(
        generatedTask: com.kidsroutine.feature.generation.data.GeneratedTask,
        familyId: String,
        childrenIds: List<String>
    ): Result<String> {
        return try {
            // First, save the task template
            val taskIdResult = saveGeneratedTask(generatedTask, familyId, "PARENT_AI")
            if (taskIdResult.isFailure) {
                return Result.failure(taskIdResult.exceptionOrNull() ?: Exception("Failed to save task"))
            }

            val taskId = taskIdResult.getOrNull() ?: return Result.failure(Exception("No task ID"))

            // Then assign to each child
            childrenIds.forEach { childId ->
                assignTaskToChild(taskId, childId, familyId)
            }

            Log.d("TaskSaveRepository", "Task saved and assigned to ${childrenIds.size} children")
            Result.success(taskId)
        } catch (e: Exception) {
            Log.e("TaskSaveRepository", "Error in saveAndAssignToFamily: ${e.message}", e)
            Result.failure(e)
        }
    }
}