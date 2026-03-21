package com.kidsroutine.feature.notifications.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.AppNotification
import com.kidsroutine.feature.notifications.data.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun loadNotifications(userId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                Log.d("NotificationVM", "Loading notifications for user: $userId")
                notificationRepository.observeUserNotifications(userId)
                    .collect { notifications ->
                        val unreadCount = notifications.count { !it.isRead }
                        Log.d("NotificationVM", "Loaded ${notifications.size} notifications, ${unreadCount} unread")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            notifications = notifications,
                            unreadCount = unreadCount
                        )
                    }
            } catch (e: Exception) {
                Log.e("NotificationVM", "Error loading notifications", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load notifications"
                )
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                Log.d("NotificationVM", "Marking notification as read: $notificationId")
                notificationRepository.markAsRead(notificationId)
            } catch (e: Exception) {
                Log.e("NotificationVM", "Error marking as read", e)
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                Log.d("NotificationVM", "Deleting notification: $notificationId")
                notificationRepository.deleteNotification(notificationId)
            } catch (e: Exception) {
                Log.e("NotificationVM", "Error deleting notification", e)
            }
        }
    }
}