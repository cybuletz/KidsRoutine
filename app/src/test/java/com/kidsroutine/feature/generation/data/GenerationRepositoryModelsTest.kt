package com.kidsroutine.feature.generation.data

import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.TaskCategory
import com.kidsroutine.core.model.TaskCreator
import com.kidsroutine.core.model.TaskType
import org.junit.Assert.*
import org.junit.Test

class GenerationRepositoryModelsTest {

    // ── GeneratedTask ──────────────────────────────────────────────

    @Test
    fun `GeneratedTask stores all fields`() {
        val task = GeneratedTask(
            title = "Clean Room",
            description = "Tidy up your bedroom",
            estimatedDurationSec = 600,
            category = "CHORES",
            difficulty = "EASY",
            xpReward = 25,
            type = "REAL_LIFE"
        )
        assertEquals("Clean Room", task.title)
        assertEquals("Tidy up your bedroom", task.description)
        assertEquals(600, task.estimatedDurationSec)
        assertEquals("CHORES", task.category)
        assertEquals("EASY", task.difficulty)
        assertEquals(25, task.xpReward)
        assertEquals("REAL_LIFE", task.type)
    }

    @Test
    fun `GeneratedTask equality for identical instances`() {
        val a = GeneratedTask("T", "D", 60, "HEALTH", "MEDIUM", 10, "LOGIC")
        val b = GeneratedTask("T", "D", 60, "HEALTH", "MEDIUM", 10, "LOGIC")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `GeneratedTask inequality when title differs`() {
        val a = GeneratedTask("A", "D", 60, "HEALTH", "MEDIUM", 10, "LOGIC")
        val b = GeneratedTask("B", "D", 60, "HEALTH", "MEDIUM", 10, "LOGIC")
        assertNotEquals(a, b)
    }

    @Test
    fun `GeneratedTask copy changes only specified field`() {
        val original = GeneratedTask("T", "D", 60, "HEALTH", "MEDIUM", 10, "LOGIC")
        val copied = original.copy(xpReward = 50)
        assertEquals(50, copied.xpReward)
        assertEquals(original.title, copied.title)
        assertEquals(original.type, copied.type)
    }

    // ── GeneratedChallenge ─────────────────────────────────────────

    @Test
    fun `GeneratedChallenge stores all fields`() {
        val challenge = GeneratedChallenge(
            title = "Reading Week",
            description = "Read 30 minutes every day",
            durationDays = 7,
            category = "LEARNING",
            successCondition = "Complete all 7 days"
        )
        assertEquals("Reading Week", challenge.title)
        assertEquals("Read 30 minutes every day", challenge.description)
        assertEquals(7, challenge.durationDays)
        assertEquals("LEARNING", challenge.category)
        assertEquals("Complete all 7 days", challenge.successCondition)
    }

    @Test
    fun `GeneratedChallenge equality for identical instances`() {
        val a = GeneratedChallenge("C", "D", 3, "HEALTH", "Done")
        val b = GeneratedChallenge("C", "D", 3, "HEALTH", "Done")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `GeneratedChallenge inequality when durationDays differs`() {
        val a = GeneratedChallenge("C", "D", 3, "HEALTH", "Done")
        val b = GeneratedChallenge("C", "D", 5, "HEALTH", "Done")
        assertNotEquals(a, b)
    }

    @Test
    fun `GeneratedChallenge copy changes only specified field`() {
        val original = GeneratedChallenge("C", "D", 3, "HEALTH", "Done")
        val copied = original.copy(title = "New Title")
        assertEquals("New Title", copied.title)
        assertEquals(original.durationDays, copied.durationDays)
    }

    // ── GeneratedTasksResponse ─────────────────────────────────────

    @Test
    fun `GeneratedTasksResponse stores success state with tasks`() {
        val task = GeneratedTask("T", "D", 60, "HEALTH", "EASY", 10, "LOGIC")
        val response = GeneratedTasksResponse(
            success = true,
            tasks = listOf(task),
            cached = false,
            quotaRemaining = 5
        )
        assertTrue(response.success)
        assertEquals(1, response.tasks.size)
        assertFalse(response.cached)
        assertEquals(5, response.quotaRemaining)
    }

    @Test
    fun `GeneratedTasksResponse failure state`() {
        val response = GeneratedTasksResponse(
            success = false,
            tasks = emptyList(),
            cached = false,
            quotaRemaining = 0
        )
        assertFalse(response.success)
        assertTrue(response.tasks.isEmpty())
    }

    @Test
    fun `GeneratedTasksResponse with empty tasks list`() {
        val response = GeneratedTasksResponse(
            success = true,
            tasks = emptyList(),
            cached = true,
            quotaRemaining = 3
        )
        assertTrue(response.success)
        assertTrue(response.tasks.isEmpty())
        assertTrue(response.cached)
    }

    @Test
    fun `GeneratedTasksResponse cached flag is preserved`() {
        val cached = GeneratedTasksResponse(true, emptyList(), cached = true, quotaRemaining = 2)
        val fresh  = GeneratedTasksResponse(true, emptyList(), cached = false, quotaRemaining = 2)
        assertTrue(cached.cached)
        assertFalse(fresh.cached)
        assertNotEquals(cached, fresh)
    }

    // ── GeneratedChallengesResponse ────────────────────────────────

    @Test
    fun `GeneratedChallengesResponse stores success state with challenges`() {
        val challenge = GeneratedChallenge("C", "D", 7, "HEALTH", "Done")
        val response = GeneratedChallengesResponse(
            success = true,
            challenges = listOf(challenge),
            cached = false,
            quotaRemaining = 2
        )
        assertTrue(response.success)
        assertEquals(1, response.challenges.size)
        assertEquals(2, response.quotaRemaining)
    }

    @Test
    fun `GeneratedChallengesResponse failure state`() {
        val response = GeneratedChallengesResponse(
            success = false,
            challenges = emptyList(),
            cached = false,
            quotaRemaining = 0
        )
        assertFalse(response.success)
        assertTrue(response.challenges.isEmpty())
    }

    @Test
    fun `GeneratedChallengesResponse with empty challenges list`() {
        val response = GeneratedChallengesResponse(
            success = true,
            challenges = emptyList(),
            cached = true,
            quotaRemaining = 1
        )
        assertTrue(response.challenges.isEmpty())
        assertTrue(response.cached)
    }

    // ── toTaskModel() ──────────────────────────────────────────────

    private fun sampleTask(
        type: String = "LOGIC",
        category: String = "CREATIVITY",
        difficulty: String = "HARD",
        xpReward: Int = 30
    ) = GeneratedTask(
        title = "Sample",
        description = "Desc",
        estimatedDurationSec = 120,
        category = category,
        difficulty = difficulty,
        xpReward = xpReward,
        type = type
    )

    @Test
    fun `toTaskModel maps type correctly`() {
        val model = sampleTask(type = "CREATIVE").toTaskModel("fam1")
        assertEquals(TaskType.CREATIVE, model.type)
    }

    @Test
    fun `toTaskModel maps category correctly`() {
        val model = sampleTask(category = "HEALTH").toTaskModel("fam1")
        assertEquals(TaskCategory.HEALTH, model.category)
    }

    @Test
    fun `toTaskModel maps difficulty correctly`() {
        val model = sampleTask(difficulty = "EASY").toTaskModel("fam1")
        assertEquals(DifficultyLevel.EASY, model.difficulty)
    }

    @Test
    fun `toTaskModel defaults blank type to REAL_LIFE`() {
        val model = sampleTask(type = "").toTaskModel("fam1")
        assertEquals(TaskType.REAL_LIFE, model.type)
    }

    @Test
    fun `toTaskModel defaults blank category to CREATIVITY`() {
        val model = sampleTask(category = "").toTaskModel("fam1")
        assertEquals(TaskCategory.CREATIVITY, model.category)
    }

    @Test
    fun `toTaskModel defaults blank difficulty to MEDIUM`() {
        val model = sampleTask(difficulty = "").toTaskModel("fam1")
        assertEquals(DifficultyLevel.MEDIUM, model.difficulty)
    }

    @Test
    fun `toTaskModel sets creator to AI_GENERATED`() {
        val model = sampleTask().toTaskModel("fam1")
        assertEquals(TaskCreator.AI_GENERATED, model.createdBy)
    }

    @Test
    fun `toTaskModel passes familyId through`() {
        val model = sampleTask().toTaskModel("family_abc")
        assertEquals("family_abc", model.familyId)
    }

    @Test
    fun `toTaskModel maps xpReward to TaskReward`() {
        val model = sampleTask(xpReward = 42).toTaskModel("fam1")
        assertEquals(42, model.reward.xp)
    }

    @Test
    fun `toTaskModel preserves title and description`() {
        val task = GeneratedTask("My Title", "My Desc", 90, "OUTDOOR", "MEDIUM", 15, "REAL_LIFE")
        val model = task.toTaskModel("fam1")
        assertEquals("My Title", model.title)
        assertEquals("My Desc", model.description)
    }

    @Test
    fun `toTaskModel preserves estimatedDurationSec`() {
        val task = GeneratedTask("T", "D", 300, "HEALTH", "EASY", 10, "LOGIC")
        val model = task.toTaskModel("fam1")
        assertEquals(300, model.estimatedDurationSec)
    }

    @Test
    fun `toTaskModel id starts with gen_ prefix`() {
        val model = sampleTask().toTaskModel("fam1")
        assertTrue("id should start with gen_", model.id.startsWith("gen_"))
    }
}
