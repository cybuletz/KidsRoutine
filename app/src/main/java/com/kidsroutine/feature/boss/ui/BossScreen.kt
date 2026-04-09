package com.kidsroutine.feature.boss.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.kidsroutine.core.model.BossModel
import com.kidsroutine.core.model.UserModel
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private val BattleRed = Color(0xFFE53935)
private val BattleRedDark = Color(0xFFB71C1C)
private val BattleRedLight = Color(0xFFEF5350)
private val HpBarGreen = Color(0xFF4CAF50)
private val HpBarYellow = Color(0xFFFFEB3B)
private val HpBarRed = Color(0xFFF44336)
private val BgLight = Color(0xFFFFFBF0)
private val BgDark = Color(0xFF1A1A2E)
private val TextDark = Color(0xFF2D3436)
private val TextLight = Color(0xFFFFFFFF)
private val GoldAccent = Color(0xFFFFD700)
private val CardBg = Color(0xFFFFFFFF)
private val CardBgDark = Color(0xFF16213E)

@Composable
fun BossScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit,
    viewModel: BossViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.familyId) {
        Log.d("BossScreen", "Loading boss for family: ${currentUser.familyId}")
        viewModel.loadBoss(currentUser.familyId, currentUser.userId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        when {
            uiState.isLoading -> LoadingState()
            uiState.boss?.isDefeated == true -> VictoryScreen(
                boss = uiState.boss!!,
                mvpUserId = uiState.mvpUserId,
                onBackClick = onBackClick
            )
            uiState.boss?.isExpired == true -> ExpiredScreen(
                boss = uiState.boss!!,
                onBackClick = onBackClick
            )
            uiState.boss != null -> BattleArena(
                boss = uiState.boss!!,
                mvpUserId = uiState.mvpUserId,
                timeRemaining = uiState.timeRemaining,
                onBackClick = onBackClick
            )
            else -> NoBossState(
                onGenerateBoss = {
                    viewModel.generateNewBoss(
                        familyId = currentUser.familyId,
                        familySize = 3
                    )
                },
                onBackClick = onBackClick,
                currentXp = uiState.currentXp
            )
        }

        // Back button overlay
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(top = 40.dp, start = 8.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextLight
            )
        }
    }
}

// ─── Battle Arena ───────────────────────────────────────────────────────────

@Composable
private fun BattleArena(
    boss: BossModel,
    mvpUserId: String?,
    timeRemaining: Long,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 80.dp, bottom = 140.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer
        TimerDisplay(timeRemaining = timeRemaining)

        Spacer(modifier = Modifier.height(16.dp))

        // Boss emoji with animation
        BossEmojiDisplay(emoji = boss.type.emoji, isAlive = boss.isAlive)

        Spacer(modifier = Modifier.height(8.dp))

        // Boss name and type
        Text(
            text = boss.type.displayName,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextLight
        )
        Text(
            text = boss.type.description,
            fontSize = 14.sp,
            color = TextLight.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // HP Bar
        HpBar(
            currentHp = boss.currentHp,
            maxHp = boss.maxHp,
            hpPercentage = boss.hpPercentage
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Damage Tracker
        DamageTracker(
            damageLog = boss.damageLog,
            mvpUserId = mvpUserId,
            totalDamage = boss.totalDamage,
            maxHp = boss.maxHp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Week & Season info
        BossInfoCard(boss = boss)
    }
}

// ─── Boss Emoji Display with Shake/Pulse ────────────────────────────────────

@Composable
private fun BossEmojiDisplay(emoji: String, isAlive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "boss_anim")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val shake by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    val shakeOffset = if (isAlive) (sin(shake * PI * 2).toFloat() * 3f) else 0f
    val scaleValue = if (isAlive) pulse else 0.9f

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scaleValue)
            .offset(x = shakeOffset.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        BattleRed.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 72.sp
        )
    }
}

