package com.kidsroutine.feature.family.ui

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.common.collect.Multimaps.index
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.TaskInstance
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.TaskStatus
import com.kidsroutine.core.model.TaskType
import com.kidsroutine.feature.challenges.ui.ActiveChallengesScreen
import com.kidsroutine.feature.challenges.ui.ChallengeDetailScreen
import com.kidsroutine.feature.community.ui.MarketplaceScreen
import com.kidsroutine.feature.community.ui.PublishScreen
import com.kidsroutine.feature.generation.ui.GenerationScreen
import com.kidsroutine.feature.generation.ui.WeeklyPlanScreen
import com.kidsroutine.feature.notifications.ui.NotificationViewModel
import com.kidsroutine.feature.onboarding.ui.ParentOnboardingWizard
import com.kidsroutine.feature.parent.ui.ParentAiInsightCard
import com.kidsroutine.feature.parent.ui.ParentPendingTasksScreen
import com.kidsroutine.feature.parent.ui.ParentPrivilegeApprovalsScreen
import com.kidsroutine.feature.parent.ui.ParentControlsScreen
import com.kidsroutine.feature.settings.ui.SettingsScreen
import com.kidsroutine.feature.tasks.ui.TaskDetailsScreen
import com.kidsroutine.feature.tasks.ui.TaskListScreen
import com.kidsroutine.feature.tasks.ui.TaskManagementViewModel
import com.kidsroutine.feature.notifications.ui.NotificationsScreen

