package com.kidsroutine.feature.tasks.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.feature.generation.data.GeneratedChallenge
import com.kidsroutine.feature.tasks.data.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChildItemUiState(
    val tasks: List<TaskModel> = emptyList(),
    val challenges: List<GeneratedChallenge> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChildItemViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildItemUiState())
    val uiState: StateFlow<ChildItemUiState> = _uiState.asStateFlow()

    private var childId = ""
    private var familyId = ""

    fun loadChildItems(childId: String, familyId: String) {
        this.childId = childId
        this.familyId = familyId

        Log.d("ChildItemVM", "Setting up real-time listeners for child: $childId")

        // Listen to tasks in real-time
        viewModelScope.launch {
            try {
                taskRepository.observeChildAssignedTasks(childId, familyId)
                    .collect { tasks ->
                        Log.d("ChildItemVM", "Tasks updated: ${tasks.size}")
                        _uiState.value = _uiState.value.copy(
                            tasks = tasks,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                Log.e("ChildItemVM", "Error loading tasks: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load items"
                )
            }
        }

        // Listen to challenges in real-time
        viewModelScope.launch {
            try {
                taskRepository.observeChildAssignedChallenges(childId, familyId)
                    .collect { challenges ->
                        Log.d("ChildItemVM", "Challenges updated: ${challenges.size}")
                        _uiState.value = _uiState.value.copy(challenges = challenges)
                    }
            } catch (e: Exception) {
                Log.e("ChildItemVM", "Error loading challenges: ${e.message}", e)
            }
        }

        // Listen for refresh events from notifications
        viewModelScope.launch {
            RefreshEventManager.refreshEvent.collect {
                Log.d("ChildItemVM", "Refresh event received!")
                // Real-time listeners will automatically update
            }
        }
    }
}