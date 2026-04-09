package com.kidsroutine.core.engine.progression_engine

import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.TaskReward

/**
 * Factory for creating TaskModel instances in tests.
 * Provides sensible defaults so tests only specify what matters.
 */
object TestTaskFactory {
    fun create(
        id: String = "test_task_1",
        title: String = "Test Task",
        xp: Int = 10,
        difficulty: DifficultyLevel = DifficultyLevel.EASY,
        requiresCoop: Boolean = false,
        familyId: String = "test_family"
    ): TaskModel = TaskModel(
        id = id,
        title = title,
        reward = TaskReward(xp = xp),
        difficulty = difficulty,
        requiresCoop = requiresCoop,
        familyId = familyId
    )
}
