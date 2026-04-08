package com.kidsroutine.feature.daily.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
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
import androidx.lifecycle.viewModelScope
import com.kidsroutine.feature.family.ui.ChildTaskProposalScreen
import com.kidsroutine.feature.lootbox.ui.LootBoxOverlay
import com.kidsroutine.feature.lootbox.ui.LootBoxPhase
import com.kidsroutine.feature.lootbox.ui.LootBoxScreen
import com.kidsroutine.feature.lootbox.ui.LootBoxViewModel
import com.kidsroutine.feature.notifications.ui.NotificationViewModel
import com.kidsroutine.feature.notifications.ui.NotificationsScreen
import com.kidsroutine.feature.rewards.ui.RewardsScreen
import com.kidsroutine.feature.world.ui.WorldScreen
import com.kidsroutine.feature.pet.ui.PetScreen
import com.kidsroutine.feature.boss.ui.BossScreen
import com.kidsroutine.feature.spinwheel.ui.SpinWheelScreen
import com.kidsroutine.feature.events.ui.EventScreen
import com.kidsroutine.feature.storyarc.ui.StoryArcScreen
import com.kidsroutine.feature.wallet.ui.WalletScreen
import com.kidsroutine.feature.skilltree.ui.SkillTreeScreen
import com.kidsroutine.feature.rituals.ui.RitualsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val OrangePrimary = Color(0xFFFF6B35)
private val BgLight       = Color(0xFFFFFBF0)
private val PinkPropose   = Color(0xFFFF6B9D)

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

    // Tracks whether the world node detail card is open — used to hide FABs
    var worldDetailShowing by remember { mutableStateOf(false) }

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
                    onLootBoxClick         = { innerNavController.navigate("lootbox") },
                    onWorldClick           = { innerNavController.navigate("world") },
                    onPetClick             = { innerNavController.navigate("pet") },
                    onBossBattleClick      = { innerNavController.navigate("boss_battle") },
                    onSpinWheelClick       = { innerNavController.navigate("spin_wheel") },
                    onEventsClick          = { innerNavController.navigate("events") },
                    onStoryArcClick        = { innerNavController.navigate("story_arc") },
                    onWalletClick          = { innerNavController.navigate("wallet") },
                    onSkillTreeClick       = { innerNavController.navigate("skill_tree") },
                    onRitualsClick         = { innerNavController.navigate("rituals") }
                )
            }

            composable("challenges") {
                currentRoute = "challenges"
                ActiveChallengesScreen(
                    currentUser           = currentUser,
                    onBackClick           = { innerNavController.navigate("daily") },
                    onStartChallengeClick = { innerNavController.navigate("challenges") },
                    onChallengeClick      = { challenge ->
                        parentNavController.navigate(Routes.challengeDetail(challenge.challengeId))
                    },
                    onViewDetailClick     = { challenge ->
                        parentNavController.navigate(Routes.challengeDetail(challenge.challengeId))
                    }
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
                    currentUser                  = currentUser,
                    onBackClick                  = { innerNavController.navigate("daily") },
                    onLootBoxClick               = { innerNavController.navigate("lootbox") },
                    onNodeDetailVisibilityChange = { worldDetailShowing = it }
                )
            }

            composable("rewards") {
                currentRoute = "rewards"
                RewardsScreen(
                    currentUser       = currentUser,
                    onBackClick       = { innerNavController.navigate("daily") },
                    onAvatarShopClick = { }
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

            composable("lootbox") {
                currentRoute = "lootbox"
                val lootBoxViewModel: LootBoxViewModel = hiltViewModel()
                val lootBoxUiState by lootBoxViewModel.uiState.collectAsState()

                // Seed a box if the viewmodel is idle (i.e. arrived here via nav, not from TaskExecution)
                LaunchedEffect(Unit) {
                    if (lootBoxUiState.phase == LootBoxPhase.IDLE) {
                        lootBoxViewModel.presentBox(
                            box    = LootBox(earnedFor = "All quests done today!"),
                            userId = currentUser.userId
                        )
                    }
                }

                lootBoxUiState.lootBox?.let { box ->
                    LootBoxScreen(
                        lootBox = box,
                        onBack  = { innerNavController.popBackStack() },
                        onClaim = { lootBoxViewModel.dismiss() }
                    )
                }

                // Navigate back after reward persists
                LaunchedEffect(key1 = lootBoxUiState.phase) {
                    if (lootBoxUiState.phase == LootBoxPhase.DONE) {
                        delay(3500)
                        innerNavController.popBackStack()
                    }
                }
            }

            composable("pet") {
                currentRoute = "pet"
                PetScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }

            composable("boss_battle") {
                currentRoute = "boss_battle"
                BossScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }

            composable("spin_wheel") {
                currentRoute = "spin_wheel"
                SpinWheelScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }

            composable("events") {
                currentRoute = "events"
                EventScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }

            composable("story_arc") {
                currentRoute = "story_arc"
                StoryArcScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }

            composable("wallet") {
                currentRoute = "wallet"
                WalletScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }

            composable("skill_tree") {
                currentRoute = "skill_tree"
                SkillTreeScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }

            composable("rituals") {
                currentRoute = "rituals"
                RitualsScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }
        }

        PersistentNavBar(
            currentRoute            = currentRoute,
            currentUser             = currentUser,
            hideOverlayButtons      = worldDetailShowing,
            onDailyClick            = { innerNavController.navigate("daily")         { popUpTo("daily") } },
            onChallengesClick       = { innerNavController.navigate("challenges")    { popUpTo("daily") } },
            onLeaderboardClick      = { innerNavController.navigate("leaderboard")   { popUpTo("daily") } },
            onWorldClick            = { innerNavController.navigate("world")         { popUpTo("daily") } },
            onAchievementsClick     = { innerNavController.navigate("achievements")  { popUpTo("daily") } },
            onChatClick             = onFamilyMessagingClick,
            onRewardsClick          = { innerNavController.navigate("rewards")       { popUpTo("daily") } },
            pendingRewardCount      = 0,
            unreadNotificationCount = notificationUiState.unreadCount,
            onNotificationsClick    = { innerNavController.navigate("notifications") { popUpTo("daily") } },
            onProposeTaskClick      = { innerNavController.navigate("child_task_proposal") }
        )
    }
}

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

