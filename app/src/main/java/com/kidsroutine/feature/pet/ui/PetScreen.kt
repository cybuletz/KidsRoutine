package com.kidsroutine.feature.pet.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.PetEvolutionStage
import com.kidsroutine.core.model.PetAccessory
import com.kidsroutine.core.model.PetAccessoryCategory
import com.kidsroutine.core.model.PetModel
import com.kidsroutine.core.model.PetMood
import com.kidsroutine.core.model.PetSpecies
import com.kidsroutine.core.model.UserModel
import kotlinx.coroutines.delay

private val Accent = Color(0xFF9B59B6)
private val AccentLight = Color(0xFFD2B4DE)
private val BgLight = Color(0xFFFFFBF0)
private val TextDark = Color(0xFF2D3436)
private val HappinessGreen = Color(0xFF27AE60)
private val HappinessGreenLight = Color(0xFF2ECC71)
private val EnergyBlue = Color(0xFF2980B9)
private val EnergyBlueLight = Color(0xFF3498DB)

@Composable
fun PetScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit,
    viewModel: PetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.userId) {
        Log.d("PetScreen", "Loading pet for user: ${currentUser.userId}")
        viewModel.loadPet(currentUser.userId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.30f)
                .background(
                    Brush.verticalGradient(
                        listOf(Accent, Color(0xFF8E44AD))
                    )
                )
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
                    text = "🐾 My Pet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(40.dp))
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Accent)
                    }
                }
                uiState.adoptionMode -> {
                    AdoptionFlow(
                        uiState = uiState,
                        userId = currentUser.userId,
                        onSpeciesSelected = viewModel::selectSpecies,
                        onAdopt = { species, name ->
                            viewModel.adoptPet(currentUser.userId, species, name)
                        },
                        onClearError = viewModel::clearError
                    )
                }
                uiState.pet != null -> {
                    PetDisplay(
                        pet = uiState.pet!!,
                        currentXp = uiState.currentXp,
                        showShop = uiState.showShop,
                        ownedAccessoryIds = uiState.ownedAccessoryIds,
                        onFeed = { viewModel.feedPet() },
                        onInteract = viewModel::interactWithPet,
                        onTrain = { viewModel.trainPet() },
                        onGroom = viewModel::groomPet,
                        onAdventure = { viewModel.adventureWithPet() },
                        onNap = viewModel::napPet,
                        onTreat = { viewModel.treatPet() },
                        onTreasureHunt = { viewModel.treasureHuntWithPet() },
                        onShopClick = viewModel::toggleShop,
                        onPurchase = viewModel::purchaseAccessory,
                        onEquip = viewModel::equipAccessory
                    )
                }
            }
        }

        // Error snackbar
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = viewModel::clearError) {
                        Text("OK", color = Color.White)
                    }
                },
                containerColor = Color(0xFFE74C3C)
            ) {
                Text(error, color = Color.White)
            }
        }
    }
}

// ─── Pet Display ────────────────────────────────────────────────────────────────

