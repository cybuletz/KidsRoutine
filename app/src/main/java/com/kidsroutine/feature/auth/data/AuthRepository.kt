package com.kidsroutine.feature.auth.data

import com.kidsroutine.core.model.UserModel
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<UserModel?>
    suspend fun signInAnonymously(): UserModel
    suspend fun signOut()
}