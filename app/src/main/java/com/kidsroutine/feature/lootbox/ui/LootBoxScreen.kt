package com.kidsroutine.feature.lootbox.ui

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.kidsroutine.core.model.*
import kotlinx.coroutines.delay

private val BgDark       = Color(0xFF0D0D2B)
private val GoldLight    = Color(0xFFFFD700)
private val OrangePrimary = Color(0xFFFF6B35)

// ── Rarity colors ────────────────────────────────────────────────────────────
private fun rarityColor(rarity: LootBoxRarity) = when (rarity) {
    LootBoxRarity.COMMON    -> Color(0xFF9E9E9E)
    LootBoxRarity.RARE      -> Color(0xFF2196F3)
    LootBoxRarity.EPIC      -> Color(0xFF9B5DE5)
    LootBoxRarity.LEGENDARY -> Color(0xFFFFD700)
}

private fun rarityLabel(rarity: LootBoxRarity) = when (rarity) {
    LootBoxRarity.COMMON    -> "Common"
    LootBoxRarity.RARE      -> "Rare ✨"
    LootBoxRarity.EPIC      -> "Epic 🔥"
    LootBoxRarity.LEGENDARY -> "LEGENDARY 👑"
}

// ── Screen states ─────────────────────────────────────────────────────────────
private enum class OpenState { IDLE, SHAKING, REVEALING, DONE }

