package com.kidsroutine.feature.daily.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.EntitlementsRepository
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.generation.data.GeneratedTask
import com.kidsroutine.feature.generation.data.GenerationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiSuggestionUiState(
    val isLoading:   Boolean            = false,
    val suggestions: List<GeneratedTask> = emptyList(),
    val error:       String?            = null,
    val dismissed:   Boolean            = false,
    val quotaRemaining: Int             = -1   // -1 = not yet fetched
)

@HiltViewModel
class AiSuggestionViewModel @Inject constructor(
    private val generationRepository: GenerationRepository,
    private val entitlementsRepository: EntitlementsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiSuggestionUiState())
    val uiState: StateFlow<AiSuggestionUiState> = _uiState.asStateFlow()

    fun loadSuggestions(
        child: UserModel,
        completedTaskTitles: List<String> = emptyList()
    ) {
        if (_uiState.value.isLoading) return
        if (_uiState.value.suggestions.isNotEmpty()) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val entitlements = entitlementsRepository.getEntitlements(child.userId, child.familyId)
                val tier = entitlements.planType.name

                val result = generationRepository.generateTasks(
                    familyId          = child.familyId,
                    childAge          = runCatching { child.age }.getOrDefault(8),
                    preferences       = emptyList(),
                    recentCompletions = completedTaskTitles.takeLast(5),
                    tier              = tier,
                    count             = 3
                )

                result.onSuccess { response ->
                    Log.d("AiSuggestionVM", "Got ${response.tasks.size} suggestions (quota: ${response.quotaRemaining})")
                    _uiState.value = _uiState.value.copy(
                        isLoading      = false,
                        suggestions    = response.tasks,
                        quotaRemaining = response.quotaRemaining
                    )
                }

                result.onFailure { e ->
                    Log.e("AiSuggestionVM", "Suggestion error: ${e.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = e.message
                    )
                }
            } catch (e: Exception) {
                Log.e("AiSuggestionVM", "Exception: ${e.message}", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun dismiss() {
        _uiState.value = _uiState.value.copy(dismissed = true)
    }

    fun refresh(child: UserModel, completedTaskTitles: List<String> = emptyList()) {
        _uiState.value = AiSuggestionUiState()
        loadSuggestions(child, completedTaskTitles)
    }
}
