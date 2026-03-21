package com.kidsroutine.feature.notifications.data

import com.kidsroutine.core.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun sendNotification(notification: AppNotification)
    fun observeUserNotifications(userId: String): Flow<List<AppNotification>>
    suspend fun markAsRead(notificationId: String)
    suspend fun deleteNotification(notificationId: String)
}