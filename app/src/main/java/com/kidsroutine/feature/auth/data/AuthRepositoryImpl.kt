package com.kidsroutine.feature.auth.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.database.dao.UserDao
import com.kidsroutine.core.database.entity.UserEntity
import com.kidsroutine.core.model.Role
import com.kidsroutine.core.model.UserModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun signInAnonymously(): UserModel {
        val result = firebaseAuth.signInAnonymously().await()
        val uid = result.user?.uid ?: throw Exception("Auth failed")

        Log.d("AuthRepository", "Signed in anonymously: $uid")

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

        userDao.upsert(userEntity)

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

        Log.d("AuthRepository", "Anonymous user initialized")
        return userEntity.toUserModel()
    }

    override suspend fun signInWithEmail(email: String, password: String): UserModel {
        Log.d("AuthRepository", "Signing in with email: $email")

        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("Auth failed")

        // Get or create user
        val userDoc = firestore.collection("users").document(uid).get().await()

        return if (userDoc.exists()) {
            // Existing user
            val data = userDoc.data ?: throw Exception("Invalid user data")
            val user = UserModel(
                userId = uid,
                role = Role.valueOf(data["role"] as? String ?: Role.CHILD.name),
                familyId = data["familyId"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "",
                xp = (data["xp"] as? Number)?.toInt() ?: 0,
                level = (data["level"] as? Number)?.toInt() ?: 1,
                streak = (data["streak"] as? Number)?.toInt() ?: 0,
                lastActiveAt = (data["lastActiveAt"] as? Number)?.toLong() ?: 0L
            )
            userDao.upsert(UserEntity(
                userId = user.userId,
                role = user.role.name,
                familyId = user.familyId,
                displayName = user.displayName,
                xp = user.xp,
                level = user.level,
                streak = user.streak,
                lastActiveAt = user.lastActiveAt
            ))
            Log.d("AuthRepository", "Signed in existing user: $uid")
            user
        } else {
            throw Exception("User not found")
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        role: Role
    ): UserModel {
        Log.d("AuthRepository", "Signing up with email: $email, role=$role")

        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("Auth failed")

        val familyId = if (role == Role.PARENT) "family_$uid" else ""

        val userEntity = UserEntity(
            userId = uid,
            role = role.name,
            familyId = familyId,
            displayName = displayName,
            xp = 0,
            level = 1,
            streak = 0,
            lastActiveAt = System.currentTimeMillis()
        )

        firestore.collection("users").document(uid)
            .set(mapOf(
                "userId" to uid,
                "role" to role.name,
                "familyId" to familyId,
                "displayName" to displayName,
                "xp" to 0,
                "level" to 1,
                "streak" to 0,
                "lastActiveAt" to System.currentTimeMillis(),
                "createdAt" to System.currentTimeMillis()
            ))
            .await()

        userDao.upsert(userEntity)
        Log.d("AuthRepository", "New user created: $uid with role=$role")

        return userEntity.toUserModel()
    }

    override suspend fun signInWithGoogle(googleIdToken: String): UserModel {
        Log.d("AuthRepository", "Signing in with Google")

        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val uid = result.user?.uid ?: throw Exception("Auth failed")
        val displayName = result.user?.displayName ?: "User"

        val userDoc = firestore.collection("users").document(uid).get().await()

        return if (userDoc.exists()) {
            val data = userDoc.data ?: throw Exception("Invalid user data")
            val user = UserModel(
                userId = uid,
                role = Role.valueOf(data["role"] as? String ?: Role.PARENT.name),
                familyId = data["familyId"] as? String ?: "",
                displayName = data["displayName"] as? String ?: displayName,
                xp = (data["xp"] as? Number)?.toInt() ?: 0,
                level = (data["level"] as? Number)?.toInt() ?: 1,
                streak = (data["streak"] as? Number)?.toInt() ?: 0,
                lastActiveAt = (data["lastActiveAt"] as? Number)?.toLong() ?: 0L
            )
            userDao.upsert(UserEntity(
                userId = user.userId,
                role = user.role.name,
                familyId = user.familyId,
                displayName = user.displayName,
                xp = user.xp,
                level = user.level,
                streak = user.streak,
                lastActiveAt = user.lastActiveAt
            ))
            Log.d("AuthRepository", "Google sign in - existing user: $uid")
            user
        } else {
            val familyId = "family_$uid"
            val userEntity = UserEntity(
                userId = uid,
                role = Role.PARENT.name,
                familyId = familyId,
                displayName = displayName,
                xp = 0,
                level = 1,
                streak = 0,
                lastActiveAt = System.currentTimeMillis()
            )

            firestore.collection("users").document(uid)
                .set(mapOf(
                    "userId" to uid,
                    "role" to Role.PARENT.name,
                    "familyId" to familyId,
                    "displayName" to displayName,
                    "xp" to 0,
                    "level" to 1,
                    "streak" to 0,
                    "lastActiveAt" to System.currentTimeMillis(),
                    "createdAt" to System.currentTimeMillis()
                ))
                .await()

            userDao.upsert(userEntity)
            Log.d("AuthRepository", "New Google user created: $uid")
            userEntity.toUserModel()
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        Log.d("AuthRepository", "Signed out")
    }

    override fun getCurrentUser(): UserModel? {
        return null // Will implement later
    }

    private fun UserEntity.toUserModel() = UserModel(
        userId = userId,
        role = Role.valueOf(role),
        familyId = familyId,
        displayName = displayName,
        xp = xp,
        level = level,
        streak = streak,
        lastActiveAt = lastActiveAt
    )
}