package com.kidsroutine.feature.notifications.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kidsroutine.core.model.AppNotification
import com.kidsroutine.core.model.NotificationType
import com.kidsroutine.feature.tasks.ui.RefreshEventManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

const val NOTIFICATION_CHANNEL_ID = "kidsroutine_notifications"
const val NOTIFICATION_CHANNEL_NAME = "KidsRoutine Notifications"
private const val TAG = "FCM"

@AndroidEntryPoint
class PushNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepositoryImpl

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
        Log.d(TAG, "Subscribed to all_users topic")

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d(TAG, "FCM token saved to Firestore ✓")
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "Failed to save FCM token", error)
                }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")

            val title = it.title ?: "KidsRoutine"
            val body = it.body ?: ""
            val type = remoteMessage.data["type"] ?: "TASK_REMINDER"
            val userId = remoteMessage.data["userId"] ?: ""
            val icon = remoteMessage.data["icon"] ?: "🔔"
            val refreshTrigger = remoteMessage.data["refreshTrigger"] ?: "false"
            val taskId = remoteMessage.data["taskId"] ?: ""

            // ════════════════════════════════════════════════════════════════════════
            // ✨ NEW: Handle TASK_DELETED notification type
            // ════════════════════════════════════════════════════════════════════════
            if (type == "TASK_DELETED") {
                Log.d(TAG, "📋 Task deletion notification received for taskId: $taskId")

                // Remove task from local cache if using a local database
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Optional: Remove from local database if you have task caching
                        // taskProgressDao.deleteByTaskId(taskId)
                        Log.d(TAG, "Processed task deletion for $taskId")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing task deletion", e)
                    }
                }
            }

            val notification = AppNotification(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                type = try {
                    NotificationType.valueOf(type)
                } catch (e: Exception) {
                    if (type == "TASK_DELETED") NotificationType.TASK_REMINDER else NotificationType.TASK_REMINDER
                },
                title = title,
                body = body,
                icon = icon,
                actionUrl = remoteMessage.data["actionUrl"] ?: "",
                isRead = false,
                createdAt = System.currentTimeMillis()
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    notificationRepository.saveNotification(notification)
                    Log.d(TAG, "Notification saved to database ✓")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving notification", e)
                }
            }

            // ════════════════════════════════════════════════════════════════════════
            // ✨ Trigger refresh for TASK_DELETED, TASK_ASSIGNED, CHALLENGE_ASSIGNED
            // ════════════════════════════════════════════════════════════════════════
            if (refreshTrigger == "true" && (
                        type.contains("DELETED") ||
                                type.contains("ASSIGNED") ||
                                type.contains("TASK") ||
                                type.contains("CHALLENGE")
                        )) {
                Log.d(TAG, "🔄 Triggering UI refresh for notification type: $type")
                CoroutineScope(Dispatchers.Default).launch {
                    RefreshEventManager.triggerRefresh()
                }
            }

            showNotification(title, body, icon)
        }

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }
    }

    private fun showNotification(title: String, body: String, icon: String) {
        createNotificationChannel()

        val notificationId = (System.currentTimeMillis() / 1000).toInt()

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

        Log.d(TAG, "System notification shown: $title")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifications from KidsRoutine"

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}