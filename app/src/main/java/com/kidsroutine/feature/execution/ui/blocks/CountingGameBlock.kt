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
import kotlinx.coroutines.delay

/**
 * COUNTING GAME: Count emoji objects and pick the correct number.
 * Features:
 * - Grid of emoji objects to count
 * - Age-adaptive difficulty (simple counting → estimation)
 * - Animated option buttons with bounce feedback
 * - 3 correct answers to win
 */
@Composable
fun CountingGameBlock(
    ageGroup: AgeGroup = AgeGroup.EXPLORER,
    onSuccess: () -> Unit
) {
    data class CountProblem(
        val emojis: List<String>,
        val question: String,
        val answer: Int,
        val options: List<Int>
    )

    val allEmojis = listOf("🍎", "🐱", "⭐", "🌸", "🐶", "🍊", "🦋", "🐟", "🌈", "🎈")

    fun generateProblem(): CountProblem = when (ageGroup) {
        AgeGroup.SPROUT -> {
            val emoji = allEmojis.take(3).random()
            val count = (1..5).random()
            val grid = List(count) { emoji }
            val wrong1 = (count + (1..2).random()).coerceAtMost(9)
            val wrong2 = (count - (1..2).random()).coerceAtLeast(1)
            CountProblem(
                emojis = grid,
                question = "How many $emoji?",
                answer = count,
                options = listOf(count, wrong1, wrong2).distinct()
                    .let { if (it.size < 3) it + (1..9).filter { n -> n !in it }.take(3 - it.size) else it }
                    .shuffled()
            )
        }
        AgeGroup.EXPLORER -> {
            val target = allEmojis.take(5).random()
            val distractor = allEmojis.filter { it != target }.take(2)
            val targetCount = (5..10).random()
            val distractorCount = (3..7).random()
            val grid = List(targetCount) { target } +
                List(distractorCount) { distractor.random() }
            val wrong1 = targetCount + (1..3).random()
            val wrong2 = (targetCount - (1..3).random()).coerceAtLeast(1)
            CountProblem(
                emojis = grid.shuffled(),
                question = "How many $target?",
                answer = targetCount,
                options = listOf(targetCount, wrong1, wrong2).distinct()
                    .let { if (it.size < 3) it + (1..20).filter { n -> n !in it }.take(3 - it.size) else it }
                    .shuffled()
            )
        }
        AgeGroup.TRAILBLAZER -> {
            val skipBy = listOf(2, 3, 5).random()
            val emoji = "🔵"
            val groups = (3..6).random()
            val count = groups * skipBy
            val grid = List(count) { emoji }
            val wrong1 = count + skipBy
            val wrong2 = count - skipBy
            CountProblem(
                emojis = grid,
                question = "Count by ${skipBy}s — how many $emoji?",
                answer = count,
                options = listOf(count, wrong1, wrong2).shuffled()
            )
        }
        AgeGroup.LEGEND -> {
            val emoji = "🟢"
            val exact = (50..200).random()
            val shown = exact
            val grid = List(shown) { emoji }
            val tolerance = (exact * 0.15).toInt().coerceAtLeast(5)
            val close = exact + (-tolerance..tolerance).random()
            val far1 = exact + (tolerance + 10..tolerance + 40).random()
            val far2 = (exact - (tolerance + 10..tolerance + 40).random()).coerceAtLeast(10)
            CountProblem(
                emojis = grid,
                question = "Estimate: ~how many $emoji?",
                answer = close,
                options = listOf(close, far1, far2).shuffled()
            )
        }
    }

    var problem by remember { mutableStateOf(generateProblem()) }
    var score by remember { mutableStateOf(0) }
    var answered by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    // Advance to next problem after answer feedback
    LaunchedEffect(answered) {
        if (answered) {
            delay(900)
            if (score >= 3) {
                showSuccess = true
                delay(1200)
                onSuccess()
            } else {
                problem = generateProblem()
                answered = false
                selectedOption = null
            }
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
            "🔢 Counting Game",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFF6B35)
        )

        // Score
        Box(
            Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFFF6B35).copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Score: $score / 3", fontWeight = FontWeight.Bold, color = Color(0xFFFF6B35))
        }

        // Emoji grid
        val maxDisplay = if (ageGroup == AgeGroup.LEGEND) 80 else problem.emojis.size
        val displayEmojis = problem.emojis.take(maxDisplay)
        val columns = when {
            displayEmojis.size <= 6 -> 3
            displayEmojis.size <= 15 -> 5
            displayEmojis.size <= 40 -> 8
            else -> 10
        }
        val fontSize = when {
            displayEmojis.size <= 10 -> 28.sp
            displayEmojis.size <= 30 -> 20.sp
            else -> 14.sp
        }

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFD93D).copy(alpha = 0.1f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val rows = (displayEmojis.size + columns - 1) / columns
                for (row in 0 until rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (col in 0 until columns) {
                            val idx = row * columns + col
                            if (idx < displayEmojis.size) {
                                Text(displayEmojis[idx], fontSize = fontSize)
                            }
                        }
                    }
                }
                if (ageGroup == AgeGroup.LEGEND && problem.emojis.size > maxDisplay) {
                    Text(
                        "... and more!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        // Question
        Text(
            problem.question,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center
        )

        // Options
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            problem.options.forEach { option ->
                CountingOption(
                    value = option,
                    isSelected = selectedOption == option,
                    isCorrect = option == problem.answer,
                    isAnswered = answered,
                    onClick = {
                        if (!answered) {
                            SoundManager.playTap()
                            selectedOption = option
                            answered = true
                            if (option == problem.answer) {
                                SoundManager.playSuccess()
                                score++
                            } else {
                                SoundManager.playError()
                            }
                        }
                    }
                )
            }
        }

        // Success banner
        AnimatedVisibility(
            visible = showSuccess,
            enter = scaleIn(animationSpec = tween(500)) + fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF90EE90))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "🎉 Great Counting!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text("You got 3 correct!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CountingOption(
    value: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    isAnswered: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "counting_option_scale"
    )

    val bgColor = when {
        !isAnswered -> Color(0xFF4ECDC4).copy(alpha = 0.1f)
        isSelected && isCorrect -> Color(0xFF90EE90)
        isSelected && !isCorrect -> Color(0xFFFFCCCC)
        isAnswered && isCorrect -> Color(0xFF90EE90).copy(alpha = 0.5f)
        else -> Color(0xFF4ECDC4).copy(alpha = 0.1f)
    }

    val textColor = when {
        isSelected && isCorrect -> Color(0xFF2E7D32)
        isSelected && !isCorrect -> Color(0xFFC62828)
        isAnswered && isCorrect -> Color(0xFF2E7D32)
        else -> Color(0xFF4ECDC4)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(enabled = !isAnswered) { onClick() }
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            value.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}
