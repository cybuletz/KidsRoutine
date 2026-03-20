package com.kidsroutine.feature.daily.data

import com.kidsroutine.core.model.UserModel
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun updateUserXp(userId: String, xpGained: Int)
    fun observeUser(userId: String): Flow<UserModel>
}