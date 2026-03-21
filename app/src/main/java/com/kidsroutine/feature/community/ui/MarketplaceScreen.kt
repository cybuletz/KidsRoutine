package com.kidsroutine.feature.community.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton

private val GradientStart = Color(0xFF667EEA)
private val GradientEnd = Color(0xFF764BA2)
private val BgLight = Color(0xFFFFFBF0)
private val TextDark = Color(0xFF2D3436)

@Composable
fun MarketplaceScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMarketplace()
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
                .statusBarsPadding()  // ← ADD THIS

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
                    text = "📦 Community Library",
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
                MarketplaceTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.activeTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = when (tab) {
                                    MarketplaceTab.TASKS -> "📋 Tasks"
                                    MarketplaceTab.CHALLENGES -> "🎯 Challenges"
                                },
                                fontWeight = FontWeight.Bold
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            uiState.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDark
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                ) {
                    // Filters
                    Text(
                        text = "Filter",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedDifficulty == null,
                                onClick = { viewModel.filterByDifficulty(null) },
                                label = { Text("All Levels") }
                            )
                        }
                        items(DifficultyLevel.entries) { difficulty ->
                            FilterChip(
                                selected = uiState.selectedDifficulty == difficulty,
                                onClick = { viewModel.filterByDifficulty(difficulty) },
                                label = { Text(difficulty.name) }
                            )
                        }
                    }

                    // Content based on tab
                    when (uiState.activeTab) {
                        MarketplaceTab.TASKS -> {
                            TasksContent(
                                tasks = uiState.tasks,
                                currentUser = currentUser,  // ADD THIS
                                onImport = { task -> viewModel.importTask(currentUser.userId, task.taskId) },
                                isImporting = uiState.isImporting,
                                viewModel = viewModel  // ADD THIS
                            )
                        }
                        MarketplaceTab.CHALLENGES -> {
                            ChallengesContent(
                                challenges = uiState.challenges,
                                currentUser = currentUser,  // ADD THIS
                                onImport = { challenge -> viewModel.importChallenge(currentUser.userId, challenge.challengeId) },
                                isImporting = uiState.isImporting,
                                viewModel = viewModel  // ADD THIS
                            )
                        }
                    }
                }
            }
        }

        // Success message
        AnimatedVisibility(
            visible = uiState.successMessage != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Text(
                    text = uiState.successMessage ?: "",
                    color = Color(0xFF2E7D32),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TasksContent(
    tasks: List<SharedTask>,
    currentUser: UserModel,
    onImport: (SharedTask) -> Unit,
    isImporting: Boolean,
    viewModel: MarketplaceViewModel
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📋", fontSize = 40.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "No tasks found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks) { task ->
                TaskMarketplaceCard(
                    task = task,
                    currentUser = currentUser,  // ADD THIS
                    onImport = { onImport(task) },
                    isImporting = isImporting,
                    viewModel = viewModel  // ADD THIS
                )
            }
        }
    }
}

@Composable
private fun ChallengesContent(
    challenges: List<SharedChallenge>,
    currentUser: UserModel,
    onImport: (SharedChallenge) -> Unit,
    isImporting: Boolean,
    viewModel: MarketplaceViewModel
) {
    if (challenges.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎯", fontSize = 40.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "No challenges found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(challenges) { challenge ->
                ChallengeMarketplaceCard(
                    challenge = challenge,
                    onImport = { onImport(challenge) },
                    isImporting = isImporting
                )
            }
        }
    }
}

@Composable
private fun TaskMarketplaceCard(
    task: SharedTask,
    currentUser: UserModel,
    onImport: () -> Unit,
    isImporting: Boolean,
    viewModel: MarketplaceViewModel
) {
    var showRatingDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    if (showRatingDialog) {
        RatingDialog(
            contentId = task.taskId,
            contentType = "task",
            onRate = { rating, review ->
                // Submit rating
                val userRating = UserRating(
                    userId = currentUser.userId,
                    contentId = task.taskId,
                    contentType = "task",
                    rating = rating,
                    review = review
                )
                // TODO: Call viewModel to submit rating
                showRatingDialog = false
            },
            onDismiss = { showRatingDialog = false }
        )
    }

    if (showReportDialog) {
        ReportDialog(
            contentId = task.taskId,
            contentType = "task",
            onReport = { reason, description ->
                val report = ContentReport(
                    contentId = task.taskId,
                    contentType = "task",
                    reportedBy = currentUser.userId,
                    reason = reason,
                    description = description
                )
                viewModel.reportContent(report)  // ADD THIS
                showReportDialog = false
            },
            onDismiss = { showReportDialog = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GradientStart.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(getCategoryEmoji(task.category), fontSize = 24.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = task.creatorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            // Description
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2
            )

            // Tags and ratings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE3F2FD)
                ) {
                    Text(
                        text = task.difficulty.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.padding(4.dp, 2.dp)
                    )
                }

                if (task.totalRatings > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFE082).copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(4.dp, 2.dp)
                                .clickable { showRatingDialog = true },
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "⭐ ${String.format("%.1f", task.averageRating)} (${task.totalRatings})",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF6F00)
                            )
                        }
                    }
                } else {
                    TextButton(
                        onClick = { showRatingDialog = true },
                        modifier = Modifier.height(24.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "⭐ Rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF6F00)
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFD93D).copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "+${task.reward.xp} XP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B35),
                        modifier = Modifier.padding(4.dp, 2.dp)
                    )
                }
            }

            // Buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onImport,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart),
                    enabled = !isImporting
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Add to Library",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showReportDialog = true },
                    modifier = Modifier
                        .width(44.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text("🚩", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun ChallengeMarketplaceCard(
    challenge: SharedChallenge,
    onImport: () -> Unit,
    isImporting: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GradientStart.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(getCategoryEmoji(challenge.category), fontSize = 24.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = challenge.creatorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            // Description
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2
            )

            // Challenge details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE3F2FD)
                ) {
                    Text(
                        text = "📅 ${challenge.duration}d",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.padding(4.dp, 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "⭐ ${challenge.dailyXpReward}/day",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(4.dp, 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF3E5F5)
                ) {
                    Text(
                        text = "🎁 +${challenge.completionBonusXp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6A1B9A),
                        modifier = Modifier.padding(4.dp, 2.dp)
                    )
                }
            }

            // Ratings
            if (challenge.totalRatings > 0) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFE082).copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⭐ ${String.format("%.1f", challenge.averageRating)} from ${challenge.totalRatings} ratings",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF6F00),
                        modifier = Modifier.padding(8.dp, 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Import button
            Button(
                onClick = onImport,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GradientStart),
                enabled = !isImporting
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Add to Library",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun getCategoryEmoji(category: TaskCategory): String {
    return when (category) {
        TaskCategory.MORNING_ROUTINE -> "🌅"
        TaskCategory.LEARNING -> "📚"
        TaskCategory.CHORES -> "🧹"
        TaskCategory.HEALTH -> "🏃"
        TaskCategory.CREATIVITY -> "🎨"
        TaskCategory.SOCIAL -> "👥"
        TaskCategory.FAMILY -> "👨‍👩‍👧"
        TaskCategory.OUTDOOR -> "🌳"
        TaskCategory.SLEEP -> "😴"
        TaskCategory.SCREEN_TIME -> "📱"
    }
}