package com.kidsroutine.feature.world.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.core.model.WorldModel
import com.kidsroutine.core.model.WorldNode
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.world.data.WorldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorldUiState(
    val isLoading: Boolean = true,
    val world: WorldModel? = null,
    val selectedNode: WorldNode? = null,
    val showNodeDetail: Boolean = false,
    val currentUser: UserModel = UserModel(),
    val error: String? = null
)

@HiltViewModel
class WorldViewModel @Inject constructor(
    private val worldRepository: WorldRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorldUiState())
    val uiState: StateFlow<WorldUiState> = _uiState.asStateFlow()

    fun loadWorld(userId: String, fallbackUser: UserModel) {
        // Apply fallback immediately — synchronous, no coroutine needed
        viewModelScope.launch {
            try {
                val initialWorld = worldRepository.getWorld(fallbackUser.totalXpEarned)
                _uiState.update {
                    it.copy(currentUser = fallbackUser, world = initialWorld, isLoading = false)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                Log.w("WorldViewModel", "Initial world load cancelled — applying fallback")
                val fallback = runCatching { worldRepository.getWorld(fallbackUser.totalXpEarned) }.getOrNull()
                _uiState.update { it.copy(currentUser = fallbackUser, world = fallback, isLoading = false) }
            } catch (e: Exception) {
                Log.e("WorldViewModel", "Failed to load world", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        // Separate coroutine for live updates — won't block initial render
        viewModelScope.launch {
            try {
                userRepository.observeUser(userId).collect { user ->
                    if (user != null) {
                        val world = worldRepository.getWorld(user.totalXpEarned)
                        _uiState.update {
                            it.copy(currentUser = user, world = world, isLoading = false)
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("WorldViewModel", "Failed to observe user for world", e)
                val fallbackWorld = runCatching { worldRepository.getWorld(fallbackUser.totalXpEarned) }.getOrNull()
                _uiState.update { it.copy(error = e.message, isLoading = false, world = fallbackWorld ?: it.world) }
            }
        }
    }

    fun onNodeTapped(node: WorldNode) {
        _uiState.update { it.copy(selectedNode = node, showNodeDetail = true) }
    }

    fun dismissNodeDetail() {
        _uiState.update { it.copy(showNodeDetail = false) }
    }
}