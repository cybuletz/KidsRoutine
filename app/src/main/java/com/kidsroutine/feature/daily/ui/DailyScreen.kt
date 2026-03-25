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
import com.kidsroutine.core.engine.SeasonalThemeManager


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
// DailyScreen — adds onLootBoxClick
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
    onLootBoxClick: () -> Unit = {},          // ← NEW
    viewModel: DailyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.userId) {
        Log.d("DailyScreen", "Initializing with user: ${currentUser.userId}")
        viewModel.init(currentUser)
    }

    LaunchedEffect(Unit) {
        RefreshEventManager.refreshEvent.collect {
            Log.d("DailyScreen", "🔔 Refresh triggered by push notification!")
            viewModel.init(currentUser)
        }
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
                onLootBoxClick         = onLootBoxClick          // ← NEW
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DailyContent — threads onLootBoxClick down
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
    onLootBoxClick: () -> Unit              // ← NEW
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DailyHeader(
                    uiState              = uiState,
                    onProfileClick       = onProfileClick,
                    onNotificationsClick = onNotificationsClick
                )
            }

            // ← ProgressSection now receives onLootBoxClick
            item { ProgressSection(uiState.dailyState, uiState.currentUser, onLootBoxClick) }

            item {
                val arc = uiState.activeStoryArc
                if (arc != null && !arc.isComplete) {
                    StoryArcBannerCard(arc = arc)
                }
            }

            item {
                Text(
                    text       = "Today's Tasks",
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

        // Chat Bubble
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-78).dp)
                .zIndex(10f)
                .navigationBarsPadding()
        ) {
            ChatBubbleButton(onClick = onFamilyMessagingClick)
        }

        // Bottom Navigation Bar (retained from original — not used when PersistentNavBar is active,
        // but kept so DailyScreen still compiles standalone if ever needed)
        Surface(
            modifier       = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color          = Color.White,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 0.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                NavItemButton(
                    icon       = Icons.Default.Home,
                    label      = "Daily",
                    isSelected = true,
                    onClick    = { },
                    modifier   = Modifier.weight(1f)
                )
                NavItemButton(
                    icon       = Icons.Default.EmojiEvents,
                    label      = "Challenges",
                    isSelected = false,
                    onClick    = onChallengesClick,
                    modifier   = Modifier.weight(1f)
                )
                NavItemButton(
                    icon       = Icons.Default.BarChart,
                    label      = "Leaderboard",
                    isSelected = false,
                    onClick    = onStatsClick,
                    modifier   = Modifier.weight(1f)
                )
                Box(modifier = Modifier.weight(1f)) {
                    NavItemButton(
                        icon      = Icons.Default.Language,
                        label     = "World",
                        isSelected = false,
                        onClick   = { },
                        modifier  = Modifier.fillMaxHeight()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    NavItemButton(
                        icon      = Icons.Default.PhotoAlbum,
                        label     = "Moments",
                        isSelected = false,
                        onClick   = { },
                        modifier  = Modifier.fillMaxHeight()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    NavItemButton(
                        icon      = Icons.Default.EmojiEvents,
                        label     = "Achievements",
                        isSelected = false,
                        onClick   = onAchievementsClick
                    )
                    if (uiState.currentUser.badges.isNotEmpty()) {
                        Surface(
                            shape    = CircleShape,
                            color    = Color(0xFFFF6B35),
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text       = "${uiState.currentUser.badges.size}",
                                    color      = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ProgressSection — loot box teaser is now a real Button
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProgressSection(
    state: DailyStateModel,
    currentUser: UserModel,
    onLootBoxClick: () -> Unit           // ← NEW
) {
    val allDone = state.completedCount == state.tasks.size && state.tasks.isNotEmpty()

    val animatedProgress by animateFloatAsState(
        targetValue    = state.completionPercent,
        animationSpec  = tween(durationMillis = 800, easing = EaseOutCubic),
        label          = "progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "allDonePulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = if (allDone) 0.9f else 0.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label         = "glowAlpha"
    )

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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (allDone) {
                    Text(
                        "🎉 All done today!",
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFFFF6B35),
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

            LinearProgressIndicator(
                progress   = { animatedProgress },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color      = if (allDone) Color(0xFFFFD700) else OrangePrimary,
                trackColor = OrangePrimary.copy(alpha = 0.15f)
            )

            // ── Loot box teaser — real tappable button when all tasks done ──
            AnimatedVisibility(visible = allDone, enter = fadeIn() + expandVertically()) {
                Button(
                    onClick  = onLootBoxClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
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
// Everything below is UNCHANGED from your committed version
// ─────────────────────────────────────────────────────────────────────────────

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
private fun NavItemButton(
    icon: ImageVector,
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
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(0.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint               = if (isSelected) OrangePrimary else Color.Gray,
            modifier           = Modifier.size(24.dp)
        )
        Text(
            text       = label,
            fontSize   = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color      = if (isSelected) OrangePrimary else Color.Gray,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            lineHeight = 10.sp
        )
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
private fun StreakBadge(streak: Int) {
    val scale by animateFloatAsState(
        targetValue   = if (streak > 0) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "streak_scale"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.25f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = "🔥", fontSize = 28.sp)
        Text(
            text       = "$streak",
            style      = MaterialTheme.typography.titleLarge,
            color      = Color.White,
            fontWeight = FontWeight.ExtraBold
        )
        Text(text = "streak", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(0.8f))
    }
}

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
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .scale(scale)
                .alpha(if (isDone) 0.65f else 1f)
                .clickable(enabled = !isDone) {
                    pressed = true
                    onClick()
                },
            shape     = RoundedCornerShape(20.dp),
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
