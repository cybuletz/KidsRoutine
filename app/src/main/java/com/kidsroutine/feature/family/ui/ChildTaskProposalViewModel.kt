package com.kidsroutine.feature.family.ui

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
import java.util.UUID

data class ChildTaskProposalUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ChildTaskProposalViewModel @Inject constructor(
    private val familyRepository: FamilyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildTaskProposalUiState())
    val uiState: StateFlow<ChildTaskProposalUiState> = _uiState.asStateFlow()

    fun proposeTask(
        familyId: String,
        childId: String,
        title: String,
        description: String,
        xpReward: Int
    ) {
        if (familyId.isEmpty() || childId.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Invalid family or child ID")
            return
        }

        if (title.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Task title is required")
            return
        }

        _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)

        viewModelScope.launch {
            try {
                Log.d("ChildTaskProposalVM", "Proposing task: $title from child: $childId")

                // Create proposal task
                val proposalTask = TaskModel(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    reward = com.kidsroutine.core.model.TaskReward(xp = xpReward),
                    category = com.kidsroutine.core.model.TaskCategory.FAMILY,
                    difficulty = com.kidsroutine.core.model.DifficultyLevel.EASY,
                    createdBy = com.kidsroutine.core.model.TaskCreator.CHILD,
                    familyId = familyId,
                    requiresParent = true
                )

                familyRepository.proposeChildTask(familyId, childId, proposalTask)

                Log.d("ChildTaskProposalVM", "Task proposed successfully")
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    successMessage = "Task proposal sent to parent! 🎉"
                )
            } catch (e: Exception) {
                Log.e("ChildTaskProposalVM", "Error proposing task", e)
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "Failed to propose task"
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