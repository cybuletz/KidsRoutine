package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class SkillTreeModelTest {

    // ── SkillNode.progressPercent ───────────────────────────────────

    @Test
    fun `progressPercent at zero`() {
        val node = SkillNode(requiredTaskCount = 10, currentProgress = 0)
        assertEquals(0f, node.progressPercent, 0.01f)
    }

    @Test
    fun `progressPercent at half`() {
        val node = SkillNode(requiredTaskCount = 20, currentProgress = 10)
        assertEquals(0.5f, node.progressPercent, 0.01f)
    }

    @Test
    fun `progressPercent capped at 1_0`() {
        val node = SkillNode(requiredTaskCount = 10, currentProgress = 20)
        assertEquals(1.0f, node.progressPercent, 0.01f)
    }

    @Test
    fun `progressPercent with zero required`() {
        val node = SkillNode(requiredTaskCount = 0, currentProgress = 5)
        assertEquals(0f, node.progressPercent, 0.01f)
    }

    // ── SkillBranch ─────────────────────────────────────────────────

    @Test
    fun `there are 5 skill branches`() {
        assertEquals(5, SkillBranch.entries.size)
    }

    @Test
    fun `all branches have non-empty display names`() {
        SkillBranch.entries.forEach {
            assertTrue(it.displayName.isNotEmpty())
        }
    }

    @Test
    fun `all branches have related categories`() {
        SkillBranch.entries.forEach {
            assertTrue("${it.name} should have related categories", it.relatedCategories.isNotEmpty())
        }
    }

    @Test
    fun `RESPONSIBILITY branch relates to CHORES`() {
        assertTrue(SkillBranch.RESPONSIBILITY.relatedCategories.contains(TaskCategory.CHORES))
    }

    @Test
    fun `LEARNING branch relates to LEARNING category`() {
        assertTrue(SkillBranch.LEARNING.relatedCategories.contains(TaskCategory.LEARNING))
    }

    // ── SkillTreeDefaults ───────────────────────────────────────────

    @Test
    fun `defaults create nodes for all 5 branches`() {
        val defaults = SkillTreeDefaults.createDefaultNodes()
        assertEquals(5, defaults.size)
    }

    @Test
    fun `each branch has 3 nodes`() {
        val defaults = SkillTreeDefaults.createDefaultNodes()
        defaults.values.forEach { nodes ->
            assertEquals(3, nodes.size)
        }
    }

    @Test
    fun `first node in each branch has no prerequisite`() {
        val defaults = SkillTreeDefaults.createDefaultNodes()
        defaults.values.forEach { nodes ->
            assertNull(nodes.first().prerequisiteNodeId)
        }
    }

    @Test
    fun `second node references first as prerequisite`() {
        val defaults = SkillTreeDefaults.createDefaultNodes()
        defaults.values.forEach { nodes ->
            assertEquals(nodes[0].nodeId, nodes[1].prerequisiteNodeId)
        }
    }

    @Test
    fun `third node references second as prerequisite`() {
        val defaults = SkillTreeDefaults.createDefaultNodes()
        defaults.values.forEach { nodes ->
            assertEquals(nodes[1].nodeId, nodes[2].prerequisiteNodeId)
        }
    }

    @Test
    fun `xpBonusPercent increases through chain`() {
        val defaults = SkillTreeDefaults.createDefaultNodes()
        defaults.values.forEach { nodes ->
            assertTrue(nodes[0].xpBonusPercent < nodes[1].xpBonusPercent)
            assertTrue(nodes[1].xpBonusPercent < nodes[2].xpBonusPercent)
        }
    }

    @Test
    fun `requiredTaskCount increases through chain`() {
        val defaults = SkillTreeDefaults.createDefaultNodes()
        defaults.values.forEach { nodes ->
            assertTrue(nodes[0].requiredTaskCount < nodes[1].requiredTaskCount)
            assertTrue(nodes[1].requiredTaskCount < nodes[2].requiredTaskCount)
        }
    }

    @Test
    fun `all node IDs are unique`() {
        val defaults = SkillTreeDefaults.createDefaultNodes()
        val allIds = defaults.values.flatten().map { it.nodeId }
        assertEquals(allIds.size, allIds.toSet().size)
    }
}
