package com.kidsroutine.feature.challenges.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import com.kidsroutine.core.model.ChallengeModel
import com.kidsroutine.core.model.ChallengeProgress
import com.kidsroutine.core.model.UserModel
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.ChallengeStatus
import kotlinx.coroutines.tasks.await

private val GradientStart = Color(0xFF6C5CE7)
private val GradientEnd = Color(0xFFA29BFE)
private val BgLight = Color(0xFFFFFBF0)
private val TextDark = Color(0xFF2D3436)

@Composable
fun ActiveChallengesScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit,
    onStartChallengeClick: () -> Unit,
    onChallengeClick: (ChallengeModel) -> Unit,
    viewModel: ActiveChallengesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.userId) {
        viewModel.loadActiveChallenges(currentUser.userId)
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
                    text = "Active Challenges",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onStartChallengeClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Start Challenge",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                        Text(
                            uiState.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDark
                        )
                    }
                }
            } else if (uiState.activeChallenges.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎯", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No Active Challenges",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Start a challenge to build healthy habits!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onStartChallengeClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Start Challenge", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.activeChallenges) { (challenge, progress) ->
                        ChallengeCard(
                            challenge     = challenge,
                            progress      = progress,
                            currentUserId = currentUser.userId,
                            onClick       = { onChallengeClick(challenge) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: ChallengeModel,
    progress: ChallengeProgress,
    currentUserId: String = "",
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Category icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GradientStart.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(getCategoryEmoji(challenge.category), fontSize = 24.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = challenge.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }
            }

            // Progress section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Streak badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFCDD2)
                    ) {
                        Text(
                            text = "🔥 ${progress.currentStreak} day streak",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = "⭐ +${challenge.dailyXpReward} XP/day",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Progress bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Day ${progress.currentDay}/${progress.totalDays}",
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
                        progress = { (progress.currentDay.toFloat() / progress.totalDays).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GradientStart,
                        trackColor = Color(0xFFEEEEEE)
                    )
                }
            }

            CoopProgressSection(
                challenge     = challenge,
                myProgress    = progress,
                currentUserId = currentUserId
            )

            // Action button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
            ) {
                Text(
                    "View Details",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private fun getCategoryEmoji(category: com.kidsroutine.core.model.TaskCategory): String {
    return when (category) {
        com.kidsroutine.core.model.TaskCategory.MORNING_ROUTINE -> "🌅"
        com.kidsroutine.core.model.TaskCategory.LEARNING -> "📚"
        com.kidsroutine.core.model.TaskCategory.CHORES -> "🧹"
        com.kidsroutine.core.model.TaskCategory.HEALTH -> "🏃"
        com.kidsroutine.core.model.TaskCategory.CREATIVITY -> "🎨"
        com.kidsroutine.core.model.TaskCategory.SOCIAL -> "👥"
        com.kidsroutine.core.model.TaskCategory.FAMILY -> "👨‍👩‍👧"
        com.kidsroutine.core.model.TaskCategory.OUTDOOR -> "🌳"
        com.kidsroutine.core.model.TaskCategory.SLEEP -> "😴"
        com.kidsroutine.core.model.TaskCategory.SCREEN_TIME -> "📱"
    }
}

/**
 * Shows two participants' progress bars side by side for co-op challenges.
 * Reads other participant's progress live from Firestore.
 */
@Composable
fun CoopProgressSection(
    challenge: ChallengeModel,
    myProgress: ChallengeProgress,
    currentUserId: String
) {
    if (!challenge.isCoOp) return

    // Load partner progress
    var partnerName     by remember { mutableStateOf("Partner") }
    var partnerProgress by remember { mutableStateOf<ChallengeProgress?>(null) }

    LaunchedEffect(challenge.challengeId) {
        try {
            val firestore = FirebaseFirestore.getInstance()

            // Find the other participant (parentId or childId)
            val partnerId = when {
                challenge.parentId.isNotEmpty() && challenge.parentId != currentUserId -> challenge.parentId
                challenge.childId.isNotEmpty()  && challenge.childId  != currentUserId -> challenge.childId
                else -> return@LaunchedEffect
            }

            val userDoc = firestore.collection("users").document(partnerId).get().await()
            partnerName = userDoc.getString("displayName") ?: "Partner"

            val progressDoc = firestore
                .collection("users").document(partnerId)
                .collection("challenge_progress").document(challenge.challengeId)
                .get().await()

            if (progressDoc.exists()) {
                partnerProgress = ChallengeProgress(
                    challengeId  = challenge.challengeId,
                    userId       = partnerId,
                    currentDay   = (progressDoc.getLong("currentDay")?.toInt())  ?: 0,
                    totalDays    = (progressDoc.getLong("totalDays")?.toInt())   ?: challenge.duration,
                    completedDays= (progressDoc.getLong("completedDays")?.toInt()) ?: 0,
                    currentStreak= (progressDoc.getLong("currentStreak")?.toInt()) ?: 0,
                    successRate  = (progressDoc.getDouble("successRate")?.toFloat()) ?: 0f,
                    status       = try { ChallengeStatus.valueOf(progressDoc.getString("status") ?: "ACTIVE") }
                    catch (_: Exception) { ChallengeStatus.ACTIVE }
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("CoopProgressSection", "Error loading partner progress", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // Co-op badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF9B59B6).copy(alpha = 0.15f),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "🤝 Co-op Challenge",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9B59B6),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        // Side-by-side progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // MY progress
            ParticipantProgressBar(
                label    = "You",
                progress = myProgress.currentDay.toFloat() / myProgress.totalDays.coerceAtLeast(1),
                days     = "${myProgress.completedDays}/${myProgress.totalDays}",
                color    = Color(0xFF667EEA),
                modifier = Modifier.weight(1f)
            )
            // PARTNER progress
            ParticipantProgressBar(
                label    = partnerName,
                progress = partnerProgress?.let {
                    it.currentDay.toFloat() / it.totalDays.coerceAtLeast(1)
                } ?: 0f,
                days     = partnerProgress?.let { "${it.completedDays}/${it.totalDays}" } ?: "0/-",
                color    = Color(0xFFFF6B35),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ParticipantProgressBar(
    label: String,
    progress: Float,
    days: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue   = progress.coerceIn(0f, 1f),
        animationSpec = androidx.compose.animation.core.tween(800),
        label         = "participantProgress"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 1
            )
            Text(
                text = days,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color      = color,
            trackColor = color.copy(alpha = 0.2f)
        )
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}