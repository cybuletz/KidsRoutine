package com.kidsroutine.feature.rewards.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.PrivilegeRequestStatus
import com.kidsroutine.core.model.UserModel

private val OrangePrimary = Color(0xFFFF6B35)
private val YellowGold    = Color(0xFFFFD700)
private val BgLight       = Color(0xFFFFFBF0)
private val PurpleAccent  = Color(0xFF9B5DE5)
private val TealAccent    = Color(0xFF4ECDC4)
private val GreenDone     = Color(0xFF06D6A0)

// ── Privilege data model ────────────────────────────────────────────────────
data class Privilege(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val xpCost: Int,
    val category: PrivilegeCategory,
    val isCustom: Boolean = false
)

enum class PrivilegeCategory { SCREEN, FOOD, ACTIVITY, SOCIAL, SLEEP, CUSTOM }

val defaultPrivileges = listOf(
    Privilege("screen_15",    "📱", "+15 min screen time",    "Extra 15 minutes on your device",    50,  PrivilegeCategory.SCREEN),
    Privilege("screen_30",    "📺", "+30 min screen time",    "Half an hour extra on any screen",   90,  PrivilegeCategory.SCREEN),
    Privilege("dinner_choice","🍕", "Pick tonight's dinner",  "You choose what everyone eats!",     80,  PrivilegeCategory.FOOD),
    Privilege("skip_chore",   "🧹", "Skip one chore today",   "One chore gets a free pass",         100, PrivilegeCategory.ACTIVITY),
    Privilege("movie_night",  "🎬", "Pick the movie tonight", "Your choice on movie night",         60,  PrivilegeCategory.SOCIAL),
    Privilege("stay_up",      "🌙", "Stay up 30 min later",   "Bedtime gets pushed back a bit",     120, PrivilegeCategory.SLEEP),
    Privilege("game_hour",    "🎮", "Extra gaming hour",      "One full hour of gaming unlocked",   150, PrivilegeCategory.SCREEN),
    Privilege("outdoor_day",  "🏕️", "Outdoor adventure day",  "Ask for a special outdoor trip!",   200, PrivilegeCategory.ACTIVITY),
    Privilege("sleepover",    "🛏️", "Sleepover request",      "Request a friend sleepover",         300, PrivilegeCategory.SOCIAL),
    Privilege("ice_cream",    "🍦", "Ice cream trip",         "Trip to get ice cream!",             150, PrivilegeCategory.FOOD),
    Privilege("art_supplies", "🎨", "New art supplies",       "Ask for new creative supplies",      250, PrivilegeCategory.CUSTOM),
    Privilege("book_choice",  "📚", "Choose next book",       "Pick the next family read-aloud",    40,  PrivilegeCategory.SOCIAL),
)

// ── EMOJI options for custom privilege picker ───────────────────────────────
private val customEmojiOptions = listOf(
    "🎯","🎪","🎭","🎨","🎬","🎤","🎵","🎶","🎸","🎺","🎻","🥁",
    "🏀","⚽","🏈","🎾","🏊","🚴","🧗","🛹","🎿","🏂","🥋","🎣",
    "🍰","🍩","🍪","🍫","🧁","🍭","🥤","🧃","🍿","🍜",
    "🌈","🦄","🐶","🐱","🦁","🐯","🦊","🐼","🐨","🐸",
    "🚗","✈️","🚀","🛸","🎠","🎡","🎢","🏰","🏖️","🌴",
    "💎","👑","🌟","✨","🎁","🎀","🎊","🎉","🏆","🥇"
)

