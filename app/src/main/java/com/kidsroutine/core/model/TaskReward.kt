package com.kidsroutine.core.model

data class TaskReward(
    val xp: Int = 0,
    val bonusConditions: List<String> = emptyList()
)