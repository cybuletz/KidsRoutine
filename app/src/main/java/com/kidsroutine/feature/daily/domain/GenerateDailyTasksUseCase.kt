package com.kidsroutine.feature.daily.domain

import android.util.Log
import com.kidsroutine.core.engine.task_engine.GenerationContext
import com.kidsroutine.core.engine.task_engine.TaskEngine
import com.kidsroutine.core.model.*
import com.kidsroutine.feature.daily.data.DailyRepository
import javax.inject.Inject

class GenerateDailyTasksUseCase @Inject constructor(
    private val taskEngine: TaskEngine,
    private val repository: DailyRepository
) {
    /**
     * Generates exactly 5 tasks for today if not already generated.
     * Respects daily generation limit (1/day).
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
        val templates = repository.fetchTaskTemplatesFromFirestore(user.familyId)
        Log.d("GenerateDailyTasks", "Fetched ${templates.size} templates")
        Log.d("GenerateDailyTasks", "Templates: ${templates.map { it.templateId }}")

        if (templates.isEmpty()) return GenerationOutcome.NoTemplatesAvailable

        val context = GenerationContext(
            userId             = user.userId,
            date               = date,
            recentTemplateIds  = recentTemplateIds,
            activeChallengeTaskIds = injectedTasks.map { it.instanceId },
            userPreferences    = user.preferences
        )
        val tasks = taskEngine.generateDailyTasks(templates, injectedTasks, context)
        repository.saveDailyTasks(user.userId, date, tasks)
        return GenerationOutcome.Success(tasks)
    }
}

sealed class GenerationOutcome {
    data class Success(val tasks: List<TaskInstance>) : GenerationOutcome()
    data object AlreadyGenerated : GenerationOutcome()
    data object NoTemplatesAvailable : GenerationOutcome()
}
