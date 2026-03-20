package com.kidsroutine.feature.community.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.ChildLeaderboardEntry
import com.kidsroutine.core.model.FamilyLeaderboardEntry
import com.kidsroutine.core.model.ChallengeLeaderboardEntry
import com.kidsroutine.core.model.UserModel

private val GradientStart = Color(0xFFFFD700)
private val GradientEnd = Color(0xFFFF8C00)
private val BgLight = Color(0xFFFFFBF0)
private val TextDark = Color(0xFF2D3436)

@Composable
fun LeaderboardScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLeaderboards()
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
            modifier = Modifier.fillMaxSize()
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
                    text = "🏆 Leaderboards",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(40.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Tab selector
            TabRow(
                selectedTabIndex = uiState.activeTab.ordinal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                containerColor = Color.Transparent,
                divider = {}
            ) {
                LeaderboardTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.activeTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = when (tab) {
                                    LeaderboardTab.CHILDREN -> "👶 Kids"
                                    LeaderboardTab.FAMILIES -> "👨‍👩‍👧 Families"
                                    LeaderboardTab.CHALLENGES -> "🎯 Challenges"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                }
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
                    Text(uiState.error!!, color = Color.Red)
                }
            } else {
                when (uiState.activeTab) {
                    LeaderboardTab.CHILDREN -> ChildLeaderboardContent(uiState.childLeaderboard)
                    LeaderboardTab.FAMILIES -> FamilyLeaderboardContent(uiState.familyLeaderboard)
                    LeaderboardTab.CHALLENGES -> ChallengeLeaderboardContent(uiState.challengeLeaderboard)
                }
            }
        }
    }
}

@Composable
private fun ChildLeaderboardContent(entries: List<ChildLeaderboardEntry>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(entries) { index, entry ->
            ChildLeaderboardRow(entry, index + 1)
        }
    }
}

@Composable
private fun ChildLeaderboardRow(entry: ChildLeaderboardEntry, rank: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (rank) {
                1 -> Color(0xFFFFD700).copy(alpha = 0.15f)  // Gold
                2 -> Color(0xFFC0C0C0).copy(alpha = 0.15f)  // Silver
                3 -> Color(0xFFCD7F32).copy(alpha = 0.15f)  // Bronze
                else -> Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank medal
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFFD700)
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> Color(0xFFEEEEEE)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "$rank"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Child info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Family: ${entry.familyId.take(8)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // XP badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFD93D).copy(alpha = 0.3f)
            ) {
                Text(
                    text = "⭐ ${entry.xp} XP",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun FamilyLeaderboardContent(entries: List<FamilyLeaderboardEntry>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(entries) { index, entry ->
            FamilyLeaderboardRow(entry, index + 1)
        }
    }
}

@Composable
private fun FamilyLeaderboardRow(entry: FamilyLeaderboardEntry, rank: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (rank) {
                1 -> Color(0xFFFFD700).copy(alpha = 0.15f)
                2 -> Color(0xFFC0C0C0).copy(alpha = 0.15f)
                3 -> Color(0xFFCD7F32).copy(alpha = 0.15f)
                else -> Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank medal
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFFD700)
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> Color(0xFFEEEEEE)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "$rank"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Family info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.familyName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "👥 ${entry.memberCount} members",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text("·", color = Color.Gray)
                    Text(
                        text = "🔥 ${entry.streak} day streak",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            // Total XP badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFD93D).copy(alpha = 0.3f)
            ) {
                Text(
                    text = "⭐ ${entry.totalXp}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ChallengeLeaderboardContent(entries: List<ChallengeLeaderboardEntry>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(entries) { index, entry ->
            ChallengeLeaderboardRow(entry, index + 1)
        }
    }
}

@Composable
private fun ChallengeLeaderboardRow(entry: ChallengeLeaderboardEntry, rank: Int) {
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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank number
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            // Challenge info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1
                )
                Text(
                    text = "Completed by ${entry.completedByCount} ${if (entry.completedByCount == 1) "child" else "children"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // Average completion days
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF4ECDC4).copy(alpha = 0.2f)
            ) {
                Text(
                    text = "⏱️ ${String.format("%.1f", entry.averageCompletionDays)}d avg",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4ECDC4),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}