// ─── HP Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun HpBar(currentHp: Int, maxHp: Int, hpPercentage: Float) {
    val animatedHp by animateFloatAsState(
        targetValue = hpPercentage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "hp_anim"
    )

    val hpColor = when {
        hpPercentage > 0.6f -> HpBarGreen
        hpPercentage > 0.3f -> HpBarYellow
        else -> HpBarRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "❤️ HP",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight.copy(alpha = 0.8f)
            )
            Text(
                text = "$currentHp / $maxHp",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedHp.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(hpColor.copy(alpha = 0.8f), hpColor)
                        )
                    )
            )
        }
    }
}

// ─── Timer Display ──────────────────────────────────────────────────────────

@Composable
private fun TimerDisplay(timeRemaining: Long) {
    val days = timeRemaining / (24 * 60 * 60 * 1000)
    val hours = (timeRemaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
    val minutes = (timeRemaining % (60 * 60 * 1000)) / (60 * 1000)

    val timerText = when {
        days > 0 -> "${days}d ${hours}h remaining"
        hours > 0 -> "${hours}h ${minutes}m remaining"
        minutes > 0 -> "${minutes}m remaining"
        else -> "⏰ Time's up!"
    }

    val isUrgent = timeRemaining in 1..URGENT_THRESHOLD

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isUrgent) BattleRed.copy(alpha = 0.3f)
                else Color.White.copy(alpha = 0.1f)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "⏳ $timerText",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isUrgent) BattleRedLight else TextLight.copy(alpha = 0.9f)
        )
    }
}

// ─── Damage Tracker ─────────────────────────────────────────────────────────

@Composable
private fun DamageTracker(
    damageLog: Map<String, Int>,
    mvpUserId: String?,
    totalDamage: Int,
    maxHp: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBgDark)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚔️ Damage Tracker",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
            Text(
                text = "$totalDamage / $maxHp",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextLight.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (damageLog.isEmpty()) {
            Text(
                text = "No damage dealt yet — complete tasks to attack!",
                fontSize = 13.sp,
                color = TextLight.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            val sortedEntries = damageLog.entries.sortedByDescending { it.value }
            sortedEntries.forEachIndexed { index, (userId, damage) ->
                val isMvp = userId == mvpUserId
                DamageLogEntry(
                    rank = index + 1,
                    userId = userId,
                    damage = damage,
                    totalMaxHp = maxHp,
                    isMvp = isMvp
                )
                if (index < sortedEntries.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DamageLogEntry(
    rank: Int,
    userId: String,
    damage: Int,
    totalMaxHp: Int,
    isMvp: Boolean
) {
    val damagePercent = if (totalMaxHp > 0) damage.toFloat() / totalMaxHp else 0f
    val animatedWidth by animateFloatAsState(
        targetValue = damagePercent.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "dmg_bar_$userId"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isMvp) {
                    Text(text = "👑 ", fontSize = 14.sp)
                }
                Text(
                    text = "#$rank",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMvp) GoldAccent else TextLight.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = displayUserId(userId),
                    fontSize = 14.sp,
                    fontWeight = if (isMvp) FontWeight.Bold else FontWeight.Medium,
                    color = if (isMvp) GoldAccent else TextLight
                )
            }
            Text(
                text = "$damage dmg",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = BattleRedLight
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedWidth)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(BattleRed, BattleRedLight)
                        )
                    )
            )
        }
    }
}

// ─── Boss Info Card ─────────────────────────────────────────────────────────

@Composable
private fun BossInfoCard(boss: BossModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBgDark)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        InfoChip(label = "Week", value = boss.week)
        InfoChip(label = "Season", value = boss.season.name.lowercase().replaceFirstChar { it.uppercase() })
        InfoChip(label = "Reward", value = "+${boss.victoryXpBonus} XP")
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextLight.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextLight,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── Victory Screen ─────────────────────────────────────────────────────────

