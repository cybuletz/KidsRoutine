package com.kidsroutine.feature.daily.data

import android.util.Log
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

            // Get current user from local database
            val currentUser = userDao.getUserSync(userId)
            Log.d("UserRepository", "getCurrentUser result: $currentUser")

            if (currentUser != null) {
                Log.d("UserRepository", "Found user in local DB: ${currentUser.displayName}, current xp=${currentUser.xp}")

                // Add XP locally
                val newXp = currentUser.xp + xpGained
                val updatedUser = currentUser.copy(xp = newXp)
                Log.d("UserRepository", "Created updated user entity: xp=$newXp")

                userDao.upsert(updatedUser)
                Log.d("UserRepository", "✓ Updated local Room DB: new XP = $newXp")

                // Sync to Firestore
                Log.d("UserRepository", "Syncing to Firestore: users/$userId with xp=$newXp")
                try {
                    firestore.collection("users").document(userId)
                        .update("xp", newXp)
                        .await()
                    Log.d("UserRepository", "✓ Firestore update successful!")
                } catch (updateError: Exception) {
                    Log.w("UserRepository", "Update failed, trying set with merge...", updateError)
                    firestore.collection("users").document(userId)
                        .set(mapOf("xp" to newXp), com.google.firebase.firestore.SetOptions.merge())
                        .await()
                    Log.d("UserRepository", "✓ Firestore set successful!")
                }
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
                Log.d("UserRepository", "observeUser emitting: xp=${entity.xp}")
                UserModel(
                    userId = entity.userId,
                    role = Role.valueOf(entity.role),
                    familyId = entity.familyId,
                    displayName = entity.displayName,
                    xp = entity.xp,
                    level = entity.level,
                    streak = entity.streak,
                    lastActiveAt = entity.lastActiveAt
                )
            }
    }
}