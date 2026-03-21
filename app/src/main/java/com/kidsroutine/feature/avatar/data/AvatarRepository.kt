package com.kidsroutine.feature.avatar.data

import com.kidsroutine.core.model.AvatarCustomization
import com.kidsroutine.core.model.AvatarItem
import kotlinx.coroutines.flow.Flow

interface AvatarRepository {
    suspend fun getAllAvatarItems(): List<AvatarItem>
    suspend fun getAvatarItemsByCategory(category: String): List<AvatarItem>
    suspend fun getUserAvatarCustomization(userId: String): AvatarCustomization
    suspend fun updateAvatarCustomization(userId: String, customization: AvatarCustomization)
    suspend fun unlockAvatarItem(userId: String, itemId: String)
    suspend fun saveAvatarPreset(userId: String, presetName: String, customization: AvatarCustomization)
    fun observeAvatarCustomization(userId: String): Flow<AvatarCustomization>
}