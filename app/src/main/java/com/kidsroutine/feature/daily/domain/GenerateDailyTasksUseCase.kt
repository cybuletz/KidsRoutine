package com.kidsroutine.feature.daily.domain

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.engine.task_engine.GenerationContext
import com.kidsroutine.core.engine.task_engine.TaskEngine
import com.kidsroutine.core.engine.challenge_engine.ChallengeEngine
import com.kidsroutine.core.model.*
import com.kidsroutine.core.common.util.DateUtils
import com.kidsroutine.feature.daily.data.DailyRepository
import com.kidsroutine.feature.challenges.data.ChallengeRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.kidsroutine.feature.daily.data.StoryArcRepository

class GenerateDailyTasksUseCase @Inject constructor(
    private val taskEngine: TaskEngine,
    private val challengeRepository: ChallengeRepository,
    private val challengeEngine: ChallengeEngine,
    private val repository: DailyRepository,
    private val firestore: FirebaseFirestore,
    private val storyArcRepository: StoryArcRepository
) {
    suspend operator fun invoke(
        user: UserModel,
        date: String,
        injectedTasks: List<TaskInstance> = emptyList(),
        recentTemplateIds: List<String> = emptyList()
    ): GenerationOutcome {
        Log.d("GenerateDailyTasks", "Starting generation for userId=${user.userId}, familyId=${user.familyId}, date=$date")

        // ✅ CRITICAL: ONE-TIME DAILY RESYNC FROM FIRESTORE
        // Firestore is the single source of truth - wipe Room and rebuild completely
        try {
            Log.d("GenerateDailyTasks", "🔄 Starting daily Firestore resync...")

            // Step 1: Get all ASSIGNED assignments from Firestore
            val assignmentsSnapshot = firestore
                .collection("families")
                .document(user.familyId)
                .collection("users")
                .document(user.userId)
                .collection("assignments")
                .whereEqualTo("status", "ASSIGNED")
                .get().await()

            Log.d("GenerateDailyTasks", "Found ${assignmentsSnapshot.documents.size} ASSIGNED assignments in Firestore")

            val assignedTaskIds = assignmentsSnapshot.documents.mapNotNull { it.getString("taskId") }

            // ✅ NEW: Get progress for today to filter out already-completed recurrent tasks
            val todayProgressSnapshot = firestore
                .collection("families")
                .document(user.familyId)
                .collection("users")
                .document(user.userId)
                .collection("task_progress")
                .whereEqualTo("date", date)
                .get().await()

            // ✅ Get completed task IDs for today to filter recurrent tasks
            val completedTaskIdsToday = todayProgressSnapshot.documents
                .mapNotNull { it.getString("templateId") }  // ✅ This now works!
                .toSet()

            Log.d("GenerateDailyTasks", "Already completed today: $completedTaskIdsToday")

            val assignedInstances = mutableListOf<TaskInstance>()

            // ✅ Filter out tasks already completed today
            for (taskId in assignedTaskIds) {
                if (taskId in completedTaskIdsToday) {
                    Log.d("GenerateDailyTasks", "⏭️ Skipping recurrent task (already completed today): $taskId")
                    continue
                }

                try {
                    val taskDoc = firestore
                        .collection("families").document(user.familyId)
                        .collection("tasks").document(taskId)
                        .get().await()

                    val taskModel = taskDoc.toObject(TaskModel::class.java)
                    if (taskModel != null && taskModel.title.isNotBlank()) {
                        assignedInstances.add(
                            TaskInstance(
                                instanceId = "assign_${taskId}_${System.currentTimeMillis()}",
                                templateId = taskId,
                                task = taskModel,
                                assignedDate = date,
                                userId = user.userId,
                                injectedByChallengeId = null,
                                status = TaskStatus.PENDING,
                                completedAt = 0L
                            )
                        )
                        Log.d("GenerateDailyTasks", "Fetched assignment: ${taskModel.title}")
                    }
                } catch (e: Exception) {
                    Log.e("GenerateDailyTasks", "Error fetching task $taskId", e)
                }
            }

            // Step 2: Get all COMPLETED task_progress from Firestore for TODAY
            val completedInstances = mutableListOf<TaskInstance>()

            for (doc in todayProgressSnapshot.documents) {
                try {
                    val taskInstanceId = doc.getString("taskInstanceId") ?: continue
                    val taskTitle = doc.getString("taskTitle") ?: "Completed Task"
                    val completedAt = doc.getLong("completionTime") ?: 0L
                    val taskJson = doc.getString("taskJson")

                    // Try to deserialize task from JSON if available
                    val taskModel = if (!taskJson.isNullOrBlank()) {
                        try {
                            val gson = com.google.gson.Gson()
                            gson.fromJson(taskJson, TaskModel::class.java)
                        } catch (e: Exception) {
                            TaskModel(title = taskTitle)
                        }
                    } else {
                        TaskModel(title = taskTitle)
                    }

                    completedInstances.add(
                        TaskInstance(
                            instanceId = taskInstanceId,
                            templateId = doc.getString("templateId") ?: "",
                            task = taskModel,
                            assignedDate = date,
                            userId = user.userId,
                            injectedByChallengeId = null,
                            status = TaskStatus.COMPLETED,
                            completedAt = completedAt
                        )
                    )
                    Log.d("GenerateDailyTasks", "Recovered completed task: $taskTitle")
                } catch (e: Exception) {
                    Log.e("GenerateDailyTasks", "Error processing completed task from Firestore", e)
                }
            }

            // Step 3: Combine assigned + completed from Firestore
            val firestoreTasks = assignedInstances + completedInstances

            // Step 4: Replace Room entirely with Firestore data
            repository.replaceTasksForDate(user.familyId, user.userId, date, firestoreTasks)

            Log.d("GenerateDailyTasks", "✅ Resync complete: ${assignedInstances.size} pending, ${completedInstances.size} completed")

        } catch (e: Exception) {
            Log.e("GenerateDailyTasks", "❌ Error during Firestore resync", e)
            return GenerationOutcome.Error
        }

        // ✅ Check if we've already generated AI tasks for today
        if (repository.hasTasksForDate(user.familyId, user.userId, date)) {
            Log.d("GenerateDailyTasks", "Tasks already generated for $date, skipping AI generation")
            return GenerationOutcome.AlreadyGenerated
        }

        // ✅ STEP 1: Fetch active challenges for this user
        try {
            Log.d("GenerateDailyTasks", "Fetching active challenges for user: ${user.userId}")
            val activeChallenges = challengeRepository.getActiveChallenges(user.userId, user.familyId)

            Log.d("GenerateDailyTasks", "Found ${activeChallenges.size} active challenges")

            // STEP 2: Generate challenge tasks
            val challengeTasks = mutableListOf<TaskInstance>()
            for (progress in activeChallenges) {
                try {
                    val challenge = challengeRepository.getChallenge(progress.challengeId)
                    if (challenge != null) {
                        val task = challengeEngine.generateDailyTask(challenge, progress, date)
                        if (task != null) {
                            val instance = TaskInstance(
                                instanceId = task.id,
                                templateId = progress.challengeId,
                                task = task,
                                assignedDate = date,
                                userId = user.userId,
                                injectedByChallengeId = progress.challengeId,
                                status = TaskStatus.PENDING,
                                completedAt = 0L
                            )
                            challengeTasks.add(instance)
                            Log.d("GenerateDailyTasks", "Added challenge task: ${task.title}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GenerateDailyTasks", "Error processing challenge ${progress.challengeId}", e)
                }
            }

            Log.d("GenerateDailyTasks", "Generated ${challengeTasks.size} challenge tasks")

            // STEP 2b: Inject today's story arc chapter (if active)
            val storyTasks = mutableListOf<TaskInstance>()
            try {
                val activeArc = storyArcRepository.getActiveArc(user.familyId)
                if (activeArc != null && !activeArc.isComplete) {
                    val chapter = activeArc.chapters.getOrNull(activeArc.currentDay - 1)
                    if (chapter != null) {
                        val storyTask = TaskModel(
                            id          = "story_${activeArc.arcId}_day${activeArc.currentDay}",
                            type        = TaskType.STORY,
                            title       = chapter.taskTitle,
                            description = chapter.taskDescription,
                            category    = try { TaskCategory.valueOf(chapter.category) }
                            catch (_: Exception) { TaskCategory.CREATIVITY },
                            difficulty  = try { DifficultyLevel.valueOf(chapter.difficulty) }
                            catch (_: Exception) { DifficultyLevel.MEDIUM },
                            estimatedDurationSec = chapter.estimatedDurationSec,
                            reward      = TaskReward(xp = chapter.xpReward),
                            createdBy   = TaskCreator.SYSTEM,
                            familyId    = user.familyId
                        )
                        storyTasks.add(
                            TaskInstance(
                                instanceId           = storyTask.id,
                                templateId           = activeArc.arcId,
                                task                 = storyTask,
                                assignedDate         = date,
                                userId               = user.userId,
                                injectedByChallengeId = "story_${activeArc.arcId}",
                                status               = TaskStatus.PENDING,
                                completedAt          = 0L
                            )
                        )
                        Log.d("GenerateDailyTasks", "Injected story task: ${chapter.taskTitle}")
                    }
                }
            } catch (e: Exception) {
                Log.e("GenerateDailyTasks", "Error fetching story arc for family ${user.familyId}", e)
            }

            // STEP 3: Combine generated tasks with injected ones
            val allGeneratedTasks = challengeTasks + storyTasks + injectedTasks
            Log.d("GenerateDailyTasks", "Total generated tasks to save: ${allGeneratedTasks.size}")

            // STEP 4: Save generated tasks to Room and Firestore
            if (allGeneratedTasks.isNotEmpty()) {
                repository.saveDailyTasks(user.familyId, user.userId, date, allGeneratedTasks)
                Log.d("GenerateDailyTasks", "✅ Saved ${allGeneratedTasks.size} generated tasks")
            }

            return GenerationOutcome.Success(allGeneratedTasks)
        } catch (e: Exception) {
            Log.e("GenerateDailyTasks", "❌ Error generating daily tasks", e)
            return GenerationOutcome.Error
        }
    }

    // ✅ FIXED: Only fetch from assignments collection where status="ASSIGNED"
    // Never fetch from task_progress or anywhere else
    private suspend fun fetchParentAssignedTasks(user: UserModel, date: String): List<TaskInstance> {
        val result = mutableListOf<TaskInstance>()
        try {
            // ✅ CRITICAL: Only query /families/{familyId}/users/{userId}/assignments/
            // with status="ASSIGNED" - this filters out completed/deleted tasks
            val assignmentsSnapshot = firestore
                .collection("families")
                .document(user.familyId)
                .collection("users")
                .document(user.userId)
                .collection("assignments")
                .whereEqualTo("status", "ASSIGNED")  // ✅ ONLY ASSIGNED - never completed
                .get().await()

            Log.d("GenerateDailyTasks", "Found ${assignmentsSnapshot.documents.size} assignments with status=ASSIGNED")

            val assignedTaskIds = assignmentsSnapshot.documents.mapNotNull { it.getString("taskId") }
            for (taskId in assignedTaskIds) {
                try {
                    val taskDoc = firestore
                        .collection("families").document(user.familyId)
                        .collection("tasks").document(taskId)
                        .get().await()

                    val taskModel = taskDoc.toObject(TaskModel::class.java)
                    if (taskModel != null && taskModel.title.isNotBlank()) {
                        result.add(
                            TaskInstance(
                                instanceId           = "assign_${taskId}_${System.currentTimeMillis()}",
                                templateId           = taskId,
                                task                 = taskModel,
                                assignedDate         = date,
                                userId               = user.userId,
                                injectedByChallengeId = null,
                                status               = TaskStatus.PENDING,
                                completedAt          = 0L
                            )
                        )
                        Log.d("GenerateDailyTasks", "Fetched parent-assigned task: ${taskModel.title}")
                    }
                } catch (e: Exception) {
                    Log.e("GenerateDailyTasks", "Error fetching task $taskId from family ${user.familyId}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("GenerateDailyTasks", "Error fetching parent assignments for user ${user.userId}", e)
        }
        return result
    }
}

sealed class GenerationOutcome {
    data class Success(val tasks: List<TaskInstance>) : GenerationOutcome()
    object AlreadyGenerated : GenerationOutcome()
    object NoTemplatesAvailable : GenerationOutcome()
    object Error : GenerationOutcome()
}