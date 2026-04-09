package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class InteractionBlockTest {

    // ── InteractionBlockType enum ───────────────────────────────────

    @Test
    fun `InteractionBlockType has 9 entries`() {
        assertEquals(9, InteractionBlockType.entries.size)
    }

    @Test
    fun `includes all expected block types`() {
        val names = InteractionBlockType.entries.map { it.name }
        assertTrue(names.contains("TAP_SELECT"))
        assertTrue(names.contains("MULTI_SELECT"))
        assertTrue(names.contains("DRAW_INPUT"))
        assertTrue(names.contains("TEXT_INPUT"))
        assertTrue(names.contains("PHOTO_CAPTURE"))
        assertTrue(names.contains("TIMER"))
        assertTrue(names.contains("CHECKBOX"))
        assertTrue(names.contains("PARENT_CONFIRM"))
        assertTrue(names.contains("DUAL_CONFIRM"))
    }

    @Test
    fun `valueOf round-trips for InteractionBlockType`() {
        for (type in InteractionBlockType.entries) {
            assertEquals(type, InteractionBlockType.valueOf(type.name))
        }
    }

    // ── InteractionBlock defaults ───────────────────────────────────

    @Test
    fun `default blockId is empty`() {
        assertEquals("", InteractionBlock().blockId)
    }

    @Test
    fun `default type is TAP_SELECT`() {
        assertEquals(InteractionBlockType.TAP_SELECT, InteractionBlock().type)
    }

    @Test
    fun `default config is empty map`() {
        assertTrue(InteractionBlock().config.isEmpty())
    }

    @Test
    fun `default required is true`() {
        assertTrue(InteractionBlock().required)
    }

    // ── custom values ───────────────────────────────────────────────

    @Test
    fun `stores blockId`() {
        val block = InteractionBlock(blockId = "blk_42")
        assertEquals("blk_42", block.blockId)
    }

    @Test
    fun `stores type`() {
        val block = InteractionBlock(type = InteractionBlockType.PHOTO_CAPTURE)
        assertEquals(InteractionBlockType.PHOTO_CAPTURE, block.type)
    }

    @Test
    fun `stores config map`() {
        val cfg = mapOf("key" to "value", "count" to 5)
        val block = InteractionBlock(config = cfg)
        assertEquals("value", block.config["key"])
        assertEquals(5, block.config["count"])
    }

    @Test
    fun `stores required false`() {
        val block = InteractionBlock(required = false)
        assertFalse(block.required)
    }

    // ── equality ────────────────────────────────────────────────────

    @Test
    fun `data class equality`() {
        val a = InteractionBlock(blockId = "b1", type = InteractionBlockType.TIMER)
        val b = InteractionBlock(blockId = "b1", type = InteractionBlockType.TIMER)
        assertEquals(a, b)
    }

    @Test
    fun `copy updates selected fields`() {
        val orig = InteractionBlock(blockId = "b1", required = true)
        val copy = orig.copy(required = false)
        assertEquals("b1", copy.blockId)
        assertFalse(copy.required)
    }
}
