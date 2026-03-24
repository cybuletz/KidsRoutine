package com.kidsroutine.feature.billing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.BuiltInContentPacks
import com.kidsroutine.core.model.ContentPack
import com.kidsroutine.core.model.ContentPackTier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContentPacksUiState(
    val isLoading: Boolean = false,
    val packs: List<ContentPack> = emptyList(),
    val unlockedPackIds: Set<String> = emptySet(),
    val userXp: Int = 0,
    val isPro: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class ContentPacksViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ContentPacksUiState())
    val uiState: StateFlow<ContentPacksUiState> = _uiState.asStateFlow()

    fun init(userXp: Int, isPro: Boolean, unlockedPackIds: Set<String> = emptySet()) {
        _uiState.update {
            it.copy(
                packs           = BuiltInContentPacks.all,
                userXp          = userXp,
                isPro           = isPro,
                unlockedPackIds = unlockedPackIds,
                isLoading       = false
            )
        }
    }

    fun unlockPack(pack: ContentPack) {
        val state = _uiState.value
        if (pack.packId in state.unlockedPackIds) {
            _uiState.update { it.copy(error = "You already own this pack!") }
            return
        }
        if (pack.tier == ContentPackTier.PRO && !state.isPro) {
            _uiState.update { it.copy(error = "⭐ Upgrade to PRO to unlock this pack!") }
            return
        }
        if (pack.xpCost > 0 && state.userXp < pack.xpCost) {
            _uiState.update { it.copy(error = "Not enough XP! Need ${pack.xpCost} XP.") }
            return
        }
        viewModelScope.launch {
            // Simulate async unlock (replace with real Firestore write if needed)
            delay(400)
            _uiState.update {
                it.copy(
                    unlockedPackIds = it.unlockedPackIds + pack.packId,
                    userXp          = it.userXp - pack.xpCost.coerceAtLeast(0),
                    successMessage  = "🎉 ${pack.name} unlocked!",
                    error           = null
                )
            }
            Log.d("ContentPacksVM", "Pack unlocked: ${pack.name}")
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }
}