package com.kidsroutine.feature.moments.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.MomentModel
import com.kidsroutine.feature.moments.data.MomentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MomentsUiState(
    val isLoading: Boolean = false,
    val moments: List<MomentModel> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MomentsViewModel @Inject constructor(
    private val repository: MomentsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MomentsUiState())
    val uiState: StateFlow<MomentsUiState> = _uiState.asStateFlow()

    fun loadMoments(familyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val moments = repository.getMoments(familyId)
                _uiState.value = _uiState.value.copy(isLoading = false, moments = moments)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Failed to load moments"
                )
            }
        }
    }

    fun addReaction(momentId: String, userId: String, emoji: String) {
        viewModelScope.launch {
            repository.addReaction(momentId, userId, emoji)
            // Optimistic local update
            _uiState.value = _uiState.value.copy(
                moments = _uiState.value.moments.map { moment ->
                    if (moment.momentId == momentId) {
                        moment.copy(reactions = moment.reactions + (userId to emoji))
                    } else moment
                }
            )
        }
    }
}