package com.kidsroutine.feature.community.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.*
import com.kidsroutine.feature.community.data.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublishUiState(
    val isLoading: Boolean = false,
    val activeTab: PublishTab = PublishTab.TASK,

    // Task publish form
    val taskTitle: String = "",
    val taskDescription: String = "",
    val taskCategory: TaskCategory = TaskCategory.LEARNING,
    val taskDifficulty: DifficultyLevel = DifficultyLevel.EASY,
    val taskType: TaskType = TaskType.LEARNING,
    val taskDuration: Int = 300,
    val taskXp: Int = 10,

    // Challenge publish form
    val challengeTitle: String = "",
    val challengeDescription: String = "",
    val challengeCategory: TaskCategory = TaskCategory.HEALTH,
    val challengeDifficulty: DifficultyLevel = DifficultyLevel.EASY,
    val challengeDuration: Int = 7,
    val dailyXp: Int = 15,
    val bonusXp: Int = 75,
    val streakXp: Int = 5,

    // State
    val isPublishing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap()
)

enum class PublishTab {
    TASK,
    CHALLENGE
}

@HiltViewModel
class PublishViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishUiState())
    val uiState: StateFlow<PublishUiState> = _uiState.asStateFlow()

    fun selectTab(tab: PublishTab) {
        Log.d("PublishVM", "Selecting tab: $tab")
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    // Task form updates
    fun updateTaskTitle(title: String) {
        _uiState.value = _uiState.value.copy(taskTitle = title)
    }

    fun updateTaskDescription(description: String) {
        _uiState.value = _uiState.value.copy(taskDescription = description)
    }

    fun updateTaskCategory(category: TaskCategory) {
        _uiState.value = _uiState.value.copy(taskCategory = category)
    }

    fun updateTaskDifficulty(difficulty: DifficultyLevel) {
        _uiState.value = _uiState.value.copy(taskDifficulty = difficulty)
    }

    fun updateTaskType(type: TaskType) {
        _uiState.value = _uiState.value.copy(taskType = type)
    }

    fun updateTaskDuration(duration: Int) {
        _uiState.value = _uiState.value.copy(taskDuration = duration)
    }

    fun updateTaskXp(xp: Int) {
        _uiState.value = _uiState.value.copy(taskXp = xp)
    }

    // Challenge form updates
    fun updateChallengeTitle(title: String) {
        _uiState.value = _uiState.value.copy(challengeTitle = title)
    }

    fun updateChallengeDescription(description: String) {
        _uiState.value = _uiState.value.copy(challengeDescription = description)
    }

    fun updateChallengeCategory(category: TaskCategory) {
        _uiState.value = _uiState.value.copy(challengeCategory = category)
    }

    fun updateChallengeDifficulty(difficulty: DifficultyLevel) {
        _uiState.value = _uiState.value.copy(challengeDifficulty = difficulty)
    }

    fun updateChallengeDuration(duration: Int) {
        _uiState.value = _uiState.value.copy(challengeDuration = duration)
    }

    fun updateDailyXp(xp: Int) {
        _uiState.value = _uiState.value.copy(dailyXp = xp)
    }

    fun updateBonusXp(xp: Int) {
        _uiState.value = _uiState.value.copy(bonusXp = xp)
    }

    fun updateStreakXp(xp: Int) {
        _uiState.value = _uiState.value.copy(streakXp = xp)
    }

    fun publishTask(userId: String, creatorName: String) {
        Log.d("PublishVM", "Publishing task: ${_uiState.value.taskTitle}")

        // Validate
        val errors = mutableMapOf<String, String>()
        if (_uiState.value.taskTitle.isBlank()) errors["title"] = "Title required"
        if (_uiState.value.taskDescription.isBlank()) errors["description"] = "Description required"
        if (_uiState.value.taskXp < 1) errors["xp"] = "XP must be at least 1"

        if (errors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(validationErrors = errors)
            return
        }

        _uiState.value = _uiState.value.copy(isPublishing = true, error = null)

        viewModelScope.launch {
            try {
                val task = SharedTask(
                    taskId = "task_${System.currentTimeMillis()}",
                    title = _uiState.value.taskTitle,
                    description = _uiState.value.taskDescription,
                    category = _uiState.value.taskCategory,
                    difficulty = _uiState.value.taskDifficulty,
                    type = _uiState.value.taskType,
                    estimatedDurationSec = _uiState.value.taskDuration,
                    reward = TaskReward(xp = _uiState.value.taskXp),
                    createdBy = userId,
                    creatorName = creatorName,
                    status = ContentStatus.PENDING
                )

                communityRepository.publishTask(task)

                _uiState.value = _uiState.value.copy(
                    isPublishing = false,
                    successMessage = "✅ Task submitted for review! Approval typically takes 24 hours.",
                    taskTitle = "",
                    taskDescription = "",
                    taskXp = 10,
                    validationErrors = emptyMap()
                )

                Log.d("PublishVM", "Task published successfully")
            } catch (e: Exception) {
                Log.e("PublishVM", "Error publishing task", e)
                _uiState.value = _uiState.value.copy(
                    isPublishing = false,
                    error = e.message ?: "Failed to publish task"
                )
            }
        }
    }

    fun publishChallenge(userId: String, creatorName: String) {
        Log.d("PublishVM", "Publishing challenge: ${_uiState.value.challengeTitle}")

        // Validate
        val errors = mutableMapOf<String, String>()
        if (_uiState.value.challengeTitle.isBlank()) errors["title"] = "Title required"
        if (_uiState.value.challengeDescription.isBlank()) errors["description"] = "Description required"
        if (_uiState.value.dailyXp < 1) errors["dailyXp"] = "Daily XP must be at least 1"
        if (_uiState.value.challengeDuration < 1) errors["duration"] = "Duration must be at least 1 day"

        if (errors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(validationErrors = errors)
            return
        }

        _uiState.value = _uiState.value.copy(isPublishing = true, error = null)

        viewModelScope.launch {
            try {
                val challenge = SharedChallenge(
                    challengeId = "challenge_${System.currentTimeMillis()}",
                    title = _uiState.value.challengeTitle,
                    description = _uiState.value.challengeDescription,
                    category = _uiState.value.challengeCategory,
                    difficulty = _uiState.value.challengeDifficulty,
                    duration = _uiState.value.challengeDuration,
                    dailyXpReward = _uiState.value.dailyXp,
                    completionBonusXp = _uiState.value.bonusXp,
                    streakBonusXp = _uiState.value.streakXp,
                    createdBy = userId,
                    creatorName = creatorName,
                    status = ContentStatus.PENDING
                )

                communityRepository.publishChallenge(challenge)

                _uiState.value = _uiState.value.copy(
                    isPublishing = false,
                    successMessage = "✅ Challenge submitted for review! Approval typically takes 24 hours.",
                    challengeTitle = "",
                    challengeDescription = "",
                    dailyXp = 15,
                    bonusXp = 75,
                    streakXp = 5,
                    validationErrors = emptyMap()
                )

                Log.d("PublishVM", "Challenge published successfully")
            } catch (e: Exception) {
                Log.e("PublishVM", "Error publishing challenge", e)
                _uiState.value = _uiState.value.copy(
                    isPublishing = false,
                    error = e.message ?: "Failed to publish challenge"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            error = null,
            validationErrors = emptyMap()
        )
    }
}