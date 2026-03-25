package com.kidsroutine.feature.daily.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kidsroutine.core.engine.SeasonalThemeManager
import com.kidsroutine.core.model.LootBox
import com.kidsroutine.core.model.LootBoxRarity
import com.kidsroutine.core.model.LootBoxReward
import com.kidsroutine.core.model.LootBoxRewardType
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.achievements.ui.AchievementsScreen
import com.kidsroutine.feature.challenges.ui.ActiveChallengesScreen
import com.kidsroutine.feature.community.ui.LeaderboardScreen
import androidx.compose.material.icons.filled.Language
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.feature.family.ui.ChildTaskProposalScreen
import com.kidsroutine.feature.lootbox.ui.LootBoxScreen
import com.kidsroutine.feature.moments.ui.MomentsScreen
import com.kidsroutine.feature.notifications.ui.NotificationViewModel
import com.kidsroutine.feature.notifications.ui.NotificationsScreen
import com.kidsroutine.feature.rewards.ui.RewardsScreen
import com.kidsroutine.feature.world.ui.WorldScreen

private val OrangePrimary = Color(0xFFFF6B35)
private val BgLight       = Color(0xFFFFFBF0)
private val PinkPropose   = Color(0xFFFF6B9D)

// ── Seasonal banner ───────────────────────────────────────────────────────────
@Composable
fun SeasonalBanner(themeManager: SeasonalThemeManager) {
    val theme = remember { themeManager.getActiveTheme() }
    if (theme.bannerText.isBlank()) return

    val infiniteTransition = rememberInfiniteTransition(label = "bannerPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.85f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label         = "bannerAlpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape  = RoundedCornerShape(12.dp),
        color  = theme.primaryAccent.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, theme.primaryAccent.copy(alpha = 0.4f))
    ) {
        Text(
            text       = theme.bannerText,
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color      = theme.primaryAccent,
            modifier   = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .graphicsLayer { this.alpha = alpha }
        )
    }
}

// ── Main screen ───────────────────────────────────────────────────────────────
@Composable
fun ChildMainScreen(
    currentUser: UserModel,
    onTaskClick: (com.kidsroutine.core.model.TaskInstance) -> Unit,
    onFamilyMessagingClick: () -> Unit,
    parentNavController: NavController
) {
    val innerNavController = rememberNavController()
    var currentRoute by remember { mutableStateOf("daily") }

    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val notificationUiState by notificationViewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.userId) {
        notificationViewModel.loadNotifications(currentUser.userId)
    }

    val themeManager = remember { SeasonalThemeManager() }

    Box(modifier = Modifier.fillMaxSize()) {

        NavHost(
            navController    = innerNavController,
            startDestination = "daily",
            modifier         = Modifier.fillMaxSize()
        ) {
            composable("daily") {
                currentRoute = "daily"
                DailyScreen(
                    currentUser            = currentUser,
                    onTaskClick            = onTaskClick,
                    onChallengesClick      = { innerNavController.navigate("challenges") },
                    onAchievementsClick    = { innerNavController.navigate("achievements") },
                    onFamilyMessagingClick = onFamilyMessagingClick,
                    onStatsClick           = { innerNavController.navigate("leaderboard") },
                    onProfileClick         = { parentNavController.navigate(Routes.CHILD_PROFILE) },
                    onNotificationsClick   = { innerNavController.navigate("notifications") },
                    onLootBoxClick         = { innerNavController.navigate("lootbox") }   // ← NEW
                )
            }

            composable("challenges") {
                currentRoute = "challenges"
                ActiveChallengesScreen(
                    currentUser           = currentUser,
                    onBackClick           = { innerNavController.navigate("daily") },
                    onStartChallengeClick = { innerNavController.navigate("challenges") },
                    onChallengeClick      = { }
                )
            }

            composable("leaderboard") {
                currentRoute = "leaderboard"
                LeaderboardScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }

            composable("achievements") {
                currentRoute = "achievements"
                AchievementsScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }

            composable("world") {
                currentRoute = "world"
                WorldScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }

            composable("rewards") {
                currentRoute = "rewards"
                RewardsScreen(
                    currentUser       = currentUser,
                    onBackClick       = { innerNavController.navigate("daily") },
                    onAvatarShopClick = { /* TODO: navigate to avatar customization */ }
                )
            }

            composable("moments") {
                currentRoute = "moments"
                MomentsScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }

            composable("notifications") {
                currentRoute = "notifications"
                NotificationsScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") },
                    viewModel   = notificationViewModel
                )
            }

            composable("child_task_proposal") {
                currentRoute = "child_task_proposal"
                ChildTaskProposalScreen(
                    currentUser       = currentUser,
                    onProposalSuccess = { innerNavController.navigate("daily") },
                    onBackClick       = { innerNavController.popBackStack() }
                )
            }

            // ── Loot Box screen ────────────────────────────────────────────
            composable("lootbox") {
                currentRoute = "lootbox"
                // In a real build you'd pass the actual pending LootBox from
                // a DailyViewModel / RewardsViewModel. This sample box ensures
                // the screen renders immediately — swap it out when the real
                // data layer is wired.
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
                    onBack  = { innerNavController.popBackStack() },
                    onClaim = { /* TODO: viewModel.claimLootBox(it) */ }
                )
            }
        }

        // ── Persistent bottom nav bar ─────────────────────────────────────
        PersistentNavBar(
            currentRoute            = currentRoute,
            currentUser             = currentUser,
            onDailyClick            = { innerNavController.navigate("daily")         { popUpTo("daily") } },
            onChallengesClick       = { innerNavController.navigate("challenges")    { popUpTo("daily") } },
            onLeaderboardClick      = { innerNavController.navigate("leaderboard")   { popUpTo("daily") } },
            onWorldClick            = { innerNavController.navigate("world")         { popUpTo("daily") } },
            onAchievementsClick     = { innerNavController.navigate("achievements")  { popUpTo("daily") } },
            onMomentsClick          = { innerNavController.navigate("moments")       { popUpTo("daily") } },
            onChatClick             = onFamilyMessagingClick,
            onRewardsClick          = { innerNavController.navigate("rewards")       { popUpTo("daily") } },
            pendingRewardCount      = 0,
            unreadNotificationCount = notificationUiState.unreadCount,
            onNotificationsClick    = { innerNavController.navigate("notifications") { popUpTo("daily") } },
            onProposeTaskClick      = { innerNavController.navigate("child_task_proposal") }
        )
    }
}