@Composable
private fun PetDisplay(
    pet: PetModel,
    currentXp: Int = 0,
    showShop: Boolean = false,
    ownedAccessoryIds: List<String> = emptyList(),
    onFeed: () -> Unit,
    onInteract: () -> Unit,
    onTrain: () -> Unit = {},
    onGroom: () -> Unit = {},
    onAdventure: () -> Unit = {},
    onNap: () -> Unit = {},
    onTreat: () -> Unit = {},
    onTreasureHunt: () -> Unit = {},
    onShopClick: () -> Unit = {},
    onPurchase: (PetAccessory) -> Unit = {},
    onEquip: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var bouncing by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (bouncing) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { bouncing = false },
        label = "petBounce"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        // Pet emoji display with mood-based animations
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedPetEmoji(pet = pet)

                Spacer(Modifier.height(8.dp))

                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "${pet.species.displayName} • ${pet.stage.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(Modifier.height(12.dp))

                // Mood indicator
                MoodIndicator(mood = pet.mood)

                // Currently equipped accessory display
                pet.accessoryId?.let { accId ->
                    val equipped = PetUiState.SHOP_ITEMS.find { it.id == accId }
                    if (equipped != null) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Accent.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(equipped.emoji, fontSize = 16.sp)
                                Text(
                                    text = "Wearing: ${equipped.name}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Accent
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Available XP balance card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            elevation = CardDefaults.cardElevation(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("⭐", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "Available XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7B6B3A)
                        )
                        Text(
                            text = "$currentXp XP",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB8860B)
                        )
                    }
                }
                val minItemCost = PetUiState.SHOP_ITEMS.minOf { it.xpCost }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentXp >= minItemCost) HappinessGreen.copy(alpha = 0.15f) else Color(0xFFFFCDD2)
                ) {
                    Text(
                        text = if (currentXp >= minItemCost) "Ready to spend!" else "Earn more XP!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (currentXp >= minItemCost) HappinessGreen else Color(0xFFD32F2F),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Main Stats Card ──────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Vitals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                StatBar(
                    label = "Happiness",
                    emoji = "😊",
                    value = pet.happiness,
                    gradientColors = listOf(HappinessGreen, HappinessGreenLight)
                )

                StatBar(
                    label = "Energy",
                    emoji = "⚡",
                    value = pet.energy,
                    gradientColors = listOf(EnergyBlue, EnergyBlueLight)
                )

                StatBar(
                    label = "Style",
                    emoji = "✨",
                    value = pet.style,
                    gradientColors = listOf(Color(0xFFE91E63), Color(0xFFF48FB1))
                )

                StatBar(
                    label = "Care Level",
                    emoji = "💕",
                    value = pet.careLevel,
                    gradientColors = listOf(Color(0xFFFF6F00), Color(0xFFFFCA28))
                )

                // Evolution progress
                EvolutionProgress(stage = pet.stage)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Activity Progression Card ────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Activity Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Accent.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${pet.totalActivities} total",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Accent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Activity progress rows — each shows count + a mini progress bar
                ActivityProgressRow(emoji = "🍖", label = "Feeding", count = pet.totalFed, milestone = 20, color = HappinessGreen)
                ActivityProgressRow(emoji = "🎾", label = "Playing", count = pet.totalPlayed, milestone = 30, color = Accent)
                ActivityProgressRow(emoji = "🏋️", label = "Training", count = pet.totalTrained, milestone = 15, color = Color(0xFFE67E22))
                ActivityProgressRow(emoji = "🛁", label = "Grooming", count = pet.totalGroomed, milestone = 15, color = Color(0xFF3498DB))
                ActivityProgressRow(emoji = "🗺️", label = "Adventures", count = pet.totalAdventures, milestone = 10, color = Color(0xFF8E44AD))
                ActivityProgressRow(emoji = "😴", label = "Naps", count = pet.totalNaps, milestone = 20, color = Color(0xFF1ABC9C))
                ActivityProgressRow(emoji = "🍪", label = "Treats", count = pet.totalTreats, milestone = 15, color = Color(0xFFE74C3C))
                ActivityProgressRow(emoji = "🗝️", label = "Treasure Hunts", count = pet.totalTreasureHunts, milestone = 10, color = Color(0xFFF1C40F))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Action buttons — primary row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    bouncing = true
                    onFeed()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentXp >= PetUiState.FEED_COST) HappinessGreen else Color.Gray
                ),
                enabled = currentXp >= PetUiState.FEED_COST
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Feed 🍖",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${PetUiState.FEED_COST} XP",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Button(
                onClick = {
                    bouncing = true
                    onInteract()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Play 🎾",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Free",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Action buttons — secondary row (new activities)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    bouncing = true
                    onGroom()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498DB))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Groom 🛁", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Free", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            Button(
                onClick = {
                    bouncing = true
                    onTrain()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentXp >= PetViewModel.TRAIN_COST) Color(0xFFE67E22) else Color.Gray
                ),
                enabled = currentXp >= PetViewModel.TRAIN_COST
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Train 🏋️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${PetViewModel.TRAIN_COST} XP", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            Button(
                onClick = {
                    bouncing = true
                    onAdventure()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentXp >= PetViewModel.ADVENTURE_COST) Color(0xFF8E44AD) else Color.Gray
                ),
                enabled = currentXp >= PetViewModel.ADVENTURE_COST
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Adventure 🗺️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${PetViewModel.ADVENTURE_COST} XP", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Action buttons — third row (new activities)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    bouncing = true
                    onNap()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1ABC9C))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nap 😴", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Free", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            Button(
                onClick = {
                    bouncing = true
                    onTreat()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentXp >= PetViewModel.TREAT_COST) Color(0xFFE74C3C) else Color.Gray
                ),
                enabled = currentXp >= PetViewModel.TREAT_COST
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Treat 🍪", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${PetViewModel.TREAT_COST} XP", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            Button(
                onClick = {
                    bouncing = true
                    onTreasureHunt()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentXp >= PetViewModel.TREASURE_HUNT_COST) Color(0xFFF1C40F) else Color.Gray
                ),
                enabled = currentXp >= PetViewModel.TREASURE_HUNT_COST
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Treasure 🗝️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${PetViewModel.TREASURE_HUNT_COST} XP", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Shop button
        Button(
            onClick = onShopClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF39C12)
            )
        ) {
            Text(
                text = "🛍️ Pet Shop",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Shop section
        if (showShop) {
            Spacer(Modifier.height(16.dp))
            PetShopSection(
                currentXp = currentXp,
                ownedAccessoryIds = ownedAccessoryIds,
                equippedAccessoryId = pet.accessoryId,
                onPurchase = onPurchase,
                onEquip = onEquip
            )
        }

        Spacer(Modifier.height(16.dp))

        // Pet info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pet Info",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                InfoRow(label = "Species", value = pet.species.displayName)
                InfoRow(label = "Stage", value = "${pet.stage.emoji} ${pet.stage.displayName}")
                InfoRow(label = "Mood", value = "${pet.mood.emoji} ${pet.mood.description}")
                InfoRow(label = "Days Alive", value = "${pet.daysAlive}")
                InfoRow(label = "Total Activities", value = "${pet.totalActivities}")
                InfoRow(label = "Best Streak", value = "${pet.longestHappyStreak} days")
            }
        }

        Spacer(Modifier.height(140.dp))
    }
}

