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
    fun unlockPack(pack: ContentPack, userId: String = "", familyId: String = "") {
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
                    successMessage  = "🎉 ${pack.name} unlocked! Tasks are being added...",
                    error           = null
                )
            }
            // Persist entitlement
            if (userId.isNotBlank()) saveUnlockedPack(userId, pack.packId)

            // ✅ FIX: Seed pack tasks into family task pool via Cloud Function
            if (userId.isNotBlank() && familyId.isNotBlank()) {
                seedPackTasks(userId, familyId, pack.packId)
            }
            Log.d("ContentPacksVM", "Pack unlocked + tasks seeded: ${pack.name}")
        }
    }

    private suspend fun seedPackTasks(userId: String, familyId: String, packId: String) {
        try {
            val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
            val data = hashMapOf(
                "userId"   to userId,
                "familyId" to familyId,
                "packId"   to packId
            )
            functions.getHttpsCallable("applyContentPack")
                .call(data)
                .await()
            Log.d("ContentPacksVM", "✓ applyContentPack Cloud Function completed for pack=$packId")
            _uiState.update { it.copy(successMessage = "🎉 ${it.packs.find { p -> p.packId == packId }?.name ?: "Pack"} unlocked! New tasks added to your family.") }
        } catch (e: Exception) {
            Log.e("ContentPacksVM", "applyContentPack failed: ${e.message}")
            // Non-fatal — pack is still unlocked, tasks just need retry
            _uiState.update { it.copy(successMessage = "Pack unlocked! Tasks will sync shortly.") }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(successMessage = null, error = null) }
}