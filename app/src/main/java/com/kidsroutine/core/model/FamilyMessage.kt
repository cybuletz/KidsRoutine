package com.kidsroutine.core.model

data class FamilyMessage(
    val id: String = "msg_${System.currentTimeMillis()}_${kotlin.random.Random.nextInt(10000)}",
    val familyId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val relatedTaskId: String? = null,  // For task-related messages
    val relatedTaskTitle: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

enum class MessageType {
    TEXT,
    TASK_REMINDER,
    ACHIEVEMENT_CELEBRATION,
    COMPLETION_CONFIRMATION,
    ENCOURAGEMENT
}