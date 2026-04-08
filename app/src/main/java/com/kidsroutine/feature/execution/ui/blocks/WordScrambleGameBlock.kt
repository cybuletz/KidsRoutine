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
 * WORD SCRAMBLE GAME: Unscramble letters to form words.
 * Ages 8+: Common words
 * Ages 13+: Longer/harder words
 * Ages 17+: Vocabulary words
 */
@Composable
fun WordScrambleGameBlock(
    ageGroup: AgeGroup = AgeGroup.EXPLORER,
    onSuccess: () -> Unit
) {
    val wordPool = when (ageGroup) {
        AgeGroup.SPROUT -> listOf("cat", "dog", "sun", "hat", "cup")
        AgeGroup.EXPLORER -> listOf("brave", "cloud", "flame", "dream", "plant", "smile", "magic")
        AgeGroup.TRAILBLAZER -> listOf("adventure", "challenge", "discovery", "strength", "creative", "powerful")
        AgeGroup.LEGEND -> listOf("perseverance", "opportunity", "determination", "independence", "resilience")
    }

    var currentWord by remember { mutableStateOf(wordPool.random()) }
    var scrambled by remember(currentWord) {
        val letters = currentWord.toList().shuffled().joinToString("")
        // Ensure scrambled word differs from original
        mutableStateOf(
            if (letters.equals(currentWord, ignoreCase = true) && currentWord.length > 1) {
                currentWord.reversed()
            } else {
                letters
            }
        )
    }
    var guess by remember { mutableStateOf("") }
    var score by remember { mutableStateOf(0) }
    var showSuccess by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    val targetScore = 3

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "📝 Word Scramble",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF9B59B6)
        )

        Box(
            Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFF9B59B6).copy(alpha = 0.2f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Score: $score/$targetScore", fontWeight = FontWeight.Bold, color = Color(0xFF9B59B6))
        }

        // Scrambled word display
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFD93D).copy(alpha = 0.15f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                scrambled.uppercase(),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF6B35),
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )
        }

        // Hint
        if (showHint) {
            Text(
                "Hint: First letter is '${currentWord.first().uppercase()}'",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // Input
        OutlinedTextField(
            value = guess,
            onValueChange = { guess = it },
            label = { Text("Your answer") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hint button
            TextButton(onClick = { showHint = true }) {
                Text("💡 Hint")
            }

            // Submit
            Button(
                onClick = {
                    if (guess.trim().equals(currentWord, ignoreCase = true)) {
                        SoundManager.playSuccess()
                        score++
                        if (score >= targetScore) {
                            showSuccess = true
                            onSuccess()
                        } else {
                            currentWord = wordPool.random()
                            val newScrambled = currentWord.toList().shuffled().joinToString("")
                            scrambled = if (newScrambled.equals(currentWord, ignoreCase = true) && currentWord.length > 1) {
                                currentWord.reversed()
                            } else {
                                newScrambled
                            }
                            guess = ""
                            showHint = false
                        }
                    } else {
                        SoundManager.playError()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B59B6))
            ) {
                Text("Submit ✓", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Success
        AnimatedVisibility(visible = showSuccess, enter = scaleIn() + fadeIn()) {
            Text(
                "🎉 Word Master!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF9B59B6)
            )
        }
    }
}
