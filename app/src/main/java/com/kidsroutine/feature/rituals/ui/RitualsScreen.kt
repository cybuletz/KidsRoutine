package com.kidsroutine.feature.rituals.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.FamilyRitual
import com.kidsroutine.core.model.RitualFrequency
import com.kidsroutine.core.model.RitualType
import com.kidsroutine.core.model.UserModel

private val RitualPurple = Color(0xFF7C3AED)
private val RitualPurpleLight = Color(0xFFEDE9FE)
private val RitualWarm = Color(0xFFFF8F00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RitualsScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit
) {
    val viewModel: RitualsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.familyId) {
        viewModel.loadRituals(currentUser.familyId)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF0))
    ) {
        // Header
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(RitualPurple, Color(0xFF5B21B6))))
                .padding(top = 48.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.TopStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🙏", fontSize = 36.sp)
                Spacer(Modifier.height(4.dp))
                Text("Family Rituals", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("Meaningful moments together", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RitualPurple)
            }
            return@Column
        }

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Create button
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Rituals", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
                    IconButton(
                        onClick = { viewModel.toggleCreateForm() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(RitualPurple)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, "Add", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Create Form
            if (state.showCreateForm) {
                item {
                    CreateRitualCard(
                        onSave = { type, title, desc, freq, goalTitle, goalTarget, goalUnit ->
                            viewModel.createRitual(currentUser.familyId, type, title, desc, freq, goalTitle, goalTarget, goalUnit)
                        },
                        onCancel = { viewModel.toggleCreateForm() }
                    )
                }
            }

            // Empty state
            if (state.rituals.isEmpty() && !state.showCreateForm) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🕊️", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No rituals yet", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Text("Create your first family ritual!", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Ritual cards
            items(state.rituals) { ritual ->
                RitualCard(
                    ritual = ritual,
                    currentUserId = currentUser.userId,
                    gratitudeText = if (state.selectedRitual?.ritualId == ritual.ritualId) state.gratitudeText else "",
                    onGratitudeTextChange = { viewModel.updateGratitudeText(it) },
                    onSelect = { viewModel.selectRitual(ritual) },
                    onComplete = { viewModel.completeRitual(ritual.ritualId, currentUser.familyId) },
                    onSubmitGratitude = { viewModel.submitGratitude(ritual.ritualId, currentUser.userId, currentUser.familyId) },
                    onUpdateGoal = { viewModel.updateGoalProgress(ritual.ritualId, currentUser.familyId) },
                    onDelete = { viewModel.deleteRitual(ritual.ritualId, currentUser.familyId) },
                    isSelected = state.selectedRitual?.ritualId == ritual.ritualId
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun RitualCard(
    ritual: FamilyRitual,
    currentUserId: String,
    gratitudeText: String,
    onGratitudeTextChange: (String) -> Unit,
    onSelect: () -> Unit,
    onComplete: () -> Unit,
    onSubmitGratitude: () -> Unit,
    onUpdateGoal: () -> Unit,
    onDelete: () -> Unit,
    isSelected: Boolean
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onSelect() }
            .padding(16.dp)
            .animateContentSize()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(ritual.type.emoji, fontSize = 28.sp)
                    Column {
                        Text(ritual.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                        Text(
                            "${ritual.frequency.name.lowercase().replaceFirstChar { it.uppercase() }} · ${ritual.type.displayName}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                Row {
                    IconButton(onClick = onComplete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Check, "Complete", tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (ritual.description.isNotBlank()) {
                Text(ritual.description, fontSize = 13.sp, color = Color.Gray)
            }

            // XP Reward
            Box(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(RitualPurpleLight)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("⭐ ${ritual.completionXp} XP for everyone", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RitualPurple)
            }

            // Expanded content based on type
            AnimatedVisibility(visible = isSelected) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Divider(color = Color(0xFFE0E0E0))

                    when (ritual.type) {
                        RitualType.GRATITUDE_CIRCLE -> {
                            Text("Share what you're grateful for today:", fontWeight = FontWeight.SemiBold, color = RitualPurple)

                            // Existing entries
                            ritual.gratitudeEntries.forEach { (userId, text) ->
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFFF8E1))
                                        .padding(12.dp)
                                ) {
                                    Text("\"$text\"", fontSize = 14.sp, color = Color(0xFF5D4037))
                                }
                            }

                            // Input for current user
                            val alreadySubmitted = ritual.gratitudeEntries.containsKey(currentUserId)
                            if (!alreadySubmitted) {
                                OutlinedTextField(
                                    value = gratitudeText,
                                    onValueChange = onGratitudeTextChange,
                                    label = { Text("I'm grateful for...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Button(
                                    onClick = onSubmitGratitude,
                                    enabled = gratitudeText.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = RitualPurple),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Share 🙏", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                Text("✅ You've shared today!", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        RitualType.FAMILY_MEETING -> {
                            Text("📋 Meeting Agenda (${ritual.meetingDurationMin} min):", fontWeight = FontWeight.SemiBold, color = RitualPurple)
                            ritual.agendaItems.forEachIndexed { index, item ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("${index + 1}.", fontWeight = FontWeight.Bold, color = RitualPurple)
                                    Text(item, color = Color(0xFF374151))
                                }
                            }
                            if (ritual.agendaItems.isEmpty()) {
                                Text("No agenda items set yet.", color = Color.Gray, fontSize = 13.sp)
                            }
                        }

                        RitualType.SHARED_GOAL -> {
                            if (ritual.goalTitle.isNotBlank()) {
                                Text("🎯 ${ritual.goalTitle}", fontWeight = FontWeight.Bold, color = RitualPurple)
                                val goalProgress by animateFloatAsState(
                                    ritual.goalProgressPercent, tween(600), label = "ritualGoal"
                                )
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE0E0E0))
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(goalProgress)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Brush.horizontalGradient(listOf(RitualPurple, RitualWarm)))
                                    )
                                }
                                Text(
                                    "${ritual.goalProgress}/${ritual.goalTarget} ${ritual.goalUnit}",
                                    fontSize = 13.sp, color = Color.Gray
                                )
                                Button(
                                    onClick = onUpdateGoal,
                                    enabled = ritual.goalProgress < ritual.goalTarget,
                                    colors = ButtonDefaults.buttonColors(containerColor = RitualPurple),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("+1 ${ritual.goalUnit} ✨", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        RitualType.MEMORY_LANE -> {
                            Text("📸 Share a favorite memory with your family!", fontWeight = FontWeight.SemiBold, color = RitualPurple)
                            Text("Look through photos together and pick one to talk about.", fontSize = 13.sp, color = Color.Gray)
                        }

                        RitualType.CELEBRATION -> {
                            Text("🎉 Time to celebrate!", fontWeight = FontWeight.SemiBold, color = RitualPurple)
                            Text("Recognize achievements and milestones together.", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRitualCard(
    onSave: (RitualType, String, String, RitualFrequency, String, Int, String) -> Unit,
    onCancel: () -> Unit
) {
    var selectedType by remember { mutableStateOf(RitualType.GRATITUDE_CIRCLE) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(RitualFrequency.DAILY) }
    var goalTitle by remember { mutableStateOf("") }
    var goalTarget by remember { mutableStateOf("3") }
    var goalUnit by remember { mutableStateOf("times") }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RitualPurpleLight)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("New Ritual", fontWeight = FontWeight.ExtraBold, color = RitualPurple, fontSize = 16.sp)

            // Type selection
            Text("Type:", fontSize = 13.sp, color = Color.Gray)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RitualType.entries.forEach { type ->
                    val isSelected = type == selectedType
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) RitualPurple.copy(alpha = 0.2f) else Color.White)
                            .clickable { selectedType = type }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(type.emoji, fontSize = 18.sp)
                            Text(type.displayName.split(" ").first(), fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) RitualPurple else Color.Gray)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Frequency
            Text("Frequency:", fontSize = 13.sp, color = Color.Gray)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RitualFrequency.entries.forEach { freq ->
                    val isSelected = freq == frequency
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) RitualPurple else Color.White)
                            .clickable { frequency = freq }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            freq.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else Color.Gray
                        )
                    }
                }
            }

            // Goal fields (for SHARED_GOAL type)
            if (selectedType == RitualType.SHARED_GOAL) {
                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = { goalTitle = it },
                    label = { Text("Goal (e.g., Cook dinner together)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = goalTarget,
                        onValueChange = { goalTarget = it.filter { c -> c.isDigit() } },
                        label = { Text("Target") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = goalUnit,
                        onValueChange = { goalUnit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(selectedType, title, description, frequency, goalTitle, goalTarget.toIntOrNull() ?: 0, goalUnit)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RitualPurple),
                    shape = RoundedCornerShape(10.dp),
                    enabled = title.isNotBlank()
                ) {
                    Text("Create 🙏", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
