package com.kidsroutine.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.family.ui.ParentDashboardScreen
import com.kidsroutine.feature.family.ui.InviteChildrenScreen
import com.kidsroutine.feature.tasks.ui.TaskListScreen
import com.kidsroutine.feature.tasks.ui.CreateTaskScreen
import com.kidsroutine.feature.parent.ui.ParentPendingTasksScreen
import com.kidsroutine.feature.challenges.ui.ActiveChallengesScreen
import com.kidsroutine.feature.challenges.ui.StartChallengesScreen
import com.kidsroutine.feature.challenges.ui.ChallengeDetailScreen
import com.kidsroutine.feature.community.ui.MarketplaceScreen
import com.kidsroutine.feature.community.ui.PublishScreen
import com.kidsroutine.feature.community.ui.ModerationScreen
import com.kidsroutine.feature.community.ui.LeaderboardScreen

fun NavGraphBuilder.parentNavGraph(
    currentUser: UserModel,
    navController: NavController
) {
    navigation(
        route = "parent_graph",
        startDestination = Routes.PARENT_DASHBOARD
    ) {
        // Parent dashboard (home)
        composable(Routes.PARENT_DASHBOARD) {
            ParentDashboardScreen(
                currentUser = currentUser,
                onInviteClick = { navController.navigate(Routes.INVITE_CHILDREN) },
                onTasksClick = { navController.navigate(Routes.TASK_LIST) },
                onPendingClick = { navController.navigate(Routes.PENDING_TASKS) },
                onChallengesClick = { navController.navigate(Routes.PARENT_CHALLENGES) },
                onMarketplaceClick = { navController.navigate(Routes.MARKETPLACE) },
                onPublishClick = { navController.navigate(Routes.PUBLISH) },
                onModerationClick = { navController.navigate(Routes.MODERATION) },  // ADD THIS
                onSettingsClick = { /* TODO */ }
            )
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
            val showCreateTaskScreen = remember { mutableStateOf(false) }

            if (showCreateTaskScreen.value) {
                CreateTaskScreen(
                    currentUser = currentUser,
                    onTaskCreated = {
                        showCreateTaskScreen.value = false
                    },
                    onBackClick = {
                        showCreateTaskScreen.value = false
                    }
                )
            } else {
                TaskListScreen(
                    currentUser = currentUser,
                    onCreateTaskClick = { showCreateTaskScreen.value = true },
                    onBackClick = { navController.popBackStack() }
                )
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

        // Marketplace (add button to parent dashboard)
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

        // Leaderboard (add to parent graph)
        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
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