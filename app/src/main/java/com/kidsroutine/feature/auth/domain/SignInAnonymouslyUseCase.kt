package com.kidsroutine.feature.auth.domain

import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.auth.data.AuthRepository
import javax.inject.Inject

class SignInAnonymouslyUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): UserModel {
        return authRepository.signInAnonymously()
    }
}