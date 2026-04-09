package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class TimedEventModelTest {

    // ── EventType enum ──────────────────────────────────────────────

    @Test
    fun `EventType has 4 entries`() {
        assertEquals(4, EventType.entries.size)
    }

    @Test
    fun `all event types have non-empty displayName`() {
        EventType.entries.forEach {
            assertTrue("${it.name} displayName", it.displayName.isNotEmpty())
        }
    }

    @Test
    fun `all event types have non-empty emoji`() {
        EventType.entries.forEach {
            assertTrue("${it.name} emoji", it.emoji.isNotEmpty())
        }
    }

    // ── EventProgress.tokensAvailable ───────────────────────────────

    @Test
    fun `tokensAvailable is earned minus spent`() {
        val progress = EventProgress(tokensEarned = 50, tokensSpent = 20)
        assertEquals(30, progress.tokensAvailable)
    }

    @Test
    fun `tokensAvailable is 0 when all spent`() {
        val progress = EventProgress(tokensEarned = 30, tokensSpent = 30)
        assertEquals(0, progress.tokensAvailable)
    }

    @Test
    fun `tokensAvailable can go negative`() {
        val progress = EventProgress(tokensEarned = 10, tokensSpent = 20)
        assertEquals(-10, progress.tokensAvailable)
    }

    // ── EventProgress defaults ──────────────────────────────────────

    @Test
    fun `default progress has 0 tasks completed`() {
        val progress = EventProgress()
        assertEquals(0, progress.tasksCompleted)
    }

    @Test
    fun `default progress is not complete`() {
        val progress = EventProgress()
        assertFalse(progress.isComplete)
    }

    // ── TimedEvent defaults ─────────────────────────────────────────

    @Test
    fun `default event type is SEASONAL`() {
        val event = TimedEvent()
        assertEquals(EventType.SEASONAL, event.type)
    }

    @Test
    fun `default targetTaskCount is 20`() {
        val event = TimedEvent()
        assertEquals(20, event.targetTaskCount)
    }

    @Test
    fun `default isActive is false`() {
        val event = TimedEvent()
        assertFalse(event.isActive)
    }

    @Test
    fun `default isGlobal is true`() {
        val event = TimedEvent()
        assertTrue(event.isGlobal)
    }

    @Test
    fun `default hasLeaderboard is true`() {
        val event = TimedEvent()
        assertTrue(event.hasLeaderboard)
    }

    @Test
    fun `default rewardXp is 200`() {
        val event = TimedEvent()
        assertEquals(200, event.rewardXp)
    }

    // ── EventShopItem defaults ──────────────────────────────────────

    @Test
    fun `shop item default type is avatar_item`() {
        val item = EventShopItem()
        assertEquals("avatar_item", item.type)
    }

    // ── SeasonalEvents predefined events ────────────────────────────

    @Test
    fun `HALLOWEEN_HUNT is a HOLIDAY event`() {
        assertEquals(EventType.HOLIDAY, SeasonalEvents.HALLOWEEN_HUNT.type)
    }

    @Test
    fun `HALLOWEEN_HUNT season is HALLOWEEN`() {
        assertEquals(Season.HALLOWEEN, SeasonalEvents.HALLOWEEN_HUNT.season)
    }

    @Test
    fun `WINTER_WONDERLAND has positive reward XP`() {
        assertTrue(SeasonalEvents.WINTER_WONDERLAND.rewardXp > 0)
    }

    @Test
    fun `SUMMER_SPRINT is a SEASONAL event`() {
        assertEquals(EventType.SEASONAL, SeasonalEvents.SUMMER_SPRINT.type)
    }

    @Test
    fun `SUMMER_SPRINT has 50 target tasks`() {
        assertEquals(50, SeasonalEvents.SUMMER_SPRINT.targetTaskCount)
    }

    @Test
    fun `all seasonal events have non-empty titles`() {
        val events = listOf(
            SeasonalEvents.HALLOWEEN_HUNT,
            SeasonalEvents.WINTER_WONDERLAND,
            SeasonalEvents.SUMMER_SPRINT
        )
        events.forEach { assertTrue(it.title.isNotEmpty()) }
    }

    @Test
    fun `all seasonal events have non-empty event token names`() {
        val events = listOf(
            SeasonalEvents.HALLOWEEN_HUNT,
            SeasonalEvents.WINTER_WONDERLAND,
            SeasonalEvents.SUMMER_SPRINT
        )
        events.forEach { assertTrue(it.eventTokenName.isNotEmpty()) }
    }
}
