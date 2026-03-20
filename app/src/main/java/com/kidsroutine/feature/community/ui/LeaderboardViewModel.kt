package com.kidsroutine.feature.community.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.ChildLeaderboardEntry
import com.kidsroutine.core.model.FamilyLeaderboardEntry
import com.kidsroutine.core.model.ChallengeLeaderboardEntry
import com.kidsroutine.feature.community.data.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val activeTab: LeaderboardTab = LeaderboardTab.CHILDREN,
    val childLeaderboard: List<ChildLeaderboardEntry> = emptyList(),
    val familyLeaderboard: List<FamilyLeaderboardEntry> = emptyList(),
    val challengeLeaderboard: List<ChallengeLeaderboardEntry> = emptyList(),
    val error: String? = null
)

enum class LeaderboardTab {
    CHILDREN,
    FAMILIES,
    CHALLENGES
}

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    fun loadLeaderboards() {
        Log.d("LeaderboardVM", "Loading leaderboards")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val childLeaderboard = communityRepository.getChildLeaderboard(limit = 100)
                val familyLeaderboard = communityRepository.getFamilyLeaderboard(limit = 50)
                val challengeLeaderboard = communityRepository.getChallengeLeaderboard(limit = 50)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    childLeaderboard = childLeaderboard,
                    familyLeaderboard = familyLeaderboard,
                    challengeLeaderboard = challengeLeaderboard,
                    error = null
                )

                Log.d("LeaderboardVM", "Loaded ${childLeaderboard.size} children, ${familyLeaderboard.size} families")
            } catch (e: Exception) {
                Log.e("LeaderboardVM", "Error loading leaderboards", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load leaderboards"
                )
            }
        }
    }

    fun selectTab(tab: LeaderboardTab) {
        Log.d("LeaderboardVM", "Selecting tab: $tab")
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }
}