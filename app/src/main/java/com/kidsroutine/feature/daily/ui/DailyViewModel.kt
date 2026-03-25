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
import com.kidsroutine.feature.daily.data.StoryArcRepository
import com.kidsroutine.feature.generation.data.GeneratedTask
import com.kidsroutine.feature.generation.data.TaskSaveRepository


data class DailyUiState(
    val isLoading: Boolean = true,
    val dailyState: DailyStateModel = DailyStateModel(),
    val currentUser: UserModel = UserModel(),
    val activeStoryArc: com.kidsroutine.core.model.StoryArc? = null,
    val error: String? = null
)

@HiltViewModel
class DailyViewModel @Inject constructor(
    private val getDailyState: GetDailyStateUseCase,
    private val generateDailyTasks: GenerateDailyTasksUseCase,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
    private val storyArcRepository: StoryArcRepository,
    private val taskSaveRepository: TaskSaveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    // Guard: track which userId+date we have already initialized for,
    // so navigating away and back does NOT re-trigger the whole Firestore pipeline
    private var initializedFor: String = ""

    fun init(user: UserModel) {
        val today = DateUtils.todayString()
        val key   = "${user.userId}_$today"

        // ── PERFORMANCE FIX: skip re-init if same user+date is already live ──
        if (initializedFor == key) {
            Log.d("DailyViewModel", "init() skipped — already initialized for $key")
            return
        }
        initializedFor = key
        Log.d("DailyViewModel", "init() starting for $key")

        viewModelScope.launch {
            // Generate today's task instances once (idempotent — safe to call multiple times)
            generateDailyTasks(user, today)

            combine(
                getDailyState(user.userId, today),
                userRepository.observeUser(user.userId),
                taskRepository.observeChildAssignedTasks(user.userId, user.familyId)
            ) { dailyState, observedUser, assignedTasks ->
                Log.d("DailyViewModel", "Pipeline update: xp=${observedUser?.xp}, completed=${dailyState.completedCount}, assigned=${assignedTasks.size}")

                // Merge parent-assigned tasks into the daily list (no duplicates)
                val mergedTasks = dailyState.tasks.toMutableList()
                assignedTasks.forEach { assignedTask ->
                    if (mergedTasks.none { it.task.id == assignedTask.id }) {
                        mergedTasks.add(
                            TaskInstance(
                                instanceId             = "${assignedTask.id}_${today}",
                                templateId             = assignedTask.id,
                                task                   = assignedTask,
                                resolvedValues         = emptyMap(),
                                assignedDate           = today,
                                userId                 = user.userId,
                                injectedByChallengeId  = null
                            )
                        )
                    }
                }

                // ── Filter: completed tasks disappear after 1.5-second linger ──
                val now = System.currentTimeMillis()
                val displayTasks = mergedTasks.filter { instance ->
                    instance.status != TaskStatus.COMPLETED ||
                            (now - instance.completedAt) < 1_500L
                }

                Triple(dailyState.copy(tasks = displayTasks), observedUser ?: user, assignedTasks)
            }
                .catch { e ->
                    Log.e("DailyViewModel", "Pipeline error", e)
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { (dailyState, observedUser, _) ->
                    _uiState.update {
                        it.copy(
                            isLoading   = false,
                            dailyState  = dailyState,
                            currentUser = observedUser
                        )
                    }
                }
        }

        loadActiveStoryArc(user.familyId)
    }

    // Called only by push-notification refresh — bypasses the guard key
    fun forceRefresh(user: UserModel) {
        initializedFor = ""   // clear the guard so init() re-runs fully
        init(user)
    }

    fun addSuggestedTask(task: GeneratedTask) {
        val user = _uiState.value.currentUser
        if (user.userId.isBlank()) return

        viewModelScope.launch {
            try {
                val result = taskSaveRepository.saveAndAssignToFamily(
                    generatedTask = task,
                    familyId      = user.familyId,
                    childrenIds   = listOf(user.userId)
                )
                result.onSuccess {
                    Log.d("DailyVM", "AI suggested task added: ${task.title}")
                    // The existing combine() pipeline observes taskRepository,
                    // which will pick up the new task automatically — no manual reload needed.
                }
                result.onFailure { e ->
                    Log.e("DailyVM", "Failed to add suggested task: ${e.message}")
                    _uiState.update { it.copy(error = "Couldn't add task: ${e.message}") }
                }
            } catch (e: Exception) {
                Log.e("DailyVM", "Exception adding suggested task", e)
            }
        }
    }


    private fun loadActiveStoryArc(familyId: String) {
        if (familyId.isEmpty()) return
        viewModelScope.launch {
            try {
                val arc = storyArcRepository.getActiveArc(familyId)
                _uiState.update { it.copy(activeStoryArc = arc) }
            } catch (e: Exception) {
                Log.w("DailyViewModel", "Could not load story arc: ${e.message}")
            }
        }
    }

}
