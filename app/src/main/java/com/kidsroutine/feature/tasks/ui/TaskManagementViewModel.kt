package com.kidsroutine.feature.tasks.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.feature.tasks.data.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskManagementUiState(
    val isLoading: Boolean = false,
    val tasks: List<TaskModel> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class TaskManagementViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskManagementUiState())
    val uiState: StateFlow<TaskManagementUiState> = _uiState.asStateFlow()

    // ── Real-time listener — replaces the one-shot suspend call ──────────
    // Called from TaskListScreen / ParentTasksTab LaunchedEffect
    fun observeFamilyTasks(familyId: String) {
        if (familyId.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Invalid family ID")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            taskRepository.observeFamilyTasks(familyId)
                .catch { e ->
                    Log.e("TaskManagementViewModel", "Real-time listener error", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = e.message ?: "Failed to load tasks"
                    )
                }
                .collect { tasks ->
                    Log.d("TaskManagementViewModel", "Real-time update: ${tasks.size} tasks")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tasks     = tasks,
                        error     = null
                    )
                }
        }
    }

    // ── Keep loadFamilyTasks as a one-shot fallback for screens that need it ──
    fun loadFamilyTasks(familyId: String) {
        if (familyId.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Invalid family ID")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                Log.d("TaskManagementViewModel", "One-shot load for family: $familyId")
                val tasks = taskRepository.getFamilyTasks(familyId)
                Log.d("TaskManagementViewModel", "Loaded ${tasks.size} tasks")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    tasks     = tasks,
                    error     = null
                )
            } catch (e: Exception) {
                Log.e("TaskManagementViewModel", "Error loading tasks", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Failed to load tasks",
                    tasks     = emptyList()
                )
            }
        }
    }

    fun createTask(familyId: String, task: TaskModel) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                Log.d("TaskManagementViewModel", "Creating task: ${task.title}")
                taskRepository.createTask(familyId, task)
                Log.d("TaskManagementViewModel", "Task created successfully")
                // No manual state update needed — observeFamilyTasks flow will push the update
                _uiState.value = _uiState.value.copy(
                    isLoading      = false,
                    successMessage = "Task created successfully!"
                )
            } catch (e: Exception) {
                Log.e("TaskManagementViewModel", "Error creating task", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Failed to create task"
                )
            }
        }
    }

    fun deleteTask(familyId: String, taskId: String) {
        viewModelScope.launch {
            try {
                Log.d("TaskManagementViewModel", "Deleting task: $taskId")
                taskRepository.deleteTask(familyId, taskId)
                Log.d("TaskManagementViewModel", "Task deleted successfully")
                // Flow will push the removal automatically
                _uiState.value = _uiState.value.copy(successMessage = "Task deleted!")
            } catch (e: Exception) {
                Log.e("TaskManagementViewModel", "Error deleting task", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete task"
                )
            }
        }
    }

    fun updateTask(familyId: String, task: TaskModel) {
        viewModelScope.launch {
            try {
                Log.d("TaskManagementViewModel", "Updating task: ${task.id}")
                taskRepository.updateTask(familyId, task)
                Log.d("TaskManagementViewModel", "Task updated successfully")
                // Flow will push the updated doc automatically
                _uiState.value = _uiState.value.copy(successMessage = "Task updated!")
            } catch (e: Exception) {
                Log.e("TaskManagementViewModel", "Error updating task", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update task"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, error = null)
    }
}
