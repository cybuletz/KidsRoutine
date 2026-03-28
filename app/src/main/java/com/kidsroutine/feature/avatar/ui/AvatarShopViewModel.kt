package com.kidsroutine.feature.avatar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import com.kidsroutine.core.model.AvatarContentPack
import com.kidsroutine.core.model.AvatarLayerItem
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

// ── UI State ──────────────────────────────────────────────────────────────────
data class AvatarShopUiState(
    val xp: Int = 0,                                    // XP balance (not coins)
    val coins: Int = 0,                                 // For display purposes if needed
    val ownedPackIds: Set<String> = emptySet(),
    val pendingPurchasePack: AvatarContentPack? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AvatarShopViewModel @Inject constructor(
    private val repository: AvatarRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String
        get() = savedStateHandle.get<String>("userId") ?: ""

    private val _uiState = MutableStateFlow(AvatarShopUiState())
    val uiState: StateFlow<AvatarShopUiState> = _uiState.asStateFlow()

    fun init(userId: String) {
        savedStateHandle["userId"] = userId
        loadShopData(userId)
    }

    private fun loadShopData(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Get user XP instead of coins
                val userXp = repository.getUserXp(userId)
                val ownedPacks = repository.getOwnedAvatarPacks(userId)

                _uiState.update {
                    it.copy(
                        xp = userXp,
                        coins = userXp,  // Keep for backward compatibility in UI
                        ownedPackIds = ownedPacks,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load shop: ${e.message}"
                    )
                }
            }
        }
    }

    // ── Purchase item individually ────────────────────────────────────────────
    fun purchaseItem(item: AvatarLayerItem) {
        val state = _uiState.value
        val xpCost = item.coinCost  // coinCost field stores XP cost

        // Validate
        if (state.xp < xpCost) {
            _uiState.update {
                it.copy(
                    errorMessage = "⚠️ Not enough XP! Need $xpCost XP. You have ${state.xp} XP."
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                // Deduct XP from user
                repository.deductUserXp(userId, xpCost)

                // Get current avatar and unlock item
                val currentAvatar = repository.getAvatar(userId) ?: AvatarState(userId = userId)
                val updatedAvatar = currentAvatar.copy(
                    unlockedItemIds = currentAvatar.unlockedItemIds + item.id
                )
                repository.saveAvatar(updatedAvatar)

                // Update UI
                _uiState.update {
                    it.copy(
                        xp = it.xp - xpCost,
                        coins = it.coins - xpCost,
                        successMessage = "✨ ${item.name} unlocked!"
                    )
                }

                // Clear message after delay
                kotlinx.coroutines.delay(2000)
                _uiState.update { it.copy(successMessage = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "❌ Purchase failed: ${e.message}")
                }
            }
        }
    }

    // ── Purchase entire pack ──────────────────────────────────────────────────
    fun purchasePack(pack: AvatarContentPack) {
        val state = _uiState.value
        val totalXpCost = pack.packPrice

        // Validate
        if (state.xp < totalXpCost) {
            _uiState.update {
                it.copy(
                    errorMessage = "⚠️ Not enough XP! Need $totalXpCost XP. You have ${state.xp} XP."
                )
            }
            return
        }

        // Show confirmation dialog
        _uiState.update { it.copy(pendingPurchasePack = pack) }
    }

    fun confirmPurchase(pack: AvatarContentPack) {
        val state = _uiState.value
        val totalXpCost = pack.packPrice

        if (state.xp < totalXpCost) {
            dismissPurchase()
            return
        }

        viewModelScope.launch {
            try {
                // Deduct XP
                repository.deductUserXp(userId, totalXpCost)

                // Get current avatar
                var currentAvatar = repository.getAvatar(userId) ?: AvatarState(userId = userId)

                // Unlock all items in pack
                val newUnlockedIds = currentAvatar.unlockedItemIds.toMutableSet()
                for (item in pack.items) {
                    newUnlockedIds.add(item.id)
                }

                // Add pack to owned packs
                val newOwnedPacks = currentAvatar.ownedPackIds.toMutableSet()
                newOwnedPacks.add(pack.id)

                // Save updated avatar
                val updatedAvatar = currentAvatar.copy(
                    unlockedItemIds = newUnlockedIds,
                    ownedPackIds = newOwnedPacks
                )
                repository.saveAvatar(updatedAvatar)
                repository.addOwnedAvatarPack(userId, pack.id)

                // Update UI state
                _uiState.update {
                    it.copy(
                        xp = it.xp - totalXpCost,
                        coins = it.coins - totalXpCost,
                        ownedPackIds = newOwnedPacks,
                        pendingPurchasePack = null,
                        successMessage = "🎉 ${pack.name} pack unlocked!"
                    )
                }

                // Clear message after delay
                kotlinx.coroutines.delay(2000)
                _uiState.update { it.copy(successMessage = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "❌ Purchase failed: ${e.message}",
                        pendingPurchasePack = null
                    )
                }
            }
        }
    }

    fun dismissPurchase() {
        _uiState.update { it.copy(pendingPurchasePack = null) }
    }

    fun previewPack(pack: AvatarContentPack) {
        // Implement if needed
    }

    fun openCoinStore() {
        // Implement if needed - show XP purchase options
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun getAvailablePacks(): List<AvatarContentPack> = AvatarSeeder.premiumPacks
}