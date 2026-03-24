package com.kidsroutine.feature.generation.ui

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.generation.data.WeekTheme
import com.kidsroutine.feature.generation.data.WeeklyDayPlan
import com.kidsroutine.feature.generation.data.WeeklyTask

// ── Palette ─────────────────────────────────────────────────────────────────
private val BgWarm    = Color(0xFFFFFBF0)
private val CardWht   = Color(0xFFFFFFFF)
private val TextDark  = Color(0xFF2D3436)
private val TextGray  = Color(0xFF636E72)
private val GoldCol   = Color(0xFFFFD700)
private val ProPurple = Color(0xFF9B59B6)

@Composable
fun WeeklyPlanScreen(
    currentUser: UserModel,
    familyChildren: List<UserModel> = emptyList(),
    onBackClick: () -> Unit = {},
    viewModel: WeeklyPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWarm)
    ) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────
            item { WeeklyHeader(onBackClick = onBackClick) }

            // ── PRO gate ─────────────────────────────────────────────────
            if (!uiState.isPro && !uiState.isLoading && uiState.weeklyPlan == null) {
                item { ProGateBanner() }
            }

            // ── Theme + goal picker (shown before result) ─────────────────
            if (!uiState.showResult) {
                item {
                    WeekThemePicker(
                        selectedTheme = uiState.selectedTheme,
                        onThemeSelect = { viewModel.selectTheme(it) }
                    )
                }

                item {
                    WeeklyGoalPicker(
                        selectedGoals = uiState.selectedGoals,
                        onToggleGoal  = { viewModel.toggleGoal(it) }
                    )
                }

                item {
                    WeeklyGenerateButton(
                        isLoading      = uiState.isLoading,
                        quotaRemaining = uiState.quotaRemaining,
                        onClick        = {
                            viewModel.generateWeeklyPlan(currentUser, familyChildren)
                        }
                    )
                }
            }

            // ── Error ─────────────────────────────────────────────────────
            if (uiState.error != null) {
                item {
                    WeeklyErrorBanner(
                        message   = uiState.error ?: "",
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }

            // ── Result ────────────────────────────────────────────────────
            if (uiState.showResult && uiState.weeklyPlan != null) {
                val plan = uiState.weeklyPlan!!

                item {
                    WeeklyResultHeader(
                        theme     = plan.weekTheme,
                        totalXp   = plan.totalFamilyXp,
                        dayCount  = plan.days.size,
                        isCached  = uiState.isCached,
                        onReset   = { viewModel.reset() }
                    )
                }

                itemsIndexed(plan.days) { index, day ->
                    WeeklyDayCard(day = day, index = index)
                }
            }
        }

        // Loading overlay
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter   = fadeIn(),
            exit    = fadeOut()
        ) {
            WeeklyLoadingOverlay()
        }
    }
}

// ── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun WeeklyHeader(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF11998E), Color(0xFF38EF7D))
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier            = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                tint     = Color.White,
                modifier = Modifier.size(36.dp)
            )
            Text(
                "Weekly Family Plan",
                color      = Color.White,
                fontSize   = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "7 days planned by AI for your family",
                color    = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
            Surface(
                shape  = RoundedCornerShape(50.dp),
                color  = GoldCol.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, GoldCol.copy(alpha = 0.5f))
            ) {
                Text(
                    "⭐ PRO Feature",
                    color      = GoldCol,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        IconButton(
            onClick  = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Text("←", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ── PRO gate ─────────────────────────────────────────────────────────────────

@Composable
private fun ProGateBanner() {
    Surface(
        shape   = RoundedCornerShape(16.dp),
        color   = ProPurple.copy(alpha = 0.08f),
        border  = BorderStroke(1.dp, ProPurple.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier              = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("👑", fontSize = 32.sp)
            Column {
                Text(
                    "PRO Feature",
                    fontWeight = FontWeight.Bold,
                    color      = ProPurple
                )
                Text(
                    "Upgrade to PRO to generate a full 7-day family plan.",
                    fontSize = 12.sp,
                    color    = TextGray
                )
            }
        }
    }
}

// ── Theme picker ──────────────────────────────────────────────────────────────

@Composable
private fun WeekThemePicker(
    selectedTheme: WeekTheme,
    onThemeSelect: (WeekTheme) -> Unit
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Choose your week's theme",
            fontWeight = FontWeight.Bold,
            fontSize   = 16.sp,
            color      = TextDark
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(WeekTheme.entries) { theme ->
                val isSelected = selectedTheme == theme
                val themeColor = when (theme) {
                    WeekTheme.ADVENTURE  -> Color(0xFF11998E)
                    WeekTheme.DISCIPLINE -> Color(0xFF667EEA)
                    WeekTheme.CREATIVITY -> Color(0xFFFF6B35)
                    WeekTheme.WELLNESS   -> Color(0xFF4CAF50)
                }
                val scale by animateFloatAsState(
                    targetValue   = if (isSelected) 1.05f else 1f,
                    animationSpec = spring(Spring.DampingRatioMediumBouncy),
                    label         = "themeScale"
                )
                Surface(
                    shape   = RoundedCornerShape(16.dp),
                    color   = if (isSelected) themeColor.copy(0.15f) else CardWht,
                    border  = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) themeColor else Color(0xFFEEEEEE)
                    ),
                    modifier = Modifier
                        .width(100.dp)
                        .scale(scale)
                        .clickable { onThemeSelect(theme) }
                ) {
                    Column(
                        modifier            = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(theme.emoji, fontSize = 28.sp)
                        Text(
                            theme.label,
                            fontSize   = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color      = if (isSelected) themeColor else TextGray,
                            textAlign  = TextAlign.Center
                        )
                        Text(
                            theme.description,
                            fontSize  = 9.sp,
                            color     = TextGray,
                            textAlign = TextAlign.Center,
                            maxLines  = 2
                        )
                    }
                }
            }
        }
    }
}

