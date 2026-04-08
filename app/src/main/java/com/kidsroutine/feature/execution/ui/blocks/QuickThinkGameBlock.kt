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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsroutine.core.common.util.SoundManager
import com.kidsroutine.core.model.AgeGroup
import kotlinx.coroutines.delay

/**
 * QUICK THINK GAME: Rapid-fire trivia and decision-making.
 * Teens: subject trivia, critical thinking
 * All ages: general knowledge scaled to age
 */
@Composable
fun QuickThinkGameBlock(
    ageGroup: AgeGroup = AgeGroup.TRAILBLAZER,
    onSuccess: () -> Unit
) {
    data class TriviaQuestion(
        val question: String,
        val correctAnswer: String,
        val wrongAnswers: List<String>
    ) {
        val allOptions: List<String> get() = (wrongAnswers + correctAnswer).shuffled()
    }

    val questionPool = when (ageGroup) {
        AgeGroup.SPROUT -> listOf(
            TriviaQuestion("What color is the sky?", "Blue", listOf("Green", "Red")),
            TriviaQuestion("How many legs does a cat have?", "4", listOf("2", "6")),
            TriviaQuestion("What sound does a dog make?", "Woof", listOf("Meow", "Moo"))
        )
        AgeGroup.EXPLORER -> listOf(
            TriviaQuestion("What planet is closest to the Sun?", "Mercury", listOf("Venus", "Mars", "Earth")),
            TriviaQuestion("How many continents are there?", "7", listOf("5", "6", "8")),
            TriviaQuestion("What is the largest ocean?", "Pacific", listOf("Atlantic", "Indian", "Arctic")),
            TriviaQuestion("What gas do plants breathe in?", "Carbon dioxide", listOf("Oxygen", "Nitrogen", "Hydrogen"))
        )
        AgeGroup.TRAILBLAZER -> listOf(
            TriviaQuestion("What is the chemical symbol for gold?", "Au", listOf("Go", "Gd", "Ag")),
            TriviaQuestion("Who wrote Romeo and Juliet?", "Shakespeare", listOf("Dickens", "Austen", "Poe")),
            TriviaQuestion("What is the powerhouse of the cell?", "Mitochondria", listOf("Nucleus", "Ribosome", "Golgi")),
            TriviaQuestion("In what year did World War II end?", "1945", listOf("1943", "1944", "1946")),
            TriviaQuestion("What is the speed of light?", "300,000 km/s", listOf("150,000 km/s", "500,000 km/s", "1,000 km/s"))
        )
        AgeGroup.LEGEND -> listOf(
            TriviaQuestion("What is the derivative of x²?", "2x", listOf("x", "x²", "2")),
            TriviaQuestion("What economic system is based on supply and demand?", "Capitalism", listOf("Socialism", "Feudalism", "Mercantilism")),
            TriviaQuestion("What is Occam's Razor?", "Simplest explanation is usually correct", listOf("Always question authority", "Correlation implies causation", "History repeats itself")),
            TriviaQuestion("What is the Big O notation of binary search?", "O(log n)", listOf("O(n)", "O(n²)", "O(1)"))
        )
    }

    val questions = remember { questionPool.shuffled().take(3) }
    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var answered by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(15000) }

    val currentQuestion = questions.getOrNull(currentIndex)

    // Timer
    LaunchedEffect(currentIndex, answered) {
        if (!answered && currentQuestion != null) {
            timeLeft = 15000
            while (timeLeft > 0 && !answered) {
                delay(100)
                timeLeft -= 100
            }
            if (!answered) {
                answered = true
                SoundManager.playError()
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
        Text(
            "💡 Quick Think",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFE67E22)
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFE67E22).copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Q${currentIndex + 1}/${questions.size}", fontWeight = FontWeight.Bold, color = Color(0xFFE67E22))
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFE67E22).copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("${(timeLeft / 1000)}s", fontWeight = FontWeight.Bold, color = Color(0xFFE67E22))
            }
        }

        // Timer bar
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.LightGray)
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((timeLeft / 15000f).coerceIn(0f, 1f))
                    .background(if (timeLeft > 5000) Color(0xFFE67E22) else Color(0xFFE53935))
            )
        }

        if (currentQuestion != null) {
            // Question
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE67E22).copy(alpha = 0.1f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    currentQuestion.question,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
            }

            // Options
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                currentQuestion.allOptions.forEach { option ->
                    val isCorrect = option == currentQuestion.correctAnswer
                    val isSelected = option == selectedAnswer
                    val bgColor = when {
                        !answered -> Color(0xFFE67E22).copy(alpha = 0.08f)
                        isSelected && isCorrect -> Color(0xFF90EE90)
                        isSelected && !isCorrect -> Color(0xFFFFCCCC)
                        isCorrect && answered -> Color(0xFF90EE90).copy(alpha = 0.5f)
                        else -> Color(0xFFE67E22).copy(alpha = 0.08f)
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .clickable(enabled = !answered) {
                                selectedAnswer = option
                                answered = true
                                if (isCorrect) {
                                    SoundManager.playSuccess()
                                    score++
                                } else {
                                    SoundManager.playError()
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(option, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
                    }
                }
            }

            // Next button
            if (answered) {
                LaunchedEffect(answered) {
                    delay(1500)
                    if (currentIndex < questions.size - 1) {
                        currentIndex++
                        answered = false
                        selectedAnswer = ""
                    } else {
                        showSuccess = true
                        if (score >= 2) onSuccess()
                    }
                }
            }
        }

        AnimatedVisibility(visible = showSuccess, enter = scaleIn() + fadeIn()) {
            Text(
                if (score >= 2) "🎉 Quick Thinker!" else "📚 Keep Practicing!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE67E22)
            )
        }
    }
}
