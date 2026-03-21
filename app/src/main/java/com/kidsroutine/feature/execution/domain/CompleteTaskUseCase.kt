package com.kidsroutine.feature.execution.domain

import android.util.Log
import com.kidsroutine.core.engine.progression_engine.ProgressionEngine
import com.kidsroutine.core.engine.task_engine.TaskEngine
import com.kidsroutine.core.engine.task_engine.ValidationResult
import com.kidsroutine.core.model.*
import com.kidsroutine.core.common.util.DateUtils
import com.kidsroutine.feature.execution.data.TaskProgressRepository
import com.kidsroutine.feature.daily.data.UserRepository
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CompleteTaskUseCase @Inject constructor(
    private val taskEngine: TaskEngine,
    private val progressionEngine: ProgressionEngine,
    private val repository: TaskProgressRepository,
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore
) {
    suspend operator fun invoke(
        task: TaskModel,
        userId: String,
        photoUrl: String? = null,
        currentStreak: Int = 0,
        lastActiveDate: String = ""
    ): CompletionResult {
        Log.d("CompleteTaskUseCase", "=== START TASK COMPLETION ===")
        Log.d("CompleteTaskUseCase", "userId=$userId, taskId=${task.id}, baseXp=${task.reward.xp}")

        val today    = DateUtils.todayString()
        val progress = TaskProgressModel(taskInstanceId = task.id, userId = userId, date = today)
        val validation = taskEngine.validate(task, progress, photoUrl)

        Log.d("CompleteTaskUseCase", "Validation result: $validation")

        val (status, validationStatus) = when (validation) {
            ValidationResult.Approved      -> TaskStatus.COMPLETED to ValidationStatus.APPROVED
            ValidationResult.PendingParent -> TaskStatus.COMPLETED to ValidationStatus.PENDING
            is ValidationResult.Rejected   -> {
                Log.e("CompleteTaskUseCase", "Task rejected: ${validation.reason}")
                return CompletionResult.Rejected(validation.reason)
            }
        }

        // 1. Save task progress locally
        Log.d("CompleteTaskUseCase", "Saving task progress...")
        val progressModel = TaskProgressModel(
            taskInstanceId   = task.id,
            userId           = userId,
            date             = today,
            status           = status,
            completionTime   = System.currentTimeMillis(),
            validationStatus = validationStatus,
            photoUrl         = photoUrl,
            syncedToFirestore = false
        )
        repository.saveProgress(progressModel)
        Log.d("CompleteTaskUseCase", "Task progress saved ✓")

        // 1.5 SYNC TO FIRESTORE IMMEDIATELY WITH ALL FIELDS
        Log.d("CompleteTaskUseCase", "Syncing to Firestore...")
        try {
            firestore.collection("taskProgress")
                .document("${task.id}_${userId}_${System.currentTimeMillis()}")
                .set(mapOf(
                    "taskInstanceId" to task.id,
                    "userId" to userId,
                    "date" to today,
                    "status" to status.name,
                    "completionTime" to System.currentTimeMillis(),
                    "validationStatus" to validationStatus.name,
                    "photoUrl" to photoUrl,
                    "taskTitle" to task.title,
                    "familyId" to task.familyId,
                    "xpGained" to 0
                ))
                .await()
            Log.d("CompleteTaskUseCase", "Firestore sync successful ✓")
        } catch (e: Exception) {
            Log.e("CompleteTaskUseCase", "Firestore sync failed", e)
        }

        // 2. Calculate XP
        val newStreak = progressionEngine.streakCalculator.computeStreak(currentStreak, lastActiveDate, today)
        val xpGained  = progressionEngine.xpCalculator.forTask(
            task,
            isCoop        = task.requiresCoop,
            isStreakBonus = newStreak > 1
        )
        Log.d("CompleteTaskUseCase", "XP calculated: baseXp=${task.reward.xp}, isCoop=${task.requiresCoop}, streak=$newStreak, finalXp=$xpGained")

        // 3. UPDATE USER'S TOTAL XP
        Log.d("CompleteTaskUseCase", "Calling userRepository.updateUserXp($userId, $xpGained)...")
        try {
            userRepository.updateUserXp(userId, xpGained)
            Log.d("CompleteTaskUseCase", "XP update completed ✓")
        } catch (e: Exception) {
            Log.e("CompleteTaskUseCase", "XP update FAILED", e)
            throw e
        }

        Log.d("CompleteTaskUseCase", "=== TASK COMPLETION SUCCESS ===")
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