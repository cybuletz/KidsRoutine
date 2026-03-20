package com.kidsroutine.core.model

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val user: UserModel) : AuthState()
    data class Error(val message: String) : AuthState()
}