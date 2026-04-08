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
 * FACT OR FICTION GAME: Identify true vs false statements.
 * Builds critical thinking and news literacy.
 * Ages 13+: real-world facts, common misconceptions.
 */
@Composable
fun FactFictionGameBlock(
    ageGroup: AgeGroup = AgeGroup.TRAILBLAZER,
    onSuccess: () -> Unit
) {
    data class Statement(val text: String, val isFact: Boolean, val explanation: String)

    val statements = when (ageGroup) {
        AgeGroup.SPROUT, AgeGroup.EXPLORER -> listOf(
            Statement("The Sun is a star", true, "The Sun is indeed a star — the closest one to Earth!"),
            Statement("Fish can fly", false, "Most fish swim — only a few species can glide above water briefly."),
            Statement("Humans have 206 bones", true, "Adults have exactly 206 bones in their body."),
            Statement("The Moon is made of cheese", false, "The Moon is made of rock and dust, not cheese!"),
            Statement("Sharks are mammals", false, "Sharks are fish, not mammals.")
        )
        AgeGroup.TRAILBLAZER -> listOf(
            Statement("Lightning never strikes the same place twice", false, "Lightning frequently strikes tall structures repeatedly."),
            Statement("The Great Wall of China is visible from space", false, "It's too narrow to see with the naked eye from space."),
            Statement("Octopuses have three hearts", true, "Two pump blood to the gills, one to the rest of the body."),
            Statement("Humans use only 10% of their brains", false, "Brain scans show we use all parts of the brain."),
            Statement("Honey never expires", true, "Archaeologists found 3000-year-old honey that was still edible!"),
            Statement("Goldfish have a 3-second memory", false, "Goldfish can remember things for months.")
        )
        AgeGroup.LEGEND -> listOf(
            Statement("Correlation implies causation", false, "Correlation shows a relationship, not that one causes the other."),
            Statement("The economy always recovers from recessions", true, "Historically, economies have always eventually recovered."),
            Statement("You can copyright an idea", false, "Copyright protects expressions of ideas, not ideas themselves."),
            Statement("Antibiotics work against viruses", false, "Antibiotics only work against bacteria, not viruses."),
            Statement("A year on Venus is shorter than a day on Venus", true, "Venus rotates so slowly that its day is longer than its year!"),
            Statement("Compound interest is the 8th wonder of the world", false, "This quote is often attributed to Einstein, but there's no proof he said it.")
        )
    }

    val selectedStatements = remember { statements.shuffled().take(5) }
    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var answered by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val current = selectedStatements.getOrNull(currentIndex)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "🔍 Fact or Fiction?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF673AB7)
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF673AB7).copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("${currentIndex + 1}/${selectedStatements.size}", fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Score: $score", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            }
        }

        if (current != null) {
            // Statement card
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF673AB7).copy(alpha = 0.08f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "\"${current.text}\"",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF333333)
                )
            }

            // Fact / Fiction buttons
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // FACT button
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF4CAF50).copy(alpha = if (answered) 0.3f else 0.1f))
                        .clickable(enabled = !answered) {
                            answered = true
                            showExplanation = true
                            if (current.isFact) {
                                SoundManager.playSuccess()
                                score++
                            } else {
                                SoundManager.playError()
                            }
                        }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", fontSize = 32.sp)
                        Text("FACT", fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                    }
                }

                // FICTION button
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE53935).copy(alpha = if (answered) 0.3f else 0.1f))
                        .clickable(enabled = !answered) {
                            answered = true
                            showExplanation = true
                            if (!current.isFact) {
                                SoundManager.playSuccess()
                                score++
                            } else {
                                SoundManager.playError()
                            }
                        }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❌", fontSize = 32.sp)
                        Text("FICTION", fontWeight = FontWeight.ExtraBold, color = Color(0xFFE53935))
                    }
                }
            }

            // Explanation
            AnimatedVisibility(visible = showExplanation, enter = fadeIn() + expandVertically()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(16.dp)
                ) {
                    Text(
                        "💡 ${current.explanation}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF333333)
                    )
                }
            }

            if (answered) {
                LaunchedEffect(Unit) {
                    delay(2500)
                    if (currentIndex < selectedStatements.size - 1) {
                        currentIndex++
                        answered = false
                        showExplanation = false
                    } else {
                        showSuccess = true
                        if (score >= 3) onSuccess()
                    }
                }
            }
        }

        AnimatedVisibility(visible = showSuccess, enter = scaleIn() + fadeIn()) {
            Text(
                if (score >= 3) "🎉 Truth Seeker!" else "🔍 Keep Questioning!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF673AB7)
            )
        }
    }
}
