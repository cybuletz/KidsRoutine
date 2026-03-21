package com.kidsroutine.feature.family.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.FamilyModel
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.TaskReward
import com.kidsroutine.core.model.TaskCategory
import com.kidsroutine.core.model.DifficultyLevel
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

    override suspend fun getFamiliesByInviteCode(inviteCode: String): List<FamilyModel> {
        return try {
            Log.d("FamilyRepository", "Searching for family with code: $inviteCode")
            val snapshot = firestore.collection("families")
                .whereEqualTo("inviteCode", inviteCode)
                .limit(1)
                .get()
                .await()

            val families = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    FamilyModel(
                        familyId = data["familyId"] as? String ?: "",
                        familyName = data["familyName"] as? String ?: "",
                        memberIds = (data["memberIds"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                        familyXp = (data["familyXp"] as? Number)?.toInt() ?: 0,
                        familyStreak = (data["familyStreak"] as? Number)?.toInt() ?: 0,
                        sharedChallengeIds = (data["sharedChallengeIds"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                        inviteCode = data["inviteCode"] as? String ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("FamilyRepository", "Error parsing family", e)
                    null
                }
            }

            Log.d("FamilyRepository", "Found ${families.size} families with code: $inviteCode")
            families
        } catch (e: Exception) {
            Log.e("FamilyRepository", "Error searching for family", e)
            emptyList()
        }
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
            val updatedMembers = family.memberIds + memberId

            firestore.collection("families").document(familyId)
                .update("memberIds", updatedMembers)
                .await()

            firestore.collection("users").document(memberId)
                .update("familyId", familyId)
                .await()

            Log.d("FamilyRepository", "Member added: $memberId to family: $familyId")
        } catch (e: Exception) {
            Log.e("FamilyRepository", "Error adding member to family", e)
            throw e
        }
    }

    override suspend fun getInviteCode(familyId: String): String {
        return try {
            val doc = firestore.collection("families").document(familyId).get().await()
            doc.data?.get("inviteCode") as? String ?: ""
        } catch (e: Exception) {
            Log.e("FamilyRepository", "Error getting invite code", e)
            ""
        }
    }

    override fun observeFamily(familyId: String): Flow<FamilyModel?> = flow {
        try {
            firestore.collection("families").document(familyId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FamilyRepository", "Error observing family", error)
                        return@addSnapshotListener
                    }

                    if (snapshot?.exists() == true) {
                        val data = snapshot.data ?: return@addSnapshotListener
                        val family = FamilyModel(
                            familyId = data["familyId"] as? String ?: "",
                            familyName = data["familyName"] as? String ?: "",
                            memberIds = (data["memberIds"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                            familyXp = (data["familyXp"] as? Number)?.toInt() ?: 0,
                            familyStreak = (data["familyStreak"] as? Number)?.toInt() ?: 0,
                            sharedChallengeIds = (data["sharedChallengeIds"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                            inviteCode = data["inviteCode"] as? String ?: ""
                        )
                    }
                }
        } catch (e: Exception) {
            Log.e("FamilyRepository", "Error observing family", e)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CHILD → PARENT TASK SYSTEM
    // ═══════════════════════════════════════════════════════════════════════

    override  suspend fun proposeChildTask(familyId: String, childId: String, task: TaskModel) {
        try {
            Log.d("FamilyRepository", "Proposing child task: ${task.title}")

            // Store in pending_tasks collection
            firestore
                .collection("families")
                .document(familyId)
                .collection("pending_child_tasks")
                .document(task.id)
                .set(mapOf(
                    "id" to task.id,
                    "title" to task.title,
                    "description" to task.description,
                    "category" to task.category.name,
                    "difficulty" to task.difficulty.name,
                    "estimatedDurationSec" to task.estimatedDurationSec,
                    "xp" to task.reward.xp,
                    "createdBy" to "CHILD",
                    "childId" to childId,
                    "requiresParent" to true,
                    "status" to "PENDING",
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "familyId" to familyId
                ))
                .await()

            Log.d("FamilyRepository", "Child task proposed successfully")
        } catch (e: Exception) {
            Log.e("FamilyRepository", "Error proposing child task", e)
            throw e
        }
    }

    override suspend fun getPendingChildTasks(familyId: String): List<TaskModel> {
        return try {
            Log.d("FamilyRepository", "Fetching pending child tasks for family: $familyId")
            val snapshot = firestore
                .collection("families")
                .document(familyId)
                .collection("pending_child_tasks")
                .whereEqualTo("status", "PENDING")
                .get()
                .await()

            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    TaskModel(
                        id = data["id"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        category = try {
                            TaskCategory.valueOf(data["category"] as? String ?: "FAMILY")
                        } catch (e: Exception) {
                            TaskCategory.FAMILY
                        },
                        difficulty = try {
                            DifficultyLevel.valueOf(data["difficulty"] as? String ?: "EASY")
                        } catch (e: Exception) {
                            DifficultyLevel.EASY
                        },
                        estimatedDurationSec = (data["estimatedDurationSec"] as? Long)?.toInt() ?: 0,
                        reward = TaskReward(
                            xp = (data["xp"] as? Long)?.toInt() ?: 0
                        ),
                        requiresParent = data["requiresParent"] as? Boolean ?: true,
                        familyId = familyId
                    )
                } catch (e: Exception) {
                    Log.e("FamilyRepository", "Error parsing pending task", e)
                    null
                }
            }
            Log.d("FamilyRepository", "Fetched ${tasks.size} pending child tasks")
            tasks
        } catch (e: Exception) {
            Log.e("FamilyRepository", "Error fetching pending child tasks", e)
            throw e
        }
    }

    override suspend fun approveChildTask(familyId: String, taskId: String) {
        try {
            Log.d("FamilyRepository", "Approving child task: $taskId")

            // Move from pending to approved
            val pendingTask = firestore
                .collection("families")
                .document(familyId)
                .collection("pending_child_tasks")
                .document(taskId)
                .get()
                .await()

            val taskData = pendingTask.data ?: throw Exception("Task not found")

            // Create approved task in active tasks
            firestore
                .collection("families")
                .document(familyId)
                .collection("tasks")
                .document(taskId)
                .set(taskData + mapOf(
                    "status" to "APPROVED",
                    "approvedAt" to com.google.firebase.Timestamp.now()
                ))
                .await()

            // Delete from pending
            firestore
                .collection("families")
                .document(familyId)
                .collection("pending_child_tasks")
                .document(taskId)
                .delete()
                .await()

            Log.d("FamilyRepository", "Child task approved successfully")
        } catch (e: Exception) {
            Log.e("FamilyRepository", "Error approving child task", e)
            throw e
        }
    }

    override suspend fun rejectChildTask(familyId: String, taskId: String, reason: String) {
        try {
            Log.d("FamilyRepository", "Rejecting child task: $taskId")

            // Update status to rejected
            firestore
                .collection("families")
                .document(familyId)
                .collection("pending_child_tasks")
                .document(taskId)
                .update(mapOf(
                    "status" to "REJECTED",
                    "rejectionReason" to reason,
                    "rejectedAt" to com.google.firebase.Timestamp.now()
                ))
                .await()

            Log.d("FamilyRepository", "Child task rejected successfully")
        } catch (e: Exception) {
            Log.e("FamilyRepository", "Error rejecting child task", e)
            throw e
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}