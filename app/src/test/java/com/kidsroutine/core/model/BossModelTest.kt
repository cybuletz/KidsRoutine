package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class BossModelTest {

    // ── hpPercentage ────────────────────────────────────────────────

    @Test
    fun `hpPercentage at full health`() {
        val boss = BossModel(maxHp = 200, currentHp = 200)
        assertEquals(1.0f, boss.hpPercentage, 0.01f)
    }

    @Test
    fun `hpPercentage at half health`() {
        val boss = BossModel(maxHp = 200, currentHp = 100)
        assertEquals(0.5f, boss.hpPercentage, 0.01f)
    }

    @Test
    fun `hpPercentage at zero health`() {
        val boss = BossModel(maxHp = 200, currentHp = 0)
        assertEquals(0f, boss.hpPercentage, 0.01f)
    }

    @Test
    fun `hpPercentage zero when maxHp is zero`() {
        val boss = BossModel(maxHp = 0, currentHp = 0)
        assertEquals(0f, boss.hpPercentage, 0.01f)
    }

    // ── isAlive ─────────────────────────────────────────────────────

    @Test
    fun `isAlive true when hp positive and not expired`() {
        val boss = BossModel(currentHp = 50, isExpired = false)
        assertTrue(boss.isAlive)
    }

    @Test
    fun `isAlive false when hp is zero`() {
        val boss = BossModel(currentHp = 0, isExpired = false)
        assertFalse(boss.isAlive)
    }

    @Test
    fun `isAlive false when expired`() {
        val boss = BossModel(currentHp = 100, isExpired = true)
        assertFalse(boss.isAlive)
    }

    // ── damageForTask ───────────────────────────────────────────────

    @Test
    fun `easy task deals 5 damage`() {
        val boss = BossModel()
        val task = TaskModel(difficulty = DifficultyLevel.EASY)
        assertEquals(5, boss.damageForTask(task))
    }

    @Test
    fun `medium task deals 10 damage`() {
        val boss = BossModel()
        val task = TaskModel(difficulty = DifficultyLevel.MEDIUM)
        assertEquals(10, boss.damageForTask(task))
    }

    @Test
    fun `hard task deals 20 damage`() {
        val boss = BossModel()
        val task = TaskModel(difficulty = DifficultyLevel.HARD)
        assertEquals(20, boss.damageForTask(task))
    }

    @Test
    fun `coop task gets 1_5x multiplier`() {
        val boss = BossModel()
        val task = TaskModel(difficulty = DifficultyLevel.MEDIUM, requiresCoop = true)
        // 10 * 1.5 = 15
        assertEquals(15, boss.damageForTask(task))
    }

    @Test
    fun `easy coop task deals 7`() {
        val boss = BossModel()
        val task = TaskModel(difficulty = DifficultyLevel.EASY, requiresCoop = true)
        // 5 * 1.5 = 7.5 → 7 (toInt)
        assertEquals(7, boss.damageForTask(task))
    }

    // ── hpForFamily (companion) ─────────────────────────────────────

    @Test
    fun `hpForFamily scales with family size`() {
        val hp1 = BossModel.hpForFamily(BossType.HOMEWORK_HYDRA, 1, DifficultyLevel.MEDIUM)
        val hp4 = BossModel.hpForFamily(BossType.HOMEWORK_HYDRA, 4, DifficultyLevel.MEDIUM)
        assertTrue(hp4 > hp1)
    }

    @Test
    fun `hpForFamily scales with difficulty`() {
        val hpEasy = BossModel.hpForFamily(BossType.HOMEWORK_HYDRA, 3, DifficultyLevel.EASY)
        val hpMed  = BossModel.hpForFamily(BossType.HOMEWORK_HYDRA, 3, DifficultyLevel.MEDIUM)
        val hpHard = BossModel.hpForFamily(BossType.HOMEWORK_HYDRA, 3, DifficultyLevel.HARD)
        assertTrue(hpEasy < hpMed)
        assertTrue(hpMed < hpHard)
    }

    @Test
    fun `hpForFamily uses boss base hp`() {
        val hpHydra  = BossModel.hpForFamily(BossType.HOMEWORK_HYDRA, 2, DifficultyLevel.MEDIUM)
        val hpKraken = BossModel.hpForFamily(BossType.CHAOS_KRAKEN, 2, DifficultyLevel.MEDIUM)
        assertTrue("Chaos Kraken (350 base) should have more HP than Homework Hydra (200 base)",
            hpKraken > hpHydra)
    }

    @Test
    fun `hpForFamily with family size 1 at least 1x base`() {
        val hp = BossModel.hpForFamily(BossType.HOMEWORK_HYDRA, 1, DifficultyLevel.MEDIUM)
        // sizeMultiplier = max(1 * 0.75, 1) = 1.0, diffMultiplier = 1.0
        // 200 * 1.0 * 1.0 = 200
        assertTrue(hp >= BossType.HOMEWORK_HYDRA.baseHp)
    }

    // ── BossType enum ───────────────────────────────────────────────

    @Test
    fun `BossType has 6 entries`() {
        assertEquals(6, BossType.entries.size)
    }

    @Test
    fun `all boss types have positive base hp`() {
        BossType.entries.forEach {
            assertTrue("${it.name} should have positive baseHp", it.baseHp > 0)
        }
    }

    @Test
    fun `all boss types have non-empty display names`() {
        BossType.entries.forEach {
            assertTrue(it.displayName.isNotEmpty())
        }
    }
}
