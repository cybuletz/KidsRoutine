package com.kidsroutine.feature.tasks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.UserModel

private val GradientStart = Color(0xFF4A90E2)
private val GradientEnd = Color(0xFF357ABD)
private val BgLight = Color(0xFFFFFBF0)

@Composable
fun TaskListScreen(
    currentUser: UserModel,
    onCreateTaskClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: TaskManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<TaskModel?>(null) }

    LaunchedEffect(currentUser.familyId) {
        if (currentUser.familyId.isNotEmpty()) {
            viewModel.loadFamilyTasks(currentUser.familyId)
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
                    text = "Manage Tasks",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCreateTaskClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create Task",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
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
                        Text(uiState.error!!, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (uiState.tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📝", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No tasks yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Create your first task to get started!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onCreateTaskClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GradientStart
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Create Task")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.tasks) { task ->
                        TaskCard(
                            task = task,
                            onDeleteClick = { showDeleteDialog = task }
                        )
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Task?") },
                text = { Text("Are you sure you want to delete \"${showDeleteDialog!!.title}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTask(currentUser.familyId, showDeleteDialog!!.id)
                            showDeleteDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskModel,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Main row (always visible) ──────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    modifier              = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (task.category) {
                            com.kidsroutine.core.model.TaskCategory.MORNING_ROUTINE -> "🌅"
                            com.kidsroutine.core.model.TaskCategory.LEARNING        -> "📚"
                            com.kidsroutine.core.model.TaskCategory.CHORES          -> "🧹"
                            com.kidsroutine.core.model.TaskCategory.HEALTH          -> "🏃"
                            com.kidsroutine.core.model.TaskCategory.CREATIVITY      -> "🎨"
                            com.kidsroutine.core.model.TaskCategory.SOCIAL          -> "👥"
                            com.kidsroutine.core.model.TaskCategory.FAMILY          -> "👨‍👩‍👧"
                            com.kidsroutine.core.model.TaskCategory.OUTDOOR         -> "🌳"
                            com.kidsroutine.core.model.TaskCategory.SLEEP           -> "😴"
                            com.kidsroutine.core.model.TaskCategory.SCREEN_TIME     -> "📱"
                        },
                        fontSize = 32.sp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = task.title,
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF2D3436)
                        )
                        // Show truncated description when collapsed
                        if (task.description.isNotEmpty() && !expanded) {
                            Text(
                                text     = task.description,
                                style    = MaterialTheme.typography.labelSmall,
                                color    = Color.Gray,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            modifier              = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE3F2FD)) {
                                Text(
                                    "⭐ ${task.reward.xp}",
                                    style    = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFFF3E0)) {
                                Text(
                                    "⏱ ${task.estimatedDurationSec / 60}m",
                                    style    = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                // Expand chevron + delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint               = Color.LightGray,
                        modifier           = Modifier.size(20.dp)
                    )
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint               = Color(0xFFF44336),
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── Expanded description panel ─────────────────────────────────
            if (expanded && task.description.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA))
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        "Description",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.Gray,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text  = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2D3436),
                        lineHeight = 22.sp
                    )
                }
            } else if (expanded && task.description.isEmpty()) {
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No description provided.", fontSize = 13.sp, color = Color.Gray)
                }
            }
        }
    }
}
