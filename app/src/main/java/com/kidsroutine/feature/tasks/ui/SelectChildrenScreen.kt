package com.kidsroutine.feature.tasks.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.core.model.Role
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

private val GradientStart = Color(0xFF4A90E2)
private val GradientEnd = Color(0xFF357ABD)
private val BgLight = Color(0xFFFFFBF0)

@Composable
fun SelectChildrenScreen(
    task: TaskModel,
    currentUser: UserModel,
    onBackClick: () -> Unit,
    onAssignmentComplete: () -> Unit,
    viewModel: TaskManagementViewModel = hiltViewModel()
) {
    var children by remember { mutableStateOf<List<UserModel>>(emptyList()) }
    var selectedChildrenIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Load family children
    LaunchedEffect(currentUser.familyId) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val childrenSnapshot = firestore
                .collection("users")
                .whereEqualTo("familyId", currentUser.familyId)
                .whereEqualTo("role", "CHILD")
                .get()
                .await()

            children = childrenSnapshot.documents.mapNotNull { doc ->
                UserModel(
                    userId = doc.id,
                    displayName = doc.getString("displayName") ?: "Child",
                    email = doc.getString("email") ?: "",
                    role = Role.CHILD
                )
            }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
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
            // Header
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
                    text = "Assign to Children",
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
                // Task info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3436)
                        )
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "⭐ ${task.reward.xp} XP",
                            style = MaterialTheme.typography.labelMedium,
                            color = GradientStart,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // Parent approval toggle
                var requiresParent by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .clickable { requiresParent = !requiresParent },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = requiresParent,
                        onCheckedChange = { requiresParent = it }
                    )
                    Column {
                        Text(
                            text = "Requires Parent Approval",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D3436)
                        )
                        Text(
                            text = "Parent must review and approve this task",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // Children selection
                Text(
                    text = "Select children to assign this task:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (children.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("👶", fontSize = 40.sp)
                            Text(
                                "No children in your family yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        children.forEach { child ->
                            ChildSelectionCard(
                                child = child,
                                isSelected = child.userId in selectedChildrenIds,
                                onToggle = {
                                    selectedChildrenIds = if (child.userId in selectedChildrenIds) {
                                        selectedChildrenIds - child.userId
                                    } else {
                                        selectedChildrenIds + child.userId
                                    }
                                }
                            )
                        }
                    }
                }

                // Assign button
                Button(
                    onClick = {
                        isSaving = true
                        scope.launch {
                            try {
                                val firestore = FirebaseFirestore.getInstance()
                                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                    .format(java.util.Date())

                                // Generate ID for the task
                                val taskId = UUID.randomUUID().toString()

                                Log.d("SelectChildren", "Saving task: title='${task.title}', requiresParent=$requiresParent, familyId=${currentUser.familyId}")

                                // First, save the task itself to Firestore
                                firestore.collection("tasks").document(taskId).set(
                                    mapOf(
                                        "id" to taskId,
                                        "type" to task.type.name,
                                        "title" to task.title,
                                        "description" to task.description,
                                        "category" to task.category.name,
                                        "difficulty" to task.difficulty.name,
                                        "estimatedDurationSec" to task.estimatedDurationSec,
                                        "reward" to mapOf("xp" to task.reward.xp),
                                        "validationType" to task.validationType.name,
                                        "requiresParent" to requiresParent,  // ← Uses the toggle, good
                                        "requiresCoop" to task.requiresCoop,
                                        "validationType" to task.validationType.name,  // ← DUPLICATE - remove
                                        "tags" to task.tags,
                                        "createdBy" to task.createdBy.name,
                                        "interactionBlocks" to emptyList<Map<String, Any>>(),
                                        "isActive" to task.isActive,
                                        "familyId" to currentUser.familyId
                                    )
                                ).await()

                                Log.d("SelectChildren", "Task saved to Firestore with ID: $taskId")

                                // Then create task assignment for each selected child
                                selectedChildrenIds.forEach { childId ->
                                    val taskAssignmentId = "${childId}_${taskId}_${System.currentTimeMillis()}"

                                    Log.d("SelectChildren", "Creating assignment for child: $childId")

                                    firestore.collection("taskAssignments").document(taskAssignmentId).set(
                                        mapOf(
                                            "taskId" to taskId,
                                            "childId" to childId,
                                            "familyId" to currentUser.familyId,
                                            "assignedDate" to today,
                                            "status" to "ASSIGNED",
                                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                        )
                                    ).await()

                                    Log.d("SelectChildren", "Assignment created: $taskAssignmentId")
                                }

                                Log.d("SelectChildren", "All tasks assigned successfully")
                                isSaving = false
                                onAssignmentComplete()
                            } catch (e: Exception) {
                                Log.e("SelectChildren", "Error assigning tasks", e)
                                isSaving = false
                            }
                        }
                    },
                    enabled = selectedChildrenIds.isNotEmpty() && !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Assign to ${selectedChildrenIds.size} child${if (selectedChildrenIds.size != 1) "ren" else ""}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ChildSelectionCard(
    child: UserModel,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF4A90E2).copy(alpha = 0.15f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4A90E2))
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFF4A90E2).copy(alpha = 0.2f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("👦", fontSize = 24.sp)
                    }
                }

                Column {
                    Text(
                        text = child.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D3436)
                    )
                    Text(
                        text = child.email,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            if (isSelected) {
                Surface(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape),
                    color = Color(0xFF4A90E2)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}