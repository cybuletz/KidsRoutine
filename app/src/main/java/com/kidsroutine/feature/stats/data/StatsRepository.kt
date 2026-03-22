package com.kidsroutine.feature.stats.data

import com.kidsroutine.core.model.UserModel
import kotlinx.coroutines.flow.Flow

data class UserStatsModel(
    val userId: String = "",
    val displayName: String = "",
    val totalXp: Int = 0,
    val level: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val tasksCompleted: Int = 0,
    val badgesUnlocked: Int = 0,
    val thisWeekXp: Int = 0,
    val thisMonthXp: Int = 0
)

data class FamilyStatsModel(
    val familyId: String = "",
    val familyName: String = "",
    val memberCount: Int = 0,
    val familyXp: Int = 0,
    val familyStreak: Int = 0,
    val totalTasksCompleted: Int = 0,
    val avgXpPerMember: Int = 0
)

interface StatsRepository {
    suspend fun getUserStats(userId: String): UserStatsModel?
    suspend fun getFamilyStats(familyId: String): FamilyStatsModel?
    fun observeUserStats(userId: String): Flow<UserStatsModel?>
    suspend fun getWeeklyProgress(userId: String): List<Int>  // Daily XP for last 7 days
    suspend fun getMonthlyProgress(userId: String): List<Int> // Weekly XP for last 4 weeks
}