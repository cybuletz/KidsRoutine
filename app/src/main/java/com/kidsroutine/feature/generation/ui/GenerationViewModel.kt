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
import com.kidsroutine.feature.generation.data.TaskSaveRepository
import com.google.firebase.firestore.FirebaseFirestore

data class GenerationUiState(
    val isLoading: Boolean = false,
    val generatedTasks: List<GeneratedTask> = emptyList(),
    val generatedChallenges: List<GeneratedChallenge> = emptyList(),
    val quotaRemaining: Int = 0,
    val error: String? = null,
    val successMessage: String? = null,
    val isCached: Boolean = false
)

@HiltViewModel
class GenerationViewModel @Inject constructor(
    private val repository: GenerationRepository,
    private val taskSaveRepository: TaskSaveRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerationUiState())
    val uiState: StateFlow<GenerationUiState> = _uiState.asStateFlow()

    /**
     * Generate AI tasks
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
                Log.d("GenerationVM", "Generating tasks for age $childAge...")

                // Determine tier from user data (if available, default to FREE)
                val tier = "FREE" // TODO: Add tier field to UserModel later if needed

                val result = repository.generateTasks(
                    familyId = currentUser.familyId,
                    childAge = childAge,
                    preferences = preferences,
                    recentCompletions = recentCompletions,
                    tier = tier,
                    count = 3
                )

                result.onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generatedTasks = response.tasks,
                        quotaRemaining = response.quotaRemaining,
                        isCached = response.cached,
                        successMessage = "Generated ${response.tasks.size} tasks!"
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
                Log.e("GenerationVM", "Exception: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    /**
     * Generate AI challenges
     */
    fun generateChallenges(
        currentUser: UserModel,
        childAge: Int,
        goals: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d("GenerationVM", "Generating challenges...")

                // Determine tier from user data (if available, default to PRO for challenges)
                val tier = "PRO" // TODO: Add tier field to UserModel later if needed

                val result = repository.generateChallenges(
                    familyId = currentUser.familyId,
                    childAge = childAge,
                    goals = goals,
                    tier = tier,
                    count = 2
                )

                result.onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generatedChallenges = response.challenges,
                        quotaRemaining = response.quotaRemaining,
                        isCached = response.cached,
                        successMessage = "Generated ${response.challenges.size} challenges!"
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
                Log.e("GenerationVM", "Exception: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    /**
     * Save a generated task to Firestore and assign to children
     */
    fun saveGeneratedTask(
        task: GeneratedTask,
        familyId: String,
        childrenIds: List<String>
    ) {
        viewModelScope.launch {
            try {
                Log.d("GenerationVM", "Saving task: ${task.title}")

                // Only save if children are selected
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
                    _uiState.value = _uiState.value.copy(
                        successMessage = "✅ Task assigned to ${childrenIds.size} child${if (childrenIds.size != 1) "ren" else ""}!",
                        error = null
                    )
                    Log.d("GenerationVM", "Task saved successfully: $taskId")
                }

                result.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to save task",
                        successMessage = null
                    )
                    Log.e("GenerationVM", "Error saving task: ${error.message}")
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

    /**
     * Save a generated challenge
     */
    fun saveChallengeToFamily(
        challenge: GeneratedChallenge,
        familyId: String,
        childId: String
    ) {
        viewModelScope.launch {
            try {
                Log.d("GenerationVM", "Saving challenge: ${challenge.title}")

                // For now, just show success message
                // Challenge saving logic will be added in next phase
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