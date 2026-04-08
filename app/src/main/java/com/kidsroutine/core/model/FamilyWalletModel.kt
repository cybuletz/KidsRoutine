package com.kidsroutine.core.model

/**
 * Financial Literacy System — Real-world reward integration.
 * Bridges the gap to financial literacy apps like Greenlight/BusyKid.
 */

data class FamilyWallet(
    val familyId: String = "",
    val xpToMoneyRate: Float = 0.01f,    // 1 XP = $0.01 by default
    val currencySymbol: String = "$",
    val isEnabled: Boolean = false,       // parent opt-in
    val createdAt: Long = 0L
) {
    /** Convert XP to monetary value */
    fun xpToMoney(xp: Int): Float = xp * xpToMoneyRate

    /** Format monetary value */
    fun formatMoney(xp: Int): String {
        val amount = xpToMoney(xp)
        return "$currencySymbol${"%.2f".format(amount)}"
    }
}

data class SavingsGoal(
    val goalId: String = "",
    val userId: String = "",
    val familyId: String = "",
    val title: String = "",           // "New bike"
    val emoji: String = "🎯",
    val targetXp: Int = 0,            // total XP needed
    val currentXp: Int = 0,           // XP saved so far
    val targetMoneyValue: Float = 0f, // real money equivalent
    val isComplete: Boolean = false,
    val createdAt: Long = 0L,
    val completedAt: Long = 0L
) {
    val progressPercent: Float
        get() = if (targetXp > 0) (currentXp.toFloat() / targetXp).coerceIn(0f, 1f) else 0f

    val xpRemaining: Int get() = (targetXp - currentXp).coerceAtLeast(0)
}

/** Financial literacy task templates for "Earn & Learn" */
object FinancialLiteracyTasks {
    val templates = listOf(
        TaskModel(
            id = "fin_grocery_compare",
            type = TaskType.LEARNING,
            title = "Price Detective",
            description = "Compare prices of 3 items at the grocery store. Which is the best deal?",
            category = TaskCategory.LEARNING,
            difficulty = DifficultyLevel.MEDIUM,
            reward = TaskReward(xp = 30)
        ),
        TaskModel(
            id = "fin_tip_calculator",
            type = TaskType.LOGIC,
            title = "Tip Calculator",
            description = "Calculate a 15% tip on a restaurant bill",
            category = TaskCategory.LEARNING,
            difficulty = DifficultyLevel.MEDIUM,
            gameType = GameType.LOGIC_GAME,
            reward = TaskReward(xp = 25)
        ),
        TaskModel(
            id = "fin_budget_outing",
            type = TaskType.LEARNING,
            title = "Budget Planner",
            description = "Plan a family outing within a $50 budget",
            category = TaskCategory.LEARNING,
            difficulty = DifficultyLevel.HARD,
            reward = TaskReward(xp = 50)
        ),
        TaskModel(
            id = "fin_savings_tracker",
            type = TaskType.REAL_LIFE,
            title = "Savings Tracker",
            description = "Track your spending for a day and find one thing you could save on",
            category = TaskCategory.LEARNING,
            difficulty = DifficultyLevel.EASY,
            reward = TaskReward(xp = 20)
        )
    )
}
