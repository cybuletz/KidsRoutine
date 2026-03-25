package com.kidsroutine.feature.family.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.Role
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.challenges.ui.ActiveChallengesScreen
import com.kidsroutine.feature.community.ui.LeaderboardScreen
import com.kidsroutine.feature.community.ui.MarketplaceScreen
import com.kidsroutine.feature.community.ui.PublishScreen
import com.kidsroutine.feature.generation.ui.GenerationScreen
import com.kidsroutine.feature.generation.ui.WeeklyPlanScreen
import com.kidsroutine.feature.notifications.ui.NotificationViewModel
import com.kidsroutine.feature.onboarding.ui.ParentOnboardingWizard
import com.kidsroutine.feature.parent.ui.ParentPendingTasksScreen
import com.kidsroutine.feature.settings.ui.SettingsScreen
import com.kidsroutine.feature.tasks.ui.TaskListScreen

private val OrangePrimary = Color(0xFFFF6B35)
private val GradientStart = Color(0xFFFF6B35)
private val GradientEnd   = Color(0xFFFFD93D)
private val BgLight       = Color(0xFFFFFBF0)
private val TextDark      = Color(0xFF2D3436)
private val PinkChat      = Color(0xFFEC407A)

// ── Entry point ───────────────────────────────────────────────────────────────
@Composable
fun ParentDashboardScreen(
    currentUser: UserModel,
    familyMembers: List<UserModel>,
    onFamilyMessagingClick: () -> Unit,
    onTaskClick: (taskId: String) -> Unit = {},
    onUpgradeClick: () -> Unit = {},
    onContentPacksClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    viewModel: ParentDashboardViewModel = hiltViewModel()
) {
    val innerNav = rememberNavController()
    var currentTab by remember { mutableStateOf("home") }

    val notifViewModel: NotificationViewModel = hiltViewModel()
    val notifState by notifViewModel.uiState.collectAsState()
    LaunchedEffect(currentUser.userId) { notifViewModel.loadNotifications(currentUser.userId) }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(currentUser.familyId) {
        if (currentUser.familyId.isNotEmpty()) viewModel.loadFamily(currentUser.familyId)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Inner NavHost ──────────────────────────────────────────────────
        NavHost(
            navController    = innerNav,
            startDestination = "home",
            modifier         = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp) // space for nav bar
        ) {
            composable("home") {
                currentTab = "home"
                ParentHomeTab(
                    currentUser            = currentUser,
                    familyMembers          = familyMembers.filter { it.userId != currentUser.userId },
                    uiState                = uiState,
                    unreadCount            = notifState.unreadCount,
                    onNotificationsClick   = { innerNav.navigate("notifications") },
                    onProfileClick         = onProfileClick,
                    onPendingClick         = { innerNav.navigate("tasks") },
                    onFamilyMessagingClick = onFamilyMessagingClick,
                    onCreateTaskClick      = { innerNav.navigate("tasks") }
                )
            }
            composable("tasks") {
                currentTab = "tasks"
                ParentTasksTab(
                    currentUser       = currentUser,
                    onUpgradeClick    = onUpgradeClick
                )
            }
            composable("family") {
                currentTab = "family"
                ParentFamilyTab(
                    currentUser   = currentUser,
                    familyMembers = familyMembers.filter { it.userId != currentUser.userId },
                    uiState       = uiState,
                    onChallengesClick = { innerNav.navigate("discover") }
                )
            }
            composable("discover") {
                currentTab = "discover"
                ParentDiscoverTab(
                    currentUser         = currentUser,
                    familyMembers       = familyMembers,
                    onUpgradeClick      = onUpgradeClick,
                    onGenerationClick   = { innerNav.navigate("generation") },
                    onWeeklyPlanClick   = { innerNav.navigate("weekly_plan") },
                    onMarketplaceClick  = { innerNav.navigate("marketplace") },
                    onPublishClick      = { innerNav.navigate("publish") },
                    onContentPacksClick = onContentPacksClick,
                    onModerationClick   = { innerNav.navigate("moderation") }
                )
            }
            composable("settings") {
                currentTab = "settings"
                SettingsScreen(
                    currentUser       = currentUser,
                    familyInviteCode  = uiState.inviteCode,
                    onSignOutClick    = onSignOutClick,
                    onUpgradeClick    = onUpgradeClick,
                    onContentPacksClick = onContentPacksClick
                )
            }
            composable("notifications") {
                currentTab = "notifications"
                com.kidsroutine.feature.notifications.ui.NotificationsScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNav.popBackStack() },
                    viewModel   = notifViewModel
                )
            }
            composable("generation") {
                GenerationScreen(currentUser = currentUser, onBackClick = { innerNav.popBackStack() })
            }
            composable("weekly_plan") {
                WeeklyPlanScreen(currentUser = currentUser, familyChildren = familyMembers, onBackClick = { innerNav.popBackStack() })
            }
            composable("marketplace") {
                MarketplaceScreen(currentUser = currentUser, onBackClick = { innerNav.popBackStack() })
            }
            composable("publish") {
                PublishScreen(currentUser = currentUser, onBackClick = { innerNav.popBackStack() })
            }
            composable("moderation") {
                com.kidsroutine.feature.community.ui.ModerationScreen(onBackClick = { innerNav.popBackStack() })
            }
        }

        // ── Bottom Nav Bar ─────────────────────────────────────────────────
        ParentNavBar(
            currentTab          = currentTab,
            unreadNotifications = notifState.unreadCount,
            onHomeClick         = { innerNav.navigate("home")     { popUpTo("home") } },
            onTasksClick        = { innerNav.navigate("tasks")    { popUpTo("home") } },
            onFamilyClick       = { innerNav.navigate("family")   { popUpTo("home") } },
            onDiscoverClick     = { innerNav.navigate("discover") { popUpTo("home") } },
            onSettingsClick     = { innerNav.navigate("settings") { popUpTo("home") } },
            modifier            = Modifier.align(Alignment.BottomCenter)
        )

        // ── Chat Bubble ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-90).dp)
                .zIndex(10f)
                .navigationBarsPadding()
        ) {
            Button(
                onClick  = onFamilyMessagingClick,
                modifier = Modifier.size(width = 56.dp, height = 50.dp),
                shape    = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = PinkChat),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Default.Message, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// TAB 1 — HOME
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun ParentHomeTab(
    currentUser: UserModel,
    familyMembers: List<UserModel>,
    uiState: ParentDashboardUiState,
    unreadCount: Int,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onPendingClick: () -> Unit,
    onFamilyMessagingClick: () -> Unit,
    onCreateTaskClick: () -> Unit,
) {
    var selectedChild by remember { mutableStateOf<UserModel?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Gradient header ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 32.dp)
        ) {
            Column {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column {
                        Text(
                            "Good ${timeOfDayGreeting()} 👋",
                            fontSize = 14.sp,
                            color    = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            currentUser.displayName,
                            fontSize   = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                        if (uiState.family != null) {
                            Text(
                                uiState.family.familyName,
                                fontSize = 13.sp,
                                color    = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Notification bell
                        Box {
                            IconButton(
                                onClick  = onNotificationsClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            if (unreadCount > 0) {
                                Surface(
                                    shape    = CircleShape,
                                    color    = Color(0xFFE74C3C),
                                    modifier = Modifier.size(16.dp).align(Alignment.TopEnd)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text(if (unreadCount > 9) "9+" else "$unreadCount", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        // Profile avatar
                        IconButton(
                            onClick  = onProfileClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // ── Family stat chips (slim row) ───────────────────────────────────
        if (uiState.family != null) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-16).dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip("⭐", "${uiState.family.familyXp} XP",    Modifier.weight(1f))
                StatChip("🔥", "${uiState.family.familyStreak}d",  Modifier.weight(1f))
                StatChip("👥", "${uiState.family.memberIds.size}",  Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(4.dp))

        // ── Action Required banner (conditional) ──────────────────────────
        if (unreadCount > 0) {
            ActionRequiredBanner(
                message = "$unreadCount item${if (unreadCount > 1) "s" else ""} need your attention",
                onClick  = onNotificationsClick,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        // ── Children horizontal scroll ─────────────────────────────────────
        if (familyMembers.isNotEmpty()) {
            Text(
                "Your Children",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = TextDark,
                modifier   = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(familyMembers) { child ->
                    ChildSummaryCard(
                        child   = child,
                        onClick = { selectedChild = child }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // ── Quick actions (contextual, max 3) ─────────────────────────────
        Text(
            "Quick Actions",
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold,
            color      = TextDark,
            modifier   = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))
        Column(
            modifier              = Modifier.padding(horizontal = 20.dp),
            verticalArrangement   = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionRow(
                icon    = Icons.Default.Add,
                color   = Color(0xFF4A90E2),
                title   = "Create a Task",
                subtitle = "Assign a new task to your children",
                onClick = onCreateTaskClick
            )
            QuickActionRow(
                icon    = Icons.Default.Pending,
                color   = Color(0xFFFF9800),
                title   = "Child Proposals",
                subtitle = "Tasks your children want to do",
                onClick = onPendingClick
            )
            QuickActionRow(
                icon    = Icons.Default.EmojiEvents,
                color   = Color(0xFF9B59B6),
                title   = "Start a Challenge",
                subtitle = "Set a family-wide goal",
                onClick = onPendingClick
            )
        }

        Spacer(Modifier.height(32.dp))
    }

    // ── Child detail bottom sheet ──────────────────────────────────────────
    selectedChild?.let { child ->
        ChildDetailSheet(
            child          = child,
            onDismiss      = { selectedChild = null },
            onMessageClick = onFamilyMessagingClick
        )
    }

    // ── Onboarding wizard — shown once when family has no children ─────────
    val context = LocalContext.current
    val prefs   = remember {
        context.getSharedPreferences("parent_prefs", android.content.Context.MODE_PRIVATE)
    }
    val wizardSeen  = remember { prefs.getBoolean("wizard_seen", false) }
    var showWizard  by remember { mutableStateOf(!wizardSeen && familyMembers.isEmpty()) }

    if (showWizard && uiState.family != null) {
        ParentOnboardingWizard(
            parentName  = currentUser.displayName,
            inviteCode  = uiState.inviteCode,
            onDismiss   = {
                prefs.edit().putBoolean("wizard_seen", true).apply()
                showWizard = false
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// TAB 2 — TASKS
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun ParentTasksTab(
    currentUser: UserModel,
    onUpgradeClick: () -> Unit
) {
    var selectedSegment by remember { mutableStateOf(0) }
    var showCreateTask by remember { mutableStateOf(false) }
    var createdTask by remember { mutableStateOf<com.kidsroutine.core.model.TaskModel?>(null) }

    // Handle the Create → Assign children flow inline
    when {
        createdTask != null -> {
            com.kidsroutine.feature.tasks.ui.SelectChildrenScreen(
                task                 = createdTask!!,
                currentUser          = currentUser,
                onBackClick          = { createdTask = null },
                onAssignmentComplete = { createdTask = null }
            )
            return
        }
        showCreateTask -> {
            com.kidsroutine.feature.tasks.ui.CreateTaskScreen(
                currentUser   = currentUser,
                onTaskCreated = { task -> createdTask = task },
                onBackClick   = { showCreateTask = false }
            )
            return
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        // ── Header with + button ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Tasks",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
                // Show + only on My Tasks tab
                if (selectedSegment == 0) {
                    IconButton(onClick = { showCreateTask = true }) {
                        Icon(
                            imageVector        = Icons.Default.Add,
                            contentDescription = "Create Task",
                            tint               = Color.White,
                            modifier           = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // ── Segmented control ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("My Tasks", "Proposals").forEachIndexed { index, label ->
                Surface(
                    modifier        = Modifier.weight(1f).clickable { selectedSegment = index },
                    shape           = RoundedCornerShape(10.dp),
                    color           = if (selectedSegment == index) Color.White else Color.Transparent,
                    shadowElevation = if (selectedSegment == index) 2.dp else 0.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text       = label,
                            fontSize   = 14.sp,
                            fontWeight = if (selectedSegment == index) FontWeight.Bold else FontWeight.Medium,
                            color      = if (selectedSegment == index) OrangePrimary else Color.Gray
                        )
                    }
                }
            }
        }

        // ── Content ────────────────────────────────────────────────────────
        when (selectedSegment) {
            0 -> com.kidsroutine.feature.tasks.ui.TaskListScreen(
                currentUser       = currentUser,
                onCreateTaskClick = { showCreateTask = true },
                onBackClick       = { }  // no-op: navigation handled by bottom nav bar
            )
            1 -> com.kidsroutine.feature.parent.ui.ParentPendingTasksScreen(
                currentUser = currentUser,
                onBackClick = { }   // no-op: navigation handled by bottom nav bar
            )
        }
    }
}


// ══════════════════════════════════════════════════════════════════════════════
// TAB 3 — FAMILY
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun ParentFamilyTab(
    currentUser: UserModel,
    familyMembers: List<UserModel>,
    uiState: ParentDashboardUiState,
    onChallengesClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text("Family", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Children section header ───────────────────────────────────────
        item {
            Text(
                "Children",
                fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))
        }

        // ── Children list ─────────────────────────────────────────────────
        if (familyMembers.isEmpty()) {
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier            = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("👶", fontSize = 40.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No children yet", fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Share your invite code from Settings", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        familyMembers.forEachIndexed { index, child ->
                            FamilyMemberRow(child = child)
                            if (index < familyMembers.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color    = Color(0xFFF0F0F0)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Challenges header ─────────────────────────────────────────────
        item {
            Spacer(Modifier.height(24.dp))
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Challenges", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                TextButton(onClick = onChallengesClick) {
                    Text("Manage", color = OrangePrimary, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Challenges — rendered as plain cards, no nested LazyColumn ────
        item {
            // Direct composable call — ActiveChallengesScreen internally uses LazyColumn
            // so we replace it here with a simple non-scrolling card placeholder
            // that navigates to the full screen on tap
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable(onClick = onChallengesClick),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape    = RoundedCornerShape(13.dp),
                        color    = Color(0xFF9B59B6).copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("🏆", fontSize = 22.sp)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("View Challenges", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                        Text("Manage active family challenges", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
        }

        // ── Leaderboard header ────────────────────────────────────────────
        item {
            Spacer(Modifier.height(24.dp))
            Text(
                "Family Leaderboard",
                fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        // ── Leaderboard — same pattern, card that navigates ───────────────
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                // Render family members ranked by XP inline — no LazyColumn
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    val ranked = familyMembers.sortedByDescending { it.xp }
                    if (ranked.isEmpty()) {
                        Box(
                            modifier            = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment    = Alignment.Center
                        ) {
                            Text("No members to rank yet", color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        ranked.forEachIndexed { index, member ->
                            Row(
                                modifier              = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Rank badge
                                Surface(
                                    modifier = Modifier.size(28.dp),
                                    shape    = CircleShape,
                                    color    = when (index) {
                                        0    -> Color(0xFFFFD700)
                                        1    -> Color(0xFFB0BEC5)
                                        2    -> Color(0xFFCD7F32)
                                        else -> Color(0xFFEEEEEE)
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            "${index + 1}",
                                            fontSize   = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color      = if (index < 3) Color.White else Color.Gray
                                        )
                                    }
                                }
                                Text(
                                    member.displayName,
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color      = TextDark,
                                    modifier   = Modifier.weight(1f)
                                )
                                Text(
                                    "${member.xp} XP",
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = OrangePrimary
                                )
                            }
                            if (index < ranked.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color    = Color(0xFFF0F0F0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════════════
// TAB 4 — DISCOVER
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun ParentDiscoverTab(
    currentUser: UserModel,
    familyMembers: List<UserModel>,
    onUpgradeClick: () -> Unit,
    onGenerationClick: () -> Unit,
    onWeeklyPlanClick: () -> Unit,
    onMarketplaceClick: () -> Unit,
    onPublishClick: () -> Unit,
    onContentPacksClick: () -> Unit = {},   // ← ADD THIS
    onModerationClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text("Discover", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(20.dp))

        Column(
            modifier            = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DiscoverCard("✨", "AI Task Generator",    "Create personalised tasks using AI",             Color(0xFF4A90E2), onGenerationClick)
            DiscoverCard("📅", "Weekly Planner",       "AI 7-day family schedule · PRO",                 Color(0xFF11998E), onWeeklyPlanClick)
            DiscoverCard("🎁", "Content Packs", "Browse & unlock themed task packs", Color(0xFF667EEA), onContentPacksClick)
            DiscoverCard("🌍", "Community Library",    "Browse tasks shared by other families",          Color(0xFF667EEA), onMarketplaceClick)
            DiscoverCard("📤", "Publish Content",      "Share your best tasks with the community",       Color(0xFFFF6B35), onPublishClick)
            if (currentUser.isAdmin) {
                DiscoverCard("🛡️", "Moderation Panel", "Admin only — review community content",         Color(0xFFE74C3C), onModerationClick)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}


// ══════════════════════════════════════════════════════════════════════════════
// BOTTOM NAV BAR
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun ParentNavBar(
    currentTab: String,
    unreadNotifications: Int,
    onHomeClick: () -> Unit,
    onTasksClick: () -> Unit,
    onFamilyClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier        = modifier.fillMaxWidth(),
        color           = Color.White,
        shadowElevation = 12.dp
    ) {
        Column {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                ParentNavItem(Icons.Default.Home,        "Home",     currentTab == "home",     onHomeClick)
                ParentNavItem(Icons.Default.CheckCircle, "Tasks",    currentTab == "tasks",    onTasksClick)
                ParentNavItem(Icons.Default.People,      "Family",   currentTab == "family",   onFamilyClick)
                ParentNavItem(Icons.Default.Explore,     "Discover", currentTab == "discover", onDiscoverClick)
                ParentNavItem(Icons.Default.Settings,    "Settings", currentTab == "settings", onSettingsClick)
            }
            Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().background(Color.White))
        }
    }
}

@Composable
private fun ParentNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier            = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint     = if (isSelected) OrangePrimary else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize   = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color      = if (isSelected) OrangePrimary else Color.Gray,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// REUSABLE COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StatChip(emoji: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
    }
}

@Composable
private fun ActionRequiredBanner(message: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("⚠️", fontSize = 20.sp)
            Text(message, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF856404), modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ChildSummaryCard(child: UserModel, onClick: () -> Unit) {
    var isOnline by remember { mutableStateOf(false) }
    LaunchedEffect(child.userId) {
        FirebaseFirestore.getInstance().collection("users").document(child.userId)
            .addSnapshotListener { snap, _ ->
                isOnline = snap?.getBoolean("isOnline") ?: false
            }
    }
    Card(
        modifier  = Modifier.width(140.dp).clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = OrangePrimary.copy(alpha = 0.15f)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("👤", fontSize = 26.sp)
                    }
                }
                if (isOnline) {
                    Surface(modifier = Modifier.size(12.dp).align(Alignment.BottomEnd), shape = CircleShape, color = Color(0xFF4CAF50)) {}
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(child.displayName.split(" ").first(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Lv ${child.level}", fontSize = 11.sp, color = OrangePrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("${child.xp} XP", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun FamilyMemberRow(child: UserModel) {
    var isOnline by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf(child.displayName) }
    LaunchedEffect(child.userId) {
        FirebaseFirestore.getInstance().collection("users").document(child.userId)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    isOnline    = snap.getBoolean("isOnline") ?: false
                    displayName = snap.getString("displayName") ?: child.displayName
                }
            }
    }
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = OrangePrimary.copy(alpha = 0.15f)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text("👤", fontSize = 22.sp) }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(displayName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextDark)
                Surface(modifier = Modifier.size(7.dp), shape = CircleShape, color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFBDBDBD)) {}
            }
            Text("Level ${child.level} · ${child.xp} XP · 🔥 ${child.streak}", fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.Default.BarChart, contentDescription = "Stats", tint = Color(0xFF4A90E2), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun QuickActionRow(
    icon: ImageVector,
    color: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.12f)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextDark)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun DiscoverCard(
    emoji: String,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit              // ← replaces content: @Composable () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(18.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape    = RoundedCornerShape(13.dp),
                color    = color.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(emoji, fontSize = 22.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint     = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChildDetailSheet(
    child: UserModel,
    onDismiss: () -> Unit,
    onMessageClick: () -> Unit
) {
    var isOnline by remember { mutableStateOf(false) }
    LaunchedEffect(child.userId) {
        FirebaseFirestore.getInstance().collection("users").document(child.userId)
            .addSnapshotListener { snap, _ ->
                isOnline = snap?.getBoolean("isOnline") ?: false
            }
    }

    ModalBottomSheet(
        onDismissRequest  = onDismiss,
        containerColor    = Color.White,
        shape             = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar + name
            Surface(
                modifier = Modifier.size(72.dp),
                shape    = CircleShape,
                color    = OrangePrimary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("👤", fontSize = 36.sp)
                }
            }
            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    child.displayName,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextDark
                )
                Surface(
                    modifier = Modifier.size(10.dp),
                    shape    = CircleShape,
                    color    = if (isOnline) Color(0xFF4CAF50) else Color(0xFFBDBDBD)
                ) {}
            }
            Text(
                "Level ${child.level}",
                fontSize = 14.sp,
                color    = OrangePrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(20.dp))

            // Stats row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChildStatItem("⭐", "${child.xp}", "Total XP")
                ChildStatItem("🔥", "${child.streak}", "Day Streak")
                ChildStatItem("🏆", "Lv ${child.level}", "Level")
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(20.dp))

            // Today's tasks placeholder
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Today's Tasks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OrangePrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        "View all",
                        fontSize = 12.sp,
                        color    = OrangePrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Placeholder — in future replace with real task list from ViewModel
            Surface(
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                color         = Color(0xFFF9F9F9),
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier         = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Open the Tasks tab to see ${child.displayName.split(" ").first()}'s tasks",
                        fontSize  = 13.sp,
                        color     = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Message button
            Button(
                onClick  = {
                    onDismiss()
                    onMessageClick()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = PinkChat)
            ) {
                Icon(Icons.Default.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Message ${child.displayName.split(" ").first()}",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChildStatItem(emoji: String, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, fontSize = 22.sp)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}


private fun timeOfDayGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "morning"
        hour < 17 -> "afternoon"
        else      -> "evening"
    }
}

// Keep this data class here if it's not already in ParentDashboardViewModel.kt
data class ParentDashboardUiState(
    val isLoading: Boolean = false,
    val family: com.kidsroutine.core.model.FamilyModel? = null,
    val inviteCode: String = "",
    val error: String? = null
)
