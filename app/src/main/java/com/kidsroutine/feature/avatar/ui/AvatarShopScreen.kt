package com.kidsroutine.feature.avatar.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.AvatarItem
import com.kidsroutine.core.model.AvatarRarity
import com.kidsroutine.core.model.UserModel

// ── Palette ──────────────────────────────────────────────────────────────────
private val BgLight     = Color(0xFFFFFBF0)
private val GoldColor   = Color(0xFFFFD700)
private val PurpleEpic  = Color(0xFF9B59B6)
private val BlueRare    = Color(0xFF3498DB)
private val GreenCommon = Color(0xFF2ECC71)
private val OrangeGrad  = Color(0xFFFF6B35)

@Composable
fun AvatarShopScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit,
    viewModel: AvatarShopViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Initialise once
    LaunchedEffect(currentUser.userId) {
        viewModel.init(currentUser.userId, currentUser.xp)
    }

    // Auto-clear messages after 3 s
    LaunchedEffect(uiState.purchaseSuccess, uiState.error) {
        if (uiState.purchaseSuccess != null || uiState.error != null) {
            kotlinx.coroutines.delay(3_000)
            viewModel.clearMessages()
        }
    }

    val filteredItems = remember(uiState.items, uiState.selectedRarityFilter) {
        if (uiState.selectedRarityFilter == null) uiState.items
        else uiState.items.filter { it.rarity == uiState.selectedRarityFilter }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgLight)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(OrangeGrad, Color(0xFFFFD93D))))
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment   = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛍️ Avatar Shop", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Spend XP to unlock items", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                    // XP Badge
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f)) {
                        Text(
                            text = "⭐ ${uiState.userXp} XP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // ── Rarity filter chips ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RarityChip("All", null,            uiState.selectedRarityFilter == null)       { viewModel.setRarityFilter(null) }
                RarityChip("Common",    AvatarRarity.COMMON,    uiState.selectedRarityFilter == AvatarRarity.COMMON)    { viewModel.setRarityFilter(AvatarRarity.COMMON) }
                RarityChip("Rare",      AvatarRarity.RARE,      uiState.selectedRarityFilter == AvatarRarity.RARE)      { viewModel.setRarityFilter(AvatarRarity.RARE) }
                RarityChip("Epic",      AvatarRarity.EPIC,      uiState.selectedRarityFilter == AvatarRarity.EPIC)      { viewModel.setRarityFilter(AvatarRarity.EPIC) }
                RarityChip("Legendary", AvatarRarity.LEGENDARY, uiState.selectedRarityFilter == AvatarRarity.LEGENDARY) { viewModel.setRarityFilter(AvatarRarity.LEGENDARY) }
            }

            // ── Loading / Grid ───────────────────────────────────────────────
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangeGrad)
                }
            } else if (filteredItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items found", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredItems) { item ->
                        ShopItemCard(
                            item       = item,
                            isOwned    = item.itemId in uiState.unlockedItemIds,
                            canAfford  = uiState.userXp >= item.xpCost,
                            onBuy      = { viewModel.purchaseItem(currentUser.userId, item) }
                        )
                    }
                }
            }
        }

        // ── Toast messages ───────────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.purchaseSuccess != null || uiState.error != null,
            enter   = slideInVertically { it } + fadeIn(),
            exit    = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.error != null) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                shadowElevation = 8.dp
            ) {
                Text(
                    text     = uiState.purchaseSuccess ?: uiState.error ?: "",
                    color    = if (uiState.error != null) Color(0xFFC62828) else Color(0xFF2E7D32),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    }
}

// ── Shop item card ────────────────────────────────────────────────────────────

@Composable
private fun ShopItemCard(
    item: AvatarItem,
    isOwned: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue   = if (isOwned) 1f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label         = "itemScale"
    )
    val rarityColor = rarityColor(item.rarity)

    Card(
        modifier  = Modifier.fillMaxWidth().scale(scale),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        border    = BorderStroke(if (isOwned) 2.dp else 1.dp, if (isOwned) rarityColor else Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(if (isOwned) 6.dp else 2.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Rarity badge
            Surface(shape = RoundedCornerShape(8.dp), color = rarityColor.copy(alpha = 0.15f)) {
                Text(
                    text     = item.rarity.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color    = rarityColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            // Item emoji / icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(rarityColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🎨", fontSize = 32.sp)   // placeholder; real art would go here
                if (isOwned) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }

            // Name
            Text(
                text      = item.name,
                fontSize  = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines  = 2,
                color     = Color(0xFF2D3436)
            )

            // Category label
            Text(
                text    = item.category.name.replace("_", " "),
                fontSize = 11.sp,
                color   = Color.Gray
            )

            // Buy / owned button
            if (isOwned) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE8F5E9)) {
                    Text(
                        "✓ Owned",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                Button(
                    onClick  = onBuy,
                    enabled  = canAfford,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = rarityColor,
                        disabledContainerColor = Color(0xFFCCCCCC)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text  = if (canAfford) "⭐ ${item.xpCost} XP" else "🔒 ${item.xpCost} XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Rarity filter chip ────────────────────────────────────────────────────────

@Composable
private fun RarityChip(
    label: String,
    rarity: AvatarRarity?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (rarity != null) rarityColor(rarity) else OrangeGrad
    Surface(
        shape  = RoundedCornerShape(20.dp),
        color  = if (isSelected) color.copy(alpha = 0.15f) else Color.White,
        border = BorderStroke(1.dp, if (isSelected) color else Color(0xFFEEEEEE)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text     = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color    = if (isSelected) color else Color.Gray,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

private fun rarityColor(rarity: AvatarRarity): Color = when (rarity) {
    AvatarRarity.COMMON    -> GreenCommon
    AvatarRarity.RARE      -> BlueRare
    AvatarRarity.EPIC      -> PurpleEpic
    AvatarRarity.LEGENDARY -> GoldColor
}