package com.kidsroutine.core.model

enum class WorldNodeStatus { LOCKED, UNLOCKED, COMPLETED }

// Extended to 10 themes — used by WorldLevelData for procedural generation
enum class WorldTheme {
    JUNGLE, OCEAN, SPACE, VOLCANO, ARCTIC, NEON_CITY,
    CRYSTAL, CLOUD, DESERT, COSMOS
}

data class WorldNode(
    val nodeId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val emoji: String = "⭐",
    val requiredXp: Int = 0,
    val status: WorldNodeStatus = WorldNodeStatus.LOCKED,
    val positionX: Float = 0.5f,
    val positionY: Float = 0.5f,
    val rewardXp: Int = 50,
    val theme: WorldTheme = WorldTheme.JUNGLE,
    val isSpecial: Boolean = false,   // boss/milestone node
    val levelNumber: Int = 0          // global level index 1–500
)

data class WorldModel(
    val worldId: String = "",
    val title: String = "",
    val theme: WorldTheme = WorldTheme.JUNGLE,
    val nodes: List<WorldNode> = emptyList(),
    val totalXpRequired: Int = 0
)
