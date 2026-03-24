package com.kidsroutine.feature.world.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.WorldModel
import com.kidsroutine.core.model.WorldNode
import com.kidsroutine.feature.world.data.WorldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorldUiState(
    val isLoading: Boolean = true,
    val world: WorldModel? = null,
    val selectedNode: WorldNode? = null,
    val showNodeDetail: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WorldViewModel @Inject constructor(
    private val repository: WorldRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorldUiState())
    val uiState: StateFlow<WorldUiState> = _uiState.asStateFlow()

    fun loadWorld(userXp: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val world = repository.getWorld(userXp)
                _uiState.value = _uiState.value.copy(isLoading = false, world = world)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load world"
                )
            }
        }
    }

    fun onNodeTapped(node: WorldNode) {
        _uiState.value = _uiState.value.copy(
            selectedNode = node,
            showNodeDetail = true
        )
    }

    fun dismissNodeDetail() {
        _uiState.value = _uiState.value.copy(
            showNodeDetail = false,
            selectedNode = null
        )
    }
}