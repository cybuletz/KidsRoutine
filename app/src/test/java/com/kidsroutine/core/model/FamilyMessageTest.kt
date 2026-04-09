package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class FamilyMessageTest {

    // ── MessageType enum ────────────────────────────────────────────

    @Test
    fun `MessageType has 5 entries`() {
        assertEquals(5, MessageType.entries.size)
    }

    @Test
    fun `MessageType includes all expected types`() {
        val names = MessageType.entries.map { it.name }
        assertTrue(names.contains("TEXT"))
        assertTrue(names.contains("TASK_REMINDER"))
        assertTrue(names.contains("ACHIEVEMENT_CELEBRATION"))
        assertTrue(names.contains("COMPLETION_CONFIRMATION"))
        assertTrue(names.contains("ENCOURAGEMENT"))
    }

    // ── FamilyMessage defaults ──────────────────────────────────────

    @Test
    fun `default type is TEXT`() {
        val msg = FamilyMessage()
        assertEquals(MessageType.TEXT, msg.type)
    }

    @Test
    fun `default isRead is false`() {
        val msg = FamilyMessage()
        assertFalse(msg.isRead)
    }

    @Test
    fun `default relatedTaskId is null`() {
        val msg = FamilyMessage()
        assertNull(msg.relatedTaskId)
    }

    @Test
    fun `default relatedTaskTitle is null`() {
        val msg = FamilyMessage()
        assertNull(msg.relatedTaskTitle)
    }

    @Test
    fun `message ID is auto-generated`() {
        val msg1 = FamilyMessage()
        val msg2 = FamilyMessage()
        assertTrue(msg1.id.startsWith("msg_"))
        assertTrue(msg2.id.startsWith("msg_"))
    }

    @Test
    fun `message stores all fields`() {
        val msg = FamilyMessage(
            familyId = "f1",
            senderId = "u1",
            senderName = "Alice",
            content = "Great job!",
            type = MessageType.ENCOURAGEMENT,
            relatedTaskId = "t1",
            relatedTaskTitle = "Brush teeth"
        )
        assertEquals("f1", msg.familyId)
        assertEquals("Alice", msg.senderName)
        assertEquals("Great job!", msg.content)
        assertEquals(MessageType.ENCOURAGEMENT, msg.type)
        assertEquals("t1", msg.relatedTaskId)
    }
}
