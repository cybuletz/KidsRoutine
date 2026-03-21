package com.kidsroutine.feature.daily.ui

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.*
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem

// ── Brand colors ──────────────────────────────────────────────────────────────
private val YellowPrimary = Color(0xFFFFD93D)
private val OrangePrimary = Color(0xFFFF6B35)
private val TealSecondary = Color(0xFF4ECDC4)
private val CoopPurple    = Color(0xFF9B5DE5)
private val LogicBlue     = Color(0xFF4361EE)
private val RealLifeGreen = Color(0xFF06D6A0)
private val BgLight       = Color(0xFFFFFBF0)

@Composable
fun DailyScreen(
    currentUser: UserModel,
    onTaskClick: (TaskInstance) -> Unit,
    onChallengesClick: () -> Unit,
    onAchievementsClick: () -> Unit,  // ← ADD THIS
    onStatsClick: () -> Unit,
    viewModel: DailyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.userId) {
        Log.d("DailyScreen", "Initializing with user: ${currentUser.userId}")
        viewModel.init(currentUser)
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
                uiState = uiState,
                onTaskClick = onTaskClick,
                onChallengesClick = onChallengesClick,
                onStatsClick = onStatsClick
            )
        }
    }
}

@Composable
private fun DailyContent(
    uiState: DailyUiState,
    onTaskClick: (TaskInstance) -> Unit,
    onChallengesClick: () -> Unit,
    onAchievementsClick: () -> Unit,  // ← ADD THIS
    onStatsClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
              //  .paddingFromBaseline(bottom = 80.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { DailyHeader(uiState) }
            item { ProgressSection(uiState.dailyState, uiState.currentUser) }
            item {
                Text(
                    text = "Today's Tasks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            items(
                items = uiState.dailyState.tasks,
                key = { it.instanceId }
            ) { instance ->
                TaskCard(
                    instance = instance,
                    onClick = { onTaskClick(instance) }
                )
            }
        }

        // Bottom Navigation
        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            containerColor = Color.White,
            tonalElevation = 8.dp
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Daily") },
                label = { Text("Daily") },
                selected = true,
                onClick = { /* Already on daily */ }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Challenges") },
                label = { Text("Challenges") },
                selected = false,
                onClick = onChallengesClick
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.BarChart, contentDescription = "Leaderboard") },
                label = { Text("Leaderboard") },
                selected = false,
                onClick = onStatsClick
            )
            NavigationBarItem(
                icon = {
                    Box {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Achievements")
                        if (uiState.currentUser.badges.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Red,
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                            ) { }
                        }
                    }
                },
                label = { Text("Achievements") },
                selected = false,
                onClick = onAchievementsClick  // ← ADD THIS
            )
        }
    }
}

@Composable
private fun DailyHeader(uiState: DailyUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(YellowPrimary, OrangePrimary))
            )
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Hey, ${uiState.currentUser.displayName}! 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Ready for today?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            StreakBadge(streak = uiState.currentUser.streak)
        }
    }
}

@Composable
private fun StreakBadge(streak: Int) {
    val scale by animateFloatAsState(
        targetValue = if (streak > 0) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "streak_scale"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.25f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = "🔥", fontSize = 28.sp)
        Text(
            text = "$streak",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )
        Text(text = "streak", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(0.8f))
    }
}

@Composable
private fun ProgressSection(state: DailyStateModel, currentUser: UserModel) {
    val animatedProgress by animateFloatAsState(
        targetValue = state.completionPercent,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "progress"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${state.completedCount}/${state.tasks.size} done", fontWeight = FontWeight.SemiBold)
                Text("⭐ ${currentUser.xp} XP", color = OrangePrimary, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = OrangePrimary,
                trackColor = OrangePrimary.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
fun TaskCard(
    instance: TaskInstance,
    onClick: () -> Unit
) {
    val task = instance.task
    val cardColor = taskTypeColor(task.type)
    val icon = taskTypeIcon(task.type)

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_press"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .scale(scale)
            .clickable {
                pressed = true
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type icon circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(cardColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = cardColor, modifier = Modifier.size(28.dp))
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (task.requiresCoop) TaskChip("CO-OP", CoopPurple)
                    if (instance.injectedByChallengeId != null) TaskChip("CHALLENGE", TealSecondary)
                }
                Text(task.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐ ${task.reward.xp} XP", color = OrangePrimary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text("·", color = Color.Gray)
                    Text("~${task.estimatedDurationSec}s", color = Color.Gray, style = MaterialTheme.typography.labelLarge)
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
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

// ── Visual helpers ─────────────────────────────────────────────────────────────
fun taskTypeColor(type: TaskType): Color = when (type) {
    TaskType.LOGIC -> LogicBlue
    TaskType.REAL_LIFE -> RealLifeGreen
    TaskType.CO_OP -> CoopPurple
    TaskType.CREATIVE -> Color(0xFFFF9F1C)
    TaskType.LEARNING -> Color(0xFF4361EE)
    TaskType.EMOTIONAL -> Color(0xFFEF476F)
    TaskType.SOCIAL -> TealSecondary
}

fun taskTypeIcon(type: TaskType): ImageVector = when (type) {
    TaskType.LOGIC -> Icons.Default.Psychology
    TaskType.REAL_LIFE -> Icons.Default.Home
    TaskType.CO_OP -> Icons.Default.People
    TaskType.CREATIVE -> Icons.Default.Brush
    TaskType.LEARNING -> Icons.Default.MenuBook
    TaskType.EMOTIONAL -> Icons.Default.Favorite
    TaskType.SOCIAL -> Icons.Default.EmojiPeople
}