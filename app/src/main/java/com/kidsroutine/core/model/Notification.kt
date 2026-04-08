package com.kidsroutine.core.model

enum class NotificationType {
    // Existing
    TASK_REMINDER,
    ACHIEVEMENT_UNLOCKED,
    PARENT_APPROVAL_NEEDED,
    CHALLENGE_STARTED,
    LEADERBOARD_CHANGED,
    FAMILY_MESSAGE,

    // ── NEW: Roo-powered notification types ─────────────────────────
    STREAK_AT_RISK,          // Evening reminder that streak will break
    COMEBACK_NUDGE,          // 2+ day absence, gentle re-engagement
    PET_HUNGRY,              // Pet needs feeding (task completion)
    PET_EVOLVED,             // Pet reached next evolution stage
    LEAGUE_PROMOTION,        // Almost promoted in weekly league
    LEAGUE_DEMOTION,         // In danger zone, about to drop
    BOSS_APPEARED,           // New weekly boss battle started
    BOSS_DEFEATED,           // Family defeated the boss
    FRIEND_ACTIVITY,         // Friend completed a task / milestone
    EVENT_STARTED,           // Timed event begins
    EVENT_ENDING_SOON,       // Timed event about to end
    DAILY_SPIN_AVAILABLE,    // Daily spin wheel ready
    COMEBACK_CHALLENGE,      // Roo-covery challenge offered
    RANDOM_ENCOURAGEMENT,    // Witty Roo motivational nudge
    MILESTONE_REACHED,       // Streak/level/XP milestone
    BIRTHDAY_CELEBRATION,    // Child's birthday
    AGE_UP_CEREMONY          // Age group transition
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
    val createdAt: Long = 0L,
    // ── NEW: Rich notification metadata ─────────────────────────────
    val ageGroup: AgeGroup? = null,        // for age-adaptive copy
    val rooExpression: String? = null,      // Roo's expression emoji
    val priority: String = "MEDIUM"        // LOW, MEDIUM, HIGH
)