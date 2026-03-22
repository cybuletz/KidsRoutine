package com.kidsroutine.feature.execution.ui

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.*
import com.kidsroutine.feature.daily.ui.taskTypeColor
import com.kidsroutine.feature.daily.ui.taskTypeIcon
import com.kidsroutine.feature.execution.domain.CompletionResult
import com.kidsroutine.feature.execution.ui.blocks.InteractionBlockRenderer
import com.kidsroutine.feature.celebrations.ui.CelebrationViewModel
import com.kidsroutine.core.model.GameType


@Composable
fun TaskExecutionScreen(
    task: TaskModel,
    onBack: () -> Unit,
    onCompleted: (xpGained: Int) -> Unit,
    viewModel: ExecutionViewModel = hiltViewModel(),
    celebrationViewModel: CelebrationViewModel = hiltViewModel()  // ← ADD THIS
) {
    val uiState by viewModel.uiState.collectAsState()
    val taskColor = taskTypeColor(task.type)

    Log.d("TaskExecution", "Task: ${task.title}, GameType: ${task.gameType}")

    LaunchedEffect(task.id) { viewModel.loadTask(task) }

    // Handle completion navigation
    LaunchedEffect(uiState.result) {
        val result = uiState.result
        if (result is CompletionResult.Success) {
            Log.d("TaskExecutionScreen", "Task completed! Result: $result")
            celebrationViewModel.showTaskCompletion()

            // ← NEW: Check if badges were unlocked
            if (uiState.newBadgesUnlocked.isNotEmpty()) {
                Log.d("TaskExecutionScreen", "New badges unlocked: ${uiState.newBadgesUnlocked.size}")
                // Show first badge after task celebration completes
                kotlinx.coroutines.delay(3500)  // Wait for task celebration
                val badge = uiState.newBadgesUnlocked.first()
                celebrationViewModel.showAchievementUnlock(badge.title)
                kotlinx.coroutines.delay(500)
            } else {
                kotlinx.coroutines.delay(500)
            }

            onCompleted(result.xpGained)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFBF0))
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(taskColor, taskColor.copy(alpha = 0.6f))))
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Row(
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(taskTypeIcon(task.type), null, tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                        Column {
                            Text(task.title, style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("⭐ ${task.reward.xp} XP  ·  ~${task.estimatedDurationSec}s",
                                color = Color.White.copy(0.85f), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (task.requiresCoop) CoopBadge()
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Interaction Blocks or Game ──────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Check if task has a game instead of interaction blocks
                if (task.gameType != GameType.NONE) {
                    // Render the game
                    GameRenderer(
                        gameType = task.gameType,
                        onGameComplete = { viewModel.onEvent(ExecutionEvent.SubmitTask) }
                    )
                } else {
                    // Render interaction blocks
                    val block = task.interactionBlocks.getOrNull(uiState.currentBlockIndex)
                    if (block != null) {
                        BlockProgressIndicator(
                            current = uiState.currentBlockIndex + 1,
                            total   = task.interactionBlocks.size,
                            color   = taskColor
                        )
                        InteractionBlockRenderer(block = block, onEvent = viewModel::onEvent)
                    } else {
                        // All blocks answered — show submit
                        SubmitSection(
                            isLoading   = uiState.isCompleting,
                            taskColor   = taskColor,
                            onSubmit    = { viewModel.onEvent(ExecutionEvent.SubmitTask) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }


    }
}

@Composable
private fun BlockProgressIndicator(current: Int, total: Int, color: Color) {
    if (total <= 1) return
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (i < current) color else color.copy(alpha = 0.2f))
            )
        }
    }
}

@Composable
private fun CoopBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(0.25f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text("🤝 CO-OP TASK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
private fun SubmitSection(isLoading: Boolean, taskColor: Color, onSubmit: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("All done? Tap to submit! 🎉",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center)
        Button(
            onClick  = onSubmit,
            enabled  = !isLoading,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape    = RoundedCornerShape(18.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = taskColor)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
            } else {
                Text("Complete Task ✓", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun SuccessOverlay(
    xpGained: Int,
    needsParent: Boolean,
    taskColor: Color,
    onDismiss: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = spring(Spring.DampingRatioLowBouncy),
        label         = "success_scale"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier  = Modifier
                .fillMaxWidth(0.85f)
                .scale(scale),
            shape     = RoundedCornerShape(28.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(if (needsParent) "⏳" else "🎉", fontSize = 72.sp)
                Text(
                    if (needsParent) "Waiting for Parent!" else "Task Complete!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                if (!needsParent) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(taskColor.copy(alpha = 0.15f))
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text("+$xpGained XP ⭐", color = taskColor, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    }
                }
                Text(
                    if (needsParent) "Your parent will confirm this task soon." else "Amazing work! Keep it up!",
                    color = Color.Gray, textAlign = TextAlign.Center
                )
                Button(
                    onClick  = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = taskColor)
                ) { Text("Back to Tasks", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }
        }
    }
}
