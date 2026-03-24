package com.kidsroutine.feature.billing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.BuiltInContentPacks
import com.kidsroutine.core.model.ContentPack
import com.kidsroutine.core.model.ContentPackTier
import com.kidsroutine.core.model.EntitlementsRepository
import com.kidsroutine.core.model.PlanType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
class ContentPacksViewModel @Inject constructor(
    private val entitlementsRepository: EntitlementsRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

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

    // ── Called from ParentNavGraph ─────────────────────────────────────────
    fun loadForUser(userId: String, userXp: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Load plan type
                val entitlements = entitlementsRepository.getEntitlements(userId)
                val isPro = entitlements.planType != PlanType.FREE

                // 2. Load unlocked pack IDs from Firestore
                val unlockedPackIds = loadUnlockedPacks(userId)

                _uiState.update {
                    it.copy(
                        packs           = BuiltInContentPacks.all,
                        userXp          = userXp,
                        isPro           = isPro,
                        unlockedPackIds = unlockedPackIds,
                        isLoading       = false
                    )
                }
                Log.d("ContentPacksVM", "Loaded: isPro=$isPro, unlocked=${unlockedPackIds.size} packs")
            } catch (e: Exception) {
                Log.e("ContentPacksVM", "Load error: ${e.message}")
                _uiState.update {
                    it.copy(
                        packs     = BuiltInContentPacks.all,
                        userXp    = userXp,
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun loadUnlockedPacks(userId: String): Set<String> {
        return try {
            val snapshot = firestore
                .collection("user_content_packs")
                .document(userId)
                .get()
                .await()
            @Suppress("UNCHECKED_CAST")
            (snapshot.data?.get("unlockedPackIds") as? List<String>)?.toSet() ?: emptySet()
        } catch (e: Exception) {
            Log.w("ContentPacksVM", "Could not load unlocked packs: ${e.message}")
            emptySet()
        }
    }

    private suspend fun saveUnlockedPack(userId: String, packId: String) {
        try {
            val current = loadUnlockedPacks(userId).toMutableSet()
            current.add(packId)
            firestore.collection("user_content_packs")
                .document(userId)
                .set(mapOf("unlockedPackIds" to current.toList()))
                .await()
        } catch (e: Exception) {
            Log.w("ContentPacksVM", "Could not save unlocked pack: ${e.message}")
        }
    }

    // ── Unlock logic (unchanged, now persists) ─────────────────────────────
    fun unlockPack(pack: ContentPack, userId: String = "") {
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
            delay(400)
            val newSet = state.unlockedPackIds + pack.packId
            _uiState.update {
                it.copy(
                    unlockedPackIds = newSet,
                    userXp          = it.userXp - pack.xpCost.coerceAtLeast(0),
                    successMessage  = "🎉 ${pack.name} unlocked!",
                    error           = null
                )
            }
            // Persist to Firestore
            if (userId.isNotBlank()) saveUnlockedPack(userId, pack.packId)
            Log.d("ContentPacksVM", "Pack unlocked: ${pack.name}")
        }
    }

    fun clearMessages() = _uiState.update { it.copy(successMessage = null, error = null) }
}