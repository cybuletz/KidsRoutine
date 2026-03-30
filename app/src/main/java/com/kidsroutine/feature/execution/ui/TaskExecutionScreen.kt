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
import com.kidsroutine.feature.lootbox.ui.LootBoxViewModel

@Composable
fun TaskExecutionScreen(
    task: TaskModel,
    instanceId: String,
    currentUser: UserModel,
    onBack: () -> Unit,
    onCompleted: (xpGained: Int) -> Unit,
    viewModel: ExecutionViewModel = hiltViewModel(),
    celebrationViewModel: CelebrationViewModel = hiltViewModel(),
    lootBoxViewModel: LootBoxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val taskColor = taskTypeColor(task.type)

    Log.d("TaskExecution", "Task: ${task.title}, GameType: ${task.gameType}")

    LaunchedEffect(task.id) {
        viewModel.loadTask(task, instanceId)   // ← WAS: loadTask(task)
        viewModel.setCurrentUser(currentUser)
    }

    LaunchedEffect(uiState.result) {
        val result = uiState.result
        if (result is CompletionResult.Success) {
            Log.d("TaskExecutionScreen", "Task completed! Result: $result")
            celebrationViewModel.showTaskCompletion()

            if (uiState.newBadgesUnlocked.isNotEmpty()) {
                Log.d("TaskExecutionScreen", "New badges unlocked: ${uiState.newBadgesUnlocked.size}")
                kotlinx.coroutines.delay(3500)
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
            // Replace the header Box entirely:
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(taskColor, taskColor.copy(alpha = 0.7f))))
                    .statusBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // ── Back button row — full width, correct touch target ──────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 8.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick  = onBack,
                            modifier = Modifier.size(48.dp)    // 48dp minimum touch target
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        // Task type chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = task.type.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // ── Title + icon row ────────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(taskTypeIcon(task.type), null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                task.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                                    Text("⭐ ${task.reward.xp} XP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                                    Text("⏱ ~${task.estimatedDurationSec}s", color = Color.White.copy(0.9f), fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                    if (task.requiresCoop) {
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) { CoopBadge() }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Description — BELOW the header, on white background ──────────────────
            if (task.description.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))   // add this spacer, or increase existing offset

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .offset(y = 4.dp),       // ← change from (-12).dp to 4.dp to push it further down
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = taskColor.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("📋", fontSize = 18.sp)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Quest Description",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = task.description,
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                color = Color(0xFF2D3436)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Column(
                modifier            = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (task.gameType != GameType.NONE) {
                    GameRenderer(
                        gameType       = task.gameType,
                        onGameComplete = { viewModel.onEvent(ExecutionEvent.SubmitTask) }
                    )
                } else {
                    val block = task.interactionBlocks.getOrNull(uiState.currentBlockIndex)
                    if (block != null) {
                        BlockProgressIndicator(
                            current = uiState.currentBlockIndex + 1,
                            total   = task.interactionBlocks.size,
                            color   = taskColor
                        )
                        InteractionBlockRenderer(block = block, onEvent = viewModel::onEvent)
                    } else {
                        SubmitSection(
                            isLoading = uiState.isCompleting,
                            taskColor = taskColor,
                            onSubmit  = { viewModel.onEvent(ExecutionEvent.SubmitTask) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }

        if (uiState.showSuccessAnim && uiState.result is CompletionResult.Success) {
            val result = uiState.result as CompletionResult.Success
            SuccessOverlay(
                xpGained           = result.xpGained,
                needsParent        = result.needsParent,
                celebrationMessage = uiState.celebrationMessage,
                taskColor          = taskColor,
                onDismiss          = { viewModel.onEvent(ExecutionEvent.DismissResult) }
            )
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
        Text(
            "All done? Tap to submit! 🎉",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
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
    celebrationMessage: String,
    taskColor: Color,
    onDismiss: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = spring(Spring.DampingRatioLowBouncy),
        label         = "success_scale"
    )
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier  = Modifier.fillMaxWidth(0.85f).scale(scale),
            shape     = RoundedCornerShape(28.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier            = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(if (needsParent) "⏳" else "🎉", fontSize = 72.sp)
                Text(
                    if (needsParent) "Waiting for Parent!" else "Task Complete!",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign  = TextAlign.Center
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
                if (celebrationMessage.isNotBlank()) {
                    Text(
                        text       = celebrationMessage,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color      = Color(0xFF444444),
                        textAlign  = TextAlign.Center
                    )
                } else {
                    Text(
                        if (needsParent) "Your parent will confirm this task soon." else "Amazing work! Keep it up!",
                        color     = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    onClick  = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = taskColor)
                ) {
                    Text("Back to Tasks", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
