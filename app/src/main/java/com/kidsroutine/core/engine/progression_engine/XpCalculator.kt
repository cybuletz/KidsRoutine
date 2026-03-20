package com.kidsroutine.core.engine.progression_engine

import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.TaskModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XpCalculator @Inject constructor() {

    fun forTask(task: TaskModel, isCoop: Boolean = false, isStreakBonus: Boolean = false): Int {
        val base = task.reward.xp
        val diffMultiplier = when (task.difficulty) {
            DifficultyLevel.EASY   -> 1.0f
            DifficultyLevel.MEDIUM -> 1.5f
            DifficultyLevel.HARD   -> 2.0f
        }
        val coopBonus    = if (isCoop) 1.25f else 1.0f
        val streakBonus  = if (isStreakBonus) 1.1f else 1.0f
        return (base * diffMultiplier * coopBonus * streakBonus).toInt()
    }

    fun levelForXp(xp: Int): Int {
        // level = floor(sqrt(xp / 50)) + 1  — progressive curve
        return (Math.sqrt(xp / 50.0).toInt()) + 1
    }

    fun xpToNextLevel(currentXp: Int): Int {
        val level = levelForXp(currentXp)
        val nextLevelXp = (level * level) * 50
        return (nextLevelXp - currentXp).coerceAtLeast(0)
    }
}
