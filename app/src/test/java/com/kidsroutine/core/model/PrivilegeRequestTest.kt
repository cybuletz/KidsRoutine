package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class PrivilegeRequestTest {

    // ── PrivilegeRequestStatus enum ─────────────────────────────────

    @Test
    fun `PrivilegeRequestStatus has 3 entries`() {
        assertEquals(3, PrivilegeRequestStatus.entries.size)
    }

    @Test
    fun `PrivilegeRequestStatus includes PENDING APPROVED REJECTED`() {
        val names = PrivilegeRequestStatus.entries.map { it.name }
        assertTrue(names.contains("PENDING"))
        assertTrue(names.contains("APPROVED"))
        assertTrue(names.contains("REJECTED"))
    }

    // ── PrivilegeRequest defaults ───────────────────────────────────

    @Test
    fun `default status is PENDING`() {
        val request = PrivilegeRequest()
        assertEquals(PrivilegeRequestStatus.PENDING, request.status)
    }

    @Test
    fun `default emoji is gift`() {
        val request = PrivilegeRequest()
        assertEquals("🎁", request.privilegeEmoji)
    }

    @Test
    fun `default resolvedAt is 0`() {
        val request = PrivilegeRequest()
        assertEquals(0L, request.resolvedAt)
    }

    @Test
    fun `default parentNote is empty`() {
        val request = PrivilegeRequest()
        assertEquals("", request.parentNote)
    }

    @Test
    fun `requestId is auto-generated`() {
        val req1 = PrivilegeRequest()
        val req2 = PrivilegeRequest()
        assertNotEquals(req1.requestId, req2.requestId)
    }

    @Test
    fun `request stores all fields`() {
        val request = PrivilegeRequest(
            familyId = "f1",
            childUserId = "u1",
            childName = "Alice",
            privilegeId = "extra_screen_time",
            privilegeTitle = "Extra Screen Time",
            privilegeEmoji = "📺",
            xpCost = 100,
            status = PrivilegeRequestStatus.APPROVED,
            parentNote = "Great week!"
        )
        assertEquals("f1", request.familyId)
        assertEquals("Alice", request.childName)
        assertEquals("Extra Screen Time", request.privilegeTitle)
        assertEquals(100, request.xpCost)
        assertEquals(PrivilegeRequestStatus.APPROVED, request.status)
        assertEquals("Great week!", request.parentNote)
    }
}
