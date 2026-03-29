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

        // ✅ NEW: Always sync parent-assigned tasks, even if day was already generated
        val freshAssignedTasks = fetchParentAssignedTasks(user, date)
        if (freshAssignedTasks.isNotEmpty()) {
            Log.d("GenerateDailyTasks", "Merging ${freshAssignedTasks.size} parent-assigned tasks")
            repository.mergeAssignedTasks(user.familyId, user.userId, date, freshAssignedTasks)
        }

        // Enforce 1-per-day generation limit for the rest
        if (repository.hasTasksForDate(user.familyId, user.userId, date)) {
            Log.d("GenerateDailyTasks", "Tasks already generated for $date")
            return GenerationOutcome.AlreadyGenerated
        }

        try {
            // STEP 1: Fetch active challenges for this user
            Log.d("GenerateDailyTasks", "Fetching active challenges for user: ${user.userId}")
            val activeChallenges = challengeRepository.getActiveChallenges(user.userId)
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

            // STEP 3: Combine all tasks
            val allTasks = challengeTasks + storyTasks + injectedTasks
            Log.d("GenerateDailyTasks", "Total tasks to save: ${allTasks.size}")

            if (allTasks.isNotEmpty()) {
                // ✅ NEW: Pass familyId to save
                repository.saveDailyTasks(user.familyId, user.userId, date, allTasks)
            }

            return GenerationOutcome.Success(allTasks)
        } catch (e: Exception) {
            Log.e("GenerateDailyTasks", "Error generating daily tasks", e)
            return GenerationOutcome.NoTemplatesAvailable
        }
    }

    // ✅ NEW: requires familyId for family-scoped queries
    private suspend fun fetchParentAssignedTasks(user: UserModel, date: String): List<TaskInstance> {
        val result = mutableListOf<TaskInstance>()
        try {
            val assignmentsSnapshot = firestore
                .collection("taskAssignments")
                .whereEqualTo("childId", user.userId)
                .whereEqualTo("status", "ASSIGNED")
                .get().await()

            val assignedTaskIds = assignmentsSnapshot.documents.mapNotNull { it.getString("taskId") }
            for (taskId in assignedTaskIds) {
                try {
                    // ✅ CHANGED: Fetch from family-scoped path
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
}