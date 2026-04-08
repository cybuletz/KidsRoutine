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
 * CODE CRACK GAME: Logic puzzles and pattern decoding.
 * Ages 13+: Pattern completion, Boolean logic, simple coding concepts.
 */
@Composable
fun CodeCrackGameBlock(
    ageGroup: AgeGroup = AgeGroup.TRAILBLAZER,
    onSuccess: () -> Unit
) {
    data class CodePuzzle(
        val prompt: String,
        val code: String,
        val correctAnswer: String,
        val options: List<String>
    )

    val puzzles = when (ageGroup) {
        AgeGroup.SPROUT, AgeGroup.EXPLORER -> listOf(
            CodePuzzle(
                prompt = "What comes next?",
                code = "🔴 → 🟢 → 🔵 → 🔴 → 🟢 → ?",
                correctAnswer = "🔵",
                options = listOf("🔵", "🔴", "🟡")
            ),
            CodePuzzle(
                prompt = "What's the rule?",
                code = "2 → 4 → 8 → 16 → ?",
                correctAnswer = "32",
                options = listOf("32", "24", "20")
            )
        )
        AgeGroup.TRAILBLAZER -> listOf(
            CodePuzzle(
                prompt = "What does this code print?",
                code = "x = 5\ny = x * 2\nprint(y)",
                correctAnswer = "10",
                options = listOf("10", "5", "7", "25")
            ),
            CodePuzzle(
                prompt = "What is TRUE?",
                code = "A = true, B = false\nA AND B = ?",
                correctAnswer = "false",
                options = listOf("false", "true", "error", "null")
            ),
            CodePuzzle(
                prompt = "What does this return?",
                code = "list = [3, 1, 4, 1, 5]\nlen(list)",
                correctAnswer = "5",
                options = listOf("5", "4", "3", "14")
            )
        )
        AgeGroup.LEGEND -> listOf(
            CodePuzzle(
                prompt = "What's the output?",
                code = "for i in range(3):\n  print(i)",
                correctAnswer = "0 1 2",
                options = listOf("0 1 2", "1 2 3", "0 1 2 3", "1 2")
            ),
            CodePuzzle(
                prompt = "What's the Big O?",
                code = "for i in list:\n  for j in list:\n    print(i, j)",
                correctAnswer = "O(n²)",
                options = listOf("O(n²)", "O(n)", "O(log n)", "O(2n)")
            ),
            CodePuzzle(
                prompt = "What does this evaluate to?",
                code = "(true OR false) AND (NOT false)",
                correctAnswer = "true",
                options = listOf("true", "false", "error", "undefined")
            )
        )
    }

    val selectedPuzzles = remember { puzzles.shuffled().take(3) }
    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var answered by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val current = selectedPuzzles.getOrNull(currentIndex)
    val shuffledOptions = remember(currentIndex) {
        current?.options?.shuffled() ?: emptyList()
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "💻 Code Crack",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF00BCD4)
        )

        Box(
            Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFF00BCD4).copy(alpha = 0.2f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Puzzle ${currentIndex + 1}/${selectedPuzzles.size}", fontWeight = FontWeight.Bold, color = Color(0xFF00BCD4))
        }

        if (current != null) {
            // Prompt
            Text(
                current.prompt,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            // Code block
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E2E))
                    .padding(20.dp)
            ) {
                Text(
                    current.code,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4FC1FF),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            // Options
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                shuffledOptions.forEach { option ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF00BCD4).copy(alpha = 0.1f))
                            .clickable(enabled = !answered) {
                                answered = true
                                if (option == current.correctAnswer) {
                                    SoundManager.playSuccess()
                                    score++
                                } else {
                                    SoundManager.playError()
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(option, fontWeight = FontWeight.SemiBold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                }
            }

            if (answered) {
                LaunchedEffect(Unit) {
                    delay(1200)
                    if (currentIndex < selectedPuzzles.size - 1) {
                        currentIndex++
                        answered = false
                    } else {
                        showSuccess = true
                        if (score >= 2) onSuccess()
                    }
                }
            }
        }

        AnimatedVisibility(visible = showSuccess, enter = scaleIn() + fadeIn()) {
            Text(
                if (score >= 2) "🎉 Code Cracker!" else "💻 Keep Debugging!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF00BCD4)
            )
        }
    }
}
