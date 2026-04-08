package com.kidsroutine.core.model

/**
 * Skill Tree System — Visual progression that feels earned.
 * Replaces simple level number with branching skill trees
 * across 5 core branches tied to TaskCategory.
 */

enum class SkillBranch(
    val displayName: String,
    val emoji: String,
    val color: Long,
    val relatedCategories: List<TaskCategory>
) {
    RESPONSIBILITY(
        displayName = "Responsibility",
        emoji = "⚡",
        color = 0xFFFF6B35,
        relatedCategories = listOf(TaskCategory.CHORES, TaskCategory.MORNING_ROUTINE)
    ),
    CREATIVITY(
        displayName = "Creativity",
        emoji = "🎨",
        color = 0xFF9B59B6,
        relatedCategories = listOf(TaskCategory.CREATIVITY)
    ),
    HEALTH(
        displayName = "Health",
        emoji = "💪",
        color = 0xFF2ECC71,
        relatedCategories = listOf(TaskCategory.HEALTH, TaskCategory.OUTDOOR, TaskCategory.SLEEP)
    ),
    SOCIAL(
        displayName = "Social",
        emoji = "🤝",
        color = 0xFF3498DB,
        relatedCategories = listOf(TaskCategory.SOCIAL, TaskCategory.FAMILY)
    ),
    LEARNING(
        displayName = "Learning",
        emoji = "📚",
        color = 0xFFE67E22,
        relatedCategories = listOf(TaskCategory.LEARNING, TaskCategory.SCREEN_TIME)
    )
}

data class SkillNode(
    val nodeId: String = "",
    val branch: SkillBranch = SkillBranch.RESPONSIBILITY,
    val title: String = "",
    val description: String = "",
    val emoji: String = "",

    // Unlock requirements
    val requiredTaskCount: Int = 10,   // tasks in related categories
    val requiredLevel: Int = 0,        // optional level gate
    val prerequisiteNodeId: String? = null,

    // Rewards
    val xpBonusPercent: Int = 0,       // e.g., +5% XP on related tasks
    val avatarItemId: String? = null,  // unlocks avatar item
    val petTrickId: String? = null,    // Roo learns a new trick
    val badgeId: String? = null,       // unlocks badge

    // State
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L,
    val currentProgress: Int = 0       // tasks completed toward this node
) {
    val progressPercent: Float
        get() = if (requiredTaskCount > 0) (currentProgress.toFloat() / requiredTaskCount).coerceIn(0f, 1f) else 0f
}

data class SkillTree(
    val userId: String = "",
    val branches: Map<SkillBranch, List<SkillNode>> = emptyMap(),
    val totalNodesUnlocked: Int = 0,
    val totalXpBonusPercent: Int = 0   // cumulative bonus from all unlocked nodes
)

/** Default skill tree template with starter nodes */
object SkillTreeDefaults {
    fun createDefaultNodes(): Map<SkillBranch, List<SkillNode>> = mapOf(
        SkillBranch.RESPONSIBILITY to listOf(
            SkillNode(nodeId = "resp_1", branch = SkillBranch.RESPONSIBILITY, title = "Tidy Starter", emoji = "🧹", requiredTaskCount = 5, xpBonusPercent = 2),
            SkillNode(nodeId = "resp_2", branch = SkillBranch.RESPONSIBILITY, title = "Chore Champion", emoji = "🏅", requiredTaskCount = 20, xpBonusPercent = 5, prerequisiteNodeId = "resp_1", avatarItemId = "badge_chore_champ"),
            SkillNode(nodeId = "resp_3", branch = SkillBranch.RESPONSIBILITY, title = "Home Hero", emoji = "🦸", requiredTaskCount = 50, xpBonusPercent = 10, prerequisiteNodeId = "resp_2")
        ),
        SkillBranch.CREATIVITY to listOf(
            SkillNode(nodeId = "crea_1", branch = SkillBranch.CREATIVITY, title = "Spark", emoji = "💡", requiredTaskCount = 5, xpBonusPercent = 2),
            SkillNode(nodeId = "crea_2", branch = SkillBranch.CREATIVITY, title = "Artisan", emoji = "🎭", requiredTaskCount = 20, xpBonusPercent = 5, prerequisiteNodeId = "crea_1"),
            SkillNode(nodeId = "crea_3", branch = SkillBranch.CREATIVITY, title = "Visionary", emoji = "🌈", requiredTaskCount = 50, xpBonusPercent = 10, prerequisiteNodeId = "crea_2")
        ),
        SkillBranch.HEALTH to listOf(
            SkillNode(nodeId = "heal_1", branch = SkillBranch.HEALTH, title = "Active Start", emoji = "🏃", requiredTaskCount = 5, xpBonusPercent = 2),
            SkillNode(nodeId = "heal_2", branch = SkillBranch.HEALTH, title = "Gym Warrior", emoji = "💪", requiredTaskCount = 20, xpBonusPercent = 5, prerequisiteNodeId = "heal_1", avatarItemId = "pose_gym_warrior"),
            SkillNode(nodeId = "heal_3", branch = SkillBranch.HEALTH, title = "Wellness Master", emoji = "🧘", requiredTaskCount = 50, xpBonusPercent = 10, prerequisiteNodeId = "heal_2")
        ),
        SkillBranch.SOCIAL to listOf(
            SkillNode(nodeId = "soc_1", branch = SkillBranch.SOCIAL, title = "Friendly", emoji = "👋", requiredTaskCount = 5, xpBonusPercent = 2),
            SkillNode(nodeId = "soc_2", branch = SkillBranch.SOCIAL, title = "Team Player", emoji = "🤝", requiredTaskCount = 20, xpBonusPercent = 5, prerequisiteNodeId = "soc_1"),
            SkillNode(nodeId = "soc_3", branch = SkillBranch.SOCIAL, title = "Community Star", emoji = "🌟", requiredTaskCount = 50, xpBonusPercent = 10, prerequisiteNodeId = "soc_2")
        ),
        SkillBranch.LEARNING to listOf(
            SkillNode(nodeId = "learn_1", branch = SkillBranch.LEARNING, title = "Curious Mind", emoji = "🔍", requiredTaskCount = 5, xpBonusPercent = 2),
            SkillNode(nodeId = "learn_2", branch = SkillBranch.LEARNING, title = "Knowledge Seeker", emoji = "📖", requiredTaskCount = 20, xpBonusPercent = 5, prerequisiteNodeId = "learn_1"),
            SkillNode(nodeId = "learn_3", branch = SkillBranch.LEARNING, title = "Scholar", emoji = "🎓", requiredTaskCount = 50, xpBonusPercent = 10, prerequisiteNodeId = "learn_2")
        )
    )
}
