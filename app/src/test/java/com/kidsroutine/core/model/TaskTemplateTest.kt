package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class TaskTemplateTest {

    // ── defaults ────────────────────────────────────────────────────

    @Test
    fun `default templateId is empty`() {
        val template = TaskTemplate()
        assertEquals("", template.templateId)
    }

    @Test
    fun `default familyId is null`() {
        val template = TaskTemplate()
        assertNull(template.familyId)
    }

    @Test
    fun `default generationParams is empty map`() {
        val template = TaskTemplate()
        assertTrue(template.generationParams.isEmpty())
    }

    @Test
    fun `default baseTask is default TaskModel`() {
        val template = TaskTemplate()
        assertEquals(TaskModel(), template.baseTask)
    }

    // ── with values ─────────────────────────────────────────────────

    @Test
    fun `stores all fields`() {
        val task = TaskModel(title = "Brush teeth", category = TaskCategory.MORNING_ROUTINE)
        val template = TaskTemplate(
            templateId = "t1",
            familyId = "f1",
            generationParams = mapOf("time" to "morning", "repeat" to true),
            baseTask = task
        )
        assertEquals("t1", template.templateId)
        assertEquals("f1", template.familyId)
        assertEquals(2, template.generationParams.size)
        assertEquals("Brush teeth", template.baseTask.title)
    }

    @Test
    fun `familyId can be set to non-null`() {
        val template = TaskTemplate(familyId = "fam123")
        assertEquals("fam123", template.familyId)
    }

    @Test
    fun `generationParams preserves types`() {
        val params = mapOf("count" to 3, "label" to "test", "active" to true)
        val template = TaskTemplate(generationParams = params)
        assertEquals(3, template.generationParams["count"])
        assertEquals("test", template.generationParams["label"])
        assertEquals(true, template.generationParams["active"])
    }

    // ── copy ────────────────────────────────────────────────────────

    @Test
    fun `copy preserves unchanged fields`() {
        val template = TaskTemplate(templateId = "t1", familyId = "f1")
        val copy = template.copy(templateId = "t2")
        assertEquals("t2", copy.templateId)
        assertEquals("f1", copy.familyId)
    }

    // ── equality ────────────────────────────────────────────────────

    @Test
    fun `same data equals`() {
        val a = TaskTemplate(templateId = "t1", familyId = "f1")
        val b = TaskTemplate(templateId = "t1", familyId = "f1")
        assertEquals(a, b)
    }

    @Test
    fun `different data not equals`() {
        val a = TaskTemplate(templateId = "t1")
        val b = TaskTemplate(templateId = "t2")
        assertNotEquals(a, b)
    }
}
