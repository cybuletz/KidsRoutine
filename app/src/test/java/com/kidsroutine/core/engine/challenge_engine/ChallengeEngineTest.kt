package com.kidsroutine.core.engine.challenge_engine

import com.kidsroutine.core.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ChallengeEngineTest {

    private lateinit var engine: ChallengeEngine

    @Before
    fun setUp() {
        engine = ChallengeEngine()
    }

    private fun createChallenge(
        duration: Int = 7,
        frequency: ChallengeFrequency = ChallengeFrequency.DAILY,
        dailyXpReward: Int = 10,
        completionBonusXp: Int = 50,
        streakBonusXp: Int = 5,
        validationType: ValidationType = ValidationType.SELF,
        familyId: String = "fam1"
    ) = ChallengeModel(
        challengeId = "challenge1",
        title = "Test Challenge",
        duration = duration,
        frequency = frequency,
        dailyXpReward = dailyXpReward,
        completionBonusXp = completionBonusXp,
        streakBonusXp = streakBonusXp,
        validationType = validationType,
        familyId = familyId,
        dailyTaskTemplate = TaskModel(id = "tmpl1", title = "Do the thing")
    )

    private fun createProgress(
        currentDay: Int = 1,
        totalDays: Int = 7,
        completedDays: Int = 0,
        currentStreak: Int = 0,
        dailyProgress: Map<String, Boolean> = emptyMap(),
        status: ChallengeStatus = ChallengeStatus.ACTIVE
    ) = ChallengeProgress(
        challengeId = "challenge1",
        userId = "user1",
        currentDay = currentDay,
        totalDays = totalDays,
        completedDays = completedDays,
        currentStreak = currentStreak,
        dailyProgress = dailyProgress,
        status = status,
        startDate = "2026-04-01"
    )

    // ── startChallenge ──────────────────────────────────────────────

    @Test
    fun `startChallenge creates progress at day 1`() {
        val challenge = createChallenge(duration = 14)
        val progress = engine.startChallenge(challenge, "user1", "2026-04-09")
        assertEquals(1, progress.currentDay)
        assertEquals(14, progress.totalDays)
        assertEquals(0, progress.completedDays)
        assertEquals(ChallengeStatus.ACTIVE, progress.status)
        assertEquals("user1", progress.userId)
    }

    @Test
    fun `startChallenge sets correct start and end dates`() {
        val challenge = createChallenge(duration = 7)
        val progress = engine.startChallenge(challenge, "user1", "2026-04-09")
        assertEquals("2026-04-09", progress.startDate)
        assertEquals("2026-04-15", progress.endDate)
    }

    // ── recordDailyProgress ─────────────────────────────────────────

    @Test
    fun `recording a completed day increments completedDays`() {
        val challenge = createChallenge()
        val progress = createProgress()
        val (updated, status) = engine.recordDailyProgress(challenge, progress, completed = true, date = "2026-04-01")
        assertEquals(1, updated.completedDays)
        assertEquals(1, updated.currentStreak)
        assertEquals(ChallengeStatus.ACTIVE, status)
    }

    @Test
    fun `recording a failed day resets streak to 0`() {
        val progress = createProgress(currentStreak = 3)
        val (updated, _) = engine.recordDailyProgress(createChallenge(), progress, completed = false, date = "2026-04-01")
        assertEquals(0, updated.currentStreak)
    }

    @Test
    fun `completing all days marks challenge as COMPLETED`() {
        val challenge = createChallenge(duration = 3)
        val progress = createProgress(
            totalDays = 3,
            completedDays = 2,
            dailyProgress = mapOf("2026-04-01" to true, "2026-04-02" to true)
        )
        val (_, status) = engine.recordDailyProgress(challenge, progress, completed = true, date = "2026-04-03")
        assertEquals(ChallengeStatus.COMPLETED, status)
    }

    @Test
    fun `missing 2 consecutive daily days fails challenge`() {
        val challenge = createChallenge(frequency = ChallengeFrequency.DAILY)
        val progress = createProgress(
            dailyProgress = mapOf("2026-04-07" to false) // 1 missed day already
        )
        val (_, status) = engine.recordDailyProgress(
            challenge, progress, completed = false, date = "2026-04-08"
        )
        assertEquals(ChallengeStatus.FAILED, status)
    }

    @Test
    fun `currentDay increments after each recording`() {
        val progress = createProgress(currentDay = 3)
        val (updated, _) = engine.recordDailyProgress(createChallenge(), progress, completed = true, date = "2026-04-03")
        assertEquals(4, updated.currentDay)
    }

    @Test
    fun `lastCompletedDate updates when completed`() {
        val progress = createProgress()
        val (updated, _) = engine.recordDailyProgress(createChallenge(), progress, completed = true, date = "2026-04-05")
        assertEquals("2026-04-05", updated.lastCompletedDate)
    }

    @Test
    fun `lastCompletedDate stays same when not completed`() {
        val progress = createProgress().copy(lastCompletedDate = "2026-04-04")
        val (updated, _) = engine.recordDailyProgress(createChallenge(), progress, completed = false, date = "2026-04-05")
        assertEquals("2026-04-04", updated.lastCompletedDate)
    }

    // ── calculateDailyXp ────────────────────────────────────────────

    @Test
    fun `calculateDailyXp returns base reward without streak bonus`() {
        val challenge = createChallenge(dailyXpReward = 15)
        assertEquals(15, engine.calculateDailyXp(challenge, createProgress()))
    }

    @Test
    fun `calculateDailyXp adds streak bonus`() {
        val challenge = createChallenge(dailyXpReward = 10, streakBonusXp = 5)
        val progress = createProgress(currentStreak = 3)
        // 10 + (5 * 3) = 25
        assertEquals(25, engine.calculateDailyXp(challenge, progress, streakBonus = true))
    }

    @Test
    fun `calculateDailyXp no streak bonus when streakBonus flag is false`() {
        val challenge = createChallenge(dailyXpReward = 10, streakBonusXp = 5)
        val progress = createProgress(currentStreak = 3)
        assertEquals(10, engine.calculateDailyXp(challenge, progress, streakBonus = false))
    }

    // ── calculateCompletionBonus ────────────────────────────────────

    @Test
    fun `calculateCompletionBonus returns challenge bonus`() {
        val challenge = createChallenge(completionBonusXp = 100)
        assertEquals(100, engine.calculateCompletionBonus(challenge))
    }

    // ── generateDailyTask ───────────────────────────────────────────

    @Test
    fun `generateDailyTask returns task for active challenge`() {
        val challenge = createChallenge()
        val progress = createProgress(currentDay = 3, totalDays = 7, currentStreak = 2)
        val task = engine.generateDailyTask(challenge, progress, "2026-04-03")
        assertNotNull(task)
        assertTrue(task!!.title.contains("Day 3/7"))
        assertTrue(task.description.contains("Streak: 2"))
    }

    @Test
    fun `generateDailyTask returns null for non-active challenge`() {
        val challenge = createChallenge()
        val progress = createProgress(status = ChallengeStatus.COMPLETED)
        assertNull(engine.generateDailyTask(challenge, progress, "2026-04-03"))
    }

    @Test
    fun `generateDailyTask returns null for failed challenge`() {
        val challenge = createChallenge()
        val progress = createProgress(status = ChallengeStatus.FAILED)
        assertNull(engine.generateDailyTask(challenge, progress, "2026-04-03"))
    }

    @Test
    fun `generateDailyTask sets correct ID format`() {
        val challenge = createChallenge()
        val progress = createProgress()
        val task = engine.generateDailyTask(challenge, progress, "2026-04-03")
        assertEquals("challenge_challenge1_2026-04-03", task?.id)
    }

    @Test
    fun `generateDailyTask copies challenge properties`() {
        val challenge = createChallenge(
            validationType = ValidationType.PARENT_REQUIRED,
            familyId = "family123"
        )
        val progress = createProgress()
        val task = engine.generateDailyTask(challenge, progress, "2026-04-03")
        assertNotNull(task)
        assertTrue(task!!.requiresParent)
        assertEquals("family123", task.familyId)
    }
}
