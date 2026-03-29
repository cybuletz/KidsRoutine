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

data class DailyUiState(
    val isLoading: Boolean = true,
    val dailyState: DailyStateModel = DailyStateModel(),
    val currentUser: UserModel = UserModel(),
    val activeStoryArc: StoryArc? = null,
    val error: String? = null
)

@HiltViewModel
class DailyViewModel @Inject constructor(
    private val getDailyState: GetDailyStateUseCase,
    private val generateDailyTasks: GenerateDailyTasksUseCase,
    private val userRepository: UserRepository,
    private val storyArcRepository: StoryArcRepository,
    private val taskSaveRepository: TaskSaveRepository,
    private val dailyRepository: DailyRepository,
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

        // Step 6: Live Firestore listener for newly assigned tasks — fires ONLY on real changes
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

        assignmentListener = firestore
            .collection("taskAssignments")
            .whereEqualTo("childId", user.userId)
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

                // ✅ Handle new assignments
                if (addedChanges.isNotEmpty()) {
                    Log.d("DailyViewModel", "🔔 ${addedChanges.size} new task assignment(s)")
                    viewModelScope.launch {
                        val newInstances = mutableListOf<TaskInstance>()

                        for (change in addedChanges) {
                            val taskId = change.document.getString("taskId") ?: continue
                            try {
                                // ✅ CHANGED: Fetch from family-scoped path
                                val taskDoc = firestore
                                    .collection("families").document(user.familyId)
                                    .collection("tasks").document(taskId)
                                    .get()
                                    .await()

                                val taskModel = taskDoc.toObject(TaskModel::class.java)
                                if (taskModel != null && taskModel.title.isNotBlank()) {
                                    newInstances.add(
                                        TaskInstance(
                                            instanceId           = "new_${taskId}_${System.currentTimeMillis()}",
                                            templateId           = taskId,
                                            task                 = taskModel,
                                            assignedDate         = date,
                                            userId               = user.userId,
                                            injectedByChallengeId = null,
                                            status               = TaskStatus.PENDING,
                                            completedAt          = 0L
                                        )
                                    )
                                    Log.d("DailyViewModel", "New task fetched: ${taskModel.title}")
                                }
                            } catch (e: Exception) {
                                Log.e("DailyViewModel", "Error fetching task $taskId from family ${user.familyId}", e)
                            }
                        }

                        if (newInstances.isNotEmpty()) {
                            // ✅ NEW: Pass familyId to mergeAssignedTasks
                            dailyRepository.mergeAssignedTasks(user.familyId, user.userId, date, newInstances)
                            Log.d("DailyViewModel", "✅ Merged ${newInstances.size} new assigned tasks")
                        }
                    }
                }

                // ✅ Handle modified assignments (task updated by parent)
                if (modifiedChanges.isNotEmpty()) {
                    Log.d("DailyViewModel", "✏️ ${modifiedChanges.size} task assignment(s) modified")
                    viewModelScope.launch {
                        // ✅ NEW: Pass familyId to refreshTasksForDate
                        dailyRepository.refreshTasksForDate(user.familyId, user.userId, date)
                        Log.d("DailyViewModel", "✅ Refreshed tasks for modifications")
                    }
                }

                // ✅ Handle deleted assignments (parent deleted a task)
                if (deletedChanges.isNotEmpty()) {
                    Log.d("DailyViewModel", "🗑️ ${deletedChanges.size} task assignment(s) deleted")
                    viewModelScope.launch {
                        // When task is deleted, trigger a refresh to update progress
                        // ✅ NEW: Pass familyId to refreshTasksForDate
                        dailyRepository.refreshTasksForDate(user.familyId, user.userId, date)
                        Log.d("DailyViewModel", "✅ Refreshed tasks after deletion")
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

    override fun onCleared() {
        super.onCleared()
        assignmentListener?.remove()
        Log.d("DailyViewModel", "🧹 ViewModel cleared, listener removed")
    }
}