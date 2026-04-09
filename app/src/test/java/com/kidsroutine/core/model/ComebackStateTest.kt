package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class ComebackStateTest {

    // ── forReturn factory ───────────────────────────────────────────

    @Test
    fun `forReturn with 3 days assigns QUICK_RETURN`() {
        val state = ComebackState.forReturn("user1", daysAbsent = 3, previousStreak = 5)
        assertEquals(ComebackChallengeType.QUICK_RETURN, state.challenge)
        assertTrue(state.isActive)
        assertTrue(state.showWelcomeBack)
        assertTrue(state.freeShieldGranted)
    }

    @Test
    fun `forReturn with 5 days assigns COMEBACK_KID`() {
        val state = ComebackState.forReturn("user1", daysAbsent = 5, previousStreak = 10)
        assertEquals(ComebackChallengeType.COMEBACK_KID, state.challenge)
        assertTrue(state.freeShieldGranted) // daysAbsent <= 5
    }

    @Test
    fun `forReturn with 7 days assigns COMEBACK_KID`() {
        val state = ComebackState.forReturn("user1", daysAbsent = 7, previousStreak = 10)
        assertEquals(ComebackChallengeType.COMEBACK_KID, state.challenge)
        assertFalse(state.freeShieldGranted) // daysAbsent > 5
    }

    @Test
    fun `forReturn with 8+ days assigns FULL_RECOVERY`() {
        val state = ComebackState.forReturn("user1", daysAbsent = 8, previousStreak = 20)
        assertEquals(ComebackChallengeType.FULL_RECOVERY, state.challenge)
    }

    @Test
    fun `forReturn preserves userId and previousStreak`() {
        val state = ComebackState.forReturn("user42", daysAbsent = 3, previousStreak = 15)
        assertEquals("user42", state.userId)
        assertEquals(15, state.previousStreak)
    }

    // ── isChallengeComplete ─────────────────────────────────────────

    @Test
    fun `isChallengeComplete returns true when tasks reach required`() {
        val state = ComebackState(
            challenge = ComebackChallengeType.QUICK_RETURN, // requires 1
            tasksCompletedToday = 1
        )
        assertTrue(state.isChallengeComplete)
    }

    @Test
    fun `isChallengeComplete returns false when not enough tasks`() {
        val state = ComebackState(
            challenge = ComebackChallengeType.COMEBACK_KID, // requires 3
            tasksCompletedToday = 2
        )
        assertFalse(state.isChallengeComplete)
    }

    @Test
    fun `isChallengeComplete returns true when tasks exceed required`() {
        val state = ComebackState(
            challenge = ComebackChallengeType.QUICK_RETURN,
            tasksCompletedToday = 5
        )
        assertTrue(state.isChallengeComplete)
    }

    // ── potentialStreakRecovery ──────────────────────────────────────

    @Test
    fun `potentialStreakRecovery for QUICK_RETURN is 25 percent`() {
        val state = ComebackState(
            challenge = ComebackChallengeType.QUICK_RETURN,
            previousStreak = 20
        )
        // 20 * 0.25 = 5
        assertEquals(5, state.potentialStreakRecovery)
    }

    @Test
    fun `potentialStreakRecovery for COMEBACK_KID is 50 percent`() {
        val state = ComebackState(
            challenge = ComebackChallengeType.COMEBACK_KID,
            previousStreak = 20
        )
        // 20 * 0.50 = 10
        assertEquals(10, state.potentialStreakRecovery)
    }

    @Test
    fun `potentialStreakRecovery for FULL_RECOVERY is 75 percent`() {
        val state = ComebackState(
            challenge = ComebackChallengeType.FULL_RECOVERY,
            previousStreak = 20
        )
        // 20 * 0.75 = 15
        assertEquals(15, state.potentialStreakRecovery)
    }

    @Test
    fun `potentialStreakRecovery is at least 1`() {
        val state = ComebackState(
            challenge = ComebackChallengeType.QUICK_RETURN,
            previousStreak = 2
        )
        // 2 * 0.25 = 0.5 → 0, but coerced to 1
        assertEquals(1, state.potentialStreakRecovery)
    }

    // ── ComebackChallengeType ───────────────────────────────────────

    @Test
    fun `QUICK_RETURN requires 1 task`() {
        assertEquals(1, ComebackChallengeType.QUICK_RETURN.tasksRequired)
    }

    @Test
    fun `COMEBACK_KID requires 3 tasks`() {
        assertEquals(3, ComebackChallengeType.COMEBACK_KID.tasksRequired)
    }

    @Test
    fun `FULL_RECOVERY requires 5 tasks`() {
        assertEquals(5, ComebackChallengeType.FULL_RECOVERY.tasksRequired)
    }

    @Test
    fun `streak recovery percentages increase with difficulty`() {
        assertTrue(
            ComebackChallengeType.QUICK_RETURN.streakRecoveryPercent <
                    ComebackChallengeType.COMEBACK_KID.streakRecoveryPercent
        )
        assertTrue(
            ComebackChallengeType.COMEBACK_KID.streakRecoveryPercent <
                    ComebackChallengeType.FULL_RECOVERY.streakRecoveryPercent
        )
    }
}
