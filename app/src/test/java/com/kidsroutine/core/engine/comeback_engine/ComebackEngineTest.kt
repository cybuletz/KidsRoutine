package com.kidsroutine.core.engine.comeback_engine

import com.kidsroutine.core.model.ComebackChallengeType
import com.kidsroutine.core.model.ComebackState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ComebackEngineTest {

    private lateinit var engine: ComebackEngine

    @Before
    fun setUp() {
        engine = ComebackEngine()
    }

    // ── checkForComeback ────────────────────────────────────────────

    @Test
    fun `no comeback for blank lastActiveDate`() {
        assertNull(engine.checkForComeback("user1", "", "2026-04-09", 5))
    }

    @Test
    fun `no comeback for 1 day absence`() {
        assertNull(engine.checkForComeback("user1", "2026-04-08", "2026-04-09", 5))
    }

    @Test
    fun `no comeback for 2 day absence`() {
        assertNull(engine.checkForComeback("user1", "2026-04-07", "2026-04-09", 5))
    }

    @Test
    fun `comeback triggered for 3 day absence`() {
        val state = engine.checkForComeback("user1", "2026-04-06", "2026-04-09", 5)
        assertNotNull(state)
        assertEquals(3, state!!.daysAbsent)
        assertEquals(ComebackChallengeType.QUICK_RETURN, state.challenge)
        assertTrue(state.isActive)
    }

    @Test
    fun `comeback uses COMEBACK_KID for 5 day absence`() {
        val state = engine.checkForComeback("user1", "2026-04-04", "2026-04-09", 10)
        assertNotNull(state)
        assertEquals(5, state!!.daysAbsent)
        assertEquals(ComebackChallengeType.COMEBACK_KID, state.challenge)
        assertTrue(state.freeShieldGranted) // daysAbsent <= 5
    }

    @Test
    fun `comeback uses FULL_RECOVERY for 10 day absence`() {
        val state = engine.checkForComeback("user1", "2026-03-30", "2026-04-09", 20)
        assertNotNull(state)
        assertEquals(ComebackChallengeType.FULL_RECOVERY, state!!.challenge)
        assertFalse(state.freeShieldGranted) // daysAbsent > 5
    }

    @Test
    fun `comeback preserves previousStreak`() {
        val state = engine.checkForComeback("user1", "2026-04-06", "2026-04-09", 15)
        assertEquals(15, state!!.previousStreak)
    }

    // ── recordTaskCompletion ────────────────────────────────────────

    @Test
    fun `recordTaskCompletion increments tasksCompletedToday`() {
        val state = ComebackState(
            userId = "user1",
            isActive = true,
            challenge = ComebackChallengeType.QUICK_RETURN,
            tasksCompletedToday = 0
        )
        val updated = engine.recordTaskCompletion(state)
        assertEquals(1, updated.tasksCompletedToday)
    }

    @Test
    fun `recordTaskCompletion sets streakRecovery when challenge complete`() {
        val state = ComebackState(
            userId = "user1",
            isActive = true,
            challenge = ComebackChallengeType.QUICK_RETURN, // needs 1 task
            tasksCompletedToday = 0,
            previousStreak = 10
        )
        val updated = engine.recordTaskCompletion(state)
        assertTrue(updated.isChallengeComplete)
        assertTrue(updated.streakRecovered > 0)
    }

    @Test
    fun `recordTaskCompletion does nothing to inactive state`() {
        val state = ComebackState(userId = "user1", isActive = false)
        val updated = engine.recordTaskCompletion(state)
        assertEquals(0, updated.tasksCompletedToday)
    }

    // ── recoveredStreak ─────────────────────────────────────────────

    @Test
    fun `recoveredStreak returns at least 1 when challenge complete`() {
        val state = ComebackState(
            userId = "user1",
            challenge = ComebackChallengeType.QUICK_RETURN,
            tasksCompletedToday = 1,
            previousStreak = 10
        )
        val recovered = engine.recoveredStreak(state)
        assertTrue(recovered >= 1)
    }

    @Test
    fun `recoveredStreak returns 1 when challenge not complete`() {
        val state = ComebackState(
            userId = "user1",
            challenge = ComebackChallengeType.COMEBACK_KID,
            tasksCompletedToday = 0,
            previousStreak = 20
        )
        assertEquals(1, engine.recoveredStreak(state))
    }

    // ── generateMissedSummary ───────────────────────────────────────

    @Test
    fun `missedSummary starts with short absence message for few days`() {
        val summary = engine.generateMissedSummary(5, null, emptyList())
        assertTrue(summary[0].contains("5 days"))
    }

    @Test
    fun `missedSummary uses 'a lot has happened' for long absence`() {
        val summary = engine.generateMissedSummary(10, null, emptyList())
        assertTrue(summary[0].contains("a lot has happened"))
    }

    @Test
    fun `missedSummary includes pet message when pet exists`() {
        val summary = engine.generateMissedSummary(5, "Rex", emptyList())
        assertTrue(summary.any { it.contains("Rex") })
    }

    @Test
    fun `missedSummary skips pet message when no pet`() {
        val summary = engine.generateMissedSummary(5, null, emptyList())
        assertFalse(summary.any { it.contains("kept them company") })
    }

    @Test
    fun `missedSummary includes up to 3 friend updates`() {
        val friends = listOf("Alice leveled up!", "Bob got Gold!", "Chris won!", "Dave completed!")
        val summary = engine.generateMissedSummary(5, null, friends)
        // Should include first 3, not 4th
        assertTrue(summary.any { it.contains("Alice") })
        assertTrue(summary.any { it.contains("Chris") })
        assertFalse(summary.any { it.contains("Dave") })
    }

    @Test
    fun `missedSummary always ends with comeback encouragement`() {
        val summary = engine.generateMissedSummary(5, null, emptyList())
        assertTrue(summary.last().contains("comeback"))
    }
}
