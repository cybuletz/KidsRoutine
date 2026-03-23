package com.kidsroutine.feature.generation.ui

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
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
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.generation.data.GeneratedChallenge
import com.kidsroutine.feature.generation.data.GeneratedTask
import kotlinx.coroutines.launch
import com.kidsroutine.feature.tasks.ui.SelectChildrenScreen


// ────────────────────────────────────────────────────────────────────────────
// COLOR PALETTE
// ────────────────────────────────────────────────────────────────────────────

private val PrimaryBlue = Color(0xFF667EEA)
private val SecondaryOrange = Color(0xFFFF6B35)
private val AccentPurple = Color(0xFF9B59B6)
private val SuccessGreen = Color(0xFF2ECC71)
private val BgLight = Color(0xFFF8F9FA)
private val TextDark = Color(0xFF2D3436)

// ────────────────────────────────────────────────────────────────────────────
// MAIN SCREEN
// ────────────────────────────────────────────────────────────────────────────

@Composable
fun GenerationScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit = {},
    viewModel: GenerationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableStateOf(GenerationTab.TASKS) }
    var showChildSelection by remember { mutableStateOf(false) }
    var selectedTaskForAssignment by remember { mutableStateOf<com.kidsroutine.core.model.TaskModel?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        Log.d("GenerationScreen", "Screen loaded for user: ${currentUser.displayName}")
    }

    // ════════════════════════════════════════════════════════════════════════
    // SHOW CHILD SELECTION SCREEN (FULL SCREEN) ← NEW!
    // ════════════════════════════════════════════════════════════════════════
    if (showChildSelection && selectedTaskForAssignment != null) {
        SelectChildrenScreen(
            task = selectedTaskForAssignment!!,
            currentUser = currentUser,
            onBackClick = {
                showChildSelection = false
                selectedTaskForAssignment = null
            },
            onAssignmentComplete = {
                showChildSelection = false
                selectedTaskForAssignment = null
            }
        )
        return  // ← EXIT early, don't show GenerationScreen
    }

    // ════════════════════════════════════════════════════════════════════════
    // NORMAL GENERATION SCREEN
    // ════════════════════════════════════════════════════════════════════════
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgLight)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ──────── HERO SECTION ────────
            item {
                HeroSection(currentUser = currentUser)
            }

            // ──────── TAB SELECTOR ────────
            item {
                TabSelector(
                    activeTab = activeTab,
                    onTabChange = { activeTab = it }
                )
            }

            // ──────── CONTENT ────────
            when (activeTab) {
                GenerationTab.TASKS -> {
                    item {
                        TaskGenerationContent(
                            currentUser = currentUser,
                            uiState = uiState,
                            viewModel = viewModel,
                            onGenerateClick = {
                                coroutineScope.launch {
                                    viewModel.generateTasks(
                                        currentUser = currentUser,
                                        childAge = currentUser.level + 4
                                    )
                                }
                            },
                            onTaskSelected = { taskModel ->  // ← NEW callback
                                selectedTaskForAssignment = taskModel
                                showChildSelection = true
                            }
                        )
                    }
                }

                GenerationTab.CHALLENGES -> {
                    item {
                        ChallengeGenerationContent(
                            currentUser = currentUser,
                            uiState = uiState,
                            viewModel = viewModel,
                            onGenerateClick = {
                                coroutineScope.launch {
                                    viewModel.generateChallenges(
                                        currentUser = currentUser,
                                        childAge = currentUser.level + 4
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // ──────── ERROR MESSAGE ────────
            if (uiState.error != null) {
                item {
                    ErrorBanner(
                        message = uiState.error ?: "",
                        onDismiss = { viewModel.clearMessages() }
                    )
                }
            }

            // ──────── SUCCESS MESSAGE ────────
            if (uiState.successMessage != null) {
                item {
                    SuccessBanner(
                        message = uiState.successMessage ?: "",
                        onDismiss = { viewModel.clearMessages() }
                    )
                }
            }
        }
    }
}

enum class GenerationTab {
    TASKS, CHALLENGES
}

// ────────────────────────────────────────────────────────────────────────────
// HERO SECTION
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(currentUser: UserModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryBlue, AccentPurple)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "✨ Create Custom Tasks",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "For ${currentUser.displayName}",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "🤖 Powered by Gemini AI",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// TAB SELECTOR
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun TabSelector(
    activeTab: GenerationTab,
    onTabChange: (GenerationTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabButton(
            label = "📋 Tasks",
            isActive = activeTab == GenerationTab.TASKS,
            onClick = { onTabChange(GenerationTab.TASKS) },
            modifier = Modifier.weight(1f)
        )

        TabButton(
            label = "🏆 Challenges",
            isActive = activeTab == GenerationTab.CHALLENGES,
            onClick = { onTabChange(GenerationTab.CHALLENGES) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) PrimaryBlue else Color.Transparent,
            contentColor = if (isActive) Color.White else TextDark
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

// ────────────────────────────────────────────────────────────────────────────
// TASK GENERATION CONTENT
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun TaskGenerationContent(
    currentUser: UserModel,
    uiState: GenerationUiState,
    viewModel: GenerationViewModel,
    onGenerateClick: () -> Unit,
    onTaskSelected: (com.kidsroutine.core.model.TaskModel) -> Unit = {}  // ← NEW!
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ──────── INPUT SECTION ────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "📝 Task Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                // ═════ DIFFICULTY SELECTOR (NOW INTERACTIVE!) ═════
                Text(
                    text = "Difficulty Level",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("EASY", "MEDIUM", "HARD").forEach { difficulty ->
                        DifficultyChip(
                            label = difficulty,
                            emoji = when (difficulty) {
                                "EASY" -> "🟢"
                                "MEDIUM" -> "🟠"
                                else -> "🔴"
                            },
                            isSelected = uiState.selectedDifficulty == difficulty,
                            onClick = { viewModel.toggleDifficulty(difficulty) }
                        )
                    }
                }

                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                // ═════ PREFERENCES SELECTOR (NOW INTERACTIVE!) ═════
                Text(
                    text = "Preferences (Multi-select)",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("🎨 Creative", "⚽ Sports", "📚 Learning", "🧘 Wellness").forEach { pref ->
                        PreferenceChip(
                            label = pref,
                            isSelected = pref in uiState.selectedPreferences,
                            onClick = { viewModel.togglePreference(pref) }
                        )
                    }
                }
            }
        }

        // ──────── QUOTA INDICATOR ────────
        QuotaCard(
            remaining = uiState.quotaRemaining,
            limit = 3,
            tier = "FREE"
        )

        // ──────── GENERATE BUTTON (NOW ENABLED!) ────────
        Button(
            onClick = onGenerateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SecondaryOrange,
                disabledContainerColor = Color(0xFFCCCCCC)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !uiState.isLoading && uiState.quotaRemaining > 0
        ) {
            if (uiState.isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text("Generating...", fontWeight = FontWeight.Bold)
                }
            } else if (uiState.quotaRemaining <= 0) {
                Text("📊 Quota Exceeded - Upgrade to PRO", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else {
                Text("✨ Generate Tasks", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // ──────── RESULTS ────────
        if (uiState.generatedTasks.isNotEmpty()) {
            Text(
                text = "Generated Tasks (${uiState.generatedTasks.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            uiState.generatedTasks.forEachIndexed { index, task ->
                TaskCard(
                    task = task,
                    index = index,
                    viewModel = viewModel,
                    currentUser = currentUser,
                    onUseNow = { onTaskSelected(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ────────────────────────────────────────────────────────────────────────────
// CHALLENGE GENERATION CONTENT
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChallengeGenerationContent(
    currentUser: UserModel,
    uiState: GenerationUiState,
    viewModel: GenerationViewModel,
    onGenerateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ──────── INPUT SECTION ────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🎯 Challenge Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                // ═════ GOALS SELECTOR (NOW INTERACTIVE!) ═════
                Text(
                    text = "Goals (Multi-select)",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("💤 Better Sleep", "📱 Screen Time", "🏃 Health", "🤝 Social").forEach { goal ->
                        GoalChip(
                            label = goal,
                            isSelected = goal in uiState.selectedGoals,
                            onClick = { viewModel.toggleGoal(goal) }
                        )
                    }
                }

                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                Text(
                    text = "Duration: 3-30 days",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // ──────── QUOTA INDICATOR ────────
        QuotaCard(
            remaining = uiState.quotaRemaining,
            limit = 2,
            tier = "PRO"
        )

        // ──────── GENERATE BUTTON ────────
        Button(
            onClick = onGenerateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentPurple,
                disabledContainerColor = Color(0xFFCCCCCC)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !uiState.isLoading && uiState.quotaRemaining > 0
        ) {
            if (uiState.isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text("Generating...", fontWeight = FontWeight.Bold)
                }
            } else if (uiState.quotaRemaining <= 0) {
                Text("🔒 PRO Only - Upgrade Required", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else {
                Text("🏆 Generate Challenge", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // ──────── RESULTS ────��───
        if (uiState.generatedChallenges.isNotEmpty()) {
            Text(
                text = "Generated Challenges (${uiState.generatedChallenges.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            uiState.generatedChallenges.forEachIndexed { index, challenge ->
                ChallengeCard(challenge = challenge, index = index)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ────────────────────────────────────────────────────────────────────────────
// CARDS & COMPONENTS
// ──────────────────────────────────────────────────────────��─────────────────

@Composable
private fun TaskCard(
    task: GeneratedTask,
    index: Int,
    onUseNow: (com.kidsroutine.core.model.TaskModel) -> Unit = {},
    viewModel: GenerationViewModel = hiltViewModel(),
    currentUser: com.kidsroutine.core.model.UserModel = com.kidsroutine.core.model.UserModel()
) {
    var showCustomizationModal by remember { mutableStateOf(false) }
    var currentTask by remember { mutableStateOf(task) }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentTask.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (currentTask.difficulty) {
                            "EASY" -> Color(0xFFE8F5E9)
                            "MEDIUM" -> Color(0xFFFFF3E0)
                            else -> Color(0xFFFFEBEE)
                        }
                    ) {
                        Text(
                            text = currentTask.difficulty.take(1).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (currentTask.difficulty) {
                                "EASY" -> SuccessGreen
                                "MEDIUM" -> SecondaryOrange
                                else -> Color(0xFFC62828)
                            },
                            modifier = Modifier.padding(4.dp, 2.dp)
                        )
                    }
                }

                // Description
                Text(
                    text = currentTask.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailBadge("⏱️", "${currentTask.estimatedDurationSec}s")
                    DetailBadge("⭐", "${currentTask.xpReward} XP")
                    DetailBadge("📂", currentTask.category.take(3))
                }

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            // Convert to TaskModel and pass to parent
                            val taskModel = com.kidsroutine.core.model.TaskModel(
                                id = "task_${System.currentTimeMillis()}",
                                type = try {
                                    com.kidsroutine.core.model.TaskType.valueOf(currentTask.type)
                                } catch (e: Exception) {
                                    com.kidsroutine.core.model.TaskType.REAL_LIFE
                                },
                                title = currentTask.title,
                                description = currentTask.description,
                                category = try {
                                    com.kidsroutine.core.model.TaskCategory.valueOf(currentTask.category)
                                } catch (e: Exception) {
                                    com.kidsroutine.core.model.TaskCategory.LEARNING
                                },
                                difficulty = try {
                                    com.kidsroutine.core.model.DifficultyLevel.valueOf(currentTask.difficulty)
                                } catch (e: Exception) {
                                    com.kidsroutine.core.model.DifficultyLevel.EASY
                                },
                                estimatedDurationSec = currentTask.estimatedDurationSec,
                                reward = com.kidsroutine.core.model.TaskReward(xp = currentTask.xpReward),
                                familyId = currentUser.familyId
                            )
                            onUseNow(taskModel)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("✓ Use Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showCustomizationModal = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("✏️", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.addToFavorites(currentTask) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("❤️", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.shareToMarketplace(currentTask, currentUser.familyId) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📤", fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // CUSTOMIZATION MODAL ONLY
    // ════════════════════════════════════════════════════════════════════════
    TaskCustomizationModal(
        task = currentTask,
        isVisible = showCustomizationModal,
        onDismiss = { showCustomizationModal = false },
        onSave = { updatedTask ->
            currentTask = updatedTask
            showCustomizationModal = false
        }
    )
}

@Composable
private fun ChallengeCard(challenge: GeneratedChallenge, index: Int) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = challenge.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Duration badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFE3F2FD)
                ) {
                    Text(
                        text = "📅 ${challenge.durationDays} days",
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(8.dp, 4.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Description
                Text(
                    text = challenge.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Success condition
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF9C4)
                ) {
                    Text(
                        text = "✅ ${challenge.successCondition}",
                        fontSize = 13.sp,
                        color = Color(0xFF827717),
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* Start challenge */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentPurple
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Start", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { /* Preview */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Preview", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// UTILITY COMPONENTS
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuotaCard(remaining: Int, limit: Int, tier: String = "FREE") {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                remaining > 0 -> Color(0xFFE8F5E9)
                else -> Color(0xFFFFEBEE)
            }
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Generations Today ($tier)",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$remaining of $limit remaining",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                // Progress circle
                CircularProgressIndicator(
                    progress = { (remaining.coerceAtLeast(0).toFloat() / limit).coerceIn(0f, 1f) },
                    modifier = Modifier.size(60.dp),
                    color = if (remaining > 0) SuccessGreen else Color(0xFFC62828),
                    strokeWidth = 4.dp
                )
            }

            // Upgrade button if needed
            if (remaining <= 0) {
                Button(
                    onClick = { /* Navigate to upgrade */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("⭐ Upgrade to PRO - Unlimited Generations!", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DifficultyChip(
    label: String,
    emoji: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryBlue else Color(0xFFF0F0F0),
        border = if (isSelected) BorderStroke(2.dp, PrimaryBlue) else null,
        modifier = Modifier
            .clickable { onClick() }
            .animateContentSize()
    ) {
        Text(
            text = "$emoji $label",
            fontSize = 12.sp,
            modifier = Modifier.padding(10.dp, 6.dp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextDark
        )
    }
}

@Composable
private fun PreferenceChip(
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryBlue.copy(alpha = 0.2f) else Color.White,
        border = BorderStroke(
            1.dp,
            if (isSelected) PrimaryBlue else Color(0xFFEEEEEE)
        ),
        modifier = Modifier
            .clickable { onClick() }
            .animateContentSize()
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            modifier = Modifier.padding(10.dp, 6.dp),
            color = if (isSelected) PrimaryBlue else TextDark,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun GoalChip(
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) AccentPurple.copy(alpha = 0.2f) else Color.White,
        border = BorderStroke(
            1.dp,
            if (isSelected) AccentPurple else Color(0xFFEEEEEE)
        ),
        modifier = Modifier
            .clickable { onClick() }
            .animateContentSize()
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            modifier = Modifier.padding(10.dp, 6.dp),
            color = if (isSelected) AccentPurple else TextDark,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun DetailBadge(emoji: String, text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Text(
            text = "$emoji $text",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(8.dp, 4.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFEBEE),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️ $message",
                color = Color(0xFFC62828),
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFC62828))
            }
        }
    }
}

@Composable
private fun SuccessBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFE8F5E9),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✅ $message",
                color = SuccessGreen,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = SuccessGreen)
            }
        }
    }
}