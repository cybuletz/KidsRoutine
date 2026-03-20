package com.kidsroutine.feature.parent.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.feature.family.data.FamilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PendingTasksUiState(
    val isLoading: Boolean = false,
    val pendingTasks: List<TaskModel> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ParentPendingTasksViewModel @Inject constructor(
    private val familyRepository: FamilyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingTasksUiState())
    val uiState: StateFlow<PendingTasksUiState> = _uiState.asStateFlow()

    fun loadPendingTasks(familyId: String) {
        if (familyId.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Invalid family ID")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                Log.d("ParentPendingTasksVM", "Loading pending tasks for family: $familyId")
                val tasks = familyRepository.getPendingChildTasks(familyId)
                Log.d("ParentPendingTasksVM", "Loaded ${tasks.size} pending tasks")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pendingTasks = tasks,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("ParentPendingTasksVM", "Error loading pending tasks", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load pending tasks",
                    pendingTasks = emptyList()
                )
            }
        }
    }

    fun approveTask(familyId: String, taskId: String) {
        viewModelScope.launch {
            try {
                Log.d("ParentPendingTasksVM", "Approving task: $taskId")
                familyRepository.approveChildTask(familyId, taskId)
                Log.d("ParentPendingTasksVM", "Task approved successfully")

                _uiState.value = _uiState.value.copy(
                    pendingTasks = _uiState.value.pendingTasks.filter { it.id != taskId },
                    successMessage = "Task approved! ✓"
                )
            } catch (e: Exception) {
                Log.e("ParentPendingTasksVM", "Error approving task", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to approve task"
                )
            }
        }
    }

    fun rejectTask(familyId: String, taskId: String, reason: String = "") {
        viewModelScope.launch {
            try {
                Log.d("ParentPendingTasksVM", "Rejecting task: $taskId")
                familyRepository.rejectChildTask(familyId, taskId, reason)
                Log.d("ParentPendingTasksVM", "Task rejected successfully")

                _uiState.value = _uiState.value.copy(
                    pendingTasks = _uiState.value.pendingTasks.filter { it.id != taskId },
                    successMessage = "Task declined"
                )
            } catch (e: Exception) {
                Log.e("ParentPendingTasksVM", "Error rejecting task", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to reject task"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            error = null
        )
    }
}