package com.kidsroutine.feature.daily.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.database.dao.UserDao
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.core.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun updateUserXp(userId: String, xpGained: Int) {
        try {
            Log.d("UserRepository", ">>> START updateUserXp: userId=$userId, xpGained=$xpGained")

            val userRef = firestore.collection("users").document(userId)

            // ── Firestore is the single source of truth ──
            // Atomic increment: always update spendable `xp`
            // Only increment `totalXpEarned` when earning (positive), never decrement it
            val updates = mutableMapOf<String, Any>(
                "xp" to FieldValue.increment(xpGained.toLong())
            )
            if (xpGained > 0) {
                updates["totalXpEarned"] = FieldValue.increment(xpGained.toLong())
            }
            userRef.update(updates).await()
            Log.d("UserRepository", "✓ Firestore atomic update successful: xpGained=$xpGained")

            // Read back authoritative values from Firestore
            val snapshot = userRef.get().await()
            val newXp = (snapshot.getLong("xp") ?: 0).toInt()
            val newTotalXpEarned = (snapshot.getLong("totalXpEarned") ?: 0).toInt()
            Log.d("UserRepository", "✓ Read back from Firestore: xp=$newXp, totalXpEarned=$newTotalXpEarned")

            // Sync Firestore → Room (local cache)
            val currentUser = userDao.getUserSync(userId)
            if (currentUser != null) {
                userDao.upsert(currentUser.copy(xp = newXp, totalXpEarned = newTotalXpEarned))
                Log.d("UserRepository", "✓ Synced to Room: xp=$newXp, totalXpEarned=$newTotalXpEarned")
            } else {
                Log.e("UserRepository", "ERROR: User not found in local DB: $userId")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "FATAL ERROR in updateUserXp", e)
            e.printStackTrace()
        }
    }

    override fun observeUser(userId: String): Flow<UserModel> {
        Log.d("UserRepository", "observeUser called for userId=$userId")
        return userDao.getUser(userId)
            .filterNotNull()
            .map { entity ->
                Log.d("UserRepository", "observeUser emitting: xp=${entity.xp}, totalXpEarned=${entity.totalXpEarned}")
                UserModel(
                    userId = entity.userId,
                    role = Role.valueOf(entity.role),
                    familyId = entity.familyId,
                    displayName = entity.displayName,
                    xp = entity.xp,
                    level = entity.level,
                    streak = entity.streak,
                    lastActiveAt = entity.lastActiveAt,
                    totalXpEarned = entity.totalXpEarned
                )
            }
    }
}