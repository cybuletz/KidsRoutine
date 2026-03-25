package com.kidsroutine.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.avatar.ui.AvatarCustomizationScreen
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
import com.kidsroutine.feature.generation.ui.DailyPlanScreen
import com.kidsroutine.feature.generation.ui.GenerationScreen
import com.kidsroutine.feature.generation.ui.WeeklyPlanScreen
import com.kidsroutine.feature.notifications.ui.NotificationsScreen
import com.kidsroutine.feature.parent.ui.ParentPendingTasksScreen
import com.kidsroutine.feature.profile.ui.ChildProfileScreen
import com.kidsroutine.feature.profile.ui.ParentProfileScreen
import com.kidsroutine.feature.stats.ui.StatsScreen
import com.kidsroutine.feature.tasks.ui.CreateTaskScreen
import com.kidsroutine.feature.tasks.ui.SelectChildrenScreen
import com.kidsroutine.feature.tasks.ui.TaskListScreen
import com.kidsroutine.feature.avatar.ui.AvatarShopScreen
import com.kidsroutine.feature.billing.ContentPacksScreen
import com.kidsroutine.feature.billing.ContentPacksViewModel
import com.kidsroutine.feature.billing.UpgradeScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.kidsroutine.core.model.TaskModel


fun NavGraphBuilder.parentNavGraph(
    currentUser: UserModel,
    familyMembers: List<UserModel>,
    navController: NavController
) {
    navigation(
        route = "parent_graph",
        startDestination = Routes.PARENT_DASHBOARD
    ) {
        // Parent dashboard (home)
        composable(Routes.PARENT_DASHBOARD) {
            ParentDashboardScreen(
                currentUser           = currentUser,
                familyMembers         = familyMembers,
                onFamilyMessagingClick = { navController.navigate(Routes.FAMILY_MESSAGING) },
                onUpgradeClick        = { navController.navigate(Routes.UPGRADE) },
                onContentPacksClick   = { navController.navigate(Routes.CONTENT_PACKS) },
                onProfileClick        = { navController.navigate(Routes.PARENT_PROFILE) },
                onSignOutClick        = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    // Pop entire back stack back to root — MainActivity will detect signout via AuthState
                    navController.navigate("parent_graph") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Parent Profile Screen
        composable(Routes.PARENT_PROFILE) {
            // Filter out the parent from family members (only show children)
            val childrenOnly = familyMembers.filter { it.userId != currentUser.userId }

            ParentProfileScreen(
                user = currentUser,
                familyMembers = childrenOnly,  // ← Use filtered list
                onBackClick = { navController.popBackStack() },
                onAddChildClick = { navController.navigate(Routes.INVITE_CHILDREN) },
                onStatsClick = { navController.navigate(Routes.PARENT_STATS) },
                onSettingsClick = { navController.popBackStack() },
                onChildClick = { child ->
                    navController.navigate(Routes.CHILD_PROFILE)
                },
                onChildStatsClick = { navController.navigate(Routes.STATS) }
            )
        }

        // Child Profile Screen - Accessible from Parent Profile
        composable(Routes.CHILD_PROFILE) {
            val selectedChild = remember { mutableStateOf<UserModel?>(null) }
            val childToDisplay = selectedChild.value ?: familyMembers.firstOrNull()

            if (childToDisplay != null) {
                ChildProfileScreen(
                    user = childToDisplay,
                    onBackClick = { navController.popBackStack() },
                    onAvatarCustomizeClick = { navController.navigate(Routes.AVATAR_CUSTOMIZATION) },
                    onStatsClick = { navController.navigate(Routes.PARENT_STATS) },
                    onSettingsClick = { navController.popBackStack() },
                    )
            }
        }

        // Avatar Customization Screen (NEW - for viewing child's avatar)
        composable(Routes.AVATAR_CUSTOMIZATION) {
            val childToDisplay = familyMembers.firstOrNull()
            if (childToDisplay != null) {
                AvatarCustomizationScreen(
                    currentUser = childToDisplay,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        // Invite children
        composable(Routes.INVITE_CHILDREN) {
            InviteChildrenScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Task management
        composable(Routes.TASK_LIST) {
            var showCreateTaskScreen by remember { mutableStateOf(false) }
            var createdTask by remember { mutableStateOf<TaskModel?>(null) }

            when {
                createdTask != null -> {
                    // Show child selection screen after task creation
                    SelectChildrenScreen(
                        task = createdTask!!,
                        currentUser = currentUser,
                        onBackClick = {
                            createdTask = null
                            showCreateTaskScreen = false
                        },
                        onAssignmentComplete = {
                            createdTask = null
                            showCreateTaskScreen = false
                        }
                    )
                }
                showCreateTaskScreen -> {
                    // Show task creation screen
                    CreateTaskScreen(
                        currentUser = currentUser,
                        onTaskCreated = { task ->
                            createdTask = task
                        },
                        onBackClick = {
                            showCreateTaskScreen = false
                        }
                    )
                }
                else -> {
                    // Show task list
                    TaskListScreen(
                        currentUser = currentUser,
                        onCreateTaskClick = { showCreateTaskScreen = true },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }

        // Pending child tasks
        composable(Routes.PENDING_TASKS) {
            ParentPendingTasksScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Parent challenges
        composable(Routes.PARENT_CHALLENGES) {
            val showStartChallengeScreen = remember { mutableStateOf(false) }

            if (showStartChallengeScreen.value) {
                StartChallengesScreen(
                    currentUser = currentUser,
                    onBackClick = { showStartChallengeScreen.value = false },
                    onChallengeStarted = { showStartChallengeScreen.value = false }
                )
            } else {
                ActiveChallengesScreen(
                    currentUser = currentUser,
                    onBackClick = { navController.popBackStack() },
                    onStartChallengeClick = { showStartChallengeScreen.value = true },
                    onChallengeClick = { challenge ->
                        navController.navigate(Routes.parentChallengeDetail(challenge.challengeId))
                    }
                )
            }
        }

        // Challenge detail (for parents)
        composable(
            route = Routes.PARENT_CHALLENGE_DETAIL,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: return@composable
            ChallengeDetailScreen(
                currentUser = currentUser,
                challengeId = challengeId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Family messaging
        composable(Routes.FAMILY_MESSAGING) {
            FamilyMessagingScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        // AI Task/Challenge Generation Screen
        composable(Routes.GENERATION) {
            GenerationScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Marketplace
        composable(Routes.MARKETPLACE) {
            MarketplaceScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Publish
        composable(Routes.PUBLISH) {
            PublishScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Leaderboard
        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Stats Screen - Parent can view their own stats
        composable(Routes.PARENT_STATS) {
            StatsScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.WEEKLY_PLAN) {
            WeeklyPlanScreen(
                currentUser     = currentUser,
                familyChildren  = familyMembers,
                onBackClick     = { navController.popBackStack() }
            )
        }

        composable(Routes.DAILY_PLAN) {
            DailyPlanScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.AVATAR_SHOP) {
            AvatarShopScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.CONTENT_PACKS) {
            val contentPacksViewModel: ContentPacksViewModel = hiltViewModel()
            val contentUiState by contentPacksViewModel.uiState.collectAsState()

            // Load entitlements + unlocked packs once
            LaunchedEffect(currentUser.userId) {
                contentPacksViewModel.loadForUser(
                    userId = currentUser.userId,
                    userXp = currentUser.xp
                )
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
                onUpgradeSuccess = { _ ->
                    // Return to dashboard — the next screen load will re-read entitlements
                    navController.popBackStack()
                }
            )
        }

        // Moderation panel (admin only)
        composable(Routes.MODERATION) {
            ModerationScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}