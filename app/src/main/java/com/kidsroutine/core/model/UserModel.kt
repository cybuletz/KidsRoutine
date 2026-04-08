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
    val isOnline: Boolean = false,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val streakShieldActive: Boolean = false,
    val activeChallengeIds: List<String> = emptyList(),
    val preferences: UserPreferences = UserPreferences(),
    val badges: List<Badge> = emptyList(),
    val avatarId: String = "",  // avatar looked up separately via AvatarRepository
    val createdAt: Long = 0L,
    val lastActiveAt: Long = 0L,
    val lastActiveDate: String = "",   // ← ADD: "yyyy-MM-dd" string for streak logic
    val age: Int = 8,                  // ← ADD: child age, default 8

    // ── NEW: Competitive feature fields ─────────────────────────────
    val ageGroup: AgeGroup = AgeGroup.fromAge(8),   // computed from age
    val league: League = League.BRONZE,               // current league tier
    val weeklyXp: Int = 0,                            // XP earned this week (for leagues)
    val petId: String = "",                            // companion pet ID
    val darkModeEnabled: Boolean = false,              // user preference for dark mode
    val comebackStreakSaved: Int = 0                   // streak value saved by comeback system
)