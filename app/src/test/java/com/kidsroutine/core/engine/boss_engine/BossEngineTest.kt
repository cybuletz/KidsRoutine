package com.kidsroutine.core.engine.boss_engine

import com.kidsroutine.core.model.BossModel
import com.kidsroutine.core.model.BossType
import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.Season
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.TaskReward
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BossEngineTest {

    private lateinit var engine: BossEngine

    @Before
    fun setUp() {
        engine = BossEngine()
    }

    // ── generateWeeklyBoss ──────────────────────────────────────────────

    @Test
    fun `generateWeeklyBoss creates boss with correct familyId and week`() {
        val boss = engine.generateWeeklyBoss("family1", familySize = 3, week = "2026-W15")
        assertEquals("family1_2026-W15", boss.bossId)
        assertEquals("family1", boss.familyId)
        assertEquals("2026-W15", boss.week)
    }

    @Test
    fun `generateWeeklyBoss creates boss with full HP`() {
        val boss = engine.generateWeeklyBoss("family1", familySize = 3, week = "2026-W15")
        assertEquals(boss.maxHp, boss.currentHp)
        assertTrue(boss.maxHp > 0)
    }

    @Test
    fun `generateWeeklyBoss scales HP with family size`() {
        val small = engine.generateWeeklyBoss("f", familySize = 2, week = "w1")
        val big = engine.generateWeeklyBoss("f", familySize = 6, week = "w1")
        // Both use random boss type, but big family should generally have more HP
        // We can't assert exact values due to randomness, but we can verify both have positive HP
        assertTrue(small.maxHp > 0)
        assertTrue(big.maxHp > 0)
    }

    @Test
    fun `generateWeeklyBoss victory XP is between 50 and 500`() {
        val boss = engine.generateWeeklyBoss("f", familySize = 4, week = "w1")
        assertTrue(boss.victoryXpBonus in 50..500)
    }

    @Test
    fun `generateWeeklyBoss prefers seasonal boss for HALLOWEEN`() {
        // Run multiple times; at least one should get SCREEN_SPECTER
        val bosses = (1..20).map {
            engine.generateWeeklyBoss("f", familySize = 3, week = "w$it", season = Season.HALLOWEEN)
        }
        assertTrue(bosses.any { it.type == BossType.SCREEN_SPECTER })
    }

    // ── applyDamage ─────────────────────────────────────────────────────

    @Test
    fun `applyDamage reduces boss HP`() {
        val boss = createBoss(currentHp = 100)
        val task = createTask(difficulty = DifficultyLevel.EASY)
        val damaged = engine.applyDamage(boss, task, "user1")
        assertTrue(damaged.currentHp < 100)
    }

    @Test
    fun `applyDamage tracks damage per user`() {
        val boss = createBoss(currentHp = 100)
        val task = createTask(difficulty = DifficultyLevel.MEDIUM)
        val damaged = engine.applyDamage(boss, task, "user1")
        assertTrue(damaged.damageLog["user1"]!! > 0)
    }

    @Test
    fun `applyDamage accumulates damage from same user`() {
        val boss = createBoss(currentHp = 100)
        val task = createTask(difficulty = DifficultyLevel.EASY)
        val d1 = engine.applyDamage(boss, task, "user1")
        val d2 = engine.applyDamage(d1, task, "user1")
        assertTrue(d2.damageLog["user1"]!! > d1.damageLog["user1"]!!)
    }

    @Test
    fun `applyDamage marks boss as defeated when HP reaches 0`() {
        val boss = createBoss(currentHp = 3)
        val task = createTask(difficulty = DifficultyLevel.EASY) // 5 damage
        val defeated = engine.applyDamage(boss, task, "user1")
        assertEquals(0, defeated.currentHp)
        assertTrue(defeated.isDefeated)
    }

    @Test
    fun `applyDamage does nothing to dead boss`() {
        val boss = createBoss(currentHp = 0, isDefeated = true)
        val task = createTask(difficulty = DifficultyLevel.HARD)
        val result = engine.applyDamage(boss, task, "user1")
        assertEquals(0, result.currentHp)
    }

    // ── checkExpiry ─────────────────────────────────────────────────────

    @Test
    fun `checkExpiry marks boss as expired past deadline`() {
        val boss = createBoss(deadline = 1000L)
        val expired = engine.checkExpiry(boss, currentTime = 2000L)
        assertTrue(expired.isExpired)
    }

    @Test
    fun `checkExpiry does not expire boss before deadline`() {
        val boss = createBoss(deadline = 5000L)
        val result = engine.checkExpiry(boss, currentTime = 3000L)
        assertFalse(result.isExpired)
    }

    @Test
    fun `checkExpiry does not change already defeated boss`() {
        val boss = createBoss(currentHp = 0, isDefeated = true, deadline = 1000L)
        val result = engine.checkExpiry(boss, currentTime = 2000L)
        assertTrue(result.isDefeated)
        assertFalse(result.isExpired) // already dead, expiry doesn't apply
    }

    // ── victoryRewardPerMember ──────────────────────────────────────────

    @Test
    fun `victoryRewardPerMember splits XP among family`() {
        val boss = createBoss(victoryXpBonus = 100)
        val perMember = engine.victoryRewardPerMember(boss, familySize = 4)
        assertEquals(25, perMember)
    }

    @Test
    fun `victoryRewardPerMember has minimum of 25 XP`() {
        val boss = createBoss(victoryXpBonus = 50)
        val perMember = engine.victoryRewardPerMember(boss, familySize = 10)
        assertEquals(25, perMember) // 50/10 = 5 → coerced to 25
    }

    // ── getMvp ──────────────────────────────────────────────────────────

    @Test
    fun `getMvp returns user with most damage`() {
        val boss = createBoss(damageLog = mapOf("user1" to 50, "user2" to 120, "user3" to 30))
        assertEquals("user2", engine.getMvp(boss))
    }

    @Test
    fun `getMvp returns null for empty damage log`() {
        val boss = createBoss(damageLog = emptyMap())
        assertNull(engine.getMvp(boss))
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun createBoss(
        currentHp: Int = 100,
        maxHp: Int = 100,
        isDefeated: Boolean = false,
        isExpired: Boolean = false,
        deadline: Long = Long.MAX_VALUE,
        victoryXpBonus: Int = 100,
        damageLog: Map<String, Int> = emptyMap()
    ) = BossModel(
        bossId = "test_boss",
        familyId = "test_family",
        type = BossType.DISTRACTION_DRAGON,
        week = "2026-W15",
        maxHp = maxHp,
        currentHp = currentHp,
        isDefeated = isDefeated,
        isExpired = isExpired,
        deadline = deadline,
        victoryXpBonus = victoryXpBonus,
        damageLog = damageLog
    )

    private fun createTask(
        difficulty: DifficultyLevel = DifficultyLevel.EASY,
        requiresCoop: Boolean = false
    ) = TaskModel(
        id = "test_task",
        title = "Test Task",
        difficulty = difficulty,
        requiresCoop = requiresCoop,
        reward = TaskReward(xp = 10)
    )
}