// ─── Pet Shop Section ────────────────────────────────────────────────────────────

@Composable
private fun PetShopSection(
    currentXp: Int,
    ownedAccessoryIds: List<String>,
    equippedAccessoryId: String?,
    onPurchase: (PetAccessory) -> Unit,
    onEquip: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(PetAccessoryCategory.HAT) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "🛍️ Pet Shop",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Dress up your companion!",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "💰 $currentXp XP available",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Accent
            )

            Spacer(Modifier.height(12.dp))

            // Category tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PetAccessoryCategory.entries.forEach { category ->
                    val isSelected = category == selectedCategory
                    Surface(
                        modifier = Modifier
                            .clickable { selectedCategory = category },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Accent else Color(0xFFF0F0F0)
                    ) {
                        Text(
                            text = "${category.emoji}\n${category.label}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextDark,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Items in selected category
            val categoryItems = PetUiState.SHOP_ITEMS.filter { it.category == selectedCategory }
            categoryItems.forEach { accessory ->
                // Consumable items (SNACKs) can always be re-purchased
                val isOwned = accessory.isPermanent && accessory.id in ownedAccessoryIds
                val isEquipped = accessory.isPermanent && accessory.id == equippedAccessoryId
                val canAfford = currentXp >= accessory.xpCost

                ShopItemCard(
                    accessory = accessory,
                    isOwned = isOwned,
                    isEquipped = isEquipped,
                    canAfford = canAfford,
                    onPurchase = { onPurchase(accessory) },
                    onEquip = { onEquip(accessory.id) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ShopItemCard(
    accessory: PetAccessory,
    isOwned: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
    onPurchase: () -> Unit,
    onEquip: () -> Unit
) {
    val borderColor = when {
        isEquipped -> Accent
        isOwned -> HappinessGreen
        else -> Color(0xFFE0E0E0)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isEquipped) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (isEquipped) Accent.copy(alpha = 0.05f) else Color(0xFFFAFAFA)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Emoji
            Text(
                text = accessory.emoji,
                fontSize = 32.sp
            )

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = accessory.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = accessory.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (accessory.happinessBoost > 0) {
                        Text(
                            text = "😊+${accessory.happinessBoost}",
                            fontSize = 11.sp,
                            color = HappinessGreen
                        )
                    }
                    if (accessory.energyBoost > 0) {
                        Text(
                            text = "⚡+${accessory.energyBoost}",
                            fontSize = 11.sp,
                            color = EnergyBlue
                        )
                    }
                    if (!accessory.isPermanent) {
                        val hours = accessory.durationMinutes / 60
                        val mins = accessory.durationMinutes % 60
                        val durationText = when {
                            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
                            hours > 0 -> "${hours}h"
                            else -> "${mins}m"
                        }
                        Text(
                            text = "⏱️$durationText",
                            fontSize = 11.sp,
                            color = Color(0xFFE67E22)
                        )
                    } else {
                        Text(
                            text = "♾️",
                            fontSize = 11.sp,
                            color = Accent
                        )
                    }
                }
            }

            // Action button
            when {
                isEquipped -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Accent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Equipped ✓",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Accent
                        )
                    }
                }
                isOwned -> {
                    Button(
                        onClick = onEquip,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Equip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Button(
                        onClick = onPurchase,
                        enabled = canAfford,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) Color(0xFFF39C12) else Color.Gray
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${accessory.xpCost} XP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ─── Animated Pet Emoji ─────────────────────────────────────────────────────────
@Composable
private fun AnimatedPetEmoji(pet: PetModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "petMoodAnim")

    // Find equipped accessory for visual display
    val equippedAccessory = pet.accessoryId?.let { id ->
        PetUiState.SHOP_ITEMS.find { it.id == id }
    }

    Box(contentAlignment = Alignment.Center) {
        when (pet.mood) {
        PetMood.ECSTATIC -> {
            // Bouncy excited animation
            val bounce by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bounce"
            )
            val wiggle by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(300),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "wiggle"
            )
            Text(
                text = pet.displayEmoji,
                fontSize = 80.sp,
                modifier = Modifier.graphicsLayer {
                    translationY = bounce
                    rotationZ = wiggle
                    scaleX = 1.05f
                    scaleY = 1.05f
                }
            )
            Text("✨", fontSize = 24.sp, modifier = Modifier.graphicsLayer { translationY = bounce * 0.5f })
        }
        PetMood.HAPPY -> {
            // Gentle bounce
            val bounce by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "happyBounce"
            )
            Text(
                text = pet.displayEmoji,
                fontSize = 80.sp,
                modifier = Modifier.graphicsLayer {
                    translationY = bounce
                }
            )
        }
        PetMood.CONTENT -> {
            // Subtle breathing/scale animation
            val breathe by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.03f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "breathe"
            )
            Text(
                text = pet.displayEmoji,
                fontSize = 80.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = breathe
                    scaleY = breathe
                }
            )
        }
        PetMood.BORED -> {
            // Slow side-to-side sway
            val sway by infiniteTransition.animateFloat(
                initialValue = -3f,
                targetValue = 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "sway"
            )
            Text(
                text = pet.displayEmoji,
                fontSize = 80.sp,
                modifier = Modifier.graphicsLayer {
                    rotationZ = sway
                    alpha = 0.85f
                }
            )
        }
        PetMood.SAD -> {
            // Droopy/sagging animation
            val droop by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "droop"
            )
            val tilt by infiniteTransition.animateFloat(
                initialValue = -2f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "sadTilt"
            )
            Text(
                text = pet.displayEmoji,
                fontSize = 80.sp,
                modifier = Modifier.graphicsLayer {
                    translationY = droop
                    rotationZ = tilt
                    alpha = 0.75f
                    scaleX = 0.95f
                    scaleY = 0.95f
                }
            )
            Text("💧", fontSize = 18.sp)
        }
        PetMood.SLEEPING -> {
            // Sleeping/breathing with Zzz
            val breathe by infiniteTransition.animateFloat(
                initialValue = 0.92f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "sleepBreathe"
            )
            val zzzAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "zzz"
            )
            val zzzOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500),
                    repeatMode = RepeatMode.Restart
                ),
                label = "zzzOffset"
            )
            Box {
                Text(
                    text = pet.displayEmoji,
                    fontSize = 80.sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = breathe
                        scaleY = breathe
                        alpha = 0.7f
                    }
                )
                Text(
                    "💤",
                    fontSize = 22.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .graphicsLayer {
                            alpha = zzzAlpha
                            translationY = zzzOffset
                            translationX = 10f
                        }
                )
            }
            }
        }

        // Equipped accessory visual overlay
        if (equippedAccessory != null) {
            val accessoryBounce by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "accessoryBounce"
            )
            // Position accessory based on category
            val alignment = when (equippedAccessory.category) {
                PetAccessoryCategory.HAT     -> Alignment.TopCenter
                PetAccessoryCategory.COLLAR  -> Alignment.CenterEnd
                PetAccessoryCategory.TOY     -> Alignment.BottomEnd
                PetAccessoryCategory.BED     -> Alignment.BottomCenter
                PetAccessoryCategory.SNACK   -> Alignment.CenterStart
            }
            Box(
                modifier = Modifier
                    .align(alignment)
                    .graphicsLayer {
                        translationY = accessoryBounce
                    }
            ) {
                Surface(
                    shape = CircleShape,
                    color = Accent.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(equippedAccessory.emoji, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodIndicator(mood: PetMood) {
    val moodColor by animateColorAsState(
        targetValue = when (mood) {
            PetMood.ECSTATIC -> Color(0xFFF39C12)
            PetMood.HAPPY -> Color(0xFF27AE60)
            PetMood.CONTENT -> Color(0xFF3498DB)
            PetMood.BORED -> Color(0xFF95A5A6)
            PetMood.SAD -> Color(0xFFE74C3C)
            PetMood.SLEEPING -> Color(0xFF8E44AD)
        },
        animationSpec = tween(durationMillis = 500),
        label = "moodColor"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = moodColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = "${mood.emoji} ${mood.description}",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = moodColor
        )
    }
}

// ─── Stat Bar ───────────────────────────────────────────────────────────────────

@Composable
private fun StatBar(
    label: String,
    emoji: String,
    value: Int,
    gradientColors: List<Color>
) {
    val animatedValue by animateFloatAsState(
        targetValue = value / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "statBar_$label"
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$emoji $label",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            Text(
                text = "$value / 100",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFEEEEEE))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedValue)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Brush.horizontalGradient(gradientColors))
            )
        }
    }
}

