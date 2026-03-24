package com.kidsroutine.feature.world.data

import com.kidsroutine.core.model.WorldModel
import com.kidsroutine.core.model.WorldNode
import com.kidsroutine.core.model.WorldNodeStatus
import com.kidsroutine.core.model.WorldTheme
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorldRepositoryImpl @Inject constructor() : WorldRepository {

    override suspend fun getWorld(userXp: Int): WorldModel {
        val nodes = listOf(
            WorldNode(
                nodeId = "node_1",
                title = "Starter Island",
                subtitle = "Begin your journey",
                emoji = "🏝️",
                requiredXp = 0,
                status = statusFor(userXp, 0),
                positionX = 0.18f,
                positionY = 0.82f,
                rewardXp = 50,
                theme = WorldTheme.JUNGLE
            ),
            WorldNode(
                nodeId = "node_2",
                title = "Forest Trail",
                subtitle = "100 XP needed",
                emoji = "🌲",
                requiredXp = 100,
                status = statusFor(userXp, 100),
                positionX = 0.42f,
                positionY = 0.68f,
                rewardXp = 75,
                theme = WorldTheme.JUNGLE
            ),
            WorldNode(
                nodeId = "node_3",
                title = "River Crossing",
                subtitle = "250 XP needed",
                emoji = "🌊",
                requiredXp = 250,
                status = statusFor(userXp, 250),
                positionX = 0.70f,
                positionY = 0.58f,
                rewardXp = 100,
                theme = WorldTheme.OCEAN
            ),
            WorldNode(
                nodeId = "node_4",
                title = "Crystal Cave",
                subtitle = "450 XP needed",
                emoji = "💎",
                requiredXp = 450,
                status = statusFor(userXp, 450),
                positionX = 0.28f,
                positionY = 0.42f,
                rewardXp = 125,
                theme = WorldTheme.JUNGLE
            ),
            WorldNode(
                nodeId = "node_5",
                title = "Sky Temple",
                subtitle = "700 XP needed",
                emoji = "⛩️",
                requiredXp = 700,
                status = statusFor(userXp, 700),
                positionX = 0.60f,
                positionY = 0.30f,
                rewardXp = 150,
                theme = WorldTheme.SPACE
            ),
            WorldNode(
                nodeId = "node_6",
                title = "Champion Peak",
                subtitle = "1000 XP needed",
                emoji = "🏆",
                requiredXp = 1000,
                status = statusFor(userXp, 1000),
                positionX = 0.38f,
                positionY = 0.14f,
                rewardXp = 200,
                theme = WorldTheme.SPACE,
                isSpecial = true
            )
        )
        return WorldModel(
            worldId = "world_1",
            title = "Kids Routine World",
            theme = WorldTheme.JUNGLE,
            nodes = nodes,
            totalXpRequired = 1000
        )
    }

    private fun statusFor(userXp: Int, required: Int) = when {
        userXp >= required -> WorldNodeStatus.UNLOCKED
        else               -> WorldNodeStatus.LOCKED
    }
}