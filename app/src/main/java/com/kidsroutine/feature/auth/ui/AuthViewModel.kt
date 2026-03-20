package com.kidsroutine.feature.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.AuthState
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.auth.domain.SignInAnonymouslyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInAnonymously: SignInAnonymouslyUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        signIn()
    }

    private fun signIn() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val user = signInAnonymously()
                Log.d("AuthViewModel", "User signed in: ${user.userId}")
                _authState.value = AuthState.Authenticated(user)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign in failed", e)
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }
}