package com.kidsroutine.feature.avatar.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.AvatarCategory
import com.kidsroutine.core.model.AvatarItem
import com.kidsroutine.core.model.AvatarRarity
import com.kidsroutine.core.model.UserModel
import androidx.compose.foundation.border


private val GradientStart = ComposeColor(0xFF667EEA)
private val GradientEnd   = ComposeColor(0xFF764BA2)
private val BgLight       = ComposeColor(0xFFFFFBF0)

@Composable
fun AvatarCustomizationScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit,
    viewModel: AvatarCustomizationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableStateOf(0) }  // 0 = Customize, 1 = Shop

    // Shop ViewModel — initialised here so it shares the same lifecycle
    val shopViewModel: AvatarShopViewModel = hiltViewModel()
    val shopUiState by shopViewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.userId) {
        viewModel.init(currentUser.userId, currentUser.xp)
    }

    // Init shop whenever we switch to the Shop tab (or on first load)
    LaunchedEffect(currentUser.userId, activeTab) {
        if (activeTab == 1) {
            shopViewModel.init(currentUser.userId, currentUser.xp)
        }
    }

    // Auto-clear shop messages
    LaunchedEffect(shopUiState.purchaseSuccess, shopUiState.error) {
        if (shopUiState.purchaseSuccess != null || shopUiState.error != null) {
            kotlinx.coroutines.delay(3_000)
            shopViewModel.clearMessages()
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier         = Modifier.fillMaxSize().background(BgLight),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(BgLight)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint               = ComposeColor.White,
                        modifier           = Modifier.size(24.dp)
                    )
                }
                Text(
                    text       = "🎨 Customize Avatar",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = ComposeColor.White
                )
                Text(
                    text     = "${uiState.userXp} XP",
                    style    = MaterialTheme.typography.labelLarge,
                    color    = ComposeColor.White,
                    modifier = Modifier
                        .background(ComposeColor.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }

            // ── Tab row: Customize | Shop ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(ComposeColor(0xFFEEEEEE), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("🎨 Customize", "🛒 Shop").forEachIndexed { index, label ->
                    Surface(
                        modifier        = Modifier.weight(1f).clickable { activeTab = index },
                        shape           = RoundedCornerShape(10.dp),
                        color           = if (activeTab == index) ComposeColor.White else ComposeColor.Transparent,
                        shadowElevation = if (activeTab == index) 2.dp else 0.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier.padding(vertical = 10.dp)
                        ) {
                            Text(
                                text       = label,
                                fontSize   = 14.sp,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                                color      = if (activeTab == index) GradientStart else ComposeColor.Gray
                            )
                        }
                    }
                }
            }

            // ── Tab content ───────────────────────────────────────────────────
            when (activeTab) {

                // ── CUSTOMIZE TAB ─────────────────────────────────────────────
                0 -> {
                    LazyColumn(
                        modifier            = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AvatarPreview(
                                customization = uiState.customization,
                                modifier      = Modifier.fillMaxWidth().height(250.dp)
                            )
                        }

                        item {
                            CategorySelector(
                                categories         = AvatarCategory.entries,
                                selectedCategory   = uiState.selectedCategory,
                                onCategorySelected = { viewModel.selectCategory(it) }
                            )
                        }

                        item {
                            ItemsGrid(
                                items = uiState.allItems.filter {
                                    it.category == uiState.selectedCategory &&
                                            it.itemId in uiState.customization.unlockedItemIds
                                },                                unlockedItemIds = uiState.customization.unlockedItemIds,
                                selectedItemId  = when (uiState.selectedCategory) {
                                    AvatarCategory.BODY        -> uiState.customization.body.selectedItemId
                                    AvatarCategory.EYES        -> uiState.customization.eyes.selectedItemId
                                    AvatarCategory.MOUTH       -> uiState.customization.mouth.selectedItemId
                                    AvatarCategory.HAIRSTYLE   -> uiState.customization.hairstyle.selectedItemId
                                    AvatarCategory.ACCESSORIES -> uiState.customization.accessories.selectedItemId
                                    AvatarCategory.CLOTHING    -> uiState.customization.clothing.selectedItemId
                                    AvatarCategory.BACKGROUND  -> uiState.customization.background.selectedItemId
                                },
                                userXp         = uiState.userXp,
                                onItemSelect   = { viewModel.selectItem(it.itemId) },
                                onItemUnlock   = { viewModel.unlockAndSelectItem(currentUser.userId, it) }
                            )
                        }

                        item {
                            ColorPickerSection(onColorSelected = { viewModel.changeItemColor(it) })
                        }
                    }

                    // Save button only on Customize tab
                    Button(
                        onClick  = { viewModel.saveCustomization(currentUser.userId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        shape   = RoundedCornerShape(12.dp),
                        colors  = ButtonDefaults.buttonColors(containerColor = ComposeColor(0xFFFF6B35)),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                color       = ComposeColor.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text       = if (uiState.isSaving) "Saving..." else "Save Avatar",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                    }
                }

                // ── SHOP TAB ──────────────────────────────────────────────────
                1 -> {
                    val filteredItems = remember(shopUiState.items, shopUiState.selectedRarityFilter) {
                        if (shopUiState.selectedRarityFilter == null) shopUiState.items
                        else shopUiState.items.filter { it.rarity == shopUiState.selectedRarityFilter }
                    }

                    AvatarShopContent(
                        uiState       = shopUiState,
                        filteredItems = filteredItems,
                        currentUserId = currentUser.userId,
                        viewModel     = shopViewModel
                    )
                }
            }
        }
    }
}

