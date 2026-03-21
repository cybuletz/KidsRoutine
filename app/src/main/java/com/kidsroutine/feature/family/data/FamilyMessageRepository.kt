package com.kidsroutine.feature.family.data

import com.kidsroutine.core.model.FamilyMessage
import kotlinx.coroutines.flow.Flow

interface FamilyMessageRepository {
    suspend fun sendMessage(message: FamilyMessage)
    fun observeFamilyMessages(familyId: String): Flow<List<FamilyMessage>>
    suspend fun markAsRead(messageId: String)
    suspend fun deleteMessage(messageId: String)
}