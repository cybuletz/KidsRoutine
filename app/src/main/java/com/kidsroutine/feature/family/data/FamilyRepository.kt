package com.kidsroutine.feature.family.data

import com.kidsroutine.core.model.FamilyModel
import com.kidsroutine.core.model.TaskModel
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    suspend fun createFamily(userId: String, familyName: String): FamilyModel
    suspend fun getFamily(familyId: String): FamilyModel?
    suspend fun updateFamily(family: FamilyModel)
    suspend fun addMemberToFamily(familyId: String, memberId: String)
    suspend fun getInviteCode(familyId: String): String
    fun observeFamily(familyId: String): Flow<FamilyModel?>

    // Child → Parent task methods
    suspend fun proposeChildTask(familyId: String, childId: String, task: TaskModel)
    suspend fun getPendingChildTasks(familyId: String): List<TaskModel>
    suspend fun approveChildTask(familyId: String, taskId: String)
    suspend fun rejectChildTask(familyId: String, taskId: String, reason: String)}