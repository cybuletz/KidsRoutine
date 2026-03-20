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
import com.kidsroutine.feature.family.ui.ParentDashboardScreen
import com.kidsroutine.feature.family.ui.InviteChildrenScreen
import com.kidsroutine.feature.auth.ui.ChildLoginScreen
import com.kidsroutine.feature.auth.ui.ChildSignUpScreen
import com.kidsroutine.feature.family.ui.JoinFamilyScreen
import com.kidsroutine.feature.auth.ui.RoleSelectionScreen
import com.kidsroutine.feature.tasks.ui.CreateTaskScreen
import com.kidsroutine.feature.tasks.ui.TaskListScreen
import com.kidsroutine.feature.parent.ui.ParentPendingTasksScreen
import com.kidsroutine.feature.family.ui.ChildTaskProposalScreen


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
    object ParentLogin : AppScreen()
    object ParentSignUp : AppScreen()
    object ChildLogin : AppScreen()
    object ChildSignUp : AppScreen()
    object RoleSelection : AppScreen()
    data class FamilySetup(val user: UserModel) : AppScreen()
    data class JoinFamily(val user: UserModel) : AppScreen()
    data class ParentDashboard(val user: UserModel) : AppScreen()
    data class ChildProposal(val user: UserModel) : AppScreen()
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
                    user.role == Role.PARENT -> {
                        AppScreen.ParentDashboard(user)
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

        is AppScreen.ChildProposal -> {
            val user = (currentScreen as AppScreen.ChildProposal).user
            ChildTaskProposalScreen(
                currentUser = user,
                onProposalSuccess = {
                    currentScreen = AppScreen.MainApp(user)
                },
                onBackClick = {
                    currentScreen = AppScreen.MainApp(user)
                }
            )
        }

        is AppScreen.RoleSelection -> {
            RoleSelectionScreen(
                onParentSelected = {
                    currentScreen = AppScreen.ParentLogin
                },
                onChildSelected = {
                    currentScreen = AppScreen.ChildLogin
                }
            )
        }

        is AppScreen.ParentLogin -> {
            ParentLoginScreen(
                onLoginSuccess = { user ->
                    if (user.role == Role.PARENT && user.familyId.isEmpty()) {
                        currentScreen = AppScreen.FamilySetup(user)
                    } else {
                        currentScreen = AppScreen.ParentDashboard(user)
                    }
                },
                onSignUpClick = {
                    currentScreen = AppScreen.ParentSignUp
                }
            )
        }

        is AppScreen.ParentSignUp -> {
            ParentSignUpScreen(
                onSignUpSuccess = { user ->
                    currentScreen = AppScreen.FamilySetup(user)
                },
                onBackClick = {
                    currentScreen = AppScreen.ParentLogin
                }
            )
        }

        is AppScreen.ChildLogin -> {
            ChildLoginScreen(
                onLoginSuccess = { user ->
                    if (user.role == Role.CHILD && user.familyId.isEmpty()) {
                        currentScreen = AppScreen.JoinFamily(user)
                    } else {
                        currentScreen = AppScreen.MainApp(user)
                    }
                },
                onSignUpClick = {
                    currentScreen = AppScreen.ChildSignUp
                }
            )
        }

        is AppScreen.ChildSignUp -> {
            ChildSignUpScreen(
                onSignUpSuccess = { user ->
                    currentScreen = AppScreen.JoinFamily(user)
                },
                onBackClick = {
                    currentScreen = AppScreen.ChildLogin
                }
            )
        }

        is AppScreen.FamilySetup -> {
            val user = (currentScreen as AppScreen.FamilySetup).user
            FamilySetupScreen(
                currentUser = user,
                onFamilyCreated = { family ->
                    val updatedUser = user.copy(familyId = family.familyId)
                    currentScreen = AppScreen.ParentDashboard(updatedUser)
                }
            )
        }

        is AppScreen.JoinFamily -> {
            val user = (currentScreen as AppScreen.JoinFamily).user
            JoinFamilyScreen(
                currentUser = user,
                onJoinSuccess = { familyId ->
                    val updatedUser = user.copy(familyId = familyId)
                    currentScreen = AppScreen.MainApp(updatedUser)
                },
                onBackClick = {
                    currentScreen = AppScreen.ChildLogin
                }
            )
        }

        is AppScreen.ParentDashboard -> {
            val user = (currentScreen as AppScreen.ParentDashboard).user
            var showInviteScreen by remember { mutableStateOf(false) }
            var showTaskListScreen by remember { mutableStateOf(false) }
            var showCreateTaskScreen by remember { mutableStateOf(false) }
            var showPendingTasksScreen by remember { mutableStateOf(false) }

            when {
                showCreateTaskScreen -> {
                    CreateTaskScreen(
                        currentUser = user,
                        onTaskCreated = {
                            showCreateTaskScreen = false
                            showTaskListScreen = true
                        },
                        onBackClick = {
                            showCreateTaskScreen = false
                            showTaskListScreen = true
                        }
                    )
                }
                showTaskListScreen -> {
                    TaskListScreen(
                        currentUser = user,
                        onCreateTaskClick = {
                            showCreateTaskScreen = true
                        },
                        onBackClick = {
                            showTaskListScreen = false
                        }
                    )
                }
                showPendingTasksScreen -> {
                    ParentPendingTasksScreen(
                        currentUser = user,
                        onBackClick = {
                            showPendingTasksScreen = false
                        }
                    )
                }
                showInviteScreen -> {
                    InviteChildrenScreen(
                        currentUser = user,
                        onBackClick = { showInviteScreen = false }
                    )
                }
                else -> {
                    ParentDashboardScreen(
                        currentUser = user,
                        onInviteClick = { showInviteScreen = true },
                        onTasksClick = { showTaskListScreen = true },
                        onPendingClick = { showPendingTasksScreen = true },
                        onSettingsClick = {
                            // TODO: Navigate to settings
                        }
                    )
                }
            }
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