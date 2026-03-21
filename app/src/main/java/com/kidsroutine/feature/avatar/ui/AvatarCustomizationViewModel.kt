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
                    it.copy(
                        customization = customization,
                        allItems = allItems,
                        isLoading = false
                    )
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
        val currentState = _uiState.value
        val category = currentState.selectedCategory

        val updated = when(category) {
            AvatarCategory.BODY -> currentState.customization.copy(body = currentState.customization.body.copy(selectedItemId = itemId))
            AvatarCategory.EYES -> currentState.customization.copy(eyes = currentState.customization.eyes.copy(selectedItemId = itemId))
            AvatarCategory.MOUTH -> currentState.customization.copy(mouth = currentState.customization.mouth.copy(selectedItemId = itemId))
            AvatarCategory.HAIRSTYLE -> currentState.customization.copy(hairstyle = currentState.customization.hairstyle.copy(selectedItemId = itemId))
            AvatarCategory.ACCESSORIES -> currentState.customization.copy(accessories = currentState.customization.accessories.copy(selectedItemId = itemId))
            AvatarCategory.CLOTHING -> currentState.customization.copy(clothing = currentState.customization.clothing.copy(selectedItemId = itemId))
            AvatarCategory.BACKGROUND -> currentState.customization.copy(background = currentState.customization.background.copy(selectedItemId = itemId))
        }

        _uiState.update { it.copy(customization = updated) }
    }

    fun changeItemColor(newColor: String) {
        val currentState = _uiState.value
        val category = currentState.selectedCategory

        val updated = when(category) {
            AvatarCategory.BODY -> currentState.customization.copy(body = currentState.customization.body.copy(selectedColor = newColor))
            AvatarCategory.EYES -> currentState.customization.copy(eyes = currentState.customization.eyes.copy(selectedColor = newColor))
            AvatarCategory.MOUTH -> currentState.customization.copy(mouth = currentState.customization.mouth.copy(selectedColor = newColor))
            AvatarCategory.HAIRSTYLE -> currentState.customization.copy(hairstyle = currentState.customization.hairstyle.copy(selectedColor = newColor))
            AvatarCategory.ACCESSORIES -> currentState.customization.copy(accessories = currentState.customization.accessories.copy(selectedColor = newColor))
            AvatarCategory.CLOTHING -> currentState.customization.copy(clothing = currentState.customization.clothing.copy(selectedColor = newColor))
            AvatarCategory.BACKGROUND -> currentState.customization.copy(background = currentState.customization.background.copy(selectedColor = newColor))
        }

        _uiState.update { it.copy(customization = updated) }
    }

    fun unlockAndSelectItem(userId: String, item: AvatarItem) {
        val xpCost = item.xpCost
        val userXp = _uiState.value.userXp

        if (userXp < xpCost) {
            _uiState.update { it.copy(error = "Not enough XP! Need ${xpCost - userXp} more") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                avatarRepository.unlockAvatarItem(userId, item.itemId)
                selectItem(item.itemId)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        success = "Item unlocked!",
                        userXp = userXp - xpCost
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