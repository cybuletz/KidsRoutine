package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsroutine.core.common.util.SoundManager
import com.kidsroutine.core.model.AgeGroup
import com.kidsroutine.feature.execution.ui.ExecutionEvent
import kotlinx.coroutines.delay

/**
 * SPEED GAME: Tap the correct color before time runs out!
 * Features:
 * - Countdown timer bar (animated shrink)
 * - Dynamic color matching
 * - Increasing difficulty
 * - Glow/pulse animations
 * - Age-adaptive: more targets and less time for older groups
 */
@Composable
fun SpeedGameBlock(
    ageGroup: AgeGroup = AgeGroup.EXPLORER,
    onSuccess: () -> Unit
) {
    val colors = listOf(
        Color(0xFFFF6B35), // Orange
        Color(0xFF4ECDC4), // Teal
        Color(0xFFFFD93D), // Yellow
        Color(0xFF95E1D3)  // Mint
    )

    var currentColor by remember { mutableStateOf(colors.random()) }
    var correctColor by remember { mutableStateOf(colors.random()) }
    // Time scales with age group: more time for younger, less for older
    val initialTime = when (ageGroup) {
        AgeGroup.SPROUT -> 7000
        AgeGroup.EXPLORER -> 5000
        AgeGroup.TRAILBLAZER -> 4000
        AgeGroup.LEGEND -> 3000
    }
    val targetScore = when (ageGroup) {
        AgeGroup.SPROUT -> 3
        AgeGroup.EXPLORER -> 3
        AgeGroup.TRAILBLAZER -> 5
        AgeGroup.LEGEND -> 7
    }
    var timeLeft by remember { mutableStateOf(initialTime) }
    var score by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Timer countdown
    LaunchedEffect(gameOver) {
        if (!gameOver) {
            while (timeLeft > 0) {
                delay(100)
                timeLeft -= 100
            }
            gameOver = true
        }
    }

    // Auto-win after targetScore correct
    LaunchedEffect(score) {
        if (score >= targetScore) {
            gameOver = true
            showSuccess = true
            delay(1500)
            onSuccess()
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Title & Score
        Text(
            "⚡ Speed Game",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFF6B35)
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFFFD93D).copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Score: $score/$targetScore", fontWeight = FontWeight.Bold, color = Color(0xFFFF6B35))
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFFF6B35).copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("${(timeLeft / 1000).toInt()}s", fontWeight = FontWeight.Bold, color = Color(0xFFFF6B35))
            }
        }

        // Timer bar
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.LightGray)
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((timeLeft / initialTime.toFloat()).coerceIn(0f, 1f))
                    .background(Color(0xFFFF6B35))
            )
        }

        // Instructions
        Text(
            "Tap the ${getColorName(correctColor)} button!",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        // 4 colored buttons
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpeedButton(
                    color = colors[0],
                    isCorrect = colors[0] == correctColor,
                    onClick = {
                        if (colors[0] == correctColor) {
                            SoundManager.playSuccess()
                            score++
                            correctColor = colors.random()
                            timeLeft = initialTime
                        } else {
                            SoundManager.playError()
                        }
                    }
                )
                SpeedButton(
                    color = colors[1],
                    isCorrect = colors[1] == correctColor,
                    onClick = {
                        if (colors[1] == correctColor) {
                            SoundManager.playSuccess()
                            score++
                            correctColor = colors.random()
                            timeLeft = initialTime
                        } else {
                            SoundManager.playError()
                        }
                    }
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpeedButton(
                    color = colors[2],
                    isCorrect = colors[2] == correctColor,
                    onClick = {
                        if (colors[2] == correctColor) {
                            SoundManager.playSuccess()
                            score++
                            correctColor = colors.random()
                            timeLeft = initialTime
                        } else {
                            SoundManager.playError()
                        }
                    }
                )
                SpeedButton(
                    color = colors[3],
                    isCorrect = colors[3] == correctColor,
                    onClick = {
                        if (colors[3] == correctColor) {
                            SoundManager.playSuccess()
                            score++
                            correctColor = colors.random()
                            timeLeft = initialTime
                        } else {
                            SoundManager.playError()
                        }
                    }
                )
            }
        }

        // Results
        if (gameOver) {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(animationSpec = tween(500)) + fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (showSuccess) Color(0xFF90EE90) else Color(0xFFFFCCCC))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            if (showSuccess) "🎉 You Won!" else "⏰ Time's Up!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Final Score: $score/$targetScore",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedButton(
    color: Color,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "button_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.45f)
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isCorrect) {
            Box(
                Modifier
                    .fillMaxSize(0.8f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
    }
}

private fun getColorName(color: Color): String = when {
    color == Color(0xFFFF6B35) -> "Orange"
    color == Color(0xFF4ECDC4) -> "Teal"
    color == Color(0xFFFFD93D) -> "Yellow"
    color == Color(0xFF95E1D3) -> "Mint"
    else -> "color"
}