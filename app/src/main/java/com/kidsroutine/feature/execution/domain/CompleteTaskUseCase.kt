package com.kidsroutine.feature.execution.domain

import com.kidsroutine.core.engine.progression_engine.ProgressionEngine
import com.kidsroutine.core.engine.task_engine.TaskEngine
import com.kidsroutine.core.engine.task_engine.ValidationResult
import com.kidsroutine.core.model.*
import com.kidsroutine.core.common.util.DateUtils
import com.kidsroutine.feature.execution.data.TaskProgressRepository
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val taskEngine: TaskEngine,
    private val progressionEngine: ProgressionEngine,
    private val repository: TaskProgressRepository
) {
    suspend operator fun invoke(
        task: TaskModel,
        userId: String,
        photoUrl: String? = null,
        currentStreak: Int = 0,
        lastActiveDate: String = ""
    ): CompletionResult {
        val today    = DateUtils.todayString()
        val progress = TaskProgressModel(taskInstanceId = task.id, userId = userId, date = today)
        val validation = taskEngine.validate(task, progress, photoUrl)

        val (status, validationStatus) = when (validation) {
            ValidationResult.Approved      -> TaskStatus.COMPLETED to ValidationStatus.APPROVED
            ValidationResult.PendingParent -> TaskStatus.COMPLETED to ValidationStatus.PENDING
            is ValidationResult.Rejected   -> return CompletionResult.Rejected(validation.reason)
        }

        repository.saveProgress(
            TaskProgressModel(
                taskInstanceId   = task.id,
                userId           = userId,
                date             = today,
                status           = status,
                completionTime   = System.currentTimeMillis(),
                validationStatus = validationStatus,
                photoUrl         = photoUrl,
                syncedToFirestore = false
            )
        )

        val newStreak = progressionEngine.streakCalculator.computeStreak(currentStreak, lastActiveDate, today)
        val xpGained  = progressionEngine.xpCalculator.forTask(
            task,
            isCoop        = task.requiresCoop,
            isStreakBonus = newStreak > 1
        )
        return CompletionResult.Success(
            xpGained    = xpGained,
            newStreak   = newStreak,
            needsParent = validation is ValidationResult.PendingParent
        )
    }
}

sealed class CompletionResult {
    data class Success(val xpGained: Int, val newStreak: Int, val needsParent: Boolean) : CompletionResult()
    data class Rejected(val reason: String) : CompletionResult()
}
