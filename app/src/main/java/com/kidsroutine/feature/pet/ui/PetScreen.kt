package com.kidsroutine.feature.pet.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.PetEvolutionStage
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
                        onFeed = viewModel::feedPet,
                        onInteract = viewModel::interactWithPet
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
    onFeed: () -> Unit,
    onInteract: () -> Unit
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

        // Pet emoji display
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
                    text = pet.displayEmoji,
                    fontSize = 80.sp,
                    modifier = Modifier.scale(scale)
                )

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
            }
        }

        Spacer(Modifier.height(16.dp))

        // Stats bars
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
                    text = "Stats",
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

                // Evolution progress
                EvolutionProgress(stage = pet.stage)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Action buttons
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
                colors = ButtonDefaults.buttonColors(containerColor = HappinessGreen)
            ) {
                Text(
                    text = "Feed 🍖",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
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
                Text(
                    text = "Play 🎾",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
                InfoRow(label = "Days Alive", value = "${pet.daysAlive}")
                InfoRow(label = "Times Fed", value = "${pet.totalFed}")
                InfoRow(label = "Best Streak", value = "${pet.longestHappyStreak} days")
            }
        }

        Spacer(Modifier.height(140.dp))
    }
}

// ─── Mood Indicator ─────────────────────────────────────────────────────────────

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
