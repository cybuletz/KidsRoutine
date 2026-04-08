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
 * DEBATE PROMPT GAME: Ethical scenario response with structured argument building.
 * Features:
 * - SPROUT: Simple "what would you do?" with picture options
 * - EXPLORER: Pick a side and select supporting reasons
 * - TRAILBLAZER: Build arguments from strong/weak reason options
 * - LEGEND: Free-text response + confidence self-rating
 * - Animated transitions between scenario and response phases
 */
@Composable
fun DebatePromptGameBlock(
    ageGroup: AgeGroup = AgeGroup.EXPLORER,
    onSuccess: () -> Unit
) {
    when (ageGroup) {
        AgeGroup.SPROUT -> SproutDebate(onSuccess)
        AgeGroup.EXPLORER -> ExplorerDebate(onSuccess)
        AgeGroup.TRAILBLAZER -> TrailblazerDebate(onSuccess)
        AgeGroup.LEGEND -> LegendDebate(onSuccess)
    }
}

// ── SPROUT: Simple scenario with picture options ──

@Composable
private fun SproutDebate(onSuccess: () -> Unit) {
    data class Scenario(val question: String, val options: List<Pair<String, String>>)

    val scenarios = remember {
        listOf(
            Scenario(
                "Your friend is sad because they dropped their ice cream. What do you do?",
                listOf("🤗 Give a hug" to "hug", "🍦 Share yours" to "share", "😊 Cheer them up" to "cheer")
            ),
            Scenario(
                "You found a toy that isn't yours on the playground. What do you do?",
                listOf("🔍 Find the owner" to "find", "👨‍🏫 Tell a teacher" to "tell", "📦 Put in lost & found" to "lost")
            ),
            Scenario(
                "A new kid at school has no one to play with. What do you do?",
                listOf("👋 Say hello" to "hello", "🎮 Invite to play" to "play", "😄 Be their friend" to "friend")
            )
        )
    }

    var scenario by remember { mutableStateOf(scenarios.random()) }
    var selected by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(selected) {
        if (selected) {
            delay(1500)
            showSuccess = true
            SoundManager.playSuccess()
            delay(1200)
            onSuccess()
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
            "💭 What Would You Do?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFE84393)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFCE4EC))
                .padding(20.dp)
        ) {
            Text(
                scenario.question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        scenario.options.forEach { (label, _) ->
            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "sprout_option_scale"
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE84393).copy(alpha = 0.1f))
                    .clickable(enabled = !selected) {
                        SoundManager.playTap()
                        selected = true
                    }
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE84393)
                )
            }
        }

        AnimatedVisibility(
            visible = showSuccess,
            enter = scaleIn(tween(500)) + fadeIn(),
            exit = fadeOut()
        ) {
            SuccessBanner("🎉 Great choice! You're kind!")
        }
    }
}

// ── EXPLORER: Pick a side and select reasons ──

