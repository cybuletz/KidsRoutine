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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
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
import com.kidsroutine.core.model.ParentControlSettings
import com.kidsroutine.core.model.UserEntitlements
import com.kidsroutine.core.model.PlanType
import com.kidsroutine.core.model.defaultEntitlements
import com.kidsroutine.feature.achievements.ui.AchievementsScreenimport com.kidsroutine.feature.challenges.ui.ActiveChallengesScreen
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
import kotlinx.coroutines.tasks.await

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

    // Weekly XP Summary overlay — shows on Monday
    var showWeeklySummary by remember { mutableStateOf(false) }
    LaunchedEffect(currentUser.userId) {
        val today = java.time.LocalDate.now()
        if (today.dayOfWeek == java.time.DayOfWeek.MONDAY) {
            // Show the summary on Monday if it hasn't been dismissed yet this session
            delay(1500) // Brief delay for the screen to load
            showWeeklySummary = true
        }
    }

    LaunchedEffect(currentUser.userId) {
        notificationViewModel.loadNotifications(currentUser.userId)
    }

    // Load parent controls and entitlements for Fun Zone gating
    var parentControls by remember { mutableStateOf(ParentControlSettings()) }
    var entitlements by remember { mutableStateOf(UserEntitlements()) }

    LaunchedEffect(currentUser.userId, currentUser.familyId) {
        if (currentUser.familyId.isNotEmpty()) {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                // Load parent controls
                val controlDoc = db.collection("families").document(currentUser.familyId)
                    .collection("parent_controls").document(currentUser.userId)
                    .get().await()
                if (controlDoc.exists()) {
                    val data = controlDoc.data ?: emptyMap()
                    parentControls = ParentControlSettings(
                        childId           = currentUser.userId,
                        familyId          = currentUser.familyId,
                        petEnabled        = data["petEnabled"] as? Boolean ?: true,
                        bossBattleEnabled = data["bossBattleEnabled"] as? Boolean ?: true,
                        dailySpinEnabled  = data["dailySpinEnabled"] as? Boolean ?: true,
                        storyArcsEnabled  = data["storyArcsEnabled"] as? Boolean ?: true,
                        eventsEnabled     = data["eventsEnabled"] as? Boolean ?: true,
                        skillTreeEnabled  = data["skillTreeEnabled"] as? Boolean ?: true,
                        walletEnabled     = data["walletEnabled"] as? Boolean ?: true,
                        ritualsEnabled    = data["ritualsEnabled"] as? Boolean ?: true
                    )
                }
                // Load entitlements — find the parent's entitlements (family-level billing)
                val usersSnap = db.collection("users")
                    .whereEqualTo("familyId", currentUser.familyId)
                    .whereEqualTo("role", "PARENT")
                    .get().await()
                val parentId = usersSnap.documents.firstOrNull()?.id
                if (parentId != null) {
                    val entDoc = db.collection("user_entitlements").document(parentId).get().await()
                    if (entDoc.exists()) {
                        val planStr = entDoc.data?.get("planType") as? String ?: "FREE"
                        val planType = try { com.kidsroutine.core.model.PlanType.valueOf(planStr) } catch (_: Exception) { com.kidsroutine.core.model.PlanType.FREE }
                        entitlements = planType.defaultEntitlements(parentId)
                    }
                }
            } catch (_: Exception) { /* Use defaults */ }
        }
    }

    // Also set up a real-time listener to auto-refresh when parent changes controls
    LaunchedEffect(currentUser.userId, currentUser.familyId) {
        if (currentUser.familyId.isNotEmpty()) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("families").document(currentUser.familyId)
                .collection("parent_controls").document(currentUser.userId)
                .addSnapshotListener { snapshot, _ ->
                    val data = snapshot?.data ?: return@addSnapshotListener
                    parentControls = ParentControlSettings(
                        childId           = currentUser.userId,
                        familyId          = currentUser.familyId,
                        petEnabled        = data["petEnabled"] as? Boolean ?: true,
                        bossBattleEnabled = data["bossBattleEnabled"] as? Boolean ?: true,
                        dailySpinEnabled  = data["dailySpinEnabled"] as? Boolean ?: true,
                        storyArcsEnabled  = data["storyArcsEnabled"] as? Boolean ?: true,
                        eventsEnabled     = data["eventsEnabled"] as? Boolean ?: true,
                        skillTreeEnabled  = data["skillTreeEnabled"] as? Boolean ?: true,
                        walletEnabled     = data["walletEnabled"] as? Boolean ?: true,
                        ritualsEnabled    = data["ritualsEnabled"] as? Boolean ?: true
                    )
                }
        }
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
                    onRitualsClick         = { innerNavController.navigate("rituals") },
                    onChallengeDetailClick = { challenge ->
                        parentNavController.navigate(Routes.challengeDetail(challenge.challengeId))
                    }
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

            composable("fun_zone") {
                currentRoute = "fun_zone"
                FunZoneFullScreen(
                    userLevel        = currentUser.level,
                    parentControls   = parentControls,
                    entitlements     = entitlements,
                    onBackClick       = { innerNavController.navigate("daily") },
                    onPetClick        = { innerNavController.navigate("pet") },
                    onBossBattleClick = { innerNavController.navigate("boss_battle") },
                    onSpinWheelClick  = { innerNavController.navigate("spin_wheel") },
                    onEventsClick     = { innerNavController.navigate("events") },
                    onStoryArcClick   = { innerNavController.navigate("story_arc") },
                    onWalletClick     = { innerNavController.navigate("wallet") },
                    onSkillTreeClick  = { innerNavController.navigate("skill_tree") },
                    onRitualsClick    = { innerNavController.navigate("rituals") }
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
            onFunZoneClick          = { innerNavController.navigate("fun_zone")      { popUpTo("daily") } },
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

        // ── Persistent XP Balance Widget ──────────────────────────────
        // Visible across all child screens, positioned at top-center
        if (!worldDetailShowing) {
            XpBalanceWidget(
                xp = currentUser.xp,
                level = currentUser.level,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .offset(y = 2.dp)
                    .zIndex(20f)
            )
        }

        // ── Weekly XP Summary Overlay ────────────────────────────────────
        if (showWeeklySummary) {
            WeeklyXpSummaryOverlay(
                user = currentUser,
                onDismiss = { showWeeklySummary = false }
            )
        }
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
    onFunZoneClick: () -> Unit,
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
        NavItem("fun_zone",    Icons.Default.SportsEsports,"Fun Zone",    onFunZoneClick),
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

// ─────────────────────────────────────────────────────────────────────────────
// Fun Zone Full Screen — attractive feature discovery hub
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FunZoneFullScreen(
    userLevel: Int = 1,
    parentControls: ParentControlSettings = ParentControlSettings(),
    entitlements: UserEntitlements = UserEntitlements(),
    onBackClick: () -> Unit,
    onPetClick: () -> Unit,
    onBossBattleClick: () -> Unit,
    onSpinWheelClick: () -> Unit,
    onEventsClick: () -> Unit,
    onStoryArcClick: () -> Unit,
    onWalletClick: () -> Unit,
    onSkillTreeClick: () -> Unit,
    onRitualsClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "funZoneGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 0.8f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label         = "funGlow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F0))
    ) {
        // ── Header ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text       = "🎮 Fun Zone",
                    style      = MaterialTheme.typography.headlineLarge,
                    color      = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 32.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "Your playground of awesome activities!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        // ── Content ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Helper: check if feature is visible (parent control + billing tier)
            fun isFeatureVisible(featureKey: String): Boolean {
                return parentControls.isFunZoneFeatureEnabled(featureKey) &&
                       entitlements.hasFunZoneFeature(featureKey)
            }

            // Companion & Care — show section if pet is visible
            if (isFeatureVisible("pet")) {
                Text(
                    text       = "🐾 Companion & Care",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF2D3436),
                    modifier   = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )

                FunZoneFeatureCard(
                    emoji       = "🐾",
                    title       = "My Pet",
                    description = "Feed, play, and watch your companion grow! Your pet's happiness depends on you.",
                    accentColor = Color(0xFF06D6A0),
                    onClick     = onPetClick,
                    badge       = "🌟 Popular",
                    requiredLevel = 1,
                    userLevel = userLevel
                )
            }

            // Action & Adventure — show section if either boss or spin is visible
            val showBoss = isFeatureVisible("boss_battle")
            val showSpin = isFeatureVisible("daily_spin")
            if (showBoss || showSpin) {
                Text(
                    text       = "⚔️ Action & Adventure",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF2D3436),
                    modifier   = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (showBoss) {
                        FunZoneCompactCard(
                            emoji       = "⚔️",
                            title       = "Boss Battle",
                            description = "Team up to defeat weekly bosses!",
                            accentColor = Color(0xFFEF476F),
                            onClick     = onBossBattleClick,
                            modifier    = Modifier.weight(1f),
                            requiredLevel = 5,
                            userLevel = userLevel
                        )
                    }
                    if (showSpin) {
                        FunZoneCompactCard(
                            emoji       = "🎡",
                            title       = "Daily Spin",
                            description = "Spin the wheel for surprise rewards!",
                            accentColor = Color(0xFFFF9F1C),
                            onClick     = onSpinWheelClick,
                            modifier    = Modifier.weight(1f),
                            requiredLevel = 2,
                            userLevel = userLevel
                        )
                    }
                }
            }

            // Explore & Discover — show section if any of story/events/skill are visible
            val showStory = isFeatureVisible("story_arcs")
            val showEvents = isFeatureVisible("events")
            val showSkills = isFeatureVisible("skill_tree")
            if (showStory || showEvents || showSkills) {
                Text(
                    text       = "🌍 Explore & Discover",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF2D3436),
                    modifier   = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )

                if (showStory) {
                    FunZoneFeatureCard(
                        emoji       = "📖",
                        title       = "Story Arcs",
                        description = "Embark on multi-day narrative adventures. Complete chapters to unlock the story!",
                        accentColor = Color(0xFF8B5CF6),
                        onClick     = onStoryArcClick,
                        requiredLevel = 3,
                        userLevel = userLevel
                    )
                }

                if (showEvents || showSkills) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (showEvents) {
                            FunZoneCompactCard(
                                emoji       = "📅",
                                title       = "Events",
                                description = "Limited-time seasonal fun!",
                                accentColor = Color(0xFF4361EE),
                                onClick     = onEventsClick,
                                modifier    = Modifier.weight(1f),
                                requiredLevel = 4,
                                userLevel = userLevel
                            )
                        }
                        if (showSkills) {
                            FunZoneCompactCard(
                                emoji       = "🌳",
                                title       = "Skill Tree",
                                description = "Unlock new abilities & skills!",
                                accentColor = Color(0xFF667EEA),
                                onClick     = onSkillTreeClick,
                                modifier    = Modifier.weight(1f),
                                requiredLevel = 3,
                                userLevel = userLevel
                            )
                        }
                    }
                }
            }

            // Money & Mindfulness — show section if wallet or rituals visible
            val showWallet = isFeatureVisible("wallet")
            val showRituals = isFeatureVisible("rituals")
            if (showWallet || showRituals) {
                Text(
                    text       = "💫 Money & Mindfulness",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF2D3436),
                    modifier   = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (showWallet) {
                        FunZoneCompactCard(
                            emoji       = "💰",
                            title       = "My Wallet",
                            description = "Savings goals & financial smarts!",
                            accentColor = Color(0xFF11998E),
                            onClick     = onWalletClick,
                            modifier    = Modifier.weight(1f),
                            requiredLevel = 4,
                            userLevel = userLevel
                        )
                    }
                    if (showRituals) {
                        FunZoneCompactCard(
                            emoji       = "🙏",
                            title       = "Rituals",
                            description = "Family gratitude & bonding!",
                            accentColor = Color(0xFF9B5DE5),
                            onClick     = onRitualsClick,
                            modifier    = Modifier.weight(1f),
                            requiredLevel = 2,
                            userLevel = userLevel
                        )
                    }
                }
            }

            Spacer(Modifier.height(140.dp))
        }
    }
}

