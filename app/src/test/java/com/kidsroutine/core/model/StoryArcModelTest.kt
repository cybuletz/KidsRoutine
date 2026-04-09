package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class StoryArcModelTest {

    // ── StoryChapter defaults ───────────────────────────────────────

    @Test
    fun `default chapter day is 1`() {
        val chapter = StoryChapter()
        assertEquals(1, chapter.day)
    }

    @Test
    fun `default xpReward is 50`() {
        val chapter = StoryChapter()
        assertEquals(50, chapter.xpReward)
    }

    @Test
    fun `default category is CREATIVITY`() {
        val chapter = StoryChapter()
        assertEquals("CREATIVITY", chapter.category)
    }

    @Test
    fun `default difficulty is MEDIUM`() {
        val chapter = StoryChapter()
        assertEquals("MEDIUM", chapter.difficulty)
    }

    @Test
    fun `default type is STORY`() {
        val chapter = StoryChapter()
        assertEquals("STORY", chapter.type)
    }

    // ── StoryArc defaults ───────────────────────────────────────────

    @Test
    fun `default currentDay is 1`() {
        val arc = StoryArc()
        assertEquals(1, arc.currentDay)
    }

    @Test
    fun `default isComplete is false`() {
        val arc = StoryArc()
        assertFalse(arc.isComplete)
    }

    @Test
    fun `default chapters is empty`() {
        val arc = StoryArc()
        assertTrue(arc.chapters.isEmpty())
    }

    @Test
    fun `default arcEmoji is book`() {
        val arc = StoryArc()
        assertEquals("📖", arc.arcEmoji)
    }

    // ── StoryArc with data ──────────────────────────────────────────

    @Test
    fun `arc with 3 chapters`() {
        val arc = StoryArc(
            arcId = "arc1",
            chapters = listOf(
                StoryChapter(day = 1, chapterTitle = "Ch1"),
                StoryChapter(day = 2, chapterTitle = "Ch2"),
                StoryChapter(day = 3, chapterTitle = "Ch3")
            )
        )
        assertEquals(3, arc.chapters.size)
        assertEquals("Ch1", arc.chapters[0].chapterTitle)
        assertEquals("Ch3", arc.chapters[2].chapterTitle)
    }

    // ── StoryArcResponse ────────────────────────────────────────────

    @Test
    fun `default response is not successful`() {
        val response = StoryArcResponse()
        assertFalse(response.success)
    }

    @Test
    fun `default response arc is null`() {
        val response = StoryArcResponse()
        assertNull(response.arc)
    }

    @Test
    fun `successful response has arc`() {
        val response = StoryArcResponse(
            success = true,
            arc = StoryArc(arcId = "test"),
            quotaRemaining = 5
        )
        assertTrue(response.success)
        assertNotNull(response.arc)
        assertEquals(5, response.quotaRemaining)
    }
}
