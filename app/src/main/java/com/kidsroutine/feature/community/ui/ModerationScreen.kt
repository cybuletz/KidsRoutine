package com.kidsroutine.feature.community.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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
                when (uiState.activeTab) {
                    ModerationTab.PENDING_TASKS -> {
                        PendingTasksList(
                            tasks = uiState.pendingTasks,
                            selected = uiState.selectedTask,
                            onSelect = { viewModel.selectTask(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp)
                        )
                    }
                    ModerationTab.PENDING_CHALLENGES -> {
                        PendingChallengesList(
                            challenges = uiState.pendingChallenges,
                            selected = uiState.selectedChallenge,
                            onSelect = { viewModel.selectChallenge(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp)
                        )
                    }
                    ModerationTab.REPORTS -> {
                        ReportsList(
                            reports = uiState.reports,
                            selected = uiState.selectedReport,
                            onSelect = { viewModel.selectReport(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }

        // Task Detail Modal
        if (uiState.selectedTask != null) {
            TaskDetailModal(
                task = uiState.selectedTask!!,
                onApprove = { viewModel.approveTask(uiState.selectedTask!!.taskId) },
                onReject = { reason -> viewModel.rejectTask(uiState.selectedTask!!.taskId, reason) },
                onDismiss = { viewModel.clearSelection() },
                isProcessing = uiState.isProcessing
            )
        }

        // Challenge Detail Modal
        if (uiState.selectedChallenge != null) {
            ChallengeDetailModal(
                challenge = uiState.selectedChallenge!!,
                onApprove = { viewModel.approveChallenge(uiState.selectedChallenge!!.challengeId) },
                onReject = { reason -> viewModel.rejectChallenge(uiState.selectedChallenge!!.challengeId, reason) },
                onDismiss = { viewModel.clearSelection() },
                isProcessing = uiState.isProcessing
            )
        }

        // Report Detail Modal
        if (uiState.selectedReport != null) {
            ReportDetailModal(
                report = uiState.selectedReport!!,
                onResolve = { action -> viewModel.resolveReport(uiState.selectedReport!!.reportId, action) },
                onDismiss = { viewModel.clearSelection() },
                isProcessing = uiState.isProcessing
            )
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
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
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

// ===== MODAL DIALOGS =====

@Composable
private fun TaskDetailModal(
    task: SharedTask,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    onDismiss: () -> Unit,
    isProcessing: Boolean
) {
    var rejectReason by remember { mutableStateOf("") }
    var showRejectInput by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Task Review",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDark,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE3F2FD)
                            ) {
                                Text(
                                    text = "📊 ${task.difficulty.name}",
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
                                    text = "⭐ ${task.reward.xp} XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(6.dp, 3.dp)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Creator: ${task.creatorName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                if (showRejectInput) {
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Rejection reason...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        enabled = !isProcessing
                    ) {
                        Text("✅ Approve", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
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
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                        enabled = !isProcessing
                    ) {
                        Text(if (showRejectInput) "Reject" else "❌ Reject", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeDetailModal(
    challenge: SharedChallenge,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    onDismiss: () -> Unit,
    isProcessing: Boolean
) {
    var rejectReason by remember { mutableStateOf("") }
    var showRejectInput by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Challenge Review",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = challenge.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDark,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                    }

                    item {
                        Text(
                            text = "Creator: ${challenge.creatorName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                if (showRejectInput) {
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Rejection reason...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        enabled = !isProcessing
                    ) {
                        Text("✅ Approve", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
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
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                        enabled = !isProcessing
                    ) {
                        Text(if (showRejectInput) "Reject" else "❌ Reject", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportDetailModal(
    report: ContentReport,
    onResolve: (String) -> Unit,
    onDismiss: () -> Unit,
    isProcessing: Boolean
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Report Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = report.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDark,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE3F2FD)
                            ) {
                                Text(
                                    text = "Content: ${report.contentType}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF1565C0),
                                    modifier = Modifier.padding(6.dp, 3.dp)
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
                                    modifier = Modifier.padding(6.dp, 3.dp)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Reported by: ${report.reportedBy.take(8)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onResolve("APPROVED") },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        enabled = !isProcessing
                    ) {
                        Text("✅ Valid", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onResolve("REMOVED") },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                        enabled = !isProcessing
                    ) {
                        Text("❌ Delete", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}