package com.kidsroutine.feature.generation.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.generation.data.GeneratedChallenge
import com.kidsroutine.feature.generation.data.GeneratedTask
import com.kidsroutine.feature.generation.data.GenerationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.kidsroutine.feature.generation.data.TaskSaveRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel

data class GenerationUiState(
    val isLoading: Boolean = false,
    val generatedTasks: List<GeneratedTask> = emptyList(),
    val generatedChallenges: List<GeneratedChallenge> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val quotaRemaining: Int = 3,  // ← CHANGE: Default to 3 for FREE tier
    val isCached: Boolean = false,
    val selectedDifficulty: String = "MEDIUM",
    val selectedPreferences: Set<String> = setOf("🎨 Creative"),
    val selectedGoals: Set<String> = setOf("🏃 Health"),
    val generationHistory: List<GeneratedTask> = emptyList()
)

@HiltViewModel
class GenerationViewModel @Inject constructor(
    private val repository: GenerationRepository,
    private val taskSaveRepository: TaskSaveRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerationUiState())
    val uiState: StateFlow<GenerationUiState> = _uiState.asStateFlow()

    // ════════════════════════════════════════════════════════════════════════
    // PREFERENCE MANAGEMENT
    // ════════════════════════════════════════════════════════════════════════

    fun toggleDifficulty(difficulty: String) {
        _uiState.value = _uiState.value.copy(selectedDifficulty = difficulty)
        Log.d("GenerationVM", "Difficulty selected: $difficulty")
    }

    fun togglePreference(preference: String) {
        val current = _uiState.value.selectedPreferences.toMutableSet()
        if (preference in current) {
            current.remove(preference)
        } else {
            current.add(preference)
        }
        _uiState.value = _uiState.value.copy(selectedPreferences = current)
        Log.d("GenerationVM", "Preferences updated: $current")
    }

    fun toggleGoal(goal: String) {
        val current = _uiState.value.selectedGoals.toMutableSet()
        if (goal in current) {
            current.remove(goal)
        } else {
            current.add(goal)
        }
        _uiState.value = _uiState.value.copy(selectedGoals = current)
        Log.d("GenerationVM", "Goals updated: $current")
    }

    // ════════════════════════════════════════════════════════════════════════
    // TASK GENERATION
    // ════════════════════════════════════════════════════════════════════════

    fun generateTasks(
        currentUser: UserModel,
        childAge: Int,
        recentCompletions: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d("GenerationVM", "Generating tasks for age $childAge...")

                // Check quota first
                if (_uiState.value.quotaRemaining <= 0) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "🚫 Daily quota reached. Try again tomorrow or upgrade to PRO!"
                    )
                    return@launch
                }

                val preferences = _uiState.value.selectedPreferences.toList()
                val tier = "FREE"  // TODO: Add tier field to UserModel

                val result = repository.generateTasks(
                    familyId = currentUser.familyId,
                    childAge = childAge,
                    preferences = preferences,
                    recentCompletions = recentCompletions,
                    tier = tier,
                    count = 1  // ✅ Generate 1 task per click
                )

                result.onSuccess { response ->
                    // Track in history
                    val newHistory = (_uiState.value.generationHistory + response.tasks).takeLast(10)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generatedTasks = response.tasks,
                        quotaRemaining = response.quotaRemaining,
                        isCached = response.cached,
                        generationHistory = newHistory,
                        successMessage = "✅ Generated ${response.tasks.size} tasks!"
                    )
                    Log.d("GenerationVM", "Success: ${response.tasks.size} tasks")
                }

                result.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to generate tasks"
                    )
                    Log.e("GenerationVM", "Error: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
                Log.e("GenerationVM", "Exception: ${e.message}", e)
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // CHALLENGE GENERATION
    // ════════════════════════════════════════════════════════════════════════

    fun generateChallenges(
        currentUser: UserModel,
        childAge: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d("GenerationVM", "Generating challenges...")

                val goals = _uiState.value.selectedGoals.toList()
                val tier = "PRO"  // Challenges require PRO

                val result = repository.generateChallenges(
                    familyId = currentUser.familyId,
                    childAge = childAge,
                    goals = goals,
                    tier = tier,
                    count = 1  // ✅ Generate 1 challenge per click
                )

                result.onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generatedChallenges = response.challenges,
                        quotaRemaining = response.quotaRemaining,
                        isCached = response.cached,
                        successMessage = "✅ Generated ${response.challenges.size} challenges!"
                    )
                    Log.d("GenerationVM", "Success: ${response.challenges.size} challenges")
                }

                result.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to generate challenges"
                    )
                    Log.e("GenerationVM", "Error: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
                Log.e("GenerationVM", "Exception: ${e.message}", e)
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // TASK SAVING & MANAGEMENT
    // ════════════════════════════════════════════════════════════════════════

    fun saveGeneratedTask(
        task: GeneratedTask,
        familyId: String,
        childrenIds: List<String>
    ) {
        viewModelScope.launch {
            try {
                Log.d("GenerationVM", "Saving task: ${task.title}")

                if (childrenIds.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        error = "Please select at least one child",
                        successMessage = null
                    )
                    return@launch
                }

                val result = taskSaveRepository.saveAndAssignToFamily(
                    generatedTask = task,
                    familyId = familyId,
                    childrenIds = childrenIds
                )

                result.onSuccess { taskId ->
                    // Add to favorites
                    addToFavorites(task)

                    _uiState.value = _uiState.value.copy(
                        successMessage = "✅ Task assigned to ${childrenIds.size} child${if (childrenIds.size != 1) "ren" else ""}!",
                        error = null
                    )
                    Log.d("GenerationVM", "Task saved: $taskId")
                }

                result.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to save task",
                        successMessage = null
                    )
                    Log.e("GenerationVM", "Error saving: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error: ${e.message}",
                    successMessage = null
                )
                Log.e("GenerationVM", "Exception: ${e.message}", e)
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // FAVORITES & HISTORY
    // ════════════════════════════════════════════════════════════════════════

    fun addToFavorites(task: GeneratedTask) {
        // TODO: Save to Firestore favorites collection
        Log.d("GenerationVM", "Added to favorites: ${task.title}")
    }

    fun shareToMarketplace(task: GeneratedTask, familyId: String) {
        // TODO: Post to marketplace collection with status=PENDING for moderation
        Log.d("GenerationVM", "Shared to marketplace: ${task.title}")
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    fun saveChallengeToFamily(
        challenge: GeneratedChallenge,
        familyId: String,
        childId: String
    ) {
        viewModelScope.launch {
            try {
                Log.d("GenerationVM", "Saving challenge: ${challenge.title}")
                _uiState.value = _uiState.value.copy(
                    successMessage = "✅ Challenge saved! Assign to your child to start.",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error: ${e.message}",
                    successMessage = null
                )
            }
        }
    }
}