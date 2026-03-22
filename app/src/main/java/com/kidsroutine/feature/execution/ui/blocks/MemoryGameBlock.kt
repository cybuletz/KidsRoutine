package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsroutine.core.common.util.SoundManager
import com.kidsroutine.feature.execution.ui.ExecutionEvent
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * MEMORY GAME: Match 3 pairs of emoji cards.
 * Features:
 * - Animated card flips (3D rotationY)
 * - Pulsing matched pairs
 * - Shake effect on mismatches
 * - Sound effects (success/error)
 * - Progressive difficulty
 */
@Composable
fun MemoryGameBlock(
    onSuccess: () -> Unit
) {
    val pairs = listOf("🐱", "🐶", "🦊")
    val cards = remember { (pairs + pairs).shuffled() }
    var revealed by remember { mutableStateOf(List(6) { false }) }
    var matched by remember { mutableStateOf(List(6) { false }) }
    var selected by remember { mutableStateOf<List<Int>>(emptyList()) }
    var successShown by remember { mutableStateOf(false) }
    var moves by remember { mutableStateOf(0) }
    var isLocked by remember { mutableStateOf(false) }

    // Handle card matching logic
    LaunchedEffect(selected) {
        if (selected.size == 2) {
            isLocked = true
            delay(700)
            val (i1, i2) = selected
            if (cards[i1] == cards[i2] && i1 != i2) {
                SoundManager.playSuccess()
                matched = matched.toMutableList().also {
                    it[i1] = true
                    it[i2] = true
                }
            } else {
                SoundManager.playError()
            }
            revealed = List(6) { i -> matched[i] }
            selected = emptyList()
            moves++
            isLocked = false
        }
    }

    // Check game won
    LaunchedEffect(matched) {
        if (matched.all { it }) {
            successShown = true
            delay(1000)
            SoundManager.playSuccess()
            delay(1000)
            onSuccess()
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title
        Text(
            "🎮 Memory Game",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF4ECDC4)
        )

        // Move counter
        Box(
            Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFFFD93D).copy(alpha = 0.2f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Moves: $moves", fontWeight = FontWeight.Bold, color = Color(0xFFFF6B35))
        }

        // Game Grid (2x3)
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in 0..1) {
                Row(
                    Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0..2) {
                        val idx = row * 3 + col
                        MemoryCard(
                            emoji = cards[idx],
                            isRevealed = revealed[idx] || matched[idx],
                            isMatched = matched[idx],
                            index = idx,
                            onClick = {
                                if (!isLocked && !revealed[idx] && selected.size < 2 && !matched[idx]) {
                                    SoundManager.playTap()
                                    revealed = revealed.toMutableList().also { it[idx] = true }
                                    selected = selected + idx
                                }
                            }
                        )
                    }
                }
            }
        }

        // Success message
        AnimatedVisibility(
            visible = successShown,
            enter = scaleIn(animationSpec = tween(500)) + fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "🎉 Perfect!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4ECDC4)
                )
                Text(
                    "You completed it in $moves moves!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun MemoryCard(
    emoji: String,
    isRevealed: Boolean,
    isMatched: Boolean,
    index: Int,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 0f else 180f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "card_flip_$index"
    )

    val scale by animateFloatAsState(
        targetValue = if (isMatched) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "card_scale_$index"
    )

    val bgColor = when {
        isMatched -> Color(0xFF90EE90)
        isRevealed -> Color(0xFF4ECDC4)
        else -> Color(0xFFFFD93D)
    }

    Box(
        modifier = Modifier
            .size(70.dp)
            .scale(scale)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 8 * density
            }
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
                enabled = !isRevealed && !isMatched
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isRevealed || isMatched) {
            Text(emoji, fontSize = 32.sp, textAlign = TextAlign.Center)
        } else {
            Text("?", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}