// ── Main screen ─────────────────────────────────────────────────────────────
@Composable
fun RewardsScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit = {},
    onAvatarShopClick: () -> Unit = {},
    viewModel: RewardsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.userId) {
        viewModel.loadMyRequests(currentUser.userId)
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    var selectedTab       by remember { mutableIntStateOf(0) }
    val tabs = listOf("🛍️ Privileges", "📋 My Requests", "🏆 Achievements", "🎨 Avatar")

    var pendingPrivilege  by remember { mutableStateOf<Privilege?>(null) }
    var showCustomDialog  by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BgLight)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(YellowGold, OrangePrimary)))
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🎁", fontSize = 28.sp)
                            Text(
                                "Reward Shop",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Text(
                            "Spend your XP on real rewards!",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color.White.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("⭐", fontSize = 16.sp)
                                Text(
                                    "${currentUser.xp} XP available",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Tabs ──────────────────────────────────────────────────────
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = OrangePrimary,
                    edgePadding = 0.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick  = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }

            // ── Toast messages ────────────────────────────────────────────
            if (uiState.successMessage != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = GreenDone.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, GreenDone)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("✅", fontSize = 20.sp)
                            Text(uiState.successMessage!!, color = Color(0xFF064E3B), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            if (uiState.errorMessage != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF44336).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFFF44336).copy(alpha = 0.5f))
                    ) {
                        Text(
                            uiState.errorMessage!!,
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(16.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ── Tab content ───────────────────────────────────────────────
            when (selectedTab) {
                0 -> {
                    item {
                        Text(
                            "Privileges",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }
                    items(defaultPrivileges) { privilege ->
                        PrivilegeCard(
                            privilege = privilege,
                            userXp    = currentUser.xp,
                            onSpend   = { pendingPrivilege = it }
                        )
                    }
                    // ── Custom privilege card at the bottom ───────────────
                    item { Spacer(Modifier.height(8.dp)) }
                    item {
                        Card(
                            modifier  = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .clickable { showCustomDialog = true },
                            shape     = RoundedCornerShape(20.dp),
                            colors    = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            border    = BorderStroke(1.5.dp, OrangePrimary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier  = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(OrangePrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("➕", fontSize = 26.sp)
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        "Create your own request",
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 15.sp,
                                        color      = Color(0xFF2D3436)
                                    )
                                    Text("Have something specific in mind? Ask for it!", fontSize = 12.sp, color = Color.Gray)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OrangePrimary)
                            }
                        }
                    }
                }
                1 -> {
                    if (uiState.isLoading) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = OrangePrimary)
                            }
                        }
                    } else if (uiState.myRequests.isEmpty()) {
                        item { MyRequestsEmpty() }
                    } else {
                        items(uiState.myRequests, key = { it.requestId }) { req ->
                            MyRequestCard(
                                req      = req,
                                onCancel = { viewModel.cancelRequest(it.requestId) }
                            )
                        }
                    }
                }
                2 -> { item { AchievementsGrid(currentUser) } }
                3 -> { item { AvatarShortcut(onAvatarShopClick) } }
            }
        }

        // ── Confirmation dialog (default privileges) ──────────────────────
        pendingPrivilege?.let { priv ->
            PrivilegePurchaseDialog(
                privilege = priv,
                userXp    = currentUser.xp,
                onConfirm = {
                    viewModel.requestPrivilege(
                        familyId  = currentUser.familyId,
                        userId    = currentUser.userId,
                        childName = currentUser.displayName,
                        privilege = priv
                    )
                    pendingPrivilege = null
                    selectedTab = 1
                },
                onDismiss = { pendingPrivilege = null }
            )
        }

        // ── Custom privilege creation dialog ──────────────────────────────
        if (showCustomDialog) {
            CustomPrivilegeDialog(
                userXp    = currentUser.xp,
                onConfirm = { customPrivilege ->
                    viewModel.requestPrivilege(
                        familyId  = currentUser.familyId,
                        userId    = currentUser.userId,
                        childName = currentUser.displayName,
                        privilege = customPrivilege
                    )
                    showCustomDialog = false
                    selectedTab = 1
                },
                onDismiss = { showCustomDialog = false }
            )
        }
    }
}

