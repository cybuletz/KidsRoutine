package com.kidsroutine.feature.daily.domain

import com.kidsroutine.core.model.TaskInstance
import com.kidsroutine.core.model.TaskModel
import org.junit.Assert.*
import org.junit.Test

class GenerationOutcomeTest {

    // ── Success ─────────────────────────────────────────────────────

    @Test
    fun `Success wraps task list`() {
        val tasks = listOf(
            TaskInstance(instanceId = "t1", task = TaskModel(title = "Brush teeth")),
            TaskInstance(instanceId = "t2", task = TaskModel(title = "Make bed"))
        )
        val outcome = GenerationOutcome.Success(tasks)
        assertEquals(2, outcome.tasks.size)
        assertEquals("Brush teeth", outcome.tasks[0].task.title)
    }

    @Test
    fun `Success with empty list`() {
        val outcome = GenerationOutcome.Success(emptyList())
        assertTrue(outcome.tasks.isEmpty())
    }

    @Test
    fun `Success is GenerationOutcome`() {
        val outcome: GenerationOutcome = GenerationOutcome.Success(emptyList())
        assertTrue(outcome is GenerationOutcome.Success)
    }

    // ── AlreadyGenerated ────────────────────────────────────────────

    @Test
    fun `AlreadyGenerated is singleton`() {
        val a = GenerationOutcome.AlreadyGenerated
        val b = GenerationOutcome.AlreadyGenerated
        assertSame(a, b)
    }

    @Test
    fun `AlreadyGenerated is GenerationOutcome`() {
        val outcome: GenerationOutcome = GenerationOutcome.AlreadyGenerated
        assertTrue(outcome is GenerationOutcome.AlreadyGenerated)
    }

    // ── NoTemplatesAvailable ────────────────────────────────────────

    @Test
    fun `NoTemplatesAvailable is singleton`() {
        val a = GenerationOutcome.NoTemplatesAvailable
        val b = GenerationOutcome.NoTemplatesAvailable
        assertSame(a, b)
    }

    @Test
    fun `NoTemplatesAvailable is GenerationOutcome`() {
        val outcome: GenerationOutcome = GenerationOutcome.NoTemplatesAvailable
        assertTrue(outcome is GenerationOutcome.NoTemplatesAvailable)
    }

    // ── Error ───────────────────────────────────────────────────────

    @Test
    fun `Error is singleton`() {
        val a = GenerationOutcome.Error
        val b = GenerationOutcome.Error
        assertSame(a, b)
    }

    @Test
    fun `Error is GenerationOutcome`() {
        val outcome: GenerationOutcome = GenerationOutcome.Error
        assertTrue(outcome is GenerationOutcome.Error)
    }

    // ── when exhaustiveness ─────────────────────────────────────────

    @Test
    fun `when covers all branches`() {
        val outcomes = listOf(
            GenerationOutcome.Success(emptyList()),
            GenerationOutcome.AlreadyGenerated,
            GenerationOutcome.NoTemplatesAvailable,
            GenerationOutcome.Error
        )
        for (outcome in outcomes) {
            val label = when (outcome) {
                is GenerationOutcome.Success -> "success"
                GenerationOutcome.AlreadyGenerated -> "already"
                GenerationOutcome.NoTemplatesAvailable -> "no_templates"
                GenerationOutcome.Error -> "error"
            }
            assertTrue(label.isNotEmpty())
        }
    }
}