@Composable
private fun PersistentNavBar(
    currentRoute: String,
    currentUser: UserModel,
    hideOverlayButtons: Boolean = false,
    onDailyClick: () -> Unit,
    onChallengesClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onWorldClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onChatClick: () -> Unit,
    unreadNotificationCount: Int = 0,
    onNotificationsClick: () -> Unit = {},
    onProposeTaskClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    pendingRewardCount: Int = 0,
    modifier: Modifier = Modifier
) {
    data class NavItem(val route: String, val icon: ImageVector, val label: String, val onClick: () -> Unit)
    val items = listOf(
        NavItem("daily",       Icons.Default.Home,         "Daily",       onDailyClick),
        NavItem("challenges",  Icons.Default.EmojiEvents,  "Challenges",  onChallengesClick),
        NavItem("leaderboard", Icons.Default.BarChart,     "Leaderboard", onLeaderboardClick),
        NavItem("world",       Icons.Default.Language,     "World",       onWorldClick),
        NavItem("rewards",     Icons.Default.CardGiftcard, "Rewards",     onRewardsClick)
    )

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
                        .height(72.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        val isSelected = currentRoute == item.route
                        val pillAlpha by animateFloatAsState(
                            targetValue   = if (isSelected) 1f else 0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label         = "pill_${item.route}"
                        )
                        val iconScale by animateFloatAsState(
                            targetValue   = if (isSelected) 1.15f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label         = "scale_${item.route}"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(onClick = item.onClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 56.dp, height = 36.dp)
                                    .graphicsLayer { alpha = pillAlpha }
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(OrangePrimary.copy(alpha = 0.12f))
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.label,
                                        tint     = if (isSelected) OrangePrimary else Color.Gray,
                                        modifier = Modifier
                                            .size(26.dp)
                                            .graphicsLayer { scaleX = iconScale; scaleY = iconScale }
                                    )
                                    if (item.route == "rewards" && pendingRewardCount > 0) {
                                        Surface(
                                            shape    = CircleShape,
                                            color    = OrangePrimary,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .align(Alignment.TopEnd)
                                                .zIndex(5f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Text("$pendingRewardCount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text       = item.label,
                                    fontSize   = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color      = if (isSelected) OrangePrimary else Color.Gray,
                                    maxLines   = 1,
                                    overflow   = TextOverflow.Ellipsis
                                )
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

        // Propose Task FAB — hidden when world node detail is open
        if (!hideOverlayButtons) {
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
        }

        // Chat FAB — hidden when world node detail is open
        if (!hideOverlayButtons) {
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
                    Icon(Icons.Default.Message, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}