@file:OptIn(ExperimentalMaterial3Api::class)

package com.kidsroutine.feature.tasks.ui

import androidx.compose.material3.ExperimentalMaterial3Api

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.TaskCategory
import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.GameType
import com.kidsroutine.core.model.TaskReward
import com.kidsroutine.core.model.UserModel
import java.util.*

private val GradientStart = Color(0xFF4A90E2)
private val GradientEnd = Color(0xFF357ABD)
private val BgLight = Color(0xFFFFFBF0)

@Composable
fun TaskDetailsScreen(
    task: TaskModel,
    currentUser: UserModel,
    onBackClick: () -> Unit,
    viewModel: TaskManagementViewModel = hiltViewModel()
) {
    var isEditMode by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Edit form state
    var editTitle by remember { mutableStateOf(task.title) }
    var editDescription by remember { mutableStateOf(task.description) }
    var editEstimatedMinutes by remember { mutableStateOf((task.estimatedDurationSec / 60).toString()) }
    var editXpReward by remember { mutableStateOf(task.reward.xp.toString()) }
    var editCategory by remember { mutableStateOf(task.category) }
    var editDifficulty by remember { mutableStateOf(task.difficulty) }
    var editGameType by remember { mutableStateOf(task.gameType) }
    var editExpirationMode by remember { mutableStateOf(if (task.expiresAt != null) "timestamp" else "none") }
    var editExpirationDate by remember { mutableStateOf("") }
    var editExpirationDays by remember { mutableStateOf(task.durationDays?.toString() ?: "") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null && isEditMode) {
            isEditMode = false
            viewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Top Bar
            item {
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
                        text = if (isEditMode) "Edit Task" else "Task Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isEditMode) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }

            // Display Mode
            if (!isEditMode) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3436)
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Spacer(Modifier.height(16.dp))

                            // Info Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                InfoBox("Category", task.category.name, Modifier.weight(1f))
                                InfoBox("Difficulty", task.difficulty.name, Modifier.weight(1f))
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                InfoBox("Duration", "${task.estimatedDurationSec / 60} min", Modifier.weight(1f))
                                InfoBox("XP Reward", "⭐ ${task.reward.xp}", Modifier.weight(1f))
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                InfoBox("Game Type", task.gameType.name, Modifier.weight(1f))
                                InfoBox("Status", if (task.isActive) "Active" else "Inactive", Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                // Edit Mode
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            // Title
                            OutlinedTextField(
                                value = editTitle,
                                onValueChange = { editTitle = it },
                                label = { Text("Task Title") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Description
                            OutlinedTextField(
                                value = editDescription,
                                onValueChange = { editDescription = it },
                                label = { Text("Description") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 4
                            )

                            // Category
                            var expandedCategory by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expandedCategory,
                                onExpandedChange = { expandedCategory = !expandedCategory },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                OutlinedTextField(
                                    value = editCategory.name,
                                    onValueChange = {},
                                    label = { Text("Category") },
                                    readOnly = true,
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) }
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedCategory,
                                    onDismissRequest = { expandedCategory = false }
                                ) {
                                    TaskCategory.values().forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category.name) },
                                            onClick = {
                                                editCategory = category
                                                expandedCategory = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Difficulty
                            var expandedDifficulty by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expandedDifficulty,
                                onExpandedChange = { expandedDifficulty = !expandedDifficulty },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                OutlinedTextField(
                                    value = editDifficulty.name,
                                    onValueChange = {},
                                    label = { Text("Difficulty") },
                                    readOnly = true,
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDifficulty) }
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedDifficulty,
                                    onDismissRequest = { expandedDifficulty = false }
                                ) {
                                    DifficultyLevel.values().forEach { difficulty ->
                                        DropdownMenuItem(
                                            text = { Text(difficulty.name) },
                                            onClick = {
                                                editDifficulty = difficulty
                                                expandedDifficulty = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Duration
                            OutlinedTextField(
                                value = editEstimatedMinutes,
                                onValueChange = { editEstimatedMinutes = it },
                                label = { Text("Duration (minutes)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // XP Reward
                            OutlinedTextField(
                                value = editXpReward,
                                onValueChange = { editXpReward = it },
                                label = { Text("XP Reward") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Game Type
                            var expandedGame by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expandedGame,
                                onExpandedChange = { expandedGame = !expandedGame },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                OutlinedTextField(
                                    value = editGameType.name,
                                    onValueChange = {},
                                    label = { Text("Game Type") },
                                    readOnly = true,
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGame) }
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedGame,
                                    onDismissRequest = { expandedGame = false }
                                ) {
                                    GameType.values().forEach { gameType ->
                                        DropdownMenuItem(
                                            text = { Text(gameType.name) },
                                            onClick = {
                                                editGameType = gameType
                                                expandedGame = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Save/Cancel buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { isEditMode = false },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !uiState.isLoading
                                ) {
                                    Text("Cancel", color = Color.White)
                                }

                                Button(
                                    onClick = { showConfirmDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !uiState.isLoading && editTitle.isNotEmpty()
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Save Changes", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Messages
            if (uiState.successMessage != null && !isEditMode) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        color = Color(0xFFD4EDDA),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = uiState.successMessage!!,
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFF155724),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (uiState.error != null) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        color = Color(0xFFF8D7DA),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = uiState.error!!,
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFF721C24),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Changes") },
            text = { Text("Are you sure you want to save these changes?") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        val updatedTask = task.copy(
                            title = editTitle,
                            description = editDescription,
                            estimatedDurationSec = (editEstimatedMinutes.toIntOrNull() ?: 30) * 60,
                            reward = TaskReward(xp = editXpReward.toIntOrNull() ?: 10),
                            category = editCategory,
                            difficulty = editDifficulty,
                            gameType = editGameType
                        )
                        viewModel.updateTask(currentUser.familyId, updatedTask)
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun InfoBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2D3436),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}