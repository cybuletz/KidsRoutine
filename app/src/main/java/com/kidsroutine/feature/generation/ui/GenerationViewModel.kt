package com.kidsroutine.feature.generation.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.generation.data.GeneratedChallenge
import com.kidsroutine.feature.generation.data.GeneratedTask
import com.kidsroutine.feature.generation.data.GenerationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for task/challenge generation
 */
data class GenerationUiState(
    val isLoading: Boolean = false,
    val generatedTasks: List<GeneratedTask> = emptyList(),
    val generatedChallenges: List<GeneratedChallenge> = emptyList(),
    val quotaRemaining: Int = 0,
    val error: String? = null,
    val successMessage: String? = null,
    val isCached: Boolean = false
)

/**
 * ViewModel for AI task & challenge generation
 * Calls Cloud Functions (Gemini-only)
 */
@HiltViewModel
class GenerationViewModel @Inject constructor(
    private val repository: GenerationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerationUiState())
    val uiState: StateFlow<GenerationUiState> = _uiState.asStateFlow()

    /**
     * Generate AI tasks via Cloud Function
     * Powered by Gemini API
     */
    fun generateTasks(
        currentUser: UserModel,
        childAge: Int,
        preferences: List<String> = emptyList(),
        recentCompletions: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d("GenerationVM", "⏳ Generating tasks for age $childAge via Gemini...")

                val result = repository.generateTasks(
                    familyId = currentUser.familyId,
                    childAge = childAge,
                    preferences = preferences,
                    recentCompletions = recentCompletions,
                    tier = currentUser.subscriptionTier ?: "FREE",
                    count = 3
                )

                result.onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generatedTasks = response.tasks,
                        quotaRemaining = response.quotaRemaining,
                        isCached = response.cached,
                        successMessage = "✅ Generated ${response.tasks.size} tasks! ${if (response.cached) "(Cached)" else ""}"
                    )
                    Log.d("GenerationVM", "✅ Success: ${response.tasks.size} tasks generated")
                }

                result.onFailure { error ->
                    val errorMsg = when {
                        error.message?.contains("PERMISSION_DENIED") == true ->
                            "❌ Task generation not available on your tier. Upgrade to PRO+"
                        error.message?.contains("RESOURCE_EXHAUSTED") == true ->
                            "❌ Daily quota reached. Try again tomorrow"
                        error.message?.contains("unauthenticated") == true ->
                            "❌ Please sign in to generate tasks"
                        else -> error.message ?: "Failed to generate tasks"
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                    Log.e("GenerationVM", "❌ Error: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("GenerationVM", "❌ Exception: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Generate AI challenges via Cloud Function
     * Powered by Gemini API
     * PRO+ tier only
     */
    fun generateChallenges(
        currentUser: UserModel,
        childAge: Int,
        goals: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d("GenerationVM", "⏳ Generating challenges for age $childAge via Gemini...")

                // Check if user has PRO tier
                if (currentUser.subscriptionTier != "PRO" && currentUser.subscriptionTier != "PREMIUM") {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "❌ Challenge generation requires PRO tier. Upgrade now!"
                    )
                    Log.w("GenerationVM", "User tier: ${currentUser.subscriptionTier} (requires PRO)")
                    return@launch
                }

                val result = repository.generateChallenges(
                    familyId = currentUser.familyId,
                    childAge = childAge,
                    goals = goals,
                    tier = currentUser.subscriptionTier!!,
                    count = 2
                )

                result.onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generatedChallenges = response.challenges,
                        quotaRemaining = response.quotaRemaining,
                        isCached = response.cached,
                        successMessage = "✅ Generated ${response.challenges.size} challenges! ${if (response.cached) "(Cached)" else ""}"
                    )
                    Log.d("GenerationVM", "✅ Success: ${response.challenges.size} challenges generated")
                }

                result.onFailure { error ->
                    val errorMsg = when {
                        error.message?.contains("PERMISSION_DENIED") == true ->
                            "❌ Challenge generation not available on FREE tier"
                        error.message?.contains("RESOURCE_EXHAUSTED") == true ->
                            "❌ Challenge quota reached. Try again tomorrow"
                        error.message?.contains("unauthenticated") == true ->
                            "❌ Please sign in to generate challenges"
                        else -> error.message ?: "Failed to generate challenges"
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                    Log.e("GenerationVM", "❌ Error: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("GenerationVM", "❌ Exception: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Clear error and success messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    /**
     * Clear generated tasks
     */
    fun clearGeneratedTasks() {
        _uiState.value = _uiState.value.copy(
            generatedTasks = emptyList()
        )
    }

    /**
     * Clear generated challenges
     */
    fun clearGeneratedChallenges() {
        _uiState.value = _uiState.value.copy(
            generatedChallenges = emptyList()
        )
    }
}