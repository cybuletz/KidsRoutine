package com.kidsroutine.navigation

import android.util.Log
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.avatar.ui.AvatarCustomizationScreen
import com.kidsroutine.feature.avatar.ui.AvatarShopScreen
import com.kidsroutine.feature.billing.ContentPacksScreen
import com.kidsroutine.feature.billing.ContentPacksViewModel
import com.kidsroutine.feature.billing.UpgradeScreen
import com.kidsroutine.feature.challenges.ui.ActiveChallengesScreen
import com.kidsroutine.feature.challenges.ui.ChallengeDetailScreen
import com.kidsroutine.feature.challenges.ui.StartChallengesScreen
import com.kidsroutine.feature.community.ui.LeaderboardScreen
import com.kidsroutine.feature.community.ui.MarketplaceScreen
import com.kidsroutine.feature.community.ui.ModerationScreen
import com.kidsroutine.feature.community.ui.PublishScreen
import com.kidsroutine.feature.family.ui.FamilyMessagingScreen
import com.kidsroutine.feature.family.ui.InviteChildrenScreen
import com.kidsroutine.feature.family.ui.ParentDashboardScreen
import com.kidsroutine.feature.family.ui.ParentFamilyEntryScreen
import com.kidsroutine.feature.generation.ui.DailyPlanScreen
import com.kidsroutine.feature.generation.ui.GenerationScreen
import com.kidsroutine.feature.generation.ui.WeeklyPlanScreen
import com.kidsroutine.feature.notifications.ui.NotificationsScreen
import com.kidsroutine.feature.parent.ui.ParentPendingTasksScreen
import com.kidsroutine.feature.parent.ui.ParentPrivilegeApprovalsScreen
import com.kidsroutine.feature.profile.ui.ChildProfileScreen
import com.kidsroutine.feature.profile.ui.ParentProfileScreen
import com.kidsroutine.feature.stats.ui.StatsScreen
import com.kidsroutine.feature.tasks.ui.CreateTaskScreen
import com.kidsroutine.feature.tasks.ui.SelectChildrenScreen
import com.kidsroutine.feature.tasks.ui.TaskListScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kidsroutine.feature.avatar.ui.AvatarShopViewModel
import com.kidsroutine.feature.tasks.ui.TaskDetailsScreen
import com.kidsroutine.feature.tasks.ui.TaskManagementViewModel

