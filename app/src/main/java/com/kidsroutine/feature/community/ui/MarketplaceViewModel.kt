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

data class MarketplaceUiState(
    val isLoading: Boolean = false,
    val activeTab: MarketplaceTab = MarketplaceTab.TASKS,
    val tasks: List<SharedTask> = emptyList(),
    val challenges: List<SharedChallenge> = emptyList(),
    val selectedCategory: TaskCategory? = null,
    val selectedDifficulty: DifficultyLevel? = null,
    val isImporting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

enum class MarketplaceTab {
    TASKS,
    CHALLENGES
}

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    fun loadMarketplace() {
        Log.d("MarketplaceVM", "Loading marketplace")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val tasks = communityRepository.getApprovedTasks(
                    category = _uiState.value.selectedCategory,
                    difficulty = _uiState.value.selectedDifficulty,
                    limit = 50
                )
                val challenges = communityRepository.getApprovedChallenges(
                    category = _uiState.value.selectedCategory,
                    difficulty = _uiState.value.selectedDifficulty,
                    limit = 50
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    tasks = tasks,
                    challenges = challenges,
                    error = null
                )

                Log.d("MarketplaceVM", "Loaded ${tasks.size} tasks and ${challenges.size} challenges")
            } catch (e: Exception) {
                Log.e("MarketplaceVM", "Error loading marketplace", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load marketplace"
                )
            }
        }
    }

    fun selectTab(tab: MarketplaceTab) {
        Log.d("MarketplaceVM", "Selecting tab: $tab")
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun filterByCategory(category: TaskCategory?) {
        Log.d("MarketplaceVM", "Filtering by category: $category")
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadMarketplace()
    }

    fun filterByDifficulty(difficulty: DifficultyLevel?) {
        Log.d("MarketplaceVM", "Filtering by difficulty: $difficulty")
        _uiState.value = _uiState.value.copy(selectedDifficulty = difficulty)
        loadMarketplace()
    }

    fun importTask(userId: String, taskId: String) {
        Log.d("MarketplaceVM", "Importing task: $taskId")
        _uiState.value = _uiState.value.copy(isImporting = true, error = null)

        viewModelScope.launch {
            try {
                communityRepository.importTask(userId, taskId)
                Log.d("MarketplaceVM", "Task imported successfully")

                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    successMessage = "✅ Task imported to your library!"
                )
            } catch (e: Exception) {
                Log.e("MarketplaceVM", "Error importing task", e)
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = e.message ?: "Failed to import task"
                )
            }
        }
    }

    fun importChallenge(userId: String, challengeId: String) {
        Log.d("MarketplaceVM", "Importing challenge: $challengeId")
        _uiState.value = _uiState.value.copy(isImporting = true, error = null)

        viewModelScope.launch {
            try {
                communityRepository.importChallenge(userId, challengeId)
                Log.d("MarketplaceVM", "Challenge imported successfully")

                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    successMessage = "✅ Challenge imported to your library!"
                )
            } catch (e: Exception) {
                Log.e("MarketplaceVM", "Error importing challenge", e)
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = e.message ?: "Failed to import challenge"
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