@Composable
fun LootBoxScreen(
    lootBox: LootBox,
    onBack: () -> Unit = {},
    onClaim: (LootBox) -> Unit = {}
) {

    var openState by remember { mutableStateOf(OpenState.IDLE) }
    var triggerAnimation by remember { mutableStateOf(false) }  // ✅ ADD THIS

    val shownReward = remember {
        lootBox.reward ?: run {
            val rewardPool = listOf(
                LootBoxReward(type = LootBoxRewardType.XP_BOOST, rarity = LootBoxRarity.COMMON, title = "XP Surge", description = "+25 bonus XP!", emoji = "⚡", xpValue = 25),
                LootBoxReward(type = LootBoxRewardType.XP_BOOST, rarity = LootBoxRarity.RARE, title = "Double XP", description = "+75 bonus XP!", emoji = "🔥", xpValue = 75),
                LootBoxReward(type = LootBoxRewardType.XP_BOOST, rarity = LootBoxRarity.EPIC, title = "XP Explosion", description = "+150 XP mega bonus!", emoji = "💥", xpValue = 150),
                LootBoxReward(type = LootBoxRewardType.STREAK_SHIELD, rarity = LootBoxRarity.RARE, title = "Streak Shield", description = "Your streak is protected!", emoji = "🛡️", xpValue = 0),
                LootBoxReward(type = LootBoxRewardType.BADGE, rarity = LootBoxRarity.EPIC, title = "Mystery Badge", description = "A rare achievement badge!", emoji = "🏅", xpValue = 0),
                LootBoxReward(type = LootBoxRewardType.MYSTERY, rarity = LootBoxRarity.LEGENDARY, title = "Legendary Drop", description = "Something truly special!", emoji = "🌟", xpValue = 200),
                LootBoxReward(type = LootBoxRewardType.AVATAR_ITEM, rarity = LootBoxRarity.COMMON, title = "Avatar Flair", description = "A cool new avatar accessory!", emoji = "🎨", xpValue = 0),
                LootBoxReward(type = LootBoxRewardType.XP_BOOST, rarity = LootBoxRarity.COMMON, title = "Quick Boost", description = "+10 XP!", emoji = "✨", xpValue = 10),
            )
            val roll = (1..100).random()
            val rarity = when {
                roll <= 3 -> LootBoxRarity.LEGENDARY
                roll <= 15 -> LootBoxRarity.EPIC
                roll <= 40 -> LootBoxRarity.RARE
                else -> LootBoxRarity.COMMON
            }
            rewardPool.firstOrNull { it.rarity == rarity } ?: rewardPool.first()
        }
    }

    // Shake animation
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val shakeX by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue  = 12f,
        animationSpec = infiniteRepeatable(tween(80, easing = LinearEasing), RepeatMode.Reverse),
        label = "shakeX"
    )

    // Box scale pop
    val boxScale by animateFloatAsState(
        targetValue = when (openState) {
            OpenState.SHAKING   -> 1.1f
            OpenState.REVEALING -> 0f
            else                -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "boxScale"
    )

    // Reward reveal scale
    val rewardScale by animateFloatAsState(
        targetValue = if (openState == OpenState.DONE) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "rewardScale"
    )

    // Glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue  = if (openState == OpenState.DONE) 1f else 0.3f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to BgDark,
                    0.5f to Color(0xFF1A1042),
                    1f to Color(0xFF0F2B4A)
                )
            )
    ) {
        // Back button
        IconButton(
            onClick  = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                "🎁 Loot Box",
                fontSize   = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = GoldLight,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Earned for: ${lootBox.earnedFor}",
                color    = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // ── Box or Reward ──────────────────────────────────────────
            if (openState != OpenState.DONE) {
                // The box
                val offsetX = if (openState == OpenState.SHAKING) shakeX.dp else 0.dp
                Box(
                    modifier = Modifier
                        .offset(x = offsetX)
                        .scale(boxScale)
                        .size(180.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(GoldLight.copy(alpha = 0.4f), Color(0xFF9B5DE5).copy(alpha = 0.6f))
                            )
                        )
                        .border(3.dp, GoldLight.copy(alpha = glowAlpha), RoundedCornerShape(32.dp))
                        .clickable(enabled = openState == OpenState.IDLE) {
                            Log.d("LootBoxScreen", "Box clicked! Current state: $openState")
                            triggerAnimation = true  // ✅ CHANGE THIS
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎁", fontSize = 80.sp)
                }

                Spacer(Modifier.height(40.dp))

                if (openState == OpenState.IDLE) {
                    Text(
                        "Tap to open!",
                        color    = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                }

                // Trigger sequence
                LaunchedEffect(triggerAnimation) {  // ✅ LISTEN TO triggerAnimation INSTEAD
                    if (triggerAnimation) {
                        Log.d("LootBoxScreen", "Starting animation sequence")
                        delay(900)
                        Log.d("LootBoxScreen", "Setting to REVEALING")
                        openState = OpenState.REVEALING
                        delay(500)
                        Log.d("LootBoxScreen", "Setting to DONE")
                        openState = OpenState.DONE
                        triggerAnimation = false  // ✅ RESET THE TRIGGER
                    }
                }

            } else {
                // ── Reward revealed ─────────────────────────────────────
                val reward = shownReward
                val rc = rarityColor(reward.rarity)

                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(
                                Brush.radialGradient(listOf(rc.copy(alpha = 0.35f), Color.Transparent)),
                                CircleShape
                            )
                            .blur(20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .scale(rewardScale)
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(rc.copy(alpha = 0.2f))
                            .border(3.dp, rc.copy(alpha = glowAlpha), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(reward.emoji, fontSize = 72.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.alpha(if (openState == OpenState.DONE) 1f else 0f)
                ) {
                    Surface(
                        shape  = RoundedCornerShape(50.dp),
                        color  = rc.copy(alpha = 0.2f),
                        border = BorderStroke(1.5.dp, rc)
                    ) {
                        Text(
                            rarityLabel(reward.rarity),
                            color      = rc,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 13.sp,
                            modifier   = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    Text(
                        reward.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        reward.description,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )

                    if (reward.type == LootBoxRewardType.XP_BOOST && reward.xpValue > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GoldLight.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, GoldLight.copy(alpha = 0.6f))
                        ) {
                            Text(
                                "⭐ +${reward.xpValue} XP",
                                color = GoldLight,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onClaim(lootBox.copy(isOpened = true))
                            onBack()
                        },
                        shape   = RoundedCornerShape(14.dp),
                        colors  = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        modifier = Modifier.fillMaxWidth(0.65f).height(50.dp)
                    ) {
                        Text("🎉 Awesome!", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
