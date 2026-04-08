package com.kidsroutine.feature.spinwheel.data

import com.kidsroutine.core.model.DailySpinState
import com.kidsroutine.core.model.SpinWheelResult

interface SpinWheelRepository {
    suspend fun getDailyState(userId: String, date: String): DailySpinState?
    suspend fun saveDailyState(state: DailySpinState)
    suspend fun saveSpinResult(userId: String, result: SpinWheelResult)
}