@Composable
private fun ExplorerDebate(onSuccess: () -> Unit) {
    data class Dilemma(
        val question: String,
        val sideA: String,
        val sideB: String,
        val reasonsA: List<String>,
        val reasonsB: List<String>
    )

    val dilemmas = remember {
        listOf(
            Dilemma(
                "Should students wear school uniforms?",
                "Yes, uniforms are good",
                "No, students should choose",
                listOf("Everyone looks equal", "Less morning decisions", "Shows school pride"),
                listOf("Self-expression matters", "Comfort is important", "Saves money for families")
            ),
            Dilemma(
                "Should homework be optional?",
                "Yes, make it optional",
                "No, keep homework",
                listOf("More free time to play", "Less stress for students", "Learning happens in school"),
                listOf("Practice makes perfect", "Builds responsibility", "Parents see progress")
            )
        )
    }

    var dilemma by remember { mutableStateOf(dilemmas.random()) }
    var chosenSide by remember { mutableStateOf<String?>(null) }
    var selectedReasons by remember { mutableStateOf<Set<String>>(emptySet()) }
    var submitted by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val currentReasons = when (chosenSide) {
        dilemma.sideA -> dilemma.reasonsA
        dilemma.sideB -> dilemma.reasonsB
        else -> emptyList()
    }

    LaunchedEffect(submitted) {
        if (submitted) {
            showSuccess = true
            SoundManager.playSuccess()
            delay(1500)
            onSuccess()
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "⚖️ Pick a Side",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF6C5CE7)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFEDE7F6))
                .padding(16.dp)
        ) {
            Text(
                dilemma.question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (chosenSide == null) {
            Text("Choose your side:", fontWeight = FontWeight.Bold, color = Color.Gray)
            listOf(dilemma.sideA, dilemma.sideB).forEach { side ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF6C5CE7).copy(alpha = 0.1f))
                        .clickable {
                            SoundManager.playTap()
                            chosenSide = side
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(side, fontWeight = FontWeight.Bold, color = Color(0xFF6C5CE7))
                }
            }
        } else {
            Text(
                "Your side: $chosenSide",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6C5CE7)
            )

            Text("Select your reasons (pick at least 2):", color = Color.Gray)

            currentReasons.forEach { reason ->
                val isSelected = reason in selectedReasons
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color(0xFF6C5CE7) else Color(0xFF6C5CE7).copy(alpha = 0.08f)
                        )
                        .clickable(enabled = !submitted) {
                            SoundManager.playTap()
                            selectedReasons = if (isSelected) selectedReasons - reason
                            else selectedReasons + reason
                        }
                        .padding(14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        "${if (isSelected) "✓ " else ""}$reason",
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF6C5CE7)
                    )
                }
            }

            if (selectedReasons.size >= 2 && !submitted) {
                Button(
                    onClick = {
                        SoundManager.playTap()
                        submitted = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit Argument", fontWeight = FontWeight.Bold)
                }
            }
        }

        AnimatedVisibility(
            visible = showSuccess,
            enter = scaleIn(tween(500)) + fadeIn(),
            exit = fadeOut()
        ) {
            SuccessBanner("🎉 Great argument!")
        }
    }
}

// ── TRAILBLAZER: Build argument with strong/weak reasons ──

@Composable
private fun TrailblazerDebate(onSuccess: () -> Unit) {
    data class EthicalScenario(
        val prompt: String,
        val strongReasons: List<String>,
        val weakReasons: List<String>
    )

    val scenarios = remember {
        listOf(
            EthicalScenario(
                "Should AI be used to grade student essays?",
                strongReasons = listOf(
                    "AI can provide instant, consistent feedback",
                    "Frees teachers to focus on personal mentoring",
                    "Reduces unconscious grading bias"
                ),
                weakReasons = listOf(
                    "AI is popular right now",
                    "It would save paper",
                    "Students wouldn't need to write neatly"
                )
            ),
            EthicalScenario(
                "Should cities ban cars from downtown areas?",
                strongReasons = listOf(
                    "Reduces air pollution and improves health",
                    "Makes streets safer for pedestrians and cyclists",
                    "Encourages public transport use and reduces congestion"
                ),
                weakReasons = listOf(
                    "It sounds like a cool idea",
                    "People could walk more to get exercise",
                    "Car horns are annoying"
                )
            )
        )
    }

    var scenario by remember { mutableStateOf(scenarios.random()) }
    val allReasons = remember(scenario) { (scenario.strongReasons + scenario.weakReasons).shuffled() }
    var selectedReasons by remember { mutableStateOf<Set<String>>(emptySet()) }
    var submitted by remember { mutableStateOf(false) }
    var isGoodArgument by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(submitted) {
        if (submitted && isGoodArgument) {
            showSuccess = true
            SoundManager.playSuccess()
            delay(1500)
            onSuccess()
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "🏗️ Argument Builder",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFE17055)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFF3E0))
                .padding(16.dp)
        ) {
            Text(
                scenario.prompt,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Text(
            "Pick 3 strongest reasons to support your argument:",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        allReasons.forEachIndexed { idx, reason ->
            val isSelected = reason in selectedReasons
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            submitted && isSelected && reason in scenario.strongReasons -> Color(0xFF90EE90)
                            submitted && isSelected && reason in scenario.weakReasons -> Color(0xFFFFCCCC)
                            isSelected -> Color(0xFFE17055)
                            else -> Color(0xFFE17055).copy(alpha = 0.08f)
                        }
                    )
                    .clickable(enabled = !submitted) {
                        SoundManager.playTap()
                        selectedReasons = if (isSelected) {
                            selectedReasons - reason
                        } else if (selectedReasons.size < 3) {
                            selectedReasons + reason
                        } else {
                            selectedReasons
                        }
                    }
                    .padding(14.dp)
            ) {
                Text(
                    "${idx + 1}. $reason",
                    fontWeight = FontWeight.Bold,
                    color = when {
                        submitted && isSelected && reason in scenario.strongReasons -> Color(0xFF2E7D32)
                        submitted && isSelected -> Color(0xFFC62828)
                        isSelected -> Color.White
                        else -> Color(0xFFE17055)
                    },
                    fontSize = 14.sp
                )
            }
        }

        if (selectedReasons.size == 3 && !submitted) {
            Button(
                onClick = {
                    SoundManager.playTap()
                    val strongCount = selectedReasons.count { it in scenario.strongReasons }
                    isGoodArgument = strongCount >= 2
                    submitted = true
                    if (!isGoodArgument) SoundManager.playError()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE17055)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Submit Argument", fontWeight = FontWeight.Bold)
            }
        }

        if (submitted && !isGoodArgument) {
            Text(
                "💡 Try picking stronger reasons. Strong reasons use evidence and logic!",
                color = Color(0xFFC62828),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = {
                    submitted = false
                    selectedReasons = emptySet()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE17055)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Try Again", fontWeight = FontWeight.Bold)
            }
        }

        AnimatedVisibility(
            visible = showSuccess,
            enter = scaleIn(tween(500)) + fadeIn(),
            exit = fadeOut()
        ) {
            SuccessBanner("🎉 Strong argument! Well reasoned!")
        }
    }
}

