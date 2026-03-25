package com.kidsroutine.core.model

enum class PrivilegeRequestStatus { PENDING, APPROVED, REJECTED }

data class PrivilegeRequest(
    val requestId: String = java.util.UUID.randomUUID().toString(),
    val familyId: String = "",
    val childUserId: String = "",
    val childName: String = "",
    val privilegeId: String = "",
    val privilegeTitle: String = "",
    val privilegeEmoji: String = "🎁",
    val xpCost: Int = 0,
    val status: PrivilegeRequestStatus = PrivilegeRequestStatus.PENDING,
    val parentNote: String = "",          // rejection reason or approval note
    val requestedAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long = 0L
)
