package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class ChallengeModelTest {

    // ── ChallengeType enum ──────────────────────────────────────────

    @Test
    fun `ChallengeType has 4 entries`() {
        assertEquals(4, ChallengeType.entries.size)
    }

    // ── ChallengeFrequency enum ─────────────────────────────────────

    @Test
    fun `ChallengeFrequency has 2 entries`() {
        assertEquals(2, ChallengeFrequency.entries.size)
    }

    // ── ChallengeStatus enum ────────────────────────────────────────

    @Test
    fun `ChallengeStatus has 5 entries`() {
        assertEquals(5, ChallengeStatus.entries.size)
    }

    @Test
    fun `ChallengeStatus includes ACTIVE COMPLETED FAILED PAUSED ARCHIVED`() {
        val statuses = ChallengeStatus.entries.map { it.name }
        assertTrue(statuses.contains("ACTIVE"))
        assertTrue(statuses.contains("COMPLETED"))
        assertTrue(statuses.contains("FAILED"))
        assertTrue(statuses.contains("PAUSED"))
        assertTrue(statuses.contains("ARCHIVED"))
    }

    // ── ChallengeProgress defaults ──────────────────────────────────

    @Test
    fun `default progress starts at day 1`() {
        val progress = ChallengeProgress()
        assertEquals(1, progress.currentDay)
    }

    @Test
    fun `default progress has 0 completed days`() {
        val progress = ChallengeProgress()
        assertEquals(0, progress.completedDays)
    }

    @Test
    fun `default progress status is ACTIVE`() {
        val progress = ChallengeProgress()
        assertEquals(ChallengeStatus.ACTIVE, progress.status)
    }

    @Test
    fun `default progress has 0 successRate`() {
        val progress = ChallengeProgress()
        assertEquals(0f, progress.successRate, 0.01f)
    }

    @Test
    fun `default progress dailyProgress is empty`() {
        val progress = ChallengeProgress()
        assertTrue(progress.dailyProgress.isEmpty())
    }

    // ── ChallengeModel defaults ─────────────────────────────────────

    @Test
    fun `default challenge type is DAILY_HABIT`() {
        val model = ChallengeModel()
        assertEquals(ChallengeType.DAILY_HABIT, model.type)
    }

    @Test
    fun `default challenge duration is 7 days`() {
        val model = ChallengeModel()
        assertEquals(7, model.duration)
    }

    @Test
    fun `default frequency is DAILY`() {
        val model = ChallengeModel()
        assertEquals(ChallengeFrequency.DAILY, model.frequency)
    }

    @Test
    fun `default targetDaysPerWeek is 7`() {
        val model = ChallengeModel()
        assertEquals(7, model.targetDaysPerWeek)
    }

    @Test
    fun `default dailyXpReward is 10`() {
        val model = ChallengeModel()
        assertEquals(10, model.dailyXpReward)
    }

    @Test
    fun `default completionBonusXp is 50`() {
        val model = ChallengeModel()
        assertEquals(50, model.completionBonusXp)
    }

    @Test
    fun `default streakBonusXp is 5`() {
        val model = ChallengeModel()
        assertEquals(5, model.streakBonusXp)
    }

    @Test
    fun `default validationType is SELF`() {
        val model = ChallengeModel()
        assertEquals(ValidationType.SELF, model.validationType)
    }

    @Test
    fun `default createdBy is SYSTEM`() {
        val model = ChallengeModel()
        assertEquals(TaskCreator.SYSTEM, model.createdBy)
    }

    @Test
    fun `default isActive is true`() {
        val model = ChallengeModel()
        assertTrue(model.isActive)
    }

    @Test
    fun `default isCoOp is false`() {
        val model = ChallengeModel()
        assertFalse(model.isCoOp)
    }

    // ── ChallengeSuccessCondition ───────────────────────────────────

    @Test
    fun `success condition stores type and value`() {
        val cond = ChallengeSuccessCondition("COUNT", 3, "count")
        assertEquals("COUNT", cond.type)
        assertEquals(3, cond.value)
        assertEquals("count", cond.unit)
    }

    @Test
    fun `default success condition unit is empty`() {
        val cond = ChallengeSuccessCondition("BOOLEAN", true)
        assertEquals("", cond.unit)
    }
}
