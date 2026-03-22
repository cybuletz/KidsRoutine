package com.kidsroutine.feature.tasks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.TaskReward
import com.kidsroutine.core.model.TaskCategory
import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.UserModel
import java.util.UUID
import com.kidsroutine.core.model.GameType


private val GradientStart = Color(0xFF4A90E2)
private val GradientEnd = Color(0xFF357ABD)
private val BgLight = Color(0xFFFFFBF0)

@Composable
fun CreateTaskScreen(
    currentUser: UserModel,
    onTaskCreated: (TaskModel) -> Unit,
    onBackClick: () -> Unit,
    viewModel: TaskManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var estimatedMinutes by remember { mutableStateOf("30") }
    var selectedEmoji by remember { mutableStateOf("⭐") }
    var xpReward by remember { mutableStateOf("10") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.MORNING_ROUTINE) }
    var selectedDifficulty by remember { mutableStateOf(DifficultyLevel.EASY) }
    var selectedGame by remember { mutableStateOf(GameType.NONE) }

    // Success message handling
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            val newTask = TaskModel(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                estimatedDurationSec = (estimatedMinutes.toIntOrNull() ?: 30) * 60,
                reward = TaskReward(xp = xpReward.toIntOrNull() ?: 10),
                category = selectedCategory,
                difficulty = selectedDifficulty,
                gameType = selectedGame,
                familyId = currentUser.familyId
            )
            onTaskCreated(newTask)
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
                .verticalScroll(rememberScrollState())
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
                    text = "Create New Task",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(40.dp))
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Emoji picker
                Text(
                    text = "Task Icon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val emojis = listOf("⭐", "🎯", "🏆", "💪", "🔥", "✨", "🚀", "💎")
                    emojis.forEach { emoji ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedEmoji == emoji) GradientStart else Color(0xFFF0F0F0),
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { selectedEmoji = emoji }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(emoji, fontSize = 24.sp)
                            }
                        }
                    }
                }

                // Title field
                Text(
                    text = "Task Name",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g., Brush Teeth") },
                    leadingIcon = {
                        Icon(Icons.Default.Title, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Description field
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("What does the child need to do?") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )

                // Estimated time field
                Text(
                    text = "Estimated Time (minutes)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = estimatedMinutes,
                    onValueChange = { estimatedMinutes = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("30") },
                    leadingIcon = {
                        Icon(Icons.Default.Timer, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Game Selection Section ──────────────────────────────────────
                Text(
                    text = "Add a Micro-Game (Optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Memory Game
                    GameSelectionCard(
                        icon = "🎮",
                        title = "Memory Game",
                        description = "Match pairs of cards",
                        isSelected = selectedGame == GameType.MEMORY_GAME,
                        onClick = {
                            selectedGame = if (selectedGame == GameType.MEMORY_GAME) GameType.NONE else GameType.MEMORY_GAME
                        }
                    )

                    // Speed Game
                    GameSelectionCard(
                        icon = "⚡",
                        title = "Speed Game",
                        description = "Tap colors against the clock",
                        isSelected = selectedGame == GameType.SPEED_GAME,
                        onClick = {
                            selectedGame = if (selectedGame == GameType.SPEED_GAME) GameType.NONE else GameType.SPEED_GAME
                        }
                    )

                    // Logic Game
                    GameSelectionCard(
                        icon = "🧠",
                        title = "Logic Puzzle",
                        description = "Solve math problems",
                        isSelected = selectedGame == GameType.LOGIC_GAME,
                        onClick = {
                            selectedGame = if (selectedGame == GameType.LOGIC_GAME) GameType.NONE else GameType.LOGIC_GAME
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))

                // XP reward field
                Text(
                    text = "XP Reward",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = xpReward,
                    onValueChange = { xpReward = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("10") },
                    leadingIcon = {
                        Text("⭐", fontSize = 18.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Category selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val categories = listOf(
                    TaskCategory.MORNING_ROUTINE,
                    TaskCategory.LEARNING,
                    TaskCategory.CHORES,
                    TaskCategory.HEALTH,
                    TaskCategory.CREATIVITY
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.take(3).forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.name.replace("_", " ")) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.drop(3).forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.name.replace("_", " ")) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Difficulty selector
                Text(
                    text = "Difficulty",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DifficultyLevel.values().forEach { difficulty ->
                        FilterChip(
                            selected = selectedDifficulty == difficulty,
                            onClick = { selectedDifficulty = difficulty },
                            label = { Text(difficulty.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Error message
                if (uiState.error != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Create button
                Button(
                    onClick = {
                        if (title.isEmpty()) {
                            // Show error
                        } else {
                            val newTask = TaskModel(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                description = description,
                                estimatedDurationSec = (estimatedMinutes.toIntOrNull() ?: 30) * 60,
                                reward = TaskReward(xp = xpReward.toIntOrNull() ?: 10),
                                category = selectedCategory,
                                difficulty = selectedDifficulty,
                                gameType = selectedGame,
                                familyId = currentUser.familyId
                            )
                            viewModel.createTask(currentUser.familyId, newTask)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 32.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart
                    ),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Create Task",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameSelectionCard(
    icon: String,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF4ECDC4).copy(alpha = 0.2f) else Color(0xFFF5F5F5)
    val borderColor = if (isSelected) Color(0xFF4ECDC4) else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 32.sp)
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436)
                )
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}