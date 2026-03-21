package com.kidsroutine.feature.celebrations.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CelebrationEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: CelebrationType = CelebrationType.TASK_COMPLETION,
    val data: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class CelebrationType {
    TASK_COMPLETION,
    ACHIEVEMENT_UNLOCK,
    LEVEL_UP,
    MILESTONE
}

@HiltViewModel
class CelebrationViewModel @Inject constructor() : ViewModel() {

    private val _celebrationEvent = MutableStateFlow<CelebrationEvent?>(null)
    val celebrationEvent: StateFlow<CelebrationEvent?> = _celebrationEvent.asStateFlow()

    fun showTaskCompletion() {
        Log.d("CelebrationVM", "Showing task completion celebration")
        _celebrationEvent.value = CelebrationEvent(
            type = CelebrationType.TASK_COMPLETION
        )
    }

    fun showAchievementUnlock(achievementName: String) {
        Log.d("CelebrationVM", "Showing achievement unlock: $achievementName")
        _celebrationEvent.value = CelebrationEvent(
            type = CelebrationType.ACHIEVEMENT_UNLOCK,
            data = achievementName
        )
    }

    fun showLevelUp(newLevel: Int) {
        Log.d("CelebrationVM", "Showing level up: $newLevel")
        _celebrationEvent.value = CelebrationEvent(
            type = CelebrationType.LEVEL_UP,
            data = newLevel.toString()
        )
    }

    fun showMilestone(milestone: String) {
        Log.d("CelebrationVM", "Showing milestone: $milestone")
        _celebrationEvent.value = CelebrationEvent(
            type = CelebrationType.MILESTONE,
            data = milestone
        )
    }

    fun dismissCelebration() {
        _celebrationEvent.value = null
    }
}