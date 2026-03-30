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
    private val taskInstanceDao: com.kidsroutine.core.database.dao.TaskInstanceDao,
    private val aiGenerationService: AIGenerationService
) {
    suspend operator fun invoke(
        task: TaskModel,
        userId: String,
        familyId: String,  // ✅ NEW: REQUIRED for family-scoped Firestore paths
        instanceId: String = task.id,
        photoUrl: String? = null,
        currentStreak: Int = 0,
        lastActiveDate: String = "",
        childName: String = "",
        childAge: Int = 8
    ): CompletionResult {
        Log.d("CompleteTaskUseCase", "=== START TASK COMPLETION ===")
        Log.d("CompleteTaskUseCase", "familyId=$familyId, userId=$userId, taskId=${task.id}, baseXp=${task.reward.xp}")

        val today      = DateUtils.todayString()
        val progress   = TaskProgressModel(
            taskInstanceId = task.id,
            userId = userId,
            familyId = familyId,  // ✅ NEW
            date = today
        )
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
            familyId          = familyId,  // ✅ NEW
            date              = today,
            status            = status,
            completionTime    = System.currentTimeMillis(),
            validationStatus  = validationStatus,
            photoUrl          = photoUrl,
            taskTitle         = task.title,
            syncedToFirestore = false
        )
        repository.saveProgress(progressModel)

        // ✅ 1.5: Sync taskProgress to Firestore with FAMILY-SCOPED path
        val completionTimestamp = System.currentTimeMillis()
        try {
            firestore
                .collection("families").document(familyId)
                .collection("users").document(userId)
                .collection("task_progress")
                .document("${instanceId}_${completionTimestamp}")
                .set(mapOf(
                    "taskInstanceId"  to task.id,
                    "userId"          to userId,
                    "familyId"        to familyId,  // ✅ NEW
                    "date"            to today,
                    "status"          to status.name,
                    "completionTime"  to completionTimestamp,
                    "validationStatus" to validationStatus.name,
                    "photoUrl"        to photoUrl,
                    "taskTitle"       to task.title,
                    "templateId"      to task.id  // ✅ ADD THIS LINE - store the task template ID
                ))
                .await()
            Log.d("CompleteTaskUseCase", "✅ Task progress synced to family-scoped Firestore path")
        } catch (e: Exception) {
            Log.e("CompleteTaskUseCase", "❌ taskProgress Firestore sync failed", e)
        }

        // ✅ 1.6: DELETE task_instances from Firestore (task is completed, no longer "assigned")
        try {
            firestore
                .collection("families").document(familyId)
                .collection("users").document(userId)
                .collection("task_instances")
                .document(instanceId)
                .delete()  // ✅ DELETE, not UPDATE
                .await()
            Log.d("CompleteTaskUseCase", "✅ task_instances deleted from Firestore (task completed)")
        } catch (e: Exception) {
            Log.e("CompleteTaskUseCase", "❌ task_instances deletion failed (non-fatal)", e)
        }

        // ✅ Also delete from Room
        try {
            // Mark as COMPLETED in Room (not delete, so history is kept)
            taskInstanceDao.markCompleted(familyId, userId, instanceId, completionTimestamp)
            Log.d("CompleteTaskUseCase", "✅ Room task_instances deleted")
        } catch (e: Exception) {
            Log.e("CompleteTaskUseCase", "❌ Room deletion failed (non-fatal)", e)
        }


        // 2. Calculate XP
        val newStreak = progressionEngine.streakCalculator.computeStreak(currentStreak, lastActiveDate, today)
        val xpGained  = progressionEngine.xpCalculator.forTask(
            task,
            isCoop        = task.requiresCoop,
            isStreakBonus = newStreak > 1
        )

        // 3. Update XP and streak
        try {
            userRepository.updateUserXp(userId, xpGained)
        } catch (e: Exception) {
            Log.e("CompleteTaskUseCase", "❌ XP update FAILED", e)
            throw e
        }

        // 3.1: Write streak + lastActiveDate back to Firestore users doc
        try {
            firestore.collection("users").document(userId).update(
                mapOf(
                    "streak"         to newStreak,
                    "lastActiveAt"   to System.currentTimeMillis(),
                    "lastActiveDate" to today
                )
            ).await()
            Log.d("CompleteTaskUseCase", "✅ Streak updated to $newStreak")
        } catch (e: Exception) {
            Log.e("CompleteTaskUseCase", "⚠️ Streak write failed (non-fatal)", e)
        }

        // 4. Check achievements
        try {
            val newBadges = achievementRepository.checkAndUnlockAchievements(userId)
            if (newBadges.isNotEmpty()) Log.d("CompleteTaskUseCase", "🏆 Unlocked ${newBadges.size} badges!")
        } catch (e: Exception) {
            Log.e("CompleteTaskUseCase", "⚠️ Achievement check failed (non-fatal)", e)
        }

        // 5. Advance story arc
        if (task.type == TaskType.STORY) {
            try {
                val arcId = task.id.removePrefix("story_").substringBeforeLast("_day")
                if (arcId.isNotBlank()) {
                    val arc = storyArcRepository.getActiveArc(familyId)
                    if (arc != null && arc.arcId == arcId) {
                        if (arc.currentDay >= arc.chapters.size) storyArcRepository.completeArc(arcId)
                        else storyArcRepository.advanceDay(arcId)
                    }
                }
            } catch (e: Exception) {
                Log.w("CompleteTaskUseCase", "⚠️ Could not advance story arc: ${e.message}")
            }
        }

        // 6. Generate AI celebration message (non-blocking, best-effort)
        var celebrationMessage = defaultCelebration(childName, task.title, newStreak)
        try {
            val effectiveName = childName.ifBlank { "Champion" }
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
                    familyId  = familyId,
                    childAge  = childAge
                ),
                maxTokens   = 60,
                temperature = 0.9f
            )

            result.onSuccess { msg ->
                if (msg.isNotBlank()) celebrationMessage = msg
            }
        } catch (e: Exception) {
            Log.w("CompleteTaskUseCase", "⚠️ Celebration message failed (non-fatal): ${e.message}")
        }

        Log.d("CompleteTaskUseCase", "=== TASK COMPLETION SUCCESS ===")
        return CompletionResult.Success(
            xpGained           = xpGained,
            newStreak          = newStreak,
            needsParent        = validation is ValidationResult.PendingParent,
            celebrationMessage = celebrationMessage
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
        val celebrationMessage: String = ""
    ) : CompletionResult()
    data class Rejected(val reason: String) : CompletionResult()
}