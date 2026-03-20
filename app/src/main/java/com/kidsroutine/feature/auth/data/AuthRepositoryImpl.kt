package com.kidsroutine.feature.auth.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.database.dao.UserDao
import com.kidsroutine.core.database.entity.UserEntity
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.core.model.Role
import com.kidsroutine.core.network.safeFirestoreCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AuthRepository {

    override fun observeAuthState(): Flow<UserModel?> {
        return userDao.getUser(getCurrentUserId() ?: "")
            .filterNotNull()
            .map { entity ->
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

    override suspend fun signInAnonymously(): UserModel {
        val result = firebaseAuth.signInAnonymously().await()
        val uid = result.user?.uid ?: throw Exception("Auth failed")

        Log.d("AuthRepository", "Signed in anonymously: $uid")

        // Create user data
        val userEntity = UserEntity(
            userId = uid,
            role = Role.CHILD.name,
            familyId = "family_$uid",
            displayName = "Child",
            xp = 0,
            level = 1,
            streak = 0,
            lastActiveAt = System.currentTimeMillis()
        )

        // 1. Save to Room database
        userDao.upsert(userEntity)
        Log.d("AuthRepository", "User saved to Room database")

        // 2. Initialize in Firestore with full data
        safeFirestoreCall {
            firestore.collection("users").document(uid)
                .set(mapOf(
                    "userId" to uid,
                    "role" to Role.CHILD.name,
                    "familyId" to "family_$uid",
                    "displayName" to "Child",
                    "xp" to 0,
                    "level" to 1,
                    "streak" to 0,
                    "lastActiveAt" to System.currentTimeMillis()
                ))
                .await()
            Log.d("AuthRepository", "User initialized in Firestore")
        }

        return UserModel(
            userId = uid,
            role = Role.CHILD,
            familyId = "family_$uid",
            displayName = "Child",
            xp = 0,
            level = 1,
            streak = 0,
            lastActiveAt = System.currentTimeMillis()
        )
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        Log.d("AuthRepository", "Signed out")
    }

    private fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}