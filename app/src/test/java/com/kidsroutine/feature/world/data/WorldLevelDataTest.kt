package com.kidsroutine.feature.world.data

import org.junit.Assert.*
import org.junit.Test

class WorldLevelDataTest {

    // ── xpForLevel basic levels ─────────────────────────────────────

    @Test
    fun `level 1 requires 50 XP`() {
        assertEquals(50, WorldLevelData.xpForLevel(1))
    }

    @Test
    fun `level 2 requires 100 XP`() {
        assertEquals(100, WorldLevelData.xpForLevel(2))
    }

    @Test
    fun `level 50 requires 2500 XP`() {
        // 50 levels * 50 XP = 2500
        assertEquals(2500, WorldLevelData.xpForLevel(50))
    }

    // ── xpForLevel tier transitions ─────────────────────────────────

    @Test
    fun `level 51 starts tier 2 at 80 XP per level`() {
        // tier 1: 50*50=2500, tier 2 level 1: 80
        assertEquals(2580, WorldLevelData.xpForLevel(51))
    }

    @Test
    fun `level 100 cumulative XP`() {
        // tier 1: 50*50=2500, tier 2: 50*80=4000
        assertEquals(6500, WorldLevelData.xpForLevel(100))
    }

    @Test
    fun `level 150 cumulative XP`() {
        // tier 1: 2500, tier 2: 4000, tier 3: 50*120=6000
        assertEquals(12500, WorldLevelData.xpForLevel(150))
    }

    @Test
    fun `level 200 cumulative XP`() {
        // tier 1: 2500, tier 2: 4000, tier 3: 6000, tier 4: 50*160=8000
        assertEquals(20500, WorldLevelData.xpForLevel(200))
    }

    // ── xpForLevel monotonically increasing ─────────────────────────

    @Test
    fun `XP is monotonically increasing`() {
        var prev = 0
        for (level in 1..500) {
            val xp = WorldLevelData.xpForLevel(level)
            assertTrue("Level $level ($xp) should be > level ${level-1} ($prev)", xp > prev)
            prev = xp
        }
    }

    // ── xpForLevel final level ──────────────────────────────────────

    @Test
    fun `level 500 is the max and has large XP`() {
        val xp = WorldLevelData.xpForLevel(500)
        assertTrue("Level 500 XP should be > 100000", xp > 100000)
    }

    // ── XP per-level increments increase per tier ───────────────────

    @Test
    fun `XP increment grows at tier boundaries`() {
        val incrementT1 = WorldLevelData.xpForLevel(50) - WorldLevelData.xpForLevel(49)
        val incrementT2 = WorldLevelData.xpForLevel(100) - WorldLevelData.xpForLevel(99)
        val incrementT3 = WorldLevelData.xpForLevel(150) - WorldLevelData.xpForLevel(149)
        assertTrue(incrementT2 > incrementT1)
        assertTrue(incrementT3 > incrementT2)
    }

    // ── xpForLevel at boundary levels ───────────────────────────────

    @Test
    fun `level 250 cumulative XP`() {
        // t1: 2500, t2: 4000, t3: 6000, t4: 8000, t5: 50*200=10000
        assertEquals(30500, WorldLevelData.xpForLevel(250))
    }

    @Test
    fun `level 300 cumulative XP`() {
        // +t6: 50*250=12500
        assertEquals(43000, WorldLevelData.xpForLevel(300))
    }

    @Test
    fun `level 350 cumulative XP`() {
        // +t7: 50*300=15000
        assertEquals(58000, WorldLevelData.xpForLevel(350))
    }

    @Test
    fun `level 400 cumulative XP`() {
        // +t8: 50*350=17500
        assertEquals(75500, WorldLevelData.xpForLevel(400))
    }

    @Test
    fun `level 450 cumulative XP`() {
        // +t9: 50*400=20000
        assertEquals(95500, WorldLevelData.xpForLevel(450))
    }

    @Test
    fun `level 500 cumulative XP`() {
        // +t10: 50*500=25000
        assertEquals(120500, WorldLevelData.xpForLevel(500))
    }
}
