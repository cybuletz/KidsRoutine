package com.kidsroutine.feature.billing

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.PlanType
import com.kidsroutine.core.model.UserModel

private val GoldTop    = Color(0xFFFFD700)
private val GoldBottom = Color(0xFFFF8C00)
private val PurpleTop  = Color(0xFF7C3AED)
private val PurpleBot  = Color(0xFF4F46E5)
private val BgDark     = Color(0xFF0F0F1A)
private val CardBg     = Color(0xFF1E1E3F)
private val GreenAccent = Color(0xFF10B981)

@Composable
fun UpgradeScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit,
    onUpgradeSuccess: (PlanType) -> Unit = {},
    viewModel: BillingViewModel = hiltViewModel()
) {
    val uiState  by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as? Activity
    var selectedPlan by remember { mutableStateOf(PlanType.PRO) }

    // Initialise billing client when screen opens
    LaunchedEffect(currentUser.userId) {
        viewModel.init(currentUser.userId)
    }

    // React to purchase outcome
    LaunchedEffect(uiState.purchaseState) {
        when (val state = uiState.purchaseState) {
            is PurchaseState.Success -> {
                viewModel.onPurchaseSuccess(currentUser.userId, state.planType)
                onUpgradeSuccess(state.planType)
                viewModel.resetPurchaseState()
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            IconButton(
                onClick  = onBackClick,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }

            UpgradeHero()
            Spacer(Modifier.height(24.dp))

            // ── Current Free Plan ────────────────────────────────────────
            FreePlanCard(
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(16.dp))

            // Live prices from Play Store (fallback to hardcoded if not loaded)
            val proPrice     = uiState.products.find { it.productId == BillingProducts.PRO_MONTHLY }
                ?.formattedPrice ?: "$4.99 / month"
            val premiumPrice = uiState.products.find { it.productId == BillingProducts.PREMIUM_MONTHLY }
                ?.formattedPrice ?: "$9.99 / month"

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PlanCard(
                    plan       = PlanType.PRO,
                    isSelected = selectedPlan == PlanType.PRO,
                    onSelect   = { selectedPlan = PlanType.PRO },
                    price      = proPrice,
                    features   = listOf(
                        "20 AI-powered task generations / day",
                        "5 AI challenge generations / day",
                        "3 AI daily plans / day",
                        "4 weekly family plans / month",
                        "Boss battles, Story arcs, Events",
                        "Skill trees & progression system",
                        "Full parent controls & XP bank",
                        "Up to 5 children per family",
                        "Custom difficulty settings"
                    ),
                    highlightText = "Most Popular"
                )
                PlanCard(
                    plan       = PlanType.PREMIUM,
                    isSelected = selectedPlan == PlanType.PREMIUM,
                    onSelect   = { selectedPlan = PlanType.PREMIUM },
                    price      = premiumPrice,
                    features   = listOf(
                        "Unlimited AI generations (tasks, challenges, plans)",
                        "30 weekly family plans / month",
                        "Seasonal themes & exclusive content",
                        "All Fun Zone features unlocked",
                        "All PRO features included",
                        "Up to 20 children per family",
                        "Priority support & early access",
                        "Story-based tasks & custom narratives"
                    ),
                    highlightText = "Best Value"
                )
            }

            Spacer(Modifier.height(28.dp))

            // Error message
            if (uiState.error != null) {
                Text(
                    text      = uiState.error!!,
                    color     = Color(0xFFFF6B6B),
                    fontSize  = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            // Purchase state feedback
            when (uiState.purchaseState) {
                is PurchaseState.Pending -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GoldTop)
                    }
                }
                is PurchaseState.Cancelled -> {
                    Text(
                        "Purchase cancelled. Tap below to try again.",
                        color     = Color.White.copy(alpha = 0.6f),
                        fontSize  = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    )
                }
                else -> Unit
            }

            // CTA button
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = GoldTop) }
            } else {
                UpgradeCta(
                    selectedPlan  = selectedPlan,
                    isLoading     = uiState.purchaseState is PurchaseState.Pending,
                    onUpgradeClick = {
                        viewModel.clearError()
                        if (activity != null) {
                            viewModel.purchase(activity, selectedPlan)
                        }
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Why KidsRoutine is Better ────────────────────────────────
            WhyBetterSection(modifier = Modifier.padding(horizontal = 20.dp))

            Spacer(Modifier.height(20.dp))

            // ── Feature Comparison vs Other Apps ─────────────────────────
            CompetitiveComparisonSection(modifier = Modifier.padding(horizontal = 20.dp))

            Spacer(Modifier.height(20.dp))

            // Restore purchases
            TextButton(
                onClick  = { viewModel.restorePurchases(currentUser.userId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Restore purchases",
                    color    = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }

            Text(
                "Cancel anytime · Secure payment via Google Play · Auto-renews monthly",
                fontSize  = 11.sp,
                color     = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Hero ────────────────────────────────────────────────────────────────────

@Composable
private fun UpgradeHero() {
    val infiniteTransition = rememberInfiniteTransition(label = "crownPulse")
    val crownScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.12f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label         = "crown"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Brush.verticalGradient(listOf(PurpleTop, PurpleBot))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(crownScale)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(GoldTop, GoldBottom))),
                contentAlignment = Alignment.Center
            ) { Text("👑", fontSize = 40.sp) }

            Text(
                "Unlock the Full Experience",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                "AI-powered tasks, story arcs, boss battles & more",
                fontSize  = 13.sp,
                color     = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

// ─── Free Plan Card ──────────────────────────────────────────────────────────

@Composable
private fun FreePlanCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🆓", fontSize = 18.sp)
                Text(
                    "Your Current Free Plan",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White.copy(alpha = 0.7f)
                )
            }
            val freeFeatures = listOf(
                "✅ 3 AI task suggestions / day",
                "✅ Pet companion",
                "✅ Daily spin wheel",
                "✅ Family rituals",
                "✅ 2 trial AI challenge prompts",
                "✅ 2 trial AI daily plan prompts",
                "❌ Boss battles, Story arcs, Events",
                "❌ Skill trees & progression",
                "❌ Parent controls & XP bank",
                "❌ Weekly family planner"
            )
            freeFeatures.forEach { feature ->
                val isIncluded = feature.startsWith("✅")
                Text(
                    feature,
                    fontSize = 11.sp,
                    color    = if (isIncluded) Color.White.copy(alpha = 0.5f)
                               else Color(0xFFFF6B6B).copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ─── Plan card ───────────────────────────────────────────────────────────────

@Composable
private fun PlanCard(
    plan: PlanType,
    isSelected: Boolean,
    onSelect: () -> Unit,
    price: String,
    features: List<String>,
    highlightText: String? = null
) {
    val borderColor = if (isSelected) GoldTop else Color.White.copy(alpha = 0.15f)
    val bgColor     = if (isSelected) CardBg.copy(alpha = 0.95f) else CardBg.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width  = if (isSelected) 2.dp else 1.dp,
                color  = borderColor,
                shape  = RoundedCornerShape(16.dp)
            ),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(plan.emoji, fontSize = 20.sp)
                    Text(
                        plan.displayName,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isSelected) GoldTop else Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(price, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                    if (highlightText != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (plan == PlanType.PRO) GreenAccent.copy(alpha = 0.2f) else GoldTop.copy(alpha = 0.2f)
                        ) {
                            Text(
                                highlightText,
                                fontSize   = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (plan == PlanType.PRO) GreenAccent else GoldTop,
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            if (isSelected) {
                Surface(shape = RoundedCornerShape(6.dp), color = GoldTop.copy(alpha = 0.2f)) {
                    Text(
                        "✓ Selected",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = GoldTop,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            features.forEach { feature ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✅", fontSize = 12.sp)
                    Text(feature, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                }
            }
        }
    }
}

// ─── Why KidsRoutine is Better ───────────────────────────────────────────────

@Composable
private fun WhyBetterSection(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2744))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "🏆 Why KidsRoutine Stands Out",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = GoldTop
            )

            val advantages = listOf(
                Triple("🤖", "AI-Personalized Tasks",
                    "Gemini AI creates unique, age-appropriate tasks tailored to YOUR child's interests and developmental stage. No generic checklists."),
                Triple("📖", "Story-Driven Adventures",
                    "Multi-day narrative arcs turn chores into epic quests. Kids complete chapters, not tasks — motivation through storytelling."),
                Triple("⚔️", "Family Boss Battles",
                    "Collaborative family challenges where everyone works together to defeat weekly bosses. No other app has real-time family co-op."),
                Triple("🌳", "Skill Trees & Progression",
                    "RPG-style skill trees let kids unlock abilities as they grow. Visual proof of growth that other sticker-chart apps can't match."),
                Triple("💰", "Financial Literacy Built In",
                    "Wallet system teaches real money concepts through XP savings goals. Your child learns finance while having fun."),
                Triple("🎮", "8+ Fun Zone Activities",
                    "Pet companion, daily spin, events, rituals, and more. A complete engagement ecosystem, not just a to-do list."),
                Triple("👨\u200D👩\u200D👧", "Per-Child Parent Controls",
                    "Fine-grained controls per child: toggle features, set difficulty, manage XP economy. Other apps offer one-size-fits-all."),
                Triple("🧠", "Adaptive Difficulty",
                    "AI adjusts task difficulty based on performance. Tasks grow with your child — never too easy, never too hard.")
            )

            advantages.forEach { (emoji, title, desc) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(emoji, fontSize = 24.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            title,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                        Text(
                            desc,
                            fontSize = 11.sp,
                            color    = Color.White.copy(alpha = 0.6f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Competitive Comparison ──────────────────────────────────────────────────

@Composable
private fun CompetitiveComparisonSection(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2744))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "📊 How We Compare",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = GoldTop
            )
            Text(
                "See what makes KidsRoutine the complete family solution",
                fontSize = 11.sp,
                color    = Color.White.copy(alpha = 0.5f)
            )

            // Comparison rows
            val comparisons = listOf(
                Triple("AI-Personalized Tasks", true, false),
                Triple("Story-Driven Quests", true, false),
                Triple("Family Boss Battles", true, false),
                Triple("Skill Tree Progression", true, false),
                Triple("Financial Literacy", true, false),
                Triple("Per-Child Controls", true, false),
                Triple("Daily Spin Rewards", true, true),
                Triple("Avatar Customization", true, true),
                Triple("Basic Task Lists", true, true),
                Triple("Adaptive Difficulty", true, false),
                Triple("XP Bank & Loans", true, false),
                Triple("Weekly Family Plans", true, false)
            )

            // Header
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Feature",
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White.copy(alpha = 0.5f),
                    modifier   = Modifier.weight(1f)
                )
                Text(
                    "KidsRoutine",
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GoldTop,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.width(80.dp)
                )
                Text(
                    "Others",
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White.copy(alpha = 0.4f),
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.width(60.dp)
                )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            comparisons.forEach { (feature, us, them) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        feature,
                        fontSize = 11.sp,
                        color    = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (us) "✅" else "❌",
                        fontSize  = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.width(80.dp)
                    )
                    Text(
                        if (them) "⚠️" else "❌",
                        fontSize  = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.width(60.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "⚠️ = Limited/partial implementation in competing apps",
                fontSize = 9.sp,
                color    = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

// ─── CTA button ──────────────────────────────────────────────────────────────

@Composable
private fun UpgradeCta(
    selectedPlan: PlanType,
    isLoading: Boolean,
    onUpgradeClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue   = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "ctaScale"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .scale(scale)
    ) {
        Button(
            onClick = {
                pressed = true
                onUpgradeClick()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp),
            enabled = !isLoading
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(GoldTop, GoldBottom)),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color    = Color(0xFF1A1A2E),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Upgrade to ${selectedPlan.displayName} ${selectedPlan.emoji}",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF1A1A2E)
                    )
                }
            }
        }
    }
}