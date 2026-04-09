package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class TaskInstanceTest {

    // ── defaults ────────────────────────────────────────────────────

    @Test
    fun `default instanceId is empty`() {
        assertEquals("", TaskInstance().instanceId)
    }

    @Test
    fun `default templateId is empty`() {
        assertEquals("", TaskInstance().templateId)
    }

    @Test
    fun `default task is empty TaskModel`() {
        assertEquals(TaskModel(), TaskInstance().task)
    }

    @Test
    fun `default resolvedValues is empty map`() {
        assertTrue(TaskInstance().resolvedValues.isEmpty())
    }

    @Test
    fun `default assignedDate is empty`() {
        assertEquals("", TaskInstance().assignedDate)
    }

    @Test
    fun `default userId is empty`() {
        assertEquals("", TaskInstance().userId)
    }

    @Test
    fun `default injectedByChallengeId is null`() {
        assertNull(TaskInstance().injectedByChallengeId)
    }

    @Test
    fun `default status is PENDING`() {
        assertEquals(TaskStatus.PENDING, TaskInstance().status)
    }

    @Test
    fun `default completedAt is 0`() {
        assertEquals(0L, TaskInstance().completedAt)
    }

    // ── custom values ───────────────────────────────────────────────

    @Test
    fun `stores all fields`() {
        val task = TaskModel(id = "task1", title = "Brush teeth")
        val inst = TaskInstance(
            instanceId = "inst1",
            templateId = "tmpl1",
            task = task,
            resolvedValues = mapOf("key" to "val"),
            assignedDate = "2026-04-09",
            userId = "u1",
            injectedByChallengeId = "ch1",
            status = TaskStatus.COMPLETED,
            completedAt = 12345L
        )
        assertEquals("inst1", inst.instanceId)
        assertEquals("tmpl1", inst.templateId)
        assertEquals("Brush teeth", inst.task.title)
        assertEquals("val", inst.resolvedValues["key"])
        assertEquals("2026-04-09", inst.assignedDate)
        assertEquals("u1", inst.userId)
        assertEquals("ch1", inst.injectedByChallengeId)
        assertEquals(TaskStatus.COMPLETED, inst.status)
        assertEquals(12345L, inst.completedAt)
    }

    @Test
    fun `injectedByChallengeId null means regular task`() {
        val inst = TaskInstance(injectedByChallengeId = null)
        assertNull(inst.injectedByChallengeId)
    }

    @Test
    fun `injectedByChallengeId non-null means challenge task`() {
        val inst = TaskInstance(injectedByChallengeId = "ch42")
        assertEquals("ch42", inst.injectedByChallengeId)
    }

    // ── equality / copy ─────────────────────────────────────────────

    @Test
    fun `data class equality`() {
        val a = TaskInstance(instanceId = "i1", status = TaskStatus.PENDING)
        val b = TaskInstance(instanceId = "i1", status = TaskStatus.PENDING)
        assertEquals(a, b)
    }

    @Test
    fun `copy updates status`() {
        val orig = TaskInstance(instanceId = "i1", status = TaskStatus.PENDING)
        val completed = orig.copy(status = TaskStatus.COMPLETED, completedAt = 999L)
        assertEquals("i1", completed.instanceId)
        assertEquals(TaskStatus.COMPLETED, completed.status)
        assertEquals(999L, completed.completedAt)
    }
}
