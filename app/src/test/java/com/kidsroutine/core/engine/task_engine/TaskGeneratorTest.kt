package com.kidsroutine.core.engine.task_engine

import com.kidsroutine.core.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TaskGeneratorTest {

    private lateinit var generator: TaskGenerator

    @Before
    fun setUp() {
        generator = TaskGenerator()
    }

    private fun createTemplate(
        id: String,
        type: TaskType = TaskType.REAL_LIFE,
        requiresCoop: Boolean = false
    ) = TaskTemplate(
        templateId = id,
        baseTask = TaskModel(
            id = id,
            type = type,
            title = "Task $id",
            requiresCoop = requiresCoop
        )
    )

    private fun createContext(
        recentTemplateIds: List<String> = emptyList(),
        activeChallengeTaskIds: List<String> = emptyList()
    ) = GenerationContext(
        userId = "user1",
        date = "2026-04-09",
        recentTemplateIds = recentTemplateIds,
        activeChallengeTaskIds = activeChallengeTaskIds,
        userPreferences = UserPreferences()
    )

    // ── generate ────────────────────────────────────────────────────

    @Test
    fun `generate returns at most DAILY_TASK_LIMIT tasks`() {
        val templates = (1..20).map { createTemplate("t$it") }
        val result = generator.generate(templates, emptyList(), createContext())
        assertTrue(result.size <= TaskGenerator.DAILY_TASK_LIMIT)
    }

    @Test
    fun `generate returns exactly DAILY_TASK_LIMIT with enough templates`() {
        val templates = (1..20).map { createTemplate("t$it") }
        val result = generator.generate(templates, emptyList(), createContext())
        assertEquals(TaskGenerator.DAILY_TASK_LIMIT, result.size)
    }

    @Test
    fun `generate includes injected tasks first`() {
        val injected = listOf(
            TaskInstance(
                instanceId = "inj_1",
                templateId = "challenge_t1",
                task = TaskModel(id = "challenge_t1", title = "Challenge Task"),
                assignedDate = "2026-04-09",
                userId = "user1"
            )
        )
        val templates = (1..20).map { createTemplate("t$it") }
        val result = generator.generate(templates, injected, createContext())
        assertTrue(result.any { it.instanceId == "inj_1" })
        assertEquals(TaskGenerator.DAILY_TASK_LIMIT, result.size)
    }

    @Test
    fun `generate respects DAILY_TASK_LIMIT when injected tasks fill it`() {
        val injected = (1..TaskGenerator.DAILY_TASK_LIMIT).map {
            TaskInstance(
                instanceId = "inj_$it",
                templateId = "ct$it",
                task = TaskModel(id = "ct$it", title = "Challenge $it"),
                assignedDate = "2026-04-09",
                userId = "user1"
            )
        }
        val templates = (1..10).map { createTemplate("t$it") }
        val result = generator.generate(templates, injected, createContext())
        assertEquals(TaskGenerator.DAILY_TASK_LIMIT, result.size)
    }

    @Test
    fun `generate filters out recently used templates`() {
        val templates = listOf(
            createTemplate("t1"),
            createTemplate("t2"),
            createTemplate("t3"),
            createTemplate("t4"),
            createTemplate("t5"),
            createTemplate("t6")
        )
        val context = createContext(recentTemplateIds = listOf("t1", "t2"))
        val result = generator.generate(templates, emptyList(), context)
        assertFalse(result.any { it.templateId == "t1" })
        assertFalse(result.any { it.templateId == "t2" })
    }

    @Test
    fun `generate returns less than limit when fewer templates available`() {
        val templates = listOf(createTemplate("t1"), createTemplate("t2"))
        val result = generator.generate(templates, emptyList(), createContext())
        assertEquals(2, result.size)
    }

    @Test
    fun `generate returns empty list when no templates or injected`() {
        val result = generator.generate(emptyList(), emptyList(), createContext())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `generate assigns correct userId and date`() {
        val templates = (1..5).map { createTemplate("t$it") }
        val result = generator.generate(templates, emptyList(), createContext())
        result.forEach {
            assertEquals("user1", it.userId)
            assertEquals("2026-04-09", it.assignedDate)
        }
    }

    @Test
    fun `generate creates unique instance IDs`() {
        val templates = (1..10).map { createTemplate("t$it") }
        val result = generator.generate(templates, emptyList(), createContext())
        val ids = result.map { it.instanceId }
        assertEquals(ids.size, ids.toSet().size) // all unique
    }
}
