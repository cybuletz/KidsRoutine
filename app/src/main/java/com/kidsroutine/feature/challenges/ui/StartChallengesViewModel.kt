package com.kidsroutine.feature.challenges.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.ChallengeModel
import com.kidsroutine.feature.challenges.data.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StartChallengesUiState(
    val isLoading: Boolean = false,
    val availableChallenges: List<ChallengeModel> = emptyList(),
    val selectedCategory: String = "ALL",
    val filteredChallenges: List<ChallengeModel> = emptyList(),
    val error: String? = null,
    val isStarting: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class StartChallengesViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StartChallengesUiState())
    val uiState: StateFlow<StartChallengesUiState> = _uiState.asStateFlow()

    fun loadAvailableChallenges(familyId: String) {
        Log.d("StartChallengesVM", "Loading available challenges for family: $familyId")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // Load both system and family challenges
                val systemChallenges = challengeRepository.getSystemChallenges()
                val familyChallenges = challengeRepository.getFamilyChallenges(familyId)

                val allChallenges = systemChallenges + familyChallenges

                Log.d("StartChallengesVM", "Loaded ${allChallenges.size} challenges (${systemChallenges.size} system + ${familyChallenges.size} family)")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    availableChallenges = allChallenges,
                    filteredChallenges = allChallenges,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("StartChallengesVM", "Error loading challenges", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load challenges"
                )
            }
        }
    }

    fun filterByCategory(category: String) {
        Log.d("StartChallengesVM", "Filtering by category: $category")

        val filtered = if (category == "ALL") {
            _uiState.value.availableChallenges
        } else {
            _uiState.value.availableChallenges.filter {
                it.category.name == category
            }
        }

        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            filteredChallenges = filtered
        )
    }

    fun startChallenge(userId: String, challengeId: String) {
        Log.d("StartChallengesVM", "Starting challenge: $challengeId for user: $userId")
        _uiState.value = _uiState.value.copy(isStarting = true, error = null)

        viewModelScope.launch {
            try {
                challengeRepository.startChallenge(userId, challengeId)
                Log.d("StartChallengesVM", "Challenge started successfully")

                _uiState.value = _uiState.value.copy(
                    isStarting = false,
                    successMessage = "Challenge started! 🎉"
                )
            } catch (e: Exception) {
                Log.e("StartChallengesVM", "Error starting challenge", e)
                _uiState.value = _uiState.value.copy(
                    isStarting = false,
                    error = e.message ?: "Failed to start challenge"
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