package com.kidsroutine.feature.challenges.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.kidsroutine.core.model.ChallengeModel
import com.kidsroutine.core.model.ChallengeProgress
import com.kidsroutine.core.model.ChallengeStatus
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.core.common.util.DateUtils

private val GradientStart = Color(0xFF6C5CE7)
private val GradientEnd = Color(0xFFA29BFE)
private val BgLight = Color(0xFFFFFBF0)
private val TextDark = Color(0xFF2D3436)
private val SuccessGreen = Color(0xFF4CAF50)
private val WarningOrange = Color(0xFFFF9800)
private val ErrorRed = Color(0xFFF44336)

private fun getCategoryEmoji(category: com.kidsroutine.core.model.TaskCategory): String {
    return when (category) {
        com.kidsroutine.core.model.TaskCategory.MORNING_ROUTINE -> "\uD83C\uDF05"
        com.kidsroutine.core.model.TaskCategory.LEARNING -> "\uD83D\uDCDA"
        com.kidsroutine.core.model.TaskCategory.CHORES -> "\uD83E\uDDF9"
        com.kidsroutine.core.model.TaskCategory.HEALTH -> "\uD83C\uDFC3"
        com.kidsroutine.core.model.TaskCategory.CREATIVITY -> "\uD83C\uDFA8"
        com.kidsroutine.core.model.TaskCategory.SOCIAL -> "\uD83D\uDC65"
        com.kidsroutine.core.model.TaskCategory.FAMILY -> "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67"
        com.kidsroutine.core.model.TaskCategory.OUTDOOR -> "\uD83C\uDF33"
        com.kidsroutine.core.model.TaskCategory.SLEEP -> "\uD83D\uDE34"
        com.kidsroutine.core.model.TaskCategory.SCREEN_TIME -> "\uD83D\uDCF1"
    }
}

@Composable
fun ChallengeDetailScreen(
    currentUser: UserModel,
    challengeId: String,
    onBackClick: () -> Unit,
    viewModel: ChallengeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(challengeId) {
        viewModel.loadChallengeDetail(currentUser.userId, currentUser.familyId, challengeId)
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(1500)
            viewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        // Gradient background header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── FIX 1: statusBarsPadding() pushes header below the status bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
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
                    text = "Challenge Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(40.dp))
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("\u26A0\uFE0F", fontSize = 40.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            uiState.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDark
                        )
                    }
                }
            } else if (uiState.challenge != null && uiState.progress != null) {
                val challenge = uiState.challenge!!
                val progress = uiState.progress!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // ── Challenge header card ──────────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GradientStart.copy(alpha = 0.2f),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(getCategoryEmoji(challenge.category), fontSize = 28.sp)
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = challenge.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = challenge.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (progress.status) {
                                    ChallengeStatus.ACTIVE    -> Color(0xFFE3F2FD)
                                    ChallengeStatus.COMPLETED -> Color(0xFFE8F5E9)
                                    ChallengeStatus.FAILED    -> Color(0xFFFFEBEE)
                                    else                      -> Color(0xFFF3E5F5)
                                }
                            ) {
                                Text(
                                    text = when (progress.status) {
                                        ChallengeStatus.ACTIVE    -> "\uD83D\uDD25 Active"
                                        ChallengeStatus.COMPLETED -> "\uD83C\uDF89 Completed"
                                        ChallengeStatus.FAILED    -> "\u26A0\uFE0F Failed"
                                        else -> "\u23F8\uFE0F ${progress.status.name}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // ── Progress card ─────────────────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )

                            // ── FIX 2: No fixed height — cards grow to fit content ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Streak
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFCDD2),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp, horizontal = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("\uD83D\uDD25", fontSize = 28.sp)
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = progress.currentStreak.toString(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC62828)
                                        )
                                        Text(
                                            text = "Day Streak",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFC62828),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Completed days
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE8F5E9),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp, horizontal = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("\u2705", fontSize = 28.sp)
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = progress.completedDays.toString(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Text(
                                            text = "Completed",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF2E7D32),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Progress bar
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        // ── FIX 4: show completedDays/totalDays, not currentDay ──
                                        text = "Day ${progress.completedDays}/${progress.totalDays}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "${String.format("%.0f", progress.successRate)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GradientStart
                                    )
                                }

                                LinearProgressIndicator(
                                    // ── FIX 4: use completedDays not currentDay ──
                                    progress = { (progress.completedDays.toFloat() / progress.totalDays).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = GradientStart,
                                    trackColor = Color(0xFFEEEEEE)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF3E5F5)
                            ) {
                                Text(
                                    text = "Success Rate: ${String.format("%.1f", progress.successRate)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6A1B9A),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // ── Rewards card ──────────────────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Rewards",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFE082).copy(alpha = 0.3f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("\u2B50", fontSize = 20.sp)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "${challenge.dailyXpReward}/day",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF81C784).copy(alpha = 0.3f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("\uD83C\uDF81", fontSize = 20.sp)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "+${challenge.completionBonusXp}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE57373).copy(alpha = 0.3f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("\uD83D\uDD25", fontSize = 20.sp)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "+${challenge.streakBonusXp}/day",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── FIX 3: Last 7 Days — only highlight dates within challenge range ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Last 7 Days",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val today = DateUtils.todayString()
                                repeat(7) { i ->
                                    val date = DateUtils.addDays(today, -6 + i)

                                    // Only treat a date as part of this challenge if it
                                    // falls within startDate..endDate
                                    val withinRange = progress.startDate.isNotEmpty() &&
                                            date >= progress.startDate &&
                                            (progress.endDate.isEmpty() || date <= progress.endDate)

                                    val completed    = withinRange && progress.dailyProgress[date] == true
                                    val isFuture     = withinRange && date > today
                                    val isBeforeStart = date < progress.startDate.ifEmpty { today }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            completed     -> GradientStart         // filled purple
                                            isFuture      -> Color(0xFFD1C4E9)    // light purple (upcoming)
                                            isBeforeStart -> Color(0xFFF5F5F5)    // blank (outside range)
                                            else          -> Color(0xFFEEEEEE)    // grey circle (in range, not done)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when {
                                                    completed     -> "\u2713"
                                                    isFuture      -> "\u00B7"
                                                    isBeforeStart -> ""
                                                    else          -> "\u25CB"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    completed -> Color.White
                                                    isFuture  -> Color(0xFF7E57C2)
                                                    else      -> Color.Gray
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Action buttons (only shown when ACTIVE) ───────────────────
                    if (progress.status == ChallengeStatus.ACTIVE) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.completeDayToday(currentUser.userId, currentUser.familyId)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.completedToday) Color(0xFFEEEEEE) else SuccessGreen
                                ),
                                enabled = !uiState.completedToday
                            ) {
                                Text(
                                    if (uiState.completedToday) "\u2713 Done Today" else "Complete Today",
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.completedToday) Color.Gray else Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.skipDayToday(currentUser.userId, currentUser.familyId)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WarningOrange.copy(alpha = 0.8f)
                                )
                            ) {
                                Text(
                                    "Skip Today",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }

            // Success/completion message banner
            AnimatedVisibility(visible = uiState.successMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = uiState.successMessage ?: "",
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
