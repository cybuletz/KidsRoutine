package com.kidsroutine.feature.execution.domain

import org.junit.Assert.*
import org.junit.Test

class CompletionResultTest {

    // ── Success ─────────────────────────────────────────────────────

    @Test
    fun `Success stores xpGained`() {
        val result = CompletionResult.Success(xpGained = 25, newStreak = 5, needsParent = false)
        assertEquals(25, result.xpGained)
    }

    @Test
    fun `Success stores newStreak`() {
        val result = CompletionResult.Success(xpGained = 10, newStreak = 12, needsParent = false)
        assertEquals(12, result.newStreak)
    }

    @Test
    fun `Success stores needsParent`() {
        val result = CompletionResult.Success(xpGained = 10, newStreak = 1, needsParent = true)
        assertTrue(result.needsParent)
    }

    @Test
    fun `Success default celebrationMessage is empty`() {
        val result = CompletionResult.Success(xpGained = 10, newStreak = 1, needsParent = false)
        assertEquals("", result.celebrationMessage)
    }

    @Test
    fun `Success stores custom celebrationMessage`() {
        val result = CompletionResult.Success(
            xpGained = 10,
            newStreak = 1,
            needsParent = false,
            celebrationMessage = "🎉 Great job!"
        )
        assertEquals("🎉 Great job!", result.celebrationMessage)
    }

    @Test
    fun `Success is CompletionResult`() {
        val result: CompletionResult = CompletionResult.Success(
            xpGained = 10, newStreak = 1, needsParent = false
        )
        assertTrue(result is CompletionResult.Success)
    }

    // ── Rejected ────────────────────────────────────────────────────

    @Test
    fun `Rejected stores reason`() {
        val result = CompletionResult.Rejected("Photo required")
        assertEquals("Photo required", result.reason)
    }

    @Test
    fun `Rejected is CompletionResult`() {
        val result: CompletionResult = CompletionResult.Rejected("not valid")
        assertTrue(result is CompletionResult.Rejected)
    }

    @Test
    fun `Rejected with empty reason`() {
        val result = CompletionResult.Rejected("")
        assertEquals("", result.reason)
    }

    // ── when exhaustiveness ─────────────────────────────────────────

    @Test
    fun `when covers all branches`() {
        val results = listOf(
            CompletionResult.Success(xpGained = 10, newStreak = 1, needsParent = false),
            CompletionResult.Rejected("reason")
        )
        for (result in results) {
            val label = when (result) {
                is CompletionResult.Success -> "success:${result.xpGained}"
                is CompletionResult.Rejected -> "rejected:${result.reason}"
            }
            assertTrue(label.isNotEmpty())
        }
    }

    // ── data class behavior ─────────────────────────────────────────

    @Test
    fun `Success equality`() {
        val a = CompletionResult.Success(10, 5, false, "msg")
        val b = CompletionResult.Success(10, 5, false, "msg")
        assertEquals(a, b)
    }

    @Test
    fun `Success inequality on xp`() {
        val a = CompletionResult.Success(10, 5, false)
        val b = CompletionResult.Success(20, 5, false)
        assertNotEquals(a, b)
    }

    @Test
    fun `Rejected equality`() {
        val a = CompletionResult.Rejected("reason")
        val b = CompletionResult.Rejected("reason")
        assertEquals(a, b)
    }

    @Test
    fun `Rejected inequality`() {
        val a = CompletionResult.Rejected("reason1")
        val b = CompletionResult.Rejected("reason2")
        assertNotEquals(a, b)
    }
}
