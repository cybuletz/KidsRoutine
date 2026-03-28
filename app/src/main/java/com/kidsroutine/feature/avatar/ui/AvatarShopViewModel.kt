package com.kidsroutine.feature.avatar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.AvatarContentPack
import com.kidsroutine.core.model.AvatarState
import com.kidsroutine.feature.avatar.data.AvatarRepository
import com.kidsroutine.feature.avatar.data.AvatarSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AvatarShopUiState(
    val coins: Int = 0,
    val ownedPackIds: Set<String> = emptySet(),
    val pendingPurchasePack: AvatarContentPack? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AvatarShopViewModel @Inject constructor(
    private val repository: AvatarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvatarShopUiState())
    val uiState: StateFlow<AvatarShopUiState> = _uiState.asStateFlow()

    // Set by the caller before the shop opens
    private var currentUserId: String = ""

    fun init(userId: String) {
        currentUserId = userId
        loadShopData()
    }

    private fun loadShopData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val coins = repository.getCoins(currentUserId)
                val avatar = repository.getAvatar(currentUserId)
                _uiState.update {
                    it.copy(
                        coins = coins,
                        ownedPackIds = avatar?.ownedPackIds ?: emptySet(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun purchasePack(pack: AvatarContentPack) {
        _uiState.update { it.copy(pendingPurchasePack = pack) }
    }

    fun confirmPurchase(pack: AvatarContentPack) {
        viewModelScope.launch {
            try {
                val current = repository.getAvatar(currentUserId) ?: return@launch
                val updated = current.copy(
                    ownedPackIds = current.ownedPackIds + pack.id,
                    unlockedItemIds = current.unlockedItemIds + pack.items.map { it.id }.toSet()
                )
                repository.saveAvatar(updated)
                _uiState.update { state ->
                    state.copy(
                        ownedPackIds = updated.ownedPackIds,
                        coins = (state.coins - pack.packPrice).coerceAtLeast(0),
                        pendingPurchasePack = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, pendingPurchasePack = null) }
            }
        }
    }

    fun dismissPurchase() {
        _uiState.update { it.copy(pendingPurchasePack = null) }
    }

    fun previewPack(pack: AvatarContentPack) {
        // No-op for now — can open a preview bottom sheet in future
    }

    fun openCoinStore() {
        // Hook into your billing flow here
    }
}