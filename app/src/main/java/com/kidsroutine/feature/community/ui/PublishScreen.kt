package com.kidsroutine.feature.community.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton

private val GradientStart = Color(0xFF667EEA)
private val GradientEnd = Color(0xFF764BA2)
private val BgLight = Color(0xFFFFFBF0)
private val TextDark = Color(0xFF2D3436)

@Composable
fun PublishScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit,
    viewModel: PublishViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(2000)
            onBackClick()
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
                    text = "📤 Publish Content",
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
                PublishTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.activeTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = when (tab) {
                                    PublishTab.TASK -> "📋 Task"
                                    PublishTab.CHALLENGE -> "🎯 Challenge"
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

            // Form content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                when (uiState.activeTab) {
                    PublishTab.TASK -> PublishTaskForm(
                        uiState = uiState,
                        viewModel = viewModel,
                        currentUser = currentUser
                    )
                    PublishTab.CHALLENGE -> PublishChallengeForm(
                        uiState = uiState,
                        viewModel = viewModel,
                        currentUser = currentUser
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        // Error message
        AnimatedVisibility(visible = uiState.error != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFEBEE),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "⚠️ ${uiState.error ?: ""}",
                    color = Color(0xFFC62828),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
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
                    .fillMaxWidth()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PublishTaskForm(
    uiState: PublishUiState,
    viewModel: PublishViewModel,
    currentUser: UserModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Share your task with the community",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        // Title
        OutlinedTextField(
            value = uiState.taskTitle,
            onValueChange = { viewModel.updateTaskTitle(it) },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.validationErrors.containsKey("title"),
            shape = RoundedCornerShape(12.dp)
        )
        if (uiState.validationErrors.containsKey("title")) {
            Text(
                text = uiState.validationErrors["title"] ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Description
        OutlinedTextField(
            value = uiState.taskDescription,
            onValueChange = { viewModel.updateTaskDescription(it) },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            isError = uiState.validationErrors.containsKey("description"),
            shape = RoundedCornerShape(12.dp)
        )
        if (uiState.validationErrors.containsKey("description")) {
            Text(
                text = uiState.validationErrors["description"] ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Text(
            text = "Details",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Category dropdown
        var expandedCategory by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedCategory = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    uiState.taskCategory.name,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                Icon(Icons.Default.ExpandMore, contentDescription = null)
            }
            DropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false }
            ) {
                TaskCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            viewModel.updateTaskCategory(category)
                            expandedCategory = false
                        }
                    )
                }
            }
        }

        // Difficulty dropdown
        var expandedDifficulty by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedDifficulty = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    uiState.taskDifficulty.name,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                Icon(Icons.Default.ExpandMore, contentDescription = null)
            }
            DropdownMenu(
                expanded = expandedDifficulty,
                onDismissRequest = { expandedDifficulty = false }
            ) {
                DifficultyLevel.entries.forEach { difficulty ->
                    DropdownMenuItem(
                        text = { Text(difficulty.name) },
                        onClick = {
                            viewModel.updateTaskDifficulty(difficulty)
                            expandedDifficulty = false
                        }
                    )
                }
            }
        }

        // Duration (seconds)
        OutlinedTextField(
            value = uiState.taskDuration.toString(),
            onValueChange = { if (it.isNotEmpty()) viewModel.updateTaskDuration(it.toIntOrNull() ?: 300) },
            label = { Text("Estimated Duration (seconds)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )

        // XP Reward
        OutlinedTextField(
            value = uiState.taskXp.toString(),
            onValueChange = { if (it.isNotEmpty()) viewModel.updateTaskXp(it.toIntOrNull() ?: 10) },
            label = { Text("XP Reward") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.validationErrors.containsKey("xp"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )
        if (uiState.validationErrors.containsKey("xp")) {
            Text(
                text = uiState.validationErrors["xp"] ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Note about moderation
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFE3F2FD)
        ) {
            Text(
                text = "📋 All submissions are reviewed by our moderation team before being published. Approval typically takes 24 hours.",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(12.dp)
            )
        }

        // Publish button
        Button(
            onClick = {
                viewModel.publishTask(currentUser.userId, currentUser.displayName)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GradientStart),
            enabled = !uiState.isPublishing
        ) {
            if (uiState.isPublishing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Submit for Review",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PublishChallengeForm(
    uiState: PublishUiState,
    viewModel: PublishViewModel,
    currentUser: UserModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Share your challenge with the community",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        // Title
        OutlinedTextField(
            value = uiState.challengeTitle,
            onValueChange = { viewModel.updateChallengeTitle(it) },
            label = { Text("Challenge Title") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.validationErrors.containsKey("title"),
            shape = RoundedCornerShape(12.dp)
        )
        if (uiState.validationErrors.containsKey("title")) {
            Text(
                text = uiState.validationErrors["title"] ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Description
        OutlinedTextField(
            value = uiState.challengeDescription,
            onValueChange = { viewModel.updateChallengeDescription(it) },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            isError = uiState.validationErrors.containsKey("description"),
            shape = RoundedCornerShape(12.dp)
        )
        if (uiState.validationErrors.containsKey("description")) {
            Text(
                text = uiState.validationErrors["description"] ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Text(
            text = "Details",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Category dropdown
        var expandedCategory by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedCategory = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    uiState.challengeCategory.name,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                Icon(Icons.Default.ExpandMore, contentDescription = null)
            }
            DropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false }
            ) {
                TaskCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            viewModel.updateChallengeCategory(category)
                            expandedCategory = false
                        }
                    )
                }
            }
        }

        // Difficulty dropdown
        var expandedDifficulty by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedDifficulty = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    uiState.challengeDifficulty.name,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                Icon(Icons.Default.ExpandMore, contentDescription = null)
            }
            DropdownMenu(
                expanded = expandedDifficulty,
                onDismissRequest = { expandedDifficulty = false }
            ) {
                DifficultyLevel.entries.forEach { difficulty ->
                    DropdownMenuItem(
                        text = { Text(difficulty.name) },
                        onClick = {
                            viewModel.updateChallengeDifficulty(difficulty)
                            expandedDifficulty = false
                        }
                    )
                }
            }
        }

        // Duration
        OutlinedTextField(
            value = uiState.challengeDuration.toString(),
            onValueChange = { if (it.isNotEmpty()) viewModel.updateChallengeDuration(it.toIntOrNull() ?: 7) },
            label = { Text("Duration (days)") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.validationErrors.containsKey("duration"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )
        if (uiState.validationErrors.containsKey("duration")) {
            Text(
                text = uiState.validationErrors["duration"] ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Text(
            text = "Rewards",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Daily XP
        OutlinedTextField(
            value = uiState.dailyXp.toString(),
            onValueChange = { if (it.isNotEmpty()) viewModel.updateDailyXp(it.toIntOrNull() ?: 15) },
            label = { Text("Daily XP") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.validationErrors.containsKey("dailyXp"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )
        if (uiState.validationErrors.containsKey("dailyXp")) {
            Text(
                text = uiState.validationErrors["dailyXp"] ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Bonus XP
        OutlinedTextField(
            value = uiState.bonusXp.toString(),
            onValueChange = { if (it.isNotEmpty()) viewModel.updateBonusXp(it.toIntOrNull() ?: 75) },
            label = { Text("Completion Bonus XP") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )

        // Streak XP
        OutlinedTextField(
            value = uiState.streakXp.toString(),
            onValueChange = { if (it.isNotEmpty()) viewModel.updateStreakXp(it.toIntOrNull() ?: 5) },
            label = { Text("Daily Streak Bonus XP") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )

        // Note about moderation
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFE3F2FD)
        ) {
            Text(
                text = "📋 All submissions are reviewed by our moderation team before being published. Approval typically takes 24 hours.",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(12.dp)
            )
        }

        // Publish button
        Button(
            onClick = {
                viewModel.publishChallenge(currentUser.userId, currentUser.displayName)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GradientStart),
            enabled = !uiState.isPublishing
        ) {
            if (uiState.isPublishing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Submit for Review",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}