@Composable
private fun FunZoneFeatureCard(
    emoji: String,
    title: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit,
    badge: String? = null,
    requiredLevel: Int = 1,
    userLevel: Int = 99
) {
    val isLocked = userLevel < requiredLevel

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked, onClick = onClick),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isLocked) Color(0xFFF0F0F0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (isLocked) 1.dp else 4.dp),
        border    = BorderStroke(1.5.dp, if (isLocked) Color.Gray.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Big emoji circle
            Surface(
                modifier = Modifier.size(56.dp),
                shape    = CircleShape,
                color    = if (isLocked) Color.Gray.copy(alpha = 0.1f) else accentColor.copy(alpha = 0.12f),
                border   = BorderStroke(2.dp, if (isLocked) Color.Gray.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isLocked) {
                        Text("🔒", fontSize = 28.sp)
                    } else {
                        Text(emoji, fontSize = 28.sp)
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text       = title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 16.sp,
                        color      = if (isLocked) Color.Gray else Color(0xFF2D3436)
                    )
                    if (isLocked) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFEBEE)
                        ) {
                            Text(
                                "Lvl $requiredLevel",
                                fontSize   = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color(0xFFE53935),
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (badge != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                badge,
                                fontSize   = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color      = accentColor,
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = if (isLocked) "Reach level $requiredLevel to unlock!" else description,
                    fontSize = 12.sp,
                    color    = Color.Gray,
                    maxLines = 2
                )
            }

            Icon(
                if (isLocked) Icons.Default.Lock else Icons.Default.ChevronRight,
                contentDescription = null,
                tint     = if (isLocked) Color.Gray else accentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun FunZoneCompactCard(
    emoji: String,
    title: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    requiredLevel: Int = 1,
    userLevel: Int = 99
) {
    val isLocked = userLevel < requiredLevel

    Card(
        modifier  = modifier
            .clickable(enabled = !isLocked, onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isLocked) Color(0xFFF0F0F0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (isLocked) 1.dp else 3.dp),
        border    = BorderStroke(1.dp, if (isLocked) Color.Gray.copy(alpha = 0.15f) else accentColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape    = RoundedCornerShape(12.dp),
                    color    = if (isLocked) Color.Gray.copy(alpha = 0.1f) else accentColor.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (isLocked) "🔒" else emoji, fontSize = 20.sp)
                    }
                }
                if (isLocked) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Text(
                            "Lvl $requiredLevel",
                            fontSize   = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFFE53935),
                            modifier   = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text       = title,
                fontWeight = FontWeight.Bold,
                fontSize   = 13.sp,
                color      = if (isLocked) Color.Gray else Color(0xFF2D3436)
            )
            Text(
                text     = if (isLocked) "Reach Lvl $requiredLevel" else description,
                fontSize = 10.sp,
                color    = Color.Gray,
                maxLines = 2,
                lineHeight = 14.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// XP Balance Widget — persistent floating XP badge visible across all screens
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun XpBalanceWidget(
    xp: Int,
    level: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(20.dp),
        color    = Color.White,
        shadowElevation = 6.dp,
        border   = BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Level badge
            Surface(
                shape = CircleShape,
                color = OrangePrimary.copy(alpha = 0.15f),
                modifier = Modifier.size(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "$level",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangePrimary
                    )
                }
            }
            Text(
                "⭐ $xp XP",
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                color      = OrangePrimary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Weekly XP Summary — animated recap overlay shown on Monday mornings
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WeeklyXpSummaryOverlay(
    user: UserModel,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weeklyGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "weeklyGlowAlpha"
    )

    // Animated entrance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "weeklyScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
            .zIndex(50f),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clickable { /* Consume click to prevent propagation to dismiss overlay */ },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with animated trophy
                Text("🏆", fontSize = 48.sp, modifier = Modifier.graphicsLayer { this.alpha = glowAlpha })
                Text(
                    "Weekly Recap",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = OrangePrimary
                )
                Text(
                    "Here's how last week went!",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                // Stats grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WeeklyStat(
                        emoji = "⭐",
                        value = "${user.weeklyXp}",
                        label = "XP Earned",
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                    WeeklyStat(
                        emoji = "💎",
                        value = "${user.xp}",
                        label = "Total XP",
                        color = Color(0xFF4361EE),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WeeklyStat(
                        emoji = "🔥",
                        value = "${user.streak}d",
                        label = "Streak",
                        color = Color(0xFFE53935),
                        modifier = Modifier.weight(1f)
                    )
                    WeeklyStat(
                        emoji = "📈",
                        value = "Lvl ${user.level}",
                        label = "Level",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WeeklyStat(
                        emoji = "🏅",
                        value = user.league.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        label = "League",
                        color = Color(0xFF6A1B9A),
                        modifier = Modifier.weight(1f)
                    )
                    WeeklyStat(
                        emoji = "🐾",
                        value = if (user.petId.isNotEmpty()) "Happy" else "—",
                        label = "Pet Status",
                        color = Color(0xFF06D6A0),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Motivational message
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Text(
                        text = when {
                            user.weeklyXp >= 200 -> "🌟 Amazing week! You're on fire!"
                            user.weeklyXp >= 100 -> "💪 Great effort! Keep pushing!"
                            user.weeklyXp >= 50  -> "👍 Good start! Let's aim higher!"
                            else                  -> "🚀 New week, new goals! Let's go!"
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = OrangePrimary,
                        textAlign = TextAlign.Center
                    )
                }

                // Dismiss button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text(
                        "Let's Go! 🚀",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyStat(
    emoji: String,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(
                value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = color
            )
            Text(
                label,
                fontSize = 10.sp,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}