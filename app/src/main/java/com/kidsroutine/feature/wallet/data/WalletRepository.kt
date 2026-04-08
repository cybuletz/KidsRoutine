package com.kidsroutine.feature.wallet.data

import com.kidsroutine.core.model.FamilyWallet
import com.kidsroutine.core.model.SavingsGoal

interface WalletRepository {
    suspend fun getWallet(familyId: String): FamilyWallet?
    suspend fun saveWallet(familyId: String, wallet: FamilyWallet)
    suspend fun getSavingsGoals(userId: String): List<SavingsGoal>
    suspend fun saveSavingsGoal(goal: SavingsGoal)
    suspend fun deleteSavingsGoal(goalId: String)
    suspend fun contributeToGoal(goalId: String, xpAmount: Int)
}
