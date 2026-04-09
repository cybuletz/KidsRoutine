package com.kidsroutine.core.engine.task_engine

import com.kidsroutine.core.model.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TaskEngineTest {

    private lateinit var generator: TaskGenerator
    private lateinit var validator: TaskValidator
    private lateinit var engine: TaskEngine

    @Before
    fun setUp() {
        generator = mockk()
        validator = mockk()
        engine = TaskEngine(generator, validator)
    }

    // ── generateDailyTasks delegates to generator ───────────────────

    @Test
    fun `generateDailyTasks delegates to generator`() {
        val templates = listOf(TaskTemplate(templateId = "t1"))
        val injected = listOf(TaskInstance(instanceId = "i1"))
        val context = GenerationContext(
            userId = "u1",
            familyId = "f1",
            date = "2026-01-01",
            ageGroup = AgeGroup.EXPLORER
        )
        val expected = listOf(TaskInstance(instanceId = "gen1"))

        every { generator.generate(templates, injected, context) } returns expected

        val result = engine.generateDailyTasks(templates, injected, context)

        assertEquals(expected, result)
        verify(exactly = 1) { generator.generate(templates, injected, context) }
    }

    @Test
    fun `generateDailyTasks returns empty when generator returns empty`() {
        val context = GenerationContext(
            userId = "u1",
            familyId = "f1",
            date = "2026-01-01",
            ageGroup = AgeGroup.EXPLORER
        )
        every { generator.generate(any(), any(), any()) } returns emptyList()

        val result = engine.generateDailyTasks(emptyList(), emptyList(), context)
        assertTrue(result.isEmpty())
    }

    // ── validate delegates to validator ─────────────────────────────

    @Test
    fun `validate delegates to validator and returns Approved`() {
        val task = TaskModel(title = "Test task")
        val progress = TaskProgressModel(taskInstanceId = "t1")

        every { validator.validate(task, progress, null) } returns ValidationResult.Approved

        val result = engine.validate(task, progress)

        assertEquals(ValidationResult.Approved, result)
        verify(exactly = 1) { validator.validate(task, progress, null) }
    }

    @Test
    fun `validate passes photoUrl to validator`() {
        val task = TaskModel(validationType = ValidationType.PHOTO_REQUIRED)
        val progress = TaskProgressModel()
        val photo = "https://example.com/photo.jpg"

        every { validator.validate(task, progress, photo) } returns ValidationResult.Approved

        val result = engine.validate(task, progress, photo)

        assertEquals(ValidationResult.Approved, result)
        verify(exactly = 1) { validator.validate(task, progress, photo) }
    }

    @Test
    fun `validate returns PendingParent from validator`() {
        val task = TaskModel(validationType = ValidationType.PARENT_REQUIRED)
        val progress = TaskProgressModel()

        every { validator.validate(task, progress, null) } returns ValidationResult.PendingParent

        val result = engine.validate(task, progress)
        assertEquals(ValidationResult.PendingParent, result)
    }

    @Test
    fun `validate returns Rejected from validator`() {
        val task = TaskModel()
        val progress = TaskProgressModel()
        val rejection = ValidationResult.Rejected("Missing photo")

        every { validator.validate(task, progress, null) } returns rejection

        val result = engine.validate(task, progress)
        assertTrue(result is ValidationResult.Rejected)
        assertEquals("Missing photo", (result as ValidationResult.Rejected).reason)
    }

    // ── engine exposes components ───────────────────────────────────

    @Test
    fun `generator is accessible`() {
        assertSame(generator, engine.generator)
    }

    @Test
    fun `validator is accessible`() {
        assertSame(validator, engine.validator)
    }
}
