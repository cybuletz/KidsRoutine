package com.kidsroutine.feature.generation.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.EntitlementsRepository
import com.kidsroutine.core.model.PlanType
import com.kidsroutine.core.model.UserEntitlements
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.generation.data.GeneratedChallenge
import com.kidsroutine.feature.generation.data.GeneratedTask
import com.kidsroutine.feature.generation.data.GenerationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.kidsroutine.feature.generation.data.TaskSaveRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel

data class GenerationUiState(
    val isLoading: Boolean = false,
    val generatedTasks: List<GeneratedTask> = emptyList(),
    val generatedChallenges: List<GeneratedChallenge> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val quotaRemaining: Int = 3,
    val isCached: Boolean = false,
    val selectedDifficulty: String = "MEDIUM",
    val selectedPreferences: Set<String> = setOf("🎨 Creative"),
    val selectedGoals: Set<String> = setOf("🏃 Health"),
    val selectedAge: Int = 8,
    val generationHistory: List<GeneratedTask> = emptyList(),
    val entitlements: UserEntitlements? = null   // ← NEW: loaded on first generate
)

@HiltViewModel
class GenerationViewModel @Inject constructor(
    private val repository: GenerationRepository,
    private val taskSaveRepository: TaskSaveRepository,
    private val firestore: FirebaseFirestore,
    private val entitlementsRepository: EntitlementsRepository   // ← NEW injection
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerationUiState())
    val uiState: StateFlow<GenerationUiState> = _uiState.asStateFlow()

    // ════════════════════════════════════════════════════════════════════════
    // PREFERENCE MANAGEMENT
    // ════════════════════════════════════════════════════════════════════════

    fun setAge(age: Int) {
        _uiState.value = _uiState.value.copy(selectedAge = age)
        Log.d("GenerationVM", "Age selected: $age")
    }

    fun toggleDifficulty(difficulty: String) {
        _uiState.value = _uiState.value.copy(selectedDifficulty = difficulty)
        Log.d("GenerationVM", "Difficulty selected: $difficulty")
    }

    fun togglePreference(preference: String) {
        val current = _uiState.value.selectedPreferences.toMutableSet()
        if (preference in current) current.remove(preference) else current.add(preference)
        _uiState.value = _uiState.value.copy(selectedPreferences = current)
        Log.d("GenerationVM", "Preferences updated: $current")
    }

    fun toggleGoal(goal: String) {
        val current = _uiState.value.selectedGoals.toMutableSet()
        if (goal in current) current.remove(goal) else current.add(goal)
        _uiState.value = _uiState.value.copy(selectedGoals = current)
        Log.d("GenerationVM", "Goals updated: $current")
    }

    // ════════════════════════════════════════════════════════════════════════
    // TASK GENERATION
    // ════════════════════════════════════════════════════════════════════════

    fun generateTasks(
        currentUser: UserModel,
        childAge: Int,
        recentCompletions: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading      = true,
                error          = null,
                generatedTasks = emptyList()   // clear old results immediately
            )

            try {
                Log.d("GenerationVM", "Generating tasks for age $childAge...")

                // Load entitlements (cached after first call)
                val entitlements = entitlementsRepository.getEntitlements(currentUser.userId)
                _uiState.value = _uiState.value.copy(entitlements = entitlements)

                if (_uiState.value.quotaRemaining <= 0) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = "🚫 Daily quota reached. Try again tomorrow or upgrade to ${PlanType.PRO.displayName}!"
                    )
                    return@launch
                }

                val preferences = _uiState.value.selectedPreferences
                    .mapNotNull { displayLabelToCategory(it) }

                // ✅ Use real tier from entitlements — no more hardcoded "FREE"
                val tier = entitlements.planType.name

                val result = repository.generateTasks(
                    familyId          = currentUser.familyId,
                    childAge          = childAge,
                    preferences       = preferences,
                    recentCompletions = recentCompletions,
                    tier              = tier,
                    count             = 1
                )

                result.onSuccess { response ->
                    val newHistory = (_uiState.value.generationHistory + response.tasks).takeLast(10)
                    _uiState.value = _uiState.value.copy(
                        isLoading         = false,
                        generatedTasks    = response.tasks,
                        quotaRemaining    = response.quotaRemaining,
                        isCached          = response.cached,
                        generationHistory = newHistory,
                        successMessage    = "✅ Generated ${response.tasks.size} tasks!"
                    )
                    Log.d("GenerationVM", "Success: ${response.tasks.size} tasks, tier=$tier")
                }

                result.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = error.message ?: "Failed to generate tasks"
                    )
                    Log.e("GenerationVM", "Error: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Unknown error"
                )
                Log.e("GenerationVM", "Exception: ${e.message}", e)
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // CHALLENGE GENERATION
    // ════════════════════════════════════════════════════════════════════════

    fun generateChallenges(
        currentUser: UserModel,
        childAge: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d("GenerationVM", "Generating challenges...")

                // Load entitlements
                val entitlements = entitlementsRepository.getEntitlements(currentUser.userId)
                _uiState.value = _uiState.value.copy(entitlements = entitlements)

                // Gate: challenges require PRO or above
                if (!entitlements.canGenerateChallenges()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = "🔒 Challenge generation requires ${PlanType.PRO.displayName}. Upgrade to unlock!"
                    )
                    return@launch
                }

                val goals = _uiState.value.selectedGoals
                    .mapNotNull { displayLabelToCategory(it) }

                // ✅ Use real tier from entitlements
                val tier = entitlements.planType.name

                val result = repository.generateChallenges(
                    familyId  = currentUser.familyId,
                    childAge  = childAge,
                    goals     = goals,
                    tier      = tier,
                    count     = 1
                )

                result.onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading          = false,
                        generatedChallenges = response.challenges,
                        quotaRemaining     = response.quotaRemaining,
                        isCached           = response.cached,
                        successMessage     = "✅ Generated ${response.challenges.size} challenges!"
                    )
                    Log.d("GenerationVM", "Success: ${response.challenges.size} challenges, tier=$tier")
                }

                result.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = error.message ?: "Failed to generate challenges"
                    )
                    Log.e("GenerationVM", "Error: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Unknown error"
                )
                Log.e("GenerationVM", "Exception: ${e.message}", e)
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // TASK SAVING & MANAGEMENT
    // ════════════════════════════════════════════════════════════════════════

    fun saveGeneratedTask(
        task: GeneratedTask,
        familyId: String,
        childrenIds: List<String>
    ) {
        viewModelScope.launch {
            try {
                Log.d("GenerationVM", "Saving task: ${task.title}")
                if (childrenIds.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        error          = "Please select at least one child",
                        successMessage = null
                    )
                    return@launch
                }
                val result = taskSaveRepository.saveAndAssignToFamily(
                    generatedTask = task,
                    familyId      = familyId,
                    childrenIds   = childrenIds
                )
                result.onSuccess { taskId ->
                    addToFavorites(task)
                    _uiState.value = _uiState.value.copy(
                        successMessage = "✅ Task assigned to ${childrenIds.size} child${if (childrenIds.size != 1) "ren" else ""}!",
                        error          = null
                    )
                    Log.d("GenerationVM", "Task saved: $taskId")
                }
                result.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error          = error.message ?: "Failed to save task",
                        successMessage = null
                    )
                    Log.e("GenerationVM", "Error saving: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error          = "Error: ${e.message}",
                    successMessage = null
                )
                Log.e("GenerationVM", "Exception: ${e.message}", e)
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // FAVORITES & HISTORY
    // ════════════════════════════════════════════════════════════════════════

    fun addToFavorites(task: GeneratedTask) {
        Log.d("GenerationVM", "Added to favorites: ${task.title}")
    }

    fun shareToMarketplace(task: GeneratedTask, familyId: String) {
        Log.d("GenerationVM", "Shared to marketplace: ${task.title}")
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    // ════════════════════════════════════════════════════════════════════════
    // LABEL MAPPING
    // ════════════════════════════════════════════════════════════════════════

    private fun displayLabelToCategory(label: String): String? = when (label) {
        "🎨 Creative"       -> "CREATIVITY"
        "📚 Learning"       -> "LEARNING"
        "⚽ Sports"         -> "HEALTH"
        "🏃 Health"         -> "HEALTH"
        "🌙 Sleep"          -> "SLEEP"
        "👥 Social"         -> "SOCIAL"
        "🌿 Outdoor"        -> "OUTDOOR"
        "🏠 Chores"         -> "CHORES"
        "📱 Screen"         -> "SCREEN_TIME"
        "☀️ Morning"       -> "MORNING_ROUTINE"
        "👨‍👩‍👧 Family" -> "FAMILY"
        else                -> null
    }

    fun saveChallengeToFamily(
        challenge: GeneratedChallenge,
        familyId: String,
        childId: String
    ) {
        viewModelScope.launch {
            try {
                Log.d("GenerationVM", "Saving challenge: ${challenge.title}")
                _uiState.value = _uiState.value.copy(
                    successMessage = "✅ Challenge saved! Assign to your child to start.",
                    error          = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error          = "Error: ${e.message}",
                    successMessage = null
                )
            }
        }
    }
}