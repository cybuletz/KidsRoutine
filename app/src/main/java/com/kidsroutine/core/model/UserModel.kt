package com.kidsroutine.core.model

data class UserPreferences(
    val allowedTaskTypes: List<TaskType> = TaskType.entries,
    val maxDifficulty: DifficultyLevel = DifficultyLevel.HARD,
    val screenTimeLimitMin: Int = 60
)

data class UserModel(
    val userId: String = "",
    val role: Role = Role.CHILD,
    val familyId: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val isAdmin: Boolean = false,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val activeChallengeIds: List<String> = emptyList(),
    val preferences: UserPreferences = UserPreferences(),
    val badges: List<Badge> = emptyList(),
    val avatarCustomization: AvatarCustomization = AvatarCustomization(),
    val createdAt: Long = 0L,
    val lastActiveAt: Long = 0L
)