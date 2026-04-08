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
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.roundToInt

/**
 * ESTIMATION GAME: Guess quantities with hot/cold feedback.
 * Features:
 * - Slider input for guessing
 * - Accuracy bar with green/yellow/red zones
 * - Age-adaptive: simple counting → Fermi estimation
 * - 3 correct estimates to win
 */
@Composable
fun EstimationGameBlock(
    ageGroup: AgeGroup = AgeGroup.EXPLORER,
    onSuccess: () -> Unit
) {
    data class EstProblem(
        val question: String,
        val answer: Int,
        val sliderMax: Int,
        val tolerancePercent: Float,
        val unit: String,
        val emojis: String
    )

    fun generateProblem(): EstProblem = when (ageGroup) {
        AgeGroup.SPROUT -> {
            val emoji = listOf("🍎", "⭐", "🐱", "🎈", "🌸").random()
            val count = (2..10).random()
            EstProblem(
                question = "How many $emoji are there?",
                answer = count,
                sliderMax = 15,
                tolerancePercent = 0.15f,
                unit = "",
                emojis = List(count) { emoji }.joinToString(" ")
            )
        }
        AgeGroup.EXPLORER -> {
            val type = (0..2).random()
            when (type) {
                0 -> EstProblem(
                    question = "How many minutes to walk 1 km?",
                    answer = 12,
                    sliderMax = 30,
                    tolerancePercent = 0.25f,
                    unit = "min",
                    emojis = "🚶‍♂️ ➡️ 1 km"
                )
                1 -> EstProblem(
                    question = "How many cm is a banana?",
                    answer = 20,
                    sliderMax = 50,
                    tolerancePercent = 0.25f,
                    unit = "cm",
                    emojis = "🍌 📏"
                )
                else -> EstProblem(
                    question = "How many minutes is a movie?",
                    answer = 90,
                    sliderMax = 180,
                    tolerancePercent = 0.2f,
                    unit = "min",
                    emojis = "🎬 🍿"
                )
            }
        }
        AgeGroup.TRAILBLAZER -> {
            val type = (0..2).random()
            when (type) {
                0 -> EstProblem(
                    question = "How many grains of rice in a cup?",
                    answer = 9000,
                    sliderMax = 20000,
                    tolerancePercent = 0.2f,
                    unit = "grains",
                    emojis = "🍚 🥤"
                )
                1 -> EstProblem(
                    question = "How many pages in the Harry Potter series?",
                    answer = 4100,
                    sliderMax = 10000,
                    tolerancePercent = 0.2f,
                    unit = "pages",
                    emojis = "📚 🧙‍♂️"
                )
                else -> EstProblem(
                    question = "How many bones in the human body?",
                    answer = 206,
                    sliderMax = 500,
                    tolerancePercent = 0.2f,
                    unit = "bones",
                    emojis = "🦴 🏥"
                )
            }
        }
        AgeGroup.LEGEND -> {
            val type = (0..2).random()
            when (type) {
                0 -> EstProblem(
                    question = "How many piano tuners in a city of 1M people?",
                    answer = 100,
                    sliderMax = 1000,
                    tolerancePercent = 0.5f,
                    unit = "tuners",
                    emojis = "🎹 🔧 🏙️"
                )
                1 -> EstProblem(
                    question = "How many tennis balls fit in this room?",
                    answer = 250000,
                    sliderMax = 1000000,
                    tolerancePercent = 0.5f,
                    unit = "balls",
                    emojis = "🎾 🏠"
                )
                else -> EstProblem(
                    question = "How many breaths do you take per day?",
                    answer = 20000,
                    sliderMax = 100000,
                    tolerancePercent = 0.5f,
                    unit = "breaths",
                    emojis = "😤 🌬️"
                )
            }
        }
    }

    fun isCloseEnough(guess: Int, answer: Int, tolerancePercent: Float): Boolean {
        return if (ageGroup == AgeGroup.LEGEND) {
            // Order of magnitude check for Fermi problems
            val guessLog = if (guess > 0) log10(guess.toFloat()) else 0f
            val answerLog = log10(answer.toFloat())
            abs(guessLog - answerLog) < 1f
        } else if (ageGroup == AgeGroup.SPROUT) {
            abs(guess - answer) <= 1
        } else {
            abs(guess - answer).toFloat() / answer <= tolerancePercent
        }
    }

    fun getAccuracy(guess: Int, answer: Int): Float {
        if (answer == 0) return 0f
        val diff = abs(guess - answer).toFloat() / answer
        return (1f - diff).coerceIn(0f, 1f)
    }

    var problem by remember { mutableStateOf(generateProblem()) }
    var score by remember { mutableStateOf(0) }
    var sliderValue by remember { mutableFloatStateOf(problem.sliderMax / 2f) }
    var submitted by remember { mutableStateOf(false) }
    var lastAccuracy by remember { mutableFloatStateOf(0f) }
    var lastCorrect by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Advance after submission
    LaunchedEffect(submitted) {
        if (submitted) {
            delay(1800)
            if (score >= 3) {
                showSuccess = true
                delay(1200)
                onSuccess()
            } else {
                val newProblem = generateProblem()
                problem = newProblem
                sliderValue = newProblem.sliderMax / 2f
                submitted = false
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
            "🎯 Estimation Game",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFE17055)
        )

        // Score
        Box(
            Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFE17055).copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Score: $score / 3", fontWeight = FontWeight.Bold, color = Color(0xFFE17055))
        }

        // Emoji display
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFF3E0))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                problem.emojis,
                fontSize = if (ageGroup == AgeGroup.SPROUT) 28.sp else 36.sp,
                textAlign = TextAlign.Center
            )
        }

        // Question
        Text(
            problem.question,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center
        )

        // Slider
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Your guess: ${sliderValue.roundToInt()} ${problem.unit}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE17055)
            )
            Spacer(Modifier.height(8.dp))
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 0f..problem.sliderMax.toFloat(),
                enabled = !submitted,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFE17055),
                    activeTrackColor = Color(0xFFE17055)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(
                    problem.sliderMax.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Submit button
        if (!submitted) {
            Button(
                onClick = {
                    SoundManager.playTap()
                    val guess = sliderValue.roundToInt()
                    lastAccuracy = getAccuracy(guess, problem.answer)
                    lastCorrect = isCloseEnough(guess, problem.answer, problem.tolerancePercent)
                    submitted = true
                    if (lastCorrect) {
                        SoundManager.playSuccess()
                        score++
                    } else {
                        SoundManager.playError()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE17055)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Submit Guess", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Result feedback
        AnimatedVisibility(
            visible = submitted,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Accuracy bar
                Text(
                    "Accuracy: ${(lastAccuracy * 100).roundToInt()}%",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(lastAccuracy)
                            .background(
                                when {
                                    lastAccuracy >= 0.9f -> Color(0xFF4CAF50)
                                    lastAccuracy >= 0.7f -> Color(0xFFFFEB3B)
                                    else -> Color(0xFFF44336)
                                }
                            )
                    )
                }

                // Result message
                Text(
                    if (lastCorrect) "✅ Close enough! Answer: ${problem.answer}"
                    else "❌ Not quite! Answer: ${problem.answer} ${problem.unit}",
                    fontWeight = FontWeight.Bold,
                    color = if (lastCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                    textAlign = TextAlign.Center
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
                        "🎉 Great Estimator!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text("3 estimates on target!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
