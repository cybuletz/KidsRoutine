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
 * PATTERN MATCH GAME: Complete the pattern sequence.
 * Sprout: shapes/colors (🔴🔵🔴🔵?)
 * Explorer: numbers (2, 4, 6, ?)
 * Trailblazer+: complex sequences
 */
@Composable
fun PatternMatchGameBlock(
    ageGroup: AgeGroup = AgeGroup.SPROUT,
    onSuccess: () -> Unit
) {
    data class PatternQuestion(
        val sequence: List<String>,
        val answer: String,
        val options: List<String>
    )

    fun generatePattern(): PatternQuestion = when (ageGroup) {
        AgeGroup.SPROUT -> {
            val patterns = listOf(
                PatternQuestion(listOf("🔴", "🔵", "🔴", "🔵"), "🔴", listOf("🔴", "🟢", "🟡")),
                PatternQuestion(listOf("⭐", "🌙", "⭐", "🌙"), "⭐", listOf("⭐", "☀️", "🌈")),
                PatternQuestion(listOf("🐱", "🐶", "🐱", "🐶"), "🐱", listOf("🐱", "🐰", "🐻")),
                PatternQuestion(listOf("🟡", "🟡", "🔵", "🟡", "🟡"), "🔵", listOf("🔵", "🟡", "🔴"))
            )
            patterns.random()
        }
        AgeGroup.EXPLORER -> {
            val patterns = listOf(
                PatternQuestion(listOf("2", "4", "6", "8"), "10", listOf("10", "9", "12")),
                PatternQuestion(listOf("1", "3", "5", "7"), "9", listOf("9", "8", "11")),
                PatternQuestion(listOf("3", "6", "9", "12"), "15", listOf("15", "14", "18")),
                PatternQuestion(listOf("🔴", "🔵", "🟢", "🔴", "🔵"), "🟢", listOf("🟢", "🔴", "🟡"))
            )
            patterns.random()
        }
        AgeGroup.TRAILBLAZER, AgeGroup.LEGEND -> {
            val patterns = listOf(
                PatternQuestion(listOf("1", "1", "2", "3", "5"), "8", listOf("8", "7", "6")),
                PatternQuestion(listOf("2", "6", "18", "54"), "162", listOf("162", "108", "216")),
                PatternQuestion(listOf("1", "4", "9", "16"), "25", listOf("25", "20", "36")),
                PatternQuestion(listOf("A", "C", "E", "G"), "I", listOf("I", "H", "J"))
            )
            patterns.random()
        }
    }

    var pattern by remember { mutableStateOf(generatePattern()) }
    var score by remember { mutableStateOf(0) }
    var showSuccess by remember { mutableStateOf(false) }
    val targetScore = 3

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "🔷 Pattern Match",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF2196F3)
        )

        Box(
            Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFF2196F3).copy(alpha = 0.2f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Score: $score/$targetScore", fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
        }

        // Pattern display
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE3F2FD))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pattern.sequence.forEach { item ->
                    Text(item, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Text("?", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF6B35))
            }
        }

        // Options
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            pattern.options.shuffled().forEach { option ->
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2196F3).copy(alpha = 0.1f))
                        .clickable {
                            if (option == pattern.answer) {
                                SoundManager.playSuccess()
                                score++
                                if (score >= targetScore) {
                                    showSuccess = true
                                    onSuccess()
                                } else {
                                    pattern = generatePattern()
                                }
                            } else {
                                SoundManager.playError()
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(option, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
                }
            }
        }

        AnimatedVisibility(visible = showSuccess, enter = scaleIn() + fadeIn()) {
            Text(
                "🎉 Pattern Pro!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2196F3)
            )
        }
    }
}
