package com.kidsroutine.feature.tasks.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

    Log.d("SelectChildrenScreen", "Received task object with ID: ${task.id}, title: ${task.title}")

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
            Log.e("SelectChildren", "Error loading children: ${e.message}", e)
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
                .fillMaxHeight(0.15f)
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header
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
                        text = "Assign to Children",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(40.dp))
                }
            }

            item { Spacer(Modifier.height(24.dp)) }

            // Task Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
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
                            color = Color(0xFF4A90E2),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Approval Toggle
            item {
                var requiresParent by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
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
            }

            // Children Selection Title
            item {
                Text(
                    text = "Select children to assign this task:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp)
                )
            }

            // Children List
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (children.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
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
                }
            } else {
                items(children) { child ->
                    ChildSelectionCard(
                        child = child,
                        isSelected = child.userId in selectedChildrenIds,
                        onToggle = {
                            selectedChildrenIds = if (child.userId in selectedChildrenIds) {
                                selectedChildrenIds - child.userId
                            } else {
                                selectedChildrenIds + child.userId
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 12.dp)
                    )
                }
            }

            // Assign Button
            item {
                Button(
                    onClick = {
                        isSaving = true
                        scope.launch {
                            try {
                                val firestore = FirebaseFirestore.getInstance()
                                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                    .format(java.util.Date())

                                val taskId = task.id

                                Log.d("SelectChildren", "Saving task: title='${task.title}', familyId=${currentUser.familyId}")

                                Log.d("SelectChildrenScreen", "Using taskId: $taskId for assignment")


                                selectedChildrenIds.forEach { childId ->
                                    val taskAssignmentId = "${taskId}_${System.currentTimeMillis()}"

                                    // ✅ NEW PATH: /families/{familyId}/users/{childId}/assignments/{docId}
                                    firestore
                                        .collection("families").document(currentUser.familyId)
                                        .collection("users").document(childId)
                                        .collection("assignments").document(taskAssignmentId)
                                        .set(
                                            mapOf(
                                                "taskId" to taskId,
                                                "childId" to childId,
                                                "familyId" to currentUser.familyId,
                                                "assignedDate" to today,
                                                "status" to "ASSIGNED",
                                                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                            )
                                        ).await()

                                    Log.d("SelectChildren", "Task assignment created for child: $childId at /families/${currentUser.familyId}/users/$childId/assignments/")
                                }

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
                        .height(56.dp)
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
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
            }
        }
    }
}

@Composable
private fun ChildSelectionCard(
    child: UserModel,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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