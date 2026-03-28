package com.kidsroutine.feature.avatar.data

import com.kidsroutine.core.model.AvatarState
import kotlinx.coroutines.flow.Flow

interface AvatarRepository {
    // Existing methods
    suspend fun getAvatar(userId: String): AvatarState?
    suspend fun saveAvatar(avatar: AvatarState)
    suspend fun getCoins(userId: String): Int
    suspend fun getPlayerName(userId: String): String
    suspend fun getUnlockedItemIds(userId: String): Set<String>

    // NEW XP methods (replace coin methods)
    suspend fun getUserXp(userId: String): Int
    suspend fun deductUserXp(userId: String, amount: Int)
    suspend fun addUserXp(userId: String, amount: Int)

    // Pack ownership
    suspend fun getOwnedAvatarPacks(userId: String): Set<String>
    suspend fun addOwnedAvatarPack(userId: String, packId: String)
}