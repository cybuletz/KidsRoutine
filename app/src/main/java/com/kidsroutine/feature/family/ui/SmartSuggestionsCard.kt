package com.kidsroutine.feature.family.ui

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kidsroutine.core.model.FamilyModel
import com.kidsroutine.core.model.UserModel

private val NudgeBlue   = Color(0xFF667EEA)
private val NudgeGreen  = Color(0xFF4CAF50)
private val NudgeOrange = Color(0xFFFF6B35)
private val NudgeGold   = Color(0xFFFFD700)

data class SmartNudge(
    val emoji: String,
    val title: String,
    val body: String,
    val actionLabel: String?,
    val accentColor: Color,
    val priority: Int   // 1 = high, 3 = low
)

/**
 * Derives up to 3 contextual nudges from data already in memory —
 * no new Firestore reads, no Cloud Function needed.
 *
 * Drop inside ParentDashboardScreen's Column/scroll after FamilyInfoCard.
 *
 * @param family          Already-loaded FamilyModel
 * @param currentUser     The logged-in parent
 * @param aiQuotaUsed     Pass from ParentDashboardViewModel: how many AI tasks generated today
 * @param aiQuotaLimit    Pass from ParentDashboardViewModel: daily AI task limit
 * @param onGenerateClick Navigate to GenerationScreen
 * @param onChallengesClick Navigate to ChallengesScreen
 * @param onWeeklyPlanClick Navigate to WeeklyPlanScreen
 */
@Composable
fun SmartSuggestionsCard(
    family: FamilyModel,
    currentUser: UserModel,
    aiQuotaUsed: Int = 0,
    aiQuotaLimit: Int = 3,
    onGenerateClick: () -> Unit = {},
    onChallengesClick: () -> Unit = {},
    onWeeklyPlanClick: () -> Unit = {}
) {
    val nudges = remember(family, aiQuotaUsed) {
        buildNudges(family, currentUser, aiQuotaUsed, aiQuotaLimit)
    }

    if (nudges.isEmpty()) return

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label         = "nudgeAlpha"
    )
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Section header
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Animated brain
            val pulse = rememberInfiniteTransition(label = "brainPulse")
            val pulseScale by pulse.animateFloat(
                initialValue  = 1f,
                targetValue   = 1.15f,
                animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
                label         = "brainScale"
            )
            Text("🧠", fontSize = 18.sp, modifier = Modifier.scale(pulseScale))
            Text(
                "Smart Suggestions",
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 15.sp,
                color      = Color(0xFF2D3436)
            )
        }

        nudges.forEachIndexed { index, nudge ->
            NudgeRow(
                nudge       = nudge,
                index       = index,
                onActionClick = when (index) {
                    0    -> onGenerateClick
                    1    -> onChallengesClick
                    else -> onWeeklyPlanClick
                }
            )
        }
    }
}

@Composable
private fun NudgeRow(
    nudge: SmartNudge,
    index: Int,
    onActionClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val rowAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = index * 100),
        label         = "rowAlpha_$index"
    )
    val rowX by animateFloatAsState(
        targetValue   = if (visible) 0f else 16f,
        animationSpec = tween(380, delayMillis = index * 100, easing = EaseOutCubic),
        label         = "rowX_$index"
    )
    LaunchedEffect(Unit) { visible = true }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .offset(x = rowX.dp)
            .alpha(rowAlpha),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Emoji badge
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .background(nudge.accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(nudge.emoji, fontSize = 22.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nudge.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 13.sp,
                    color      = Color(0xFF2D3436)
                )
                Text(
                    nudge.body,
                    fontSize = 11.sp,
                    color    = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (nudge.actionLabel != null) {
                Surface(
                    shape   = RoundedCornerShape(50.dp),
                    color   = nudge.accentColor.copy(alpha = 0.12f),
                    border  = BorderStroke(1.dp, nudge.accentColor.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable(onClick = onActionClick)  // fix: use androidx.compose.foundation.clickable via surface
                ) {
                    Text(
                        nudge.actionLabel,
                        color      = nudge.accentColor,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// ── Nudge builder — purely from in-memory data ────────────────────────────────

private fun buildNudges(
    family: FamilyModel,
    currentUser: UserModel,
    aiQuotaUsed: Int,
    aiQuotaLimit: Int
): List<SmartNudge> {
    val nudges = mutableListOf<SmartNudge>()

    // 1. Streak at risk
    if (family.familyStreak in 1..2) {
        nudges.add(SmartNudge(
            emoji       = "🔥",
            title       = "Streak at risk!",
            body        = "Family streak is ${family.familyStreak} day${if (family.familyStreak == 1) "" else "s"}. Assign a task today to keep it going.",
            actionLabel = "Generate →",
            accentColor = NudgeOrange,
            priority    = 1
        ))
    }

    // 2. No active challenges for large family
    if (family.activeChallengeIds.isEmpty() && family.memberIds.size >= 2) {
        nudges.add(SmartNudge(
            emoji       = "🏆",
            title       = "No active challenges",
            body        = "Your family hasn't started a challenge yet. Challenges build long-term habits.",
            actionLabel = "Start one →",
            accentColor = NudgeBlue,
            priority    = 2
        ))
    }

    // 3. AI quota almost gone
    val quotaLeft = aiQuotaLimit - aiQuotaUsed
    if (quotaLeft == 1) {
        nudges.add(SmartNudge(
            emoji       = "⚡",
            title       = "1 AI generation left today",
            body        = "Use your last generation wisely — or upgrade to PRO for 20/day.",
            actionLabel = "Generate →",
            accentColor = NudgeGold,
            priority    = 1
        ))
    }

    // 4. Good streak — suggest weekly plan
    if (family.familyStreak >= 7) {
        nudges.add(SmartNudge(
            emoji       = "📅",
            title       = "${family.familyStreak}-day streak! Plan ahead",
            body        = "Your family is on a roll. Generate a full 7-day plan to keep the momentum.",
            actionLabel = "Plan week →",
            accentColor = NudgeGreen,
            priority    = 2
        ))
    }

    // 5. Family has children but low XP (engagement nudge)
    if (family.familyXp < 100 && family.memberIds.size >= 2) {
        nudges.add(SmartNudge(
            emoji       = "🌱",
            title       = "Your family is just getting started",
            body        = "Complete a few tasks to earn your first XP milestone together.",
            actionLabel = "Generate task →",
            accentColor = NudgeGreen,
            priority    = 3
        ))
    }

    // Return top 3, sorted by priority
    return nudges.sortedBy { it.priority }.take(3)
}