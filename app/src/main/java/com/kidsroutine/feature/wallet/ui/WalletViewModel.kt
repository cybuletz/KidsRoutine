package com.kidsroutine.feature.wallet.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.FamilyWallet
import com.kidsroutine.core.model.SavingsGoal
import com.kidsroutine.feature.wallet.data.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletUiState(
    val wallet: FamilyWallet? = null,
    val goals: List<SavingsGoal> = emptyList(),
    val isLoading: Boolean = false,
    val showCreateGoal: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    fun loadWallet(familyId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val wallet = walletRepository.getWallet(familyId)
                val goals = walletRepository.getSavingsGoals(userId)
                _uiState.value = _uiState.value.copy(
                    wallet = wallet ?: FamilyWallet(familyId = familyId),
                    goals = goals,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("WalletVM", "loadWallet error", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun enableWallet(familyId: String) {
        viewModelScope.launch {
            try {
                val wallet = FamilyWallet(
                    familyId = familyId,
                    isEnabled = true,
                    createdAt = System.currentTimeMillis()
                )
                walletRepository.saveWallet(familyId, wallet)
                _uiState.value = _uiState.value.copy(wallet = wallet)
            } catch (e: Exception) {
                Log.e("WalletVM", "enableWallet error", e)
            }
        }
    }

    fun updateRate(familyId: String, newRate: Float) {
        viewModelScope.launch {
            try {
                val current = _uiState.value.wallet ?: return@launch
                val updated = current.copy(xpToMoneyRate = newRate)
                walletRepository.saveWallet(familyId, updated)
                _uiState.value = _uiState.value.copy(wallet = updated)
            } catch (e: Exception) {
                Log.e("WalletVM", "updateRate error", e)
            }
        }
    }

    fun createGoal(userId: String, familyId: String, title: String, emoji: String, targetXp: Int) {
        viewModelScope.launch {
            try {
                val wallet = _uiState.value.wallet
                val goal = SavingsGoal(
                    userId = userId,
                    familyId = familyId,
                    title = title,
                    emoji = emoji,
                    targetXp = targetXp,
                    targetMoneyValue = wallet?.xpToMoney(targetXp) ?: 0f,
                    createdAt = System.currentTimeMillis()
                )
                walletRepository.saveSavingsGoal(goal)
                _uiState.value = _uiState.value.copy(
                    goals = _uiState.value.goals + goal,
                    showCreateGoal = false
                )
            } catch (e: Exception) {
                Log.e("WalletVM", "createGoal error", e)
            }
        }
    }

    fun contributeToGoal(goalId: String, xpAmount: Int, userId: String) {
        viewModelScope.launch {
            try {
                walletRepository.contributeToGoal(goalId, xpAmount)
                // Refresh goals
                val goals = walletRepository.getSavingsGoals(userId)
                _uiState.value = _uiState.value.copy(goals = goals)
            } catch (e: Exception) {
                Log.e("WalletVM", "contributeToGoal error", e)
            }
        }
    }

    fun deleteGoal(goalId: String, userId: String) {
        viewModelScope.launch {
            try {
                walletRepository.deleteSavingsGoal(goalId)
                val goals = walletRepository.getSavingsGoals(userId)
                _uiState.value = _uiState.value.copy(goals = goals)
            } catch (e: Exception) {
                Log.e("WalletVM", "deleteGoal error", e)
            }
        }
    }

    fun toggleCreateGoal() {
        _uiState.value = _uiState.value.copy(showCreateGoal = !_uiState.value.showCreateGoal)
    }
}
