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

data class JoinFamilyUiState(
    val isLoading: Boolean = false,
    val family: FamilyModel? = null,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class JoinFamilyViewModel @Inject constructor(
    private val familyRepository: FamilyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinFamilyUiState())
    val uiState: StateFlow<JoinFamilyUiState> = _uiState.asStateFlow()

    fun joinFamily(userId: String, inviteCode: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                Log.d("JoinFamilyVM", "Joining family with code: $inviteCode")

                // Find family by invite code
                val families = familyRepository.getFamiliesByInviteCode(inviteCode)

                if (families.isEmpty()) {
                    Log.e("JoinFamilyVM", "No family found with code: $inviteCode")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Invalid invite code"
                    )
                    return@launch
                }

                val family = families[0]
                Log.d("JoinFamilyVM", "Found family: ${family.familyId}")

                // Add user to family
                familyRepository.addMemberToFamily(family.familyId, userId)

                Log.d("JoinFamilyVM", "Successfully joined family: ${family.familyId}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    family = family,
                    success = true
                )
            } catch (e: Exception) {
                Log.e("JoinFamilyVM", "Error joining family", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to join family"
                )
            }
        }
    }
}