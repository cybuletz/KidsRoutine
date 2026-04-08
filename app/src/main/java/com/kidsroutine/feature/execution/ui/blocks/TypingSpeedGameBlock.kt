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
 * TYPING SPEED GAME: Type letters/words/sentences against the clock.
 * Features:
 * - SPROUT: Tap matching letter from 4 button options
 * - EXPLORER: Type short words via OutlinedTextField
 * - TRAILBLAZER/LEGEND: Type sentences, measure WPM and accuracy
 * - 30-second countdown timer for all ages
 * - WPM counter, accuracy %, and progress tracking
 */
@Composable
fun TypingSpeedGameBlock(
    ageGroup: AgeGroup = AgeGroup.EXPLORER,
    onSuccess: () -> Unit
) {
    val letters = ('A'..'Z').toList()

    val shortWords = listOf(
        "cat", "dog", "sun", "hat", "run", "big", "cup", "red",
        "hop", "mix", "fun", "jet", "box", "zip", "yes", "pen"
    )

    val sentences = listOf(
        "The quick brown fox jumps over the lazy dog",
        "Practice makes perfect every single day",
        "Coding is a superpower for the future",
        "Stars shine bright on a clear night",
        "Learning new things keeps your brain sharp"
    )

    val codeSnippets = listOf(
        "fun main() { println(\"Hello\") }",
        "val list = listOf(1, 2, 3)",
        "for (i in 0 until n) { sum += i }",
        "if (x > 0) return true else false",
        "data class User(val name: String)"
    )

    val targetWpm = when (ageGroup) {
        AgeGroup.SPROUT, AgeGroup.EXPLORER -> 0
        AgeGroup.TRAILBLAZER -> 20
        AgeGroup.LEGEND -> 30
    }
    val matchTarget = 5

    var timeLeft by remember { mutableIntStateOf(30) }
    var gameStarted by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // SPROUT state
    var currentLetter by remember { mutableStateOf(letters.random()) }
    var letterOptions by remember {
        mutableStateOf(
            (listOf(currentLetter) + letters.filter { it != currentLetter }.shuffled().take(3)).shuffled()
        )
    }
    var sproutScore by remember { mutableIntStateOf(0) }

    // EXPLORER state
    var currentWord by remember { mutableStateOf(shortWords.random()) }
    var typedWord by remember { mutableStateOf("") }
    var explorerScore by remember { mutableIntStateOf(0) }

    // TRAILBLAZER/LEGEND state
    var currentText by remember {
        mutableStateOf(if (ageGroup == AgeGroup.LEGEND) codeSnippets.random() else sentences.random())
    }
    var typedText by remember { mutableStateOf("") }
    var totalCharsTyped by remember { mutableIntStateOf(0) }
    var correctChars by remember { mutableIntStateOf(0) }
    var wordsCompleted by remember { mutableIntStateOf(0) }

    val elapsedSeconds = 30 - timeLeft
    val wpm = if (elapsedSeconds > 0) (wordsCompleted.toFloat() / elapsedSeconds * 60).toInt() else 0
    val accuracy = if (totalCharsTyped > 0) (correctChars.toFloat() / totalCharsTyped * 100).toInt() else 100

    // Timer
    LaunchedEffect(gameStarted) {
        if (gameStarted && !gameOver) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            gameOver = true
        }
    }

    // Win condition check
    LaunchedEffect(sproutScore, explorerScore, wpm, gameOver) {
        if (gameOver && !showSuccess) {
            val won = when (ageGroup) {
                AgeGroup.SPROUT -> sproutScore >= matchTarget
                AgeGroup.EXPLORER -> explorerScore >= matchTarget
                AgeGroup.TRAILBLAZER -> wpm >= targetWpm
                AgeGroup.LEGEND -> wpm >= targetWpm
            }
            if (won) {
                showSuccess = true
                SoundManager.playSuccess()
                delay(1500)
                onSuccess()
            }
        }
        // Early win for SPROUT/EXPLORER
        if (!gameOver) {
            val earlyWin = when (ageGroup) {
                AgeGroup.SPROUT -> sproutScore >= matchTarget
                AgeGroup.EXPLORER -> explorerScore >= matchTarget
                else -> false
            }
            if (earlyWin) {
                gameOver = true
                showSuccess = true
                SoundManager.playSuccess()
                delay(1500)
                onSuccess()
            }
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            "⌨️ Typing Speed",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0984E3)
        )

        // Stats row
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatChip("⏱ ${timeLeft}s", Color(0xFFFF6B35))
            when (ageGroup) {
                AgeGroup.SPROUT -> StatChip("✅ $sproutScore/$matchTarget", Color(0xFF00B894))
                AgeGroup.EXPLORER -> StatChip("✅ $explorerScore/$matchTarget", Color(0xFF00B894))
                AgeGroup.TRAILBLAZER, AgeGroup.LEGEND -> {
                    StatChip("WPM: $wpm", Color(0xFF00B894))
                    StatChip("Acc: $accuracy%", Color(0xFF6C5CE7))
                }
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
                    .fillMaxWidth((timeLeft / 30f).coerceIn(0f, 1f))
                    .background(
                        if (timeLeft > 10) Color(0xFF0984E3)
                        else Color(0xFFFF6B35)
                    )
            )
        }

        if (!gameStarted) {
            // Start screen
            Button(
                onClick = { gameStarted = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0984E3)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("▶ Start!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        } else if (!gameOver) {
            when (ageGroup) {
                AgeGroup.SPROUT -> {
                    // Show letter to match
                    Box(
                        Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFDFE6E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            currentLetter.toString(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0984E3)
                        )
                    }

                    Text("Tap the matching letter!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                    // 4 button options
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        letterOptions.forEach { letter ->
                            LetterButton(
                                letter = letter,
                                onClick = {
                                    SoundManager.playTap()
                                    if (letter == currentLetter) {
                                        SoundManager.playSuccess()
                                        sproutScore++
                                        val next = letters.random()
                                        currentLetter = next
                                        letterOptions = (listOf(next) +
                                            letters.filter { it != next }.shuffled().take(3)).shuffled()
                                    } else {
                                        SoundManager.playError()
                                    }
                                }
                            )
                        }
                    }
                }
                AgeGroup.EXPLORER -> {
                    // Show word to type
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFDFE6E9))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            currentWord,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0984E3)
                        )
                    }

                    OutlinedTextField(
                        value = typedWord,
                        onValueChange = { input ->
                            typedWord = input
                            if (input.equals(currentWord, ignoreCase = true)) {
                                SoundManager.playSuccess()
                                explorerScore++
                                currentWord = shortWords.random()
                                typedWord = ""
                            }
                        },
                        label = { Text("Type the word") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0984E3),
                            cursorColor = Color(0xFF0984E3)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                AgeGroup.TRAILBLAZER, AgeGroup.LEGEND -> {
                    // Show text to type
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFDFE6E9))
                            .padding(16.dp)
                    ) {
                        Text(
                            currentText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3436),
                            textAlign = TextAlign.Start
                        )
                    }

                    OutlinedTextField(
                        value = typedText,
                        onValueChange = { input ->
                            if (input.length > typedText.length) {
                                val newChar = input.last()
                                val expectedIdx = typedText.length
                                totalCharsTyped++
                                if (expectedIdx < currentText.length && newChar == currentText[expectedIdx]) {
                                    correctChars++
                                }
                            }
                            typedText = input

                            if (typedText.length >= currentText.length) {
                                SoundManager.playSuccess()
                                wordsCompleted += currentText.split(" ").size
                                currentText = if (ageGroup == AgeGroup.LEGEND) {
                                    codeSnippets.random()
                                } else {
                                    sentences.random()
                                }
                                typedText = ""
                            }
                        },
                        label = { Text("Type here...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0984E3),
                            cursorColor = Color(0xFF0984E3)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Target WPM indicator
                    Text(
                        "Target: $targetWpm WPM",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        // Game over results
        AnimatedVisibility(
            visible = gameOver,
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
                        if (showSuccess) "🎉 Great Typing!" else "⏰ Time's Up!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (showSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    when (ageGroup) {
                        AgeGroup.SPROUT -> Text(
                            "Letters matched: $sproutScore/$matchTarget",
                            fontWeight = FontWeight.Bold
                        )
                        AgeGroup.EXPLORER -> Text(
                            "Words typed: $explorerScore/$matchTarget",
                            fontWeight = FontWeight.Bold
                        )
                        AgeGroup.TRAILBLAZER, AgeGroup.LEGEND -> {
                            Text("WPM: $wpm (target: $targetWpm)", fontWeight = FontWeight.Bold)
                            Text("Accuracy: $accuracy%", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(text: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold, color = color, fontSize = 13.sp)
    }
}

@Composable
private fun LetterButton(
    letter: Char,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "letter_btn_scale"
    )

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(150)
            pressed = false
        }
    }

    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0984E3))
            .clickable { pressed = true; onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            letter.toString(),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}
