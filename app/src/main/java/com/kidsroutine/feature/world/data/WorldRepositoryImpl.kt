package com.kidsroutine.feature.world.data

import com.kidsroutine.core.model.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sin

@Singleton
class WorldRepositoryImpl @Inject constructor() : WorldRepository {

    override suspend fun getWorld(userXp: Int): WorldModel {
        val allLevels = WorldLevelData.ALL_LEVELS

        // Determine which nodes to show:
        // Always show all nodes up to and including the first LOCKED node
        // plus the next 3 locked nodes as "preview" — so the child can see what's coming
        val nodes = allLevels.map { def ->
            val status = when {
                userXp >= def.requiredXp ->
                    if (def.level == 1 || userXp >= WorldLevelData.xpForLevel(def.level))
                        WorldNodeStatus.COMPLETED
                    else
                        WorldNodeStatus.UNLOCKED
                userXp >= WorldLevelData.xpForLevel(maxOf(1, def.level - 1)) ->
                    WorldNodeStatus.UNLOCKED
                else ->
                    WorldNodeStatus.LOCKED
            }
            WorldNode(
                nodeId      = "node_${def.level}",
                title       = def.title,
                subtitle    = def.subtitle,
                emoji       = def.emoji,
                requiredXp  = def.requiredXp,
                status      = status,
                positionX   = positionX(def.level),
                positionY   = positionY(def.level),
                rewardXp    = def.rewardXp,
                theme       = def.theme,
                isSpecial   = def.isBoss,
                levelNumber = def.level
            )
        }

        // Only expose the window of nodes relevant to the user:
        // All completed + 1 unlocked + 9 upcoming locked = max 30 visible at once
        // This keeps the map performant regardless of total level count
        val lastCompletedIndex = nodes.indexOfLast { it.status == WorldNodeStatus.COMPLETED }
        val startIndex = maxOf(0, lastCompletedIndex - 5)
        val endIndex   = minOf(nodes.size, lastCompletedIndex + 25)
        val visibleNodes = nodes.subList(startIndex, endIndex)

        // Current theme = the theme of the current unlocked/active node
        val activeNode  = nodes.firstOrNull { it.status == WorldNodeStatus.UNLOCKED }
            ?: nodes.firstOrNull()
        val activeTheme = activeNode?.theme ?: WorldTheme.JUNGLE

        return WorldModel(
            worldId          = "world_main",
            title            = themeTitle(activeTheme),
            theme            = activeTheme,
            nodes            = visibleNodes,
            totalXpRequired  = WorldLevelData.xpForLevel(500)
        )
    }

    // Winding path: nodes alternate left-center-right in a S-curve pattern
    private fun positionX(level: Int): Float {
        val posInZone = ((level - 1) % 50)  // 0..49
        // S-curve: use sine wave across the zone
        val angle = posInZone.toFloat() / 49f * Math.PI.toFloat() * 4f
        return 0.5f + sin(angle) * 0.3f
    }

    private fun positionY(level: Int): Float {
        val posInZone = ((level - 1) % 50).toFloat()
        // Invert: level 1 = bottom (0.92), higher levels = top (0.08)
        return 0.92f - (posInZone / 49f) * 0.84f
    }

    private fun themeTitle(theme: WorldTheme) = when (theme) {
        WorldTheme.JUNGLE   -> "🌴 Enchanted Jungle"
        WorldTheme.OCEAN    -> "🌊 Deep Ocean"
        WorldTheme.SPACE    -> "🚀 Outer Space"
        WorldTheme.VOLCANO  -> "🌋 Volcano Island"
        WorldTheme.ARCTIC   -> "❄️ Arctic Kingdom"
        WorldTheme.NEON_CITY-> "🌃 Neon City"
        WorldTheme.CRYSTAL  -> "💎 Crystal Caves"
        WorldTheme.CLOUD    -> "☁️ Sky Citadel"
        WorldTheme.DESERT   -> "🏜️ Ancient Desert"
        WorldTheme.COSMOS   -> "🌌 The Cosmos"
    }
}
