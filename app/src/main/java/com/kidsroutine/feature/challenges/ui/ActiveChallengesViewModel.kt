package com.kidsroutine.feature.challenges.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.ChallengeModel
import com.kidsroutine.core.model.ChallengeProgress
import com.kidsroutine.core.model.ChallengeStatus
import com.kidsroutine.feature.challenges.data.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveChallengesUiState(
    val isLoading: Boolean = false,
    val activeChallenges: List<Pair<ChallengeModel, ChallengeProgress>> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ActiveChallengesViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveChallengesUiState())
    val uiState: StateFlow<ActiveChallengesUiState> = _uiState.asStateFlow()

    fun loadActiveChallenges(userId: String, familyId: String, isParent: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(java.util.Date())

                val progressList = if (isParent) {
                    // Parent: fetch ALL progress, no status filter at all
                    challengeRepository.getAllChallengeProgress(userId, familyId)
                        .filter { progress ->
                            // Only exclude truly dead challenges (FAILED/ARCHIVED)
                            // and ones whose end date has already passed
                            progress.status != ChallengeStatus.FAILED &&
                                    progress.status != ChallengeStatus.ARCHIVED &&
                                    (progress.endDate.isEmpty() || progress.endDate >= today)
                        }
                } else {
                    // Child: only ACTIVE status, and hide today's already-done ones
                    challengeRepository.getActiveChallenges(userId, familyId)
                        .filter { it.lastCompletedDate != today }
                }

                val pairs = progressList.mapNotNull { progress ->
                    val challenge = challengeRepository.getChallenge(progress.challengeId)
                    if (challenge != null) Pair(challenge, progress) else null
                }

                _uiState.value = _uiState.value.copy(
                    isLoading        = false,
                    activeChallenges = pairs
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message
                )
            }
        }
    }
}