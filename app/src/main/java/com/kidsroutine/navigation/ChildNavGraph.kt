package com.kidsroutine.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.kidsroutine.core.model.LootBox
import com.kidsroutine.core.model.LootBoxRarity
import com.kidsroutine.core.model.LootBoxReward
import com.kidsroutine.core.model.LootBoxRewardType
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.achievements.ui.AchievementsScreen
import com.kidsroutine.feature.avatar.ui.AvatarCustomizationScreen
import com.kidsroutine.feature.avatar.ui.AvatarCustomizationViewModel
import com.kidsroutine.feature.avatar.ui.AvatarShopScreen
import com.kidsroutine.feature.challenges.ui.ActiveChallengesScreen
import com.kidsroutine.feature.challenges.ui.ChallengeDetailScreen
import com.kidsroutine.feature.community.ui.LeaderboardScreen
import com.kidsroutine.feature.daily.ui.ChildMainScreen
import com.kidsroutine.feature.execution.ui.TaskExecutionScreen
import com.kidsroutine.feature.family.ui.ChildTaskProposalScreen
import com.kidsroutine.feature.family.ui.FamilyMessagingScreen
import com.kidsroutine.feature.lootbox.ui.LootBoxScreen
import com.kidsroutine.feature.moments.ui.MomentsScreen
import com.kidsroutine.feature.notifications.ui.NotificationsScreen
import com.kidsroutine.feature.profile.ui.ChildProfileScreen
import com.kidsroutine.feature.stats.ui.StatsScreen
import com.kidsroutine.feature.world.ui.WorldScreen


object TaskPassthrough {
    var pendingTask: TaskModel? = null
    var pendingInstanceId: String = ""          // ← ADD
}

fun NavGraphBuilder.childNavGraph(
    currentUser: UserModel,
    navController: NavController,
    onSignOut: () -> Unit
) {
    navigation(
        route            = "child_graph",
        startDestination = Routes.DAILY
    ) {
        composable(Routes.DAILY) {
            ChildMainScreen(
                currentUser            = currentUser,
                onTaskClick = { instance ->
                    TaskPassthrough.pendingTask       = instance.task
                    TaskPassthrough.pendingInstanceId = instance.instanceId   // ← ADD
                    navController.navigate(Routes.execution(instance.instanceId))
                },
                onFamilyMessagingClick = { navController.navigate(Routes.FAMILY_MESSAGING) },
                parentNavController    = navController
            )
        }

        composable(Routes.CHILD_PROFILE) {
            ChildProfileScreen(
                user                   = currentUser,
                onBackClick            = { navController.popBackStack() },
                onAvatarCustomizeClick = { navController.navigate(Routes.AVATAR_CUSTOMIZATION) },
                onStatsClick           = { navController.navigate(Routes.STATS) },
                onSettingsClick        = { navController.popBackStack() }
            )
        }

        composable(Routes.AVATAR_CUSTOMIZATION) {
            val viewModel = hiltViewModel<AvatarCustomizationViewModel>()
            LaunchedEffect(Unit) {
                viewModel.initWithUserId(currentUser.userId)
            }

            AvatarCustomizationScreen(
                viewModel = viewModel,
                onNavigateToShop = { navController.navigate("avatar_shop") },
                onBack = { navController.popBackStack() }
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

        composable(
            route     = Routes.EXECUTION,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) {
            val task = TaskPassthrough.pendingTask
            if (task != null) {
                TaskExecutionScreen(
                    task        = task,
                    instanceId  = TaskPassthrough.pendingInstanceId,   // ← ADD
                    currentUser = currentUser,
                    onBack      = { navController.popBackStack() },
                    onCompleted = { _ ->
                        TaskPassthrough.pendingTask       = null
                        TaskPassthrough.pendingInstanceId = ""          // ← ADD cleanup
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Routes.CHALLENGES) {
            ActiveChallengesScreen(
                currentUser           = currentUser,
                onBackClick           = { navController.popBackStack() },
                onStartChallengeClick = { navController.navigate(Routes.CHALLENGES) },
                onChallengeClick      = { challenge ->
                    navController.navigate(Routes.challengeDetail(challenge.challengeId))
                },
                onViewDetailClick     = { challenge ->
                    navController.navigate(Routes.challengeDetail(challenge.challengeId))
                }
            )
        }

        composable(Routes.FAMILY_MESSAGING) {
            FamilyMessagingScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route     = Routes.CHALLENGE_DETAIL,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: return@composable
            ChallengeDetailScreen(
                currentUser = currentUser,
                challengeId = challengeId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.AVATAR_SHOP) {
            AvatarShopScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onPackPurchased = { navController.popBackStack() }
            )
        }

        composable(Routes.STATS) {
            StatsScreen(currentUser = currentUser, onBackClick = { navController.popBackStack() })
        }

        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(currentUser = currentUser, onBackClick = { navController.popBackStack() })
        }

        composable(Routes.WORLD) {
            WorldScreen(
                currentUser    = currentUser,
                onBackClick    = { navController.popBackStack() },
                onLootBoxClick = { navController.navigate(Routes.LOOT_BOX) }
            )
        }

        composable(Routes.CHILD_TASK_PROPOSAL) {
            ChildTaskProposalScreen(
                currentUser       = currentUser,
                onProposalSuccess = { navController.popBackStack() },
                onBackClick       = { navController.popBackStack() }
            )
        }

        composable(Routes.MOMENTS) {
            MomentsScreen(currentUser = currentUser, onBackClick = { navController.popBackStack() })
        }

        composable(Routes.LOOT_BOX) {
            val sampleBox = LootBox(
                earnedFor = "All tasks done today!",
                reward = LootBoxReward(
                    type        = LootBoxRewardType.XP_BOOST,
                    rarity      = LootBoxRarity.RARE,
                    title       = "XP Surge",
                    description = "You earned a rare XP boost for finishing every task!",
                    emoji       = "⚡",
                    xpValue     = 75
                )
            )
            LootBoxScreen(
                lootBox = sampleBox,
                onBack  = { navController.popBackStack() },
                onClaim = { /* TODO: viewModel.claimLootBox(it) */ }
            )
        }
    }
}
