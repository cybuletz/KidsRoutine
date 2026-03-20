package com.kidsroutine.core.engine.progression_engine

import com.kidsroutine.core.model.TaskModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressionEngine @Inject constructor(
    val xpCalculator: XpCalculator,
    val streakCalculator: StreakCalculator
)