// ── LEGEND: Free-text response + confidence rating ──

@Composable
private fun LegendDebate(onSuccess: () -> Unit) {
    val dilemmas = remember {
        listOf(
            "A self-driving car must choose between hitting one person or swerving into a group of three. Who should program this decision, and how?",
            "A company creates AI that can detect lies with 95% accuracy. Should courts be allowed to use it as evidence?",
            "A social media platform discovers that its algorithm promotes divisive content because it increases engagement. Should they change the algorithm even if it reduces revenue?"
        )
    }

    var dilemma by remember { mutableStateOf(dilemmas.random()) }
    var response by remember { mutableStateOf("") }
    var confidence by remember { mutableIntStateOf(3) }
    var submitted by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(submitted) {
        if (submitted) {
            showSuccess = true
            SoundManager.playSuccess()
            delay(1500)
            onSuccess()
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "🧠 Ethical Dilemma",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF2D3436)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            Text(
                dilemma,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        }

        Text("Write your response:", fontWeight = FontWeight.Bold, color = Color.Gray)

        OutlinedTextField(
            value = response,
            onValueChange = { response = it },
            label = { Text("Your argument...") },
            minLines = 4,
            maxLines = 6,
            enabled = !submitted,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2D3436),
                cursorColor = Color(0xFF2D3436)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Confidence rating
        Text("How confident are you? (1-5)", fontWeight = FontWeight.Bold, color = Color.Gray)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (1..5).forEach { rating ->
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (rating == confidence) Color(0xFF2D3436) else Color(0xFFE0E0E0)
                        )
                        .clickable(enabled = !submitted) {
                            SoundManager.playTap()
                            confidence = rating
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        rating.toString(),
                        fontWeight = FontWeight.Bold,
                        color = if (rating == confidence) Color.White else Color(0xFF333333)
                    )
                }
            }
        }

        if (response.length >= 20 && !submitted) {
            Button(
                onClick = {
                    SoundManager.playTap()
                    submitted = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3436)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Submit Response", fontWeight = FontWeight.Bold)
            }
        } else if (!submitted) {
            Text(
                "Write at least 20 characters to submit",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
        }

        AnimatedVisibility(
            visible = showSuccess,
            enter = scaleIn(tween(500)) + fadeIn(),
            exit = fadeOut()
        ) {
            SuccessBanner("🎉 Thoughtful response! (Confidence: $confidence/5)")
        }
    }
}

// ── Shared success banner ──

@Composable
private fun SuccessBanner(message: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF90EE90))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            message,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF2E7D32),
            textAlign = TextAlign.Center
        )
    }
}
