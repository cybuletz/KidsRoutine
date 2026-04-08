package com.kidsroutine.feature.storyarc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.StoryArc
import com.kidsroutine.feature.daily.data.StoryArcRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoryArcUiState(
    val arc: StoryArc? = null,
    val isLoading: Boolean = false,
    val currentChapterIndex: Int = 0,
    val isAdvancing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StoryArcViewModel @Inject constructor(
    private val storyArcRepository: StoryArcRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryArcUiState())
    val uiState: StateFlow<StoryArcUiState> = _uiState.asStateFlow()

    fun loadArc(familyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val arc = storyArcRepository.getActiveArc(familyId)
                _uiState.value = _uiState.value.copy(
                    arc = arc,
                    isLoading = false,
                    currentChapterIndex = (arc?.currentDay ?: 1) - 1,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun advanceDay() {
        val arc = _uiState.value.arc ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAdvancing = true)
            try {
                storyArcRepository.advanceDay(arc.arcId)
                val updated = storyArcRepository.getActiveArc(arc.familyId)
                _uiState.value = _uiState.value.copy(
                    arc = updated,
                    isAdvancing = false,
                    currentChapterIndex = (updated?.currentDay ?: 1) - 1
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isAdvancing = false, error = e.message)
            }
        }
    }

    fun selectChapter(index: Int) {
        _uiState.value = _uiState.value.copy(currentChapterIndex = index)
    }
}
