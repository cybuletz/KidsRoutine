package com.kidsroutine.feature.wallet.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.SavingsGoal
import com.kidsroutine.core.model.UserModel

private val WalletGreen = Color(0xFF2E7D32)
private val WalletGreenLight = Color(0xFFE8F5E9)
private val WalletGold = Color(0xFFFFD700)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit
) {
    val viewModel: WalletViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.familyId, currentUser.userId) {
        viewModel.loadWallet(currentUser.familyId, currentUser.userId)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF0))
    ) {
        // Header
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(WalletGreen, Color(0xFF1B5E20))))
                .padding(top = 48.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.TopStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Savings, null, tint = WalletGold, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(8.dp))
                Text("Family Wallet", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("Earn & Save", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WalletGreen)
            }
            return@Column
        }

        val wallet = state.wallet

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wallet Status Card
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(WalletGreenLight)
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💰", fontSize = 28.sp)
                            Column {
                                Text("Wallet Status", fontWeight = FontWeight.ExtraBold, color = WalletGreen, fontSize = 16.sp)
                                Text(
                                    if (wallet?.isEnabled == true) "Active" else "Not Activated",
                                    color = if (wallet?.isEnabled == true) WalletGreen else Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (wallet?.isEnabled == true) {
                            Divider(color = WalletGreen.copy(alpha = 0.2f))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Exchange Rate", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        "1 XP = ${wallet.currencySymbol}${"%.2f".format(wallet.xpToMoneyRate)}",
                                        fontWeight = FontWeight.Bold,
                                        color = WalletGreen
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Currency", fontSize = 12.sp, color = Color.Gray)
                                    Text(wallet.currencySymbol, fontWeight = FontWeight.Bold, color = WalletGreen, fontSize = 20.sp)
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.enableWallet(currentUser.familyId) },
                                colors = ButtonDefaults.buttonColors(containerColor = WalletGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Enable Family Wallet ✨", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Savings Goals Header
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎯 Savings Goals", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
                    IconButton(
                        onClick = { viewModel.toggleCreateGoal() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(WalletGreen)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, "Add Goal", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Create Goal Form
            if (state.showCreateGoal) {
                item {
                    CreateGoalCard(
                        wallet = wallet,
                        onSave = { title, emoji, targetXp ->
                            viewModel.createGoal(currentUser.userId, currentUser.familyId, title, emoji, targetXp)
                        },
                        onCancel = { viewModel.toggleCreateGoal() }
                    )
                }
            }

            // Goals List
            if (state.goals.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🐷", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No savings goals yet", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Text("Tap + to create your first goal!", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }

            items(state.goals) { goal ->
                SavingsGoalCard(
                    goal = goal,
                    wallet = wallet,
                    onContribute = { viewModel.contributeToGoal(goal.goalId, 10, currentUser.userId) },
                    onDelete = { viewModel.deleteGoal(goal.goalId, currentUser.userId) }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SavingsGoalCard(
    goal: SavingsGoal,
    wallet: com.kidsroutine.core.model.FamilyWallet?,
    onContribute: () -> Unit,
    onDelete: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progressPercent,
        animationSpec = tween(600),
        label = "goalProgress"
    )

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
            .animateContentSize()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(goal.emoji, fontSize = 28.sp)
                    Column {
                        Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                        if (goal.isComplete) {
                            Text("✅ Goal reached!", color = WalletGreen, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                }
            }

            // Progress bar
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(WalletGreen, Color(0xFF4CAF50))
                            )
                        )
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${goal.currentXp} / ${goal.targetXp} XP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                if (wallet != null) {
                    Text(
                        wallet.formatMoney(goal.currentXp) + " / " + wallet.formatMoney(goal.targetXp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = WalletGreen
                    )
                }
            }

            if (!goal.isComplete) {
                Button(
                    onClick = onContribute,
                    colors = ButtonDefaults.buttonColors(containerColor = WalletGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save 10 XP 💰", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateGoalCard(
    wallet: com.kidsroutine.core.model.FamilyWallet?,
    onSave: (String, String, Int) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🎯") }
    var targetXpText by remember { mutableStateOf("500") }

    val emojiOptions = listOf("🎯", "🚲", "🎮", "📱", "🎸", "⚽", "🎒", "🧸", "📚", "🎨", "🎢", "🎁")

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WalletGreenLight)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("New Savings Goal", fontWeight = FontWeight.ExtraBold, color = WalletGreen)

            // Emoji picker
            Text("Pick an icon:", fontSize = 13.sp, color = Color.Gray)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                emojiOptions.take(6).forEach { e ->
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (emoji == e) WalletGreen.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { emoji = e },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(e, fontSize = 20.sp)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                emojiOptions.drop(6).forEach { e ->
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (emoji == e) WalletGreen.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { emoji = e },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(e, fontSize = 20.sp)
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What are you saving for?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = targetXpText,
                onValueChange = { targetXpText = it.filter { c -> c.isDigit() } },
                label = { Text("Target XP") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    val xp = targetXpText.toIntOrNull() ?: 0
                    if (wallet != null && xp > 0) {
                        Text("≈ ${wallet.formatMoney(xp)}", color = WalletGreen)
                    }
                }
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val xp = targetXpText.toIntOrNull() ?: 0
                        if (title.isNotBlank() && xp > 0) {
                            onSave(title, emoji, xp)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = WalletGreen),
                    shape = RoundedCornerShape(10.dp),
                    enabled = title.isNotBlank() && (targetXpText.toIntOrNull() ?: 0) > 0
                ) {
                    Text("Create 🎉", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