// ── Custom privilege creation dialog ────────────────────────────────────────
@Composable
private fun CustomPrivilegeDialog(
    userXp: Int,
    onConfirm: (Privilege) -> Unit,
    onDismiss: () -> Unit
) {
    var title       by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var xpCost      by remember { mutableIntStateOf(50) }
    var selectedEmoji by remember { mutableStateOf("🎯") }
    var showEmojiPicker by remember { mutableStateOf(false) }

    val canSubmit = title.isNotBlank() && xpCost in 10..500 && userXp >= xpCost

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.fillMaxWidth()
            ) {
                Text("✏️ Create Your Request", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier            = Modifier.fillMaxWidth()
            ) {
                // Emoji picker row
                Text("Pick an emoji:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                if (!showEmojiPicker) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(OrangePrimary.copy(alpha = 0.08f))
                            .clickable { showEmojiPicker = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(selectedEmoji, fontSize = 32.sp)
                        Text("Tap to change", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.Edit, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                    }
                } else {
                    // Grid of emoji options
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        customEmojiOptions.chunked(8).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                row.forEach { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (emoji == selectedEmoji) OrangePrimary.copy(alpha = 0.2f) else Color.Transparent)
                                            .clickable {
                                                selectedEmoji = emoji
                                                showEmojiPicker = false
                                            }
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Title field
                OutlinedTextField(
                    value         = title,
                    onValueChange = { if (it.length <= 40) title = it },
                    label         = { Text("What do you want?") },
                    placeholder   = { Text("e.g. Trip to the park") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        focusedLabelColor  = OrangePrimary
                    )
                )

                // Description field
                OutlinedTextField(
                    value         = description,
                    onValueChange = { if (it.length <= 100) description = it },
                    label         = { Text("Details (optional)") },
                    placeholder   = { Text("Tell your parent more about it") },
                    maxLines      = 2,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        focusedLabelColor  = OrangePrimary
                    )
                )

                // XP cost slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("XP to spend:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Text(
                            "⭐ $xpCost XP",
                            fontWeight = FontWeight.Bold,
                            color      = if (userXp >= xpCost) OrangePrimary else Color(0xFFF44336),
                            fontSize   = 13.sp
                        )
                    }
                    Slider(
                        value         = xpCost.toFloat(),
                        onValueChange = { xpCost = it.toInt() },
                        valueRange    = 10f..minOf(500f, userXp.toFloat().coerceAtLeast(10f)),
                        steps         = 48,  // steps of ~10 XP
                        colors        = SliderDefaults.colors(
                            thumbColor       = OrangePrimary,
                            activeTrackColor = OrangePrimary
                        )
                    )
                    if (userXp < xpCost) {
                        Text("You don't have enough XP for this amount", fontSize = 11.sp, color = Color(0xFFF44336))
                    } else {
                        Text("After request: ${userXp - xpCost} XP remaining", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = {
                    if (canSubmit) {
                        onConfirm(
                            Privilege(
                                id          = "custom_${System.currentTimeMillis()}",
                                emoji       = selectedEmoji,
                                title       = title.trim(),
                                description = description.trim().ifBlank { "Custom request" },
                                xpCost      = xpCost,
                                category    = PrivilegeCategory.CUSTOM,
                                isCustom    = true
                            )
                        )
                    }
                },
                enabled  = canSubmit,
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✅ Send Request!", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}

// ── Privilege card ──────────────────────────────────────────────────────────
@Composable
private fun PrivilegeCard(
    privilege: Privilege,
    userXp: Int,
    onSpend: (Privilege) -> Unit
) {
    val canAfford = userXp >= privilege.xpCost
    val categoryColor = when (privilege.category) {
        PrivilegeCategory.SCREEN   -> Color(0xFF4361EE)
        PrivilegeCategory.FOOD     -> Color(0xFFFF9F1C)
        PrivilegeCategory.ACTIVITY -> GreenDone
        PrivilegeCategory.SOCIAL   -> PurpleAccent
        PrivilegeCategory.SLEEP    -> TealAccent
        PrivilegeCategory.CUSTOM   -> OrangePrimary
    }

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (canAfford) Color.White else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(if (canAfford) 3.dp else 1.dp),
        border    = if (!canAfford) BorderStroke(1.dp, Color.LightGray) else null
    ) {
        Row(
            modifier  = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = if (canAfford) 0.15f else 0.07f)),
                contentAlignment = Alignment.Center
            ) {
                Text(privilege.emoji, fontSize = 26.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    privilege.title,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    color      = if (canAfford) Color(0xFF2D3436) else Color.Gray
                )
                Text(privilege.description, fontSize = 12.sp, color = Color.Gray)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("⭐", fontSize = 13.sp)
                    Text(
                        "${privilege.xpCost} XP",
                        fontWeight = FontWeight.Bold,
                        color      = if (canAfford) OrangePrimary else Color.LightGray,
                        fontSize   = 13.sp
                    )
                    if (!canAfford) {
                        Text(
                            "· Need ${privilege.xpCost - userXp} more",
                            fontSize = 11.sp,
                            color    = Color.LightGray
                        )
                    }
                }
            }
            Button(
                onClick  = { onSpend(privilege) },
                enabled  = canAfford,
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = categoryColor,
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    if (canAfford) "Spend" else "🔒",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp,
                    color      = Color.White
                )
            }
        }
    }
}

