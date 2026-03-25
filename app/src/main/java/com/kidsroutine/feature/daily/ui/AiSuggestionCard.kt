package com.kidsroutine.feature.daily.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.generation.data.GeneratedTask

private val GeminiGradientStart = Color(0xFF4A90E2)
private val GeminiGradientEnd   = Color(0xFF9B59B6)
private val CardBg              = Color(0xFF1A1A2E)

// Maps GeneratedTask.category → a display emoji (no new data model needed)
private fun categoryEmoji(category: String) = when (category) {
    "MORNING_ROUTINE" -> "☀️"
    "HEALTH"          -> "💪"
    "LEARNING"        -> "📚"
    "CREATIVE"        -> "🎨"
    "CREATIVITY"      -> "🎨"
    "SOCIAL"          -> "👥"
    "EMOTIONAL"       -> "💛"
    "REAL_LIFE"       -> "🏠"
    "OUTDOOR"         -> "🌿"
    else              -> "✨"
}

@Composable
fun AiSuggestionCard(
    currentChild: UserModel,
    completedTaskTitles: List<String> = emptyList(),
    onAccept: (task: GeneratedTask) -> Unit = {},
    viewModel: AiSuggestionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(currentChild.userId) {
        viewModel.loadSuggestions(currentChild, completedTaskTitles)
    }

    AnimatedVisibility(
        visible = !state.dismissed,
        enter   = fadeIn() + slideInVertically { -it },
        exit    = fadeOut() + slideOutVertically { -it }
    ) {
        Card(
            modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

                // ── Header ────────────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Brush.linearGradient(listOf(GeminiGradientStart, GeminiGradientEnd)),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Gemini suggests", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                "For ${currentChild.displayName.split(" ").first()}",
                                fontSize = 10.sp, color = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (state.quotaRemaining >= 0) {
                            Text(
                                "${state.quotaRemaining} left",
                                fontSize = 10.sp,
                                color    = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                        IconButton(onClick = { viewModel.refresh(currentChild, completedTaskTitles) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Refresh, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { viewModel.dismiss() }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Body ─────────────────────────────────────────────────
                when {
                    state.isLoading -> {
                        Row(
                            modifier              = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GeminiGradientStart, strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Thinking...", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                    state.error != null -> {
                        Text("Couldn't load suggestions right now.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                    state.quotaRemaining == 0 -> {
                        Text("Daily quota reached. Upgrade for more! 🚀", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                    state.suggestions.isEmpty() -> {
                        Text("No suggestions yet — complete some tasks first!", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.suggestions.forEach { task ->
                                SuggestionChip(task = task, onClick = { onAccept(task) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(task: GeneratedTask, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = RoundedCornerShape(12.dp),
        color    = Color.White.copy(alpha = 0.07f)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(categoryEmoji(task.category), fontSize = 20.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(
                    "${task.difficulty} · ${task.estimatedDurationSec}s",
                    fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f), maxLines = 1
                )
            }
            Surface(shape = RoundedCornerShape(8.dp), color = GeminiGradientStart.copy(alpha = 0.2f)) {
                Text(
                    "+${task.xpReward} XP",
                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color    = GeminiGradientStart,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
