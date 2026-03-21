package com.kidsroutine.core.model

data class LeaderboardEntry(
    val rank: Int = 0,
    val userId: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val xp: Int = 0,
    val level: Int = 1,
    val weeklyXp: Int = 0,
    val badges: Int = 0
)

data class FamilyLeaderboard(
    val familyId: String = "",
    val week: String = "",  // "2025-W12" format
    val entries: List<LeaderboardEntry> = emptyList()
)