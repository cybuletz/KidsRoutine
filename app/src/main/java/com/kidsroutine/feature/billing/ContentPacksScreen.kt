package com.kidsroutine.feature.billing

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.ContentPack
import com.kidsroutine.core.model.ContentPackTier
import com.kidsroutine.core.model.UserModel

// ── Palette ───────────────────────────────────────────────────────────────────
private val BgLight  = Color(0xFFFFFBF0)
private val ProColor = Color(0xFF9B59B6)
private val GoldXp   = Color(0xFFFFD700)
private val GreenOwn = Color(0xFF2ECC71)

@Composable
fun ContentPacksScreen(
    currentUser: UserModel,
    isPro: Boolean = false,
    onBackClick: () -> Unit,
    viewModel: ContentPacksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.userId) {
        viewModel.init(
            userXp          = currentUser.xp,
            isPro           = isPro,
            unlockedPackIds = uiState.unlockedPackIds
        )
    }

    // Auto-clear toasts
    LaunchedEffect(uiState.successMessage, uiState.error) {
        if (uiState.successMessage != null || uiState.error != null) {
            kotlinx.coroutines.delay(3_000)
            viewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgLight)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(ProColor, Color(0xFF667EEA))))
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📦 Content Packs", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Themed task bundles", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    }
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

            // ── PRO banner (if not PRO) ───────────────────────────────────────
            if (!isPro) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color    = ProColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier  = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("⭐", fontSize = 24.sp)
                        Column {
                            Text("Upgrade to PRO", fontWeight = FontWeight.Bold, color = ProColor)
                            Text("Unlock all PRO packs + unlimited AI generation", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // ── Pack list ─────────────────────────────────────────────────────
            LazyColumn(
                contentPadding        = PaddingValues(16.dp),
                verticalArrangement   = Arrangement.spacedBy(12.dp),
                modifier              = Modifier.weight(1f)
            ) {
                items(uiState.packs) { pack ->
                    PackCard(
                        pack      = pack,
                        isOwned   = pack.packId in uiState.unlockedPackIds,
                        canAfford = uiState.userXp >= pack.xpCost,
                        isPro     = isPro,
                        onUnlock  = { viewModel.unlockPack(pack, currentUser.userId) }
                    )
                }
            }
        }

        // ── Toast ──────────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = uiState.successMessage != null || uiState.error != null,
            enter    = slideInVertically { it } + fadeIn(),
            exit     = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        ) {
            Surface(
                shape            = RoundedCornerShape(12.dp),
                color            = if (uiState.error != null) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                shadowElevation  = 8.dp
            ) {
                Text(
                    text       = uiState.successMessage ?: uiState.error ?: "",
                    color      = if (uiState.error != null) Color(0xFFC62828) else Color(0xFF2E7D32),
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    }
}

// ── Pack card ─────────────────────────────────────────────────────────────────

@Composable
private fun PackCard(
    pack: ContentPack,
    isOwned: Boolean,
    canAfford: Boolean,
    isPro: Boolean,
    onUnlock: () -> Unit
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(pack.accentColor))
    } catch (e: Exception) { Color(0xFFFF6B35) }

    val isProLocked = pack.tier == ContentPackTier.PRO && !isPro

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        border    = if (isOwned) BorderStroke(2.dp, accentColor) else null,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(pack.emoji, fontSize = 28.sp)
                    Column {
                        Text(pack.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2D3436))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TierBadge(pack.tier, accentColor)
                            if (pack.taskCount > 0) TaskCountBadge("${pack.taskCount} tasks")
                        }
                    }
                }
                if (isOwned) {
                    Surface(shape = RoundedCornerShape(8.dp), color = GreenOwn.copy(alpha = 0.15f)) {
                        Icon(Icons.Default.Check, null, tint = GreenOwn, modifier = Modifier.padding(6.dp).size(20.dp))
                    }
                } else if (isProLocked) {
                    Surface(shape = RoundedCornerShape(8.dp), color = ProColor.copy(alpha = 0.1f)) {
                        Icon(Icons.Default.Lock, null, tint = ProColor, modifier = Modifier.padding(6.dp).size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(pack.description, fontSize = 13.sp, color = Color.Gray)

            // Preview task titles
            if (pack.previewTaskTitles.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Includes:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                pack.previewTaskTitles.forEach { title ->
                    Text("• $title", fontSize = 12.sp, color = Color(0xFF636E72))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Action button
            when {
                isOwned     -> Box(
                    modifier = Modifier.fillMaxWidth().background(GreenOwn.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp),
                    contentAlignment = Alignment.Center
                ) { Text("✓ Unlocked", color = GreenOwn, fontWeight = FontWeight.Bold, fontSize = 13.sp) }

                isProLocked -> Button(
                    onClick  = onUnlock,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = ProColor)
                ) { Text("⭐ PRO Required", fontSize = 13.sp, fontWeight = FontWeight.Bold) }

                pack.xpCost == 0 -> Button(
                    onClick  = onUnlock,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) { Text("🆓 Unlock Free", fontSize = 13.sp, fontWeight = FontWeight.Bold) }

                else -> Button(
                    onClick  = onUnlock,
                    enabled  = canAfford,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = accentColor,
                        disabledContainerColor = Color(0xFFCCCCCC)
                    )
                ) { Text(if (canAfford) "⭐ ${pack.xpCost} XP" else "🔒 ${pack.xpCost} XP", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun TierBadge(tier: ContentPackTier, accentColor: Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = accentColor.copy(alpha = 0.15f)) {
        Text(
            text     = tier.name,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color    = accentColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun TaskCountBadge(label: String) {
    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEEEEEE)) {
        Text(
            text     = label,
            fontSize = 10.sp,
            color    = Color.Gray,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}