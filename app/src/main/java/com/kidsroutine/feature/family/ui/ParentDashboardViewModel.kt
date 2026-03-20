package com.kidsroutine.feature.family.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.FamilyModel
import com.kidsroutine.feature.family.data.FamilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentDashboardUiState(
    val isLoading: Boolean = false,
    val family: FamilyModel? = null,
    val error: String? = null,
    val inviteCode: String = ""
)

@HiltViewModel
class ParentDashboardViewModel @Inject constructor(
    private val familyRepository: FamilyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentDashboardUiState())
    val uiState: StateFlow<ParentDashboardUiState> = _uiState.asStateFlow()

    fun loadFamily(familyId: String) {
        if (familyId.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Invalid family ID")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                Log.d("ParentDashboardViewModel", "Loading family: $familyId")
                val family = familyRepository.getFamily(familyId)
                val inviteCode = familyRepository.getInviteCode(familyId)

                if (family != null) {
                    Log.d("ParentDashboardViewModel", "Family loaded: ${family.familyName}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        family = family,
                        inviteCode = inviteCode,
                        error = null
                    )
                } else {
                    Log.e("ParentDashboardViewModel", "Family not found: $familyId")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Family not found",
                        family = null
                    )
                }
            } catch (e: Exception) {
                Log.e("ParentDashboardViewModel", "Error loading family", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load family",
                    family = null
                )
            }
        }
    }
}