// ── Goal picker ───────────────────────────────────────────────────────────────

@Composable
private fun WeeklyGoalPicker(
    selectedGoals: Set<String>,
    onToggleGoal: (String) -> Unit
) {
    val goals = listOf("HEALTH", "SLEEP", "LEARNING", "CREATIVITY", "SOCIAL", "SCREEN_TIME")
    val goalEmojis = mapOf(
        "HEALTH"      to "🏃",
        "SLEEP"       to "💤",
        "LEARNING"    to "📚",
        "CREATIVITY"  to "🎨",
        "SOCIAL"      to "👥",
        "SCREEN_TIME" to "📱"
    )

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Family goals this week (optional)",
            fontSize = 13.sp,
            color    = TextGray
        )
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            goals.take(3).forEach { goal ->
                val isSelected = goal in selectedGoals
                Surface(
                    shape   = RoundedCornerShape(50.dp),
                    color   = if (isSelected) Color(0xFF667EEA).copy(0.15f) else CardWht,
                    border  = BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFF667EEA) else Color(0xFFEEEEEE)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleGoal(goal) }
                ) {
                    Text(
                        "${goalEmojis[goal]} $goal",
                        fontSize  = 10.sp,
                        color     = if (isSelected) Color(0xFF667EEA) else TextGray,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(6.dp, 4.dp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            goals.drop(3).forEach { goal ->
                val isSelected = goal in selectedGoals
                Surface(
                    shape   = RoundedCornerShape(50.dp),
                    color   = if (isSelected) Color(0xFF667EEA).copy(0.15f) else CardWht,
                    border  = BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFF667EEA) else Color(0xFFEEEEEE)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleGoal(goal) }
                ) {
                    Text(
                        "${goalEmojis[goal]} $goal",
                        fontSize  = 10.sp,
                        color     = if (isSelected) Color(0xFF667EEA) else TextGray,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(6.dp, 4.dp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ── Generate button ───────────────────────────────────────────────────────────

@Composable
private fun WeeklyGenerateButton(
    isLoading: Boolean,
    quotaRemaining: Int,
    onClick: () -> Unit
) {
    Button(
        onClick  = onClick,
        enabled  = !isLoading && quotaRemaining > 0,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp),
        shape  = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = Color(0xFF11998E),
            disabledContainerColor = Color(0xFFCCCCCC)
        )
    ) {
        if (isLoading) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    color       = Color.White,
                    strokeWidth = 2.dp
                )
                Text("Planning your week…", fontWeight = FontWeight.Bold, color = Color.White)
            }
        } else if (quotaRemaining <= 0) {
            Text("🔒 Monthly quota reached — Upgrade", fontWeight = FontWeight.Bold, color = Color.White)
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(
                    "Plan Our Week ($quotaRemaining left)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 16.sp,
                    color      = Color.White
                )
            }
        }
    }
}

// ── Result header ─────────────────────────────────────────────────────────────

