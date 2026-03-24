package com.kidsroutine.feature.generation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.kidsroutine.feature.generation.data.DayMood
import com.kidsroutine.feature.generation.data.GeneratedDailyPlan
import com.kidsroutine.feature.generation.data.GeneratedPlanTask

// ── Palette ──────────────────────────────────────────────────────────────────
private val BgDark      = Color(0xFF080818)
private val CardBg      = Color(0xFF12122A)
private val CardBorder  = Color(0xFF2A2A50)
private val AccentGold  = Color(0xFFFFD700)
private val MorningCol  = Color(0xFFFF9F43)
private val AfternoonCol= Color(0xFF667EEA)
private val EveningCol  = Color(0xFF9B59B6)
private val TextWht     = Color(0xFFFFFFFF)
private val TextGray    = Color(0xFF8888AA)

// ── Entry point ──────────────────────────────────────────────────────────────

@Composable
fun DailyPlanScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit = {},
    viewModel: DailyPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Animated star-field background (subtle, always running)
        StarField()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────
            item {
                PlanHeader(onBackClick = onBackClick)
            }

            // ── Mood picker ───────────────────────────────────────────────
            if (!uiState.showResult) {
                item {
                    MoodPickerSection(
                        selectedMood = uiState.selectedMood,
                        onMoodSelected = { viewModel.selectMood(it) }
                    )
                }

                item {
                    GeneratePlanButton(
                        isLoading      = uiState.isLoading,
                        quotaRemaining = uiState.quotaRemaining,
                        onClick        = { viewModel.generatePlan(currentUser) }
                    )
                }
            }

            // ── Error ─────────────────────────────────────────────────────
            if (uiState.error != null) {
                item {
                    PlanErrorBanner(
                        message   = uiState.error ?: "",
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }

            // ── Result: theme banner ──────────────────────────────────────
            if (uiState.showResult && uiState.plan != null) {
                item {
                    PlanThemeBanner(
                        plan      = uiState.plan!!,
                        onReset   = { viewModel.reset() }
                    )
                }

                // ── Timeline of tasks ─────────────────────────────────────
                itemsIndexed(uiState.plan!!.tasks) { index, task ->
                    PlanTaskCard(task = task, index = index)
                }

                // ── Total XP footer ───────────────────────────────────────
                item {
                    PlanXpFooter(
                        totalXp    = uiState.plan!!.totalXp,
                        taskCount  = uiState.plan!!.tasks.size,
                        isCached   = uiState.isCached
                    )
                }
            }
        }

        // Loading overlay — cinematic spinner
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter   = fadeIn(),
            exit    = fadeOut()
        ) {
            PlanLoadingOverlay()
        }
    }
}

// ── Star field background ────────────────────────────────────────────────────

