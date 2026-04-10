package com.kidsroutine.feature.lootbox.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.LootBox
import com.kidsroutine.core.model.LootBoxRarity
import com.kidsroutine.core.model.LootBoxReward
import com.kidsroutine.core.model.LootBoxRewardType
import com.kidsroutine.feature.daily.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

enum class LootBoxPhase { IDLE, WAITING, SHAKING, BURSTING, REVEALING, DONE }

data class LootBoxUiState(
    val phase: LootBoxPhase = LootBoxPhase.IDLE,
    val lootBox: LootBox? = null,
    val reward: LootBoxReward? = null,
    val userId: String = ""
)

@HiltViewModel
class LootBoxViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LootBoxUiState())
    val uiState: StateFlow<LootBoxUiState> = _uiState.asStateFlow()

    private val rewardPool = listOf(
        LootBoxReward(type = LootBoxRewardType.XP_BOOST,      rarity = LootBoxRarity.COMMON,    title = "XP Surge",       description = "+25 bonus XP added to your total!",   emoji = "⚡", xpValue = 25),
        LootBoxReward(type = LootBoxRewardType.XP_BOOST,      rarity = LootBoxRarity.RARE,      title = "Double XP",      description = "+75 bonus XP — you're on fire!",       emoji = "🔥", xpValue = 75),
        LootBoxReward(type = LootBoxRewardType.XP_BOOST,      rarity = LootBoxRarity.EPIC,      title = "XP Explosion",   description = "+150 XP mega bonus!",                  emoji = "💥", xpValue = 150),
        LootBoxReward(type = LootBoxRewardType.STREAK_SHIELD, rarity = LootBoxRarity.RARE,      title = "Streak Shield",  description = "Your streak is protected for 1 day!",  emoji = "🛡️", xpValue = 0),
        LootBoxReward(type = LootBoxRewardType.BADGE,         rarity = LootBoxRarity.EPIC,      title = "Mystery Badge",  description = "A rare achievement badge!",             emoji = "🏅", xpValue = 0),
        LootBoxReward(type = LootBoxRewardType.MYSTERY,       rarity = LootBoxRarity.LEGENDARY, title = "Legendary Drop", description = "Something truly special — wow!",        emoji = "🌟", xpValue = 200),
        LootBoxReward(type = LootBoxRewardType.AVATAR_ITEM,   rarity = LootBoxRarity.COMMON,    title = "Avatar Flair",   description = "A cool new avatar accessory!",          emoji = "🎨", xpValue = 0),
        LootBoxReward(type = LootBoxRewardType.XP_BOOST,      rarity = LootBoxRarity.COMMON,    title = "Quick Boost",    description = "+10 XP to get you going!",              emoji = "✨", xpValue = 10),
    )

    fun presentBox(box: LootBox, userId: String = "") {
        val reward = rollReward()
        _uiState.value = LootBoxUiState(
            phase   = LootBoxPhase.WAITING,
            lootBox = box.copy(reward = reward),
            reward  = reward,
            userId  = userId
        )
    }

    fun onBoxTapped() {
        if (_uiState.value.phase != LootBoxPhase.WAITING) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(phase = LootBoxPhase.SHAKING)
            delay(600)
            _uiState.value = _uiState.value.copy(phase = LootBoxPhase.BURSTING)
            delay(700)
            _uiState.value = _uiState.value.copy(phase = LootBoxPhase.REVEALING)
        }
    }

    private val _persistComplete = MutableStateFlow(false)

    fun dismiss() {
        viewModelScope.launch {
            _persistComplete.value = false
            try {
                persistRewardSuspend()
            } catch (e: Exception) {
                Log.e("LootBoxVM", "Error in dismiss", e)
            }
            _persistComplete.value = true  // ✅ Signal completion
            _uiState.value = LootBoxUiState(phase = LootBoxPhase.DONE)
        }
    }

    private suspend fun persistRewardSuspend() {
        val state = _uiState.value
        val reward = state.reward
        val userId = state.userId

        if (reward != null && userId.isNotBlank()) {
            try {
                val userRef = firestore.collection("users").document(userId)
                when (reward.type) {
                    LootBoxRewardType.XP_BOOST, LootBoxRewardType.MYSTERY -> {
                        if (reward.xpValue > 0) {
                            userRepository.updateUserXp(userId, reward.xpValue)
                            Log.d("LootBoxVM", "✓ XP persisted: +${reward.xpValue} for $userId (Firestore + Room synced)")
                        }
                    }
                    LootBoxRewardType.STREAK_SHIELD -> {
                        userRef.update("streakShieldActive", true).await()
                        Log.d("LootBoxVM", "✓ Streak shield activated for $userId")
                    }
                    LootBoxRewardType.BADGE -> {
                        val badgeId = "lootbox_badge_${System.currentTimeMillis()}"
                        userRef.collection("badges").document(badgeId)
                            .set(mapOf(
                                "badgeId"    to badgeId,
                                "title"      to reward.title,
                                "emoji"      to reward.emoji,
                                "rarity"     to reward.rarity.name,
                                "earnedAt"   to System.currentTimeMillis(),
                                "source"     to "lootbox"
                            )).await()
                        Log.d("LootBoxVM", "✓ Badge saved: ${reward.title} for $userId")
                    }
                    LootBoxRewardType.AVATAR_ITEM -> {
                        val itemId = "avatar_item_${System.currentTimeMillis()}"
                        userRef.collection("avatar_items").document(itemId)
                            .set(mapOf(
                                "itemId"   to itemId,
                                "title"    to reward.title,
                                "emoji"    to reward.emoji,
                                "rarity"   to reward.rarity.name,
                                "earnedAt" to System.currentTimeMillis(),
                                "source"   to "lootbox"
                            )).await()
                        Log.d("LootBoxVM", "✓ Avatar item saved: ${reward.title} for $userId")
                    }
                }
                // Always write to loot_box_history
                firestore.collection("loot_box_history").add(mapOf(
                    "userId"      to userId,
                    "rewardType"  to reward.type.name,
                    "rewardTitle" to reward.title,
                    "rarity"      to reward.rarity.name,
                    "xpValue"     to reward.xpValue,
                    "openedAt"    to System.currentTimeMillis()
                )).await()
                Log.d("LootBoxVM", "✓ Full reward persisted for $userId")
            } catch (e: Exception) {
                Log.e("LootBoxVM", "Failed to persist reward: ${e.message}", e)
            }
        }

        _uiState.value = LootBoxUiState(phase = LootBoxPhase.DONE)
    }

    fun reset() {
        _uiState.value = LootBoxUiState(phase = LootBoxPhase.IDLE)
    }

    private fun rollReward(): LootBoxReward {
        val roll = (1..100).random()
        val rarity = when {
            roll <= 3  -> LootBoxRarity.LEGENDARY
            roll <= 15 -> LootBoxRarity.EPIC
            roll <= 40 -> LootBoxRarity.RARE
            else       -> LootBoxRarity.COMMON
        }
        return rewardPool.filter { it.rarity == rarity }.randomOrNull() ?: rewardPool.first()
    }
}
