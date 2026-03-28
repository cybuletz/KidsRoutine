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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.kidsroutine.feature.daily.data.StoryArcRepository
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
    private val dailyRepository: DailyRepository,      // ← injected directly for mergeAssignedTasks
    private val firestore: FirebaseFirestore            // ← injected for the snapshot listener
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    // Guard: prevent re-initialization when navigating back to this screen
    private var initializedFor: String = ""

    // Holds the Firestore listener so we can remove it when the ViewModel is cleared
    private var assignmentListener: ListenerRegistration? = null

    fun init(user: UserModel) {
        val today = DateUtils.todayString()
        val key   = "${user.userId}_$today"

        if (initializedFor == key) {
            Log.d("DailyViewModel", "init() skipped — already initialized for $key")
            return
        }
        initializedFor = key
        Log.d("DailyViewModel", "init() starting for $key")

        // Step 1: Run initial generation (idempotent — skips if already done today)
        viewModelScope.launch {
            generateDailyTasks(user, today)
        }

        // Step 2: Observe Room — emits whenever tasks change (completion, new inserts)
        viewModelScope.launch {
            getDailyState(user.userId, today)
                .catch { e ->
                    Log.e("DailyViewModel", "Daily state error", e)
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { dailyState ->
                    _uiState.update { it.copy(isLoading = false, dailyState = dailyState) }
                    Log.d("DailyViewModel", "Tasks loaded: ${dailyState.tasks.size}")
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

        assignmentListener = firestore
            .collection("taskAssignments")
            .whereEqualTo("childId", user.userId)
            .whereEqualTo("status", "ASSIGNED")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DailyViewModel", "Assignment listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                // ✨ Process ALL changes (ADDED + REMOVED)
                val documentChanges = snapshot.documentChanges

                // Handle ADDED assignments
                val addedChanges = documentChanges.filter {
                    it.type == com.google.firebase.firestore.DocumentChange.Type.ADDED
                }

                // ✨ Handle REMOVED assignments (task deleted)
                val removedChanges = documentChanges.filter {
                    it.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED
                }

                if (addedChanges.isNotEmpty()) {
                    Log.d("DailyViewModel", "🔔 ${addedChanges.size} new task assignment(s)")
                    viewModelScope.launch {
                        val newInstances = mutableListOf<TaskInstance>()

                        for (change in addedChanges) {
                            val taskId = change.document.getString("taskId") ?: continue
                            try {
                                val taskDoc = firestore
                                    .collection("tasks")
                                    .document(taskId)
                                    .get()
                                    .await()
                                val taskModel = taskDoc.toObject(TaskModel::class.java)
                                if (taskModel != null && taskModel.title.isNotBlank()) {
                                    newInstances.add(
                                        TaskInstance(
                                            instanceId            = "${taskId}_assigned",
                                            templateId            = taskId,
                                            task                  = taskModel,
                                            assignedDate          = date,
                                            userId                = user.userId,
                                            injectedByChallengeId = null
                                        )
                                    )
                                    Log.d("DailyViewModel", "New task: ${taskModel.title}")
                                }
                            } catch (e: Exception) {
                                Log.e("DailyViewModel", "Failed to fetch task $taskId: ${e.message}")
                            }
                        }

                        if (newInstances.isNotEmpty()) {
                            dailyRepository.mergeAssignedTasks(user.userId, date, newInstances)
                        }
                    }
                }

                // ✨ Handle REMOVED assignments
                if (removedChanges.isNotEmpty()) {
                    Log.d("DailyViewModel", "🗑️ ${removedChanges.size} task assignment(s) deleted")
                    viewModelScope.launch {
                        for (change in removedChanges) {
                            val taskId = change.document.getString("taskId") ?: continue
                            try {
                                val instanceId = "${taskId}_assigned"
                                Log.d("DailyViewModel", "Deleting task instance: $instanceId")
                                dailyRepository.deleteTaskInstance(user.userId, instanceId)
                                // Room Flow will emit and update UI automatically
                            } catch (e: Exception) {
                                Log.e("DailyViewModel", "Error deleting task instance: ${e.message}")
                            }
                        }
                    }
                }
            }
    }

    // Called only by push-notification refresh — bypasses the guard key
    fun forceRefresh(user: UserModel) {
        initializedFor = ""
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

    override fun onCleared() {
        super.onCleared()
        assignmentListener?.remove()  // ← clean up Firestore listener when ViewModel is destroyed
        Log.d("DailyViewModel", "ViewModel cleared, Firestore listener removed")
    }
}