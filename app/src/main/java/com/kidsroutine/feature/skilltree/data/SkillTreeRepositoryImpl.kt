package com.kidsroutine.feature.skilltree.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.SkillBranch
import com.kidsroutine.core.model.SkillNode
import com.kidsroutine.core.model.SkillTree
import com.kidsroutine.core.model.SkillTreeDefaults
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillTreeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SkillTreeRepository {

    private val collection = "skill_trees"

    override suspend fun getSkillTree(userId: String): SkillTree? {
        return try {
            val doc = firestore.collection(collection).document(userId).get().await()
            if (!doc.exists()) {
                // Initialize default tree
                val defaultTree = SkillTree(
                    userId = userId,
                    branches = SkillTreeDefaults.createDefaultNodes()
                )
                saveSkillTree(defaultTree)
                return defaultTree
            }
            docToSkillTree(doc, userId)
        } catch (e: Exception) {
            Log.e("SkillTreeRepo", "getSkillTree error", e)
            null
        }
    }

    override suspend fun saveSkillTree(tree: SkillTree) {
        try {
            val data = mutableMapOf<String, Any>(
                "userId" to tree.userId,
                "totalNodesUnlocked" to tree.totalNodesUnlocked,
                "totalXpBonusPercent" to tree.totalXpBonusPercent
            )

            // Flatten nodes into a list for Firestore storage
            val nodesList = tree.branches.flatMap { (_, nodes) ->
                nodes.map { node ->
                    mapOf(
                        "nodeId" to node.nodeId,
                        "branch" to node.branch.name,
                        "title" to node.title,
                        "description" to node.description,
                        "emoji" to node.emoji,
                        "requiredTaskCount" to node.requiredTaskCount,
                        "requiredLevel" to node.requiredLevel,
                        "prerequisiteNodeId" to (node.prerequisiteNodeId ?: ""),
                        "xpBonusPercent" to node.xpBonusPercent,
                        "avatarItemId" to (node.avatarItemId ?: ""),
                        "petTrickId" to (node.petTrickId ?: ""),
                        "badgeId" to (node.badgeId ?: ""),
                        "isUnlocked" to node.isUnlocked,
                        "unlockedAt" to node.unlockedAt,
                        "currentProgress" to node.currentProgress
                    )
                }
            }
            data["nodes"] = nodesList

            firestore.collection(collection).document(tree.userId).set(data).await()
        } catch (e: Exception) {
            Log.e("SkillTreeRepo", "saveSkillTree error", e)
        }
    }

    override suspend fun unlockNode(userId: String, nodeId: String): SkillTree? {
        return try {
            val tree = getSkillTree(userId) ?: return null
            val updatedBranches = tree.branches.mapValues { (_, nodes) ->
                nodes.map { node ->
                    if (node.nodeId == nodeId && !node.isUnlocked) {
                        node.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis())
                    } else node
                }
            }
            val unlocked = updatedBranches.values.flatten().count { it.isUnlocked }
            val xpBonus = updatedBranches.values.flatten().filter { it.isUnlocked }.sumOf { it.xpBonusPercent }
            val updated = tree.copy(
                branches = updatedBranches,
                totalNodesUnlocked = unlocked,
                totalXpBonusPercent = xpBonus
            )
            saveSkillTree(updated)
            updated
        } catch (e: Exception) {
            Log.e("SkillTreeRepo", "unlockNode error", e)
            null
        }
    }

    override suspend fun updateProgress(userId: String, nodeId: String, newProgress: Int): SkillTree? {
        return try {
            val tree = getSkillTree(userId) ?: return null
            val updatedBranches = tree.branches.mapValues { (_, nodes) ->
                nodes.map { node ->
                    if (node.nodeId == nodeId) {
                        node.copy(currentProgress = newProgress)
                    } else node
                }
            }
            val updated = tree.copy(branches = updatedBranches)
            saveSkillTree(updated)
            updated
        } catch (e: Exception) {
            Log.e("SkillTreeRepo", "updateProgress error", e)
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun docToSkillTree(doc: com.google.firebase.firestore.DocumentSnapshot, userId: String): SkillTree {
        val nodesList = (doc.get("nodes") as? List<Map<String, Any>>) ?: emptyList()
        val nodesByBranch = mutableMapOf<SkillBranch, MutableList<SkillNode>>()

        for (map in nodesList) {
            val branchName = map["branch"] as? String ?: continue
            val branch = try { SkillBranch.valueOf(branchName) } catch (_: Exception) { continue }
            val node = SkillNode(
                nodeId = map["nodeId"] as? String ?: "",
                branch = branch,
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                emoji = map["emoji"] as? String ?: "",
                requiredTaskCount = (map["requiredTaskCount"] as? Number)?.toInt() ?: 10,
                requiredLevel = (map["requiredLevel"] as? Number)?.toInt() ?: 0,
                prerequisiteNodeId = (map["prerequisiteNodeId"] as? String)?.ifBlank { null },
                xpBonusPercent = (map["xpBonusPercent"] as? Number)?.toInt() ?: 0,
                avatarItemId = (map["avatarItemId"] as? String)?.ifBlank { null },
                petTrickId = (map["petTrickId"] as? String)?.ifBlank { null },
                badgeId = (map["badgeId"] as? String)?.ifBlank { null },
                isUnlocked = map["isUnlocked"] as? Boolean ?: false,
                unlockedAt = (map["unlockedAt"] as? Number)?.toLong() ?: 0L,
                currentProgress = (map["currentProgress"] as? Number)?.toInt() ?: 0
            )
            nodesByBranch.getOrPut(branch) { mutableListOf() }.add(node)
        }

        return SkillTree(
            userId = userId,
            branches = nodesByBranch,
            totalNodesUnlocked = (doc.getLong("totalNodesUnlocked") ?: 0).toInt(),
            totalXpBonusPercent = (doc.getLong("totalXpBonusPercent") ?: 0).toInt()
        )
    }
}
