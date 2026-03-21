package com.kidsroutine

import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
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
import com.kidsroutine.feature.auth.ui.ChildLoginScreen
import com.kidsroutine.feature.auth.ui.ChildSignUpScreen
import com.kidsroutine.feature.family.ui.JoinFamilyScreen
import com.kidsroutine.feature.auth.ui.RoleSelectionScreen
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val NOTIFICATION_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }

        // Save FCM token when app starts
        saveFCMToken()

        enableEdgeToEdge()
        setContent {
            KidsRoutineTheme {
                MainContent()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PERMISSIONS", "Notification permission granted ✓")
            } else {
                Log.d("PERMISSIONS", "Notification permission denied ❌")
            }
        }
    }

    private fun saveFCMToken() {
        // Force refresh token
        FirebaseMessaging.getInstance().deleteToken()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", "New Token: $token")

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .update("fcmToken", token)
                        .addOnSuccessListener {
                            Log.d("FCM", "Token saved to Firestore ✓")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FCM", "Error saving token", e)
                        }
                }
            }
        }
    }
}

sealed class AppScreen {
    object Loading : AppScreen()
    object RoleSelection : AppScreen()
    object ParentLogin : AppScreen()
    object ParentSignUp : AppScreen()
    object ChildLogin : AppScreen()
    object ChildSignUp : AppScreen()
    data class FamilySetup(val user: UserModel) : AppScreen()
    data class JoinFamily(val user: UserModel) : AppScreen()
    data class MainApp(val user: UserModel) : AppScreen()
    data class Error(val message: String) : AppScreen()
}

@Composable
fun MainContent() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Loading) }

    // Update screen based on auth state
    LaunchedEffect(authState) {
        currentScreen = when (authState) {
            is AuthState.Loading -> AppScreen.Loading
            is AuthState.Unauthenticated -> AppScreen.RoleSelection
            is AuthState.Authenticated -> {
                val user = (authState as AuthState.Authenticated).user

                when {
                    user.role == Role.PARENT && user.familyId.isEmpty() -> {
                        AppScreen.FamilySetup(user)
                    }
                    user.role == Role.CHILD && user.familyId.isEmpty() -> {
                        AppScreen.JoinFamily(user)
                    }
                    else -> {
                        AppScreen.MainApp(user)
                    }
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

        is AppScreen.RoleSelection -> {
            RoleSelectionScreen(
                onParentSelected = { currentScreen = AppScreen.ParentLogin },
                onChildSelected = { currentScreen = AppScreen.ChildLogin }
            )
        }

        is AppScreen.ParentLogin -> {
            ParentLoginScreen(
                onLoginSuccess = { user ->
                    currentScreen = AppScreen.MainApp(user)
                },
                onSignUpClick = { currentScreen = AppScreen.ParentSignUp }
            )
        }

        is AppScreen.ParentSignUp -> {
            ParentSignUpScreen(
                onSignUpSuccess = { user ->
                    currentScreen = AppScreen.MainApp(user)
                },
                onBackClick = { currentScreen = AppScreen.ParentLogin }
            )
        }

        is AppScreen.ChildLogin -> {
            ChildLoginScreen(
                onLoginSuccess = { user ->
                    currentScreen = AppScreen.MainApp(user)
                },
                onSignUpClick = { currentScreen = AppScreen.ChildSignUp }
            )
        }

        is AppScreen.ChildSignUp -> {
            ChildSignUpScreen(
                onSignUpSuccess = { user ->
                    currentScreen = AppScreen.MainApp(user)
                },
                onBackClick = { currentScreen = AppScreen.ChildLogin }
            )
        }

        is AppScreen.FamilySetup -> {
            val user = (currentScreen as AppScreen.FamilySetup).user
            FamilySetupScreen(
                currentUser = user,
                onFamilyCreated = { _ ->
                    currentScreen = AppScreen.MainApp(user.copy(familyId = "created"))
                }
            )
        }

        is AppScreen.JoinFamily -> {
            val user = (currentScreen as AppScreen.JoinFamily).user
            JoinFamilyScreen(
                currentUser = user,
                onJoinSuccess = { familyId ->
                    currentScreen = AppScreen.MainApp(user.copy(familyId = familyId))
                },
                onBackClick = { currentScreen = AppScreen.RoleSelection }
            )
        }

        is AppScreen.MainApp -> {
            val user = (currentScreen as AppScreen.MainApp).user
            KidsRoutineNavGraph(user)
        }

        is AppScreen.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", style = MaterialTheme.typography.displayLarge)
                    Text((currentScreen as AppScreen.Error).message)
                }
            }
        }
    }
}