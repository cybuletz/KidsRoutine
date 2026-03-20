package com.kidsroutine.feature.daily.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.*
import com.kidsroutine.core.common.util.DateUtils
import com.kidsroutine.feature.daily.domain.GenerateDailyTasksUseCase
import com.kidsroutine.feature.daily.domain.GetDailyStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyUiState(
    val isLoading: Boolean = true,
    val dailyState: DailyStateModel = DailyStateModel(),
    val currentUser: UserModel = UserModel(),
    val error: String? = null
)

@HiltViewModel
class DailyViewModel @Inject constructor(
    private val getDailyState: GetDailyStateUseCase,
    private val generateDailyTasks: GenerateDailyTasksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    // Called from screen with actual user — will be replaced by auth in MVP2
    fun init(user: UserModel) {
        Log.d("DailyViewModel", "init() called with userId=${user.userId}")
        val today = DateUtils.todayString()
        Log.d("DailyViewModel", "today = $today")

        viewModelScope.launch {
            // Try generating tasks first (no-op if already done today)
            generateDailyTasks(user, today)

            // Then observe
            getDailyState(user.userId, today)
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { state ->
                    _uiState.update { it.copy(isLoading = false, dailyState = state, currentUser = user) }
                }
        }
    }
}
