package com.kidsroutine.feature.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.AuthState
import com.kidsroutine.core.model.Role
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.auth.domain.SignInAnonymouslyUseCase
import com.kidsroutine.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInAnonymously: SignInAnonymouslyUseCase,
    private val authRepository: AuthRepository,
    private val userDao: com.kidsroutine.core.database.dao.UserDao
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Log.d("AuthViewModel", "Existing session found for: ${currentUser.uid}")
            // Load user data from Firestore
            viewModelScope.launch {
                try {
                    val userDoc = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUser.uid)
                        .get()
                        .await()

                    if (userDoc.exists()) {
                        val user = UserModel(
                            userId = currentUser.uid,
                            role = Role.valueOf(
                                userDoc.data?.get("role") as? String ?: Role.CHILD.name
                            ),
                            familyId = userDoc.data?.get("familyId") as? String ?: "",
                            displayName = userDoc.data?.get("displayName") as? String ?: "",
                            email = userDoc.data?.get("email") as? String ?: currentUser.email
                            ?: "",
                            avatarUrl = userDoc.data?.get("avatarUrl") as? String ?: "",
                            isAdmin = userDoc.data?.get("isAdmin") as? Boolean ?: false,
                            xp = (userDoc.data?.get("xp") as? Number)?.toInt() ?: 0,
                            level = (userDoc.data?.get("level") as? Number)?.toInt() ?: 1,
                            streak = (userDoc.data?.get("streak") as? Number)?.toInt() ?: 0,
                            createdAt = (userDoc.data?.get("createdAt") as? Number)?.toLong() ?: 0L,
                            lastActiveAt = (userDoc.data?.get("lastActiveAt") as? Number)?.toLong()
                                ?: 0L
                        )
                        // Sync Firestore → Room so observeUser() always has fresh data
                        userDao.upsert(com.kidsroutine.core.database.entity.UserEntity(
                            userId      = user.userId,
                            role        = user.role.name,
                            familyId    = user.familyId,
                            displayName = user.displayName,
                            email       = user.email,
                            avatarUrl   = user.avatarUrl,
                            isAdmin     = user.isAdmin,
                            xp          = user.xp,
                            level       = user.level,
                            streak      = user.streak,
                            createdAt   = user.createdAt,
                            lastActiveAt = user.lastActiveAt
                        ))
                        Log.d("AuthViewModel", "Session restored for user: ${user.displayName}")
                        _authState.value = AuthState.Authenticated(user)
                    }
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error restoring session", e)
                    _authState.value = AuthState.Unauthenticated
                }
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

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