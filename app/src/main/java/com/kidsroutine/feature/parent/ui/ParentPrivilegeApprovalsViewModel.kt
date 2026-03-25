package com.kidsroutine.feature.parent.ui

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

data class PrivilegeApprovalsUiState(
    val isLoading: Boolean = false,
    val pendingRequests: List<PrivilegeRequest> = emptyList(),
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class ParentPrivilegeApprovalsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivilegeApprovalsUiState())
    val uiState: StateFlow<PrivilegeApprovalsUiState> = _uiState.asStateFlow()

    fun loadPendingRequests(familyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val snapshot = firestore.collection("privilege_requests")
                    .whereEqualTo("familyId", familyId)
                    .whereEqualTo("status", PrivilegeRequestStatus.PENDING.name)
                    .orderBy("requestedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                val requests = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PrivilegeRequest::class.java)?.copy(requestId = doc.id)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, pendingRequests = requests)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun approveRequest(requestId: String, note: String = "") {
        viewModelScope.launch {
            try {
                firestore.collection("privilege_requests")
                    .document(requestId)
                    .update(
                        mapOf(
                            "status"     to PrivilegeRequestStatus.APPROVED.name,
                            "parentNote" to note,
                            "resolvedAt" to System.currentTimeMillis()
                        )
                    ).await()
                _uiState.value = _uiState.value.copy(
                    pendingRequests = _uiState.value.pendingRequests.filter { it.requestId != requestId },
                    successMessage  = "✅ Approved!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun rejectRequest(requestId: String, note: String) {
        viewModelScope.launch {
            try {
                firestore.collection("privilege_requests")
                    .document(requestId)
                    .update(
                        mapOf(
                            "status"     to PrivilegeRequestStatus.REJECTED.name,
                            "parentNote" to note,
                            "resolvedAt" to System.currentTimeMillis()
                        )
                    ).await()
                _uiState.value = _uiState.value.copy(
                    pendingRequests = _uiState.value.pendingRequests.filter { it.requestId != requestId },
                    successMessage  = "Request declined."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, error = null)
    }
}