// ── My request card — with X dismiss/cancel button ──────────────────────────
@Composable
private fun MyRequestCard(
    req: com.kidsroutine.core.model.PrivilegeRequest,
    onCancel: (com.kidsroutine.core.model.PrivilegeRequest) -> Unit
) {
    val (statusColor, statusText, statusEmoji) = when (req.status) {
        PrivilegeRequestStatus.PENDING  -> Triple(Color(0xFFFF9800), "Pending",  "⏳")
        PrivilegeRequestStatus.APPROVED -> Triple(GreenDone,          "Approved", "✅")
        PrivilegeRequestStatus.REJECTED -> Triple(Color(0xFFF44336),  "Declined", "❌")
    }

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border    = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier  = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(req.privilegeEmoji, fontSize = 32.sp)

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(req.privilegeTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("⭐ ${req.xpCost} XP", fontSize = 12.sp, color = OrangePrimary, fontWeight = FontWeight.SemiBold)
                }
                // Parent note shown only on rejected requests
                if (req.parentNote.isNotBlank() && req.status == PrivilegeRequestStatus.REJECTED) {
                    Text(
                        "Parent said: \"${req.parentNote}\"",
                        fontSize   = 11.sp,
                        color      = Color.Gray,
                        fontStyle  = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            // Status chip + X button stacked vertically
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape  = RoundedCornerShape(8.dp),
                    color  = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        "$statusEmoji $statusText",
                        color      = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 11.sp,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // X button:
                // - PENDING  → deletes from Firestore (removes it for parent too)
                // - APPROVED/REJECTED → dismisses from local list only
                IconButton(
                    onClick  = { onCancel(req) },
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFFF5F5F5), CircleShape)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = if (req.status == PrivilegeRequestStatus.PENDING) "Cancel request" else "Dismiss",
                        tint               = Color.Gray,
                        modifier           = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MyRequestsEmpty() {
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🛍️", fontSize = 48.sp)
            Text("No requests yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Spend XP on privileges and track them here!", color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

// ── Default privilege purchase confirmation ──────────────────────────────────
@Composable
private fun PrivilegePurchaseDialog(
    privilege: Privilege,
    userXp: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var submitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.fillMaxWidth()
            ) {
                Text(privilege.emoji, fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(privilege.title, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier            = Modifier.fillMaxWidth()
            ) {
                Text(privilege.description, textAlign = TextAlign.Center, color = Color.Gray)
                Surface(
                    shape  = RoundedCornerShape(12.dp),
                    color  = OrangePrimary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("⭐", fontSize = 18.sp)
                        Text(
                            "$userXp → ${userXp - privilege.xpCost} XP",
                            fontWeight = FontWeight.Bold,
                            color      = OrangePrimary
                        )
                    }
                }
                Text(
                    "🔔 A request will be sent to your parent for approval.",
                    textAlign = TextAlign.Center,
                    fontSize  = 12.sp,
                    color     = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = {
                    if (!submitting) {
                        submitting = true
                        onConfirm()
                    }
                },
                enabled  = !submitting,
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✅ Send Request!", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}

// ── Achievements grid ────────────────────────────────────────────────────────
@Composable
private fun AchievementsGrid(currentUser: UserModel) {
    val allBadges = listOf(
        "🌟" to "First Quest",    "🔥" to "5-Day Streak",  "🚀" to "Launch Hero",
        "💎" to "Diamond Tasker", "🏆" to "Champion",      "⚡" to "Speed Runner",
        "🎯" to "Sharpshooter",   "🌈" to "Rainbow Week",  "👑" to "Quest King",
        "🦁" to "Brave Heart",    "🧠" to "Big Brain",     "🌙" to "Night Owl"
    )
    val earnedNames = currentUser.badges.map { it.title }.toSet()

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text("Achievement Wall", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            "${earnedNames.size}/${allBadges.size} earned",
            color = OrangePrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )
        allBadges.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { (emoji, name) ->
                    val isEarned = name in earnedNames
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (isEarned) YellowGold.copy(alpha = 0.2f) else Color(0xFFF0F0F0))
                                .border(2.dp, if (isEarned) YellowGold else Color.LightGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (isEarned) emoji else "❓",
                                fontSize = 28.sp,
                                modifier = if (!isEarned) Modifier.alpha(0.3f) else Modifier
                            )
                        }
                        Text(
                            name,
                            fontSize   = 10.sp,
                            textAlign  = TextAlign.Center,
                            fontWeight = if (isEarned) FontWeight.Bold else FontWeight.Normal,
                            color      = if (isEarned) Color(0xFF2D3436) else Color.LightGray,
                            maxLines   = 2
                        )
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// ── Avatar shortcut ──────────────────────────────────────────────────────────
@Composable
private fun AvatarShortcut(onAvatarShopClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(20.dp).clickable(onClick = onAvatarShopClick),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(PurpleAccent.copy(alpha = 0.15f), TealAccent.copy(alpha = 0.15f))))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🎨", fontSize = 48.sp)
                Text("Customize Your Avatar", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(
                    "Unlock hats, hair, glasses, backgrounds and more with XP!",
                    textAlign = TextAlign.Center,
                    color     = Color.Gray,
                    fontSize  = 13.sp
                )
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onAvatarShopClick,
                    shape   = RoundedCornerShape(12.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                ) {
                    Text("🛍️ Open Avatar Shop", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