// ─── Evolution Progress ─────────────────────────────────────────────────────────

@Composable
private fun EvolutionProgress(stage: PetEvolutionStage) {
    val stages = PetEvolutionStage.entries
    val currentIndex = stage.ordinal

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "🌱 Evolution",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextDark
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            stages.forEachIndexed { index, s ->
                val isReached = index <= currentIndex
                val isCurrent = index == currentIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 36.dp else 28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isReached) Accent else Color(0xFFE0E0E0)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = s.emoji,
                            fontSize = if (isCurrent) 16.sp else 12.sp
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = s.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isReached) Accent else Color.Gray,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

// ─── Info Row ───────────────────────────────────────────────────────────────────

// ─── Activity Progress Row ──────────────────────────────────────────────────────

@Composable
private fun ActivityProgressRow(
    emoji: String,
    label: String,
    count: Int,
    milestone: Int,
    color: Color
) {
    // Calculate which milestone tier we're in (repeating milestones)
    val currentTierStart = (count / milestone) * milestone
    val nextMilestone = currentTierStart + milestone
    val progress = if (milestone > 0) {
        ((count - currentTierStart).toFloat() / milestone).coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "activityProgress_$label"
    )
    val tier = count / milestone  // How many milestones completed

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Emoji
        Text(text = emoji, fontSize = 18.sp, modifier = Modifier.width(26.dp))

        // Label + count
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
                Text(
                    text = "$count / $nextMilestone",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(3.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFEEEEEE))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = animatedProgress)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
        }

        // Tier badge (star count for milestones completed)
        if (tier > 0) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "⭐$tier",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )
    }
}

