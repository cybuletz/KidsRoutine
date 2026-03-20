package com.kidsroutine.feature.family.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.FamilyModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FamilyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FamilyRepository {

    override suspend fun createFamily(userId: String, familyName: String): FamilyModel {
        Log.d("FamilyRepository", "Creating family: $familyName for userId=$userId")

        val familyId = "family_$userId"
        val inviteCode = generateInviteCode()

        val family = FamilyModel(
            familyId = familyId,
            familyName = familyName,
            memberIds = listOf(userId),
            familyXp = 0,
            familyStreak = 0,
            sharedChallengeIds = emptyList(),
            inviteCode = inviteCode
        )

        firestore.collection("families").document(familyId)
            .set(mapOf(
                "familyId" to familyId,
                "familyName" to familyName,
                "memberIds" to listOf(userId),
                "familyXp" to 0,
                "familyStreak" to 0,
                "sharedChallengeIds" to emptyList<String>(),
                "inviteCode" to inviteCode,
                "createdAt" to System.currentTimeMillis()
            ))
            .await()

        // Update user's familyId
        firestore.collection("users").document(userId)
            .update("familyId", familyId)
            .await()

        Log.d("FamilyRepository", "Family created: $familyId with inviteCode: $inviteCode")
        return family
    }

    override suspend fun getFamily(familyId: String): FamilyModel? {
        try {
            val doc = firestore.collection("families").document(familyId).get().await()
            if (doc.exists()) {
                val data = doc.data ?: return null
                return FamilyModel(
                    familyId = data["familyId"] as? String ?: "",
                    familyName = data["familyName"] as? String ?: "",
                    memberIds = (data["memberIds"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                    familyXp = (data["familyXp"] as? Number)?.toInt() ?: 0,
                    familyStreak = (data["familyStreak"] as? Number)?.toInt() ?: 0,
                    sharedChallengeIds = (data["sharedChallengeIds"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                    inviteCode = data["inviteCode"] as? String ?: ""
                )
            }
            return null
        } catch (e: Exception) {
            Log.e("FamilyRepository", "Error getting family", e)
            return null
        }
    }

    override suspend fun updateFamily(family: FamilyModel) {
        try {
            firestore.collection("families").document(family.familyId)
                .update(mapOf(
                    "familyName" to family.familyName,
                    "memberIds" to family.memberIds,
                    "familyXp" to family.familyXp,
                    "familyStreak" to family.familyStreak,
                    "sharedChallengeIds" to family.sharedChallengeIds
                ))
                .await()
            Log.d("FamilyRepository", "Family updated: ${family.familyId}")
        } catch (e: Exception) {
            Log.e("FamilyRepository", "Error updating family", e)
        }
    }

    override suspend fun addMemberToFamily(familyId: String, memberId: String) {
        try {
            val family = getFamily(familyId) ?: return
            val newMemberIds = family.memberIds.toMutableList()
            if (!newMemberIds.contains(memberId)) {
                newMemberIds.add(memberId)
                updateFamily(family.copy(memberIds = newMemberIds))

                // Update user's familyId
                firestore.collection("users").document(memberId)
                    .update("familyId", familyId)
                    .await()

                Log.d("FamilyRepository", "Member added to family: $familyId, memberId=$memberId")
            }
        } catch (e: Exception) {
            Log.e("FamilyRepository", "Error adding member to family", e)
        }
    }

    override suspend fun getInviteCode(familyId: String): String {
        val family = getFamily(familyId)
        return family?.inviteCode ?: ""
    }

    override fun observeFamily(familyId: String): Flow<FamilyModel?> = flow {
        try {
            val doc = firestore.collection("families").document(familyId).get().await()
            if (doc.exists()) {
                val data = doc.data
                if (data != null) {
                    val family = FamilyModel(
                        familyId = data["familyId"] as? String ?: "",
                        familyName = data["familyName"] as? String ?: "",
                        memberIds = (data["memberIds"] as? List<String>)?.toList() ?: emptyList(),
                        familyXp = (data["familyXp"] as? Number)?.toInt() ?: 0,
                        familyStreak = (data["familyStreak"] as? Number)?.toInt() ?: 0,
                        sharedChallengeIds = (data["sharedChallengeIds"] as? List<String>)?.toList() ?: emptyList(),
                        inviteCode = data["inviteCode"] as? String ?: ""
                    )
                    emit(family)
                } else {
                    emit(null)
                }
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.e("FamilyRepository", "Error observing family", e)
            emit(null)
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}