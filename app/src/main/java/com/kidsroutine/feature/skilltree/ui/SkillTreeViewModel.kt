package com.kidsroutine.feature.skilltree.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.SkillBranch
import com.kidsroutine.core.model.SkillNode
import com.kidsroutine.core.model.SkillTree
import com.kidsroutine.feature.skilltree.data.SkillTreeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SkillTreeUiState(
    val tree: SkillTree? = null,
    val isLoading: Boolean = false,
    val selectedBranch: SkillBranch = SkillBranch.RESPONSIBILITY,
    val selectedNode: SkillNode? = null,
    val error: String? = null
)

@HiltViewModel
class SkillTreeViewModel @Inject constructor(
    private val skillTreeRepository: SkillTreeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkillTreeUiState())
    val uiState: StateFlow<SkillTreeUiState> = _uiState.asStateFlow()

    fun loadSkillTree(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val tree = skillTreeRepository.getSkillTree(userId)
                _uiState.value = _uiState.value.copy(
                    tree = tree,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("SkillTreeVM", "loadSkillTree error", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun selectBranch(branch: SkillBranch) {
        _uiState.value = _uiState.value.copy(selectedBranch = branch, selectedNode = null)
    }

    fun selectNode(node: SkillNode) {
        _uiState.value = _uiState.value.copy(selectedNode = node)
    }

    fun dismissNodeDetail() {
        _uiState.value = _uiState.value.copy(selectedNode = null)
    }

    fun unlockNode(userId: String, nodeId: String) {
        viewModelScope.launch {
            try {
                val updated = skillTreeRepository.unlockNode(userId, nodeId)
                if (updated != null) {
                    _uiState.value = _uiState.value.copy(tree = updated, selectedNode = null)
                }
            } catch (e: Exception) {
                Log.e("SkillTreeVM", "unlockNode error", e)
            }
        }
    }
}
