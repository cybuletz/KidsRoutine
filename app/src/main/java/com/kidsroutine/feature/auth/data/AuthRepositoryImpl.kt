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
            email = "",
            avatarUrl = "",
            isAdmin = false,
            xp = 0,
            level = 1,
            streak = 0,
            createdAt = System.currentTimeMillis(),
            lastActiveAt = System.currentTimeMillis()
        )

        userDao.upsert(userEntity)

        firestore.collection("users").document(uid)
            .set(mapOf(
                "userId" to uid,
                "role" to Role.CHILD.name,
                "familyId" to "family_$uid",
                "displayName" to "Child",
                "email" to "",
                "avatarUrl" to "",
                "isAdmin" to false,
                "xp" to 0,
                "level" to 1,
                "streak" to 0,
                "createdAt" to System.currentTimeMillis(),
                "lastActiveAt" to System.currentTimeMillis(),
                "isOnline" to true
            ))
            .await()

        // ✅ ADD THIS: Create user reference in family
        firestore.collection("families")
            .document("family_$uid")
            .collection("users")
            .document(uid)
            .set(mapOf(
                "userId" to uid,
                "familyId" to "family_$uid",
                "joinedAt" to System.currentTimeMillis()
            ))
            .await()

        Log.d("AuthRepository", "Anonymous user initialized")
        return userEntity.toUserModel()
    }

    private fun setupPresenceListener(userId: String) {
        val userRef = firestore.collection("users").document(userId)

        // When connection is lost, set isOnline to false
        userRef.update("isOnline", true)
            .addOnSuccessListener {
                Log.d("AuthRepository", "User $userId marked as online")
            }
    }

    override suspend fun signInWithEmail(email: String, password: String): UserModel {
        Log.d("AuthRepository", "Signing in with email: $email")

        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("Auth failed")

        // Set user as online in Firestore
        firestore.collection("users").document(uid)
            .update("isOnline", true)
            .await()

        // Setup presence in Realtime Database (auto-disconnects on network loss)
        val presenceRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("presence/$uid")
        presenceRef.onDisconnect().removeValue()
        presenceRef.setValue(true)

        Log.d("AuthRepository", "User $uid marked as online with presence listener")

        // Get or create user
        val userDoc = firestore.collection("users").document(uid).get().await()

        return if (userDoc.exists()) {
            // Existing user
            val data = userDoc.data ?: throw Exception("Invalid user data")
            val familyId = data["familyId"] as? String ?: "family_$uid"

            UserModel(
                userId = uid,
                role = Role.valueOf(data["role"] as? String ?: Role.CHILD.name),
                familyId = familyId,
                displayName = data["displayName"] as? String ?: "User",
                email = data["email"] as? String ?: "",
                avatarUrl = data["avatarUrl"] as? String ?: "",
                isAdmin = data["isAdmin"] as? Boolean ?: false,
                xp = (data["xp"] as? Number)?.toInt() ?: 0,
                level = (data["level"] as? Number)?.toInt() ?: 1,
                streak = (data["streak"] as? Number)?.toInt() ?: 0,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                lastActiveAt = (data["lastActiveAt"] as? Number)?.toLong() ?: 0L
            )
        } else {
            throw Exception("User not found")
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String, displayName: String, role: Role): UserModel {
        Log.d("AuthRepository", "Signing up with email: $email, role: $role")

        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("Auth failed")

        Log.d("AuthRepository", "User created: $uid")

        // Determine family ID based on role
        val familyId = if (role == Role.PARENT) {
            "family_$uid"
        } else {
            // Child needs to join existing family - use a temp one for now
            "family_$uid"
        }

        val userEntity = UserEntity(
            userId = uid,
            role = role.name,
            familyId = familyId,
            displayName = displayName,
            email = email,
            avatarUrl = "",
            isAdmin = (role == Role.PARENT),
            xp = 0,
            level = 1,
            streak = 0,
            createdAt = System.currentTimeMillis(),
            lastActiveAt = System.currentTimeMillis()
        )

        userDao.upsert(userEntity)

        firestore.collection("users").document(uid)
            .set(mapOf(
                "userId" to uid,
                "role" to role.name,
                "familyId" to familyId,
                "displayName" to displayName,
                "email" to email,
                "avatarUrl" to "",
                "isAdmin" to (role == Role.PARENT),
                "xp" to 0,
                "level" to 1,
                "streak" to 0,
                "createdAt" to System.currentTimeMillis(),
                "lastActiveAt" to System.currentTimeMillis(),
                "isOnline" to true
            ))
            .await()

        // ✅ ADD THIS: Create user reference in family
        firestore.collection("families")
            .document(familyId)
            .collection("users")
            .document(uid)
            .set(mapOf(
                "userId" to uid,
                "familyId" to familyId,
                "joinedAt" to System.currentTimeMillis()
            ))
            .await()

        Log.d("AuthRepository", "User signed up and family reference created")
        return userEntity.toUserModel()
    }

    override suspend fun signInWithGoogle(googleIdToken: String): UserModel {
        Log.d("AuthRepository", "Signing in with Google")

        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val uid = result.user?.uid ?: throw Exception("Auth failed")

        val userDoc = firestore.collection("users").document(uid).get().await()

        return if (userDoc.exists()) {
            // Existing user - just update online status
            val data = userDoc.data ?: throw Exception("Invalid user data")
            val familyId = data["familyId"] as? String ?: "family_$uid"

            firestore.collection("users").document(uid)
                .update("isOnline", true)
                .await()

            Log.d("AuthRepository", "Existing Google user signed in: $uid")

            UserModel(
                userId = uid,
                role = Role.valueOf(data["role"] as? String ?: Role.CHILD.name),
                familyId = familyId,
                displayName = data["displayName"] as? String ?: "User",
                email = data["email"] as? String ?: "",
                avatarUrl = data["avatarUrl"] as? String ?: "",
                isAdmin = data["isAdmin"] as? Boolean ?: false,
                xp = (data["xp"] as? Number)?.toInt() ?: 0,
                level = (data["level"] as? Number)?.toInt() ?: 1,
                streak = (data["streak"] as? Number)?.toInt() ?: 0,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                lastActiveAt = (data["lastActiveAt"] as? Number)?.toLong() ?: 0L
            )
        } else {
            // New user
            val email = result.user?.email ?: "unknown@google.com"
            val displayName = result.user?.displayName ?: "Google User"
            val familyId = "family_$uid"

            val userEntity = UserEntity(
                userId = uid,
                role = Role.CHILD.name,
                familyId = familyId,
                displayName = displayName,
                email = email,
                avatarUrl = result.user?.photoUrl?.toString() ?: "",
                isAdmin = false,
                xp = 0,
                level = 1,
                streak = 0,
                createdAt = System.currentTimeMillis(),
                lastActiveAt = System.currentTimeMillis()
            )

            userDao.upsert(userEntity)

            firestore.collection("users").document(uid)
                .set(mapOf(
                    "userId" to uid,
                    "role" to Role.CHILD.name.toString(),
                    "familyId" to familyId,
                    "displayName" to displayName,
                    "email" to email,
                    "avatarUrl" to (result.user?.photoUrl?.toString() ?: ""),
                    "isAdmin" to false,
                    "xp" to 0,
                    "level" to 1,
                    "streak" to 0,
                    "createdAt" to System.currentTimeMillis(),
                    "lastActiveAt" to System.currentTimeMillis(),
                    "isOnline" to true
                ))
                .await()

            // ✅ ADD THIS: Create user reference in family
            firestore.collection("families")
                .document(familyId)
                .collection("users")
                .document(uid)
                .set(mapOf(
                    "userId" to uid,
                    "familyId" to familyId,
                    "joinedAt" to System.currentTimeMillis()
                ))
                .await()

            Log.d("AuthRepository", "New Google user created and family reference set: $uid")
            return userEntity.toUserModel()
        }
    }

    override suspend fun signOut() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            try {
                // Set user as offline in Firestore
                firestore.collection("users").document(userId)
                    .update("isOnline", false)
                    .await()

                // Remove from Realtime Database presence
                com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("presence/$userId")
                    .removeValue()
                    .await()

                Log.d("AuthRepository", "User $userId marked as offline")
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error setting offline status", e)
            }
        }
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