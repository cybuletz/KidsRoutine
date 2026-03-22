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
import kotlinx.coroutines.delay

/**
 * LOGIC GAME: Solve math puzzles with animated options.
 * Features:
 * - Random math problems (addition, subtraction, multiplication)
 * - Staggered option reveal animations
 * - Correct answer glow
 * - Progressive difficulty
 */
@Composable
fun LogicGameBlock(
    onSuccess: () -> Unit
) {
    data class Problem(val a: Int, val b: Int, val op: Char, val answer: Int)

    fun generateProblem(): Problem {
        val a = (1..10).random()
        val b = (1..10).random()
        val op = listOf('+', '-', '*').random()
        val answer = when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            else -> 0
        }
        return Problem(a, b, op, answer)
    }

    var problem by remember { mutableStateOf(generateProblem()) }
    var score by remember { mutableStateOf(0) }
    var answered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val options = remember(problem) {
        val correct = problem.answer
        val wrong1 = problem.answer + (1..5).random()
        val wrong2 = problem.answer - (1..5).random()
        listOf(correct, wrong1, wrong2).shuffled()
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
            "🧠 Logic Puzzle",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF4ECDC4)
        )

        Box(
            Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFF4ECDC4).copy(alpha = 0.2f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Score: $score", fontWeight = FontWeight.Bold, color = Color(0xFF4ECDC4))
        }

        // Problem
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFD93D).copy(alpha = 0.15f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${problem.a} ${problem.op} ${problem.b} = ?",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF6B35),
                textAlign = TextAlign.Center
            )
        }

        // Options with staggered animation
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEachIndexed { idx, option ->
                LogicOption(
                    option = option,
                    isCorrect = option == problem.answer,
                    index = idx,
                    isAnswered = answered,
                    onSelect = {
                        if (!answered) {
                            answered = true
                            isCorrect = option == problem.answer
                            if (isCorrect) {
                                SoundManager.playSuccess()
                                score++
                            } else {
                                SoundManager.playError()
                            }

                            // Auto-win after 3 correct
                            if (score >= 3) {
                                showSuccess = true
                                onSuccess()
                            } else {
                                problem = generateProblem()
                                answered = false
                            }
                        }
                    }
                )
            }
        }

        // Success banner
        AnimatedVisibility(
            visible = showSuccess,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
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
                        "🎉 Genius!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text("You solved all puzzles!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LogicOption(
    option: Int,
    isCorrect: Boolean,
    index: Int,
    isAnswered: Boolean,
    onSelect: () -> Unit
) {
    val isSelected = remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isSelected.value) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "option_scale_$index"
    )

    val bgColor = when {
        !isAnswered -> Color(0xFF4ECDC4).copy(alpha = 0.1f)
        isSelected.value && isCorrect -> Color(0xFF90EE90)
        isSelected.value && !isCorrect -> Color(0xFFFFCCCC)
        else -> Color(0xFF4ECDC4).copy(alpha = 0.1f)
    }

    val textColor = when {
        isSelected.value && isCorrect -> Color(0xFF2E7D32)
        isSelected.value && !isCorrect -> Color(0xFFC62828)
        else -> Color(0xFF4ECDC4)
    }

    LaunchedEffect(isAnswered) {
        if (isAnswered) {
            delay(index * 150L)
            if (isCorrect) {
                isSelected.value = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(enabled = !isAnswered) {
                isSelected.value = true
                SoundManager.playTap()
                onSelect()
            }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            option.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}