package com.kidsroutine.feature.avatar.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.AvatarCategory
import com.kidsroutine.core.model.AvatarCustomization
import com.kidsroutine.core.model.AvatarItem
import com.kidsroutine.feature.avatar.data.AvatarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AvatarCustomizationUiState(
    val customization: AvatarCustomization = AvatarCustomization(),
    val allItems: List<AvatarItem> = emptyList(),
    val selectedCategory: AvatarCategory = AvatarCategory.BODY,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val userXp: Int = 0
)

@HiltViewModel
class AvatarCustomizationViewModel @Inject constructor(
    private val avatarRepository: AvatarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvatarCustomizationUiState())
    val uiState: StateFlow<AvatarCustomizationUiState> = _uiState.asStateFlow()

    fun init(userId: String, userXp: Int) {
        _uiState.update { it.copy(isLoading = true, userXp = userXp) }
        viewModelScope.launch {
            try {
                val customization = avatarRepository.getUserAvatarCustomization(userId)
                val allItems = avatarRepository.getAllAvatarItems()
                _uiState.update {
                    it.copy(customization = customization, allItems = allItems, isLoading = false)
                }
                Log.d("AvatarCustomizationVM", "Loaded ${allItems.size} avatar items")
            } catch (e: Exception) {
                Log.e("AvatarCustomizationVM", "Error loading items", e)
                _uiState.update { it.copy(error = "Failed to load avatar items", isLoading = false) }
            }
        }
    }

    fun selectCategory(category: AvatarCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectItem(itemId: String) {
        val state = _uiState.value
        val updated = when (state.selectedCategory) {
            AvatarCategory.BODY        -> state.customization.copy(body        = state.customization.body.copy(selectedItemId = itemId))
            AvatarCategory.EYES        -> state.customization.copy(eyes        = state.customization.eyes.copy(selectedItemId = itemId))
            AvatarCategory.MOUTH       -> state.customization.copy(mouth       = state.customization.mouth.copy(selectedItemId = itemId))
            AvatarCategory.HAIRSTYLE   -> state.customization.copy(hairstyle   = state.customization.hairstyle.copy(selectedItemId = itemId))
            AvatarCategory.ACCESSORIES -> state.customization.copy(accessories = state.customization.accessories.copy(selectedItemId = itemId))
            AvatarCategory.CLOTHING    -> state.customization.copy(clothing    = state.customization.clothing.copy(selectedItemId = itemId))
            AvatarCategory.BACKGROUND  -> state.customization.copy(background  = state.customization.background.copy(selectedItemId = itemId))
        }
        _uiState.update { it.copy(customization = updated) }
    }

    fun changeItemColor(newColor: String) {
        val state = _uiState.value
        val updated = when (state.selectedCategory) {
            AvatarCategory.BODY        -> state.customization.copy(body        = state.customization.body.copy(selectedColor = newColor))
            AvatarCategory.EYES        -> state.customization.copy(eyes        = state.customization.eyes.copy(selectedColor = newColor))
            AvatarCategory.MOUTH       -> state.customization.copy(mouth       = state.customization.mouth.copy(selectedColor = newColor))
            AvatarCategory.HAIRSTYLE   -> state.customization.copy(hairstyle   = state.customization.hairstyle.copy(selectedColor = newColor))
            AvatarCategory.ACCESSORIES -> state.customization.copy(accessories = state.customization.accessories.copy(selectedColor = newColor))
            AvatarCategory.CLOTHING    -> state.customization.copy(clothing    = state.customization.clothing.copy(selectedColor = newColor))
            AvatarCategory.BACKGROUND  -> state.customization.copy(background  = state.customization.background.copy(selectedColor = newColor))
        }
        _uiState.update { it.copy(customization = updated) }
    }

    fun unlockAndSelectItem(userId: String, item: AvatarItem) {
        val state = _uiState.value
        if (state.userXp < item.xpCost) {
            _uiState.update { it.copy(error = "Not enough XP! Need ${item.xpCost - state.userXp} more") }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                // ← pass current in-memory customization — no stale DB re-read
                avatarRepository.unlockAvatarItem(userId, item.itemId, state.customization)
                selectItem(item.itemId)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        success  = "✅ ${item.name} unlocked!",
                        userXp   = it.userXp - item.xpCost,
                        customization = it.customization.copy(
                            unlockedItemIds = it.customization.unlockedItemIds + item.itemId
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("AvatarCustomizationVM", "Error unlocking item", e)
                _uiState.update { it.copy(error = "Failed to unlock item", isSaving = false) }
            }
        }
    }

    fun saveCustomization(userId: String) {
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                avatarRepository.updateAvatarCustomization(userId, _uiState.value.customization)
                _uiState.update { it.copy(isSaving = false, success = "Avatar saved!") }
            } catch (e: Exception) {
                Log.e("AvatarCustomizationVM", "Error saving customization", e)
                _uiState.update { it.copy(error = "Failed to save avatar", isSaving = false) }
            }
        }
    }
}
