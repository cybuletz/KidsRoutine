package com.kidsroutine.feature.community.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.kidsroutine.core.model.*

private val GradientStart = Color(0xFFFF6B35)
private val GradientEnd = Color(0xFFFF9800)
private val BgLight = Color(0xFFFFFBF0)
private val TextDark = Color(0xFF2D3436)

@Composable
fun ModerationScreen(
    onBackClick: () -> Unit,
    viewModel: ModerationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadModeration()
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(1500)
            viewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.25f)
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "🛡️ Moderation Panel",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(40.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Tab selector
            TabRow(
                selectedTabIndex = uiState.activeTab.ordinal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                containerColor = Color.Transparent,
                divider = {}
            ) {
                ModerationTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.activeTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = when (tab) {
                                    ModerationTab.PENDING_TASKS -> "📋 Tasks (${uiState.pendingTasks.size})"
                                    ModerationTab.PENDING_CHALLENGES -> "🎯 Challenges (${uiState.pendingChallenges.size})"
                                    ModerationTab.REPORTS -> "🚩 Reports (${uiState.reports.size})"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error!!, color = Color.Red)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // List panel
                    when (uiState.activeTab) {
                        ModerationTab.PENDING_TASKS -> {
                            PendingTasksList(
                                tasks = uiState.pendingTasks,
                                selected = uiState.selectedTask,
                                onSelect = { viewModel.selectTask(it) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        ModerationTab.PENDING_CHALLENGES -> {
                            PendingChallengesList(
                                challenges = uiState.pendingChallenges,
                                selected = uiState.selectedChallenge,
                                onSelect = { viewModel.selectChallenge(it) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        ModerationTab.REPORTS -> {
                            ReportsList(
                                reports = uiState.reports,
                                selected = uiState.selectedReport,
                                onSelect = { viewModel.selectReport(it) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // Detail panel
                    when (uiState.activeTab) {
                        ModerationTab.PENDING_TASKS -> {
                            if (uiState.selectedTask != null) {
                                TaskDetailPanel(
                                    task = uiState.selectedTask!!,
                                    onApprove = { viewModel.approveTask(uiState.selectedTask!!.taskId) },
                                    onReject = { reason -> viewModel.rejectTask(uiState.selectedTask!!.taskId, reason) },
                                    isProcessing = uiState.isProcessing,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White, RoundedCornerShape(16.dp))
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Select a task to review", color = Color.Gray)
                                }
                            }
                        }
                        ModerationTab.PENDING_CHALLENGES -> {
                            if (uiState.selectedChallenge != null) {
                                ChallengeDetailPanel(
                                    challenge = uiState.selectedChallenge!!,
                                    onApprove = { viewModel.approveChallenge(uiState.selectedChallenge!!.challengeId) },
                                    onReject = { reason -> viewModel.rejectChallenge(uiState.selectedChallenge!!.challengeId, reason) },
                                    isProcessing = uiState.isProcessing,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White, RoundedCornerShape(16.dp))
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Select a challenge to review", color = Color.Gray)
                                }
                            }
                        }
                        ModerationTab.REPORTS -> {
                            if (uiState.selectedReport != null) {
                                ReportDetailPanel(
                                    report = uiState.selectedReport!!,
                                    onResolve = { action -> viewModel.resolveReport(uiState.selectedReport!!.reportId, action) },
                                    isProcessing = uiState.isProcessing,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White, RoundedCornerShape(16.dp))
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Select a report to review", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Success message
        AnimatedVisibility(visible = uiState.successMessage != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE8F5E9),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(
                    text = uiState.successMessage ?: "",
                    color = Color(0xFF2E7D32),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PendingTasksList(
    tasks: List<SharedTask>,
    selected: SharedTask?,
    onSelect: (SharedTask) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (tasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✅ All tasks approved!", color = Color.Gray)
                }
            }
        } else {
            items(tasks) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (selected?.taskId == task.taskId) {
                                Modifier.background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                            } else {
                                Modifier
                            }
                        ),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected?.taskId == task.taskId) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            maxLines = 1
                        )
                        Text(
                            text = "By: ${task.creatorName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFFE082).copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = task.difficulty.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFF6F00),
                                    modifier = Modifier.padding(2.dp, 1.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = "+${task.reward.xp} XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(2.dp, 1.dp)
                                )
                            }
                        }
                        Button(
                            onClick = { onSelect(task) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected?.taskId == task.taskId) GradientStart else Color(0xFFCCCCCC)
                            )
                        ) {
                            Text(
                                if (selected?.taskId == task.taskId) "Review" else "Select",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingChallengesList(
    challenges: List<SharedChallenge>,
    selected: SharedChallenge?,
    onSelect: (SharedChallenge) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (challenges.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✅ All challenges approved!", color = Color.Gray)
                }
            }
        } else {
            items(challenges) { challenge ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (selected?.challengeId == challenge.challengeId) {
                                Modifier.background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                            } else {
                                Modifier
                            }
                        ),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected?.challengeId == challenge.challengeId) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = challenge.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            maxLines = 1
                        )
                        Text(
                            text = "By: ${challenge.creatorName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFFE082).copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = "${challenge.duration}d",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFF6F00),
                                    modifier = Modifier.padding(2.dp, 1.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = "+${challenge.dailyXpReward} XP/day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(2.dp, 1.dp)
                                )
                            }
                        }
                        Button(
                            onClick = { onSelect(challenge) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected?.challengeId == challenge.challengeId) GradientStart else Color(0xFFCCCCCC)
                            )
                        ) {
                            Text(
                                if (selected?.challengeId == challenge.challengeId) "Review" else "Select",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsList(
    reports: List<ContentReport>,
    selected: ContentReport?,
    onSelect: (ContentReport) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (reports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✅ No pending reports", color = Color.Gray)
                }
            }
        } else {
            items(reports) { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (selected?.reportId == report.reportId) {
                                Modifier.background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                            } else {
                                Modifier
                            }
                        ),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected?.reportId == report.reportId) Color(0xFFFFEBEE) else Color(0xFFF5F5F5)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🚩 ${report.reason.name}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828),
                            maxLines = 1
                        )
                        Text(
                            text = "Content: ${report.contentType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = report.description.take(50) + if (report.description.length > 50) "..." else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            maxLines = 1
                        )
                        Button(
                            onClick = { onSelect(report) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected?.reportId == report.reportId) Color(0xFFC62828) else Color(0xFFCCCCCC)
                            )
                        ) {
                            Text(
                                "Review",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskDetailPanel(
    task: SharedTask,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    var rejectReason by remember { mutableStateOf("") }
    var showRejectInput by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Task Review",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Title",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDark
                )

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.paddingFromBaseline(top = 16.dp)
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDark
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .paddingFromBaseline(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = "📊 ${task.difficulty.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1565C0),
                            modifier = Modifier.padding(8.dp, 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = "⭐ ${task.reward.xp} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(8.dp, 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3E5F5)
                    ) {
                        Text(
                            text = "⏱️ ${task.estimatedDurationSec}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6A1B9A),
                            modifier = Modifier.padding(8.dp, 4.dp)
                        )
                    }
                }

                Text(
                    text = "Creator: ${task.creatorName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.paddingFromBaseline(top = 16.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            if (showRejectInput) {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text("Rejection reason...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    enabled = !isProcessing
                ) {
                    Text("✅ Approve", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        if (showRejectInput) {
                            onReject(rejectReason)
                            showRejectInput = false
                            rejectReason = ""
                        } else {
                            showRejectInput = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    enabled = !isProcessing
                ) {
                    Text(if (showRejectInput) "Reject" else "❌ Reject", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ChallengeDetailPanel(
    challenge: SharedChallenge,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    var rejectReason by remember { mutableStateOf("") }
    var showRejectInput by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Challenge Review",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Title",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDark
                )

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.paddingFromBaseline(top = 16.dp)
                )
                Text(
                    text = challenge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDark
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .paddingFromBaseline(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = "📅 ${challenge.duration}d",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1565C0),
                            modifier = Modifier.padding(6.dp, 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = "⭐ ${challenge.dailyXpReward}/d",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(6.dp, 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3E5F5)
                    ) {
                        Text(
                            text = "🎁 +${challenge.completionBonusXp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6A1B9A),
                            modifier = Modifier.padding(6.dp, 3.dp)
                        )
                    }
                }

                Text(
                    text = "Creator: ${challenge.creatorName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.paddingFromBaseline(top = 16.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            if (showRejectInput) {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text("Rejection reason...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    enabled = !isProcessing
                ) {
                    Text("✅ Approve", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        if (showRejectInput) {
                            onReject(rejectReason)
                            showRejectInput = false
                            rejectReason = ""
                        } else {
                            showRejectInput = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    enabled = !isProcessing
                ) {
                    Text(if (showRejectInput) "Reject" else "❌ Reject", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ReportDetailPanel(
    report: ContentReport,
    onResolve: (String) -> Unit,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Report Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Reason",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    text = "🚩 ${report.reason.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.paddingFromBaseline(top = 16.dp)
                )
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDark
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .paddingFromBaseline(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = "Content: ${report.contentType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1565C0),
                            modifier = Modifier.padding(8.dp, 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = "Status: ${report.status}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(8.dp, 4.dp)
                        )
                    }
                }

                Text(
                    text = "Reported by: ${report.reportedBy.take(8)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.paddingFromBaseline(top = 16.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onResolve("APPROVED") },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    enabled = !isProcessing
                ) {
                    Text("❌ Delete Content", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = { onResolve("REJECTED") },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    enabled = !isProcessing
                ) {
                    Text("✅ Not a Violation", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }2
        }
    }
}