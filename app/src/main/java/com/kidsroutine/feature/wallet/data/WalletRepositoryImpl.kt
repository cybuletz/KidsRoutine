package com.kidsroutine.feature.wallet.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.FamilyWallet
import com.kidsroutine.core.model.SavingsGoal
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : WalletRepository {

    override suspend fun getWallet(familyId: String): FamilyWallet? {
        return try {
            val doc = firestore.collection("family_wallets").document(familyId).get().await()
            if (!doc.exists()) return null
            FamilyWallet(
                xpToMoneyRate = (doc.getDouble("xpToMoneyRate") ?: 0.01).toFloat(),
                currencySymbol = doc.getString("currencySymbol") ?: "$",
                isEnabled = doc.getBoolean("isEnabled") ?: true
            )
        } catch (e: Exception) {
            Log.e("WalletRepo", "getWallet error", e)
            null
        }
    }

    override suspend fun saveWallet(familyId: String, wallet: FamilyWallet) {
        try {
            firestore.collection("family_wallets").document(familyId).set(
                mapOf(
                    "xpToMoneyRate" to wallet.xpToMoneyRate,
                    "currencySymbol" to wallet.currencySymbol,
                    "isEnabled" to wallet.isEnabled
                )
            ).await()
        } catch (e: Exception) {
            Log.e("WalletRepo", "saveWallet error", e)
        }
    }

    override suspend fun getSavingsGoals(userId: String): List<SavingsGoal> {
        return try {
            val snap = firestore.collection("savings_goals")
                .whereEqualTo("userId", userId)
                .get().await()
            snap.documents.mapNotNull { doc ->
                try {
                    val target = (doc.getLong("targetXp") ?: 0).toInt()
                    val current = (doc.getLong("currentXp") ?: 0).toInt()
                    SavingsGoal(
                        goalId = doc.id,
                        userId = doc.getString("userId") ?: userId,
                        familyId = doc.getString("familyId") ?: "",
                        title = doc.getString("title") ?: "",
                        emoji = doc.getString("emoji") ?: "🎯",
                        targetXp = target,
                        currentXp = current,
                        targetMoneyValue = (doc.getDouble("targetMoneyValue") ?: 0.0).toFloat(),
                        isComplete = current >= target
                    )
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            Log.e("WalletRepo", "getSavingsGoals error", e)
            emptyList()
        }
    }

    override suspend fun saveSavingsGoal(goal: SavingsGoal) {
        try {
            val data = mapOf(
                "userId" to goal.userId,
                "familyId" to goal.familyId,
                "title" to goal.title,
                "emoji" to goal.emoji,
                "targetXp" to goal.targetXp,
                "currentXp" to goal.currentXp,
                "targetMoneyValue" to goal.targetMoneyValue,
                "isComplete" to goal.isComplete
            )
            if (goal.goalId.isBlank()) {
                firestore.collection("savings_goals").add(data).await()
            } else {
                firestore.collection("savings_goals").document(goal.goalId).set(data).await()
            }
        } catch (e: Exception) {
            Log.e("WalletRepo", "saveSavingsGoal error", e)
        }
    }

    override suspend fun deleteSavingsGoal(goalId: String) {
        try {
            firestore.collection("savings_goals").document(goalId).delete().await()
        } catch (e: Exception) {
            Log.e("WalletRepo", "deleteSavingsGoal error", e)
        }
    }

    override suspend fun contributeToGoal(goalId: String, xpAmount: Int) {
        try {
            val doc = firestore.collection("savings_goals").document(goalId).get().await()
            val current = (doc.getLong("currentXp") ?: 0).toInt()
            val target = (doc.getLong("targetXp") ?: 0).toInt()
            val newCurrent = (current + xpAmount).coerceAtMost(target)
            firestore.collection("savings_goals").document(goalId).update(
                mapOf(
                    "currentXp" to newCurrent,
                    "isComplete" to (newCurrent >= target)
                )
            ).await()
        } catch (e: Exception) {
            Log.e("WalletRepo", "contributeToGoal error", e)
        }
    }
}
