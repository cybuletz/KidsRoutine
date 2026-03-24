package com.kidsroutine.feature.lootbox.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.LootBox
import com.kidsroutine.core.model.LootBoxRarity
import com.kidsroutine.core.model.LootBoxReward
import com.kidsroutine.core.model.LootBoxRewardType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LootBoxPhase {
    IDLE,       // nothing showing
    WAITING,    // box sitting on screen, shake loop
    SHAKING,    // user tapped — violent shake
    BURSTING,   // box explodes apart
    REVEALING,  // reward card flies in
    DONE        // dismiss
}

data class LootBoxUiState(
    val phase: LootBoxPhase = LootBoxPhase.IDLE,
    val lootBox: LootBox? = null,
    val reward: LootBoxReward? = null
)

@HiltViewModel
class LootBoxViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LootBoxUiState())
    val uiState: StateFlow<LootBoxUiState> = _uiState.asStateFlow()

    // ── Catalog of possible rewards ──────────────────────────────────────────
    private val rewardPool = listOf(
        LootBoxReward(type = LootBoxRewardType.XP_BOOST,      rarity = LootBoxRarity.COMMON,    title = "XP Surge",        description = "+25 bonus XP added to your total!",   emoji = "⚡", xpValue = 25),
        LootBoxReward(type = LootBoxRewardType.XP_BOOST,      rarity = LootBoxRarity.RARE,      title = "Double XP",       description = "+75 bonus XP — you're on fire!",        emoji = "🔥", xpValue = 75),
        LootBoxReward(type = LootBoxRewardType.XP_BOOST,      rarity = LootBoxRarity.EPIC,      title = "XP Explosion",    description = "+150 XP mega bonus!",                  emoji = "💥", xpValue = 150),
        LootBoxReward(type = LootBoxRewardType.STREAK_SHIELD, rarity = LootBoxRarity.RARE,      title = "Streak Shield",   description = "Your streak is protected for 1 day!", emoji = "🛡️", xpValue = 0),
        LootBoxReward(type = LootBoxRewardType.BADGE,         rarity = LootBoxRarity.EPIC,      title = "Mystery Badge",   description = "A rare achievement badge!",            emoji = "🏅", xpValue = 0),
        LootBoxReward(type = LootBoxRewardType.MYSTERY,       rarity = LootBoxRarity.LEGENDARY, title = "Legendary Drop",  description = "Something truly special — wow!",       emoji = "🌟", xpValue = 200),
        LootBoxReward(type = LootBoxRewardType.AVATAR_ITEM,   rarity = LootBoxRarity.COMMON,    title = "Avatar Flair",    description = "A cool new avatar accessory!",         emoji = "🎨", xpValue = 0),
        LootBoxReward(type = LootBoxRewardType.XP_BOOST,      rarity = LootBoxRarity.COMMON,    title = "Quick Boost",     description = "+10 XP to get you going!",             emoji = "✨", xpValue = 10),
    )

    fun presentBox(box: LootBox) {
        // Assign a random reward weighted by rarity
        val reward = rollReward()
        _uiState.value = LootBoxUiState(
            phase   = LootBoxPhase.WAITING,
            lootBox = box.copy(reward = reward),
            reward  = reward
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

    fun dismiss() {
        _uiState.value = LootBoxUiState(phase = LootBoxPhase.DONE)
    }

    fun reset() {
        _uiState.value = LootBoxUiState(phase = LootBoxPhase.IDLE)
    }

    private fun rollReward(): LootBoxReward {
        // Weighted probability: LEGENDARY 3%, EPIC 12%, RARE 25%, COMMON 60%
        val roll = (1..100).random()
        val rarity = when {
            roll <= 3  -> LootBoxRarity.LEGENDARY
            roll <= 15 -> LootBoxRarity.EPIC
            roll <= 40 -> LootBoxRarity.RARE
            else       -> LootBoxRarity.COMMON
        }
        return rewardPool.filter { it.rarity == rarity }.randomOrNull()
            ?: rewardPool.first()
    }
}