package com.kidsroutine.feature.execution.ui

import androidx.compose.runtime.Composable
import com.kidsroutine.core.model.GameType
import com.kidsroutine.feature.execution.ui.blocks.MemoryGameBlock
import com.kidsroutine.feature.execution.ui.blocks.SpeedGameBlock
import com.kidsroutine.feature.execution.ui.blocks.LogicGameBlock

/**
 * Central dispatcher for micro-games.
 * Renders the appropriate game based on gameType.
 */
@Composable
fun GameRenderer(
    gameType: GameType,
    onGameComplete: () -> Unit
) {
    when (gameType) {
        GameType.MEMORY_GAME -> MemoryGameBlock(onSuccess = onGameComplete)
        GameType.SPEED_GAME  -> SpeedGameBlock(onSuccess = onGameComplete)
        GameType.LOGIC_GAME  -> LogicGameBlock(onSuccess = onGameComplete)
        GameType.NONE -> {} // No game
    }
}