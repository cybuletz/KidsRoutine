package com.kidsroutine.feature.achievements.data

import com.kidsroutine.core.model.Badge
import com.kidsroutine.core.model.UserAchievements
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    suspend fun checkAndUnlockAchievements(userId: String): List<Badge>
    suspend fun getUserAchievements(userId: String): UserAchievements
    fun observeUserAchievements(userId: String): Flow<UserAchievements>
    suspend fun updateBadges(userId: String, badges: List<Badge>)
}