package com.kidsroutine.feature.execution.ui

import androidx.compose.runtime.Composable
import com.kidsroutine.core.model.AgeGroup
import com.kidsroutine.core.model.GameType
import com.kidsroutine.feature.execution.ui.blocks.MemoryGameBlock
import com.kidsroutine.feature.execution.ui.blocks.SpeedGameBlock
import com.kidsroutine.feature.execution.ui.blocks.LogicGameBlock
import com.kidsroutine.feature.execution.ui.blocks.WordScrambleGameBlock
import com.kidsroutine.feature.execution.ui.blocks.PatternMatchGameBlock
import com.kidsroutine.feature.execution.ui.blocks.QuickThinkGameBlock
import com.kidsroutine.feature.execution.ui.blocks.CodeCrackGameBlock
import com.kidsroutine.feature.execution.ui.blocks.BudgetBossGameBlock
import com.kidsroutine.feature.execution.ui.blocks.FactFictionGameBlock
import com.kidsroutine.feature.execution.ui.blocks.CountingGameBlock
import com.kidsroutine.feature.execution.ui.blocks.ShapeSortGameBlock
import com.kidsroutine.feature.execution.ui.blocks.SequenceGameBlock
import com.kidsroutine.feature.execution.ui.blocks.EstimationGameBlock
import com.kidsroutine.feature.execution.ui.blocks.TypingSpeedGameBlock
import com.kidsroutine.feature.execution.ui.blocks.TimeArchitectGameBlock
import com.kidsroutine.feature.execution.ui.blocks.DebatePromptGameBlock

/**
 * Central dispatcher for micro-games.
 * Renders the appropriate game based on gameType + ageGroup for adaptive difficulty.
 * Every GameType has its own dedicated game block implementation.
 */
@Composable
fun GameRenderer(
    gameType: GameType,
    ageGroup: AgeGroup = AgeGroup.EXPLORER,
    onGameComplete: () -> Unit
) {
    when (gameType) {
        // Core games — difficulty scales with ageGroup
        GameType.MEMORY_GAME   -> MemoryGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)
        GameType.SPEED_GAME    -> SpeedGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)
        GameType.LOGIC_GAME    -> LogicGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)

        // Sprout games — each with dedicated implementation
        GameType.PATTERN_MATCH -> PatternMatchGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)
        GameType.COUNTING_GAME -> CountingGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)
        GameType.SHAPE_SORT    -> ShapeSortGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)

        // Explorer games — each with dedicated implementation
        GameType.WORD_SCRAMBLE -> WordScrambleGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)
        GameType.TRIVIA        -> QuickThinkGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)
        GameType.SEQUENCE      -> SequenceGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)

        // Trailblazer games — each with dedicated implementation
        GameType.QUICK_THINK   -> QuickThinkGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)
        GameType.CODE_CRACK    -> CodeCrackGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)
        GameType.ESTIMATION    -> EstimationGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)
        GameType.TYPING_SPEED  -> TypingSpeedGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)
        GameType.FACT_FICTION   -> FactFictionGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)

        // Legend games — each with dedicated implementation
        GameType.BUDGET_BOSS    -> BudgetBossGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)
        GameType.TIME_ARCHITECT -> TimeArchitectGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)
        GameType.DEBATE_PROMPT  -> DebatePromptGameBlock(ageGroup = ageGroup, onSuccess = onGameComplete)

        GameType.NONE -> {} // No game
    }
}