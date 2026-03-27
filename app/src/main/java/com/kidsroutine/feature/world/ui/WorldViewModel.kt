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
        viewModelScope.launch {
            try {
                // Immediately show the world with the user we already have
                val initialWorld = runCatching { worldRepository.getWorld(fallbackUser.xp) }.getOrNull()
                _uiState.update {
                    it.copy(
                        currentUser = fallbackUser,
                        world = initialWorld,
                        isLoading = initialWorld == null
                    )
                }

                // Then keep listening for real-time XP updates
                userRepository.observeUser(userId).collect { user ->
                    if (user != null) {
                        val world = worldRepository.getWorld(user.xp)
                        _uiState.update {
                            it.copy(
                                currentUser = user,
                                world = world,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                Log.e("WorldViewModel", "Failed to load world", e)
                val fallbackWorld = runCatching { worldRepository.getWorld(0) }.getOrNull()
                _uiState.update { it.copy(error = e.message, isLoading = false, world = fallbackWorld) }
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