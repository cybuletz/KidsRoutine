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
            lastActiveAt = System.currentTimeMillis(),
            totalXpEarned = 0
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
                "isOnline" to true,
                "totalXpEarned" to 0
            ))
            .await()

        // ✅ ADD ONLY THIS: Create family/users reference
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
            val familyId = data["familyId"] as? String ?: ""

            // ✅ ADD ONLY THIS: Create family/users reference if familyId exists
            if (familyId.isNotEmpty()) {
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
            }

            val user = UserModel(
                userId = uid,
                role = Role.valueOf(data["role"] as? String ?: Role.CHILD.name),
                familyId = familyId,
                displayName = data["displayName"] as? String ?: "",
                email = data["email"] as? String ?: email,
                avatarUrl = data["avatarUrl"] as? String ?: "",
                isAdmin = data["isAdmin"] as? Boolean ?: false,
                xp = (data["xp"] as? Number)?.toInt() ?: 0,
                level = (data["level"] as? Number)?.toInt() ?: 1,
                streak = (data["streak"] as? Number)?.toInt() ?: 0,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                lastActiveAt = (data["lastActiveAt"] as? Number)?.toLong() ?: 0L,
                totalXpEarned = (data["totalXpEarned"] as? Number)?.toInt() ?: 0
            )
            userDao.upsert(UserEntity(
                userId = user.userId,
                role = user.role.name,
                familyId = user.familyId,
                displayName = user.displayName,
                email = user.email,
                avatarUrl = user.avatarUrl,
                isAdmin = user.isAdmin,
                xp = user.xp,
                level = user.level,
                streak = user.streak,
                createdAt = user.createdAt,
                lastActiveAt = user.lastActiveAt,
                totalXpEarned = user.totalXpEarned
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

        // Parents must create or join a family after signup — leave familyId empty
        val familyId = ""
        val now = System.currentTimeMillis()

        val userEntity = UserEntity(
            userId = uid,
            role = role.name,
            familyId = familyId,
            displayName = displayName,
            email = email,
            avatarUrl = "",
            isAdmin = false,
            xp = 0,
            level = 1,
            streak = 0,
            createdAt = now,
            lastActiveAt = now,
            totalXpEarned = 0
        )

        firestore.collection("users").document(uid)
            .set(mapOf(
                "userId" to uid,
                "role" to role.name,
                "familyId" to familyId,
                "displayName" to displayName,
                "email" to email,
                "avatarUrl" to "",
                "isAdmin" to false,
                "xp" to 0,
                "level" to 1,
                "streak" to 0,
                "createdAt" to now,
                "lastActiveAt" to now,
                "isOnline" to true,
                "totalXpEarned" to 0
            ))
            .await()

        // Setup presence listener
        setupPresenceListener(uid)

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
            // Existing user - mark as online
            firestore.collection("users").document(uid)
                .update("isOnline", true)
                .await()

            // Setup presence in Realtime Database
            val presenceRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("presence/$uid")
            presenceRef.onDisconnect().removeValue()
            presenceRef.setValue(true)

            Log.d("AuthRepository", "Google sign in - existing user $uid marked online")

            val data = userDoc.data ?: throw Exception("Invalid user data")
            val familyId = data["familyId"] as? String ?: ""

            // ✅ ADD ONLY THIS: Create family/users reference if familyId exists
            if (familyId.isNotEmpty()) {
                firestore.collection("families")
                    .document(familyId)
                    .collection("users")
                    .document(uid)
                    .set(
                        mapOf(
                            "userId" to uid,
                            "familyId" to familyId,
                            "joinedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
            }

            val user = UserModel(
                userId = uid,
                role = Role.valueOf(data["role"] as? String ?: Role.PARENT.name),
                familyId = familyId,
                displayName = data["displayName"] as? String ?: displayName,
                email = data["email"] as? String ?: googleEmail,
                avatarUrl = data["avatarUrl"] as? String ?: "",
                isAdmin = data["isAdmin"] as? Boolean ?: false,
                xp = (data["xp"] as? Number)?.toInt() ?: 0,
                level = (data["level"] as? Number)?.toInt() ?: 1,
                streak = (data["streak"] as? Number)?.toInt() ?: 0,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                lastActiveAt = (data["lastActiveAt"] as? Number)?.toLong() ?: 0L,
                totalXpEarned = (data["totalXpEarned"] as? Number)?.toInt() ?: 0
            )
            userDao.upsert(
                UserEntity(
                    userId = user.userId,
                    role = user.role.name,
                    familyId = user.familyId,
                    displayName = user.displayName,
                    email = user.email,
                    avatarUrl = user.avatarUrl,
                    isAdmin = user.isAdmin,
                    xp = user.xp,
                    level = user.level,
                    streak = user.streak,
                    createdAt = user.createdAt,
                    lastActiveAt = user.lastActiveAt,
                    totalXpEarned = user.totalXpEarned
                )
            )
            Log.d("AuthRepository", "Google sign in - existing user: $uid")
            user
        } else {
            // New user - create as PARENT with EMPTY familyId and mark as online
            val now = System.currentTimeMillis()
            val userEntity = UserEntity(
                userId = uid,
                role = Role.PARENT.name,
                familyId = "",  // Parent must create family
                displayName = displayName,
                email = googleEmail,
                avatarUrl = "",
                isAdmin = false,
                xp = 0,
                level = 1,
                streak = 0,
                createdAt = now,
                lastActiveAt = now,
                totalXpEarned = 0
            )

            firestore.collection("users").document(uid)
                .set(mapOf(
                    "userId" to uid,
                    "role" to Role.PARENT.name,
                    "familyId" to "",
                    "displayName" to displayName,
                    "email" to googleEmail,
                    "avatarUrl" to "",
                    "isAdmin" to false,
                    "xp" to 0,
                    "level" to 1,
                    "streak" to 0,
                    "createdAt" to now,
                    "lastActiveAt" to now,
                    "isOnline" to true,
                    "totalXpEarned" to 0
                ))
                .await()

            // Setup presence in Realtime Database
            val presenceRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("presence/$uid")
            presenceRef.onDisconnect().removeValue()
            presenceRef.setValue(true)

            Log.d("AuthRepository", "New Google user created: $uid with presence listener")

            userDao.upsert(userEntity)
            Log.d("AuthRepository", "New Google user created: $uid")

            // ✅ NO FAMILY CREATION FOR NEW GOOGLE USERS - They have empty familyId
            // They must go through family setup screen first

            userEntity.toUserModel()
        }
    }

    private fun setupPresenceListener(userId: String) {
        val userRef = firestore.collection("users").document(userId)

        // When connection is lost, set isOnline to false
        userRef.update("isOnline", true)
            .addOnSuccessListener {
                Log.d("AuthRepository", "User $userId marked as online")
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
        lastActiveAt = lastActiveAt,
        totalXpEarned = totalXpEarned
    )
}