@Composable
private fun StarField() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Static dots only — Canvas doesn't support per-star animation
        // Stars are painted once; subtle parallax handled by scroll
        repeat(60) { i ->
            val x = ((i * 137.5f) % size.width)
            val y = ((i * 89.3f) % size.height)
            val r = (1f + (i % 3) * 0.8f)
            drawCircle(
                color  = Color.White.copy(alpha = 0.15f + (i % 5) * 0.06f),
                radius = r,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

// ── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun PlanHeader(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A0A3A), Color(0xFF080818))
                )
            )
            .statusBarsPadding()
    ) {
        // Pulsing glow behind icon
        val infiniteTransition = rememberInfiniteTransition(label = "glow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue  = 0.2f,
            targetValue   = 0.55f,
            animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
            label = "glowAlpha"
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.Center)
                .offset(y = (-10).dp)
                .alpha(glowAlpha)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF9B59B6).copy(0.7f), Color.Transparent)),
                    CircleShape
                )
        )

        Column(
            modifier  = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint     = AccentGold,
                modifier = Modifier.size(36.dp)
            )
            Text(
                "AI Day Planner",
                color      = TextWht,
                fontSize   = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Text(
                "Your perfect day, generated",
                color    = TextGray,
                fontSize = 13.sp
            )
            // PRO badge
            Surface(
                shape  = RoundedCornerShape(50.dp),
                color  = AccentGold.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f))
            ) {
                Text(
                    "⭐ PRO Feature",
                    color      = AccentGold,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // Back button
        IconButton(
            onClick  = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Text("←", color = TextWht, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Mood picker ───────────────────────────────────────────────────────────────

@Composable
private fun MoodPickerSection(
    selectedMood: DayMood,
    onMoodSelected: (DayMood) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "How are you feeling today?",
            color      = TextWht,
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Your plan will match your energy",
            color    = TextGray,
            fontSize = 13.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DayMood.entries.forEach { mood ->
                MoodChip(
                    mood       = mood,
                    isSelected = selectedMood == mood,
                    onClick    = { onMoodSelected(mood) },
                    modifier   = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MoodChip(
    mood: DayMood,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue   = if (isSelected) 1.05f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label         = "moodScale"
    )
    val borderColor = when (mood) {
        DayMood.ENERGETIC -> Color(0xFFFF6B35)
        DayMood.CALM      -> Color(0xFF4ECDC4)
        DayMood.NORMAL    -> Color(0xFF667EEA)
    }

    Surface(
        shape   = RoundedCornerShape(16.dp),
        color   = if (isSelected) borderColor.copy(alpha = 0.2f) else CardBg,
        border  = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) borderColor else CardBorder
        ),
        modifier = modifier
            .scale(scale)
            .clickable { onClick() }
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(mood.emoji, fontSize = 28.sp)
            Text(
                mood.label,
                color      = if (isSelected) TextWht else TextGray,
                fontSize   = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign  = TextAlign.Center
            )
        }
    }
}

// ── Generate button ───────────────────────────────────────────────────────────

@Composable
private fun GeneratePlanButton(
    isLoading: Boolean,
    quotaRemaining: Int,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue   = if (isLoading) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label         = "btnScale"
    )

    // Shimmer on the button
    val shimmerInfinite = rememberInfiniteTransition(label = "btnShimmer")
    val shimmerX by shimmerInfinite.animateFloat(
        initialValue  = -400f,
        targetValue   = 800f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label         = "shimX"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Button(
            onClick  = onClick,
            enabled  = !isLoading && quotaRemaining > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .scale(scale),
            shape  = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor         = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF667EEA), Color(0xFF9B59B6), Color(0xFF667EEA)),
                            start = androidx.compose.ui.geometry.Offset(shimmerX, 0f),
                            end   = androidx.compose.ui.geometry.Offset(shimmerX + 400f, 60f)
                        ),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = Color.White,
                            strokeWidth = 2.dp
                        )
                        Text("Building your day…", color = TextWht, fontWeight = FontWeight.Bold)
                    }
                } else if (quotaRemaining <= 0) {
                    Text("🔒 Plan quota reached — Upgrade", color = TextWht, fontWeight = FontWeight.Bold)
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, tint = AccentGold, modifier = Modifier.size(20.dp))
                        Text(
                            "Generate My Day",
                            color      = TextWht,
                            fontSize   = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

// ── Loading overlay ───────────────────────────────────────────────────────────

@Composable
private fun PlanLoadingOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    // Orbiting dots
    val angle by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
        label         = "orbit"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 0.9f,
        targetValue   = 1.1f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "pulse"
    )

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color(0xCC080818)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier         = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring of dots
                repeat(8) { i ->
                    val dotAngle = angle + (i * 45f)
                    val rad      = dotAngle * (Math.PI / 180.0)
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .offset(
                                x = (40f * kotlin.math.cos(rad)).toFloat().dp,
                                y = (40f * kotlin.math.sin(rad)).toFloat().dp
                            )
                            .background(
                                Color(0xFF667EEA).copy(alpha = 0.4f + (i * 0.07f).coerceAtMost(0.6f)),
                                CircleShape
                            )
                    )
                }
                // Central pulse
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .scale(pulseScale)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFF9B59B6), Color(0xFF667EEA))),
                            CircleShape
                        )
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "✨ Gemini is planning your day…",
                    color      = TextWht,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Analysing your age, mood & preferences",
                    color    = TextGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// ── Theme banner (first thing shown after generation) ────────────────────────

