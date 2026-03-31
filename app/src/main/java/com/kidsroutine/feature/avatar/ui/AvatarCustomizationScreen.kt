package com.kidsroutine.feature.avatar.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kidsroutine.core.model.*
import com.kidsroutine.feature.avatar.data.AvatarSeeder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarCustomizationScreen(
    viewModel: AvatarCustomizationViewModel,
    onNavigateToShop: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(AvatarCustomizationTab.BACKGROUND) }
    var previewExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF5E8))) {
        // Gradient header background (behind everything)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.15f)
                .background(Brush.verticalGradient(listOf(Color(0xFF5272F2), Color(0xFF667EEA))))
                .zIndex(0f)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar (overlays gradient) ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp)
                    .zIndex(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        "My Avatar",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Coin badge
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🪙", style = MaterialTheme.typography.labelLarge)
                                Text(
                                    "${uiState.coins}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        IconButton(onClick = onNavigateToShop) {
                            Icon(Icons.Default.ShoppingCart, "Shop", tint = Color.White)
                        }

                        IconButton(onClick = { viewModel.resetToDefault() }) {
                            Icon(Icons.Default.Refresh, "Reset", tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            // ── Preview + Gender + Skin Row ──────────────────────────────────
            AnimatedContent(
                targetState = previewExpanded,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "preview"
            ) { expanded ->
                if (expanded) {
                    // Full-screen preview modal overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                            .clickable { previewExpanded = false },
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarPreviewCard(
                            avatarState = uiState.currentAvatar,
                            modifier = Modifier
                                .width(260.dp)
                                .height(380.dp),
                            showNameBadge = true,
                            playerName = uiState.playerName
                        )
                        // Tap to close hint
                        Text(
                            "Tap to close",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                        )
                    }
                } else {
                    AvatarPreviewSection(
                        uiState = uiState,
                        onExpandPreview = { previewExpanded = true },
                        onGenderChange = { viewModel.setGender(it) },
                        onSkinToneChange = { viewModel.setSkinTone(it) }
                    )
                }
            }

            // ── Tab Bar ───────────────────────────────────────────────────────
            AvatarTabBar(
                selected = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // ── Item Grid ─────────────────────────────────────────────────────
            AvatarItemGrid(
                tab = selectedTab,
                avatarState = uiState.currentAvatar,
                unlockedItems = uiState.unlockedItemIds,
                freeItems = viewModel.getFreeItemsForTab(selectedTab),
                premiumItems = viewModel.getPremiumItemsForTab(selectedTab),
                onItemSelected = { viewModel.equipItem(it) },
                onLockedItemTapped = { onNavigateToShop() },
                modifier = Modifier.weight(1f)
            )

            // ── Save Button ───────────────────────────────────────────────────
            AvatarSaveButton(
                onSave = { viewModel.saveAvatar() },
                hasUnsavedChanges = uiState.hasUnsavedChanges
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Preview Section (avatar card + gender + skin tone)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarPreviewSection(
    uiState: AvatarUiState,
    onExpandPreview: () -> Unit,
    onGenderChange: (AvatarGender) -> Unit,
    onSkinToneChange: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Preview Card (tappable to expand)
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(220.dp)
                    .clickable { onExpandPreview() }
            ) {
                AvatarPreviewCard(
                    avatarState = uiState.currentAvatar,
                    modifier = Modifier.fillMaxSize(),
                    showNameBadge = false
                )
                // Expand icon
                Icon(
                    Icons.Default.Fullscreen, "Expand",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Right side controls
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Gender",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                GenderSelector(
                    selected = uiState.currentAvatar.gender,
                    onSelect = onGenderChange
                )

                Text(
                    "Skin Tone",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                SkinTonePicker(
                    selectedTone = uiState.currentAvatar.skinTone,
                    onToneSelected = onSkinToneChange
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Tab Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarTabBar(
    selected: AvatarCustomizationTab,
    onTabSelected: (AvatarCustomizationTab) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(AvatarCustomizationTab.entries) { tab ->
            val isSelected = selected == tab
            Surface(
                onClick = { onTabSelected(tab) },
                shape = RoundedCornerShape(50),
                color = if (isSelected) Color(0xFF5272F2) else Color(0xFFE2DEFF),
                border = if (!isSelected) BorderStroke(1.dp, Color(0xFF5272F2).copy(alpha = 0.25f))                else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tab.emoji, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFF3D3A5C)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Item Grid
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarItemGrid(
    tab: AvatarCustomizationTab,
    avatarState: AvatarState,
    unlockedItems: Set<String>,
    freeItems: List<AvatarLayerItem>,
    premiumItems: List<AvatarLayerItem>,
    onItemSelected: (AvatarLayerItem) -> Unit,
    onLockedItemTapped: (AvatarLayerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeItemId = when (tab) {
        AvatarCustomizationTab.BACKGROUND -> avatarState.activeBackground?.id
        AvatarCustomizationTab.HAIR -> avatarState.activeHair?.id
        AvatarCustomizationTab.OUTFIT -> avatarState.activeOutfit?.id
        AvatarCustomizationTab.SHOES -> avatarState.activeShoes?.id
        AvatarCustomizationTab.ACCESSORY -> avatarState.activeAccessory?.id
        AvatarCustomizationTab.SPECIAL_FX -> avatarState.activeSpecialFx?.id
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        // "None" option
        item {
            NoneItemCard(
                isSelected = activeItemId == null,
                onClick = { onItemSelected(
                    AvatarLayerItem("none_${tab.name}", "None",
                        AvatarLayerType.BACKGROUND, AvatarAssetSource.GradientBackground(
                            0xFF0D0D1A, 0xFF1A1A2E, "None"))
                ) }
            )
        }

        // Free items
        items(freeItems) { item ->
            AvatarItemCard(
                item = item,
                isSelected = item.id == activeItemId,
                isLocked = false,
                onClick = { onItemSelected(item) },
                onLockedClick = {}
            )
        }

        // Premium items (locked if not owned)
        if (premiumItems.isNotEmpty()) {
            item(span = { GridItemSpan(3) }) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⭐", style = MaterialTheme.typography.titleSmall)
                    Text("Premium",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFFFD700).copy(alpha = 0.3f)
                    )
                }
            }

            items(premiumItems) { item ->
                val isUnlocked = item.id in unlockedItems
                AvatarItemCard(
                    item = item,
                    isSelected = item.id == activeItemId,
                    isLocked = !isUnlocked,
                    onClick = { if (isUnlocked) onItemSelected(item) else onLockedItemTapped(item) },
                    onLockedClick = { onLockedItemTapped(item) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Item Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarItemCard(
    item: AvatarLayerItem,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    onLockedClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> Color(0xFF5272F2)
        item.isPremium -> Color(0xFFFFD700).copy(alpha = 0.6f)
        else -> Color(0xFF5272F2).copy(alpha = 0.25f)
    }

    val bgColor = when {
        isSelected -> Color(0xFF5272F2).copy(alpha = 0.2f)
        item.isPremium -> Color(0xFFFFD700).copy(alpha = 0.15f)
        else -> Color(0xFFE2DEFF)
    }

    val tintColor = item.tintColor?.let { Color(it) }
        ?: when (val src = item.source) {
            is AvatarAssetSource.GradientBackground -> Color(src.topColor)
            else -> Color(0xFF5272F2)
        }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        bgColor.copy(alpha = 0.5f),
                        bgColor.copy(alpha = 0.25f)
                    )
                )
            )
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { if (isLocked) onLockedClick() else onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Mini preview
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tintColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                when (val src = item.source) {
                    is AvatarAssetSource.GradientBackground -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(src.topColor), Color(src.bottomColor))
                                    )
                                )
                        )
                    }
                    else -> {
                        // Colour swatch fallback
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(tintColor.copy(alpha = 0.7f))
                        )
                    }
                }

                // Layer type icon
                val emoji = when (item.layerType) {
                    AvatarLayerType.HAIR -> "💇"
                    AvatarLayerType.OUTFIT -> "👕"
                    AvatarLayerType.SHOES -> "👟"
                    AvatarLayerType.ACCESSORY -> "🎩"
                    AvatarLayerType.SPECIAL_FX -> "✨"
                    else -> ""
                }
                if (emoji.isNotEmpty()) {
                    Text(emoji, style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                item.name,
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    isLocked -> Color(0xFF888888)
                    isSelected -> Color(0xFF5272F2)
                    else -> Color(0xFF3D3D4E)
                },
                maxLines = 1,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }

        // Lock overlay
        if (isLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, "Locked", tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                    if (item.coinCost > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("🪙 ${item.coinCost}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFD700))
                    }
                }
            }
        }

        // Selected checkmark
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5272F2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, "Selected",
                    tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }

        // Trending badge
        if (item.isPremium && AvatarSeeder.premiumPacks.any { p -> p.id == item.packId && p.isTrending }) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFFF4500)
            ) {
                Text("🔥", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
            }
        }
    }
}

// "None" card (continued)
@Composable
fun NoneItemCard(isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        if (isSelected) Color(0xFF5272F2).copy(alpha = 0.35f)
                        else Color(0xFFF0EDFF),
                        if (isSelected) Color(0xFF5272F2).copy(alpha = 0.2f)
                        else Color(0xFFD6D2F5)
                    )
                )
            )
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF5272F2) else Color(0xFF5272F2).copy(alpha = 0.25f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.07f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "None",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                "None",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color(0xFF5272F2) else Color(0xFF555566),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5272F2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, "Selected",
                    tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Save Button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarSaveButton(
    onSave: () -> Unit,
    hasUnsavedChanges: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (hasUnsavedChanges) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "saveScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .scale(scale)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (hasUnsavedChanges)
                        Brush.horizontalGradient(
                            listOf(Color(0xFF5272F2), Color(0xFF9B59F5))
                        )
                    else
                        Brush.horizontalGradient(
                            listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.08f))
                        )
                )
                .clickable(enabled = hasUnsavedChanges) { onSave() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (hasUnsavedChanges) Icons.Default.Check else Icons.Default.Done,
                    contentDescription = "Save",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    if (hasUnsavedChanges) "Save Avatar" else "Saved ✓",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}