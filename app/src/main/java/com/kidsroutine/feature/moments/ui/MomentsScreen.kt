package com.kidsroutine.feature.moments.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.MomentModel
import com.kidsroutine.core.model.UserModel
import java.text.SimpleDateFormat
import java.util.*

// ── Palette ──────────────────────────────────────────────────────────────────
private val BgDark     = Color(0xFF0A0A1A)
private val CardBg     = Color(0xFF14142A)
private val CardBorder = Color(0xFF2A2A4A)
private val AccentGold = Color(0xFFFFD700)
private val AccentTeal = Color(0xFF4ECDC4)
private val TextWht    = Color(0xFFFFFFFF)
private val TextGray   = Color(0xFF9090B0)

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun MomentsScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit = {},
    viewModel: MomentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(currentUser.familyId) {
        viewModel.loadMoments(currentUser.familyId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // ── Subtle animated gradient background ───────────────────────────
        val infiniteTransition = rememberInfiniteTransition(label = "bgGrad")
        val gradShift by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue  = 1f,
            animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
            label = "gradShift"
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sweep = 360f * gradShift
            drawRect(
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFF0A0A1A),
                        Color(0xFF0D102A),
                        Color(0xFF0A0A1A)
                    ),
                    center = androidx.compose.ui.geometry.Offset(
                        size.width * 0.3f,
                        size.height * 0.2f
                    )
                )
            )
        }

        LazyColumn(
            state  = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────
            item {
                MomentsHeader(onBackClick = onBackClick)
            }

            // ── Loading ───────────────────────────────────────────────────
            if (uiState.isLoading) {
                item { MomentsSkeleton() }
            }

            // ── Empty state ───────────────────────────────────────────────
            if (!uiState.isLoading && uiState.moments.isEmpty()) {
                item { MomentsEmpty() }
            }

            // ── Moment cards ──────────────────────────────────────────────
            itemsIndexed(
                items = uiState.moments,
                key   = { _, m -> m.momentId }
            ) { index, moment ->
                MomentCard(
                    moment     = moment,
                    index      = index,
                    currentUserId = currentUser.userId,
                    onReact    = { emoji ->
                        viewModel.addReaction(moment.momentId, currentUser.userId, emoji)
                    }
                )
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun MomentsHeader(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A0A3A), Color(0xFF0A0A1A))
                )
            )
            .statusBarsPadding()
    ) {
        // Star field behind header
        val infiniteTransition = rememberInfiniteTransition(label = "headerStars")
        repeat(20) { i ->
            val twinkle by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue  = 1f,
                animationSpec = infiniteRepeatable(
                    tween((800 + i * 77), easing = EaseInOutSine),
                    RepeatMode.Reverse
                ),
                label = "twinkle_$i"
            )
            val xFrac = (i * 5.3f) % 100f
            val yFrac = (i * 7.1f) % 100f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart)
                    .offset(x = (xFrac / 100f * 400).dp, y = (yFrac / 100f * 220).dp)
                    .size((3 + i % 5).dp)
                    .alpha(twinkle)
                    .background(Color.White, CircleShape)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("📸", fontSize = 40.sp)
            Text(
                text  = "Family Moments",
                color = TextWht,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text  = "Your adventure memories",
                color = TextGray,
                fontSize = 14.sp
            )
        }

        // Back button
        IconButton(
            onClick   = onBackClick,
            modifier  = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Text("←", color = TextWht, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Moment card ───────────────────────────────────────────────────────────────

@Composable
private fun MomentCard(
    moment: MomentModel,
    index: Int,
    currentUserId: String,
    onReact: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500, delayMillis = (index * 80).coerceAtMost(600)),
        label = "cardAlpha"
    )
    val cardY by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = tween(500, easing = EaseOutCubic, delayMillis = (index * 80).coerceAtMost(600)),
        label = "cardY"
    )
    LaunchedEffect(Unit) { visible = true }

    // Double-tap for heart reaction
    var showHeartBurst by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .offset(y = cardY.dp)
            .alpha(cardAlpha)
    ) {
        Surface(
            shape  = RoundedCornerShape(20.dp),
            color  = CardBg,
            border = BorderStroke(1.dp, CardBorder),
            shadowElevation = 8.dp,
            tonalElevation  = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                showHeartBurst = true
                                onReact("❤️")
                            },
                            onLongPress = { showReactionPicker = true }
                        )
                    }
            ) {
                // ── Photo / emoji hero area ──────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (moment.photoUrl.isEmpty()) 140.dp else 240.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    momentGradientStart(index),
                                    momentGradientEnd(index)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (moment.photoUrl.isEmpty()) {
                        // Emoji hero with subtle float
                        val floatInfinite = rememberInfiniteTransition(label = "emojiFloat_$index")
                        val floatY by floatInfinite.animateFloat(
                            initialValue = -6f,
                            targetValue  = 6f,
                            animationSpec = infiniteRepeatable(
                                tween(2000, easing = EaseInOutSine),
                                RepeatMode.Reverse
                            ),
                            label = "float"
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.offset(y = floatY.dp)
                        ) {
                            Text(moment.emoji, fontSize = 56.sp)
                            if (moment.taskTitle.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = moment.taskTitle,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // XP badge top-right
                    if (moment.xpAtMoment > 0) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color(0xFF000000).copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, null, tint = AccentGold, modifier = Modifier.size(12.dp))
                                Text(
                                    "${moment.xpAtMoment} XP",
                                    color = AccentGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Heart burst on double-tap
                    AnimatedVisibility(
                        visible = showHeartBurst,
                        enter   = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
                        exit    = scaleOut() + fadeOut(tween(400))
                    ) {
                        Text("❤️", fontSize = 60.sp)
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(800)
                            showHeartBurst = false
                        }
                    }
                }

                // ── Card body ────────────────────────────────────────────
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Title + date row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = moment.title,
                                color = TextWht,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (moment.description.isNotEmpty()) {
                                Text(
                                    text  = moment.description,
                                    color = TextGray,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Text(
                            text  = formatMomentDate(moment.createdAt),
                            color = TextGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    // Reactions row
                    if (moment.reactions.isNotEmpty() || true) {
                        ReactionRow(
                            reactions = moment.reactions,
                            onReact   = { showReactionPicker = true }
                        )
                    }
                }
            }
        }

        // Reaction picker popup
        if (showReactionPicker) {
            ReactionPicker(
                onReact = { emoji ->
                    onReact(emoji)
                    showReactionPicker = false
                },
                onDismiss = { showReactionPicker = false }
            )
        }
    }
}

