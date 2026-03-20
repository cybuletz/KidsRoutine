package com.kidsroutine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.common.designsystem.theme.KidsRoutineTheme
import com.kidsroutine.core.model.AuthState
import com.kidsroutine.core.model.Role
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.auth.ui.AuthViewModel
import com.kidsroutine.feature.auth.ui.ParentLoginScreen
import com.kidsroutine.feature.auth.ui.ParentSignUpScreen
import com.kidsroutine.feature.family.ui.FamilySetupScreen
import com.kidsroutine.navigation.KidsRoutineNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KidsRoutineTheme {
                MainContent()
            }
        }
    }
}

sealed class AppScreen {
    object Loading : AppScreen()
    object Login : AppScreen()
    object SignUp : AppScreen()
    data class FamilySetup(val user: UserModel) : AppScreen()
    data class MainApp(val user: UserModel) : AppScreen()
    data class Error(val message: String) : AppScreen()
}

@Composable
fun MainContent() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    var isSignUp by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Loading) }

    // Update screen based on auth state
    LaunchedEffect(authState) {
        currentScreen = when (authState) {
            is AuthState.Loading -> AppScreen.Loading
            is AuthState.Unauthenticated -> {
                if (isSignUp) AppScreen.SignUp else AppScreen.Login
            }
            is AuthState.Authenticated -> {
                val user = (authState as AuthState.Authenticated).user
                if (user.role == Role.PARENT && user.familyId.isEmpty()) {
                    AppScreen.FamilySetup(user)
                } else {
                    AppScreen.MainApp(user)
                }
            }
            is AuthState.Error -> {
                AppScreen.Error((authState as AuthState.Error).message)
            }
        }
    }

    // Render based on current screen
    when (currentScreen) {
        is AppScreen.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is AppScreen.Login -> {
            ParentLoginScreen(
                onLoginSuccess = { user ->
                    if (user.role == Role.PARENT && user.familyId.isEmpty()) {
                        currentScreen = AppScreen.FamilySetup(user)
                    } else {
                        currentScreen = AppScreen.MainApp(user)
                    }
                },
                onSignUpClick = {
                    isSignUp = true
                    currentScreen = AppScreen.SignUp
                }
            )
        }

        is AppScreen.SignUp -> {
            ParentSignUpScreen(
                onSignUpSuccess = { user ->
                    currentScreen = AppScreen.FamilySetup(user)
                },
                onBackClick = {
                    isSignUp = false
                    currentScreen = AppScreen.Login
                }
            )
        }

        is AppScreen.FamilySetup -> {
            val user = (currentScreen as AppScreen.FamilySetup).user
            FamilySetupScreen(
                currentUser = user,
                onFamilyCreated = { family ->
                    // Update user's familyId and navigate to main app
                    val updatedUser = user.copy(familyId = family.familyId)
                    currentScreen = AppScreen.MainApp(updatedUser)
                }
            )
        }

        is AppScreen.MainApp -> {
            val user = (currentScreen as AppScreen.MainApp).user
            KidsRoutineNavGraph(currentUser = user)
        }

        is AppScreen.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Auth Error", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    Text((currentScreen as AppScreen.Error).message)
                }
            }
        }
    }
}