package com.kidsroutine.feature.rituals.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.FamilyRitual
import com.kidsroutine.core.model.RitualFrequency
import com.kidsroutine.core.model.RitualType
import com.kidsroutine.feature.rituals.data.RitualsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RitualsUiState(
    val rituals: List<FamilyRitual> = emptyList(),
    val isLoading: Boolean = false,
    val showCreateForm: Boolean = false,
    val selectedRitual: FamilyRitual? = null,
    val gratitudeText: String = "",
    val error: String? = null
)

@HiltViewModel
class RitualsViewModel @Inject constructor(
    private val ritualsRepository: RitualsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RitualsUiState())
    val uiState: StateFlow<RitualsUiState> = _uiState.asStateFlow()

    fun loadRituals(familyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val rituals = ritualsRepository.getRituals(familyId)
                _uiState.value = _uiState.value.copy(
                    rituals = rituals,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("RitualsVM", "loadRituals error", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun createRitual(
        familyId: String,
        type: RitualType,
        title: String,
        description: String,
        frequency: RitualFrequency,
        goalTitle: String = "",
        goalTarget: Int = 0,
        goalUnit: String = "times"
    ) {
        viewModelScope.launch {
            try {
                val ritual = FamilyRitual(
                    familyId = familyId,
                    type = type,
                    title = title,
                    description = description,
                    frequency = frequency,
                    goalTitle = goalTitle,
                    goalTarget = goalTarget,
                    goalUnit = goalUnit,
                    createdAt = System.currentTimeMillis()
                )
                ritualsRepository.saveRitual(ritual)
                _uiState.value = _uiState.value.copy(showCreateForm = false)
                loadRituals(familyId)
            } catch (e: Exception) {
                Log.e("RitualsVM", "createRitual error", e)
            }
        }
    }

    fun completeRitual(ritualId: String, familyId: String) {
        viewModelScope.launch {
            try {
                ritualsRepository.completeRitual(ritualId)
                loadRituals(familyId)
            } catch (e: Exception) {
                Log.e("RitualsVM", "completeRitual error", e)
            }
        }
    }

    fun submitGratitude(ritualId: String, userId: String, familyId: String) {
        viewModelScope.launch {
            try {
                ritualsRepository.submitGratitude(ritualId, userId, _uiState.value.gratitudeText)
                _uiState.value = _uiState.value.copy(gratitudeText = "")
                loadRituals(familyId)
            } catch (e: Exception) {
                Log.e("RitualsVM", "submitGratitude error", e)
            }
        }
    }

    fun updateGoalProgress(ritualId: String, familyId: String) {
        viewModelScope.launch {
            try {
                ritualsRepository.updateGoalProgress(ritualId, 1)
                loadRituals(familyId)
            } catch (e: Exception) {
                Log.e("RitualsVM", "updateGoalProgress error", e)
            }
        }
    }

    fun deleteRitual(ritualId: String, familyId: String) {
        viewModelScope.launch {
            try {
                ritualsRepository.deleteRitual(ritualId)
                loadRituals(familyId)
            } catch (e: Exception) {
                Log.e("RitualsVM", "deleteRitual error", e)
            }
        }
    }

    fun selectRitual(ritual: FamilyRitual?) {
        _uiState.value = _uiState.value.copy(selectedRitual = ritual)
    }

    fun toggleCreateForm() {
        _uiState.value = _uiState.value.copy(showCreateForm = !_uiState.value.showCreateForm)
    }

    fun updateGratitudeText(text: String) {
        _uiState.value = _uiState.value.copy(gratitudeText = text)
    }
}
