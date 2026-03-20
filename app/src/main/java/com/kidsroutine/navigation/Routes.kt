package com.kidsroutine.navigation

object Routes {
    // ═══════════════════════════════════════════════════════════════════════
    // CHILD ROUTES
    // ═══════════════════════════════════════════════════════════════════════

    const val DAILY = "daily"
    const val EXECUTION = "execution/{taskId}"

    const val CHALLENGES = "challenges"
    const val CHALLENGE_DETAIL = "challenge_detail/{challengeId}"
    const val STATS = "stats"

    // ═══════════════════════════════════════════════════════════════════════
    // PARENT ROUTES
    // ═══════════════════════════════════════════════════════════════════════

    const val PARENT_DASHBOARD = "parent_dashboard"
    const val INVITE_CHILDREN = "invite_children"
    const val MANAGE_TASKS = "manage_tasks"
    const val CREATE_TASK = "create_task"
    const val TASK_LIST = "task_list"
    const val PENDING_TASKS = "pending_tasks"
    const val PARENT_CHALLENGES = "parent_challenges"
    const val START_CHALLENGE = "start_challenge"
    const val PARENT_CHALLENGE_DETAIL = "parent_challenge_detail/{challengeId}"

    // ═══════════════════════════════════════════════════════════════════════
    // Helper functions for navigation with arguments
    // ═══════════════════════════════════════════════════════════════════════

    fun execution(taskId: String) = "execution/$taskId"
    fun challengeDetail(challengeId: String) = "challenge_detail/$challengeId"
    fun parentChallengeDetail(challengeId: String) = "parent_challenge_detail/$challengeId"
}