package com.kidsroutine.feature.family.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.UserModel
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.filled.ArrowForward


private val GradientStart = Color(0xFFFF6B35)
private val GradientEnd = Color(0xFFFFD93D)
private val BgLight = Color(0xFFFFFBF0)
private val TextDark = Color(0xFF2D3436)
private val AccentBlue = Color(0xFF4A90E2)

@Composable
fun ParentDashboardScreen(
    currentUser: UserModel,
    onInviteClick: () -> Unit,
    onTasksClick: () -> Unit,
    onPendingClick: () -> Unit,
    onChallengesClick: () -> Unit,
    onMarketplaceClick: () -> Unit,
    onPublishClick: () -> Unit,
    onModerationClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onFamilyMessagingClick: () -> Unit,
    onProfileClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onGenerationClick: () -> Unit,
    onWeeklyPlanClick: () -> Unit,
    onUpgradeClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    onContentPacksClick: () -> Unit = {},
    viewModel: ParentDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var showCopiedToast by remember { mutableStateOf(false) }

    // Load family on first launch
    LaunchedEffect(currentUser.familyId) {
        if (currentUser.familyId.isNotEmpty()) {
            viewModel.loadFamily(currentUser.familyId)
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
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "👨‍👩‍👧",
                        fontSize = 40.sp
                    )
                    Text(
                        text = "Welcome Back!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = currentUser.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                // Profile and Settings buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Button
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick  = onNotificationsClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint     = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (unreadNotificationCount > 0) {
                            Surface(
                                shape    = CircleShape,
                                color    = Color(0xFFFF6B35),
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text       = if (unreadNotificationCount > 9) "9+" else "$unreadNotificationCount",
                                        color      = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 8.sp
                                    )
                                }
                            }
                        }
                    }

                    // Settings Button
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            if (uiState.isLoading) {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                // Error state
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 40.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            } else if (uiState.family != null) {
                // Family info card
                FamilyInfoCard(
                    family = uiState.family!!,
                    inviteCode = uiState.inviteCode,
                    onCopyInviteCode = {
                        clipboardManager.setText(AnnotatedString(uiState.inviteCode))
                        showCopiedToast = true
                    }
                )

                Spacer(Modifier.height(24.dp))

                // Family stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        emoji = "⭐",
                        label = "Family XP",
                        value = uiState.family!!.familyXp.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji = "🔥",
                        label = "Streak",
                        value = uiState.family!!.familyStreak.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji = "👥",
                        label = "Members",
                        value = uiState.family!!.memberIds.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))


                // SmartSuggestionsCard
                SmartSuggestionsCard(
                    family           = uiState.family!!,
                    currentUser      = currentUser,
                    aiQuotaUsed      = 0,   // TODO Batch 5: load from ai_quotas
                    aiQuotaLimit     = 3,
                    onGenerateClick  = onGenerationClick,
                    onChallengesClick = onChallengesClick,
                    onWeeklyPlanClick = onWeeklyPlanClick
                )

                Spacer(Modifier.height(16.dp))

                // Weekly plan card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWeeklyPlanClick() },
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("📅", fontSize = 28.sp)
                            Column {
                                Text(
                                    "📅 Plan the Whole Week",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = TextDark
                                )
                                Text(
                                    "AI 7-day family schedule ⭐ PRO",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Open",
                            tint     = Color(0xFF11998E),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Members section
                if (uiState.family!!.memberIds.isNotEmpty()) {
                    Text(
                        text = "Family Members",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(Modifier.height(12.dp))

                    MembersListCard(memberIds = uiState.family!!.memberIds)

                    Spacer(Modifier.height(24.dp))
                }

                // ──────── CREATE AI TASKS (NEW) ────────
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGenerationClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✨ Create Tasks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "AI-powered task generation",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Open",
                            tint = AccentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── PRO upgrade banner (only shown for FREE users) ────────────────────
                if (currentUser.role == com.kidsroutine.core.model.Role.PARENT) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable { onUpgradeClick() },
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E3F))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "👑 Upgrade to PRO",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700),
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Unlock story arcs, unlimited AI & more",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Upgrade",
                                tint = Color(0xFFFFD700)
                            )
                        }
                    }
                }

                // Action buttons
                ActionButtonsSection(
                    onInviteClick = onInviteClick,
                    onTasksClick = onTasksClick,
                    onPendingClick = onPendingClick,
                    onChallengesClick = onChallengesClick,
                    onMarketplaceClick = onMarketplaceClick,
                    onPublishClick = onPublishClick,
                    onModerationClick = onModerationClick,
                    onLeaderboardClick = onLeaderboardClick,
                    onFamilyMessagingClick = onFamilyMessagingClick,
                    onStatsClick = onStatsClick,
                    currentUser = currentUser
                )

                Spacer(Modifier.height(32.dp))
            }
        }

        // Copy toast
        if (showCopiedToast) {
            LaunchedEffect(Unit) {
                showCopiedToast = true
                kotlinx.coroutines.delay(2000)
                showCopiedToast = false
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "✓ Invite code copied!",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun FamilyInfoCard(
    family: com.kidsroutine.core.model.FamilyModel,
    inviteCode: String,
    onCopyInviteCode: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = family.familyName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(Modifier.height(16.dp))

            // Invite code section
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = inviteCode.isNotEmpty()) { onCopyInviteCode() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Invite Code",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = inviteCode.ifEmpty { "Loading..." },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = GradientStart,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Share this code with your children to invite them",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun StatCard(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun MembersListCard(memberIds: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            memberIds.forEach { memberId ->
                var memberName by remember { mutableStateOf("Loading...") }
                var memberIsOnline by remember { mutableStateOf(false) }

                // ← CHANGE THIS: Use snapshot listener instead of .get()
                LaunchedEffect(memberId) {
                    try {
                        // Subscribe to real-time updates
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(memberId)
                            .addSnapshotListener { snapshot, error ->
                                if (snapshot != null && snapshot.exists()) {
                                    memberName = snapshot.getString("displayName") ?: "Family Member"
                                    memberIsOnline = snapshot.getBoolean("isOnline") ?: false
                                    Log.d("MembersListCard", "$memberName isOnline: $memberIsOnline")
                                } else if (error != null) {
                                    Log.e("MembersListCard", "Error listening to member", error)
                                    memberName = "Family Member"
                                }
                            }
                    } catch (e: Exception) {
                        memberName = "Family Member"
                        Log.e("MembersListCard", "Exception in listener setup", e)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar
                    Surface(
                        modifier = Modifier
                            .size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = GradientStart.copy(alpha = 0.2f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text("👤", fontSize = 20.sp)
                        }
                    }

                    // Member info
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = memberName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                            // Online/Offline Badge
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = if (memberIsOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Text(
                                text = if (memberIsOnline) "Online" else "Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (memberIsOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Stats Icon
                    IconButton(
                        onClick = { /* Navigate to child stats */ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "View Stats",
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onInviteClick: () -> Unit,
    onTasksClick: () -> Unit,
    onPendingClick: () -> Unit,
    onChallengesClick: () -> Unit,
    onMarketplaceClick: () -> Unit,
    onPublishClick: () -> Unit,
    onModerationClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onFamilyMessagingClick: () -> Unit,
    onStatsClick: () -> Unit,
    currentUser: UserModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onInviteClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GradientStart
            )
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp)
            )
            Text(
                "Invite Children",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Button(
            onClick = onTasksClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue
            )
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp)
            )
            Text(
                "Manage Tasks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Button(
            onClick = onPendingClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800)
            )
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp)
            )
            Text(
                "Child Requests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Button(
            onClick = onChallengesClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0)
            )
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp)
            )
            Text(
                "Challenges",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Button(
            onClick = onMarketplaceClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF667EEA)
            )
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp)
            )
            Text(
                "Community Library",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Button(
            onClick = onPublishClick,  // Add this callback
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6B35)
            )
        ) {
            Icon(
                Icons.Default.CloudUpload,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp)
            )
            Text(
                "Publish Content",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Button(
            onClick = onLeaderboardClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4ECDC4)
            )
        ) {
            Text(
                "🏅 Leaderboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Button(
            onClick = onFamilyMessagingClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEC407A)
            )
        ) {
            Icon(
                Icons.Default.Message,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp)
            )
            Text(
                "Family Chat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Moderation button (admin only)
        if (currentUser.isAdmin) {
            Button(
                onClick = onModerationClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)  // Red for admin
                )
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    "🛡️ Moderation Panel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}