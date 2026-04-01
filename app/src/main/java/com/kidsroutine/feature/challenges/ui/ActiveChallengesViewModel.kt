package com.kidsroutine.feature.challenges.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.ChallengeModel
import com.kidsroutine.core.model.ChallengeProgress
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

    fun loadActiveChallenges(userId: String, familyId: String) {
        Log.d("ActiveChallengesVM", "Loading active challenges for user: $userId")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val progressList = challengeRepository.getActiveChallenges(userId, familyId)
                Log.d("ActiveChallengesVM", "Found ${progressList.size} active challenges")

                // Fetch full challenge details for each progress
                val challengesWithProgress = progressList.mapNotNull { progress ->
                    try {
                        val challenge = challengeRepository.getChallenge(progress.challengeId)
                        if (challenge != null) {
                            Pair(challenge, progress)
                        } else {
                            Log.w("ActiveChallengesVM", "Challenge not found: ${progress.challengeId}")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("ActiveChallengesVM", "Error fetching challenge: ${progress.challengeId}", e)
                        null
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activeChallenges = challengesWithProgress,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("ActiveChallengesVM", "Error loading challenges", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load challenges"
                )
            }
        }
    }
}