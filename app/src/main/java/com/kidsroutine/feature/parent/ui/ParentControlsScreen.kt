package com.kidsroutine.feature.parent.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.ParentControlSettings
import com.kidsroutine.core.model.PlanType
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.core.model.XpLoan
import com.kidsroutine.core.model.XpLoanStatus

private val OrangePrimary = Color(0xFFFF6B35)
private val TextDark = Color(0xFF2D3436)
private val BgLight = Color(0xFFFFFBF0)
private val ProPurple = Color(0xFF6C63FF)
private val SuccessGreen = Color(0xFF27AE60)
private val DangerRed = Color(0xFFE74C3C)

@Composable
fun ParentControlsScreen(
    currentUser: UserModel,
    child: UserModel,
    onBackClick: () -> Unit,
    onUpgradeClick: () -> Unit = {},
    viewModel: ParentControlsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(child.userId) {
        viewModel.loadForChild(currentUser.familyId, child.userId, currentUser.userId)
    }

    val isPro = uiState.entitlements.planType != PlanType.FREE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        // ── Header ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "⚙️ Controls for ${child.displayName}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        "Manage Fun Zone, difficulty, and XP bank",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // ── Content ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Pro upsell banner for Free users ────────────────────────
            if (!isPro) {
                ProUpsellBanner(onUpgradeClick = onUpgradeClick)
            }

            // ── Fun Zone Visibility ─────────────────────────────────────
            FunZoneVisibilitySection(
                settings = uiState.controlSettings,
                isPro = isPro,
                entitlements = uiState.entitlements,
                onToggle = { key, enabled -> viewModel.toggleFunZoneFeature(key, enabled) }
            )

            // ── Quest Difficulty Tiers ──────────────────────────────────
            QuestDifficultySection(
                settings = uiState.controlSettings,
                isPro = isPro,
                onDefaultDifficultyChange = { viewModel.setDefaultDifficulty(it) },
                onToggleDifficulty = { diff, allowed -> viewModel.toggleAllowedDifficulty(diff, allowed) },
                onMultiplierChange = { diff, mult -> viewModel.setXpMultiplier(diff, mult) }
            )

            // ── XP Economy Caps ─────────────────────────────────────────
            XpEconomySection(
                settings = uiState.controlSettings,
                isPro = isPro,
                onEarningCapChange = { viewModel.setDailyXpEarningCap(it) },
                onSpendingCapChange = { viewModel.setDailyXpSpendingCap(it) }
            )

            // ── XP Bank ─────────────────────────────────────────────────
            XpBankSection(
                activeLoans = uiState.activeLoans,
                isPro = isPro,
                child = child,
                onLendClick = { viewModel.showLoanDialog() },
                onForgiveClick = { loanId -> viewModel.showForgiveDialog(loanId) },
                onCancelClick = { loanId -> viewModel.cancelLoan(loanId, currentUser.familyId) }
            )

            Spacer(Modifier.height(140.dp))
        }
    }

    // ── Loan Dialog ─────────────────────────────────────────────────────
    if (uiState.showLoanDialog) {
        CreateLoanDialog(
            childName = child.displayName,
            onConfirm = { amount, repayPct, note ->
                viewModel.createLoan(
                    familyId = currentUser.familyId,
                    parentId = currentUser.userId,
                    childId = child.userId,
                    childName = child.displayName,
                    amount = amount,
                    repaymentPercentage = repayPct,
                    note = note
                )
            },
            onDismiss = { viewModel.dismissLoanDialog() }
        )
    }

    // ── Forgive Dialog ──────────────────────────────────────────────────
    if (uiState.showForgiveDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissForgiveDialog() },
            title = { Text("Forgive Loan?", fontWeight = FontWeight.Bold) },
            text = { Text("This will forgive the remaining balance. The child keeps the XP already received.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.forgiveLoan(uiState.selectedLoanId, currentUser.familyId)
                }) { Text("Forgive", color = SuccessGreen) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissForgiveDialog() }) { Text("Cancel") }
            }
        )
    }

    // ── Success/Error snackbar ──────────────────────────────────────────
    uiState.successMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pro Upsell Banner
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProUpsellBanner(onUpgradeClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUpgradeClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ProPurple.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("⭐", fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text("Upgrade to Pro", fontWeight = FontWeight.Bold, color = ProPurple)
                Text(
                    "Unlock parent controls, XP bank, custom difficulty settings, and more!",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ProPurple
            ) {
                Text("Upgrade", color = Color.White, fontWeight = FontWeight.Bold,
                    fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fun Zone Visibility Section
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FunZoneVisibilitySection(
    settings: ParentControlSettings,
    isPro: Boolean,
    entitlements: com.kidsroutine.core.model.UserEntitlements,
    onToggle: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎮", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Fun Zone Visibility",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
            Text(
                "Choose which Fun Zone features are visible to this child",
                fontSize = 12.sp,
                color = Color.Gray
            )

            HorizontalDivider(color = Color(0xFFF0F0F0))

            ParentControlSettings.ALL_FUN_ZONE_FEATURES.forEach { feature ->
                val isEnabled = settings.isFunZoneFeatureEnabled(feature.key)
                val availableInPlan = entitlements.hasFunZoneFeature(feature.key)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(feature.emoji, fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(feature.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                            if (!availableInPlan) {
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = ProPurple.copy(alpha = 0.15f)
                                ) {
                                    Text("PRO", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                        color = ProPurple, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Text(feature.description, fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isEnabled && availableInPlan,
                        onCheckedChange = { if (isPro || availableInPlan) onToggle(feature.key, it) },
                        enabled = isPro || availableInPlan,
                        colors = SwitchDefaults.colors(checkedTrackColor = OrangePrimary)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Quest Difficulty Section
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuestDifficultySection(
    settings: ParentControlSettings,
    isPro: Boolean,
    onDefaultDifficultyChange: (DifficultyLevel) -> Unit,
    onToggleDifficulty: (DifficultyLevel, Boolean) -> Unit,
    onMultiplierChange: (DifficultyLevel, Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Quest Difficulty Tiers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                if (!isPro) {
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = ProPurple.copy(alpha = 0.15f)) {
                        Text("PRO", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            color = ProPurple, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Text(
                "Configure which difficulty levels are available and XP reward multipliers",
                fontSize = 12.sp,
                color = Color.Gray
            )

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // Allowed difficulties
            Text("Allowed Difficulties", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)
            DifficultyLevel.entries.forEach { difficulty ->
                val isAllowed = difficulty in settings.allowedDifficulties
                val diffInfo = when (difficulty) {
                    DifficultyLevel.EASY   -> Triple("⭐",      "Easy",   Color(0xFF27AE60))
                    DifficultyLevel.MEDIUM -> Triple("⭐⭐",    "Medium", Color(0xFFFF9800))
                    DifficultyLevel.HARD   -> Triple("⭐⭐⭐",  "Hard",   Color(0xFFE74C3C))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(diffInfo.first, fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(diffInfo.second, fontWeight = FontWeight.SemiBold, color = diffInfo.third, modifier = Modifier.weight(1f))

                    // XP multiplier
                    Text(
                        "${settings.xpMultiplierFor(difficulty)}×",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(Modifier.width(8.dp))

                    Switch(
                        checked = isAllowed,
                        onCheckedChange = { if (isPro) onToggleDifficulty(difficulty, it) },
                        enabled = isPro,
                        colors = SwitchDefaults.colors(checkedTrackColor = diffInfo.third)
                    )
                }
            }

            if (isPro) {
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Text("XP Multipliers", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)
                Text("Adjust how much XP each difficulty tier rewards", fontSize = 11.sp, color = Color.Gray)

                DifficultyLevel.entries.forEach { difficulty ->
                    val multiplier = settings.xpMultiplierFor(difficulty)
                    val label = when (difficulty) {
                        DifficultyLevel.EASY   -> "Easy"
                        DifficultyLevel.MEDIUM -> "Medium"
                        DifficultyLevel.HARD   -> "Hard"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, fontSize = 13.sp, modifier = Modifier.width(60.dp))
                        Slider(
                            value = multiplier,
                            onValueChange = { onMultiplierChange(difficulty, it) },
                            valueRange = 0.5f..5.0f,
                            steps = 8,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary)
                        )
                        Text(
                            "${String.format("%.1f", multiplier)}×",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))
                Text("Default Difficulty", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DifficultyLevel.entries.forEach { difficulty ->
                        val isSelected = settings.defaultDifficulty == difficulty
                        val color = when (difficulty) {
                            DifficultyLevel.EASY   -> Color(0xFF27AE60)
                            DifficultyLevel.MEDIUM -> Color(0xFFFF9800)
                            DifficultyLevel.HARD   -> Color(0xFFE74C3C)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFF5F5F5),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onDefaultDifficultyChange(difficulty) }
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) color else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Text(
                                difficulty.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (isSelected) color else Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// XP Economy Section
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun XpEconomySection(
    settings: ParentControlSettings,
    isPro: Boolean,
    onEarningCapChange: (Int) -> Unit,
    onSpendingCapChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💎", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "XP Economy Limits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                if (!isPro) {
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = ProPurple.copy(alpha = 0.15f)) {
                        Text("PRO", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            color = ProPurple, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Text(
                "Set daily limits on XP earning and spending (0 = unlimited)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            if (isPro) {
                HorizontalDivider(color = Color(0xFFF0F0F0))

                CapSlider(
                    label = "Daily Earning Cap",
                    emoji = "📈",
                    value = settings.dailyXpEarningCap,
                    onValueChange = onEarningCapChange
                )
                CapSlider(
                    label = "Daily Spending Cap",
                    emoji = "📉",
                    value = settings.dailyXpSpendingCap,
                    onValueChange = onSpendingCapChange
                )
            }
        }
    }
}

@Composable
private fun CapSlider(
    label: String,
    emoji: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(
                if (value == 0) "Unlimited" else "$value XP/day",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = OrangePrimary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..500f,
            steps = 9,
            colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// XP Bank Section
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun XpBankSection(
    activeLoans: List<XpLoan>,
    isPro: Boolean,
    child: UserModel,
    onLendClick: () -> Unit,
    onForgiveClick: (String) -> Unit,
    onCancelClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏦", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "XP Bank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                if (!isPro) {
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = ProPurple.copy(alpha = 0.15f)) {
                        Text("PRO", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            color = ProPurple, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Text(
                "Lend XP to ${child.displayName} — they can spend it now and repay from future task earnings",
                fontSize = 12.sp,
                color = Color.Gray
            )

            if (isPro) {
                HorizontalDivider(color = Color(0xFFF0F0F0))

                // Current child XP
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF8E1), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${child.displayName}'s XP Balance", fontSize = 13.sp, color = TextDark)
                    Text("⭐ ${child.xp} XP", fontWeight = FontWeight.Bold, color = OrangePrimary)
                }

                // Active loans
                if (activeLoans.isNotEmpty()) {
                    Text("Active Loans", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)

                    activeLoans.forEach { loan ->
                        LoanCard(
                            loan = loan,
                            onForgiveClick = { onForgiveClick(loan.loanId) },
                            onCancelClick = { onCancelClick(loan.loanId) }
                        )
                    }
                } else {
                    Text(
                        "No active loans",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Lend XP button
                Button(
                    onClick = onLendClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("🏦 Lend XP to ${child.displayName}", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun LoanCard(
    loan: XpLoan,
    onForgiveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💰 ${loan.amount} XP loan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                Text(
                    "${(loan.progressPercent * 100).toInt()}% repaid",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { loan.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SuccessGreen,
                trackColor = Color(0xFFE0E0E0)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Repaid: ${loan.amountRepaid}/${loan.amount} XP", fontSize = 11.sp, color = Color.Gray)
                Text("${loan.repaymentPercentage}% auto-deduction", fontSize = 11.sp, color = Color.Gray)
            }

            if (loan.note.isNotEmpty()) {
                Text("📝 \"${loan.note}\"", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onForgiveClick) {
                    Text("Forgive", fontSize = 12.sp, color = SuccessGreen)
                }
                TextButton(onClick = onCancelClick) {
                    Text("Cancel", fontSize = 12.sp, color = DangerRed)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Create Loan Dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CreateLoanDialog(
    childName: String,
    onConfirm: (amount: Int, repayPct: Int, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("50") }
    var repayPct by remember { mutableStateOf("20") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🏦 Lend XP to $childName", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("The XP will be added to their balance immediately. They repay through future task earnings.", fontSize = 13.sp, color = Color.Gray)

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() } },
                    label = { Text("Amount (XP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = repayPct,
                    onValueChange = { repayPct = it.filter { c -> c.isDigit() } },
                    label = { Text("Auto-repayment % per task") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("% of each task's XP reward goes toward repayment") }
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., For the pet hat you wanted!") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toIntOrNull() ?: 0
                    val pct = (repayPct.toIntOrNull() ?: 20).coerceIn(0, 100)
                    if (amt > 0) onConfirm(amt, pct, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("Lend XP", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
