package com.kidsroutine.core.model

data class FamilyModel(
    val familyId: String = "",
    val familyName: String = "",
    val memberIds: List<String> = emptyList(),
    val familyXp: Int = 0,
    val familyStreak: Int = 0,
    val sharedChallengeIds: List<String> = emptyList(),
    val inviteCode: String = ""
)
