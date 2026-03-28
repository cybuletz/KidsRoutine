package com.kidsroutine.feature.avatar.data

import com.kidsroutine.core.model.AvatarState
import kotlinx.coroutines.flow.Flow

interface AvatarRepository {

    suspend fun getAvatar(userId: String): AvatarState?

    fun observeAvatar(userId: String): Flow<AvatarState?>

    suspend fun saveAvatar(state: AvatarState)

    suspend fun deleteAvatar(userId: String)

    suspend fun getUnlockedItemIds(userId: String): Set<String>

    suspend fun getCoins(userId: String): Int

    suspend fun getPlayerName(userId: String): String
}