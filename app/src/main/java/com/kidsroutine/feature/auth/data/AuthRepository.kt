package com.kidsroutine.feature.auth.data

import com.kidsroutine.core.model.UserModel

interface AuthRepository {
    suspend fun signInAnonymously(): UserModel
    suspend fun signInWithEmail(email: String, password: String): UserModel
    suspend fun signUpWithEmail(email: String, password: String, displayName: String, role: com.kidsroutine.core.model.Role): UserModel
    suspend fun signInWithGoogle(googleIdToken: String): UserModel
    suspend fun signOut()
    fun getCurrentUser(): UserModel?
}