package com.kidsroutine.feature.rewards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.PrivilegeRequest
import com.kidsroutine.core.model.PrivilegeRequestStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class RewardsUiState(
    val isLoading: Boolean = false,
    val myRequests: List<PrivilegeRequest> = emptyList(),
    val pendingCount: Int = 0,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    fun loadMyRequests(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val snapshot = firestore.collection("privilege_requests")
                    .whereEqualTo("childUserId", userId)
                    .orderBy("requestedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(20)
                    .get()
                    .await()
                val requests = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PrivilegeRequest::class.java)?.copy(requestId = doc.id)
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    myRequests = requests,
                    pendingCount = requests.count { it.status == PrivilegeRequestStatus.PENDING }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun requestPrivilege(
        familyId: String,
        userId: String,
        childName: String,
        privilege: Privilege
    ) {
        viewModelScope.launch {
            try {
                val request = PrivilegeRequest(
                    familyId       = familyId,
                    childUserId    = userId,
                    childName      = childName,
                    privilegeId    = privilege.id,
                    privilegeTitle = privilege.title,
                    privilegeEmoji = privilege.emoji,
                    xpCost         = privilege.xpCost,
                    status         = PrivilegeRequestStatus.PENDING
                )
                firestore.collection("privilege_requests")
                    .document(request.requestId)
                    .set(request)
                    .await()

                _uiState.value = _uiState.value.copy(
                    successMessage = "✅ Request sent! Waiting for parent approval.",
                    myRequests = listOf(request) + _uiState.value.myRequests,
                    pendingCount = _uiState.value.pendingCount + 1
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to send request: ${e.message}"
                )
            }
        }
    }

    fun cancelRequest(requestId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("privilege_requests")
                    .document(requestId)
                    .delete()
                    .await()
                // Remove from local state immediately
                _uiState.value = _uiState.value.copy(
                    myRequests = _uiState.value.myRequests.filter { it.requestId != requestId }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }


    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
