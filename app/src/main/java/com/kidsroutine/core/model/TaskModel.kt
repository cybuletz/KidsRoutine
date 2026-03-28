package com.kidsroutine.core.model

data class TaskModel(
    var id: String = "",
    var type: TaskType = TaskType.REAL_LIFE,
    var title: String = "",
    var description: String = "",
    var category: TaskCategory = TaskCategory.MORNING_ROUTINE,
    var difficulty: DifficultyLevel = DifficultyLevel.EASY,
    var estimatedDurationSec: Int = 60,
    var interactionBlocks: List<InteractionBlock> = emptyList(),
    var gameType: GameType = GameType.NONE,
    var validationType: ValidationType = ValidationType.SELF,
    var reward: TaskReward = TaskReward(xp = 10),
    var tags: List<String> = emptyList(),
    var requiresParent: Boolean = false,
    var requiresCoop: Boolean = false,
    var createdBy: TaskCreator = TaskCreator.SYSTEM,
    var familyId: String = "",
    var isActive: Boolean = true,
    var expiresAt: Long? = null,
    var durationDays: Int? = null
)