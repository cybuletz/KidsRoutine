// File: app/src/main/java/com/kidsroutine/navigation/Routes.kt
object Routes {
    // ═══════════════════════════════════════════════════════════════════════
    // CHILD ROUTES
    // ═══════════════════════════════════════════════════════════════════════

    const val DAILY = "daily"
    const val EXECUTION = "execution/{taskId}"
    const val CHALLENGES = "challenges"
    const val CHALLENGE_DETAIL = "challenge_detail/{challengeId}"
    const val LEADERBOARD = "leaderboard"
    const val ACHIEVEMENTS = "achievements"
    const val NOTIFICATIONS = "notifications"
    const val STATS = "stats"
    const val CHILD_PROFILE = "child_profile"
    const val AVATAR_CUSTOMIZATION = "avatar_customization"

    // ═══════════════════════════════════════════════════════════════════════
    // PARENT ROUTES
    // ═══════════════════════════════════════════════════════════════════════

    const val PARENT_DASHBOARD = "parent_dashboard"
    const val PARENT_PROFILE = "parent_profile"
    const val INVITE_CHILDREN = "invite_children"
    const val MANAGE_TASKS = "manage_tasks"
    const val CREATE_TASK = "create_task"
    const val TASK_LIST = "task_list"
    const val PENDING_TASKS = "pending_tasks"
    const val PARENT_CHALLENGES = "parent_challenges"
    const val START_CHALLENGE = "start_challenge"
    const val PARENT_CHALLENGE_DETAIL = "parent_challenge_detail/{challengeId}"

    const val PARENT_STATS = "parent_stats"

    const val GENERATION = "generation"


    // ═══════════════════════════════════════════════════════════════════════
    // COMMUNITY ROUTES
    // ═══════════════════════════════════════════════════════════════════════

    const val MARKETPLACE = "marketplace"
    const val MARKETPLACE_TASKS = "marketplace_tasks"
    const val MARKETPLACE_CHALLENGES = "marketplace_challenges"
    const val PUBLISH = "publish"
    const val MODERATION = "moderation"
    const val FAMILY_MESSAGING = "family_messaging"

    // ═══════════════════════════════════════════════════════════════════════
    // Helper functions for navigation with arguments
    // ═══════════════════════════════════════════════════════════════════════

    fun execution(taskId: String) = "execution/$taskId"
    fun challengeDetail(challengeId: String) = "challenge_detail/$challengeId"
    fun parentChallengeDetail(challengeId: String) = "parent_challenge_detail/$challengeId"

    fun selectChildren(taskId: String) = "select_children/$taskId"
}