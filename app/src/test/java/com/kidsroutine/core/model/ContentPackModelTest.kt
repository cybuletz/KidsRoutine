package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class ContentPackModelTest {

    // ── ContentPackTier enum ────────────────────────────────────────

    @Test
    fun `ContentPackTier has 3 entries`() {
        assertEquals(3, ContentPackTier.entries.size)
    }

    @Test
    fun `ContentPackTier includes FREE PRO PREMIUM`() {
        val names = ContentPackTier.entries.map { it.name }
        assertTrue(names.contains("FREE"))
        assertTrue(names.contains("PRO"))
        assertTrue(names.contains("PREMIUM"))
    }

    // ── ContentPack defaults ────────────────────────────────────────

    @Test
    fun `default tier is FREE`() {
        val pack = ContentPack()
        assertEquals(ContentPackTier.FREE, pack.tier)
    }

    @Test
    fun `default isUnlocked is false`() {
        val pack = ContentPack()
        assertFalse(pack.isUnlocked)
    }

    @Test
    fun `default emoji is package emoji`() {
        val pack = ContentPack()
        assertEquals("📦", pack.emoji)
    }

    @Test
    fun `default accentColor is orange hex`() {
        val pack = ContentPack()
        assertEquals("#FF6B35", pack.accentColor)
    }

    // ── BuiltInContentPacks ─────────────────────────────────────────

    @Test
    fun `builtIn packs list is not empty`() {
        assertTrue(BuiltInContentPacks.all.isNotEmpty())
    }

    @Test
    fun `builtIn packs have 5 entries`() {
        assertEquals(5, BuiltInContentPacks.all.size)
    }

    @Test
    fun `all packs have non-empty names`() {
        BuiltInContentPacks.all.forEach {
            assertTrue("Pack ${it.packId} should have a name", it.name.isNotEmpty())
        }
    }

    @Test
    fun `all packs have non-empty descriptions`() {
        BuiltInContentPacks.all.forEach {
            assertTrue("Pack ${it.packId} should have a description", it.description.isNotEmpty())
        }
    }

    @Test
    fun `all packs have unique IDs`() {
        val ids = BuiltInContentPacks.all.map { it.packId }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `all packs have positive taskCount`() {
        BuiltInContentPacks.all.forEach {
            assertTrue("Pack ${it.packId} should have tasks", it.taskCount > 0)
        }
    }

    @Test
    fun `all packs have previewTaskTitles`() {
        BuiltInContentPacks.all.forEach {
            assertTrue("Pack ${it.packId} should have preview titles", it.previewTaskTitles.isNotEmpty())
        }
    }

    @Test
    fun `No-Screen Day pack is FREE tier`() {
        val pack = BuiltInContentPacks.all.find { it.packId == "pack_no_screen" }
        assertNotNull(pack)
        assertEquals(ContentPackTier.FREE, pack!!.tier)
    }

    @Test
    fun `Discipline Pack is PRO tier`() {
        val pack = BuiltInContentPacks.all.find { it.packId == "pack_discipline" }
        assertNotNull(pack)
        assertEquals(ContentPackTier.PRO, pack!!.tier)
    }

    @Test
    fun `Outdoor Week pack is PRO tier with 0 xpCost`() {
        val pack = BuiltInContentPacks.all.find { it.packId == "pack_outdoor_week" }
        assertNotNull(pack)
        assertEquals(ContentPackTier.PRO, pack!!.tier)
        assertEquals(0, pack.xpCost)
    }
}
