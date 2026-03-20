package com.kidsroutine.feature.family.data

import com.kidsroutine.core.model.FamilyModel
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    suspend fun createFamily(userId: String, familyName: String): FamilyModel
    suspend fun getFamily(familyId: String): FamilyModel?
    suspend fun updateFamily(family: FamilyModel)
    suspend fun addMemberToFamily(familyId: String, memberId: String)
    suspend fun getInviteCode(familyId: String): String
    fun observeFamily(familyId: String): Flow<FamilyModel?>
}