package com.kidsroutine.feature.lootbox.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.LootBoxRarity
import com.kidsroutine.core.model.LootBoxReward
import com.kidsroutine.core.model.LootBox
import kotlin.math.*

// ── Palette ─────────────────────────────────────────────────────────────────
private val OverlayBg     = Color(0xCC000020)
private val CommonGold    = Color(0xFFFFD700)
private val RareBlue      = Color(0xFF4FC3F7)
private val EpicPurple    = Color(0xFFCE93D8)
private val LegendaryFire = Color(0xFFFF6B35)
private val CardDark      = Color(0xFF1A1040)

// ── Entry point ──────────────────────────────────────────────────────────────

/**
 * Drop this composable at the top level (alongside CelebrationOverlay).
 * Call viewModel.presentBox(box) from anywhere to trigger the sequence.
 */
@Composable
fun LootBoxOverlay(
    viewModel: LootBoxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val visible = uiState.phase != LootBoxPhase.IDLE && uiState.phase != LootBoxPhase.DONE

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)),
        exit  = fadeOut(tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OverlayBg),
            contentAlignment = Alignment.Center
        ) {
            when (uiState.phase) {
                LootBoxPhase.WAITING, LootBoxPhase.SHAKING ->
                    LootBoxWaiting(
                        reward  = uiState.reward,
                        shaking = uiState.phase == LootBoxPhase.SHAKING,
                        onTap   = { viewModel.onBoxTapped() }
                    )

                LootBoxPhase.BURSTING ->
                    LootBoxBurst(reward = uiState.reward)

                LootBoxPhase.REVEALING ->
                    LootBoxRewardReveal(
                        reward     = uiState.reward,
                        onDismiss  = { viewModel.dismiss() }
                    )

                else -> Unit
            }
        }
    }
}

// ── Phase 1 + 2: Waiting / Shaking ──────────────────────────────────────────

@Composable
private fun LootBoxWaiting(
    reward: LootBoxReward?,
    shaking: Boolean,
    onTap: () -> Unit
) {
    val rarityColor = rarityColor(reward?.rarity)

    // Idle gentle float
    val infiniteTransition = rememberInfiniteTransition(label = "boxFloat")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue  = 10f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "floatY"
    )

    // Ambient glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue  = 0.8f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Shake: rapid left-right
    val shakeX by animateFloatAsState(
        targetValue = if (shaking) 18f else 0f,
        animationSpec = if (shaking) infiniteRepeatable(
            tween(60, easing = LinearEasing),
            RepeatMode.Reverse
        ) else spring(),
        label = "shake"
    )

    // Star particles orbiting
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.offset(y = if (!shaking) floatY.dp else 0.dp)
    ) {
        // Hint text
        Text(
            text = if (shaking) "Opening…" else "Tap to open!",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Box body with orbital rings
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow ring
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .alpha(glowAlpha * 0.4f)
                    .background(
                        Brush.radialGradient(
                            listOf(rarityColor.copy(alpha = 0.6f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )

            // Orbiting star particles
            repeat(8) { i ->
                val angle by infiniteTransition.animateFloat(
                    initialValue = (i * 45f),
                    targetValue  = (i * 45f) + 360f,
                    animationSpec = infiniteRepeatable(
                        tween(3000 + i * 200, easing = LinearEasing)
                    ),
                    label = "orbit_$i"
                )
                val rad = angle * (PI / 180.0)
                val orbitR = 95f
                Box(
                    modifier = Modifier
                        .offset(
                            x = (orbitR * cos(rad)).toFloat().dp - 8.dp,
                            y = (orbitR * sin(rad)).toFloat().dp - 8.dp
                        )
                        .size(16.dp)
                        .alpha(glowAlpha)
                ) {
                    Text(
                        text = listOf("✨", "⭐", "💫", "🌟")[i % 4],
                        fontSize = 14.sp
                    )
                }
            }

            // The actual box
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .offset(x = shakeX.dp)
                    .shadow(
                        elevation  = 32.dp,
                        shape      = RoundedCornerShape(24.dp),
                        ambientColor = rarityColor,
                        spotColor    = rarityColor
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(
                                rarityColor.copy(alpha = 0.85f),
                                rarityColor.copy(alpha = 0.4f),
                                Color(0xFF0D0D2B)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .border(
                        width  = 2.dp,
                        brush  = Brush.linearGradient(
                            listOf(rarityColor, Color.White.copy(alpha = 0.3f), rarityColor)
                        ),
                        shape  = RoundedCornerShape(24.dp)
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onTap() })
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🎁", fontSize = 56.sp)
                    Text(
                        text = rarityLabel(reward?.rarity),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // Rarity indicator dots
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val dotCount = when (reward?.rarity) {
                LootBoxRarity.LEGENDARY -> 4
                LootBoxRarity.EPIC      -> 3
                LootBoxRarity.RARE      -> 2
                else                    -> 1
            }
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (i < dotCount) rarityColor else Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                )
            }
        }
    }
}

// ── Phase 3: Burst ───────────────────────────────────────────────────────────

@Composable
private fun LootBoxBurst(reward: LootBoxReward?) {
    val rarityColor = rarityColor(reward?.rarity)

    // Radial burst animation
    val burst by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "burst"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Canvas: radial burst rays
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = sqrt(cx.pow(2) + cy.pow(2)) * burst

            repeat(24) { i ->
                val angle = (i * 15.0) * (PI / 180.0)
                val len   = maxR * (0.6f + (i % 3) * 0.2f)
                drawLine(
                    color       = rarityColor.copy(alpha = (1f - burst) * 0.8f),
                    start       = Offset(cx, cy),
                    end         = Offset(
                        cx + (cos(angle) * len).toFloat(),
                        cy + (sin(angle) * len).toFloat()
                    ),
                    strokeWidth = (6f - burst * 4f).coerceAtLeast(1f)
                )
            }

            // Expanding ring
            drawCircle(
                color  = rarityColor.copy(alpha = (1f - burst) * 0.6f),
                radius = maxR * 0.5f,
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f * (1f - burst * 0.9f))
            )
        }

        // Shard emojis flying outward
        val emojis = listOf("🎊", "✨", "⭐", "🎉", "💫", "🌟", "🎈", "💥")
        emojis.forEachIndexed { i, emoji ->
            val angle = (i * 45.0) * (PI / 180.0)
            val dist  = 160f * burst
            Box(
                modifier = Modifier
                    .offset(
                        x = (cos(angle) * dist).toFloat().dp,
                        y = (sin(angle) * dist).toFloat().dp
                    )
                    .alpha((1f - burst).coerceAtLeast(0f))
            ) {
                Text(emoji, fontSize = 28.sp)
            }
        }

        // Central flash
        Box(
            modifier = Modifier
                .size((200 * (1f - burst * 0.85f)).dp.coerceAtLeast(20.dp))
                .alpha((1f - burst).coerceAtLeast(0f))
                .background(
                    Brush.radialGradient(listOf(Color.White, rarityColor, Color.Transparent)),
                    CircleShape
                )
        )
    }
}

