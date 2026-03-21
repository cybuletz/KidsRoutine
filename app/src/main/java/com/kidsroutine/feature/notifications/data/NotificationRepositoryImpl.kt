package com.kidsroutine.feature.notifications.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.AppNotification
import com.kidsroutine.core.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override suspend fun sendNotification(notification: AppNotification) {
        try {
            Log.d("NotificationRepository", "Sending notification to user: ${notification.userId}")

            val notifData = mapOf(
                "id" to notification.id,
                "userId" to notification.userId,
                "type" to notification.type.name,
                "title" to notification.title,
                "body" to notification.body,
                "icon" to notification.icon,
                "actionUrl" to notification.actionUrl,
                "isRead" to notification.isRead,
                "createdAt" to notification.createdAt
            )

            firestore.collection("notifications")
                .document(notification.id)
                .set(notifData)
                .await()

            Log.d("NotificationRepository", "Notification sent successfully")
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error sending notification", e)
        }
    }

    override fun observeUserNotifications(userId: String): Flow<List<AppNotification>> = flow {
        try {
            Log.d("NotificationRepository", "Observing notifications for user: $userId")

            firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("NotificationRepository", "Error observing notifications", error)
                        return@addSnapshotListener
                    }

                    val notifications = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            AppNotification(
                                id = data["id"] as? String ?: "",
                                userId = data["userId"] as? String ?: "",
                                type = NotificationType.valueOf(data["type"] as? String ?: "TASK_REMINDER"),
                                title = data["title"] as? String ?: "",
                                body = data["body"] as? String ?: "",
                                icon = data["icon"] as? String ?: "",
                                actionUrl = data["actionUrl"] as? String ?: "",
                                isRead = data["isRead"] as? Boolean ?: false,
                                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
                            )
                        } catch (e: Exception) {
                            Log.e("NotificationRepository", "Error parsing notification", e)
                            null
                        }
                    } ?: emptyList()

                    Log.d("NotificationRepository", "Loaded ${notifications.size} notifications")
                }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error observing notifications", e)
        }
    }

    override suspend fun markAsRead(notificationId: String) {
        try {
            Log.d("NotificationRepository", "Marking notification as read: $notificationId")
            firestore.collection("notifications").document(notificationId)
                .update("isRead", true)
                .await()
            Log.d("NotificationRepository", "Notification marked as read")
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error marking as read", e)
        }
    }

    override suspend fun deleteNotification(notificationId: String) {
        try {
            Log.d("NotificationRepository", "Deleting notification: $notificationId")
            firestore.collection("notifications").document(notificationId)
                .delete()
                .await()
            Log.d("NotificationRepository", "Notification deleted")
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error deleting notification", e)
        }
    }
}