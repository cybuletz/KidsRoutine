package com.kidsroutine.feature.daily.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.achievements.ui.AchievementsScreen
import com.kidsroutine.feature.challenges.ui.ActiveChallengesScreen
import com.kidsroutine.feature.community.ui.LeaderboardScreen
import androidx.compose.material.icons.filled.Language
import com.kidsroutine.feature.moments.ui.MomentsScreen


private val OrangePrimary = Color(0xFFFF6B35)
private val BgLight = Color(0xFFFFFBF0)

@Composable
fun ChildMainScreen(
    currentUser: UserModel,
    onTaskClick: (com.kidsroutine.core.model.TaskInstance) -> Unit,
    onFamilyMessagingClick: () -> Unit,
    parentNavController: NavController
) {
    val innerNavController = rememberNavController()
    var currentRoute by remember { mutableStateOf("daily") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Navigation content
        NavHost(
            navController = innerNavController,
            startDestination = "daily",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("daily") {
                currentRoute = "daily"
                DailyScreen(
                    currentUser = currentUser,
                    onTaskClick = onTaskClick,
                    onChallengesClick = { innerNavController.navigate("challenges") },
                    onAchievementsClick = { innerNavController.navigate("achievements") },
                    onFamilyMessagingClick = onFamilyMessagingClick,
                    onStatsClick = { innerNavController.navigate("leaderboard") },
                    onProfileClick = { parentNavController.navigate(Routes.CHILD_PROFILE) }
                )
            }

            composable("challenges") {
                currentRoute = "challenges"
                ActiveChallengesScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") },
                    onStartChallengeClick = { innerNavController.navigate("challenges") },
                    onChallengeClick = { /* Handle challenge click */ }
                )
            }

            composable("leaderboard") {
                currentRoute = "leaderboard"
                LeaderboardScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }

            composable("achievements") {
                currentRoute = "achievements"
                AchievementsScreen(
                    currentUser = currentUser,
                    onBackClick = { innerNavController.navigate("daily") }
                )
            }

            composable("world") {
                currentRoute = "world"
                // World is a full-screen experience launched via parent nav
                LaunchedEffect(Unit) {
                    innerNavController.popBackStack()
                    parentNavController.navigate(Routes.WORLD)
                }
                Box(modifier = Modifier.fillMaxSize())
            }

            composable("moments") {
                currentRoute = "moments"
                MomentsScreen(
                    currentUser  = currentUser,
                    onBackClick  = { innerNavController.navigate("daily") }
                )
            }
        }

        // Persistent Navigation Bar
        PersistentNavBar(
            currentRoute = currentRoute,
            currentUser = currentUser,
            onDailyClick = { innerNavController.navigate("daily") { popUpTo("daily") } },
            onChallengesClick = { innerNavController.navigate("challenges") { popUpTo("daily") } },
            onLeaderboardClick = { innerNavController.navigate("leaderboard") { popUpTo("daily") } },
            onAchievementsClick = { innerNavController.navigate("achievements") { popUpTo("daily") } },
            onChatClick = onFamilyMessagingClick
        )
    }
}

@Composable
private fun PersistentNavBar(
    currentRoute: String,
    currentUser: UserModel,
    onDailyClick: () -> Unit,
    onChallengesClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onWorldClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onMomentsClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 8.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItemButton(
                    icon = Icons.Default.Home,
                    label = "Daily",
                    isSelected = currentRoute == "daily",
                    onClick = onDailyClick,
                    modifier = Modifier.weight(1f)
                )

                NavBarItemButton(
                    icon = Icons.Default.EmojiEvents,
                    label = "Challenges",
                    isSelected = currentRoute == "challenges",
                    onClick = onChallengesClick,
                    modifier = Modifier.weight(1f)
                )

                NavBarItemButton(
                    icon = Icons.Default.BarChart,
                    label = "Leaderboard",
                    isSelected = currentRoute == "leaderboard",
                    onClick = onLeaderboardClick,
                    modifier = Modifier.weight(1f)
                )

                NavBarItemButton(
                    icon = Icons.Default.Language,
                    label = "World",
                    isSelected = currentRoute == "world",
                    onClick = onWorldClick,
                    modifier = Modifier.weight(1f)
                )

                NavBarItemButton(
                    icon      = Icons.Default.PhotoAlbum,
                    label     = "Moments",
                    isSelected = currentRoute == "moments",
                    onClick    = onMomentsClick,
                    modifier   = Modifier.weight(1f)
                )

                Box(modifier = Modifier.weight(1f)) {
                    NavBarItemButton(
                        icon = Icons.Default.EmojiEvents,
                        label = "Achievements",
                        isSelected = currentRoute == "achievements",
                        onClick = onAchievementsClick
                    )
                    if (currentUser.badges.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF6B35),
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "${currentUser.badges.size}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Chat Bubble
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-78).dp)
                .zIndex(10f)
                .navigationBarsPadding()
        ) {
            Button(
                onClick = onChatClick,
                modifier = Modifier
                    .size(width = 56.dp, height = 50.dp),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 2.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEC407A)
                ),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    Icons.Default.Message,
                    contentDescription = "Chat",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun NavBarItemButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(0.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) OrangePrimary else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) OrangePrimary else Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 10.sp
        )
    }
}