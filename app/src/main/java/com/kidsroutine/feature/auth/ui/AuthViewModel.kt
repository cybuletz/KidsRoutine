package com.kidsroutine.feature.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.AuthState
import com.kidsroutine.core.model.Role
import com.kidsroutine.feature.auth.domain.SignInAnonymouslyUseCase
import com.kidsroutine.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInAnonymously: SignInAnonymouslyUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signInWithEmail(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val user = authRepository.signInWithEmail(email, password)
                Log.d("AuthViewModel", "Email sign in success: ${user.userId}")
                _authState.value = AuthState.Authenticated(user)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email sign in failed", e)
                _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, displayName: String, role: Role) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val user = authRepository.signUpWithEmail(email, password, displayName, role)
                Log.d("AuthViewModel", "Sign up success: ${user.userId}")
                _authState.value = AuthState.Authenticated(user)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign up failed", e)
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun signInWithGoogle(googleIdToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val user = authRepository.signInWithGoogle(googleIdToken)
                Log.d("AuthViewModel", "Google sign in success: ${user.userId}")
                _authState.value = AuthState.Authenticated(user)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google sign in failed", e)
                _authState.value = AuthState.Error(e.message ?: "Google sign in failed")
            }
        }
    }

    fun signInAnonymouslyClick() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val user = signInAnonymously()
                Log.d("AuthViewModel", "Anonymous sign in success: ${user.userId}")
                _authState.value = AuthState.Authenticated(user)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Anonymous sign in failed", e)
                _authState.value = AuthState.Error(e.message ?: "Anonymous sign in failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }
}