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
 * SEQUENCE GAME: Find the next number in a sequence.
 * Features:
 * - Age-adaptive patterns (counting → Fibonacci → polynomial)
 * - Visual sequence display with animated "?" placeholder
 * - Pattern hint button after first wrong answer
 * - 3 correct answers to win
 */
@Composable
fun SequenceGameBlock(
    ageGroup: AgeGroup = AgeGroup.EXPLORER,
    onSuccess: () -> Unit
) {
    data class SeqProblem(
        val sequence: List<Int>,
        val answer: Int,
        val hint: String,
        val options: List<Int>
    )

    fun generateProblem(): SeqProblem = when (ageGroup) {
        AgeGroup.SPROUT -> {
            val step = listOf(1, 2).random()
            val start = (1..5).random()
            val seq = List(5) { start + it * step }
            val answer = seq.last()
            val display = seq.dropLast(1)
            val wrong1 = answer + (1..3).random()
            val wrong2 = (answer - (1..3).random()).coerceAtLeast(0)
            SeqProblem(
                sequence = display,
                answer = answer,
                hint = "Each number goes up by $step",
                options = listOf(answer, wrong1, wrong2).distinct()
                    .let { if (it.size < 3) it + listOf(answer + 4) else it }
                    .take(3).shuffled()
            )
        }
        AgeGroup.EXPLORER -> {
            val type = (0..1).random()
            when (type) {
                0 -> {
                    val step = listOf(3, 5, 10).random()
                    val start = (2..10).random()
                    val seq = List(6) { start + it * step }
                    val answer = seq.last()
                    val display = seq.dropLast(1)
                    SeqProblem(
                        sequence = display,
                        answer = answer,
                        hint = "Add $step each time",
                        options = listOf(answer, answer + step, answer - step).shuffled()
                    )
                }
                else -> {
                    val start = (2..5).random()
                    val seq = List(6) { start * (1 shl it) }
                    val answer = seq.last()
                    val display = seq.dropLast(1)
                    SeqProblem(
                        sequence = display,
                        answer = answer,
                        hint = "Each number doubles",
                        options = listOf(answer, answer + start, answer / 2).shuffled()
                    )
                }
            }
        }
        AgeGroup.TRAILBLAZER -> {
            val type = (0..2).random()
            when (type) {
                0 -> {
                    // Fibonacci-like
                    val a = (1..3).random()
                    val b = (1..4).random()
                    val seq = mutableListOf(a, b)
                    repeat(5) { seq.add(seq[seq.size - 1] + seq[seq.size - 2]) }
                    val answer = seq.last()
                    val display = seq.dropLast(1)
                    SeqProblem(
                        sequence = display,
                        answer = answer,
                        hint = "Add the last two numbers",
                        options = listOf(answer, answer + seq[seq.size - 3], answer - 1).shuffled()
                    )
                }
                1 -> {
                    // Triangular numbers: 1, 3, 6, 10, 15, 21
                    val seq = List(7) { n -> (n + 1) * (n + 2) / 2 }
                    val answer = seq.last()
                    val display = seq.dropLast(1)
                    SeqProblem(
                        sequence = display,
                        answer = answer,
                        hint = "Differences increase by 1 each time",
                        options = listOf(answer, answer + 5, answer - 3).shuffled()
                    )
                }
                else -> {
                    // Geometric ×2 or ×3
                    val multiplier = listOf(2, 3).random()
                    val start = (1..3).random()
                    val seq = List(6) { i ->
                        var result = start
                        repeat(i) { result *= multiplier }
                        result
                    }
                    val answer = seq.last()
                    val display = seq.dropLast(1)
                    SeqProblem(
                        sequence = display,
                        answer = answer,
                        hint = "Multiply by $multiplier each time",
                        options = listOf(answer, answer + multiplier, answer * 2).distinct().take(3).shuffled()
                    )
                }
            }
        }
        AgeGroup.LEGEND -> {
            val type = (0..2).random()
            when (type) {
                0 -> {
                    // Polynomial: n²
                    val seq = List(7) { (it + 1) * (it + 1) }
                    val answer = seq.last()
                    val display = seq.dropLast(1)
                    SeqProblem(
                        sequence = display,
                        answer = answer,
                        hint = "These are perfect squares (n²)",
                        options = listOf(answer, answer + 2, answer - 3).shuffled()
                    )
                }
                1 -> {
                    // Alternating add/subtract: +3, -1, +3, -1
                    val start = (5..10).random()
                    val seq = mutableListOf(start)
                    repeat(6) { i ->
                        seq.add(if (i % 2 == 0) seq.last() + 3 else seq.last() - 1)
                    }
                    val answer = seq.last()
                    val display = seq.dropLast(1)
                    SeqProblem(
                        sequence = display,
                        answer = answer,
                        hint = "Pattern alternates: +3, -1, +3, -1...",
                        options = listOf(answer, answer + 3, answer - 1).distinct().take(3).shuffled()
                    )
                }
                else -> {
                    // Cubic: n³
                    val seq = List(6) { (it + 1) * (it + 1) * (it + 1) }
                    val answer = seq.last()
                    val display = seq.dropLast(1)
                    SeqProblem(
                        sequence = display,
                        answer = answer,
                        hint = "These are perfect cubes (n³)",
                        options = listOf(answer, answer + 10, answer - 15).shuffled()
                    )
                }
            }
        }
    }

    var problem by remember { mutableStateOf(generateProblem()) }
    var score by remember { mutableStateOf(0) }
    var answered by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showHint by remember { mutableStateOf(false) }
    var wrongAttempts by remember { mutableStateOf(0) }
    var showSuccess by remember { mutableStateOf(false) }

    // Pulse animation for the "?" in the sequence
    val infiniteTransition = rememberInfiniteTransition(label = "question_pulse")
    val questionScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "question_scale"
    )

    // Advance after answer
    LaunchedEffect(answered) {
        if (answered) {
            delay(1000)
            if (score >= 3) {
                showSuccess = true
                delay(1200)
                onSuccess()
            } else {
                problem = generateProblem()
                answered = false
                selectedOption = null
                showHint = false
                wrongAttempts = 0
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
            "🔗 Sequence Finder",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF00B894)
        )

        // Score
        Box(
            Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFF00B894).copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Score: $score / 3", fontWeight = FontWeight.Bold, color = Color(0xFF00B894))
        }

        // Sequence display
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFDFE6E9))
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            problem.sequence.forEach { num ->
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        num.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3436)
                    )
                }
            }
            // Animated question mark
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .scale(questionScale)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFD93D))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF6B35)
                )
            }
        }

        // Hint button (visible after first wrong answer)
        if (wrongAttempts > 0 && !showHint && !answered) {
            TextButton(onClick = { showHint = true }) {
                Text("💡 Show Hint", color = Color(0xFFFF6B35), fontWeight = FontWeight.Bold)
            }
        }

        // Hint display
        AnimatedVisibility(
            visible = showHint,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFD93D).copy(alpha = 0.2f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "💡 ${problem.hint}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Options
        Text(
            "What comes next?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            problem.options.forEach { option ->
                SequenceOption(
                    value = option,
                    isSelected = selectedOption == option,
                    isCorrect = option == problem.answer,
                    isAnswered = answered,
                    onClick = {
                        if (!answered) {
                            SoundManager.playTap()
                            if (option == problem.answer) {
                                selectedOption = option
                                answered = true
                                SoundManager.playSuccess()
                                score++
                            } else {
                                SoundManager.playError()
                                wrongAttempts++
                            }
                        }
                    }
                )
            }
        }

        // Success
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
                        "🎉 Pattern Master!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text("You cracked 3 sequences!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SequenceOption(
    value: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    isAnswered: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "seq_option_scale"
    )

    val bgColor = when {
        !isAnswered -> Color(0xFF00B894).copy(alpha = 0.1f)
        isSelected && isCorrect -> Color(0xFF90EE90)
        isSelected && !isCorrect -> Color(0xFFFFCCCC)
        isAnswered && isCorrect -> Color(0xFF90EE90).copy(alpha = 0.5f)
        else -> Color(0xFF00B894).copy(alpha = 0.1f)
    }

    val textColor = when {
        isSelected && isCorrect -> Color(0xFF2E7D32)
        isSelected && !isCorrect -> Color(0xFFC62828)
        isAnswered && isCorrect -> Color(0xFF2E7D32)
        else -> Color(0xFF00B894)
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
