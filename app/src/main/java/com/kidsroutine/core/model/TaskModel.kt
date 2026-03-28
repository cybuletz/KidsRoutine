package com.kidsroutine.core.model

data class TaskModel(
    val id: String = "",
    val type: TaskType = TaskType.REAL_LIFE,
    val title: String = "",
    val description: String = "",
    val category: TaskCategory = TaskCategory.MORNING_ROUTINE,
    val difficulty: DifficultyLevel = DifficultyLevel.EASY,
    val estimatedDurationSec: Int = 60,
    val interactionBlocks: List<InteractionBlock> = emptyList(),
    val gameType: GameType = GameType.NONE,
    val validationType: ValidationType = ValidationType.SELF,
    val reward: TaskReward = TaskReward(xp = 10),
    val tags: List<String> = emptyList(),
    val requiresParent: Boolean = false,
    val requiresCoop: Boolean = false,
    val createdBy: TaskCreator = TaskCreator.SYSTEM,
    val familyId: String = "",       // empty = global/system task
    val isActive: Boolean = true,
    val expiresAt: Long? = null,
    val durationDays: Int? = null
)
