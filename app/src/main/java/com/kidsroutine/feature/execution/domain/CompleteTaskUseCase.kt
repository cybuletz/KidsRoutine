package com.kidsroutine.feature.execution.domain

import android.util.Log
import com.kidsroutine.core.ai.AIGenerationService
import com.kidsroutine.core.ai.GenerationContext
import com.kidsroutine.core.ai.GenerationType
import com.kidsroutine.core.engine.progression_engine.ProgressionEngine
import com.kidsroutine.core.engine.task_engine.TaskEngine
import com.kidsroutine.core.engine.task_engine.ValidationResult
import com.kidsroutine.core.model.*
import com.kidsroutine.core.common.util.DateUtils
import com.kidsroutine.feature.execution.data.TaskProgressRepository
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.achievements.data.AchievementRepository
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.feature.daily.data.StoryArcRepository
import kotlinx.coroutines.tasks.await

class CompleteTaskUseCase @Inject constructor(
    private val taskEngine: TaskEngine,
    private val progressionEngine: ProgressionEngine,
    private val repository: TaskProgressRepository,
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore,
    private val achievementRepository: AchievementRepository,
    private val storyArcRepository: StoryArcRepository,
    private val aiGenerationService: AIGenerationService   // ← NEW injection
) {
    suspend operator fun invoke(
        task: TaskModel,
        userId: String,
        photoUrl: String? = null,
        currentStreak: Int = 0,
        lastActiveDate: String = "",
        childName: String = "",      // ← NEW param — pass from ExecutionViewModel
        childAge: Int = 8,           // ← NEW param
        familyId: String = ""        // ← NEW param (already on task.familyId, kept for clarity)
    ): CompletionResult {
        Log.d("CompleteTaskUseCase", "=== START TASK COMPLETION ===")
        Log.d("CompleteTaskUseCase", "userId=$userId, taskId=${task.id}, baseXp=${task.reward.xp}")

        val today      = DateUtils.todayString()
        val progress   = TaskProgressModel(taskInstanceId = task.id, userId = userId, date = today)
        val validation = taskEngine.validate(task, progress, photoUrl)

        val (status, validationStatus) = when (validation) {
            ValidationResult.Approved      -> TaskStatus.COMPLETED to ValidationStatus.APPROVED
            ValidationResult.PendingParent -> TaskStatus.COMPLETED to ValidationStatus.PENDING
            is ValidationResult.Rejected   -> {
                Log.e("CompleteTaskUseCase", "Task rejected: ${validation.reason}")
                return CompletionResult.Rejected(validation.reason)
            }
        }

        // 1. Save task progress locally
        val progressModel = TaskProgressModel(
            taskInstanceId    = task.id,
            userId            = userId,
            date              = today,
            status            = status,
            completionTime    = System.currentTimeMillis(),
            validationStatus  = validationStatus,
            photoUrl          = photoUrl,
            syncedToFirestore = false
        )
        repository.saveProgress(progressModel)

        // 1.5 Sync taskProgress to Firestore
        try {
            firestore.collection("taskProgress")
                .document("${task.id}_${userId}_${System.currentTimeMillis()}")
                .set(mapOf(
                    "taskInstanceId"  to task.id,
                    "userId"          to userId,
                    "date"            to today,
                    "status"          to status.name,
                    "completionTime"  to System.currentTimeMillis(),
                    "validationStatus" to validationStatus.name,
                    "photoUrl"        to photoUrl,
                    "taskTitle"       to task.title,
                    "familyId"        to task.familyId,
                    "xpGained"        to 0
                ))
                .await()
        } catch (e: Exception) {
            Log.e("CompleteTaskUseCase", "taskProgress Firestore sync failed", e)
        }

        // 1.6 ── NEW: Write task_instances doc (feeds ChildSummaryCard ring) ──
        try {
            firestore.collection("task_instances")
                .document("${userId}_${task.id}_$today")
                .set(mapOf(
                    "userId"         to userId,
                    "familyId"       to task.familyId,
                    "taskId"         to task.id,
                    "taskTitle"      to task.title,
                    "status"         to status.name,
                    "date"           to today,
                    "completedAt"    to System.currentTimeMillis(),
                    "xpReward"       to task.reward.xp
                ))
                .await()
            Log.d("CompleteTaskUseCase", "task_instances doc written ✓")
        } catch (e: Exception) {
            Log.e("CompleteTaskUseCase", "task_instances write failed (non-fatal)", e)
        }

        // 2. Calculate XP
        val newStreak = progressionEngine.streakCalculator.computeStreak(currentStreak, lastActiveDate, today)
        val xpGained  = progressionEngine.xpCalculator.forTask(
            task,
            isCoop        = task.requiresCoop,
            isStreakBonus = newStreak > 1
        )

        // 3. Update XP
        try {
            userRepository.updateUserXp(userId, xpGained)
        } catch (e: Exception) {
            Log.e("CompleteTaskUseCase", "XP update FAILED", e)
            throw e
        }

        // 4. Check achievements
        try {
            val newBadges = achievementRepository.checkAndUnlockAchievements(userId)
            if (newBadges.isNotEmpty()) Log.d("CompleteTaskUseCase", "🏆 Unlocked ${newBadges.size} badges!")
        } catch (e: Exception) {
            Log.e("CompleteTaskUseCase", "Achievement check failed (non-fatal)", e)
        }

        // 5. Advance story arc
        if (task.type == TaskType.STORY) {
            try {
                val arcId = task.id.removePrefix("story_").substringBeforeLast("_day")
                if (arcId.isNotBlank() && task.familyId.isNotBlank()) {
                    val arc = storyArcRepository.getActiveArc(task.familyId)
                    if (arc != null && arc.arcId == arcId) {
                        if (arc.currentDay >= arc.chapters.size) storyArcRepository.completeArc(arcId)
                        else storyArcRepository.advanceDay(arcId)
                    }
                }
            } catch (e: Exception) {
                Log.w("CompleteTaskUseCase", "Could not advance story arc: ${e.message}")
            }
        }

        // 6. ── NEW: Generate AI celebration message (non-blocking, best-effort) ──
        var celebrationMessage = defaultCelebration(childName, task.title, newStreak)
        try {
            val effectiveName   = childName.ifBlank { "Champion" }
            val effectiveFamilyId = familyId.ifBlank { task.familyId }
            val prompt = """
                Write a single short celebratory sentence (max 12 words) for a child named $effectiveName
                who just completed the task "${task.title}". They have a $newStreak-day streak.
                Be enthusiastic, use 1-2 emojis. No quotes, just the sentence.
            """.trimIndent()

            val result = aiGenerationService.generate(
                prompt      = prompt,
                contentType = GenerationType.CUSTOM,
                context     = GenerationContext(
                    userId    = userId,
                    familyId  = effectiveFamilyId,
                    childAge  = childAge
                ),
                maxTokens   = 60,
                temperature = 0.9f
            )

            result.onSuccess { msg ->
                if (msg.isNotBlank()) celebrationMessage = msg
            }
        } catch (e: Exception) {
            Log.w("CompleteTaskUseCase", "Celebration message failed (non-fatal): ${e.message}")
        }

        Log.d("CompleteTaskUseCase", "=== TASK COMPLETION SUCCESS ===")
        return CompletionResult.Success(
            xpGained           = xpGained,
            newStreak          = newStreak,
            needsParent        = validation is ValidationResult.PendingParent,
            celebrationMessage = celebrationMessage   // ← NEW field
        )
    }

    private fun defaultCelebration(name: String, taskTitle: String, streak: Int): String {
        val n = name.ifBlank { "Champion" }
        return when {
            streak >= 7  -> "🔥 $n is on fire! $streak days strong!"
            streak >= 3  -> "⭐ Amazing, $n! Keep that streak going!"
            else         -> "🎉 Great job, $n! \"$taskTitle\" complete!"
        }
    }
}

sealed class CompletionResult {
    data class Success(
        val xpGained: Int,
        val newStreak: Int,
        val needsParent: Boolean,
        val celebrationMessage: String = ""   // ← NEW field
    ) : CompletionResult()
    data class Rejected(val reason: String) : CompletionResult()
}
