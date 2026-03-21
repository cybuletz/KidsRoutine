package com.kidsroutine.feature.community.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.*
import com.kidsroutine.feature.community.data.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModerationUiState(
    val isLoading: Boolean = false,
    val activeTab: ModerationTab = ModerationTab.PENDING_TASKS,
    val pendingTasks: List<SharedTask> = emptyList(),
    val pendingChallenges: List<SharedChallenge> = emptyList(),
    val reports: List<ContentReport> = emptyList(),
    val selectedTask: SharedTask? = null,
    val selectedChallenge: SharedChallenge? = null,
    val selectedReport: ContentReport? = null,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

enum class ModerationTab {
    PENDING_TASKS,
    PENDING_CHALLENGES,
    REPORTS
}

@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModerationUiState())
    val uiState: StateFlow<ModerationUiState> = _uiState.asStateFlow()

    fun loadModeration() {
        Log.d("ModerationVM", "Loading moderation queue")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // Get PENDING tasks (not approved!)
                val pendingTasks = communityRepository.getPendingTasks(limit = 100)

                // Get PENDING challenges (not approved!)
                val pendingChallenges = communityRepository.getPendingChallenges(limit = 100)

                // Get reports
                val reports = communityRepository.getPendingReports(limit = 100)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pendingTasks = pendingTasks,
                    pendingChallenges = pendingChallenges,
                    reports = reports,
                    error = null
                )

                Log.d("ModerationVM", "Loaded ${pendingTasks.size} tasks, ${pendingChallenges.size} challenges, ${reports.size} reports")
            } catch (e: Exception) {
                Log.e("ModerationVM", "Error loading moderation", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load moderation queue"
                )
            }
        }
    }

    fun selectTab(tab: ModerationTab) {
        Log.d("ModerationVM", "Selecting tab: $tab")
        _uiState.value = _uiState.value.copy(
            activeTab = tab,
            selectedTask = null,
            selectedChallenge = null,
            selectedReport = null
        )
    }

    fun selectTask(task: SharedTask) {
        _uiState.value = _uiState.value.copy(selectedTask = task)
    }

    fun selectChallenge(challenge: SharedChallenge) {
        _uiState.value = _uiState.value.copy(selectedChallenge = challenge)
    }

    fun selectReport(report: ContentReport) {
        _uiState.value = _uiState.value.copy(selectedReport = report)
    }

    fun approveTask(taskId: String) {
        Log.d("ModerationVM", "Approving task: $taskId")
        _uiState.value = _uiState.value.copy(isProcessing = true, error = null)

        viewModelScope.launch {
            try {
                communityRepository.approveTask(taskId)

                val updatedTasks = _uiState.value.pendingTasks.filter { it.taskId != taskId }
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    pendingTasks = updatedTasks,
                    selectedTask = null,
                    successMessage = "✅ Task approved and published!"
                )

                Log.d("ModerationVM", "Task approved")
            } catch (e: Exception) {
                Log.e("ModerationVM", "Error approving task", e)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Failed to approve task"
                )
            }
        }
    }

    fun rejectTask(taskId: String, reason: String) {
        Log.d("ModerationVM", "Rejecting task: $taskId")
        _uiState.value = _uiState.value.copy(isProcessing = true, error = null)

        viewModelScope.launch {
            try {
                communityRepository.rejectTask(taskId)

                val updatedTasks = _uiState.value.pendingTasks.filter { it.taskId != taskId }
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    pendingTasks = updatedTasks,
                    selectedTask = null,
                    successMessage = "❌ Task rejected. Creator will be notified."
                )

                Log.d("ModerationVM", "Task rejected: $reason")
            } catch (e: Exception) {
                Log.e("ModerationVM", "Error rejecting task", e)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Failed to reject task"
                )
            }
        }
    }

    fun approveChallenge(challengeId: String) {
        Log.d("ModerationVM", "Approving challenge: $challengeId")
        _uiState.value = _uiState.value.copy(isProcessing = true, error = null)

        viewModelScope.launch {
            try {
                communityRepository.approveChallenge(challengeId)

                val updatedChallenges = _uiState.value.pendingChallenges.filter { it.challengeId != challengeId }
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    pendingChallenges = updatedChallenges,
                    selectedChallenge = null,
                    successMessage = "✅ Challenge approved and published!"
                )

                Log.d("ModerationVM", "Challenge approved")
            } catch (e: Exception) {
                Log.e("ModerationVM", "Error approving challenge", e)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Failed to approve challenge"
                )
            }
        }
    }

    fun rejectChallenge(challengeId: String, reason: String) {
        Log.d("ModerationVM", "Rejecting challenge: $challengeId")
        _uiState.value = _uiState.value.copy(isProcessing = true, error = null)

        viewModelScope.launch {
            try {
                communityRepository.rejectChallenge(challengeId)

                val updatedChallenges = _uiState.value.pendingChallenges.filter { it.challengeId != challengeId }
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    pendingChallenges = updatedChallenges,
                    selectedChallenge = null,
                    successMessage = "❌ Challenge rejected. Creator will be notified."
                )

                Log.d("ModerationVM", "Challenge rejected: $reason")
            } catch (e: Exception) {
                Log.e("ModerationVM", "Error rejecting challenge", e)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Failed to reject challenge"
                )
            }
        }
    }

    fun resolveReport(reportId: String, action: String) {
        Log.d("ModerationVM", "Resolving report: $reportId with action: $action")
        _uiState.value = _uiState.value.copy(isProcessing = true, error = null)

        viewModelScope.launch {
            try {
                // Get the report to find the content
                val report = _uiState.value.reports.find { it.reportId == reportId }
                    ?: throw Exception("Report not found")

                Log.d("ModerationVM", "Found report: contentId=${report.contentId}, contentType=${report.contentType}")

                // Resolve the report
                communityRepository.resolveReport(reportId, action)

                // If APPROVED means the report is valid (violation confirmed), delete the content
                if (action == "APPROVED") {
                    Log.d("ModerationVM", "Report approved - deleting reported content: ${report.contentId}")

                    when (report.contentType) {
                        "task" -> communityRepository.rejectTask(report.contentId)
                        "challenge" -> communityRepository.rejectChallenge(report.contentId)
                        else -> Log.w("ModerationVM", "Unknown content type: ${report.contentType}")
                    }

                    Log.d("ModerationVM", "Content rejected/deleted")
                }

                // Remove report from UI
                val updatedReports = _uiState.value.reports.filter { it.reportId != reportId }
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    reports = updatedReports,
                    selectedReport = null,
                    successMessage = "✅ Report resolved. Action: $action"
                )

                Log.d("ModerationVM", "UI updated. Reports remaining: ${updatedReports.size}")

                // Reload to sync
                kotlinx.coroutines.delay(1000)
                loadModeration()

            } catch (e: Exception) {
                Log.e("ModerationVM", "Error resolving report: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Failed to resolve report: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            error = null
        )
    }
}