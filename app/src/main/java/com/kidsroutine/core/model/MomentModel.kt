package com.kidsroutine.core.model

data class MomentModel(
    val momentId: String = "",
    val userId: String = "",
    val familyId: String = "",
    val title: String = "",
    val description: String = "",
    val emoji: String = "📸",
    val photoUrl: String = "",          // optional real photo
    val xpAtMoment: Int = 0,
    val taskTitle: String = "",         // which task created this moment
    val createdAt: Long = System.currentTimeMillis(),
    val reactions: Map<String, String> = emptyMap()  // userId → emoji reaction
)