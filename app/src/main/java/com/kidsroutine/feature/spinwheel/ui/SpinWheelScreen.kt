package com.kidsroutine.feature.spinwheel.ui

import android.graphics.Paint
import android.graphics.Typeface
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.toRadians
import kotlin.random.Random
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.SpinRewardType
import com.kidsroutine.core.model.UserModel
import kotlinx.coroutines.delay

// ── Segment colors ───────────────────────────────────────────────────────────
private val segmentColors = listOf(
    Color(0xFFFF6B6B), // Red
    Color(0xFFFFD93D), // Yellow
    Color(0xFF6BCB77), // Green
    Color(0xFF4D96FF), // Blue
    Color(0xFFFF922B), // Orange
    Color(0xFFA66CFF), // Purple
    Color(0xFFFF6EB4), // Pink
    Color(0xFF20C997), // Teal
    Color(0xFF748FFC)  // Indigo
)

private val BgDark = Color(0xFF1A1A2E)
private val BgCard = Color(0xFF16213E)
private val AccentGold = Color(0xFFFFD700)

@Composable
fun SpinWheelScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit,
    viewModel: SpinWheelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.userId) {
        viewModel.loadState(currentUser.userId)
    }

    // ── Spin animation state ─────────────────────────────────────────────
    val rotation = remember { Animatable(0f) }
    val rewards = SpinRewardType.entries
    val sweepAngle = 360f / rewards.size

    LaunchedEffect(uiState.phase) {
        if (uiState.phase == SpinPhase.SPINNING) {
            val result = uiState.lastResult ?: return@LaunchedEffect
            val targetIndex = rewards.indexOf(result.reward)

            // The pointer is at the top (12 o'clock / 270° in standard math coords).
            // Segment i spans from i*sweepAngle to (i+1)*sweepAngle.
            // We want the center of the target segment to end up under the pointer.
            val segmentCenter = targetIndex * sweepAngle + sweepAngle / 2f

            // Rotation is clockwise. To land the segment center at top (0° visual),
            // we need: totalRotation mod 360 == 360 - segmentCenter
            val fullRotations = 360f * (3 + Random.nextFloat() * 2f) // 3-5 full rotations
            val landing = 360f - segmentCenter
            val target = rotation.value + fullRotations + landing -
                    (rotation.value % 360f) // normalize current offset

            rotation.animateTo(
                targetValue = target,
                animationSpec = tween(
                    durationMillis = 3000,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    // ── Reveal pop animation ─────────────────────────────────────────────
    val revealScale = remember { Animatable(0f) }
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == SpinPhase.REVEALING || uiState.phase == SpinPhase.DONE) {
            revealScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            revealScale.snapTo(0f)
        }
    }

    // ── Double XP countdown timer ────────────────────────────────────────
    var doubleXpRemaining by remember { mutableLongStateOf(0L) }
    LaunchedEffect(uiState.dailyState.hasDoubleXpActive, uiState.dailyState.doubleXpExpiresAt) {
        if (uiState.dailyState.hasDoubleXpActive && uiState.dailyState.doubleXpExpiresAt > 0L) {
            while (true) {
                val remaining = uiState.dailyState.doubleXpExpiresAt - System.currentTimeMillis()
                doubleXpRemaining = if (remaining > 0) remaining else 0L
                if (remaining <= 0) break
                delay(1000L)
            }
        } else {
            doubleXpRemaining = 0L
        }
    }

    // ── UI ───────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Top bar ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "🎡 Daily Spin",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "🎰 Spins: ${uiState.spinsRemaining}/${uiState.dailyState.maxSpins}",
                color = AccentGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // XP balance indicator
        Text(
            text = "⭐ ${uiState.currentXp} XP",
            color = AccentGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentGold)
            }
            return@Column
        }

        Spacer(Modifier.height(8.dp))

        // ── Pointer triangle ─────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .size(32.dp)
                .offset(y = 6.dp)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path, Color.White)
            drawPath(path, AccentGold.copy(alpha = 0.5f))
        }

        // ── Wheel ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier.size(320.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(12.dp, CircleShape)
                    .rotate(rotation.value)
            ) {
                drawWheel(rewards, sweepAngle)
            }

            // Center hub
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(6.dp, CircleShape)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🎯", fontSize = 22.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Spin button ──────────────────────────────────────────────────
        val hasEnoughXp = uiState.currentXp >= SpinWheelViewModel.SPIN_COST
        val canSpin = uiState.phase == SpinPhase.IDLE && uiState.dailyState.canSpin && hasEnoughXp
        Button(
            onClick = { viewModel.spin() },
            enabled = canSpin,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canSpin) AccentGold else Color.Gray,
                contentColor = BgDark,
                disabledContainerColor = Color.Gray.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (uiState.phase) {
                        SpinPhase.IDLE -> if (uiState.dailyState.canSpin) "🎰 SPIN!" else "No spins left"
                        SpinPhase.SPINNING -> "Spinning…"
                        SpinPhase.REVEALING -> "🎉 Revealing…"
                        SpinPhase.DONE -> if (uiState.dailyState.canSpin) "🎰 SPIN AGAIN!" else "No spins left"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (uiState.dailyState.canSpin && uiState.phase != SpinPhase.SPINNING && uiState.phase != SpinPhase.REVEALING) {
                    Text(
                        text = "Cost: ${SpinWheelViewModel.SPIN_COST} XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // ── If DONE and can spin again ───────────────────────────────────
        if (uiState.phase == SpinPhase.DONE && uiState.dailyState.canSpin) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.resetForNextSpin() },
                modifier = Modifier.fillMaxWidth(0.5f),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White
                )
            ) {
                Text("Next spin →", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Result reveal ────────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.phase == SpinPhase.REVEALING || uiState.phase == SpinPhase.DONE,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn()
        ) {
            uiState.lastResult?.let { result ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(BgCard, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = result.reward.emoji,
                        fontSize = 64.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (result.reward == SpinRewardType.NOTHING)
                            "Better luck next time!" else "You won!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = result.reward.displayName,
                        color = AccentGold,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = rewardDescription(result.reward),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ── Double XP timer ──────────────────────────────────────────────
        if (uiState.dailyState.hasDoubleXpActive && doubleXpRemaining > 0L) {
            Spacer(Modifier.height(16.dp))
            val minutes = (doubleXpRemaining / 60_000).toInt()
            val seconds = ((doubleXpRemaining % 60_000) / 1000).toInt()
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6C63FF), Color(0xFFFF6584))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚡ 2x XP Active!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "%02d:%02d remaining".format(minutes, seconds),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // ── No spins left banner ─────────────────────────────────────────
        if (!uiState.dailyState.canSpin && uiState.phase == SpinPhase.DONE) {
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🌙", fontSize = 32.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "All spins used for today!",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Come back tomorrow for more rewards!",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            }
        }

        uiState.error?.let { error ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = "⚠️ $error",
                color = Color(0xFFFF6B6B),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Spacer(Modifier.height(140.dp))
    }
}

// ── Wheel drawing ────────────────────────────────────────────────────────────

private fun DrawScope.drawWheel(rewards: List<SpinRewardType>, sweepAngle: Float) {
    val diameter = size.minDimension
    val radius = diameter / 2f
    val topLeft = Offset(
        (size.width - diameter) / 2f,
        (size.height - diameter) / 2f
    )
    val arcSize = Size(diameter, diameter)

    // Outer ring
    drawCircle(
        color = Color.White.copy(alpha = 0.15f),
        radius = radius + 4f,
        center = center
    )

    rewards.forEachIndexed { index, reward ->
        val startAngle = index * sweepAngle - 90f // -90° so segment 0 starts at top

        // Segment arc
        drawArc(
            color = segmentColors[index % segmentColors.size],
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = topLeft,
            size = arcSize
        )

        // Segment divider line
        val lineAngleRad = startAngle.toDouble().toRadians()
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = center,
            end = Offset(
                center.x + (radius * cos(lineAngleRad)).toFloat(),
                center.y + (radius * sin(lineAngleRad)).toFloat()
            ),
            strokeWidth = 2f
        )

        // Text (emoji + label) drawn via native canvas for rotation
        val textAngleRad = (startAngle + sweepAngle / 2f).toDouble().toRadians()
        val textRadius = radius * 0.62f

        val emojiPaint = Paint().apply {
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.16f
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.085f
            color = Color.White.toArgb()
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val textX = center.x + (textRadius * cos(textAngleRad)).toFloat()
        val textY = center.y + (textRadius * sin(textAngleRad)).toFloat()
        val rotationDeg = startAngle + sweepAngle / 2f + 90f

        drawContext.canvas.nativeCanvas.apply {
            save()
            rotate(rotationDeg, textX, textY)
            drawText(reward.emoji, textX, textY - radius * 0.03f, emojiPaint)
            drawText(reward.displayName, textX, textY + radius * 0.1f, labelPaint)
            restore()
        }
    }

    // Inner ring highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.08f),
        radius = radius * 0.22f,
        center = center
    )
}

// ── Reward descriptions ──────────────────────────────────────────────────────

private fun rewardDescription(reward: SpinRewardType): String = when (reward) {
    SpinRewardType.DOUBLE_XP -> "All XP earned is doubled for 30 minutes!"
    SpinRewardType.BONUS_AVATAR_ITEM -> "A new avatar item has been added to your wardrobe!"
    SpinRewardType.PET_TREAT -> "Your pet will love this treat — feed them now!"
    SpinRewardType.STREAK_SHIELD -> "Your streak is protected if you miss a day!"
    SpinRewardType.XP_BOOST_SMALL -> "25 bonus XP added to your total!"
    SpinRewardType.XP_BOOST_BIG -> "100 bonus XP — that's a big boost!"
    SpinRewardType.LEAGUE_SHIELD -> "You're protected from league demotion this week!"
    SpinRewardType.ROO_EMOJI -> "A special Roo sticker for your collection!"
    SpinRewardType.NOTHING -> "No reward this time — spin again tomorrow!"
}
