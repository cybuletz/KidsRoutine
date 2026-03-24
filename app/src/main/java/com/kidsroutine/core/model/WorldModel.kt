package com.kidsroutine.core.model

enum class WorldNodeStatus { LOCKED, UNLOCKED, COMPLETED }

enum class WorldTheme {
    JUNGLE, OCEAN, SPACE, VOLCANO, ARCTIC, NEON_CITY
}

data class WorldNode(
    val nodeId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val emoji: String = "⭐",
    val requiredXp: Int = 0,
    val status: WorldNodeStatus = WorldNodeStatus.LOCKED,
    val positionX: Float = 0.5f,   // 0.0–1.0 fraction of container width
    val positionY: Float = 0.5f,   // 0.0–1.0 fraction of container height
    val rewardXp: Int = 50,
    val theme: WorldTheme = WorldTheme.JUNGLE,
    val isSpecial: Boolean = false  // boss node / milestone
)

data class WorldModel(
    val worldId: String = "",
    val title: String = "",
    val theme: WorldTheme = WorldTheme.JUNGLE,
    val nodes: List<WorldNode> = emptyList(),
    val totalXpRequired: Int = 0
)