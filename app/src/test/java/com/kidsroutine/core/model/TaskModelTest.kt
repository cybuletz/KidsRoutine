package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class TaskModelTest {

    // ── TaskModel defaults ──────────────────────────────────────────

    @Test
    fun `default type is REAL_LIFE`() {
        val task = TaskModel()
        assertEquals(TaskType.REAL_LIFE, task.type)
    }

    @Test
    fun `default category is MORNING_ROUTINE`() {
        val task = TaskModel()
        assertEquals(TaskCategory.MORNING_ROUTINE, task.category)
    }

    @Test
    fun `default difficulty is EASY`() {
        val task = TaskModel()
        assertEquals(DifficultyLevel.EASY, task.difficulty)
    }

    @Test
    fun `default estimatedDurationSec is 60`() {
        val task = TaskModel()
        assertEquals(60, task.estimatedDurationSec)
    }

    @Test
    fun `default gameType is NONE`() {
        val task = TaskModel()
        assertEquals(GameType.NONE, task.gameType)
    }

    @Test
    fun `default validationType is SELF`() {
        val task = TaskModel()
        assertEquals(ValidationType.SELF, task.validationType)
    }

    @Test
    fun `default reward xp is 10`() {
        val task = TaskModel()
        assertEquals(10, task.reward.xp)
    }

    @Test
    fun `default interactionBlocks is empty`() {
        val task = TaskModel()
        assertTrue(task.interactionBlocks.isEmpty())
    }

    @Test
    fun `default requiresParent is false`() {
        val task = TaskModel()
        assertFalse(task.requiresParent)
    }

    @Test
    fun `default requiresCoop is false`() {
        val task = TaskModel()
        assertFalse(task.requiresCoop)
    }

    @Test
    fun `default createdBy is SYSTEM`() {
        val task = TaskModel()
        assertEquals(TaskCreator.SYSTEM, task.createdBy)
    }

    @Test
    fun `default isActive is true`() {
        val task = TaskModel()
        assertTrue(task.isActive)
    }

    @Test
    fun `default expiresAt is null`() {
        val task = TaskModel()
        assertNull(task.expiresAt)
    }

    // ── TaskType enum ───────────────────────────────────────────────

    @Test
    fun `TaskType has 8 entries`() {
        assertEquals(8, TaskType.entries.size)
    }

    @Test
    fun `TaskType includes all expected types`() {
        val names = TaskType.entries.map { it.name }
        assertTrue(names.contains("LOGIC"))
        assertTrue(names.contains("REAL_LIFE"))
        assertTrue(names.contains("CREATIVE"))
        assertTrue(names.contains("LEARNING"))
        assertTrue(names.contains("EMOTIONAL"))
        assertTrue(names.contains("CO_OP"))
        assertTrue(names.contains("SOCIAL"))
        assertTrue(names.contains("STORY"))
    }

    // ── TaskCategory enum ───────────────────────────────────────────

    @Test
    fun `TaskCategory has 10 entries`() {
        assertEquals(10, TaskCategory.entries.size)
    }

    @Test
    fun `TaskCategory includes all expected categories`() {
        val names = TaskCategory.entries.map { it.name }
        assertTrue(names.contains("MORNING_ROUTINE"))
        assertTrue(names.contains("HEALTH"))
        assertTrue(names.contains("LEARNING"))
        assertTrue(names.contains("CREATIVITY"))
        assertTrue(names.contains("SOCIAL"))
        assertTrue(names.contains("FAMILY"))
        assertTrue(names.contains("CHORES"))
        assertTrue(names.contains("OUTDOOR"))
        assertTrue(names.contains("SLEEP"))
        assertTrue(names.contains("SCREEN_TIME"))
    }

    // ── DifficultyLevel enum ────────────────────────────────────────

    @Test
    fun `DifficultyLevel has 3 entries`() {
        assertEquals(3, DifficultyLevel.entries.size)
    }

    // ── TaskCreator enum ────────────────────────────────────────────

    @Test
    fun `TaskCreator has 5 entries`() {
        assertEquals(5, TaskCreator.entries.size)
    }

    @Test
    fun `TaskCreator includes AI_GENERATED`() {
        assertTrue(TaskCreator.entries.map { it.name }.contains("AI_GENERATED"))
    }

    // ── ValidationType enum ─────────────────────────────────────────

    @Test
    fun `ValidationType has 5 entries`() {
        assertEquals(5, ValidationType.entries.size)
    }

    @Test
    fun `ValidationType includes AUTO SELF PHOTO_REQUIRED PARENT_REQUIRED HYBRID`() {
        val names = ValidationType.entries.map { it.name }
        assertTrue(names.contains("AUTO"))
        assertTrue(names.contains("SELF"))
        assertTrue(names.contains("PHOTO_REQUIRED"))
        assertTrue(names.contains("PARENT_REQUIRED"))
        assertTrue(names.contains("HYBRID"))
    }

    // ── InteractionBlockType enum ───────────────────────────────────

    @Test
    fun `InteractionBlockType has 9 entries`() {
        assertEquals(9, InteractionBlockType.entries.size)
    }

    // ── InteractionBlock defaults ───────────────────────────────────

    @Test
    fun `InteractionBlock default type is TAP_SELECT`() {
        val block = InteractionBlock()
        assertEquals(InteractionBlockType.TAP_SELECT, block.type)
    }

    @Test
    fun `InteractionBlock default required is true`() {
        val block = InteractionBlock()
        assertTrue(block.required)
    }

    @Test
    fun `InteractionBlock default config is empty`() {
        val block = InteractionBlock()
        assertTrue(block.config.isEmpty())
    }

    // ── TaskReward defaults ─────────────────────────────────────────

    @Test
    fun `TaskReward default xp is 0`() {
        val reward = TaskReward()
        assertEquals(0, reward.xp)
    }

    @Test
    fun `TaskReward default bonusConditions is empty`() {
        val reward = TaskReward()
        assertTrue(reward.bonusConditions.isEmpty())
    }

    // ── Role enum ───────────────────────────────────────────────────

    @Test
    fun `Role has 2 entries`() {
        assertEquals(2, Role.entries.size)
    }
}