// ── Avatar Preview ────────────────────────────────────────────────────────────
@Composable
private fun AvatarPreview(
    customization: com.kidsroutine.core.model.AvatarCustomization,
    modifier: Modifier = Modifier
) {
    val bgId       = customization.background.selectedItemId
    val hairId     = customization.hairstyle.selectedItemId
    val eyesId     = customization.eyes.selectedItemId
    val mouthId    = customization.mouth.selectedItemId
    val clothingId = customization.clothing.selectedItemId
    val accessId   = customization.accessories.selectedItemId
    val bodyColor  = customization.body.selectedColor

    // Background gradient based on selected bg item
    val bgColors = when {
        bgId.contains("galaxy",  true) -> listOf(ComposeColor(0xFF0D0D2B), ComposeColor(0xFF1A0533))
        bgId.contains("forest",  true) -> listOf(ComposeColor(0xFF1B4332), ComposeColor(0xFF52B788))
        bgId.contains("beach",   true) -> listOf(ComposeColor(0xFF0077B6), ComposeColor(0xFFADE8F4))
        bgId.contains("city",    true) -> listOf(ComposeColor(0xFF2D3436), ComposeColor(0xFF636E72))
        bgId.contains("sunset",  true) -> listOf(ComposeColor(0xFFFF6B35), ComposeColor(0xFFFFD93D))
        else                           -> listOf(ComposeColor(0xFF1A1A2E), ComposeColor(0xFF0F3460))
    }

    // Background emoji overlay
    val bgEmoji = when {
        bgId.contains("galaxy",  true) -> "🌌"
        bgId.contains("forest",  true) -> "🌲🌲"
        bgId.contains("beach",   true) -> "🌊☀️"
        bgId.contains("city",    true) -> "🏙️"
        bgId.contains("sunset",  true) -> "🌅"
        else -> ""
    }

    fun accessoryEmoji(id: String) = when {
        id.contains("crown",   true) -> "👑"
        id.contains("glasses", true) -> "🕶️"
        id.contains("hat",     true) -> "🎩"
        id.contains("cape",    true) -> "🦸"
        id.contains("wings",   true) -> "🪽"
        id.contains("sword",   true) -> "⚔️"
        id.contains("shield",  true) -> "🛡️"
        else -> ""
    }

    fun hairEmoji(id: String) = when {
        id.contains("long",   true) -> "💇"
        id.contains("curly",  true) -> "🦱"
        id.contains("spiky",  true) -> "🦔"
        id.contains("bun",    true) -> "👩"
        id.contains("short",  true) -> "💆"
        id.contains("afro",   true) -> "🧑"
        else -> if (id.isNotEmpty()) "🎭" else ""
    }

    fun eyesEmoji(id: String) = when {
        id.contains("happy",   true) -> "😊"
        id.contains("cool",    true) -> "😎"
        id.contains("sleepy",  true) -> "😴"
        id.contains("angry",   true) -> "😠"
        id.contains("wink",    true) -> "😉"
        id.contains("star",    true) -> "🤩"
        else -> if (id.isNotEmpty()) "👀" else ""
    }

    fun mouthEmoji(id: String) = when {
        id.contains("smile",  true) -> "😄"
        id.contains("laugh",  true) -> "😂"
        id.contains("sad",    true) -> "😢"
        id.contains("cool",   true) -> "😏"
        id.contains("tongue", true) -> "😛"
        else -> if (id.isNotEmpty()) "😶" else ""
    }

    fun clothingEmoji(id: String) = when {
        id.contains("hoodie", true) -> "🧥"
        id.contains("suit",   true) -> "👔"
        id.contains("tshirt", true) -> "👕"
        id.contains("dress",  true) -> "👗"
        id.contains("armor",  true) -> "🛡️"
        id.contains("robe",   true) -> "🥋"
        else -> if (id.isNotEmpty()) "👕" else ""
    }

    val faceColor: ComposeColor = try {
        val parsed = android.graphics.Color.parseColor(
            if (bodyColor.startsWith("#")) bodyColor else "#FF6B35"
        )
        ComposeColor(parsed.toLong() or 0xFF000000L)
    } catch (_: Throwable) { ComposeColor(0xFFFF6B35) }

    // Top-hat accessories (worn ON HEAD, above hair)
    val isHeadwear = accessId.contains("crown", true) ||
            accessId.contains("hat", true)
    // Eye-level accessories (worn on face)
    val isFaceWear = accessId.contains("glasses", true)
    // Body accessories
    val isBodyWear = !isHeadwear && !isFaceWear && accessId.isNotEmpty()

    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(bgColors))
        ) {
            // ── Background scene text ──────────────────────────────────────
            if (bgEmoji.isNotEmpty()) {
                Text(
                    text     = bgEmoji,
                    fontSize = 28.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                )
            }

            // ── Main avatar column ─────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier            = Modifier.fillMaxSize()
            ) {

                // HEAD LAYER 1: Crown / hat (above hair)
                if (isHeadwear) {
                    Text(accessoryEmoji(accessId), fontSize = 32.sp)
                }

                // HEAD LAYER 2: Hair
                val hair = hairEmoji(hairId)
                if (hair.isNotEmpty()) {
                    Text(hair, fontSize = 26.sp)
                }

                // HEAD LAYER 3: Face circle with eyes overlay
                Box(
                    modifier         = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(faceColor.copy(alpha = 0.25f))
                        .border(3.dp, faceColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Eyes (with glasses layered on top)
                        Box(contentAlignment = Alignment.Center) {
                            val eyes = eyesEmoji(eyesId)
                            if (eyes.isNotEmpty()) Text(eyes, fontSize = 20.sp)
                            if (isFaceWear) Text(accessoryEmoji(accessId), fontSize = 20.sp)
                        }
                        // Mouth
                        val mouth = mouthEmoji(mouthId)
                        if (mouth.isNotEmpty()) {
                            Text(mouth, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // BODY LAYER: Clothing
                val cloth = clothingEmoji(clothingId)
                if (cloth.isNotEmpty()) {
                    Text(cloth, fontSize = 32.sp)
                }

                // BODY ACCESSORY: Cape / wings / sword
                if (isBodyWear) {
                    Text(accessoryEmoji(accessId), fontSize = 26.sp)
                }
            }

            // ── Live Preview badge ────────────────────────────────────────
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                shape    = RoundedCornerShape(8.dp),
                color    = ComposeColor.Black.copy(alpha = 0.5f)
            ) {
                Text(
                    "✨ Live Preview",
                    fontSize   = 10.sp,
                    color      = ComposeColor.White,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}



// ── Category Selector ─────────────────────────────────────────────────────────
@Composable
private fun CategorySelector(
    categories: List<AvatarCategory>,
    selectedCategory: AvatarCategory,
    onCategorySelected: (AvatarCategory) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text       = "Categories",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                CategoryButton(
                    category   = category,
                    isSelected = category == selectedCategory,
                    onClick    = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryButton(
    category: AvatarCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick  = onClick,
        modifier = Modifier.height(40.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) GradientStart else ComposeColor.White,
            contentColor   = if (isSelected) ComposeColor.White else ComposeColor.Gray
        ),
        shape  = RoundedCornerShape(20.dp),
        border = if (!isSelected) ButtonDefaults.outlinedButtonBorder() else null
    ) {
        Text(text = category.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Items Grid ────────────────────────────────────────────────────────────────
@Composable
private fun ItemsGrid(
    items: List<AvatarItem>,
    unlockedItemIds: List<String>,
    selectedItemId: String,
    userXp: Int,
    onItemSelect: (AvatarItem) -> Unit,
    onItemUnlock: (AvatarItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text       = "Available Items",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(bottom = 12.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEach { item ->
                ItemCard(
                    item       = item,
                    isUnlocked = item.itemId in unlockedItemIds,
                    isSelected = item.itemId == selectedItemId,
                    userXp     = userXp,
                    onSelect   = { onItemSelect(item) },
                    onUnlock   = { onItemUnlock(item) }
                )
            }
        }
    }
}

@Composable
private fun ItemCard(
    item: AvatarItem,
    isUnlocked: Boolean,
    isSelected: Boolean,
    userXp: Int,
    onSelect: () -> Unit,
    onUnlock: () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { if (isUnlocked) onSelect() },
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isSelected) GradientStart.copy(alpha = 0.1f) else ComposeColor.White
        ),
        border = if (isSelected) BorderStroke(2.dp, GradientStart) else null
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Surface(
                        shape    = CircleShape,
                        color = run {
                            val hex = item.defaultColor.takeIf { it.startsWith("#") } ?: "#FF6B35"
                            try { ComposeColor(android.graphics.Color.parseColor(hex)) }
                            catch (_: Exception) { ComposeColor(0xFFFF6B35) }
                        },
                        modifier = Modifier.size(20.dp)
                    ) {}
                    Text(
                        text       = item.rarity.name,
                        style      = MaterialTheme.typography.labelSmall,
                        color      = ComposeColor(item.rarity.color()),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 10.sp
                    )
                }
                Text(
                    text     = item.description,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = ComposeColor.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (!isUnlocked) {
                Button(
                    onClick  = onUnlock,
                    modifier = Modifier.height(40.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = if (userXp >= item.xpCost) GradientStart else ComposeColor.LightGray
                    ),
                    enabled = userXp >= item.xpCost
                ) {
                    Text(text = "${item.xpCost} XP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint               = GradientStart,
                    modifier           = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ── Color Picker ──────────────────────────────────────────────────────────────
@Composable
private fun ColorPickerSection(onColorSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text       = "Color Customization",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(bottom = 12.dp)
        )

        val colors = listOf(
            "#FF6B35", "#667EEA", "#764BA2", "#FFD93D",
            "#FF1744", "#00BCD4", "#4CAF50", "#FFC107"
        )

        LazyRow(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(colors) { colorHex ->
                Surface(
                    shape    = CircleShape,
                    color    = ComposeColor(android.graphics.Color.parseColor(colorHex)),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onColorSelected(colorHex) }
                ) {}
            }
        }
    }
}
