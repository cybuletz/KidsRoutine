package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class FamilyModelTest {

    // ── defaults ────────────────────────────────────────────────────

    @Test
    fun `default familyId is empty`() {
        assertEquals("", FamilyModel().familyId)
    }

    @Test
    fun `default familyName is empty`() {
        assertEquals("", FamilyModel().familyName)
    }

    @Test
    fun `default memberIds is empty`() {
        assertTrue(FamilyModel().memberIds.isEmpty())
    }

    @Test
    fun `default familyXp is 0`() {
        assertEquals(0, FamilyModel().familyXp)
    }

    @Test
    fun `default familyStreak is 0`() {
        assertEquals(0, FamilyModel().familyStreak)
    }

    @Test
    fun `default sharedChallengeIds is empty`() {
        assertTrue(FamilyModel().sharedChallengeIds.isEmpty())
    }

    @Test
    fun `default inviteCode is empty`() {
        assertEquals("", FamilyModel().inviteCode)
    }

    // ── custom values ───────────────────────────────────────────────

    @Test
    fun `stores custom familyId`() {
        assertEquals("fam123", FamilyModel(familyId = "fam123").familyId)
    }

    @Test
    fun `stores memberIds list`() {
        val m = FamilyModel(memberIds = listOf("a", "b", "c"))
        assertEquals(3, m.memberIds.size)
        assertEquals("b", m.memberIds[1])
    }

    @Test
    fun `stores sharedChallengeIds`() {
        val m = FamilyModel(sharedChallengeIds = listOf("ch1", "ch2"))
        assertEquals(2, m.sharedChallengeIds.size)
    }

    @Test
    fun `stores familyXp`() {
        assertEquals(5000, FamilyModel(familyXp = 5000).familyXp)
    }

    // ── equality ────────────────────────────────────────────────────

    @Test
    fun `data class equality`() {
        val a = FamilyModel(familyId = "f1", familyName = "The Smiths")
        val b = FamilyModel(familyId = "f1", familyName = "The Smiths")
        assertEquals(a, b)
    }

    @Test
    fun `data class inequality on different id`() {
        val a = FamilyModel(familyId = "f1")
        val b = FamilyModel(familyId = "f2")
        assertNotEquals(a, b)
    }

    // ── copy ────────────────────────────────────────────────────────

    @Test
    fun `copy updates selected fields`() {
        val orig = FamilyModel(familyId = "f1", familyXp = 100)
        val updated = orig.copy(familyXp = 200, familyStreak = 3)
        assertEquals("f1", updated.familyId)
        assertEquals(200, updated.familyXp)
        assertEquals(3, updated.familyStreak)
    }
}