// ── Overloaded SeasonalBanner with modifier ────────────────────────────────────
@Composable
fun SeasonalBanner(
    themeManager: SeasonalThemeManager,
    modifier: Modifier = Modifier
) {
    val theme = remember { themeManager.getActiveTheme() }
    if (theme.bannerText.isBlank()) return

    val infiniteTransition = rememberInfiniteTransition(label = "bannerPulse2")
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.85f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label         = "bannerAlpha2"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape  = RoundedCornerShape(12.dp),
        color  = theme.primaryAccent.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, theme.primaryAccent.copy(alpha = 0.4f))
    ) {
        Text(
            text       = theme.bannerText,
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color      = theme.primaryAccent,
            modifier   = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .graphicsLayer { this.alpha = alpha }
        )
    }
}

// ── Nav bar ───────────────────────────────────────────────────────────────────
@Composable
private fun PersistentNavBar(
    currentRoute: String,
    currentUser: UserModel,
    onDailyClick: () -> Unit,
    onChallengesClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onWorldClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onMomentsClick: () -> Unit,
    onChatClick: () -> Unit,
    unreadNotificationCount: Int = 0,
    onNotificationsClick: () -> Unit = {},
    onProposeTaskClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    pendingRewardCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier        = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color           = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 0.dp, vertical = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    NavBarItemButton(
                        icon       = Icons.Default.Home,
                        label      = "Daily",
                        isSelected = currentRoute == "daily",
                        onClick    = onDailyClick,
                        modifier   = Modifier.weight(1f)
                    )
                    NavBarItemButton(
                        icon       = Icons.Default.EmojiEvents,
                        label      = "Challenges",
                        isSelected = currentRoute == "challenges",
                        onClick    = onChallengesClick,
                        modifier   = Modifier.weight(1f)
                    )
                    NavBarItemButton(
                        icon       = Icons.Default.BarChart,
                        label      = "Leaderboard",
                        isSelected = currentRoute == "leaderboard",
                        onClick    = onLeaderboardClick,
                        modifier   = Modifier.weight(1f)
                    )
                    NavBarItemButton(
                        icon       = Icons.Default.Language,
                        label      = "World",
                        isSelected = currentRoute == "world",
                        onClick    = onWorldClick,
                        modifier   = Modifier.weight(1f)
                    )
                    NavBarItemButton(
                        icon       = Icons.Default.PhotoAlbum,
                        label      = "Moments",
                        isSelected = currentRoute == "moments",
                        onClick    = onMomentsClick,
                        modifier   = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        NavBarItemButton(
                            icon       = Icons.Default.CardGiftcard,
                            label      = "Rewards",
                            isSelected = currentRoute == "rewards",
                            onClick    = onRewardsClick,
                            modifier   = Modifier.fillMaxHeight()
                        )
                        if (pendingRewardCount > 0) {
                            Surface(
                                shape    = CircleShape,
                                color    = OrangePrimary,
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .zIndex(5f)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        "$pendingRewardCount",
                                        color      = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(Color.White)
                )
            }
        }

        // Propose Task FAB — bottom-left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 16.dp, y = (-90).dp)
                .zIndex(10f)
                .navigationBarsPadding()
        ) {
            Button(
                onClick        = onProposeTaskClick,
                modifier       = Modifier.size(width = 56.dp, height = 50.dp),
                shape          = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp),
                colors         = ButtonDefaults.buttonColors(containerColor = PinkPropose),
                contentPadding = PaddingValues(0.dp),
                elevation      = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("👶", fontSize = 22.sp)
            }
        }

        // Chat bubble — bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-90).dp)
                .zIndex(10f)
                .navigationBarsPadding()
        ) {
            Button(
                onClick        = onChatClick,
                modifier       = Modifier.size(width = 56.dp, height = 50.dp),
                shape          = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp),
                colors         = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC407A)),
                contentPadding = PaddingValues(0.dp),
                elevation      = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    Icons.Default.Message,
                    contentDescription = "Chat",
                    tint               = Color.White,
                    modifier           = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun NavBarItemButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier            = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint               = if (isSelected) OrangePrimary else Color.Gray,
            modifier           = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text       = label,
            fontSize   = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color      = if (isSelected) OrangePrimary else Color.Gray,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis
        )
    }
}