private val OrangePrimary = Color(0xFFFF6B35)
private val GradientStart = Color(0xFFFF6B35)
private val GradientEnd   = Color(0xFFFFD93D)
private val BgLight       = Color(0xFFFFFBF0)
private val TextDark      = Color(0xFF2D3436)
private val PinkChat      = Color(0xFFEC407A)
private val RingDone      = Color(0xFF06D6A0)
private val RingTrack     = Color(0xFFE0E0E0)

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
    onSwitchToChild: (UserModel) -> Unit = {},
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

        NavHost(
            navController    = innerNav,
            startDestination = "home",
            modifier         = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
        ) {
            composable("home") {
                currentTab = "home"
                ParentHomeTab(
                    currentUser               = currentUser,
                    familyMembers             = familyMembers.filter { it.userId != currentUser.userId },
                    uiState                   = uiState,
                    unreadCount               = notifState.unreadCount,
                    onNotificationsClick      = { innerNav.navigate("notifications") },
                    onProfileClick            = onProfileClick,
                    onPendingClick            = { innerNav.navigate("tasks") },
                    onFamilyMessagingClick    = onFamilyMessagingClick,
                    onCreateTaskClick         = { innerNav.navigate("tasks") },
                    onPrivilegeApprovalsClick = { innerNav.navigate("privilege_approvals") },
                    onChallengesClick         = { innerNav.navigate("tasks_challenges") },
                    onGenerateForChild        = { innerNav.navigate("generation") },
                    onTaskDetailsClick        = { task -> innerNav.navigate("taskDetails/${task.id}") },
                    onChildControlsClick      = { child -> innerNav.navigate("parent_controls/${child.userId}") }
                )
            }
            composable("tasks") {
                currentTab = "tasks"
                ParentTasksTab(
                    currentUser    = currentUser,
                    onUpgradeClick = onUpgradeClick,
                    onTaskDetailsClick = { task -> innerNav.navigate("taskDetails/${task.id}") }
                )
            }

            composable(
                "taskDetails/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
                val taskManagementViewModel: TaskManagementViewModel = hiltViewModel()
                val taskUiState by taskManagementViewModel.uiState.collectAsState()

                // Load tasks if not already loaded
                LaunchedEffect(currentUser.familyId) {
                    if (taskUiState.tasks.isEmpty()) {
                        taskManagementViewModel.loadFamilyTasks(currentUser.familyId)
                    }
                }

                val task = taskUiState.tasks.find { it.id == taskId } ?: return@composable

                TaskDetailsScreen(
                    task = task,
                    familyId = currentUser.familyId,
                    onBackClick = { innerNav.popBackStack() },
                    onTaskDeleted = {
                        innerNav.popBackStack()
                        taskManagementViewModel.loadFamilyTasks(currentUser.familyId)
                    },
                    onTaskUpdated = {
                        taskManagementViewModel.loadFamilyTasks(currentUser.familyId)
                    },
                    viewModel = taskManagementViewModel
                )
            }


            composable("tasks_challenges") {
                currentTab = "tasks"
                ParentTasksTab(
                    currentUser          = currentUser,
                    onUpgradeClick       = onUpgradeClick,
                    initialSegment       = 2
                )
            }
            composable("family") {
                currentTab = "family"
                ParentFamilyTab(
                    currentUser       = currentUser,
                    familyMembers     = familyMembers.filter { it.userId != currentUser.userId },
                    uiState           = uiState,
                    onChallengesClick = { innerNav.navigate("tasks_challenges") },
                    onSwitchToChild   = onSwitchToChild   // ← NEW
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
                    currentUser         = currentUser,
                    familyInviteCode    = uiState.inviteCode,
                    onSignOutClick      = onSignOutClick,
                    onUpgradeClick      = onUpgradeClick
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
            composable("privilege_approvals") {
                currentTab = "home"
                ParentPrivilegeApprovalsScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNav.popBackStack() }
                )
            }
            composable(
                "parent_controls/{childId}",
                arguments = listOf(navArgument("childId") { type = NavType.StringType })
            ) { backStackEntry ->
                val childId = backStackEntry.arguments?.getString("childId") ?: return@composable
                val child = familyMembers.find { it.userId == childId } ?: return@composable
                currentTab = "home"
                ParentControlsScreen(
                    currentUser = currentUser,
                    child = child,
                    onBackClick = { innerNav.popBackStack() },
                    onUpgradeClick = onUpgradeClick
                )
            }
        }

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

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-90).dp)
                .zIndex(10f)
                .navigationBarsPadding()
        ) {
            Button(
                onClick        = onFamilyMessagingClick,
                modifier       = Modifier.size(width = 56.dp, height = 50.dp),
                shape          = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp),
                colors         = ButtonDefaults.buttonColors(containerColor = PinkChat),
                contentPadding = PaddingValues(0.dp),
                elevation      = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Default.Message, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

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
    onPrivilegeApprovalsClick: () -> Unit,
    onChallengesClick: () -> Unit = {},
    onGenerateForChild: (UserModel) -> Unit = {},
    onTaskDetailsClick: (TaskModel) -> Unit = {},
    onChildControlsClick: (UserModel) -> Unit = {}
) {
    var selectedChild by remember { mutableStateOf<UserModel?>(null) }

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
                        Text("Good ${timeOfDayGreeting()} 👋", fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
                        Text(currentUser.displayName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        if (uiState.family != null) {
                            Text(uiState.family.familyName, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box {
                            IconButton(
                                onClick  = onNotificationsClick,
                                modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            if (unreadCount > 0) {
                                Surface(shape = CircleShape, color = Color(0xFFE74C3C), modifier = Modifier.size(16.dp).align(Alignment.TopEnd)) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text(if (unreadCount > 9) "9+" else "$unreadCount", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        IconButton(
                            onClick  = onProfileClick,
                            modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        if (uiState.family != null) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp).offset(y = (-16).dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip("⭐", "${uiState.family.familyXp} XP",   Modifier.weight(1f))
                StatChip("🔥", "${uiState.family.familyStreak}d", Modifier.weight(1f))
                StatChip("👥", "${uiState.family.memberIds.size}", Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(4.dp))

        if (unreadCount > 0) {
            ActionRequiredBanner(
                message = "$unreadCount item${if (unreadCount > 1) "s" else ""} need your attention",
                onClick  = onNotificationsClick,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        if (familyMembers.isNotEmpty()) {
            Text("Your Children", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(horizontal = 20.dp))
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
            Spacer(Modifier.height(16.dp))

            ParentAiInsightCard(
                children           = familyMembers,
                onGenerateForChild = onGenerateForChild
            )

            Spacer(Modifier.height(8.dp))
        }

        Text("Quick Actions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(12.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionRow(Icons.Default.Add,         Color(0xFF4A90E2), "Create a Task",       "Assign a new task to your children",  onCreateTaskClick)
            QuickActionRow(Icons.Default.Pending,     Color(0xFFFF9800), "Child Proposals",     "Tasks your children want to do",      onPendingClick)
            QuickActionRow(Icons.Default.EmojiEvents, Color(0xFF9B59B6), "Start a Challenge",   "Set a family-wide goal",              onChallengesClick)
            QuickActionRow(Icons.Default.Shield,      Color(0xFF06D6A0), "Privilege Approvals", "Review requests from your children",  onPrivilegeApprovalsClick)
        }

        // ── Fun Zone Analytics ────────────────────────────────────────
        if (familyMembers.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text(
                "🎮 Fun Zone Analytics",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = TextDark,
                modifier   = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "How your children spend their XP",
                fontSize = 12.sp,
                color    = Color.Gray,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))

            familyMembers.forEach { child ->
                FunZoneAnalyticsCard(
                    child = child,
                    onControlsClick = { onChildControlsClick(child) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(140.dp))
    }

    selectedChild?.let { child ->
        ChildDetailSheet(child = child, onDismiss = { selectedChild = null }, onMessageClick = onFamilyMessagingClick)
    }

    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("parent_prefs", android.content.Context.MODE_PRIVATE) }
    val wizardSeen = remember { prefs.getBoolean("wizard_seen", false) }
    var showWizard by remember { mutableStateOf(!wizardSeen && familyMembers.isEmpty()) }

    if (showWizard && uiState.family != null) {
        ParentOnboardingWizard(
            parentName = currentUser.displayName,
            inviteCode = uiState.inviteCode,
            onDismiss  = {
                prefs.edit().putBoolean("wizard_seen", true).apply()
                showWizard = false
            }
        )
    }
}

@Composable
private fun ChildSummaryCard(child: UserModel, onClick: () -> Unit) {
    var isOnline by remember { mutableStateOf(false) }
    var completedToday by remember { mutableStateOf(0) }
    var totalToday     by remember { mutableStateOf(0) }

    LaunchedEffect(child.userId) {
        val db = FirebaseFirestore.getInstance()
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        db.collection("users").document(child.userId)
            .addSnapshotListener { snap, _ -> isOnline = snap?.getBoolean("isOnline") ?: false }

        // ✅ FIX: Assignments have no assignedDate field — query all, count total
        db.collection("families").document(child.familyId)
            .collection("users").document(child.userId)
            .collection("assignments")
            .addSnapshotListener { assignSnap, err ->
                if (err != null || assignSnap == null) return@addSnapshotListener
                totalToday = assignSnap.size()
            }

        // ✅ FIX: Completed count comes from task_progress filtered by today's date
        db.collection("families").document(child.familyId)
            .collection("users").document(child.userId)
            .collection("task_progress")
            .whereEqualTo("date", today)
            .addSnapshotListener { progressSnap, err ->
                if (err != null || progressSnap == null) return@addSnapshotListener
                completedToday = progressSnap.size()
            }
    }

    Card(
        modifier  = Modifier
            .height(80.dp)
            .width(180.dp)
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Avatar + Online indicator ──
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFFFF6B35).copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("👤", fontSize = 22.sp)
                    }
                }
                // Online/Offline bullet
                if (isOnline) {
                    Surface(
                        modifier = Modifier
                            .size(9.dp)
                            .align(Alignment.BottomEnd),
                        shape = CircleShape,
                        color = Color(0xFF4CAF50)
                    ) {}
                }
            }

            // ── Name + Task Progress (on the right) ──
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    child.displayName.split(" ").first(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF2D3436),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    "$completedToday/$totalToday",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (totalToday == 0) Color.Gray else Color(0xFFFF6B35)
                )
            }
        }
    }
}

@Composable
private fun ParentTasksTab(
    currentUser: UserModel,
    onUpgradeClick: () -> Unit,
    initialSegment: Int = 0,
    onTaskDetailsClick: (TaskModel) -> Unit = {}  // ← ADD THIS
) {
    var selectedSegment    by remember { mutableStateOf(initialSegment) }
    var showCreateTask     by remember { mutableStateOf(false) }
    var createdTask        by remember { mutableStateOf<com.kidsroutine.core.model.TaskModel?>(null) }
    var showStartChallenge by remember { mutableStateOf(false) }
    var selectedChallengeId by remember { mutableStateOf<String?>(null) }

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
        showStartChallenge -> {
            com.kidsroutine.feature.challenges.ui.StartChallengesScreen(
                currentUser        = currentUser,
                onBackClick        = { showStartChallenge = false },
                onChallengeStarted = { showStartChallenge = false }
            )
            return
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tasks", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (selectedSegment == 0) {
                    IconButton(onClick = { showCreateTask = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Task", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
                if (selectedSegment == 2) {
                    IconButton(onClick = { showStartChallenge = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New Challenge", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("My Tasks", "Proposals", "Challenges").forEachIndexed { index, label ->
                Surface(
                    modifier        = Modifier.weight(1f).clickable { selectedSegment = index },
                    shape           = RoundedCornerShape(10.dp),
                    color           = if (selectedSegment == index) Color.White else Color.Transparent,
                    shadowElevation = if (selectedSegment == index) 2.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                        Text(label, fontSize = 13.sp, fontWeight = if (selectedSegment == index) FontWeight.Bold else FontWeight.Medium, color = if (selectedSegment == index) OrangePrimary else Color.Gray)
                    }
                }
            }
        }

        // ── Info Label (only when Challenges tab is selected) ───────────────────────────────
        if (selectedSegment == 2) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF6C5CE7).copy(alpha = 0.1f)
            ) {
                Text(
                    "💡 Challenges are for the entire family — set them for everyone to enjoy!",
                    fontSize = 12.sp,
                    color = Color(0xFF6C5CE7),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        when (selectedSegment) {
            0 -> TaskListScreen(
                currentUser = currentUser,
                onCreateTaskClick = { showCreateTask = true },
                onTaskDetailsClick = onTaskDetailsClick  // ← PASS IT HERE
            )
            1 -> ParentPendingTasksScreen(currentUser = currentUser, onBackClick = { })
            2 -> ActiveChallengesScreen(
                currentUser           = currentUser,
                onBackClick           = { },
                onStartChallengeClick = { showStartChallenge = true },
                onChallengeClick      = { challenge -> selectedChallengeId = challenge.challengeId },
                onViewDetailClick     = { challenge -> selectedChallengeId = challenge.challengeId },
                showHeader            = false,
                showDeleteButton      = true
            )
        }
    }
    selectedChallengeId?.let { challengeId ->
        ChallengeDetailScreen(
            currentUser = currentUser,
            challengeId = challengeId,
            onBackClick = { selectedChallengeId = null }
        )
    }
}

@Composable
private fun ParentFamilyTab(
    currentUser: UserModel,
    familyMembers: List<UserModel>,
    uiState: ParentDashboardUiState,
    onChallengesClick: () -> Unit,
    onSwitchToChild: (UserModel) -> Unit = {}
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(BgLight), contentPadding = PaddingValues(bottom = 140.dp)) {
        item {
            Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(GradientStart, GradientEnd))).statusBarsPadding().padding(horizontal = 20.dp, vertical = 20.dp)) {
                Text("Family", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(20.dp))
        }
        item {
            Text("Children", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))
        }
        if (familyMembers.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👶", fontSize = 40.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No children yet", fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Share your invite code from Settings", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        familyMembers.forEachIndexed { index, child ->
                            FamilyMemberRowWithLogin(
                                child = child,
                                onLoginAsChild = { onSwitchToChild(child) }
                            )
                            if (index < familyMembers.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Challenges", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                TextButton(onClick = onChallengesClick) { Text("Manage", color = OrangePrimary, fontSize = 13.sp) }
            }
            Spacer(Modifier.height(8.dp))
        }
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable(onClick = onChallengesClick), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Surface(modifier = Modifier.size(46.dp), shape = RoundedCornerShape(13.dp), color = Color(0xFF9B59B6).copy(alpha = 0.12f)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text("🏆", fontSize = 22.sp) }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("View Challenges", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                        Text("Manage active family challenges", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
        }
        item {
            Spacer(Modifier.height(24.dp))
            Text("Family Leaderboard", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(8.dp))
        }
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    val ranked = familyMembers.sortedByDescending { it.xp }
                    if (ranked.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No members to rank yet", color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        ranked.forEachIndexed { index, member ->
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = when (index) { 0 -> Color(0xFFFFD700); 1 -> Color(0xFFB0BEC5); 2 -> Color(0xFFCD7F32); else -> Color(0xFFEEEEEE) }) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text("${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (index < 3) Color.White else Color.Gray)
                                    }
                                }
                                Text(member.displayName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.weight(1f))
                                Text("${member.xp} XP", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangePrimary)
                            }
                            if (index < ranked.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyMemberRowWithLogin(child: UserModel, onLoginAsChild: () -> Unit) {
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = OrangePrimary.copy(alpha = 0.15f)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text("👤", fontSize = 22.sp) }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(displayName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextDark)
                Surface(modifier = Modifier.size(7.dp), shape = CircleShape, color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFBDBDBD)) {}
                Text(if (isOnline) "Online" else "Offline", fontSize = 10.sp, color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFBDBDBD))
            }
            Text("Level ${child.level} · ${child.xp} XP · 🔥 ${child.streak}", fontSize = 12.sp, color = Color.Gray)
        }
        // Login as Child button
        OutlinedButton(
            onClick = onLoginAsChild,
            modifier = Modifier.height(32.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary)
        ) {
            Text("Switch", fontSize = 11.sp, color = OrangePrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ParentDiscoverTab(
    currentUser: UserModel,
    familyMembers: List<UserModel>,
    onUpgradeClick: () -> Unit,
    onGenerationClick: () -> Unit,
    onWeeklyPlanClick: () -> Unit,
    onMarketplaceClick: () -> Unit,
    onPublishClick: () -> Unit,
    onContentPacksClick: () -> Unit = {},
    onModerationClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight).verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(GradientStart, GradientEnd))).statusBarsPadding().padding(horizontal = 20.dp, vertical = 20.dp)) {
            Text("Discover", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.height(20.dp))

        // ── Tools ──────────────────────────────────────────────────────
        Text("Tools", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(8.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DiscoverCard("✨", "AI Task Generator",  "Create personalised tasks using AI",        Color(0xFF4A90E2), onGenerationClick)
            DiscoverCard("📅", "Weekly Planner",     "AI 7-day family schedule · PRO",            Color(0xFF11998E), onWeeklyPlanClick)
            DiscoverCard("🎁", "Content Packs",      "Browse & unlock themed task packs",         Color(0xFF667EEA), onContentPacksClick)
            DiscoverCard("🌍", "Community Library",  "Browse tasks shared by other families",     Color(0xFF667EEA), onMarketplaceClick)
            DiscoverCard("📤", "Publish Content",    "Share your best tasks with the community",  Color(0xFFFF6B35), onPublishClick)
            if (currentUser.isAdmin) {
                DiscoverCard("🛡️", "Moderation Panel", "Admin only — review community content",  Color(0xFFE74C3C), onModerationClick)
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Family Features ────────────────────────────────────────────
        Text("Family Features", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(horizontal = 20.dp))
        Text("Activities your children enjoy in the Fun Zone", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(8.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FamilyFeaturePreviewCard("🐾", "Companion Pet",     "Kids adopt & care for a virtual pet using earned XP.",       Color(0xFF06D6A0), "Free")
            FamilyFeaturePreviewCard("🎡", "Daily Spin Wheel",  "A daily reward spin for fun surprises.",                     Color(0xFFFF9F1C), "Free")
            FamilyFeaturePreviewCard("🙏", "Family Rituals",    "Gratitude circles, family meetings & bonding moments.",      Color(0xFF9B5DE5), "Free")
            FamilyFeaturePreviewCard("⚔️", "Boss Battles",      "Weekly cooperative boss battles for bonus rewards.",         Color(0xFFEF476F), "Pro")
            FamilyFeaturePreviewCard("📖", "Story Arcs",        "Multi-day narrative adventures that unfold with tasks.",     Color(0xFF8B5CF6), "Pro")
            FamilyFeaturePreviewCard("📅", "Timed Events",      "Seasonal limited-time challenges & exclusive rewards.",      Color(0xFF4361EE), "Pro")
            FamilyFeaturePreviewCard("🌳", "Skill Trees",       "Visual skill progression & ability unlocks.",               Color(0xFF667EEA), "Pro")
            FamilyFeaturePreviewCard("💰", "Family Wallet",     "Savings goals & financial literacy for kids.",               Color(0xFF11998E), "Premium")
        }
        Spacer(Modifier.height(140.dp))
    }
}

@Composable
private fun ParentNavBar(currentTab: String, unreadNotifications: Int, onHomeClick: () -> Unit, onTasksClick: () -> Unit, onFamilyClick: () -> Unit, onDiscoverClick: () -> Unit, onSettingsClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), color = Color.White, shadowElevation = 12.dp) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().height(64.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
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
private fun ParentNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier            = Modifier.clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (isSelected) OrangePrimary else Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) OrangePrimary else Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StatChip(emoji: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(emoji, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
    }
}

@Composable
private fun ActionRequiredBanner(message: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("⚠️", fontSize = 20.sp)
            Text(message, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF856404), modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun FamilyMemberRow(child: UserModel) {
    var isOnline    by remember { mutableStateOf(false) }
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
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
// ─────────────────────────────────────────────────────────────────────────────
// Fun Zone Analytics Card — shows XP economy per child
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FunZoneAnalyticsCard(child: UserModel, onControlsClick: () -> Unit = {}) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Child name + level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = OrangePrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👤", fontSize = 18.sp)
                        }
                    }
                    Column {
                        Text(
                            child.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextDark
                        )
                        Text(
                            "Level ${child.level} · ${child.ageGroup.name}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Text(
                        "⭐ ${child.xp} XP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = OrangePrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnalyticsMiniStat(
                    emoji = "🔥",
                    value = "${child.streak}d",
                    label = "Streak",
                    color = Color(0xFFC62828),
                    modifier = Modifier.weight(1f)
                )
                AnalyticsMiniStat(
                    emoji = "📊",
                    value = "${child.weeklyXp}",
                    label = "Weekly XP",
                    color = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f)
                )
                AnalyticsMiniStat(
                    emoji = "🏆",
                    value = child.league.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    label = "League",
                    color = Color(0xFF6A1B9A),
                    modifier = Modifier.weight(1f)
                )
                AnalyticsMiniStat(
                    emoji = "🐾",
                    value = if (child.petId.isNotEmpty()) "Active" else "None",
                    label = "Pet",
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }

            // Level progress bar — each level requires (level * 100) XP total
            val xpForCurrentLevel = if (child.level > 1) (child.level - 1) * 100 else 0
            val xpForNextLevel = child.level * 100
            val xpInCurrentLevel = (child.xp - xpForCurrentLevel).coerceAtLeast(0)
            val xpNeeded = (xpForNextLevel - xpForCurrentLevel).coerceAtLeast(1)
            val xpProgress = xpInCurrentLevel.toFloat() / xpNeeded
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Level Progress", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Text("${(xpProgress * 100).toInt()}% to Lvl ${child.level + 1}", fontSize = 10.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { xpProgress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = OrangePrimary,
                    trackColor = Color(0xFFEEEEEE)
                )
            }

            // Controls button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onControlsClick),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF6C63FF).copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Controls",
                        tint = Color(0xFF6C63FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "⚙️ Manage Controls & XP Bank",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFF6C63FF)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsMiniStat(
    emoji: String,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 16.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                fontSize = 9.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickActionRow(icon: ImageVector, color: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.12f)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp)) }
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
private fun DiscoverCard(emoji: String, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
        Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(modifier = Modifier.size(46.dp), shape = RoundedCornerShape(13.dp), color = color.copy(alpha = 0.12f)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text(emoji, fontSize = 22.sp) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun FamilyFeaturePreviewCard(emoji: String, title: String, description: String, color: Color, tier: String) {
    val tierColor = when (tier) {
        "Free"    -> Color(0xFF4CAF50)
        "Pro"     -> Color(0xFF7C3AED)
        "Premium" -> Color(0xFFFF9800)
        else      -> Color.Gray
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(13.dp),
                color = color.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(emoji, fontSize = 22.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                Text(description, fontSize = 12.sp, color = Color.Gray)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tierColor.copy(alpha = 0.12f)
            ) {
                Text(
                    tier,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = tierColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
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
    var showTasksPopup by remember { mutableStateOf(false) }
    var isOnline by remember { mutableStateOf(false) }
    var todaysTasks by remember { mutableStateOf(0) }
    var completedTasks by remember { mutableStateOf(0) }

    val today = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }

    LaunchedEffect(child.userId) {
        val db = FirebaseFirestore.getInstance()
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        db.collection("users").document(child.userId)
            .addSnapshotListener { snap, _ -> isOnline = snap?.getBoolean("isOnline") ?: false }

        // ✅ FIX: Total tasks = all assigned tasks (no date filter on assignments)
        db.collection("families").document(child.familyId)
            .collection("users").document(child.userId)
            .collection("assignments")
            .addSnapshotListener { snap, _ ->
                if (snap != null) todaysTasks = snap.size()
            }

        // ✅ FIX: Completed count = task_progress entries for today
        db.collection("families").document(child.familyId)
            .collection("users").document(child.userId)
            .collection("task_progress")
            .whereEqualTo("date", today)
            .addSnapshotListener { snap, _ ->
                if (snap != null) completedTasks = snap.size()
            }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = Color(0xFFFF6B35).copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("👤", fontSize = 36.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    child.displayName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436)
                )
                Surface(
                    modifier = Modifier.size(10.dp),
                    shape = CircleShape,
                    color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFBDBDBD)
                ) {}
            }
            Text(
                "Level ${child.level}",
                fontSize = 14.sp,
                color = Color(0xFFFF6B35),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChildStatItem("⭐", "${child.xp}", "Total XP")
                ChildStatItem("🔥", "${child.streak}", "Day Streak")
                ChildStatItem("🏆", "Lv ${child.level}", "Level")
            }
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(20.dp))

            // ✅ TODAY'S TASKS SECTION with real-time updates
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTasksPopup = true }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Today's Tasks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3436))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF6B35).copy(alpha = 0.12f)
                ) {
                    Text(
                        "View Details",
                        fontSize = 12.sp,
                        color = Color(0xFFFF6B35),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            // ✅ Show real-time progress
            if (todaysTasks > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "$completedTasks of $todaysTasks done today",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (todaysTasks > 0) completedTasks.toFloat() / todaysTasks.toFloat() else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            trackColor = Color(0xFFCCE4CA),
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF9F9F9),
                    tonalElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No tasks assigned today",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onDismiss(); onMessageClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC407A))
            ) {
                Icon(Icons.Default.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Message ${child.displayName.split(" ").first()}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ✅ SHOW TASKS POPUP MODAL
        if (showTasksPopup) {
            TasksDetailsPopup(
                child = child,
                todaysTasks = todaysTasks,
                completedTasks = completedTasks,
                onDismiss = { showTasksPopup = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksDetailsPopup(
    child: UserModel,
    todaysTasks: Int,
    completedTasks: Int,
    onDismiss: () -> Unit
) {
    // 1. State — must be FIRST
    var tasks by remember { mutableStateOf<List<TaskInstance>>(emptyList()) }

    // 2. today — must be defined BEFORE LaunchedEffect
    val today = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    // 3. LaunchedEffect — uses both 'today' and 'tasks'
    // ✅ Fetch tasks from Firestore
    LaunchedEffect(child.userId) {
        val db = FirebaseFirestore.getInstance()
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        // ✅ Step 1: Get completed templateIds from task_progress for today
        db.collection("families").document(child.familyId)
            .collection("users").document(child.userId)
            .collection("task_progress")
            .whereEqualTo("date", today)
            .addSnapshotListener { progressSnap, _ ->
                val completedTemplateIds = progressSnap?.documents
                    ?.mapNotNull { it.getString("templateId") }
                    ?.toSet() ?: emptySet()

                // ✅ Step 2: Load all assignments and resolve real task titles
                db.collection("families").document(child.familyId)
                    .collection("users").document(child.userId)
                    .collection("assignments")
                    .get()
                    .addOnSuccessListener { assignSnap ->
                        val docs = assignSnap.documents
                        if (docs.isEmpty()) { tasks = emptyList(); return@addOnSuccessListener }

                        val resolved = mutableListOf<TaskInstance>()
                        var remaining = docs.size

                        docs.forEach { doc ->
                            val taskId = doc.getString("taskId") ?: ""
                            val isCompleted = taskId in completedTemplateIds

                            // ✅ Fetch real title from families/{familyId}/tasks/{taskId}
                            db.collection("families").document(child.familyId)
                                .collection("tasks").document(taskId)
                                .get()
                                .addOnSuccessListener { taskDoc ->
                                    val title = taskDoc.getString("title") ?: taskId
                                    resolved.add(
                                        TaskInstance(
                                            instanceId   = doc.id,
                                            templateId   = taskId,
                                            userId       = child.userId,
                                            assignedDate = today,
                                            status       = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.PENDING,
                                            completedAt  = 0L,
                                            task         = TaskModel(id = taskId, title = title)
                                        )
                                    )
                                    remaining--
                                    if (remaining == 0) tasks = resolved.sortedBy { it.status }
                                }
                                .addOnFailureListener {
                                    resolved.add(
                                        TaskInstance(
                                            instanceId   = doc.id,
                                            templateId   = taskId,
                                            userId       = child.userId,
                                            assignedDate = today,
                                            status       = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.PENDING,
                                            completedAt  = 0L,
                                            task         = TaskModel(id = taskId, title = "Task ($taskId)")
                                        )
                                    )
                                    remaining--
                                    if (remaining == 0) tasks = resolved.sortedBy { it.status }
                                }
                        }
                    }
            }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // ── Header ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${child.displayName}'s Tasks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3436)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Progress ──
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "$completedTasks/$todaysTasks completed",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (todaysTasks > 0) completedTasks.toFloat() / todaysTasks.toFloat() else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            trackColor = Color(0xFFCCE4CA),
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Task List ──
                Text(
                    "Tasks",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D3436)
                )

                Spacer(Modifier.height(8.dp))

                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No tasks assigned",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tasks) { task ->
                            TaskRowItem(task)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Close Button ──
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TaskRowItem(task: TaskInstance) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (task.status == TaskStatus.COMPLETED)
                        Color(0xFF06D6A0).copy(alpha = 0.2f)
                    else
                        Color(0xFFFF6B35).copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (task.status == TaskStatus.COMPLETED) "✅" else "⏳",
                fontSize = 16.sp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.task.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D3436),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                if (task.status == TaskStatus.COMPLETED) "✓ Done" else "Pending",
                fontSize = 11.sp,
                color = if (task.status == TaskStatus.COMPLETED) Color(0xFF06D6A0) else Color.Gray
            )
        }
    }
}

@Composable
private fun ChildStatItem(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
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

data class ParentDashboardUiState(
    val isLoading: Boolean = false,
    val family: com.kidsroutine.core.model.FamilyModel? = null,
    val inviteCode: String = "",
    val error: String? = null
)
