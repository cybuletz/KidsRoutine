package com.kidsroutine.core.model

data class TaskReward(
    val xp: Int,
    val bonusConditions: List<String> = emptyList()
)
