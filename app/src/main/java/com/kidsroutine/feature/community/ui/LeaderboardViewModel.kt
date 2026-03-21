package com.kidsroutine.feature.community.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.ChildLeaderboardEntry
import com.kidsroutine.core.model.FamilyLeaderboardEntry
import com.kidsroutine.core.model.ChallengeLeaderboardEntry
import com.kidsroutine.core.model.LeaderboardEntry
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
    val myFamilyLeaderboard: List<LeaderboardEntry> = emptyList(),
    val error: String? = null
)

enum class LeaderboardTab {
    CHILDREN,
    FAMILIES,
    CHALLENGES,
    MY_FAMILY
}

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    fun loadLeaderboards() {
        Log.d("LeaderboardVM", "Loading global leaderboards")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                Log.d("LeaderboardVM", "Fetching child leaderboard...")
                val childLeaderboard = communityRepository.getChildLeaderboard(limit = 100)
                Log.d("LeaderboardVM", "Fetched ${childLeaderboard.size} children")

                Log.d("LeaderboardVM", "Fetching family leaderboard...")
                val familyLeaderboard = communityRepository.getFamilyLeaderboard(limit = 50)
                Log.d("LeaderboardVM", "Fetched ${familyLeaderboard.size} families")

                Log.d("LeaderboardVM", "Fetching challenge leaderboard...")
                val challengeLeaderboard = communityRepository.getChallengeLeaderboard(limit = 50)
                Log.d("LeaderboardVM", "Fetched ${challengeLeaderboard.size} challenges")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    childLeaderboard = childLeaderboard,
                    familyLeaderboard = familyLeaderboard,
                    challengeLeaderboard = challengeLeaderboard,
                    error = null
                )

                Log.d("LeaderboardVM", "✓ All global leaderboards loaded successfully")
            } catch (e: Exception) {
                Log.e("LeaderboardVM", "Error loading global leaderboards", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load leaderboards"
                )
            }
        }
    }

    fun loadMyFamilyLeaderboard(familyId: String) {
        if (familyId.isEmpty()) {
            Log.w("LeaderboardVM", "Family ID is empty, skipping family leaderboard load")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("LeaderboardVM", "Loading my family leaderboard for: $familyId")

                // Fetch all family members' XP and create leaderboard
                val myFamilyLeaderboard = communityRepository.getFamilyLeaderboard(limit = 100)
                    .filter { it.familyId == familyId }
                    .mapIndexed { index, entry ->
                        LeaderboardEntry(
                            rank = index + 1,
                            userId = entry.familyName,  // Store family name as userId for display
                            displayName = entry.familyName,
                            avatarUrl = "",
                            xp = entry.totalXp,
                            level = 1,
                            weeklyXp = entry.totalXp,
                            badges = 0
                        )
                    }

                Log.d("LeaderboardVM", "Loaded ${myFamilyLeaderboard.size} family members")

                _uiState.value = _uiState.value.copy(
                    myFamilyLeaderboard = myFamilyLeaderboard,
                    error = null
                )

                Log.d("LeaderboardVM", "✓ Family leaderboard loaded successfully")
            } catch (e: Exception) {
                Log.e("LeaderboardVM", "Error loading family leaderboard", e)
                _uiState.value = _uiState.value.copy(
                    myFamilyLeaderboard = emptyList(),
                    error = e.message ?: "Failed to load family leaderboard"
                )
            }
        }
    }

    fun selectTab(tab: LeaderboardTab) {
        Log.d("LeaderboardVM", "Selecting tab: ${tab.name}")
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }
}