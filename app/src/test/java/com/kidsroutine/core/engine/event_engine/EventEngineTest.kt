package com.kidsroutine.core.engine.event_engine

import com.kidsroutine.core.model.EventProgress
import com.kidsroutine.core.model.TimedEvent
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EventEngineTest {

    private lateinit var engine: EventEngine

    private fun createEvent(
        targetTaskCount: Int = 20,
        targetXp: Int = 0,
        startTimestamp: Long = 1000L,
        endTimestamp: Long = 5000L,
        rewardXp: Int = 200
    ) = TimedEvent(
        eventId = "test_event",
        title = "Test Event",
        targetTaskCount = targetTaskCount,
        targetXp = targetXp,
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp,
        rewardXp = rewardXp
    )

    private fun createProgress(
        tasksCompleted: Int = 0,
        xpEarned: Int = 0,
        tokensEarned: Int = 0,
        tokensSpent: Int = 0,
        isComplete: Boolean = false
    ) = EventProgress(
        eventId = "test_event",
        userId = "user1",
        tasksCompleted = tasksCompleted,
        xpEarned = xpEarned,
        tokensEarned = tokensEarned,
        tokensSpent = tokensSpent,
        isComplete = isComplete
    )

    @Before
    fun setUp() {
        engine = EventEngine()
    }

    // ── isEventActive ───────────────────────────────────────────────

    @Test
    fun `event is active when current time is within range`() {
        val event = createEvent(startTimestamp = 1000, endTimestamp = 5000)
        assertTrue(engine.isEventActive(event, 3000))
    }

    @Test
    fun `event is active at start boundary`() {
        val event = createEvent(startTimestamp = 1000, endTimestamp = 5000)
        assertTrue(engine.isEventActive(event, 1000))
    }

    @Test
    fun `event is active at end boundary`() {
        val event = createEvent(startTimestamp = 1000, endTimestamp = 5000)
        assertTrue(engine.isEventActive(event, 5000))
    }

    @Test
    fun `event is not active before start`() {
        val event = createEvent(startTimestamp = 1000, endTimestamp = 5000)
        assertFalse(engine.isEventActive(event, 999))
    }

    @Test
    fun `event is not active after end`() {
        val event = createEvent(startTimestamp = 1000, endTimestamp = 5000)
        assertFalse(engine.isEventActive(event, 5001))
    }

    // ── recordTaskCompletion ────────────────────────────────────────

    @Test
    fun `recordTaskCompletion increments tasks and XP`() {
        val event = createEvent(targetTaskCount = 20)
        val progress = createProgress(tasksCompleted = 5, xpEarned = 100)
        val updated = engine.recordTaskCompletion(progress, event, xpEarned = 25)
        assertEquals(6, updated.tasksCompleted)
        assertEquals(125, updated.xpEarned)
    }

    @Test
    fun `recordTaskCompletion adds tokens`() {
        val event = createEvent()
        val progress = createProgress(tokensEarned = 3)
        val updated = engine.recordTaskCompletion(progress, event, xpEarned = 10, tokensPerTask = 2)
        assertEquals(5, updated.tokensEarned)
    }

    @Test
    fun `recordTaskCompletion marks complete when tasks reach target`() {
        val event = createEvent(targetTaskCount = 5)
        val progress = createProgress(tasksCompleted = 4)
        val updated = engine.recordTaskCompletion(progress, event, xpEarned = 10)
        assertTrue(updated.isComplete)
    }

    @Test
    fun `recordTaskCompletion marks complete when XP reaches target`() {
        val event = createEvent(targetXp = 100)
        val progress = createProgress(xpEarned = 90)
        val updated = engine.recordTaskCompletion(progress, event, xpEarned = 15)
        assertTrue(updated.isComplete)
    }

    @Test
    fun `recordTaskCompletion does not mark complete prematurely`() {
        val event = createEvent(targetTaskCount = 10)
        val progress = createProgress(tasksCompleted = 3)
        val updated = engine.recordTaskCompletion(progress, event, xpEarned = 10)
        assertFalse(updated.isComplete)
    }

    @Test
    fun `recordTaskCompletion sets lastActivityAt`() {
        val event = createEvent()
        val progress = createProgress()
        val updated = engine.recordTaskCompletion(progress, event, xpEarned = 10)
        assertTrue(updated.lastActivityAt > 0)
    }

    // ── spendTokens ─────────────────────────────────────────────────

    @Test
    fun `spendTokens deducts from available tokens`() {
        val progress = createProgress(tokensEarned = 10, tokensSpent = 3) // available = 7
        val updated = engine.spendTokens(progress, cost = 5)
        assertNotNull(updated)
        assertEquals(8, updated!!.tokensSpent)
        assertEquals(2, updated.tokensAvailable)
    }

    @Test
    fun `spendTokens returns null when insufficient tokens`() {
        val progress = createProgress(tokensEarned = 5, tokensSpent = 3) // available = 2
        assertNull(engine.spendTokens(progress, cost = 5))
    }

    @Test
    fun `spendTokens allows exact spend`() {
        val progress = createProgress(tokensEarned = 10, tokensSpent = 5) // available = 5
        val updated = engine.spendTokens(progress, cost = 5)
        assertNotNull(updated)
        assertEquals(0, updated!!.tokensAvailable)
    }

    // ── timeRemainingSeconds ────────────────────────────────────────

    @Test
    fun `timeRemainingSeconds calculates correctly`() {
        val event = createEvent(endTimestamp = 10_000_000L) // 10000 seconds from epoch
        assertEquals(7000, engine.timeRemainingSeconds(event, 3_000_000L)) // (10_000_000-3_000_000)/1000
    }

    @Test
    fun `timeRemainingSeconds returns 0 when event has ended`() {
        val event = createEvent(endTimestamp = 1000L)
        assertEquals(0, engine.timeRemainingSeconds(event, 2000L))
    }

    // ── completionPercent ───────────────────────────────────────────

    @Test
    fun `completionPercent based on task count`() {
        val event = createEvent(targetTaskCount = 10)
        val progress = createProgress(tasksCompleted = 5)
        assertEquals(0.5f, engine.completionPercent(progress, event), 0.01f)
    }

    @Test
    fun `completionPercent based on XP when higher`() {
        val event = createEvent(targetTaskCount = 100, targetXp = 200)
        val progress = createProgress(tasksCompleted = 10, xpEarned = 160) // tasks=10%, xp=80%
        assertEquals(0.8f, engine.completionPercent(progress, event), 0.01f)
    }

    @Test
    fun `completionPercent capped at 1`() {
        val event = createEvent(targetTaskCount = 5)
        val progress = createProgress(tasksCompleted = 10)
        assertEquals(1.0f, engine.completionPercent(progress, event), 0.01f)
    }

    @Test
    fun `completionPercent is 0 for zero targets`() {
        val event = createEvent(targetTaskCount = 0, targetXp = 0)
        val progress = createProgress()
        assertEquals(0f, engine.completionPercent(progress, event), 0.01f)
    }

    // ── eventRewardXp ───────────────────────────────────────────────

    @Test
    fun `eventRewardXp returns reward when complete`() {
        val event = createEvent(rewardXp = 500)
        val progress = createProgress(isComplete = true)
        assertEquals(500, engine.eventRewardXp(event, progress))
    }

    @Test
    fun `eventRewardXp returns 0 when not complete`() {
        val event = createEvent(rewardXp = 500)
        val progress = createProgress(isComplete = false)
        assertEquals(0, engine.eventRewardXp(event, progress))
    }
}
