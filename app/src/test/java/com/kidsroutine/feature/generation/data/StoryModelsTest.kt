package com.kidsroutine.feature.generation.data

import org.junit.Assert.*
import org.junit.Test
import com.kidsroutine.core.model.StoryArc

class StoryModelsTest {

    // ── GeneratedStoryResponse defaults ─────────────────────────────

    @Test
    fun `success response with arc`() {
        val arc = StoryArc(arcId = "arc1", arcTitle = "The Dragon Quest")
        val response = GeneratedStoryResponse(
            success = true,
            arc = arc,
            cached = false,
            quotaRemaining = 5
        )
        assertTrue(response.success)
        assertNotNull(response.arc)
        assertEquals("The Dragon Quest", response.arc!!.arcTitle)
        assertFalse(response.cached)
        assertEquals(5, response.quotaRemaining)
    }

    @Test
    fun `failed response has null arc`() {
        val response = GeneratedStoryResponse(
            success = false,
            arc = null,
            cached = false,
            quotaRemaining = 0
        )
        assertFalse(response.success)
        assertNull(response.arc)
        assertEquals(0, response.quotaRemaining)
    }

    @Test
    fun `cached response is flagged`() {
        val response = GeneratedStoryResponse(
            success = true,
            arc = StoryArc(),
            cached = true,
            quotaRemaining = 3
        )
        assertTrue(response.cached)
    }

    @Test
    fun `quotaRemaining can be negative`() {
        val response = GeneratedStoryResponse(
            success = false,
            arc = null,
            cached = false,
            quotaRemaining = -1
        )
        assertEquals(-1, response.quotaRemaining)
    }

    // ── data class behavior ─────────────────────────────────────────

    @Test
    fun `same data equals`() {
        val arc = StoryArc(arcId = "arc1")
        val a = GeneratedStoryResponse(true, arc, false, 5)
        val b = GeneratedStoryResponse(true, arc, false, 5)
        assertEquals(a, b)
    }

    @Test
    fun `different data not equals`() {
        val a = GeneratedStoryResponse(true, null, false, 5)
        val b = GeneratedStoryResponse(false, null, false, 5)
        assertNotEquals(a, b)
    }

    @Test
    fun `copy changes only specified field`() {
        val original = GeneratedStoryResponse(true, null, false, 5)
        val copy = original.copy(quotaRemaining = 3)
        assertEquals(3, copy.quotaRemaining)
        assertTrue(copy.success)
    }
}
