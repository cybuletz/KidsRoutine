package com.kidsroutine.core.model

enum class AchievementType {
    TASKS_COMPLETED_10,    // Complete 10 tasks
    TASKS_COMPLETED_50,    // Complete 50 tasks
    XP_EARNED_100,         // Earn 100 XP
    XP_EARNED_500,         // Earn 500 XP
    STREAK_7_DAYS,         // 7-day streak
    STREAK_30_DAYS,        // 30-day streak
    FIRST_CHALLENGE,       // Complete first challenge
    CHALLENGE_MASTER,      // Complete 5 challenges
    COMMUNITY_CONTRIBUTOR, // Share 3 tasks/challenges
    FAMILY_HERO            // Be #1 on leaderboard
}

data class Badge(
    val id: String = "",
    val type: AchievementType = AchievementType.TASKS_COMPLETED_10,
    val title: String = "",
    val description: String = "",
    val icon: String = "",
    val unlockedAt: Long = 0L,
    val isUnlocked: Boolean = false
)

data class UserAchievements(
    val userId: String = "",
    val badges: List<Badge> = emptyList(),
    val totalBadgesUnlocked: Int = 0,
    val lastUnlockedAt: Long = 0L
)