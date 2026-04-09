package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class WorldModelTest {

    // ── WorldNodeStatus enum ────────────────────────────────────────

    @Test
    fun `WorldNodeStatus has 3 entries`() {
        assertEquals(3, WorldNodeStatus.entries.size)
    }

    @Test
    fun `WorldNodeStatus includes LOCKED UNLOCKED COMPLETED`() {
        val names = WorldNodeStatus.entries.map { it.name }
        assertTrue(names.contains("LOCKED"))
        assertTrue(names.contains("UNLOCKED"))
        assertTrue(names.contains("COMPLETED"))
    }

    // ── WorldTheme enum ─────────────────────────────────────────────

    @Test
    fun `WorldTheme has 10 entries`() {
        assertEquals(10, WorldTheme.entries.size)
    }

    @Test
    fun `WorldTheme includes all themes`() {
        val names = WorldTheme.entries.map { it.name }
        assertTrue(names.contains("JUNGLE"))
        assertTrue(names.contains("OCEAN"))
        assertTrue(names.contains("SPACE"))
        assertTrue(names.contains("VOLCANO"))
        assertTrue(names.contains("ARCTIC"))
        assertTrue(names.contains("NEON_CITY"))
        assertTrue(names.contains("CRYSTAL"))
        assertTrue(names.contains("CLOUD"))
        assertTrue(names.contains("DESERT"))
        assertTrue(names.contains("COSMOS"))
    }

    // ── WorldNode defaults ──────────────────────────────────────────

    @Test
    fun `default node status is LOCKED`() {
        val node = WorldNode()
        assertEquals(WorldNodeStatus.LOCKED, node.status)
    }

    @Test
    fun `default node theme is JUNGLE`() {
        val node = WorldNode()
        assertEquals(WorldTheme.JUNGLE, node.theme)
    }

    @Test
    fun `default node positionX is 0_5`() {
        val node = WorldNode()
        assertEquals(0.5f, node.positionX, 0.01f)
    }

    @Test
    fun `default node positionY is 0_5`() {
        val node = WorldNode()
        assertEquals(0.5f, node.positionY, 0.01f)
    }

    @Test
    fun `default rewardXp is 50`() {
        val node = WorldNode()
        assertEquals(50, node.rewardXp)
    }

    @Test
    fun `default isSpecial is false`() {
        val node = WorldNode()
        assertFalse(node.isSpecial)
    }

    @Test
    fun `default emoji is star`() {
        val node = WorldNode()
        assertEquals("⭐", node.emoji)
    }

    // ── WorldModel defaults ─────────────────────────────────────────

    @Test
    fun `default world theme is JUNGLE`() {
        val world = WorldModel()
        assertEquals(WorldTheme.JUNGLE, world.theme)
    }

    @Test
    fun `default world nodes is empty`() {
        val world = WorldModel()
        assertTrue(world.nodes.isEmpty())
    }

    @Test
    fun `world stores nodes`() {
        val nodes = listOf(
            WorldNode(nodeId = "n1", levelNumber = 1),
            WorldNode(nodeId = "n2", levelNumber = 2)
        )
        val world = WorldModel(worldId = "w1", nodes = nodes)
        assertEquals(2, world.nodes.size)
        assertEquals("n1", world.nodes[0].nodeId)
    }
}
