package com.kidsroutine.feature.achievements.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.Badge
import com.kidsroutine.core.model.UserAchievements
import com.kidsroutine.feature.achievements.data.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementsUiState(
    val isLoading: Boolean = false,
    val achievements: UserAchievements = UserAchievements(),
    val unlockedBadges: List<Badge> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    fun loadAchievements(userId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                Log.d("AchievementsVM", "Loading achievements for user: $userId")
                achievementRepository.observeUserAchievements(userId)
                    .collect { achievements ->
                        Log.d("AchievementsVM", "Loaded ${achievements.badges.size} badges")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            achievements = achievements
                        )
                    }
            } catch (e: Exception) {
                Log.e("AchievementsVM", "Error loading achievements", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load achievements"
                )
            }
        }
    }

    fun checkNewAchievements(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("AchievementsVM", "Checking new achievements for user: $userId")
                val newBadges = achievementRepository.checkAndUnlockAchievements(userId)
                if (newBadges.isNotEmpty()) {
                    Log.d("AchievementsVM", "Unlocked ${newBadges.size} new badges!")
                    _uiState.value = _uiState.value.copy(
                        unlockedBadges = newBadges
                    )
                }
            } catch (e: Exception) {
                Log.e("AchievementsVM", "Error checking achievements", e)
            }
        }
    }
}