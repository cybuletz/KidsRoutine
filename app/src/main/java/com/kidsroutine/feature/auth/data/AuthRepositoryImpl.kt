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
            email = "",  // ADD THIS
            avatarUrl = "",  // ADD THIS
            isAdmin = false,  // ADD THIS
            xp = 0,
            level = 1,
            streak = 0,
            createdAt = System.currentTimeMillis(),  // ADD THIS
            lastActiveAt = System.currentTimeMillis()
        )

        userDao.upsert(userEntity)

        firestore.collection("users").document(uid)
            .set(mapOf(
                "userId" to uid,
                "role" to Role.CHILD.name,
                "familyId" to "family_$uid",
                "displayName" to "Child",
                "email" to "",  // ADD THIS
                "avatarUrl" to "",  // ADD THIS
                "isAdmin" to false,  // ADD THIS
                "xp" to 0,
                "level" to 1,
                "streak" to 0,
                "createdAt" to System.currentTimeMillis(),  // ADD THIS
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
                email = data["email"] as? String ?: email,  // ADD THIS
                avatarUrl = data["avatarUrl"] as? String ?: "",  // ADD THIS
                isAdmin = data["isAdmin"] as? Boolean ?: false,  // ADD THIS
                xp = (data["xp"] as? Number)?.toInt() ?: 0,
                level = (data["level"] as? Number)?.toInt() ?: 1,
                streak = (data["streak"] as? Number)?.toInt() ?: 0,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,  // ADD THIS
                lastActiveAt = (data["lastActiveAt"] as? Number)?.toLong() ?: 0L
            )
            userDao.upsert(UserEntity(
                userId = user.userId,
                role = user.role.name,
                familyId = user.familyId,
                displayName = user.displayName,
                email = user.email,  // ADD THIS
                avatarUrl = user.avatarUrl,  // ADD THIS
                isAdmin = user.isAdmin,  // ADD THIS
                xp = user.xp,
                level = user.level,
                streak = user.streak,
                createdAt = user.createdAt,  // ADD THIS
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
        val now = System.currentTimeMillis()

        val userEntity = UserEntity(
            userId = uid,
            role = role.name,
            familyId = familyId,
            displayName = displayName,
            email = email,  // ADD THIS
            avatarUrl = "",  // ADD THIS
            isAdmin = false,  // ADD THIS
            xp = 0,
            level = 1,
            streak = 0,
            createdAt = now,  // ADD THIS
            lastActiveAt = now
        )

        firestore.collection("users").document(uid)
            .set(mapOf(
                "userId" to uid,
                "role" to role.name,
                "familyId" to familyId,
                "displayName" to displayName,
                "email" to email,  // ADD THIS
                "avatarUrl" to "",  // ADD THIS
                "isAdmin" to false,  // ADD THIS
                "xp" to 0,
                "level" to 1,
                "streak" to 0,
                "createdAt" to now,  // ADD THIS
                "lastActiveAt" to now
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
        val googleEmail = result.user?.email ?: ""

        val userDoc = firestore.collection("users").document(uid).get().await()

        return if (userDoc.exists()) {
            val data = userDoc.data ?: throw Exception("Invalid user data")
            val user = UserModel(
                userId = uid,
                role = Role.valueOf(data["role"] as? String ?: Role.PARENT.name),
                familyId = data["familyId"] as? String ?: "",
                displayName = data["displayName"] as? String ?: displayName,
                email = data["email"] as? String ?: googleEmail,  // ADD THIS
                avatarUrl = data["avatarUrl"] as? String ?: "",  // ADD THIS
                isAdmin = data["isAdmin"] as? Boolean ?: false,  // ADD THIS
                xp = (data["xp"] as? Number)?.toInt() ?: 0,
                level = (data["level"] as? Number)?.toInt() ?: 1,
                streak = (data["streak"] as? Number)?.toInt() ?: 0,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,  // ADD THIS
                lastActiveAt = (data["lastActiveAt"] as? Number)?.toLong() ?: 0L
            )
            userDao.upsert(UserEntity(
                userId = user.userId,
                role = user.role.name,
                familyId = user.familyId,
                displayName = user.displayName,
                email = user.email,  // ADD THIS
                avatarUrl = user.avatarUrl,  // ADD THIS
                isAdmin = user.isAdmin,  // ADD THIS
                xp = user.xp,
                level = user.level,
                streak = user.streak,
                createdAt = user.createdAt,  // ADD THIS
                lastActiveAt = user.lastActiveAt
            ))
            Log.d("AuthRepository", "Google sign in - existing user: $uid")
            user
        } else {
            // New user - create as PARENT with EMPTY familyId
            val now = System.currentTimeMillis()
            val userEntity = UserEntity(
                userId = uid,
                role = Role.PARENT.name,
                familyId = "",  // Parent must create family
                displayName = displayName,
                email = googleEmail,  // ADD THIS
                avatarUrl = "",  // ADD THIS
                isAdmin = false,  // ADD THIS
                xp = 0,
                level = 1,
                streak = 0,
                createdAt = now,  // ADD THIS
                lastActiveAt = now
            )

            firestore.collection("users").document(uid)
                .set(mapOf(
                    "userId" to uid,
                    "role" to Role.PARENT.name,
                    "familyId" to "",
                    "displayName" to displayName,
                    "email" to googleEmail,  // ADD THIS
                    "avatarUrl" to "",  // ADD THIS
                    "isAdmin" to false,  // ADD THIS
                    "xp" to 0,
                    "level" to 1,
                    "streak" to 0,
                    "createdAt" to now,  // ADD THIS
                    "lastActiveAt" to now
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
        email = email,  // ADD THIS
        avatarUrl = avatarUrl,  // ADD THIS
        isAdmin = isAdmin,  // ADD THIS
        xp = xp,
        level = level,
        streak = streak,
        createdAt = createdAt,  // ADD THIS
        lastActiveAt = lastActiveAt
    )
}