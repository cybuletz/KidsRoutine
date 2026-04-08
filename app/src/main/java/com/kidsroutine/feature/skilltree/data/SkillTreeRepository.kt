package com.kidsroutine.feature.skilltree.data

import com.kidsroutine.core.model.SkillTree

interface SkillTreeRepository {
    suspend fun getSkillTree(userId: String): SkillTree?
    suspend fun saveSkillTree(tree: SkillTree)
    suspend fun unlockNode(userId: String, nodeId: String): SkillTree?
    suspend fun updateProgress(userId: String, nodeId: String, newProgress: Int): SkillTree?
}
