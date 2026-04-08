package com.kidsroutine.feature.spinwheel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.engine.spin_engine.DailyRewardEngine
import com.kidsroutine.core.model.DailySpinState
import com.kidsroutine.core.model.PlanType
import com.kidsroutine.core.model.SpinWheelResult
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.spinwheel.data.SpinWheelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class SpinPhase {
    IDLE,
    SPINNING,
    REVEALING,
    DONE
}

data class SpinWheelUiState(
    val isLoading: Boolean = false,
    val phase: SpinPhase = SpinPhase.IDLE,
    val dailyState: DailySpinState = DailySpinState(),
    val lastResult: SpinWheelResult? = null,
    val spinsRemaining: Int = 0,
    val currentXp: Int = 0,
    val currentUserId: String = "",
    val error: String? = null
)

@HiltViewModel
class SpinWheelViewModel @Inject constructor(
    private val repository: SpinWheelRepository,
    private val rewardEngine: DailyRewardEngine,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpinWheelUiState())
    val uiState: StateFlow<SpinWheelUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private var xpObserveJob: Job? = null

    fun loadState(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val today = LocalDate.now().format(dateFormatter)
                var state = repository.getDailyState(userId, today)

                if (state == null) {
                    state = rewardEngine.createDailyState(userId, today, PlanType.FREE)
                    repository.saveDailyState(state)
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dailyState = state,
                    spinsRemaining = state.maxSpins - state.spinsUsed,
                    phase = if (state.canSpin) SpinPhase.IDLE else SpinPhase.DONE
                )

                // Observe user XP
                xpObserveJob?.cancel()
                xpObserveJob = viewModelScope.launch {
                    userRepository.observeUser(userId).collect { user ->
                        _uiState.value = _uiState.value.copy(
                            currentXp = user.xp,
                            currentUserId = userId
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun spin() {
        val current = _uiState.value
        if (current.phase == SpinPhase.SPINNING || !current.dailyState.canSpin) return

        if (current.currentXp < SPIN_COST) {
            _uiState.value = current.copy(error = "Not enough XP! You need $SPIN_COST XP to spin.")
            return
        }

        viewModelScope.launch {
            userRepository.updateUserXp(current.currentUserId, -SPIN_COST)
            // Determine the result before animation so the wheel knows where to land
            val (updatedState, result) = rewardEngine.spin(current.dailyState)

            _uiState.value = current.copy(
                phase = SpinPhase.SPINNING,
                lastResult = result,
                error = null
            )

            // Persist while the wheel animates
            try {
                repository.saveDailyState(updatedState)
                repository.saveSpinResult(updatedState.userId, result)
            } catch (e: Exception) {
                // Non-fatal: state was already computed locally
            }

            // Wait for the spin animation to finish (matches Animatable duration)
            delay(SPIN_DURATION_MS)

            _uiState.value = _uiState.value.copy(phase = SpinPhase.REVEALING)

            delay(REVEAL_PAUSE_MS)

            _uiState.value = _uiState.value.copy(
                phase = SpinPhase.DONE,
                dailyState = updatedState,
                spinsRemaining = updatedState.maxSpins - updatedState.spinsUsed
            )
        }
    }

    fun resetForNextSpin() {
        val current = _uiState.value
        if (current.dailyState.canSpin) {
            _uiState.value = current.copy(
                phase = SpinPhase.IDLE,
                lastResult = null
            )
        }
    }

    companion object {
        const val SPIN_DURATION_MS = 3000L
        const val REVEAL_PAUSE_MS = 1500L
        const val SPIN_COST = 3
    }
}
