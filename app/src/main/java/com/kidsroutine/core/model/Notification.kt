package com.kidsroutine.core.model

enum class NotificationType {
    TASK_REMINDER,
    ACHIEVEMENT_UNLOCKED,
    PARENT_APPROVAL_NEEDED,
    CHALLENGE_STARTED,
    LEADERBOARD_CHANGED,
    FAMILY_MESSAGE
}

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val type: NotificationType = NotificationType.TASK_REMINDER,
    val title: String = "",
    val body: String = "",
    val icon: String = "",
    val actionUrl: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)