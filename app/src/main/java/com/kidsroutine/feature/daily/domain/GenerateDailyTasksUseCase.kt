package com.kidsroutine.feature.daily.domain

import android.util.Log
import com.kidsroutine.core.engine.task_engine.GenerationContext
import com.kidsroutine.core.engine.task_engine.TaskEngine
import com.kidsroutine.core.engine.challenge_engine.ChallengeEngine
import com.kidsroutine.core.model.*
import com.kidsroutine.core.common.util.DateUtils
import com.kidsroutine.feature.daily.data.DailyRepository
import com.kidsroutine.feature.challenges.data.ChallengeRepository
import javax.inject.Inject

class GenerateDailyTasksUseCase @Inject constructor(
    private val taskEngine: TaskEngine,
    private val challengeRepository: ChallengeRepository,
    private val challengeEngine: ChallengeEngine,
    private val repository: DailyRepository
) {
    /**
     * Generates exactly 5 tasks for today if not already generated.
     * Respects daily generation limit (1/day).
     * Injects tasks from active challenges.
     */
    suspend operator fun invoke(
        user: UserModel,
        date: String,
        injectedTasks: List<TaskInstance> = emptyList(),
        recentTemplateIds: List<String> = emptyList()
    ): GenerationOutcome {
        // Enforce 1-per-day generation limit
        if (repository.hasTasksForDate(user.userId, date)) {
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
                                injectedByChallengeId = progress.challengeId
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

            // STEP 3: Fetch task templates
            val templates = repository.fetchTaskTemplatesFromFirestore(user.familyId)
            Log.d("GenerateDailyTasks", "Fetched ${templates.size} templates")

            if (templates.isEmpty()) return GenerationOutcome.NoTemplatesAvailable

            // STEP 4: Generate regular tasks (max 5 - challenge task count)
            val maxRegularTasks = maxOf(0, 5 - challengeTasks.size)
            val context = GenerationContext(
                userId = user.userId,
                date = date,
                recentTemplateIds = recentTemplateIds,
                activeChallengeTaskIds = challengeTasks.map { it.instanceId },
                userPreferences = user.preferences
            )

            val regularTasks = taskEngine.generateDailyTasks(
                templates,
                emptyList(),
                context
            ).take(maxRegularTasks)

            Log.d("GenerateDailyTasks", "Generated ${regularTasks.size} regular tasks")

            // STEP 5: Combine all tasks
            val allTasks = challengeTasks + regularTasks

            Log.d("GenerateDailyTasks", "Total tasks generated: ${allTasks.size} (${challengeTasks.size} challenges + ${regularTasks.size} regular)")

            // STEP 6: Save to repository
            repository.saveDailyTasks(user.userId, date, allTasks)

            return GenerationOutcome.Success(allTasks)
        } catch (e: Exception) {
            Log.e("GenerateDailyTasks", "Error generating daily tasks", e)
            return GenerationOutcome.NoTemplatesAvailable
        }
    }
}

sealed class GenerationOutcome {
    data class Success(val tasks: List<TaskInstance>) : GenerationOutcome()
    data object AlreadyGenerated : GenerationOutcome()
    data object NoTemplatesAvailable : GenerationOutcome()
}