@Composable
private fun PlanThemeBanner(plan: GeneratedDailyPlan, onReset: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val bannerScale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.85f,
        animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow),
        label         = "bannerScale"
    )
    val bannerAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label         = "bannerAlpha"
    )
    LaunchedEffect(Unit) { visible = true }

    val moodColor = when (plan.mood) {
        "ENERGETIC" -> Color(0xFFFF6B35)
        "CALM"      -> Color(0xFF4ECDC4)
        else        -> Color(0xFF667EEA)
    }
    val moodEmoji = when (plan.mood) {
        "ENERGETIC" -> "⚡"
        "CALM"      -> "🌿"
        else        -> "😊"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .scale(bannerScale)
            .alpha(bannerAlpha)
    ) {
        Surface(
            shape           = RoundedCornerShape(24.dp),
            color           = CardBg,
            border          = BorderStroke(2.dp, moodColor.copy(alpha = 0.6f)),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(moodColor.copy(alpha = 0.15f), Color.Transparent)
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(moodEmoji, fontSize = 36.sp)

                Text(
                    plan.theme,
                    color      = TextWht,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign  = TextAlign.Center
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PlanStatPill("📋", "${plan.tasks.size} tasks")
                    PlanStatPill("⭐", "${plan.totalXp} XP total")
                    PlanStatPill(moodEmoji, plan.mood.lowercase().replaceFirstChar { it.uppercase() })
                }

                TextButton(onClick = onReset) {
                    Text(
                        "🔄 Generate New Plan",
                        color    = moodColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanStatPill(emoji: String, label: String) {
    Surface(
        shape  = RoundedCornerShape(50.dp),
        color  = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Text(
            "$emoji $label",
            color    = TextGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

// ── Plan task card ────────────────────────────────────────────────────────────

@Composable
private fun PlanTaskCard(task: GeneratedPlanTask, index: Int) {
    var visible by remember { mutableStateOf(false) }
    val cardY by animateFloatAsState(
        targetValue   = if (visible) 0f else 50f,
        animationSpec = tween(450, delayMillis = (index * 100).coerceAtMost(400), easing = EaseOutCubic),
        label         = "cardY_$index"
    )
    val cardAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = (index * 100).coerceAtMost(400)),
        label         = "cardAlpha_$index"
    )
    LaunchedEffect(Unit) { visible = true }

    val slotColor = when (task.timeSlot) {
        "MORNING"   -> MorningCol
        "AFTERNOON" -> AfternoonCol
        else        -> EveningCol
    }
    val slotEmoji = when (task.timeSlot) {
        "MORNING"   -> "🌅"
        "AFTERNOON" -> "☀️"
        else        -> "🌙"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .offset(y = cardY.dp)
            .alpha(cardAlpha)
    ) {
        // Timeline connector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.width(48.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(36.dp)
                    .background(slotColor.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, slotColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(slotEmoji, fontSize = 16.sp)
            }
            if (index < 4) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(slotColor.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Card body
        Surface(
            shape           = RoundedCornerShape(16.dp),
            color           = CardBg,
            border          = BorderStroke(1.dp, CardBorder),
            shadowElevation = 4.dp,
            modifier        = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title row
                Row(
                    modifier                = Modifier.fillMaxWidth(),
                    horizontalArrangement   = Arrangement.SpaceBetween,
                    verticalAlignment       = Alignment.Top
                ) {
                    Text(
                        task.title,
                        color      = TextWht,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.weight(1f)
                    )
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
                    )
                }

                Text(
                    task.description,
                    color    = TextGray,
                    fontSize = 12.sp
                )

                // Badges row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskBadge("⏱️ ${task.estimatedDurationSec}s",  slotColor)
                    TaskBadge("⭐ ${task.xpReward} XP",           AccentGold)
                    if (task.requiresCoop) TaskBadge("👥 Co-op", Color(0xFF9B59B6))
                }
            }
        }
    }
}

@Composable
private fun TaskBadge(label: String, color: Color) {
    Surface(
        shape  = RoundedCornerShape(50.dp),
        color  = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            label,
            color    = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ── XP footer ─────────────────────────────────────────────────────────────────

@Composable
private fun PlanXpFooter(totalXp: Int, taskCount: Int, isCached: Boolean) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.8f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label         = "xpFooterScale"
    )
    LaunchedEffect(Unit) { visible = true }

    Surface(
        shape   = RoundedCornerShape(20.dp),
        color   = AccentGold.copy(alpha = 0.1f),
        border  = BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .scale(scale)
    ) {
        Row(
            modifier              = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Complete all $taskCount tasks",
                    color    = TextGray,
                    fontSize = 13.sp
                )
                Text(
                    "Earn up to $totalXp XP today!",
                    color      = AccentGold,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text("🏆", fontSize = 40.sp)
        }
    }
}

// ── Error banner ──────────────────────────────────────────────────────────────

@Composable
private fun PlanErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = Color(0xFF3A1010),
        border = BorderStroke(1.dp, Color(0xFFC62828).copy(alpha = 0.5f)),
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
                color    = Color(0xFFEF9A9A),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("✕", color = Color(0xFFEF9A9A))
            }
        }
    }
}