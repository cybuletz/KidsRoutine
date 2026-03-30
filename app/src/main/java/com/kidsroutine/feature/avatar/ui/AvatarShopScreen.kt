package com.kidsroutine.feature.avatar.ui

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kidsroutine.core.model.*
import com.kidsroutine.feature.avatar.data.AvatarSeeder

@Composable
fun AvatarShopScreen(
    viewModel: AvatarShopViewModel,
    onBack: () -> Unit,
    onPackPurchased: (AvatarContentPack) -> Unit,
    currentUserId: String  // ✨ ADD THIS PARAMETER
) {
    // ✨ Initialize ViewModel on first compose
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            viewModel.init(currentUserId)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf("all") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0D0D1A), Color(0xFF1A1A2E)))
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Shop Top Bar ─────────────────────────────────────────────────
            ShopTopBar(
                xp = uiState.xp,              // ← NOW will display correctly
                onBack = onBack
            )

            // ── XP Balance Banner ───────────────────────────────────────────
            XpBalanceBanner(
                xp = uiState.xp,              // ← Update to show XP
                onBuyXp = { viewModel.openCoinStore() }
            )

            // ── Category Filter Chips ─────────────────────────────────────────
            ShopCategoryChips(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            // ── Pack Grid ─────────────────────────────────────────────────────
            val packs = when (selectedCategory) {
                "trending" -> AvatarSeeder.premiumPacks.filter { it.isTrending }
                "new" -> AvatarSeeder.premiumPacks.filter { it.isNew }
                "owned" -> AvatarSeeder.premiumPacks.filter {
                    it.id in uiState.ownedPackIds
                }
                else -> AvatarSeeder.premiumPacks
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(packs) { pack ->
                    ShopPackCard(
                        pack = pack,
                        isOwned = pack.id in uiState.ownedPackIds,
                        onBuy = { viewModel.purchasePack(pack) },
                        onPreview = { viewModel.previewPack(pack) }
                    )
                }
            }
        }

        // ── Purchase confirmation dialog ──────────────────────────────────────
        uiState.pendingPurchasePack?.let { pack ->
            PurchaseConfirmDialog(
                pack = pack,
                xp = uiState.xp,              // ← Pass XP instead of coins
                onConfirm = {
                    viewModel.confirmPurchase(pack)
                    onPackPurchased(pack)
                },
                onDismiss = { viewModel.dismissPurchase() }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Category Chips
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ShopCategoryChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "all" to "🛍️ All",
        "trending" to "🔥 Trending",
        "new" to "✨ New",
        "owned" to "✅ Owned"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { (id, label) ->
            val isSelected = selectedCategory == id
            Surface(
                onClick = { onCategorySelected(id) },
                shape = RoundedCornerShape(50),
                color = if (isSelected) Color(0xFF5272F2) else Color.White.copy(alpha = 0.08f),
                border = if (!isSelected) BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)) else null
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = Color.White
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Shop Pack Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ShopPackCard(
    pack: AvatarContentPack,
    isOwned: Boolean,
    onBuy: () -> Unit,
    onPreview: () -> Unit
) {
    val accentColor = Color(pack.accentColor)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        accentColor.copy(alpha = 0.5f),
                        Color(0xFF0D0D1A)
                    )
                )
            )
            .border(
                width = if (isOwned) 2.dp else 1.dp,
                color = if (isOwned) Color(0xFF4CAF50)
                else accentColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable {
                Log.d("ShopPackCard", "Card clicked for pack: ${pack.name}, isOwned: $isOwned")  // ✅ DEBUG
                if (!isOwned) onBuy()
                else onPreview()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            pack.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "${pack.items.size} items",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    // Trending / New badge
                    if (pack.isTrending || pack.isNew) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (pack.isTrending) Color(0xFFFF4500)
                            else Color(0xFF00BCD4)
                        ) {
                            Text(
                                if (pack.isTrending) "🔥 Hot" else "🆕 New",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Items preview swatches
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pack.items.take(4).forEach { item ->
                        val swatchColor = item.tintColor?.let { Color(it) }
                            ?: when (val src = item.source) {
                                is AvatarAssetSource.GradientBackground -> Color(src.topColor)
                                else -> accentColor
                            }
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        )
                    }
                    if (pack.items.size > 4) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+${pack.items.size - 4}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = Color.White)
                        }
                    }
                }
            }

            // Description
            Text(
                pack.description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 2
            )

            // Buy / Owned button
            if (isOwned) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1B5E20).copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, Color(0xFF4CAF50))
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, "Owned",
                            tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Owned", style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(accentColor, accentColor.copy(alpha = 0.7f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⭐", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "${pack.packPrice}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Shop Top Bar (UPDATED)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ShopTopBar(xp: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Avatar Shop", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = Color.White)
            Text("Unlock epic character packs", style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f))
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF00FFD700).copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("⭐", style = MaterialTheme.typography.labelLarge)
                Text("$xp XP", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold, color = Color(0xFF00FFD700))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  XP Balance Banner (RENAMED from CoinBalanceBanner)
// ─────────────────────────���───────────────────────────────────────────────────

@Composable
fun XpBalanceBanner(xp: Int, onBuyXp: () -> Unit) {

    val pulse = rememberInfiniteTransition(label = "xpPulse")
    val glow by pulse.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "glow"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A2000),
        border = BorderStroke(
            1.dp,
            Color(0xFF00FFD700).copy(alpha = 0.3f * glow)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("⭐", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF00FFD700))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "$xp XP",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                    Text("Available to spend",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Purchase Confirm Dialog (UPDATED)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PurchaseConfirmDialog(
    pack: AvatarContentPack,
    xp: Int,                              // Changed from coins to xp
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = Color(pack.accentColor)
    val canAfford = xp >= pack.packPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2E),
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unlock Pack", fontWeight = FontWeight.Bold, color = Color.White)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        pack.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    pack.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cost:", color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", style = MaterialTheme.typography.bodyMedium)
                        Text("${pack.packPrice} XP",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FFD700),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Your XP:", color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", style = MaterialTheme.typography.bodyMedium)
                        Text("$xp XP",
                            fontWeight = FontWeight.Bold,
                            color = if (canAfford) Color(0xFF4CAF50) else Color(0xFFE53935),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (!canAfford) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE53935).copy(alpha = 0.15f)
                    ) {
                        Text(
                            "⚠️ Not enough XP. Earn more by completing tasks!",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFE53935)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (canAfford)
                            Brush.horizontalGradient(listOf(accentColor, accentColor.copy(alpha = 0.7f)))
                        else
                            Brush.horizontalGradient(listOf(
                                Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.08f)
                            ))
                    )
                    .clickable(enabled = canAfford) { onConfirm() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (canAfford) "✅ Confirm Purchase" else "Not enough XP",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (canAfford) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}