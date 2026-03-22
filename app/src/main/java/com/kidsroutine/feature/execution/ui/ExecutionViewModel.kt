package com.kidsroutine.feature.execution.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.*
import com.kidsroutine.feature.execution.domain.CompleteTaskUseCase
import com.kidsroutine.feature.execution.domain.CompletionResult
import com.kidsroutine.feature.achievements.data.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

data class ExecutionUiState(
    val task: TaskModel = TaskModel(),
    val currentBlockIndex: Int = 0,
    val blockAnswers: Map<String, Any> = emptyMap(),
    val photoUrl: String? = null,
    val timerRunning: Boolean = false,
    val timerSecondsLeft: Int = 0,
    val isCompleting: Boolean = false,
    val result: CompletionResult? = null,
    val showSuccessAnim: Boolean = false,
    val newBadgesUnlocked: List<Badge> = emptyList()  // ← ADD THIS
)

sealed class ExecutionEvent {
    data class BlockAnswered(val blockId: String, val answer: Any) : ExecutionEvent()
    data class PhotoCaptured(val url: String) : ExecutionEvent()
    data object TimerStarted : ExecutionEvent()
    data class TimerTick(val secondsLeft: Int) : ExecutionEvent()
    data object TimerFinished : ExecutionEvent()
    data object SubmitTask : ExecutionEvent()
    data object DismissResult : ExecutionEvent()
}

@HiltViewModel
class ExecutionViewModel @Inject constructor(
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val achievementRepository: AchievementRepository,  // ← ADD THIS
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExecutionUiState())
    val uiState: StateFlow<ExecutionUiState> = _uiState.asStateFlow()

    fun loadTask(task: TaskModel) {
        val firstBlock = task.interactionBlocks.firstOrNull()
        val timerSec   = if (firstBlock?.type == InteractionBlockType.TIMER)
            (firstBlock.config["durationSec"] as? Int) ?: 0 else 0
        _uiState.update { it.copy(task = task, timerSecondsLeft = timerSec) }
    }

    fun onEvent(event: ExecutionEvent) {
        when (event) {
            is ExecutionEvent.BlockAnswered -> {
                val updated = _uiState.value.blockAnswers + (event.blockId to event.answer)
                val nextIdx = _uiState.value.currentBlockIndex + 1
                _uiState.update { it.copy(blockAnswers = updated, currentBlockIndex = nextIdx) }
            }
            is ExecutionEvent.PhotoCaptured -> _uiState.update { it.copy(photoUrl = event.url) }
            is ExecutionEvent.TimerStarted  -> _uiState.update { it.copy(timerRunning = true) }
            is ExecutionEvent.TimerTick     -> _uiState.update { it.copy(timerSecondsLeft = event.secondsLeft) }
            is ExecutionEvent.TimerFinished -> _uiState.update { it.copy(timerRunning = false) }
            is ExecutionEvent.SubmitTask    -> submitTask()
            is ExecutionEvent.DismissResult -> _uiState.update { it.copy(result = null, showSuccessAnim = false) }
        }
    }

    private fun submitTask() {
        val state = _uiState.value
        _uiState.update { it.copy(isCompleting = true) }
        viewModelScope.launch {
            // Get the real userId from somewhere - you need to pass it to ViewModel
            // For now, get it from current Firebase user
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "demo_user"

            val result = completeTaskUseCase(
                task     = state.task,
                userId   = userId,  // ← Use real userId, not "demo_user"
                photoUrl = state.photoUrl
            )

            // ← NEW: Check for achievements
            if (result is CompletionResult.Success) {
                Log.d("ExecutionVM", "Task completed, checking for new badges...")
                try {
                    val newBadges = achievementRepository.checkAndUnlockAchievements(userId)
                    Log.d("ExecutionVM", "New badges: ${newBadges.size}")
                    _uiState.update {
                        it.copy(
                            newBadgesUnlocked = newBadges
                        )
                    }
                } catch (e: Exception) {
                    Log.e("ExecutionVM", "Error checking badges", e)
                }
            }

            _uiState.update {
                it.copy(
                    isCompleting   = false,
                    result         = result,
                    showSuccessAnim = result is CompletionResult.Success
                )
            }
        }
    }
}