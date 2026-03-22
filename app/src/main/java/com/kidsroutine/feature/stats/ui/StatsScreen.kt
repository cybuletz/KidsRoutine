package com.kidsroutine.feature.stats.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.stats.data.UserStatsModel

private val GradientStart = Color(0xFF667EEA)
private val GradientEnd = Color(0xFF764BA2)
private val BgLight = Color(0xFFFFFBF0)

@Composable
fun StatsScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.userId) {
        Log.d("StatsScreen", "Loading stats for user: ${currentUser.userId}")
        viewModel.loadUserStats(currentUser.userId)
        viewModel.loadMonthlyProgress(currentUser.userId)
        if (currentUser.familyId.isNotEmpty()) {
            viewModel.loadFamilyStats(currentUser.familyId)
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
                    text = "📊 Your Progress",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(40.dp))
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
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error!!, color = Color.Red)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // User Stats Summary
                    item {
                        uiState.userStats?.let { stats ->
                            StatsSummaryCard(stats)
                        }
                    }

                    // Weekly Progress
                    item {
                        WeeklyProgressCard(uiState.weeklyProgress)
                    }

                    // Monthly Progress
                    item {
                        MonthlyProgressCard(uiState.monthlyProgress)
                    }

                    // Family Stats (if available)
                    item {
                        uiState.familyStats?.let { familyStats ->
                            FamilyStatsCard(familyStats)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsSummaryCard(stats: UserStatsModel) {
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stats.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Level ${stats.level}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "⭐",
                    fontSize = 40.sp
                )
            }

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    label = "Total XP",
                    value = "${stats.totalXp}",
                    icon = "⭐",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Streak",
                    value = "${stats.currentStreak}",
                    icon = "🔥",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Tasks",
                    value = "${stats.tasksCompleted}",
                    icon = "✓",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Badges",
                    value = "${stats.badgesUnlocked}",
                    icon = "🏆",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(icon, fontSize = 24.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
private fun WeeklyProgressCard(dailyXp: List<Int>) {
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
                text = "📈 This Week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (dailyXp.isEmpty()) {
                Text("No data yet", color = Color.Gray)
            } else {
                // Simple bar chart representation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxXp = dailyXp.maxOrNull() ?: 1
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                    dailyXp.forEachIndexed { index, xp ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((xp.toFloat() / maxXp * 120).dp)
                                    .background(
                                        Color(0xFF667EEA),
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            // Day label
                            Text(
                                text = days.getOrNull(index) ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Summary
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE3F2FD)
                ) {
                    Text(
                        text = "Total this week: ${dailyXp.sum()} XP",
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyProgressCard(weeklyXp: List<Int>) {
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
                text = "📅 This Month",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (weeklyXp.isEmpty()) {
                Text("No data yet", color = Color.Gray)
            } else {
                // Simple bar chart representation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxXp = weeklyXp.maxOrNull() ?: 1
                    val weeks = listOf("W1", "W2", "W3", "W4")

                    weeklyXp.forEachIndexed { index, xp ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((xp.toFloat() / maxXp * 100).dp)
                                    .background(
                                        Color(0xFF764BA2),
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            // Week label
                            Text(
                                text = weeks.getOrNull(index) ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Summary
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF3E5F5)
                ) {
                    Text(
                        text = "Total this month: ${weeklyXp.sum()} XP",
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2)
                    )
                }
            }
        }
    }
}

@Composable
private fun FamilyStatsCard(familyStats: com.kidsroutine.feature.stats.data.FamilyStatsModel) {
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
                text = "👨‍👩‍👧 ${familyStats.familyName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    label = "Members",
                    value = "${familyStats.memberCount}",
                    icon = "👥",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Family XP",
                    value = "${familyStats.familyXp}",
                    icon = "⭐",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Streak",
                    value = "${familyStats.familyStreak}",
                    icon = "🔥",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}