package com.kidsroutine.feature.daily.ui

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.*
import com.kidsroutine.feature.stats.ui.StreakShieldCard
import com.kidsroutine.feature.tasks.ui.RefreshEventManager
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import com.kidsroutine.core.model.StoryArc
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.alpha
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kidsroutine.core.engine.SeasonalThemeManager
import com.kidsroutine.feature.daily.ui.AiSuggestionCard



// ── Brand colors ──────────────────────────────────────────────────────────────
private val YellowPrimary  = Color(0xFFFFD93D)
private val OrangePrimary  = Color(0xFFFF6B35)
private val TealSecondary  = Color(0xFF4ECDC4)
private val CoopPurple     = Color(0xFF9B5DE5)
private val LogicBlue      = Color(0xFF4361EE)
private val RealLifeGreen  = Color(0xFF06D6A0)
private val BgLight        = Color(0xFFFFFBF0)
private val TextDark       = Color(0xFF2D3436)
private val DoneGreen      = Color(0xFF06D6A0)
private val DoneGreenLight = Color(0xFF06D6A0).copy(alpha = 0.10f)

// ─────────────────────────────────────────────────────────────────────────────
// DailyScreen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DailyScreen(
    currentUser: UserModel,
    onTaskClick: (TaskInstance) -> Unit,
    onChallengesClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onFamilyMessagingClick: () -> Unit,
    onStatsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onLootBoxClick: () -> Unit = {},
    onWorldClick: () -> Unit = {},           // ← NEW: for "X XP away" nudge tap
    viewModel: DailyViewModel = hiltViewModel(),
    ) {
    val uiState by viewModel.uiState.collectAsState()

    // PERFORMANCE FIX: guard key lives in DailyViewModel.init() — safe to call every recomposition
    LaunchedEffect(currentUser.userId) {
        Log.d("DailyScreen", "LaunchedEffect → viewModel.init() for ${currentUser.userId}")
        viewModel.init(currentUser)
    }

    // Push-notification manual refresh
    LaunchedEffect(Unit) {
        RefreshEventManager.refreshEvent.collect {
            Log.d("DailyScreen", "🔔 Push refresh — forceRefresh")
            viewModel.forceRefresh()        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        when {
            uiState.isLoading -> DailyLoadingScreen()
            uiState.dailyState.tasks.isEmpty() -> DailyEmptyScreen()
            else -> DailyContent(
                uiState                = uiState,
                onTaskClick            = onTaskClick,
                onChallengesClick      = onChallengesClick,
                onAchievementsClick    = onAchievementsClick,
                onFamilyMessagingClick = onFamilyMessagingClick,
                onStatsClick           = onStatsClick,
                onProfileClick         = onProfileClick,
                onNotificationsClick   = onNotificationsClick,
                onLootBoxClick         = onLootBoxClick,
                onWorldClick           = onWorldClick,
                viewModel = viewModel
                )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DailyContent — NO bottom nav bar here (handled by ChildMainScreen's PersistentNavBar)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DailyContent(
    uiState: DailyUiState,
    onTaskClick: (TaskInstance) -> Unit,
    onChallengesClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onFamilyMessagingClick: () -> Unit,
    onStatsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onLootBoxClick: () -> Unit,
    onWorldClick: () -> Unit,
    viewModel: DailyViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DailyHeader(
                    uiState              = uiState,
                    onProfileClick       = onProfileClick,
                    onNotificationsClick = onNotificationsClick
                )
            }

            item {
                ProgressSection(
                    state          = uiState.dailyState,
                    currentUser    = uiState.currentUser,
                    onLootBoxClick = onLootBoxClick,
                    onWorldClick   = onWorldClick
                )
            }

            item {
                val arc = uiState.activeStoryArc
                if (arc != null && !arc.isComplete) {
                    StoryArcBannerCard(arc = arc)
                }
            }

            item {
                AiSuggestionCard(
                    currentChild        = uiState.currentUser,
                    completedTaskTitles = uiState.dailyState.tasks
                        .filter { it.status == TaskStatus.COMPLETED }
                        .map { it.task.title },
                    onAccept = { generatedTask ->
                        // ✅ Convert GeneratedTask to TaskModel
                        val taskModel = generatedTask.toTaskModel(uiState.currentUser.familyId)
                        viewModel.addSuggestedTask(taskModel)
                    }
                )
            }

            item {
                Text(
                    text = "⚔️ Today's Quests",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            items(
                items = uiState.dailyState.tasks,
                key   = { it.instanceId }
            ) { instance ->
                TaskCard(
                    instance = instance,
                    onClick  = { onTaskClick(instance) }
                )
            }
        }

        // Chat bubble — positioned above the PersistentNavBar (80dp) + 12dp gap
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-94).dp)
                .zIndex(10f)
                .navigationBarsPadding()
        ) {
            ChatBubbleButton(onClick = onFamilyMessagingClick)
        }
        // ← No Surface / bottom nav bar here — removed entirely
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ProgressSection — segmented pill bar + World nudge banner
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProgressSection(
    state: DailyStateModel,
    currentUser: UserModel,
    onLootBoxClick: () -> Unit,
    onWorldClick: () -> Unit
) {
    val allDone = state.completedCount == state.tasks.size && state.tasks.isNotEmpty()

    val infiniteTransition = rememberInfiniteTransition(label = "allDonePulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = if (allDone) 0.9f else 0.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label         = "glowAlpha"
    )

    // World nudge: show if child is within 200 XP of a world node unlock
    // We pass nextNodeXp via uiState — for now derive a placeholder from current XP
    // When WorldViewModel is wired to DailyScreen this can be real data
    val xpToNextNode = remember(currentUser.xp) {
        // Find the next 50-XP milestone as a simple approximation until WorldViewModel feeds this
        val nextMilestone = ((currentUser.xp / 50) + 1) * 50
        nextMilestone - currentUser.xp
    }
    val showWorldNudge = xpToNextNode in 1..200 && !allDone

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (allDone) Color(0xFFFFD700).copy(alpha = 0.12f) else Color.White
        ),
        border    = if (allDone) BorderStroke(2.dp, Color(0xFFFFD700).copy(alpha = glowAlpha)) else null,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Header row ────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (allDone) {
                    Text(
                        "🎉 All done today!",
                        fontWeight = FontWeight.Bold,
                        color      = OrangePrimary,
                        fontSize   = 16.sp
                    )
                } else {
                    Text(
                        "${state.completedCount}/${state.tasks.size} done",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text("⭐ ${currentUser.xp} XP", color = OrangePrimary, fontWeight = FontWeight.Bold)
            }

            // ── Segmented pill progress bar ───────────────────────────────
            // Each task gets its own pill — task type color fills when done
            if (state.tasks.isNotEmpty()) {
                SegmentedProgressBar(tasks = state.tasks)
            }

            // ── World nudge banner ────────────────────────────────────────
            AnimatedVisibility(visible = showWorldNudge, enter = fadeIn() + expandVertically()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWorldClick() },
                    shape  = RoundedCornerShape(12.dp),
                    color  = Color(0xFF1A1042).copy(alpha = 0.07f),
                    border = BorderStroke(1.dp, Color(0xFF9B5DE5).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🌍", fontSize = 20.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Only $xpToNextNode XP to unlock the next World node!",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color(0xFF4B2DA0)
                            )
                            Text("Complete tasks to get there →", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // ── Loot box button when all done ─────────────────────────────
            AnimatedVisibility(visible = allDone, enter = fadeIn() + expandVertically()) {
                Button(
                    onClick   = onLootBoxClick,
                    modifier  = Modifier.fillMaxWidth().height(52.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("🎁", fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Open your Loot Box!",
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color(0xFF7B4F00),
                        fontSize   = 15.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Segmented progress bar — one pill per task, animated fill on completion
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SegmentedProgressBar(tasks: List<TaskInstance>) {
    if (tasks.isEmpty()) return
    val gapDp = 4.dp
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gapDp)
    ) {
        tasks.forEach { instance ->
            val isDone    = instance.status == TaskStatus.COMPLETED
            val taskColor = taskTypeColor(instance.task.type)

            val animColor by animateColorAsState(
                targetValue   = if (isDone) taskColor else Color(0xFFE0E0E0),
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label         = "segColor_${instance.instanceId}"
            )
            val segScale by animateFloatAsState(
                targetValue   = if (isDone) 1.08f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label         = "segScale_${instance.instanceId}"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .scale(scaleX = 1f, scaleY = segScale)
                    .clip(RoundedCornerShape(5.dp))
                    .background(animColor)
            )
        }
    }
    // Legend row — show category dots if >1 unique type
    val uniqueTypes = tasks.map { it.task.type }.distinct()
    if (uniqueTypes.size > 1) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            uniqueTypes.forEach { type ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(taskTypeColor(type), CircleShape)
                    )
                    Text(
                        text     = taskTypeName(type),
                        fontSize = 9.sp,
                        color    = Color.Gray
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TaskCard — with 4dp left accent stripe
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TaskCard(
    instance: TaskInstance,
    onClick: () -> Unit
) {
    val task   = instance.task
    val isDone = instance.status == TaskStatus.COMPLETED
    val cardColor = if (isDone) DoneGreen else taskTypeColor(task.type)
    val icon      = taskTypeIcon(task.type)

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue   = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "card_press"
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(instance.instanceId) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter   = slideInVertically(initialOffsetY = { 40 }) + fadeIn(tween(300)),
        exit    = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(400))
    ) {
        // Outer box provides the left stripe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .scale(scale)
                .clip(RoundedCornerShape(20.dp))   // ← ADD THIS — clips the stripe artefact
        ) {
            // ── 4dp left accent stripe ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .matchParentSize()
                    .background(cardColor.copy(alpha = if (isDone) 0.4f else 1f))
                    // NO clip here — the parent clip handles it
                    .align(Alignment.CenterStart)
            )
            // ── Card body ──────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
                    .alpha(if (isDone) 0.65f else 1f)
                    .clickable(enabled = !isDone) {
                        pressed = true
                        onClick()
                    },
                shape = RoundedCornerShape(
                    topStart = 0.dp, bottomStart = 0.dp,
                    topEnd = 20.dp, bottomEnd = 20.dp
                ),
                colors    = CardDefaults.cardColors(
                    containerColor = if (isDone) DoneGreenLight else Color.White
                ),
                border    = if (isDone) BorderStroke(1.5.dp, DoneGreen.copy(alpha = 0.4f)) else null,
                elevation = CardDefaults.cardElevation(if (isDone) 0.dp else 3.dp)
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(cardColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Text("✅", fontSize = 26.sp)
                        } else {
                            Icon(
                                imageVector        = icon,
                                contentDescription = null,
                                tint               = cardColor,
                                modifier           = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (task.requiresCoop) TaskChip("CO-OP", CoopPurple)
                            if (instance.injectedByChallengeId != null) TaskChip("CHALLENGE", TealSecondary)
                            if (isDone) TaskChip("DONE", DoneGreen)
                        }
                        Text(
                            text           = task.title,
                            style          = MaterialTheme.typography.titleLarge,
                            fontWeight     = FontWeight.Bold,
                            textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                            color          = if (isDone) Color.Gray else TextDark
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "⭐ ${task.reward.xp} XP",
                                color      = if (isDone) Color.Gray else OrangePrimary,
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("·", color = Color.Gray)
                            Text("~${task.estimatedDurationSec}s", color = Color.Gray, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    if (!isDone) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                    } else {
                        Text("🏆", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

// Fix: ProgressSection references state.dailyState which doesn't exist on DailyStateModel
// The model already IS the daily state — use state.tasks directly
// The SegmentedProgressBar call above passes state.tasks (corrected below in the actual call)

@Composable
private fun ChatBubbleButton(onClick: () -> Unit) {
    Button(
        onClick        = onClick,
        modifier       = Modifier.size(width = 56.dp, height = 50.dp),
        shape          = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp),
        colors         = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC407A)),
        contentPadding = PaddingValues(0.dp),
        elevation      = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Icon(Icons.Default.Message, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun TaskChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label, color = color, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

@Composable
private fun DailyHeader(
    uiState: DailyUiState,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(YellowPrimary, OrangePrimary)))
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val themeManager = remember { SeasonalThemeManager() }
            val theme        = remember { themeManager.getActiveTheme() }
            if (theme.bannerText.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape  = RoundedCornerShape(16.dp),
                    color  = Color.White.copy(alpha = 0.25f),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    Text(
                        text       = theme.bannerText,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White,
                        modifier   = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        maxLines   = 1
                    )
                }
            }

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "Hey, ${uiState.currentUser.displayName}! 👋",
                        style      = MaterialTheme.typography.headlineMedium,
                        color      = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text  = "Ready for today?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick  = onNotificationsClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint               = Color.White,
                            modifier           = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick  = onProfileClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint               = Color.White,
                            modifier           = Modifier.size(20.dp)
                        )
                    }

                    StreakShieldCard(
                        streak       = uiState.currentUser.streak,
                        shieldActive = uiState.currentUser.streakShieldActive,
                        showLabel    = false
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyLoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = OrangePrimary, strokeWidth = 4.dp)
            Text("Loading your tasks…", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
    }
}

@Composable
private fun DailyEmptyScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🎉", fontSize = 64.sp)
            Text("All done for today!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Come back tomorrow!", color = Color.Gray)
        }
    }
}

@Composable
fun StoryArcBannerCard(
    arc: StoryArc,
    modifier: Modifier = Modifier
) {
    val currentChapter = arc.chapters.getOrNull(arc.currentDay - 1) ?: return

    val infiniteTransition = rememberInfiniteTransition(label = "storyGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 0.9f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "glowAlpha"
    )

    val storyColor = Color(0xFF8B5CF6)

    Card(
        modifier  = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = storyColor.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(1.5.dp, storyColor.copy(alpha = glowAlpha))
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(storyColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(arc.arcEmoji, fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "📖 ${arc.arcTitle} — Day ${arc.currentDay}/3",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = storyColor
                )
                Text(
                    text       = currentChapter.chapterTitle,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextDark,
                    modifier   = Modifier.padding(top = 2.dp)
                )
                Text(
                    text     = currentChapter.narrative,
                    fontSize = 12.sp,
                    color    = Color.Gray,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = storyColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text       = "⭐ ${currentChapter.xpReward}",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = storyColor,
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// task type helpers — public so WorldScreen can reference them if needed
// ─────────────────────────────────────────────────────────────────────────────
fun taskTypeColor(type: TaskType): Color = when (type) {
    TaskType.LOGIC     -> LogicBlue
    TaskType.REAL_LIFE -> RealLifeGreen
    TaskType.CO_OP     -> CoopPurple
    TaskType.CREATIVE  -> Color(0xFFFF9F1C)
    TaskType.LEARNING  -> Color(0xFF4361EE)
    TaskType.EMOTIONAL -> Color(0xFFEF476F)
    TaskType.SOCIAL    -> TealSecondary
    TaskType.STORY     -> Color(0xFF8B5CF6)
}

fun taskTypeName(type: TaskType): String = when (type) {
    TaskType.LOGIC     -> "Logic"
    TaskType.REAL_LIFE -> "Life"
    TaskType.CO_OP     -> "Co-op"
    TaskType.CREATIVE  -> "Creative"
    TaskType.LEARNING  -> "Learn"
    TaskType.EMOTIONAL -> "Emotion"
    TaskType.SOCIAL    -> "Social"
    TaskType.STORY     -> "Story"
}

fun taskTypeIcon(type: TaskType): ImageVector = when (type) {
    TaskType.LOGIC     -> Icons.Default.Psychology
    TaskType.REAL_LIFE -> Icons.Default.Home
    TaskType.CO_OP     -> Icons.Default.People
    TaskType.CREATIVE  -> Icons.Default.Brush
    TaskType.LEARNING  -> Icons.Default.MenuBook
    TaskType.EMOTIONAL -> Icons.Default.Favorite
    TaskType.SOCIAL    -> Icons.Default.EmojiPeople
    TaskType.STORY     -> Icons.Default.AutoStories
}
