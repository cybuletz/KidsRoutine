// File: app/src/main/java/com/kidsroutine/navigation/Routes.kt
object Routes {
    // ═══════════════════════════════════════════════════════════════════════
    // CHILD ROUTES
    // ═══════════════════════════════════════════════════════════════════════

    const val DAILY               = "daily"
    const val EXECUTION           = "execution/{taskId}"
    const val CHALLENGES          = "challenges"
    const val CHALLENGE_DETAIL    = "challenge_detail/{challengeId}"
    const val LEADERBOARD         = "leaderboard"
    const val ACHIEVEMENTS        = "achievements"
    const val NOTIFICATIONS       = "notifications"
    const val STATS               = "stats"
    const val CHILD_PROFILE       = "child_profile"
    const val AVATAR_CUSTOMIZATION = "avatar_customization"
    const val WORLD               = "world"
    const val MOMENTS             = "moments"
    const val CHILD_TASK_PROPOSAL = "child_task_proposal"
    const val REWARDS             = "rewards"          // ← NEW
    const val LOOT_BOX            = "lootbox"          // ← NEW
    const val PET                 = "pet"
    const val BOSS_BATTLE         = "boss_battle"
    const val SPIN_WHEEL          = "spin_wheel"
    const val EVENTS              = "events"
    const val STORY_ARC           = "story_arc"
    const val WALLET              = "wallet"
    const val SKILL_TREE          = "skill_tree"
    const val RITUALS             = "rituals"

    // ═══════════════════════════════════════════════════════════════════════
    // PARENT ROUTES
    // ═══════════════════════════════════════════════════════════════════════

    const val PARENT_DASHBOARD        = "parent_dashboard"
    const val PARENT_PROFILE          = "parent_profile"
    const val INVITE_CHILDREN         = "invite_children"
    const val MANAGE_TASKS            = "manage_tasks"
    const val CREATE_TASK             = "create_task"
    const val TASK_LIST               = "task_list"
    const val PENDING_TASKS           = "pending_tasks"
    const val PARENT_CHALLENGES       = "parent_challenges"
    const val START_CHALLENGE         = "start_challenge"
    const val PARENT_CHALLENGE_DETAIL = "parent_challenge_detail/{challengeId}"
    const val PARENT_STATS            = "parent_stats"
    const val GENERATION              = "generation"
    const val DAILY_PLAN              = "daily_plan"
    const val WEEKLY_PLAN             = "weekly_plan"
    const val PRIVILEGE_APPROVALS     = "privilege_approvals"    // ← NEW

    // ═══════════════════════════════════════════════════════════════════════
    // COMMUNITY ROUTES
    // ═══════════════════════════════════════════════════════════════════════

    const val MARKETPLACE            = "marketplace"
    const val MARKETPLACE_TASKS      = "marketplace_tasks"
    const val MARKETPLACE_CHALLENGES = "marketplace_challenges"
    const val PUBLISH                = "publish"

    // ═══════════════════════════════════════════════════════════════════════
    // GENERIC ROUTES
    // ═══════════════════════════════════════════════════════════════════════

    const val MODERATION      = "moderation"
    const val FAMILY_MESSAGING = "family_messaging"
    const val AVATAR_SHOP     = "avatar_shop"
    const val CONTENT_PACKS   = "content_packs"
    const val SEASONAL_THEME  = "seasonal_theme"
    const val UPGRADE         = "upgrade"

    // ═══════════════════════════════════════════════════════════════════════
    // Helper functions
    // ═══════════════════════════════════════════════════════════════════════

    fun execution(taskId: String)                  = "execution/$taskId"
    fun challengeDetail(challengeId: String)       = "challenge_detail/$challengeId"
    fun parentChallengeDetail(challengeId: String) = "parent_challenge_detail/$challengeId"
    fun selectChildren(taskId: String)             = "select_children/$taskId"

    // ═══════════════════════════════════════════════════════════════════════
    // PARENT NAV BAR ROUTES (inner navigation)
    // ═══════════════════════════════════════════════════════════════════════

    const val PARENT_HOME         = "parent_home"
    const val PARENT_TASKS_TAB    = "parent_tasks_tab"
    const val PARENT_FAMILY_TAB   = "parent_family_tab"
    const val PARENT_DISCOVER_TAB = "parent_discover_tab"
    const val PARENT_SETTINGS_TAB = "parent_settings_tab"
}
