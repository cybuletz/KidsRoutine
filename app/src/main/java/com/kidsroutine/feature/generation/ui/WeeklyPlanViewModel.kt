package com.kidsroutine.feature.generation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.EntitlementsRepository
import com.kidsroutine.core.model.PlanType
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.generation.data.GeneratedWeeklyPlan
import com.kidsroutine.feature.generation.data.GenerationRepository
import com.kidsroutine.feature.generation.data.WeekTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeeklyPlanUiState(
    val isLoading: Boolean = false,
    val weeklyPlan: GeneratedWeeklyPlan? = null,
    val selectedTheme: WeekTheme = WeekTheme.ADVENTURE,
    val selectedGoals: Set<String> = emptySet(),
    val error: String? = null,
    val quotaRemaining: Int = 4,
    val isCached: Boolean = false,
    val showResult: Boolean = false,
    val isPro: Boolean = false    // drives the upgrade gate UI
)

@HiltViewModel
class WeeklyPlanViewModel @Inject constructor(
    private val repository: GenerationRepository,
    private val entitlementsRepository: EntitlementsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyPlanUiState())
    val uiState: StateFlow<WeeklyPlanUiState> = _uiState.asStateFlow()

    fun selectTheme(theme: WeekTheme) {
        _uiState.value = _uiState.value.copy(
            selectedTheme = theme,
            weeklyPlan    = null,
            showResult    = false
        )
    }

    fun toggleGoal(goal: String) {
        val current = _uiState.value.selectedGoals.toMutableSet()
        if (goal in current) current.remove(goal) else current.add(goal)
        _uiState.value = _uiState.value.copy(selectedGoals = current)
    }

    fun generateWeeklyPlan(
        currentUser: UserModel,
        familyChildren: List<UserModel>   // pass from ParentDashboardViewModel
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading  = true,
                error      = null,
                weeklyPlan = null,
                showResult = false
            )

            try {
                // Check entitlements
                val entitlements = entitlementsRepository.getEntitlements(currentUser.userId, currentUser.familyId)
                val isPro = entitlements.planType != PlanType.FREE
                _uiState.value = _uiState.value.copy(isPro = isPro)

                if (!entitlements.canGenerateWeeklyPlan()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = "🔒 Weekly plans require ${PlanType.PRO.displayName}. Upgrade to unlock!",
                        isPro     = false
                    )
                    return@launch
                }

                // Build children list for Cloud Function
                val children = if (familyChildren.isNotEmpty()) {
                    familyChildren.map { child ->
                        mapOf(
                            "name" to child.displayName,
                            "age"  to (child.age.takeIf { it > 0 } ?: 10)
                        )
                    }
                } else {
                    // Fallback: single child placeholder if family not yet loaded
                    listOf(mapOf("name" to "Child", "age" to 10))
                }

                val goals = _uiState.value.selectedGoals.toList()
                val theme = _uiState.value.selectedTheme.name
                val tier  = entitlements.planType.name

                val result = repository.generateWeeklyPlan(
                    familyId    = currentUser.familyId,
                    children    = children,
                    familyGoals = goals,
                    tier        = tier,
                    weekTheme   = theme
                )

                result.onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading      = false,
                        weeklyPlan     = response.weeklyPlan,
                        quotaRemaining = response.quotaRemaining,
                        isCached       = response.cached,
                        showResult     = true,
                        isPro          = true
                    )
                }

                result.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = error.message ?: "Failed to generate weekly plan"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun reset() {
        _uiState.value = WeeklyPlanUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

// Extension until UserModel gets an `age` field in a later batch
private val UserModel.age: Int get() = 10   // safe fallback