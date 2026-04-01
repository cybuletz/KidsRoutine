package com.kidsroutine.feature.challenges.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.ChallengeModel
import com.kidsroutine.core.model.ChallengeProgress
import com.kidsroutine.core.model.ChallengeStatus
import com.kidsroutine.core.engine.challenge_engine.ChallengeEngine
import com.kidsroutine.core.common.util.DateUtils
import com.kidsroutine.feature.challenges.data.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChallengeDetailUiState(
    val isLoading: Boolean = false,
    val challenge: ChallengeModel? = null,
    val progress: ChallengeProgress? = null,
    val dailyProgress: Map<String, Boolean> = emptyMap(),
    val error: String? = null,
    val completedToday: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class ChallengeDetailViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val challengeEngine: ChallengeEngine,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeDetailUiState())
    val uiState: StateFlow<ChallengeDetailUiState> = _uiState.asStateFlow()

    fun loadChallengeDetail(userId: String, familyId: String, challengeId: String) {
        Log.d("ChallengeDetailVM", "Loading challenge detail: $challengeId for user: $userId")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val challenge = challengeRepository.getChallenge(challengeId)
                val progress = challengeRepository.getChallengeProgress(userId, familyId, challengeId)

                if (challenge == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Challenge not found"
                    )
                    return@launch
                }

                if (progress == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Challenge progress not found"
                    )
                    return@launch
                }

                val today = DateUtils.todayString()
                val completedToday = progress.dailyProgress[today] ?: false

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    challenge = challenge,
                    progress = progress,
                    dailyProgress = progress.dailyProgress,
                    completedToday = completedToday,
                    error = null
                )

                Log.d("ChallengeDetailVM", "Challenge loaded: ${challenge.title}, completed today: $completedToday")
            } catch (e: Exception) {
                Log.e("ChallengeDetailVM", "Error loading challenge detail", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load challenge"
                )
            }
        }
    }

    fun completeDayToday(userId: String, familyId: String) {
        Log.d("ChallengeDetailVM", "Completing day for user: $userId")

        val challenge = _uiState.value.challenge ?: return
        val progress = _uiState.value.progress ?: return

        viewModelScope.launch {
            try {
                val today = DateUtils.todayString()
                val (updatedProgress, newStatus) = challengeEngine.recordDailyProgress(
                    challenge = challenge,
                    progress = progress,
                    completed = true,
                    date = today
                )

                challengeRepository.updateChallengeProgress(updatedProgress, familyId)

                _uiState.value = _uiState.value.copy(
                    progress = updatedProgress,
                    completedToday = true,
                    successMessage = when (newStatus) {
                        ChallengeStatus.COMPLETED -> "🎉 Challenge completed! +${challenge.completionBonusXp} XP!"
                        else -> "✅ Day completed! +${challengeEngine.calculateDailyXp(challenge, updatedProgress, streakBonus = true)} XP"
                    },
                    error = null
                )

                Log.d("ChallengeDetailVM", "Day completed. New status: $newStatus")
            } catch (e: Exception) {
                Log.e("ChallengeDetailVM", "Error completing day", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to complete day"
                )
            }
        }
    }

    fun skipDayToday(userId: String, familyId: String) {

        Log.d("ChallengeDetailVM", "Skipping day for user: $userId")

        val challenge = _uiState.value.challenge ?: return
        val progress = _uiState.value.progress ?: return

        viewModelScope.launch {
            try {
                val today = DateUtils.todayString()
                val (updatedProgress, newStatus) = challengeEngine.recordDailyProgress(
                    challenge = challenge,
                    progress = progress,
                    completed = false,
                    date = today
                )

                challengeRepository.updateChallengeProgress(updatedProgress, familyId)

                _uiState.value = _uiState.value.copy(
                    progress = updatedProgress,
                    completedToday = false,
                    successMessage = when (newStatus) {
                        ChallengeStatus.FAILED -> "⚠️ Challenge failed. Don't give up!"
                        else -> "📝 Day skipped. Try again tomorrow!"
                    },
                    error = null
                )

                Log.d("ChallengeDetailVM", "Day skipped. New status: $newStatus")
            } catch (e: Exception) {
                Log.e("ChallengeDetailVM", "Error skipping day", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to skip day"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            error = null
        )
    }
}