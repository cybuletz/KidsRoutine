package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsroutine.core.common.util.SoundManager
import com.kidsroutine.core.model.AgeGroup
import kotlinx.coroutines.delay

/**
 * TIME ARCHITECT GAME: Schedule activities into optimal time slots.
 * Features:
 * - Tap activity then tap slot to place it
 * - Age-adaptive: simple ordering → dependency scheduling → optimization
 * - Constraint validation on submit
 * - Animated placement feedback
 */
@Composable
fun TimeArchitectGameBlock(
    ageGroup: AgeGroup = AgeGroup.EXPLORER,
    onSuccess: () -> Unit
) {
    data class Activity(
        val name: String,
        val emoji: String,
        val duration: Int,
        val validSlots: List<String>,
        val requiredBefore: List<String> = emptyList(),
        val priority: Int = 1
    )

    data class TimeSlot(
        val name: String,
        val maxHours: Int,
        val emoji: String
    )

    val slots: List<TimeSlot>
    val activities: List<Activity>

    when (ageGroup) {
        AgeGroup.SPROUT -> {
            slots = listOf(
                TimeSlot("🌅 Morning", 4, "🌅"),
                TimeSlot("☀️ Afternoon", 4, "☀️"),
                TimeSlot("🌙 Evening", 4, "🌙")
            )
            activities = listOf(
                Activity("Breakfast", "🥣", 1, listOf("🌅 Morning")),
                Activity("School", "🏫", 3, listOf("🌅 Morning", "☀️ Afternoon")),
                Activity("Bedtime", "🛏️", 1, listOf("🌙 Evening"))
            )
        }
        AgeGroup.EXPLORER -> {
            slots = listOf(
                TimeSlot("🌅 Morning", 3, "🌅"),
                TimeSlot("☀️ Afternoon", 3, "☀️"),
                TimeSlot("🌙 Evening", 3, "🌙")
            )
            activities = listOf(
                Activity("Exercise", "🏃", 1, listOf("🌅 Morning", "☀️ Afternoon")),
                Activity("Homework", "📚", 2, listOf("☀️ Afternoon", "🌙 Evening")),
                Activity("Chores", "🧹", 1, listOf("🌅 Morning", "☀️ Afternoon")),
                Activity("Reading", "📖", 1, listOf("☀️ Afternoon", "🌙 Evening")),
                Activity("Dinner", "🍽️", 1, listOf("🌙 Evening"))
            )
        }
        AgeGroup.TRAILBLAZER -> {
            slots = listOf(
                TimeSlot("🌅 Morning", 4, "🌅"),
                TimeSlot("☀️ Afternoon", 4, "☀️"),
                TimeSlot("🌆 Late Afternoon", 3, "🌆"),
                TimeSlot("🌙 Evening", 3, "🌙")
            )
            activities = listOf(
                Activity("Breakfast", "🥣", 1, listOf("🌅 Morning")),
                Activity("Math Class", "📐", 2, listOf("🌅 Morning", "☀️ Afternoon")),
                Activity("Homework", "📚", 2, listOf("☀️ Afternoon", "🌆 Late Afternoon"),
                    requiredBefore = listOf("Play Time")),
                Activity("Play Time", "⚽", 1, listOf("🌆 Late Afternoon", "🌙 Evening")),
                Activity("Dinner", "🍽️", 1, listOf("🌙 Evening"),
                    requiredBefore = listOf()),
                Activity("Eat Snack", "🍎", 1, listOf("☀️ Afternoon"),
                    requiredBefore = listOf("Exercise")),
                Activity("Exercise", "🏃", 1, listOf("🌆 Late Afternoon", "🌙 Evening"))
            )
        }
        AgeGroup.LEGEND -> {
            slots = listOf(
                TimeSlot("🌅 Morning (High Energy)", 4, "🌅"),
                TimeSlot("☀️ Midday (Medium Energy)", 3, "☀️"),
                TimeSlot("🌆 Afternoon (Low Energy)", 3, "🌆"),
                TimeSlot("🌙 Evening (Rest)", 3, "🌙")
            )
            activities = listOf(
                Activity("Deep Study", "🧠", 2, listOf("🌅 Morning (High Energy)"), priority = 5),
                Activity("Creative Writing", "✍️", 2,
                    listOf("🌅 Morning (High Energy)", "☀️ Midday (Medium Energy)"), priority = 4),
                Activity("Team Meeting", "👥", 1,
                    listOf("☀️ Midday (Medium Energy)"), priority = 3),
                Activity("Email & Admin", "📧", 1,
                    listOf("🌆 Afternoon (Low Energy)", "🌙 Evening (Rest)"), priority = 1),
                Activity("Exercise", "🏋️", 1,
                    listOf("☀️ Midday (Medium Energy)", "🌆 Afternoon (Low Energy)"), priority = 3),
                Activity("Light Reading", "📖", 1,
                    listOf("🌆 Afternoon (Low Energy)", "🌙 Evening (Rest)"), priority = 2),
                Activity("Meal Prep", "🍳", 1, listOf("🌙 Evening (Rest)"), priority = 2),
                Activity("Project Work", "💻", 2,
                    listOf("🌅 Morning (High Energy)", "☀️ Midday (Medium Energy)"),
                    requiredBefore = listOf("Email & Admin"),
                    priority = 5
                )
            )
        }
    }

    var unplaced by remember { mutableStateOf(activities.shuffled()) }
    var placements by remember {
        mutableStateOf<Map<String, List<Activity>>>(slots.associate { it.name to emptyList() })
    }
    var selectedActivity by remember { mutableStateOf<Activity?>(null) }
    var validationErrors by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSuccess by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    fun validateSchedule(): List<String> {
        val errors = mutableListOf<String>()

        // Check all placed
        if (unplaced.isNotEmpty()) {
            errors.add("Place all activities first!")
            return errors
        }

        // Check slot capacity
        slots.forEach { slot ->
            val placed = placements[slot.name] ?: emptyList()
            val totalHours = placed.sumOf { it.duration }
            if (totalHours > slot.maxHours) {
                errors.add("${slot.name} is overbooked (${totalHours}h > ${slot.maxHours}h)")
            }
        }

        // Check valid slots
        placements.forEach { (slotName, acts) ->
            acts.forEach { act ->
                if (slotName !in act.validSlots) {
                    errors.add("${act.emoji} ${act.name} can't go in $slotName")
                }
            }
        }

        // Check dependency ordering (requiredBefore)
        if (ageGroup == AgeGroup.TRAILBLAZER || ageGroup == AgeGroup.LEGEND) {
            val slotOrder = slots.mapIndexed { idx, s -> s.name to idx }.toMap()
            activities.forEach { act ->
                act.requiredBefore.forEach { beforeName ->
                    val beforeAct = activities.find { it.name == beforeName }
                    if (beforeAct != null) {
                        val actSlot = placements.entries.find { act in it.value }?.key
                        val beforeSlot = placements.entries.find { beforeAct in it.value }?.key
                        if (actSlot != null && beforeSlot != null) {
                            val actOrder = slotOrder[actSlot] ?: 0
                            val beforeOrder = slotOrder[beforeSlot] ?: 0
                            if (actOrder >= beforeOrder) {
                                errors.add("${act.emoji} ${act.name} must come before ${beforeAct.emoji} ${beforeName}")
                            }
                        }
                    }
                }
            }
        }

        return errors
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            "🕐 Time Architect",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF6C5CE7)
        )

        // Instructions
        Text(
            if (selectedActivity != null) "Now tap a time slot ⬇"
            else if (unplaced.isNotEmpty()) "Tap an activity to select it"
            else "All placed! Tap Submit ✓",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        // Unplaced activities
        if (unplaced.isNotEmpty()) {
            Text("Activities:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F0F0))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                unplaced.forEach { activity ->
                    val isSelected = selectedActivity == activity
                    val chipScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "activity_chip_scale_${activity.name}"
                    )

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .scale(chipScale)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF6C5CE7) else Color.White)
                            .clickable {
                                SoundManager.playTap()
                                selectedActivity = if (isSelected) null else activity
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(activity.emoji, fontSize = 18.sp)
                            Text(
                                "${activity.name} (${activity.duration}h)",
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF333333),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Time slots
        Text("Schedule:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        slots.forEach { slot ->
            val placed = placements[slot.name] ?: emptyList()
            val totalHours = placed.sumOf { it.duration }
            val isHighlighted = selectedActivity != null

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isHighlighted) Color(0xFF6C5CE7).copy(alpha = 0.1f)
                        else Color(0xFFF8F8F8)
                    )
                    .clickable(enabled = selectedActivity != null) {
                        val act = selectedActivity ?: return@clickable
                        SoundManager.playTap()
                        placements = placements.toMutableMap().also { map ->
                            map[slot.name] = (map[slot.name] ?: emptyList()) + act
                        }
                        unplaced = unplaced - act
                        selectedActivity = null
                        validationErrors = emptyList()
                    }
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            slot.name,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6C5CE7),
                            fontSize = 14.sp
                        )
                        Text(
                            "${totalHours}/${slot.maxHours}h",
                            fontSize = 12.sp,
                            color = if (totalHours > slot.maxHours) Color(0xFFC62828) else Color.Gray
                        )
                    }
                    if (placed.isNotEmpty()) {
                        placed.forEach { act ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(act.emoji, fontSize = 14.sp)
                                Text(
                                    "${act.name} (${act.duration}h)",
                                    fontSize = 13.sp,
                                    color = Color(0xFF333333)
                                )
                            }
                        }
                    } else {
                        Text("Empty", fontSize = 12.sp, color = Color.LightGray)
                    }
                }
            }
        }

        // Validation errors
        validationErrors.forEach { error ->
            Text(
                "⚠️ $error",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828)
            )
        }

        // Submit button
        if (!submitted) {
            Button(
                onClick = {
                    SoundManager.playTap()
                    val errors = validateSchedule()
                    if (errors.isEmpty()) {
                        submitted = true
                        showSuccess = true
                        SoundManager.playSuccess()
                    } else {
                        SoundManager.playError()
                        validationErrors = errors
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("✓ Submit Schedule", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Win check
        LaunchedEffect(showSuccess) {
            if (showSuccess) {
                delay(1500)
                onSuccess()
            }
        }

        // Success
        AnimatedVisibility(
            visible = showSuccess,
            enter = scaleIn(animationSpec = tween(500)) + fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF90EE90))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "🎉 Perfect Schedule!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text("All activities fit perfectly!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