// ── Reaction row ──────────────────────────────────────────────────────────────

@Composable
private fun ReactionRow(
    reactions: Map<String, String>,
    onReact: () -> Unit
) {
    val grouped = reactions.values.groupingBy { it }.eachCount()

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        grouped.entries.take(5).forEach { (emoji, count) ->
            Surface(
                shape  = RoundedCornerShape(50.dp),
                color  = Color.White.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(emoji, fontSize = 14.sp)
                    if (count > 1) {
                        Text(
                            "$count",
                            color = TextGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Add reaction button
        Surface(
            shape  = CircleShape,
            color  = Color.White.copy(alpha = 0.06f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier
                .size(30.dp)
                .clickable { onReact() }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("+", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Reaction picker ───────────────────────────────────────────────────────────

@Composable
private fun ReactionPicker(
    onReact: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val emojis = listOf("❤️", "🔥", "⭐", "🎉", "😮", "👏", "💪", "🥇")

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter   = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()
        ) {
            Surface(
                shape  = RoundedCornerShape(50.dp),
                color  = Color(0xFF1E1E3A),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shadowElevation = 24.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    emojis.forEachIndexed { i, emoji ->
                        var pressed by remember { mutableStateOf(false) }
                        val emojiScale by animateFloatAsState(
                            targetValue = if (pressed) 0.8f else 1f,
                            animationSpec = spring(Spring.DampingRatioMediumBouncy),
                            label = "emojiBtn_$i"
                        )
                        Text(
                            text = emoji,
                            fontSize = 26.sp,
                            modifier = Modifier
                                .scale(emojiScale)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            pressed = true
                                            tryAwaitRelease()
                                            pressed = false
                                        },
                                        onTap = { onReact(emoji) }
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}

// ── Empty / loading states ────────────────────────────────────────────────────

@Composable
private fun MomentsEmpty() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("📸", fontSize = 56.sp)
        Text("No moments yet!", color = TextWht, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "Complete tasks to create family memories here!",
            color = TextGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MomentsSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue  = 0.35f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "skeletonAlpha"
    )
    repeat(3) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(200.dp)
                .alpha(alpha)
                .background(CardBg, RoundedCornerShape(20.dp))
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun momentGradientStart(index: Int): Color = when (index % 6) {
    0 -> Color(0xFF1A0A3A)
    1 -> Color(0xFF0A2A1A)
    2 -> Color(0xFF0A1A3A)
    3 -> Color(0xFF2A1A0A)
    4 -> Color(0xFF1A2A0A)
    else -> Color(0xFF2A0A1A)
}

private fun momentGradientEnd(index: Int): Color = when (index % 6) {
    0 -> Color(0xFF3A1060)
    1 -> Color(0xFF106030)
    2 -> Color(0xFF103060)
    3 -> Color(0xFF603010)
    4 -> Color(0xFF306010)
    else -> Color(0xFF601030)
}

private fun formatMomentDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return sdf.format(Date(timestamp))
}