fun NavGraphBuilder.parentNavGraph(
    currentUser: UserModel,
    familyMembers: List<UserModel>,
    navController: NavController,
    onSignOut: () -> Unit,
    onSwitchToChild: (UserModel) -> Unit = {}
) {
    navigation(
        route            = "parent_graph",
        startDestination = Routes.PARENT_DASHBOARD
    ) {

        composable(Routes.PARENT_DASHBOARD) {
            if (currentUser.familyId.isEmpty()) {
                // currentUser is now liveUser (reactive) — this will
                // automatically recompose to the else branch once Firestore
                // updates familyId after FamilySetupScreen / JoinFamilyScreen completes
                ParentFamilyEntryScreen(
                    currentUser = currentUser,
                    onFamilySet = { /* no-op: liveUser recompose handles the transition */ }
                )
            } else {
                ParentDashboardScreen(
                    currentUser            = currentUser,
                    familyMembers          = familyMembers,
                    onFamilyMessagingClick = { navController.navigate(Routes.FAMILY_MESSAGING) },
                    onUpgradeClick         = { navController.navigate(Routes.UPGRADE) },
                    onContentPacksClick    = { navController.navigate(Routes.CONTENT_PACKS) },
                    onProfileClick         = { navController.navigate(Routes.PARENT_PROFILE) },
                    onSignOutClick         = onSignOut,
                    onSwitchToChild        = onSwitchToChild
                )
            }
        }

        composable(Routes.PARENT_PROFILE) {
            val childrenOnly = familyMembers.filter { it.userId != currentUser.userId }
            ParentProfileScreen(
                user              = currentUser,
                familyMembers     = childrenOnly,
                onBackClick       = { navController.popBackStack() },
                onAddChildClick   = { navController.navigate(Routes.INVITE_CHILDREN) },
                onStatsClick      = { navController.navigate(Routes.PARENT_STATS) },
                onSettingsClick   = { navController.popBackStack() },
                onChildClick      = { navController.navigate(Routes.CHILD_PROFILE) },
                onChildStatsClick = { navController.navigate(Routes.STATS) }
            )
        }

        composable(Routes.CHILD_PROFILE) {
            val selectedChild  = remember { mutableStateOf<UserModel?>(null) }
            val childToDisplay = selectedChild.value ?: familyMembers.firstOrNull()
            if (childToDisplay != null) {
                ChildProfileScreen(
                    user                   = childToDisplay,
                    onBackClick            = { navController.popBackStack() },
                    onAvatarCustomizeClick = { navController.navigate(Routes.AVATAR_CUSTOMIZATION) },
                    onStatsClick           = { navController.navigate(Routes.PARENT_STATS) },
                    onSettingsClick        = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.AVATAR_CUSTOMIZATION) {
            val childToDisplay = familyMembers.firstOrNull()
            if (childToDisplay != null) {
                AvatarCustomizationScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToShop = { navController.navigate("avatar_shop") },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.INVITE_CHILDREN) {
            InviteChildrenScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.TASK_LIST) {
            var showCreateTaskScreen by remember { mutableStateOf(false) }
            var createdTask by remember { mutableStateOf<TaskModel?>(null) }

            when {
                createdTask != null -> {
                    SelectChildrenScreen(
                        task                 = createdTask!!,
                        currentUser          = currentUser,
                        onBackClick          = { createdTask = null; showCreateTaskScreen = false },
                        onAssignmentComplete = { createdTask = null; showCreateTaskScreen = false }
                    )
                }
                showCreateTaskScreen -> {
                    CreateTaskScreen(
                        currentUser   = currentUser,
                        onTaskCreated = { task -> createdTask = task },
                        onBackClick   = { showCreateTaskScreen = false }
                    )
                }
                else -> {
                    TaskListScreen(
                        currentUser = currentUser,
                        onCreateTaskClick = { showCreateTaskScreen = true },
                        onTaskDetailsClick = { task ->
                            navController.navigate("task_details/${task.id}")
                        }
                    )
                }
            }
        }

        composable(
            "task_details/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            val taskManagementViewModel: TaskManagementViewModel = hiltViewModel()
            val taskUiState by taskManagementViewModel.uiState.collectAsState()

            LaunchedEffect(currentUser.familyId) {
                if (taskUiState.tasks.isEmpty()) {
                    taskManagementViewModel.loadFamilyTasks(currentUser.familyId)
                }
            }

            val task = taskUiState.tasks.find { it.id == taskId } ?: return@composable

            TaskDetailsScreen(
                task = task,
                familyId = currentUser.familyId,
                onBackClick = { navController.popBackStack() },
                onTaskDeleted = { navController.popBackStack() },
                onTaskUpdated = { navController.popBackStack() }
            )
        }

        composable(Routes.PENDING_TASKS) {
            ParentPendingTasksScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.PRIVILEGE_APPROVALS) {
            ParentPrivilegeApprovalsScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.PARENT_CHALLENGES) {
            val showStartChallengeScreen = remember { mutableStateOf(false) }
            if (showStartChallengeScreen.value) {
                StartChallengesScreen(
                    currentUser        = currentUser,
                    onBackClick        = { showStartChallengeScreen.value = false },
                    onChallengeStarted = { showStartChallengeScreen.value = false }
                )
            } else {
                ActiveChallengesScreen(
                    currentUser           = currentUser,
                    onBackClick           = { navController.popBackStack() },
                    onStartChallengeClick = { showStartChallengeScreen.value = true },
                    onChallengeClick      = { challenge ->
                        navController.navigate(Routes.parentChallengeDetail(challenge.challengeId))
                    }
                )
            }
        }

        composable(
            route     = Routes.PARENT_CHALLENGE_DETAIL,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: return@composable
            ChallengeDetailScreen(
                currentUser = currentUser,
                challengeId = challengeId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.FAMILY_MESSAGING) {
            FamilyMessagingScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.GENERATION) {
            GenerationScreen(currentUser = currentUser, onBackClick = { navController.popBackStack() })
        }

        composable(Routes.MARKETPLACE) {
            MarketplaceScreen(currentUser = currentUser, onBackClick = { navController.popBackStack() })
        }

        composable(Routes.PUBLISH) {
            PublishScreen(currentUser = currentUser, onBackClick = { navController.popBackStack() })
        }

        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(currentUser = currentUser, onBackClick = { navController.popBackStack() })
        }

        composable(Routes.PARENT_STATS) {
            StatsScreen(currentUser = currentUser, onBackClick = { navController.popBackStack() })
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(currentUser = currentUser, onBackClick = { navController.popBackStack() })
        }

        composable(Routes.WEEKLY_PLAN) {
            WeeklyPlanScreen(
                currentUser    = currentUser,
                familyChildren = familyMembers,
                onBackClick    = { navController.popBackStack() }
            )
        }

        composable(Routes.DAILY_PLAN) {
            DailyPlanScreen(currentUser = currentUser, onBackClick = { navController.popBackStack() })
        }

        composable(Routes.AVATAR_SHOP) {
            val viewModel = hiltViewModel<AvatarShopViewModel>()

            // ✨ Initialize with current user ID
            LaunchedEffect(currentUser.userId) {
                viewModel.init(currentUser.userId)
            }

            AvatarShopScreen(
                viewModel = viewModel,
                currentUserId = currentUser.userId,
                onBack = { navController.popBackStack() },
                onPackPurchased = { navController.popBackStack() }
            )
        }

        composable(Routes.CONTENT_PACKS) {
            val contentPacksViewModel: ContentPacksViewModel = hiltViewModel()
            val contentUiState by contentPacksViewModel.uiState.collectAsState()
            LaunchedEffect(currentUser.userId) {
                contentPacksViewModel.loadForUser(userId = currentUser.userId, userXp = currentUser.xp)
            }
            ContentPacksScreen(
                currentUser = currentUser,
                isPro       = contentUiState.isPro,
                onBackClick = { navController.popBackStack() },
                viewModel   = contentPacksViewModel
            )
        }

        composable(Routes.UPGRADE) {
            UpgradeScreen(
                currentUser      = currentUser,
                onBackClick      = { navController.popBackStack() },
                onUpgradeSuccess = { _ -> navController.popBackStack() }
            )
        }

        composable(Routes.MODERATION) {
            ModerationScreen(onBackClick = { navController.popBackStack() })
        }
    }
}