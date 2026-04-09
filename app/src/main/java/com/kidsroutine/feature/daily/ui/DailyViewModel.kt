package com.kidsroutine.feature.daily.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.kidsroutine.core.model.*
import com.kidsroutine.core.common.util.DateUtils
import com.kidsroutine.feature.daily.data.DailyRepository
import com.kidsroutine.feature.daily.domain.GenerateDailyTasksUseCase
import com.kidsroutine.feature.daily.domain.GetDailyStateUseCase
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.daily.data.StoryArcRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.kidsroutine.feature.generation.data.GeneratedTask
import com.kidsroutine.feature.generation.data.TaskSaveRepository
import com.kidsroutine.feature.challenges.data.ChallengeRepository

data class DailyUiState(
    val isLoading: Boolean = true,
    val dailyState: DailyStateModel = DailyStateModel(),
    val currentUser: UserModel = UserModel(),
    val activeStoryArc: StoryArc? = null,
    val activeChallenges: List<Pair<ChallengeModel, ChallengeProgress>> = emptyList(),
    val error: String? = null,
    val lootBoxClaimedToday: Boolean = false
)

@HiltViewModel
class DailyViewModel @Inject constructor(
    private val getDailyState: GetDailyStateUseCase,
    private val generateDailyTasks: GenerateDailyTasksUseCase,
    private val userRepository: UserRepository,
    private val storyArcRepository: StoryArcRepository,
    private val taskSaveRepository: TaskSaveRepository,
    private val dailyRepository: DailyRepository,
    private val challengeRepository: ChallengeRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    // Guard: prevent re-initialization when navigating back to this screen
    private var initializedFor: String = ""

    // Holds the Firestore listener so we can remove it when the ViewModel is cleared
    private var assignmentListener: ListenerRegistration? = null

    fun init(user: UserModel) {
        val today = DateUtils.todayString()
        val key   = "${user.familyId}_${user.userId}_$today"  // ✅ NEW: Include familyId

        if (initializedFor == key) {
            Log.d("DailyViewModel", "init() skipped — already initialized for $key")
            return
        }
        initializedFor = key
        Log.d("DailyViewModel", "init() starting for familyId=${user.familyId}, userId=${user.userId}, date=$today")

        // Step 1: Run initial generation (idempotent — skips if already done today)
        viewModelScope.launch {
            generateDailyTasks(user, today)
        }

        // Step 2: Observe Room — emits whenever tasks change (completion, new inserts)
        // ✅ NEW: Pass familyId to getDailyState
        viewModelScope.launch {
            getDailyState(user.familyId, user.userId, today)
                .catch { e ->
                    Log.e("DailyViewModel", "Daily state error", e)
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { dailyState ->
                    _uiState.update { it.copy(isLoading = false, dailyState = dailyState) }
                    Log.d("DailyViewModel", "✅ Tasks loaded: ${dailyState.tasks.size} pending, ${dailyState.completedCount} completed")
                }
        }

        // Step 3: Observe user XP/profile changes
        viewModelScope.launch {
            userRepository.observeUser(user.userId)
                .catch { e -> Log.w("DailyViewModel", "User observe error: ${e.message}") }
                .collect { observedUser ->
                    if (observedUser != null) {
                        _uiState.update { it.copy(currentUser = observedUser) }
                    }
                }
        }

        // Step 4: Set current user immediately (don't wait for Firestore)
        _uiState.update { it.copy(currentUser = user) }

        // Step 5: Story arc
        loadActiveStoryArc(user.familyId)

        // Step 6: Load active challenges
        loadActiveChallenges(user.userId, user.familyId)

        // Step 7: Live Firestore listener for newly assigned tasks — fires ONLY on real changes
        listenForNewAssignments(user, today)
    }

    /**
     * Attaches a Firestore real-time listener to taskAssignments for this child.
     * Handles BOTH new assignments AND deletions.
     */
    private fun listenForNewAssignments(user: UserModel, date: String) {
        // Remove any previous listener first
        assignmentListener?.remove()

        Log.d("DailyViewModel", "🔌 STARTING listener for child: ${user.userId} in family: ${user.familyId}")

        // ✅ NEW PATH: Listen to /families/{familyId}/users/{userId}/assignments/
        assignmentListener = firestore
            .collection("families").document(user.familyId)
            .collection("users").document(user.userId)
            .collection("assignments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DailyViewModel", "Assignment listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    Log.d("DailyViewModel", "⚠️ Snapshot is NULL")
                    return@addSnapshotListener
                }

                val documentChanges = snapshot.documentChanges
                Log.d("DailyViewModel", "📊 Document changes count: ${documentChanges.size}")

                val addedChanges = documentChanges.filter {
                    it.type == com.google.firebase.firestore.DocumentChange.Type.ADDED
                            && it.document.getString("status") == "ASSIGNED"
                }

                val modifiedChanges = documentChanges.filter {
                    it.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED
                            && it.document.getString("status") == "ASSIGNED"
                }

                val deletedChanges = documentChanges.filter {
                    it.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED
                }

                // ✅ Handle deleted assignments (parent deleted a task)
                if (deletedChanges.isNotEmpty()) {
                    Log.d("DailyViewModel", "🗑️ ${deletedChanges.size} task assignment(s) deleted")
                    viewModelScope.launch {
                        val deletedTaskIds = deletedChanges.mapNotNull { it.document.getString("taskId") }

                        // Remove these task instances from Room/UI
                        val currentState = _uiState.value
                        val updatedTasks = currentState.dailyState.tasks.filterNot { task ->
                            task.task.id in deletedTaskIds
                        }

                        // ✅ NEW: Delete from Room database
                        for (deletedTaskId in deletedTaskIds) {
                            val taskToDelete = currentState.dailyState.tasks.find { it.task.id == deletedTaskId }
                            if (taskToDelete != null) {
                                dailyRepository.deleteTaskInstance(
                                    currentState.currentUser.familyId,
                                    currentState.dailyState.userId,
                                    taskToDelete.instanceId
                                )
                                Log.d("DailyViewModel", "🗑️ Deleted from Room: ${taskToDelete.task.title}")
                            }
                        }

                        // Update the dailyState with filtered tasks
                        val updatedDailyState = currentState.dailyState.copy(tasks = updatedTasks)
                        _uiState.value = currentState.copy(dailyState = updatedDailyState)

                        Log.d("DailyViewModel", "✅ Removed ${deletedTaskIds.size} deleted tasks from UI and Room")
                    }
                }
            }
    }

    private fun loadActiveStoryArc(familyId: String) {
        viewModelScope.launch {
            try {
                val arc = storyArcRepository.getActiveArc(familyId)
                _uiState.update { it.copy(activeStoryArc = arc) }
            } catch (e: Exception) {
                Log.e("DailyViewModel", "Error loading story arc", e)
            }
        }
    }

    private fun loadActiveChallenges(userId: String, familyId: String) {
        viewModelScope.launch {
            try {
                val today = DateUtils.todayString()
                val progressList = challengeRepository.getActiveChallenges(userId, familyId)
                    .filter { it.lastCompletedDate != today }
                val pairs = progressList.mapNotNull { progress ->
                    val challenge = challengeRepository.getChallenge(progress.challengeId)
                    if (challenge != null) Pair(challenge, progress) else null
                }
                _uiState.update { it.copy(activeChallenges = pairs) }
                Log.d("DailyViewModel", "✅ Challenges loaded: ${pairs.size}")
            } catch (e: Exception) {
                Log.e("DailyViewModel", "Error loading challenges", e)
            }
        }
    }

    // ✅ NEW: Force refresh tasks (called from UI)
    fun forceRefresh() {
        val state = _uiState.value
        if (state.dailyState.userId.isNotEmpty() && state.currentUser.familyId.isNotEmpty()) {
            viewModelScope.launch {
                dailyRepository.refreshTasksForDate(
                    state.currentUser.familyId,
                    state.dailyState.userId,
                    DateUtils.todayString()
                )
                Log.d("DailyViewModel", "✅ Tasks refreshed")
            }
        }
    }

    // ✅ NEW: Add AI-suggested task
    fun addSuggestedTask(task: TaskModel) {
        val user = _uiState.value.currentUser
        viewModelScope.launch {
            try {
                taskSaveRepository.assignTaskToChild(
                    taskId = task.id,
                    childId = user.userId,
                    familyId = user.familyId
                )
                Log.d("DailyViewModel", "✅ Suggested task added: ${task.title}")
                // Refresh to show the new task
                forceRefresh()
            } catch (e: Exception) {
                Log.e("DailyViewModel", "❌ Error adding suggested task", e)
            }
        }
    }

    fun markLootBoxClaimed() {
        _uiState.update { it.copy(lootBoxClaimedToday = true) }
    }

    override fun onCleared() {
        super.onCleared()
        assignmentListener?.remove()
        Log.d("DailyViewModel", "🧹 ViewModel cleared, listener removed")
    }
}