// ── Phase 4: Reward Reveal ───────────────────────────────────────────────────

@Composable
private fun LootBoxRewardReveal(
    reward: LootBoxReward?,
    onDismiss: () -> Unit
) {
    val rarityColor = rarityColor(reward?.rarity)

    // Card entrance
    var entered by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue   = if (entered) 1f else 0.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "cardScale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue   = if (entered) 1f else 0f,
        animationSpec = tween(400),
        label = "cardAlpha"
    )

    // Emoji bounce
    var emojiIn by remember { mutableStateOf(false) }
    val emojiScale by animateFloatAsState(
        targetValue   = if (emojiIn) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "emojiScale"
    )

    LaunchedEffect(Unit) {
        entered = true
        kotlinx.coroutines.delay(300)
        emojiIn = true
    }

    // Continuous shimmer on card
    val shimmerInfinite = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by shimmerInfinite.animateFloat(
        initialValue = -300f,
        targetValue  = 600f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "shimmerX"
    )

    // Floating confetti
    val confettiInfinite = rememberInfiniteTransition(label = "confetti")

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background confetti rain
        repeat(16) { i ->
            val startY by confettiInfinite.animateFloat(
                initialValue = -80f,
                targetValue  = 120f,
                animationSpec = infiniteRepeatable(
                    tween((1800 + i * 130), easing = LinearEasing,
                        delayMillis = (i * 100)),
                    RepeatMode.Restart
                ),
                label = "confetti_y_$i"
            )
            val xPos = (i * 6.7f) % 100f
            Text(
                text = listOf("🎉", "✨", "⭐", "🎊", "💫")[i % 5],
                fontSize = (14 + (i % 3) * 4).sp,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart)
                    .offset(
                        x = (xPos / 100f * 400 - 200).dp,
                        y = (startY / 100f * 900 - 100).dp
                    )
                    .alpha(0.6f)
            )
        }

        // The reward card
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .scale(cardScale)
                .alpha(cardAlpha),
            shape     = RoundedCornerShape(28.dp),
            color     = CardDark,
            border    = BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(
                    listOf(rarityColor, Color.White.copy(alpha = 0.3f), rarityColor),
                    start = Offset(shimmerX, 0f),
                    end   = Offset(shimmerX + 300f, 300f)
                )
            ),
            shadowElevation = 32.dp,
            tonalElevation  = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rarity banner
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = rarityColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "✦ ${rarityLabel(reward?.rarity)} ✦",
                        color = rarityColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }

                // Big emoji (bounces in)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(emojiScale)
                        .background(
                            Brush.radialGradient(
                                listOf(rarityColor.copy(alpha = 0.25f), Color.Transparent)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(reward?.emoji ?: "🎁", fontSize = 60.sp)
                }

                // Title
                Text(
                    text = reward?.title ?: "Reward!",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                // Description
                Text(
                    text = reward?.description ?: "",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                // XP pill (if applicable)
                if ((reward?.xpValue ?: 0) > 0) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Color(0xFFFFD700).copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⭐", fontSize = 16.sp)
                            Text(
                                text = "+${reward?.xpValue} XP BONUS",
                                color = Color(0xFFFFD700),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Claim button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = rarityColor
                    )
                ) {
                    Text(
                        "Claim Reward! 🎉",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 16.sp,
                        color      = Color.White
                    )
                }
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun rarityColor(rarity: LootBoxRarity?): Color = when (rarity) {
    LootBoxRarity.LEGENDARY -> LegendaryFire
    LootBoxRarity.EPIC      -> EpicPurple
    LootBoxRarity.RARE      -> RareBlue
    else                    -> CommonGold
}

private fun rarityLabel(rarity: LootBoxRarity?): String = when (rarity) {
    LootBoxRarity.LEGENDARY -> "LEGENDARY"
    LootBoxRarity.EPIC      -> "EPIC"
    LootBoxRarity.RARE      -> "RARE"
    else                    -> "COMMON"
}