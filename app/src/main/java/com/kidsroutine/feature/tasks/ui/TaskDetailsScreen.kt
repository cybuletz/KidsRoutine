package com.kidsroutine.feature.tasks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.GameType
import com.kidsroutine.core.model.TaskCategory
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.TaskReward
import java.text.SimpleDateFormat
import java.util.Locale

private val GradientStart = Color(0xFF4A90E2)
private val GradientEnd = Color(0xFF357ABD)
private val BgLight = Color(0xFFFFFBF0)

@Composable
fun TaskDetailsScreen(
    task: TaskModel,
    familyId: String,
    onBackClick: () -> Unit,
    onTaskDeleted: () -> Unit,
    onTaskUpdated: () -> Unit,
    viewModel: TaskManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var editedTitle by remember { mutableStateOf(task.title) }
    var editedDescription by remember { mutableStateOf(task.description) }
    var editedEstimatedMinutes by remember { mutableStateOf((task.estimatedDurationSec / 60).toString()) }
    var editedXpReward by remember { mutableStateOf(task.reward.xp.toString()) }
    var selectedCategory by remember { mutableStateOf(task.category) }
    var selectedDifficulty by remember { mutableStateOf(task.difficulty) }
    var selectedGame by remember { mutableStateOf(task.gameType) }
    var selectedEmoji by remember { mutableStateOf("⭐") }

    var expirationMode by remember {
        mutableStateOf(
            when {
                task.durationDays != null -> "days"
                task.expiresAt != null -> "date"
                else -> "none"
            }
        )
    }
    var expirationDays by remember { mutableStateOf(task.durationDays?.toString() ?: "") }
    var expirationDate by remember {
        mutableStateOf(
            task.expiresAt?.let {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
            } ?: ""
        )
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            if (uiState.successMessage!!.contains("deleted", ignoreCase = true)) {
                onTaskDeleted()
            } else if (uiState.successMessage!!.contains("updated", ignoreCase = true)) {
                isEditing = false
                onTaskUpdated()
            }
            viewModel.clearMessages()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTask(familyId, task.id)
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
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
                    text = if (isEditing) "Edit Task" else "Task Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    if (isEditing) {
                        IconButton(
                            onClick = {
                                val updatedTask = task.copy(
                                    title = editedTitle,
                                    description = editedDescription,
                                    estimatedDurationSec = (editedEstimatedMinutes.toIntOrNull() ?: 30) * 60,
                                    reward = TaskReward(xp = editedXpReward.toIntOrNull() ?: 10),
                                    category = selectedCategory,
                                    difficulty = selectedDifficulty,
                                    gameType = selectedGame,
                                    expiresAt = resolveExpiresAt(expirationMode, expirationDays, expirationDate),
                                    durationDays = if (expirationMode == "days") expirationDays.toIntOrNull() else null
                                )
                                viewModel.updateTask(familyId, updatedTask)
                            }
                        ) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "Save",
                                tint = Color.White
                            )
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                if (isEditing) {
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

                    Text(
                        text = "Task Name",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3436),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        placeholder = { Text("e.g., Brush Teeth") },
                        leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3436),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        placeholder = { Text("What does the child need to do?") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(bottom = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )

                    Text(
                        text = "Estimated Time (minutes)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3436),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = editedEstimatedMinutes,
                        onValueChange = { editedEstimatedMinutes = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("30") },
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

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
                        GameSelectionCard(
                            icon = "🎮",
                            title = "Memory Game",
                            description = "Match pairs of cards",
                            isSelected = selectedGame == GameType.MEMORY_GAME,
                            onClick = {
                                selectedGame =
                                    if (selectedGame == GameType.MEMORY_GAME) GameType.NONE else GameType.MEMORY_GAME
                            }
                        )

                        GameSelectionCard(
                            icon = "⚡",
                            title = "Speed Game",
                            description = "Tap colors against the clock",
                            isSelected = selectedGame == GameType.SPEED_GAME,
                            onClick = {
                                selectedGame =
                                    if (selectedGame == GameType.SPEED_GAME) GameType.NONE else GameType.SPEED_GAME
                            }
                        )

                        GameSelectionCard(
                            icon = "🧠",
                            title = "Logic Puzzle",
                            description = "Solve math problems",
                            isSelected = selectedGame == GameType.LOGIC_GAME,
                            onClick = {
                                selectedGame =
                                    if (selectedGame == GameType.LOGIC_GAME) GameType.NONE else GameType.LOGIC_GAME
                            }
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Task Expiration (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3436),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = expirationMode == "none",
                            onClick = {
                                expirationMode = "none"
                                expirationDays = ""
                                expirationDate = ""
                            },
                            label = { Text("No Expiry") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = expirationMode == "days",
                            onClick = {
                                expirationMode = "days"
                                expirationDate = ""
                            },
                            label = { Text("# Days") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = expirationMode == "date",
                            onClick = {
                                expirationMode = "date"
                                expirationDays = ""
                            },
                            label = { Text("End Date") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (expirationMode == "days") {
                        OutlinedTextField(
                            value = expirationDays,
                            onValueChange = { expirationDays = it.filter { c -> c.isDigit() } },
                            placeholder = { Text("e.g. 7") },
                            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            label = { Text("Number of days active") }
                        )
                    }

                    if (expirationMode == "date") {
                        OutlinedTextField(
                            value = expirationDate,
                            onValueChange = { expirationDate = it },
                            placeholder = { Text("DD/MM/YYYY") },
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            label = { Text("Expiry date") }
                        )
                    }

                    Text(
                        text = "XP Reward",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3436),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = editedXpReward,
                        onValueChange = { editedXpReward = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("10") },
                        leadingIcon = { Text("⭐", fontSize = 18.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

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
                } else {
                    DetailCard(
                        title = "Task Name",
                        value = task.title,
                        icon = selectedEmoji
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailCard(
                        title = "Description",
                        value = task.description.ifBlank { "No description" },
                        icon = "📝"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailCard(
                        title = "Estimated Time",
                        value = "${task.estimatedDurationSec / 60} minutes",
                        icon = "⏱️"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailCard(
                        title = "XP Reward",
                        value = "${task.reward.xp} XP",
                        icon = "⭐"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailCard(
                        title = "Category",
                        value = task.category.name.replace("_", " "),
                        icon = "🏷️"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailCard(
                        title = "Difficulty",
                        value = task.difficulty.name,
                        icon = "🎚️"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailCard(
                        title = "Game Type",
                        value = task.gameType.name.replace("_", " "),
                        icon = "🎮"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailCard(
                        title = "Expiration",
                        value = when {
                            task.durationDays != null -> "${task.durationDays} days"
                            task.expiresAt != null -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(task.expiresAt)
                            else -> "No expiry"
                        },
                        icon = "📅"
                    )
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    value: String,
    icon: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = GradientStart.copy(alpha = 0.15f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(icon, fontSize = 20.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF2D3436),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
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

private fun resolveExpiresAt(mode: String, days: String, dateStr: String): Long? {
    return when (mode) {
        "days" -> {
            val d = days.toIntOrNull() ?: return null
            System.currentTimeMillis() + d.toLong() * 24 * 60 * 60 * 1000
        }
        "date" -> {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.parse(dateStr)?.time
            } catch (e: Exception) {
                null
            }
        }
        else -> null
    }
}