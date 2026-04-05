package com.kidsroutine.feature.avatar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import com.kidsroutine.core.model.*
import com.kidsroutine.feature.avatar.data.AvatarRepository
import com.kidsroutine.feature.avatar.data.AvatarSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI State ─────────────────────────────────────────────────────────────────
data class AvatarUiState(
    val currentAvatar: AvatarState = AvatarState(userId = ""),
    val savedAvatar: AvatarState = AvatarState(userId = ""),
    val coins: Int = 0,
    val playerName: String = "",
    val unlockedItemIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val hasUnsavedChanges: Boolean
        get() = currentAvatar != savedAvatar
}

@HiltViewModel
class AvatarCustomizationViewModel @Inject constructor(
    private val repository: AvatarRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String get() = savedStateHandle["userId"] ?: ""

    private val _uiState = MutableStateFlow(AvatarUiState())
    val uiState: StateFlow<AvatarUiState> = _uiState.asStateFlow()

    init {
        if (userId.isNotEmpty()) {
            loadAvatar()
        }
    }

    fun initWithUserId(id: String) {
        savedStateHandle["userId"] = id
        loadAvatar()
    }

    // ── Load ──────────────────────────────────────────────────────────────────
    private fun loadAvatar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val avatar = repository.getAvatar(userId)
                    ?: AvatarState(userId = userId)
                val coins = repository.getCoins(userId)
                val playerName = repository.getPlayerName(userId)
                val unlockedIds = repository.getUnlockedItemIds(userId)
                _uiState.update {
                    it.copy(
                        currentAvatar = avatar,
                        savedAvatar = avatar,
                        coins = coins,
                        playerName = playerName,
                        unlockedItemIds = unlockedIds,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load avatar")
                }
            }
        }
    }

    // ── Gender ────────────────────────────────────────────────────────────────
    fun setGender(gender: AvatarGender) {
        _uiState.update { state ->
            state.copy(
                currentAvatar = state.currentAvatar.copy(
                    gender = gender,
                    // Clear gender-incompatible hair when switching
                    activeHair = state.currentAvatar.activeHair?.takeIf {
                        gender in it.compatibleGenders
                    }
                )
            )
        }
    }

    // ── Skin Tone ─────────────────────────────────────────────────────────────
    fun setSkinTone(tone: Long) {
        _uiState.update { state ->
            state.copy(currentAvatar = state.currentAvatar.copy(skinTone = tone))
        }
    }

    // ── Equip Item ────────────────────────────────────────────────────────────
    fun equipItem(item: AvatarLayerItem) {
        _uiState.update { state ->
            val avatar = state.currentAvatar
            val updated = when (item.layerType) {
                AvatarLayerType.BACKGROUND ->
                    avatar.copy(activeBackground = item.takeUnless { it.id.startsWith("none_") })
                AvatarLayerType.HAIR ->
                    avatar.copy(activeHair = item.takeUnless { it.id.startsWith("none_") })
                AvatarLayerType.OUTFIT ->
                    avatar.copy(activeOutfit = item.takeUnless { it.id.startsWith("none_") })
                AvatarLayerType.SHOES ->
                    avatar.copy(activeShoes = item.takeUnless { it.id.startsWith("none_") })
                AvatarLayerType.ACCESSORY ->
                    avatar.copy(activeAccessory = item.takeUnless { it.id.startsWith("none_") })
                AvatarLayerType.SPECIAL_FX ->
                    avatar.copy(activeSpecialFx = item.takeUnless { it.id.startsWith("none_") })
                AvatarLayerType.EYE_STYLE ->
                    avatar.copy(activeEyeStyle = item.takeUnless { it.id.startsWith("none_") })
                AvatarLayerType.FACE_DETAIL ->
                    avatar.copy(activeFaceDetail = item.takeUnless { it.id.startsWith("none_") })
                else -> avatar
            }
            state.copy(currentAvatar = updated)
        }
    }

    // ── Hair Colour ───────────────────────────────────────────────────────────
    fun setHairColor(color: Long) {
        _uiState.update { state ->
            state.copy(currentAvatar = state.currentAvatar.copy(hairColor = color))
        }
    }

    // ── Eye Shape ─────────────────────────────────────────────────────────────
    fun setEyeShape(shapeId: String?) {
        _uiState.update { state ->
            state.copy(currentAvatar = state.currentAvatar.copy(eyeShape = shapeId))
        }
    }

    // ── Mouth Shape ──────────────────────────────────────────────────────────
    fun setMouthShape(shapeId: String?) {
        _uiState.update { state ->
            state.copy(currentAvatar = state.currentAvatar.copy(mouthShape = shapeId))
        }
    }

    // ── Eyebrow Style ────────────────────────────────────────────────────────
    fun setEyebrowStyle(styleId: String?) {
        _uiState.update { state ->
            state.copy(currentAvatar = state.currentAvatar.copy(eyebrowStyle = styleId))
        }
    }

    // ── Face Shape ────────────────────────────────────────────────────────────
    fun setFaceShape(shapeId: String?) {
        _uiState.update { state ->
            state.copy(currentAvatar = state.currentAvatar.copy(faceShape = shapeId))
        }
    }

    // ── Reset ─────────────────────────────────────────────────────────────────
    fun resetToDefault() {
        _uiState.update { state ->
            state.copy(
                currentAvatar = AvatarState(
                    userId = userId,
                    gender = state.currentAvatar.gender,
                    skinTone = 0xFFFFDBAD,
                    activeBackground = AvatarSeeder.freeBackgrounds.first(),
                    activeHair = AvatarSeeder.freeHair.firstOrNull {
                        state.currentAvatar.gender in it.compatibleGenders
                    },
                    activeOutfit = AvatarSeeder.freeOutfits.first()
                )
            )
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────
    fun saveAvatar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                repository.saveAvatar(uiState.value.currentAvatar)
                _uiState.update { state ->
                    state.copy(
                        savedAvatar = state.currentAvatar,
                        isSaving = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = "Failed to save avatar")
                }
            }
        }
    }

    // ── Item Lists per Tab ────────────────────────────────────────────────────
    fun getFreeItemsForTab(tab: AvatarCustomizationTab): List<AvatarLayerItem> {
        val gender = uiState.value.currentAvatar.gender
        return when (tab) {
            AvatarCustomizationTab.BACKGROUND -> AvatarSeeder.freeBackgrounds
            AvatarCustomizationTab.HAIR -> AvatarSeeder.freeHair.filter {
                gender in it.compatibleGenders
            }
            AvatarCustomizationTab.EYES -> AvatarSeeder.freeEyeStyles
            AvatarCustomizationTab.FACE -> AvatarSeeder.freeFaceDetails
            AvatarCustomizationTab.OUTFIT -> AvatarSeeder.freeOutfits
            AvatarCustomizationTab.ACCESSORY -> AvatarSeeder.freeAccessories.filter {
                gender in it.compatibleGenders
            }
            AvatarCustomizationTab.SPECIAL_FX -> AvatarSeeder.freeSpecialFx
        }
    }

    fun getPremiumItemsForTab(tab: AvatarCustomizationTab): List<AvatarLayerItem> {
        val gender = uiState.value.currentAvatar.gender
        val targetLayer = when (tab) {
            AvatarCustomizationTab.BACKGROUND -> AvatarLayerType.BACKGROUND
            AvatarCustomizationTab.HAIR -> AvatarLayerType.HAIR
            AvatarCustomizationTab.EYES -> AvatarLayerType.EYE_STYLE
            AvatarCustomizationTab.FACE -> AvatarLayerType.FACE_DETAIL
            AvatarCustomizationTab.OUTFIT -> AvatarLayerType.OUTFIT
            AvatarCustomizationTab.ACCESSORY -> AvatarLayerType.ACCESSORY
            AvatarCustomizationTab.SPECIAL_FX -> AvatarLayerType.SPECIAL_FX
        }
        return AvatarSeeder.allPremiumItems()
            .filter { it.layerType == targetLayer }
            .filter { gender in it.compatibleGenders }
    }
}