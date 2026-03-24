package com.kidsroutine.feature.avatar.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.AvatarItem
import com.kidsroutine.core.model.AvatarRarity
import com.kidsroutine.feature.avatar.data.AvatarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AvatarShopUiState(
    val isLoading: Boolean = false,
    val items: List<AvatarItem> = emptyList(),
    val unlockedItemIds: List<String> = emptyList(),
    val userXp: Int = 0,
    val selectedRarityFilter: AvatarRarity? = null,   // null = show all
    val purchaseSuccess: String? = null,
    val error: String? = null
)

@HiltViewModel
class AvatarShopViewModel @Inject constructor(
    private val avatarRepository: AvatarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvatarShopUiState())
    val uiState: StateFlow<AvatarShopUiState> = _uiState.asStateFlow()

    fun init(userId: String, userXp: Int) {
        _uiState.update { it.copy(isLoading = true, userXp = userXp) }
        viewModelScope.launch {
            try {
                val allItems   = avatarRepository.getAllAvatarItems()
                val customization = avatarRepository.getUserAvatarCustomization(userId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = allItems,
                        unlockedItemIds = customization.unlockedItemIds
                    )
                }
            } catch (e: Exception) {
                Log.e("AvatarShopVM", "Error loading shop", e)
                _uiState.update { it.copy(isLoading = false, error = "Failed to load shop") }
            }
        }
    }

    fun setRarityFilter(rarity: AvatarRarity?) {
        _uiState.update { it.copy(selectedRarityFilter = rarity) }
    }

    fun purchaseItem(userId: String, item: AvatarItem) {
        val state = _uiState.value
        if (item.itemId in state.unlockedItemIds) {
            _uiState.update { it.copy(error = "You already own this item!") }
            return
        }
        if (state.userXp < item.xpCost) {
            _uiState.update { it.copy(error = "Not enough XP! Need ${item.xpCost} XP.") }
            return
        }
        viewModelScope.launch {
            try {
                avatarRepository.unlockAvatarItem(userId, item.itemId)
                _uiState.update {
                    it.copy(
                        unlockedItemIds = it.unlockedItemIds + item.itemId,
                        userXp          = it.userXp - item.xpCost,
                        purchaseSuccess = "✅ ${item.name} unlocked!",
                        error           = null
                    )
                }
                Log.d("AvatarShopVM", "Item purchased: ${item.name}")
            } catch (e: Exception) {
                Log.e("AvatarShopVM", "Purchase failed", e)
                _uiState.update { it.copy(error = "Purchase failed: ${e.message}") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(purchaseSuccess = null, error = null) }
    }
}