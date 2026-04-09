package com.kidsroutine.core.engine.spin_engine

import com.kidsroutine.core.model.DailySpinState
import com.kidsroutine.core.model.PlanType
import com.kidsroutine.core.model.SpinRewardType
import com.kidsroutine.core.model.SpinWheelResult
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Daily Reward / Spin Wheel Engine.
 * Manages daily spin availability, reward selection, and state.
 */
@Singleton
class DailyRewardEngine @Inject constructor() {

    /**
     * Create initial spin state for a new day.
     */
    fun createDailyState(userId: String, date: String, planType: PlanType): DailySpinState {
        return DailySpinState(
            userId = userId,
            date = date,
            spinsUsed = 0,
            maxSpins = when (planType) {
                PlanType.FREE -> 1
                PlanType.PRO -> 1
                PlanType.PREMIUM -> 2
            }
        )
    }

    /**
     * Execute a spin and return updated state with result.
     */
    fun spin(state: DailySpinState): Pair<DailySpinState, SpinWheelResult> {
        val reward = SpinRewardType.randomReward()
        val result = SpinWheelResult(
            resultId = UUID.randomUUID().toString(),
            userId = state.userId,
            reward = reward,
            date = state.date,
            spinNumber = state.spinsUsed + 1,
            claimedAt = System.currentTimeMillis()
        )

        val updatedState = state.copy(
            spinsUsed = state.spinsUsed + 1,
            results = state.results + result
        )

        return Pair(updatedState, result)
    }

    /**
     * Get immediate XP bonus from spin result.
     * Returns the xpValue defined on each SpinRewardType.
     */
    fun immediateXpBonus(result: SpinWheelResult): Int = result.reward.xpValue
}