@Composable
private fun WeeklyResultHeader(
    theme: String,
    totalXp: Int,
    dayCount: Int,
    isCached: Boolean,
    onReset: () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWht),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF11998E).copy(alpha = 0.1f), Color.Transparent)
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("📅", fontSize = 40.sp)
            Text(
                "$theme Week",
                fontSize   = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = TextDark
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WeekStatChip("📋", "$dayCount days")
                WeekStatChip("⭐", "$totalXp family XP")
                if (isCached) WeekStatChip("⚡", "Cached")
            }
            TextButton(onClick = onReset) {
                Text("🔄 Regenerate", color = Color(0xFF11998E), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun WeekStatChip(emoji: String, label: String) {
    Surface(
        shape  = RoundedCornerShape(50.dp),
        color  = Color(0xFFF0F0F0),
        border = BorderStroke(1.dp, Color(0xFFDDDDDD))
    ) {
        Text(
            "$emoji $label",
            color    = TextGray,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

// ── Day card ─────────────────────────────────────────────────────────────────

@Composable
private fun WeeklyDayCard(day: WeeklyDayPlan, index: Int) {
    var expanded by remember { mutableStateOf(index == 0) } // first day open by default
    var visible  by remember { mutableStateOf(false) }

    val cardAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = index * 80),
        label         = "dayAlpha_$index"
    )
    val cardY by animateFloatAsState(
        targetValue   = if (visible) 0f else 20f,
        animationSpec = tween(350, delayMillis = index * 80, easing = EaseOutCubic),
        label         = "dayY_$index"
    )
    LaunchedEffect(Unit) { visible = true }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .offset(y = cardY.dp)
            .alpha(cardAlpha)
            .clickable { expanded = !expanded },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWht),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Day header row
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(day.dayEmoji, fontSize = 24.sp)
                    Column {
                        Text(
                            day.dayName,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 15.sp,
                            color      = TextDark
                        )
                        Text(
                            "${day.tasks.size} tasks · ${day.tasks.sumOf { it.xpReward }} XP",
                            fontSize = 12.sp,
                            color    = TextGray
                        )
                    }
                }
                Text(
                    if (expanded) "▲" else "▼",
                    color    = TextGray,
                    fontSize = 12.sp
                )
            }

            // Expandable tasks
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    Spacer(Modifier.height(4.dp))
                    day.tasks.forEach { task ->
                        WeeklyTaskRow(task = task)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyTaskRow(task: WeeklyTask) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.Top
    ) {
        // Child name badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF667EEA).copy(alpha = 0.1f)
        ) {
            Text(
                task.childName.take(8),
                fontSize   = 10.sp,
                color      = Color(0xFF667EEA),
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.title,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextDark
            )
            Text(
                task.description,
                fontSize = 11.sp,
                color    = TextGray
            )
            Row(
                modifier              = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "⭐ ${task.xpReward} XP",
                    fontSize = 10.sp,
                    color    = Color(0xFFFF9800)
                )
                Text(
                    "⏱️ ${task.estimatedDurationSec}s",
                    fontSize = 10.sp,
                    color    = TextGray
                )
                if (task.requiresCoop) {
                    Text("👥 Co-op", fontSize = 10.sp, color = Color(0xFF9B59B6))
                }
            }
        }

        // Difficulty dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    when (task.difficulty) {
                        "HARD"   -> Color(0xFFC62828)
                        "MEDIUM" -> Color(0xFFFF9800)
                        else     -> Color(0xFF4CAF50)
                    },
                    CircleShape
                )
                .align(Alignment.Top)
                .padding(top = 4.dp)
        )
    }
}

// ── Loading overlay ───────────────────────────────────────────────────────────

@Composable
private fun WeeklyLoadingOverlay() {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color(0x99FFFFFF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color       = Color(0xFF11998E),
                strokeWidth = 4.dp,
                modifier    = Modifier.size(56.dp)
            )
            Text(
                "✨ Planning your family's week…",
                fontWeight = FontWeight.SemiBold,
                color      = TextDark
            )
            Text(
                "Gemini is crafting 7 days of activities",
                fontSize = 13.sp,
                color    = TextGray
            )
        }
    }
}

// ── Error banner ──────────────────────────────────────────────────────────────

@Composable
private fun WeeklyErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = Color(0xFFFFEBEE),
        border = BorderStroke(1.dp, Color(0xFFC62828).copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier              = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                "⚠️ $message",
                color    = Color(0xFFC62828),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("✕", color = Color(0xFFC62828))
            }
        }
    }
}