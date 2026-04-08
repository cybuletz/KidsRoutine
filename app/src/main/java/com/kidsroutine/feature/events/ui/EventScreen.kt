package com.kidsroutine.feature.events.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.EventShopItem
import com.kidsroutine.core.model.TimedEvent
import com.kidsroutine.core.model.UserModel

// ── Event theme colors ──────────────────────────────────────────────
private val EventAccent = Color(0xFFE67E22)
private val EventAccentLight = Color(0xFFF39C12)
private val EventGradientStart = Color(0xFFE67E22)
private val EventGradientEnd = Color(0xFFF1C40F)
private val ProgressGreenStart = Color(0xFF27AE60)
private val ProgressGreenEnd = Color(0xFF2ECC71)
private val BgLight = Color(0xFFFFFBF0)
private val TextDark = Color(0xFF2D3436)
private val CardBg = Color(0xFFFFFFFF)
private val ShopBg = Color(0xFFFFF8E1)
private val DisabledGray = Color(0xFFBDC3C7)

@Composable
fun EventScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.userId) {
        viewModel.loadEvents(currentUser.userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        // ── Header ──────────────────────────────────────────────────
        EventHeader(onBackClick = onBackClick)

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EventAccent)
                }
            }
            uiState.events.isEmpty() -> {
                NoEventsState()
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    // ── Event selector (horizontal scroll) ──────────
                    if (uiState.events.size > 1) {
                        Spacer(Modifier.height(12.dp))
                        EventSelector(
                            events = uiState.events,
                            selectedEvent = uiState.selectedEvent,
                            onEventSelected = { viewModel.selectEvent(it) }
                        )
                    }

                    uiState.selectedEvent?.let { event ->
                        val progress = uiState.progress

                        Spacer(Modifier.height(16.dp))

                        // ── Event banner ────────────────────────────
                        EventBanner(event = event)

                        Spacer(Modifier.height(16.dp))

                        // ── Countdown timer ─────────────────────────
                        CountdownTimer(timeRemaining = uiState.timeRemaining)

                        Spacer(Modifier.height(16.dp))

                        // ── Progress section ────────────────────────
                        if (progress != null) {
                            ProgressSection(
                                event = event,
                                progress = progress,
                                completionPercent = viewModel.getCompletionPercent()
                            )

                            Spacer(Modifier.height(16.dp))

                            // ── Token balance ───────────────────────
                            TokenBalanceCard(
                                tokenName = event.eventTokenName,
                                tokensAvailable = progress.tokensAvailable
                            )

                            Spacer(Modifier.height(12.dp))
                        }

                        // ── Shop toggle button ──────────────────────
                        if (uiState.shopItems.isNotEmpty()) {
                            ShopToggleButton(
                                showShop = uiState.showShop,
                                onClick = { viewModel.toggleShop() }
                            )
                        }

                        // ── Token shop ──────────────────────────────
                        if (uiState.showShop) {
                            Spacer(Modifier.height(12.dp))
                            TokenShopSection(
                                items = uiState.shopItems,
                                tokensAvailable = progress?.tokensAvailable ?: 0,
                                purchasedItemIds = progress?.rewardsClaimed ?: emptyList(),
                                onPurchase = { viewModel.purchaseShopItem(it) }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // ── Rewards section ─────────────────────────
                        RewardsSection(event = event)

                        Spacer(Modifier.height(140.dp))
                    }
                }

                // ── Purchase feedback snackbar ──────────────────────
                uiState.purchaseMessage?.let { message ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Snackbar(
                            action = {
                                TextButton(onClick = { viewModel.dismissPurchaseMessage() }) {
                                    Text("OK", color = Color.White)
                                }
                            },
                            containerColor = EventAccent
                        ) {
                            Text(message, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// COMPONENTS
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun EventHeader(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(EventGradientStart, EventGradientEnd)))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Text(
            text = "🎪 Events",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun NoEventsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🎪", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No Active Events",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Check back soon! 🎪",
                style = MaterialTheme.typography.bodyLarge,
                color = TextDark.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun EventSelector(
    events: List<TimedEvent>,
    selectedEvent: TimedEvent?,
    onEventSelected: (TimedEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        events.forEach { event ->
            val isSelected = event.eventId == selectedEvent?.eventId
            Card(
                modifier = Modifier
                    .width(140.dp)
                    .clickable { onEventSelected(event) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) EventAccent else CardBg
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 6.dp else 2.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = event.emoji, fontSize = 28.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextDark,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EventBanner(event: TimedEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(EventGradientStart, EventGradientEnd)),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(text = event.emoji, fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.type.emoji,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = event.type.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "${event.startDate} — ${event.endDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CountdownTimer(timeRemaining: Long) {
    val days = timeRemaining / 86400
    val hours = (timeRemaining % 86400) / 3600
    val minutes = (timeRemaining % 3600) / 60
    val seconds = timeRemaining % 60

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⏱️ Time Remaining",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeDigitBox(value = days, label = "DAYS", pulse = pulse)
                TimeSeparator()
                TimeDigitBox(value = hours, label = "HRS", pulse = pulse)
                TimeSeparator()
                TimeDigitBox(value = minutes, label = "MIN", pulse = pulse)
                TimeSeparator()
                TimeDigitBox(value = seconds, label = "SEC", pulse = pulse)
            }
        }
    }
}

@Composable
private fun TimeDigitBox(value: Long, label: String, pulse: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size((56 * pulse).dp)
                .clip(RoundedCornerShape(12.dp))
                .background(EventAccent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString().padStart(2, '0'),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 22.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextDark.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TimeSeparator() {
    Text(
        text = ":",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = EventAccent,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun ProgressSection(
    event: TimedEvent,
    progress: com.kidsroutine.core.model.EventProgress,
    completionPercent: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = completionPercent,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    emoji = "✅",
                    value = "${progress.tasksCompleted}/${event.targetTaskCount}",
                    label = "Tasks"
                )
                StatItem(
                    emoji = "⭐",
                    value = "${progress.xpEarned}",
                    label = "XP Earned"
                )
                StatItem(
                    emoji = "🪙",
                    value = "${progress.tokensEarned}",
                    label = "Tokens"
                )
            }

            Spacer(Modifier.height(16.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8E8E8))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(ProgressGreenStart, ProgressGreenEnd)
                            )
                        )
                        .animateContentSize()
                )
                Text(
                    text = "${(completionPercent * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (completionPercent > 0.4f) Color.White else TextDark,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (progress.isComplete) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "🎉 Event Complete!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ProgressGreenStart,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun StatItem(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextDark.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun TokenBalanceCard(tokenName: String, tokensAvailable: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShopBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "💰",
                fontSize = 24.sp
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$tokensAvailable",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = EventAccent
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = tokenName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextDark.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ShopToggleButton(showShop: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EventAccent),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.ShoppingCart,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (showShop) "Hide Token Shop" else "🛒 Open Token Shop",
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun TokenShopSection(
    items: List<EventShopItem>,
    tokensAvailable: Int,
    purchasedItemIds: List<String>,
    onPurchase: (EventShopItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShopBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🛍️ Token Shop",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(Modifier.height(12.dp))

            // Use a fixed height grid so it works inside a scrollable column
            val rows = (items.size + 1) / 2
            val gridHeight = (rows * 180).dp

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(gridHeight),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false
            ) {
                items(items, key = { it.itemId }) { item ->
                    val alreadyPurchased = item.itemId in purchasedItemIds
                    val canAfford = tokensAvailable >= item.tokenCost

                    ShopItemCard(
                        item = item,
                        canAfford = canAfford,
                        alreadyPurchased = alreadyPurchased,
                        onPurchase = { onPurchase(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShopItemCard(
    item: EventShopItem,
    canAfford: Boolean,
    alreadyPurchased: Boolean,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = item.emoji, fontSize = 32.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "🪙 ${item.tokenCost}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = EventAccent
            )
            Spacer(Modifier.height(8.dp))

            when {
                alreadyPurchased -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ProgressGreenStart.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✅ Owned",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ProgressGreenStart
                        )
                    }
                }
                else -> {
                    Button(
                        onClick = onPurchase,
                        enabled = canAfford,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EventAccent,
                            disabledContainerColor = DisabledGray
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (canAfford) "Buy" else "Need more",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardsSection(event: TimedEvent) {
    val hasRewards = event.rewardAvatarItemIds.isNotEmpty() ||
            event.rewardXp > 0 ||
            event.rewardBadgeId.isNotEmpty()

    if (!hasRewards) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🏆 Rewards",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(Modifier.height(12.dp))

            if (event.rewardXp > 0) {
                RewardRow(emoji = "⭐", text = "${event.rewardXp} XP Bonus")
            }
            if (event.rewardBadgeId.isNotEmpty()) {
                RewardRow(emoji = "🏅", text = "Exclusive Event Badge")
            }
            if (event.rewardAvatarItemIds.isNotEmpty()) {
                RewardRow(
                    emoji = "👕",
                    text = "${event.rewardAvatarItemIds.size} Exclusive Avatar Item${if (event.rewardAvatarItemIds.size > 1) "s" else ""}"
                )
            }
        }
    }
}

@Composable
private fun RewardRow(emoji: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(EventAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )
    }
}
