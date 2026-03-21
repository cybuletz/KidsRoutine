package com.kidsroutine.navigation

object Routes {
    const val DAILY = "daily"
    const val EXECUTION = "execution/{taskId}"
    const val CHALLENGES = "challenges"
    const val CHALLENGE_DETAIL = "challenge_detail/{challengeId}"
    const val LEADERBOARD = "leaderboard"
    const val ACHIEVEMENTS = "achievements"
    const val NOTIFICATIONS = "notifications"
    const val STATS = "stats"
    const val PARENT_DASHBOARD = "parent_dashboard"
    const val INVITE_CHILDREN = "invite_children"
    const val TASK_LIST = "task_list"
    const val PENDING_TASKS = "pending_tasks"
    const val PARENT_CHALLENGES = "parent_challenges"
    const val PARENT_CHALLENGE_DETAIL = "parent_challenge_detail/{challengeId}"
    const val MARKETPLACE = "marketplace"
    const val PUBLISH = "publish"
    const val MODERATION = "moderation"

    fun challengeDetail(id: String) = "challenge_detail/$id"
    fun parentChallengeDetail(id: String) = "parent_challenge_detail/$id"
    fun execution(id: String) = "execution/$id"
}

    const val PUBLISH = "publish"
    const val MODERATION = "moderation"

    // ═══════════════════════════════════════════════════════════════════════
    // Helper functions for navigation with arguments
    // ═══════════════════════════════════════════════════════════════════════

    fun execution(taskId: String) = "execution/$taskId"
    fun challengeDetail(challengeId: String) = "challenge_detail/$challengeId"
    fun parentChallengeDetail(challengeId: String) = "parent_challenge_detail/$challengeId"
}