package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class MomentModelTest {

    // ── defaults ────────────────────────────────────────────────────

    @Test
    fun `default momentId is empty`() {
        assertEquals("", MomentModel().momentId)
    }

    @Test
    fun `default userId is empty`() {
        assertEquals("", MomentModel().userId)
    }

    @Test
    fun `default familyId is empty`() {
        assertEquals("", MomentModel().familyId)
    }

    @Test
    fun `default title is empty`() {
        assertEquals("", MomentModel().title)
    }

    @Test
    fun `default description is empty`() {
        assertEquals("", MomentModel().description)
    }

    @Test
    fun `default emoji is camera`() {
        assertEquals("📸", MomentModel().emoji)
    }

    @Test
    fun `default photoUrl is empty`() {
        assertEquals("", MomentModel().photoUrl)
    }

    @Test
    fun `default xpAtMoment is 0`() {
        assertEquals(0, MomentModel().xpAtMoment)
    }

    @Test
    fun `default taskTitle is empty`() {
        assertEquals("", MomentModel().taskTitle)
    }

    @Test
    fun `default reactions is empty map`() {
        assertTrue(MomentModel().reactions.isEmpty())
    }

    @Test
    fun `default createdAt is nonzero`() {
        assertTrue(MomentModel().createdAt > 0)
    }

    // ── custom values ───────────────────────────────────────────────

    @Test
    fun `stores all fields`() {
        val m = MomentModel(
            momentId = "m1",
            userId = "u1",
            familyId = "f1",
            title = "First Task!",
            description = "Completed morning routine",
            emoji = "🌟",
            photoUrl = "https://photo.jpg",
            xpAtMoment = 500,
            taskTitle = "Brush teeth",
            createdAt = 1234567890L,
            reactions = mapOf("u2" to "❤️", "u3" to "🎉")
        )
        assertEquals("m1", m.momentId)
        assertEquals("u1", m.userId)
        assertEquals("f1", m.familyId)
        assertEquals("First Task!", m.title)
        assertEquals("Completed morning routine", m.description)
        assertEquals("🌟", m.emoji)
        assertEquals("https://photo.jpg", m.photoUrl)
        assertEquals(500, m.xpAtMoment)
        assertEquals("Brush teeth", m.taskTitle)
        assertEquals(1234567890L, m.createdAt)
        assertEquals(2, m.reactions.size)
        assertEquals("❤️", m.reactions["u2"])
    }

    // ── equality / copy ─────────────────────────────────────────────

    @Test
    fun `data class equality`() {
        val ts = 12345L
        val a = MomentModel(momentId = "m1", createdAt = ts)
        val b = MomentModel(momentId = "m1", createdAt = ts)
        assertEquals(a, b)
    }

    @Test
    fun `copy updates selected fields`() {
        val orig = MomentModel(momentId = "m1", title = "T1", xpAtMoment = 100, createdAt = 100L)
        val updated = orig.copy(xpAtMoment = 200)
        assertEquals("m1", updated.momentId)
        assertEquals("T1", updated.title)
        assertEquals(200, updated.xpAtMoment)
    }
}
