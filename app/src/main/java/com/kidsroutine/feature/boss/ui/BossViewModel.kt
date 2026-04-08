package com.kidsroutine.feature.boss.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.engine.boss_engine.BossEngine
import com.kidsroutine.core.model.BossModel
import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.Season
import com.kidsroutine.feature.boss.data.BossRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class BossUiState(
    val boss: BossModel? = null,
    val isLoading: Boolean = false,
    val damageDealt: Int = 0,
    val mvpUserId: String? = null,
    val timeRemaining: Long = 0L,
    val error: String? = null
)

@HiltViewModel
class BossViewModel @Inject constructor(
    private val bossRepository: BossRepository,
    private val bossEngine: BossEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(BossUiState())
    val uiState: StateFlow<BossUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun loadBoss(familyId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading active boss for family: $familyId")
                val boss = bossRepository.getActiveBoss(familyId)

                if (boss != null) {
                    val now = System.currentTimeMillis()
                    val checkedBoss = bossEngine.checkExpiry(boss, now)
                    if (checkedBoss.isExpired && !boss.isExpired) {
                        bossRepository.saveBoss(checkedBoss)
                    }

                    val mvp = bossEngine.getMvp(checkedBoss)
                    _uiState.value = _uiState.value.copy(
                        boss = checkedBoss,
                        isLoading = false,
                        damageDealt = checkedBoss.totalDamage,
                        mvpUserId = mvp,
                        timeRemaining = (checkedBoss.deadline - now).coerceAtLeast(0L)
                    )
                    startCountdown(checkedBoss.deadline)
                    Log.d(TAG, "Boss loaded: ${checkedBoss.type.displayName}, HP: ${checkedBoss.currentHp}/${checkedBoss.maxHp}")
                } else {
                    Log.d(TAG, "No active boss found for family: $familyId")
                    _uiState.value = _uiState.value.copy(
                        boss = null,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading boss", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load boss"
                )
            }
        }
    }

    fun generateNewBoss(
        familyId: String,
        familySize: Int,
        season: Season = Season.NONE,
        difficulty: DifficultyLevel = DifficultyLevel.MEDIUM
    ) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val week = currentWeekString()
                Log.d(TAG, "Generating new boss for family: $familyId, week: $week, size: $familySize")

                val boss = bossEngine.generateWeeklyBoss(
                    familyId = familyId,
                    familySize = familySize,
                    week = week,
                    season = season,
                    difficulty = difficulty
                )

                val now = System.currentTimeMillis()
                val deadline = now + WEEK_MILLIS
                val bossWithDeadline = boss.copy(
                    startedAt = now,
                    deadline = deadline
                )

                bossRepository.saveBoss(bossWithDeadline)

                _uiState.value = _uiState.value.copy(
                    boss = bossWithDeadline,
                    isLoading = false,
                    damageDealt = 0,
                    mvpUserId = null,
                    timeRemaining = WEEK_MILLIS
                )
                startCountdown(deadline)
                Log.d(TAG, "New boss generated: ${bossWithDeadline.type.displayName}, HP: ${bossWithDeadline.maxHp}")
            } catch (e: Exception) {
                Log.e(TAG, "Error generating boss", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to generate boss"
                )
            }
        }
    }

    private fun startCountdown(deadline: Long) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val remaining = (deadline - System.currentTimeMillis()).coerceAtLeast(0L)
                _uiState.value = _uiState.value.copy(timeRemaining = remaining)
                if (remaining <= 0L) break
                delay(1000L)
            }
        }
    }

    private fun currentWeekString(): String {
        val now = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekNumber = now.get(weekFields.weekOfWeekBasedYear())
        val year = now.get(weekFields.weekBasedYear())
        return "$year-W${weekNumber.toString().padStart(2, '0')}"
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }

    companion object {
        private const val TAG = "BossViewModel"
        private val WEEK_MILLIS = TimeUnit.DAYS.toMillis(7)
    }
}