@Composable
private fun VictoryScreen(
    boss: BossModel,
    mvpUserId: String?,
    onBackClick: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
    }

    // Confetti particles
    val confettiEmojis = listOf("🎉", "🏆", "⭐", "✨", "🎊", "💥", "🔥")
    var confettiVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        confettiVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B5E20),
                        Color(0xFF2E7D32),
                        BgDark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Confetti overlay
        AnimatedVisibility(
            visible = confettiVisible,
            enter = fadeIn(tween(800))
        ) {
            ConfettiOverlay(emojis = confettiEmojis)
        }

        AnimatedVisibility(
            visible = showContent,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "🏆", fontSize = 80.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "VICTORY!",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${boss.type.emoji} ${boss.type.displayName} has been defeated!",
                    fontSize = 18.sp,
                    color = TextLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Reward display
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🎁 Rewards",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "+${boss.victoryXpBonus} XP Bonus",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${boss.victoryLootRarity.name} Loot Box",
                            fontSize = 16.sp,
                            color = TextLight.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Total damage dealt: ${boss.totalDamage}",
                            fontSize = 13.sp,
                            color = TextLight.copy(alpha = 0.6f)
                        )
                    }
                }

                // MVP display
                if (mvpUserId != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "👑 MVP: ${displayUserId(mvpUserId)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BattleRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(140.dp))
            }
        }
    }
}

// ─── Confetti Overlay ───────────────────────────────────────────────────────

@Composable
private fun ConfettiOverlay(emojis: List<String>) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_fall"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        val positions = remember {
            List(15) {
                Triple(
                    (Random.nextFloat() * 0.9f + 0.05f),
                    Random.nextFloat(),
                    emojis.random()
                )
            }
        }
        positions.forEach { (xFraction, yOffset, emoji) ->
            val y = ((yOffset + offset) % 1.2f) - 0.1f
            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier
                    .fillMaxWidth(xFraction)
                    .fillMaxHeight(y.coerceIn(0f, 1f))
                    .align(Alignment.TopStart)
            )
        }
    }
}

// ─── Expired Screen ─────────────────────────────────────────────────────────

@Composable
private fun ExpiredScreen(
    boss: BossModel,
    onBackClick: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4A0000),
                        BgDark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = showContent,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(text = "💀", fontSize = 80.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "TIME'S UP!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BattleRedLight
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${boss.type.emoji} ${boss.type.displayName} survived!",
                    fontSize = 18.sp,
                    color = TextLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Damage dealt: ${boss.totalDamage} / ${boss.maxHp}",
                    fontSize = 14.sp,
                    color = TextLight.copy(alpha = 0.6f)
                )
                Text(
                    text = "HP remaining: ${boss.currentHp}",
                    fontSize = 14.sp,
                    color = TextLight.copy(alpha = 0.6f)
                )
                Text(
                    text = "XP penalty: -${boss.defeatXpPenalty} XP",
                    fontSize = 14.sp,
                    color = BattleRedLight.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BattleRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Try Again Next Week",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ─── No Boss State ──────────────────────────────────────────────────────────

@Composable
private fun NoBossState(
    onGenerateBoss: () -> Unit,
    onBackClick: () -> Unit,
    currentXp: Int = 0
) {
    val hasEnoughXp = currentXp >= BossViewModel.BOSS_ENTRY_COST
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "⚔️", fontSize = 72.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Active Boss",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Rally your family and summon a new boss to battle together!",
                fontSize = 15.sp,
                color = TextLight.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "⭐ $currentXp XP",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoldAccent
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGenerateBoss,
                enabled = hasEnoughXp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BattleRed,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(52.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚔️ Summon Boss",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cost: ${BossViewModel.BOSS_ENTRY_COST} XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ─── Loading State ──────────────────────────────────────────────────────────

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = BattleRed)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Entering the arena...",
                fontSize = 16.sp,
                color = TextLight.copy(alpha = 0.7f)
            )
        }
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────

private fun displayUserId(userId: String): String {
    return if (userId.length > 8) userId.take(8) + "…" else userId
}

private const val URGENT_THRESHOLD = 6L * 60 * 60 * 1000 // 6 hours
