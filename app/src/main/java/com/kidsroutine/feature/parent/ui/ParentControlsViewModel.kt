package com.kidsroutine.feature.parent.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.ParentControlSettings
import com.kidsroutine.core.model.UserEntitlements
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.core.model.XpLoan
import com.kidsroutine.core.model.XpLoanStatus
import com.kidsroutine.core.model.EntitlementsRepository
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.parent.data.ParentControlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentControlsUiState(
    val isLoading: Boolean = false,
    val selectedChildId: String = "",
    val controlSettings: ParentControlSettings = ParentControlSettings(),
    val activeLoans: List<XpLoan> = emptyList(),
    val allFamilyLoans: List<XpLoan> = emptyList(),
    val entitlements: UserEntitlements = UserEntitlements(),
    val error: String? = null,
    val successMessage: String? = null,
    val showLoanDialog: Boolean = false,
    val showForgiveDialog: Boolean = false,
    val selectedLoanId: String = ""
)

@HiltViewModel
class ParentControlsViewModel @Inject constructor(
    private val parentControlRepository: ParentControlRepository,
    private val userRepository: UserRepository,
    private val entitlementsRepository: EntitlementsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentControlsUiState())
    val uiState: StateFlow<ParentControlsUiState> = _uiState.asStateFlow()

    fun loadForChild(familyId: String, childId: String, parentUserId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, selectedChildId = childId, error = null)

        viewModelScope.launch {
            try {
                val settings = parentControlRepository.getControlSettings(familyId, childId)
                val loans = parentControlRepository.getActiveLoans(familyId, childId)
                val allLoans = parentControlRepository.getAllFamilyLoans(familyId)
                val entitlements = entitlementsRepository.getEntitlements(parentUserId, familyId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    controlSettings = settings,
                    activeLoans = loans,
                    allFamilyLoans = allLoans,
                    entitlements = entitlements
                )
                Log.d(TAG, "Loaded controls for child $childId: ${settings.petEnabled}, loans=${loans.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading controls", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load controls: ${e.message}"
                )
            }
        }
    }

    // ── Fun Zone Toggle ────────────────────────────────────────────────

    fun toggleFunZoneFeature(featureKey: String, enabled: Boolean) {
        val current = _uiState.value.controlSettings
        val updated = when (featureKey) {
            "pet"         -> current.copy(petEnabled = enabled)
            "boss_battle" -> current.copy(bossBattleEnabled = enabled)
            "daily_spin"  -> current.copy(dailySpinEnabled = enabled)
            "story_arcs"  -> current.copy(storyArcsEnabled = enabled)
            "events"      -> current.copy(eventsEnabled = enabled)
            "skill_tree"  -> current.copy(skillTreeEnabled = enabled)
            "wallet"      -> current.copy(walletEnabled = enabled)
            "rituals"     -> current.copy(ritualsEnabled = enabled)
            else          -> current
        }
        _uiState.value = _uiState.value.copy(controlSettings = updated)
        saveSettings(updated)
    }

    // ── Difficulty Configuration ───────────────────────────────────────

    fun setDefaultDifficulty(difficulty: DifficultyLevel) {
        val updated = _uiState.value.controlSettings.copy(defaultDifficulty = difficulty)
        _uiState.value = _uiState.value.copy(controlSettings = updated)
        saveSettings(updated)
    }

    fun toggleAllowedDifficulty(difficulty: DifficultyLevel, allowed: Boolean) {
        val current = _uiState.value.controlSettings.allowedDifficulties.toMutableList()
        if (allowed) current.add(difficulty) else current.remove(difficulty)
        // Ensure at least one difficulty is always allowed
        if (current.isEmpty()) current.add(DifficultyLevel.EASY)
        val updated = _uiState.value.controlSettings.copy(allowedDifficulties = current)
        _uiState.value = _uiState.value.copy(controlSettings = updated)
        saveSettings(updated)
    }

    fun setXpMultiplier(difficulty: DifficultyLevel, multiplier: Float) {
        val current = _uiState.value.controlSettings
        val updated = when (difficulty) {
            DifficultyLevel.EASY   -> current.copy(xpMultiplierEasy = multiplier)
            DifficultyLevel.MEDIUM -> current.copy(xpMultiplierMedium = multiplier)
            DifficultyLevel.HARD   -> current.copy(xpMultiplierHard = multiplier)
        }
        _uiState.value = _uiState.value.copy(controlSettings = updated)
        saveSettings(updated)
    }

    // ── XP Economy Caps ────────────────────────────────────────────────

    fun setDailyXpEarningCap(cap: Int) {
        val updated = _uiState.value.controlSettings.copy(dailyXpEarningCap = cap)
        _uiState.value = _uiState.value.copy(controlSettings = updated)
        saveSettings(updated)
    }

    fun setDailyXpSpendingCap(cap: Int) {
        val updated = _uiState.value.controlSettings.copy(dailyXpSpendingCap = cap)
        _uiState.value = _uiState.value.copy(controlSettings = updated)
        saveSettings(updated)
    }

    // ── XP Bank / Loans ────────────────────────────────────────────────

    fun showLoanDialog() {
        _uiState.value = _uiState.value.copy(showLoanDialog = true)
    }

    fun dismissLoanDialog() {
        _uiState.value = _uiState.value.copy(showLoanDialog = false)
    }

    fun createLoan(
        familyId: String,
        parentId: String,
        childId: String,
        childName: String,
        amount: Int,
        repaymentPercentage: Int,
        note: String
    ) {
        viewModelScope.launch {
            try {
                val loan = XpLoan(
                    familyId = familyId,
                    parentId = parentId,
                    childId = childId,
                    childName = childName,
                    amount = amount,
                    repaymentPercentage = repaymentPercentage,
                    note = note,
                    status = XpLoanStatus.ACTIVE
                )
                parentControlRepository.createLoan(loan)

                // Credit XP to child immediately
                userRepository.updateUserXp(childId, amount)

                // Refresh loans
                val loans = parentControlRepository.getActiveLoans(familyId, childId)
                _uiState.value = _uiState.value.copy(
                    activeLoans = loans,
                    showLoanDialog = false,
                    successMessage = "Lent $amount XP to $childName!"
                )
                Log.d(TAG, "Created loan: $amount XP to $childName")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating loan", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create loan: ${e.message}",
                    showLoanDialog = false
                )
            }
        }
    }

    fun forgiveLoan(loanId: String, familyId: String) {
        viewModelScope.launch {
            try {
                parentControlRepository.forgiveLoan(loanId, familyId)
                val loans = parentControlRepository.getActiveLoans(familyId, _uiState.value.selectedChildId)
                _uiState.value = _uiState.value.copy(
                    activeLoans = loans,
                    showForgiveDialog = false,
                    successMessage = "Loan forgiven!"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error forgiving loan", e)
            }
        }
    }

    fun cancelLoan(loanId: String, familyId: String) {
        viewModelScope.launch {
            try {
                parentControlRepository.cancelLoan(loanId, familyId)
                val loans = parentControlRepository.getActiveLoans(familyId, _uiState.value.selectedChildId)
                _uiState.value = _uiState.value.copy(activeLoans = loans)
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling loan", e)
            }
        }
    }

    fun showForgiveDialog(loanId: String) {
        _uiState.value = _uiState.value.copy(showForgiveDialog = true, selectedLoanId = loanId)
    }

    fun dismissForgiveDialog() {
        _uiState.value = _uiState.value.copy(showForgiveDialog = false, selectedLoanId = "")
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null, error = null)
    }

    // ── Internal ───────────────────────────────────────────────────────

    private fun saveSettings(settings: ParentControlSettings) {
        viewModelScope.launch {
            try {
                parentControlRepository.saveControlSettings(settings)
                Log.d(TAG, "Saved parent controls for ${settings.childId}")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving controls", e)
                _uiState.value = _uiState.value.copy(error = "Failed to save: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "ParentControlsVM"
    }
}
