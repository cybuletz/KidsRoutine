package com.kidsroutine.feature.generation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.generation.data.DayMood
import com.kidsroutine.feature.generation.data.GeneratedDailyPlan
import com.kidsroutine.feature.generation.data.GenerationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyPlanUiState(
    val isLoading: Boolean = false,
    val plan: GeneratedDailyPlan? = null,
    val selectedMood: DayMood = DayMood.NORMAL,
    val error: String? = null,
    val quotaRemaining: Int = 3,
    val isCached: Boolean = false,
    val showResult: Boolean = false      // drives the cinematic reveal
)

@HiltViewModel
class DailyPlanViewModel @Inject constructor(
    private val repository: GenerationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyPlanUiState())
    val uiState: StateFlow<DailyPlanUiState> = _uiState.asStateFlow()

    fun selectMood(mood: DayMood) {
        _uiState.value = _uiState.value.copy(selectedMood = mood, plan = null, showResult = false)
    }

    fun generatePlan(currentUser: UserModel) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading  = true,
                error      = null,
                plan       = null,
                showResult = false
            )

            try {
                val mood = _uiState.value.selectedMood.name

                val result = repository.generateDailyPlan(
                    familyId    = currentUser.familyId,
                    childAge = 10,
                    preferences = emptyList(),
                    goals       = emptyList(),
                    tier        = "PRO",
                    mood        = mood
                )

                result.onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading      = false,
                        plan           = response.plan,
                        quotaRemaining = response.quotaRemaining,
                        isCached       = response.cached,
                        showResult     = true
                    )
                }

                result.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = error.message ?: "Failed to generate plan"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun reset() {
        _uiState.value = DailyPlanUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}