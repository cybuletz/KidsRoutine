package com.kidsroutine.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.achievements.ui.AchievementsScreen
import com.kidsroutine.feature.daily.ui.DailyScreen
import com.kidsroutine.feature.execution.ui.TaskExecutionScreen
import com.kidsroutine.feature.challenges.ui.ActiveChallengesScreen
import com.kidsroutine.feature.challenges.ui.ChallengeDetailScreen
import com.kidsroutine.feature.community.ui.LeaderboardScreen
import com.kidsroutine.feature.family.ui.FamilyMessagingScreen
import com.kidsroutine.feature.notifications.ui.NotificationsScreen

object TaskPassthrough {
    var pendingTask: TaskModel? = null
}

fun NavGraphBuilder.childNavGraph(
    currentUser: UserModel,
    navController: NavController
) {
    navigation(
        route = "child_graph",
        startDestination = Routes.DAILY
    ) {
        // Daily tasks
        composable(Routes.DAILY) {
            DailyScreen(
                currentUser = currentUser,
                onTaskClick = { instance ->
                    TaskPassthrough.pendingTask = instance.task
                    navController.navigate(Routes.execution(instance.instanceId))
                },
                onChallengesClick = {
                    navController.navigate(Routes.CHALLENGES)
                },
                onAchievementsClick = {
                    navController.navigate(Routes.ACHIEVEMENTS)
                },
                onFamilyMessagingClick = {  // ← ADD THIS
                    navController.navigate(Routes.FAMILY_MESSAGING)
                },
                onStatsClick = {
                    navController.navigate(Routes.LEADERBOARD)
                }
            )
        }

        composable(Routes.ACHIEVEMENTS) {
            AchievementsScreen(
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

        // Task execution
        composable(
            route = Routes.EXECUTION,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) {
            val task = TaskPassthrough.pendingTask
            if (task != null) {
                TaskExecutionScreen(
                    task = task,
                    onBack = { navController.popBackStack() },
                    onCompleted = { _ ->
                        TaskPassthrough.pendingTask = null
                        navController.popBackStack()
                    }
                )
            }
        }

        // Active challenges
        composable(Routes.CHALLENGES) {
            ActiveChallengesScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() },
                onStartChallengeClick = {
                    navController.navigate(Routes.CHALLENGES) // TODO: StartChallengesScreen
                },
                onChallengeClick = { challenge ->
                    navController.navigate(Routes.challengeDetail(challenge.challengeId))
                }
            )
        }

        // Family messaging
        composable(Routes.FAMILY_MESSAGING) {
            FamilyMessagingScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Challenge detail
        composable(
            route = Routes.CHALLENGE_DETAIL,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: return@composable
            ChallengeDetailScreen(
                currentUser = currentUser,
                challengeId = challengeId,
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

        composable(Routes.ACHIEVEMENTS) {
            AchievementsScreen(
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

        // Stats/Progress (TODO: Create StatsScreen)
        composable(Routes.STATS) {
            // TODO: Implement StatsScreen
            androidx.compose.material3.Text("Stats Screen - Coming Soon")
        }
    }
}