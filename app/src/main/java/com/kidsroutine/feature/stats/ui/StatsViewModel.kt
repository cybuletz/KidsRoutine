package com.kidsroutine.feature.stats.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.feature.stats.data.FamilyStatsModel
import com.kidsroutine.feature.stats.data.StatsRepository
import com.kidsroutine.feature.stats.data.UserStatsModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val isLoading: Boolean = false,
    val userStats: UserStatsModel? = null,
    val familyStats: FamilyStatsModel? = null,
    val weeklyProgress: List<Int> = emptyList(),
    val monthlyProgress: List<Int> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    fun loadUserStats(userId: String) {
        Log.d("StatsVM", "Loading stats for user: $userId")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val userStats = statsRepository.getUserStats(userId)
                val weeklyProgress = statsRepository.getWeeklyProgress(userId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userStats = userStats,
                    weeklyProgress = weeklyProgress
                )

                Log.d("StatsVM", "User stats loaded: $userStats")
            } catch (e: Exception) {
                Log.e("StatsVM", "Error loading user stats", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load stats"
                )
            }
        }
    }

    fun loadFamilyStats(familyId: String) {
        Log.d("StatsVM", "Loading stats for family: $familyId")

        viewModelScope.launch {
            try {
                val familyStats = statsRepository.getFamilyStats(familyId)

                _uiState.value = _uiState.value.copy(
                    familyStats = familyStats
                )

                Log.d("StatsVM", "Family stats loaded: $familyStats")
            } catch (e: Exception) {
                Log.e("StatsVM", "Error loading family stats", e)
            }
        }
    }

    fun loadMonthlyProgress(userId: String) {
        Log.d("StatsVM", "Loading monthly progress for: $userId")

        viewModelScope.launch {
            try {
                val monthlyProgress = statsRepository.getMonthlyProgress(userId)

                _uiState.value = _uiState.value.copy(
                    monthlyProgress = monthlyProgress
                )

                Log.d("StatsVM", "Monthly progress loaded: $monthlyProgress")
            } catch (e: Exception) {
                Log.e("StatsVM", "Error loading monthly progress", e)
            }
        }
    }
}