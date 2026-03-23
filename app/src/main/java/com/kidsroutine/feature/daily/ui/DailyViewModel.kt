package com.kidsroutine.feature.daily.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.*
import com.kidsroutine.core.common.util.DateUtils
import com.kidsroutine.feature.daily.domain.GenerateDailyTasksUseCase
import com.kidsroutine.feature.daily.domain.GetDailyStateUseCase
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.tasks.data.TaskRepository
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
    private val generateDailyTasks: GenerateDailyTasksUseCase,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    fun init(user: UserModel) {
        Log.d("DailyViewModel", "init() called with userId=${user.userId}")
        val today = DateUtils.todayString()

        viewModelScope.launch {
            generateDailyTasks(user, today)

            combine(
                getDailyState(user.userId, today),
                userRepository.observeUser(user.userId),
                taskRepository.observeChildAssignedTasks(user.userId, user.familyId)
            ) { dailyState, observedUser, assignedTasks ->
                Log.d("DailyViewModel", "Got update: user.xp=${observedUser?.xp}, dailyState.completedCount=${dailyState.completedCount}, assignedTasks=${assignedTasks.size}")

                // Merge assigned tasks with daily tasks
                val mergedTasks = dailyState.tasks.toMutableList()
                assignedTasks.forEach { assignedTask ->
                    if (mergedTasks.none { it.task.id == assignedTask.id }) {
                        val newInstance = TaskInstance(
                            instanceId = "${assignedTask.id}_${System.currentTimeMillis()}",
                            templateId = assignedTask.id,
                            task = assignedTask,
                            resolvedValues = emptyMap(),
                            assignedDate = today,
                            userId = user.userId,
                            injectedByChallengeId = null
                        )
                        mergedTasks.add(newInstance)
                        Log.d("DailyViewModel", "Added newly assigned task: ${assignedTask.title}")
                    }
                }

                Triple(dailyState.copy(tasks = mergedTasks), observedUser ?: user, assignedTasks)
            }
                .catch { e ->
                    Log.e("DailyViewModel", "Error", e)
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { (dailyState, observedUser, _) ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        dailyState = dailyState,
                        currentUser = observedUser
                    ) }
                }
        }
    }
}