package com.kidsroutine.feature.family.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.FamilyModel
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.family.data.FamilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilySetupUiState(
    val isLoading: Boolean = false,
    val family: FamilyModel? = null,
    val familyName: String = "",
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class FamilySetupViewModel @Inject constructor(
    private val familyRepository: FamilyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilySetupUiState())
    val uiState: StateFlow<FamilySetupUiState> = _uiState.asStateFlow()

    fun createFamily(userId: String, familyName: String) {
        if (familyName.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Family name is required")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                Log.d("FamilySetupViewModel", "Creating family: $familyName")
                val family = familyRepository.createFamily(userId, familyName)
                Log.d("FamilySetupViewModel", "Family created successfully: ${family.familyId}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    family = family,
                    success = true,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("FamilySetupViewModel", "Error creating family", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create family",
                    success = false
                )
            }
        }
    }

    fun updateFamilyName(name: String) {
        _uiState.value = _uiState.value.copy(familyName = name, error = null)
    }
}