// ─── Adoption Flow ──────────────────────────────────────────────────────────────

@Composable
private fun AdoptionFlow(
    uiState: PetUiState,
    userId: String,
    onSpeciesSelected: (PetSpecies) -> Unit,
    onAdopt: (PetSpecies, String) -> Unit,
    onClearError: () -> Unit
) {
    var petName by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🥚",
                    fontSize = 56.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Adopt a Pet!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Choose your companion and give them a name",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Species selection grid
        Text(
            text = "Choose a Species",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(PetSpecies.entries) { species ->
                SpeciesCard(
                    species = species,
                    isSelected = uiState.selectedSpecies == species,
                    onClick = { onSpeciesSelected(species) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Name input
        AnimatedVisibility(
            visible = uiState.selectedSpecies != null,
            enter = fadeIn() + scaleIn(initialScale = 0.9f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = petName,
                    onValueChange = {
                        petName = it
                        onClearError()
                    },
                    label = { Text("Pet Name") },
                    placeholder = { Text("What will you call your pet?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        cursorColor = Accent,
                        focusedLabelColor = Accent
                    )
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        uiState.selectedSpecies?.let { species ->
                            onAdopt(species, petName.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    enabled = petName.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Adopt! 🎉",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─── Species Card ───────────────────────────────────────────────────────────────

@Composable
private fun SpeciesCard(
    species: PetSpecies,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Accent else Color.Transparent,
        animationSpec = tween(300),
        label = "speciesBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, borderColor, RoundedCornerShape(16.dp))
                } else {
                    Modifier
                }
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccentLight.copy(alpha = 0.3f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = species.emoji,
                fontSize = 32.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = species.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Accent else TextDark,
                textAlign = TextAlign.Center
            )
        }
    }
}
