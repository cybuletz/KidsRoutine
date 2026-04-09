package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class TaskProgressModelTest {

    // ── TaskStatus enum ─────────────────────────────────────────────

    @Test
    fun `TaskStatus has 3 entries`() {
        assertEquals(3, TaskStatus.entries.size)
    }

    @Test
    fun `TaskStatus includes PENDING COMPLETED FAILED`() {
        val names = TaskStatus.entries.map { it.name }
        assertTrue(names.contains("PENDING"))
        assertTrue(names.contains("COMPLETED"))
        assertTrue(names.contains("FAILED"))
    }

    // ── ValidationStatus enum ───────────────────────────────────────

    @Test
    fun `ValidationStatus has 3 entries`() {
        assertEquals(3, ValidationStatus.entries.size)
    }

    @Test
    fun `ValidationStatus includes PENDING APPROVED REJECTED`() {
        val names = ValidationStatus.entries.map { it.name }
        assertTrue(names.contains("PENDING"))
        assertTrue(names.contains("APPROVED"))
        assertTrue(names.contains("REJECTED"))
    }

    // ── TaskProgressModel defaults ──────────────────────────────────

    @Test
    fun `default status is PENDING`() {
        val progress = TaskProgressModel()
        assertEquals(TaskStatus.PENDING, progress.status)
    }

    @Test
    fun `default validationStatus is PENDING`() {
        val progress = TaskProgressModel()
        assertEquals(ValidationStatus.PENDING, progress.validationStatus)
    }

    @Test
    fun `default completionTime is null`() {
        val progress = TaskProgressModel()
        assertNull(progress.completionTime)
    }

    @Test
    fun `default photoUrl is null`() {
        val progress = TaskProgressModel()
        assertNull(progress.photoUrl)
    }

    @Test
    fun `default syncedToFirestore is false`() {
        val progress = TaskProgressModel()
        assertFalse(progress.syncedToFirestore)
    }

    @Test
    fun `progress stores all fields`() {
        val progress = TaskProgressModel(
            taskInstanceId = "ti1",
            userId = "u1",
            familyId = "f1",
            date = "2026-04-09",
            status = TaskStatus.COMPLETED,
            completionTime = 12345L,
            validationStatus = ValidationStatus.APPROVED,
            taskTitle = "Brush teeth",
            syncedToFirestore = true
        )
        assertEquals("ti1", progress.taskInstanceId)
        assertEquals(TaskStatus.COMPLETED, progress.status)
        assertEquals(12345L, progress.completionTime)
        assertEquals(ValidationStatus.APPROVED, progress.validationStatus)
        assertTrue(progress.syncedToFirestore)
    }

    // ── TaskInstance defaults ────────────────────────────────────────

    @Test
    fun `TaskInstance default status is PENDING`() {
        val instance = TaskInstance()
        assertEquals(TaskStatus.PENDING, instance.status)
    }

    @Test
    fun `TaskInstance default injectedByChallengeId is null`() {
        val instance = TaskInstance()
        assertNull(instance.injectedByChallengeId)
    }

    @Test
    fun `TaskInstance stores data`() {
        val instance = TaskInstance(
            instanceId = "inst1",
            templateId = "t1",
            assignedDate = "2026-04-09",
            userId = "u1",
            injectedByChallengeId = "ch1",
            status = TaskStatus.COMPLETED,
            completedAt = 99999L
        )
        assertEquals("inst1", instance.instanceId)
        assertEquals("ch1", instance.injectedByChallengeId)
        assertEquals(TaskStatus.COMPLETED, instance.status)
        assertEquals(99999L, instance.completedAt)
    }
}
