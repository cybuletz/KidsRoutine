package com.kidsroutine.feature.stats.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.kidsroutine.feature.stats.data.UserStatsModel

// ── Palette (matches StatsScreen) ────────────────────────────────────────────
private val InsightBlue   = Color(0xFF667EEA)
private val InsightGold   = Color(0xFFFFD700)
private val InsightGreen  = Color(0xFF4CAF50)
private val InsightOrange = Color(0xFFFF6B35)
private val InsightPurple = Color(0xFF9B59B6)
private val CardBg        = Color(0xFFFFFFFF)

data class BehaviourInsight(
    val emoji: String,
    val title: String,
    val description: String,
    val accentColor: Color,
    val progressValue: Float   // 0f–1f, used for the mini-bar
)

/**
 * Drop this inside StatsScreen's LazyColumn, after StatsSummaryCard.
 * Pass in the already-loaded UserStatsModel — no new data fetching needed.
 */
@Composable
fun BehaviourInsightCard(stats: UserStatsModel) {
    val insights = remember(stats) { buildInsights(stats) }

    var visible by remember { mutableStateOf(false) }
    val cardAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label         = "insightAlpha"
    )
    val cardY by animateFloatAsState(
        targetValue   = if (visible) 0f else 24f,
        animationSpec = tween(500, easing = EaseOutCubic),
        label         = "insightY"
    )
    LaunchedEffect(Unit) { visible = true }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .offset(y = cardY.dp)
            .alpha(cardAlpha),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Animated brain icon
                val shimmerInfinite = rememberInfiniteTransition(label = "brainShimmer")
                val shimmerAlpha by shimmerInfinite.animateFloat(
                    initialValue  = 0.7f,
                    targetValue   = 1f,
                    animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
                    label         = "brainAlpha"
                )
                Text("🧠", fontSize = 24.sp, modifier = Modifier.alpha(shimmerAlpha))
                Column {
                    Text(
                        "Behaviour Insights",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 16.sp,
                        color      = Color(0xFF2D3436)
                    )
                    Text(
                        "Based on your activity this week",
                        fontSize = 12.sp,
                        color    = Color.Gray
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            // Insight rows
            insights.forEachIndexed { index, insight ->
                InsightRow(insight = insight, index = index)
            }

            // Encouragement footer
            EncouragementBanner(stats = stats)
        }
    }
}

@Composable
private fun InsightRow(insight: BehaviourInsight, index: Int) {
    var visible by remember { mutableStateOf(false) }
    val rowAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = index * 120),
        label         = "rowAlpha_$index"
    )
    val rowX by animateFloatAsState(
        targetValue   = if (visible) 0f else (-20f),
        animationSpec = tween(400, delayMillis = index * 120, easing = EaseOutCubic),
        label         = "rowX_$index"
    )
    LaunchedEffect(Unit) { visible = true }

    // Animated progress bar value
    val animatedProgress by animateFloatAsState(
        targetValue   = if (visible) insight.progressValue else 0f,
        animationSpec = tween(700, delayMillis = index * 120 + 200, easing = EaseOutCubic),
        label         = "progress_$index"
    )

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .offset(x = rowX.dp)
            .alpha(rowAlpha),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Emoji badge
                Box(
                    modifier         = Modifier
                        .size(32.dp)
                        .background(insight.accentColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(insight.emoji, fontSize = 16.sp)
                }
                Column {
                    Text(
                        insight.title,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color(0xFF2D3436)
                    )
                    Text(
                        insight.description,
                        fontSize = 11.sp,
                        color    = Color.Gray
                    )
                }
            }

            // Percentage label
            Text(
                "${(animatedProgress * 100).toInt()}%",
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                color      = insight.accentColor
            )
        }

        // Animated progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFFEEEEEE), CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(insight.accentColor.copy(alpha = 0.7f), insight.accentColor)
                        ),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun EncouragementBanner(stats: UserStatsModel) {
    val (emoji, message) = remember(stats) { buildEncouragement(stats) }

    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = InsightBlue.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, InsightBlue.copy(alpha = 0.2f))
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 22.sp)
            Text(
                message,
                fontSize = 12.sp,
                color    = Color(0xFF2D3436),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Data builders — derive insights from existing UserStatsModel ──────────────

private fun buildInsights(stats: UserStatsModel): List<BehaviourInsight> {
    // Streak consistency: currentStreak / max(longestStreak, 7)
    val streakConsistency = if (stats.longestStreak > 0)
        (stats.currentStreak.toFloat() / stats.longestStreak.toFloat()).coerceIn(0f, 1f)
    else 0f

    // Weekly engagement: thisWeekXp / 500 (500 XP/week = excellent)
    val weeklyEngagement = (stats.thisWeekXp.toFloat() / 500f).coerceIn(0f, 1f)

    // Task momentum: tasksCompleted mapped to 0-1 (100 tasks = max)
    val taskMomentum = (stats.tasksCompleted.toFloat() / 100f).coerceIn(0f, 1f)

    // Badge hustle: badgesUnlocked / 10 (10 badges = max)
    val badgeHustle = (stats.badgesUnlocked.toFloat() / 10f).coerceIn(0f, 1f)

    return listOf(
        BehaviourInsight(
            emoji         = "🔥",
            title         = "Streak Consistency",
            description   = if (stats.currentStreak >= 7)
                "Incredible! ${stats.currentStreak} days strong"
            else
                "${stats.currentStreak} day streak — keep going!",
            accentColor   = InsightOrange,
            progressValue = streakConsistency
        ),
        BehaviourInsight(
            emoji         = "⚡",
            title         = "Weekly Engagement",
            description   = "${stats.thisWeekXp} XP earned this week",
            accentColor   = InsightBlue,
            progressValue = weeklyEngagement
        ),
        BehaviourInsight(
            emoji         = "✅",
            title         = "Task Momentum",
            description   = "${stats.tasksCompleted} tasks completed overall",
            accentColor   = InsightGreen,
            progressValue = taskMomentum
        ),
        BehaviourInsight(
            emoji         = "🏅",
            title         = "Achievement Progress",
            description   = "${stats.badgesUnlocked} badges unlocked",
            accentColor   = InsightPurple,
            progressValue = badgeHustle
        )
    )
}

private fun buildEncouragement(stats: UserStatsModel): Pair<String, String> {
    return when {
        stats.currentStreak >= 14 -> "🏆" to "You're on a ${stats.currentStreak}-day streak — you're unstoppable!"
        stats.currentStreak >= 7  -> "🔥" to "A whole week of consistency! Keep the flame alive."
        stats.thisWeekXp >= 300   -> "⚡" to "Great energy this week — ${stats.thisWeekXp} XP is serious work!"
        stats.tasksCompleted >= 50 -> "🎯" to "50+ tasks done. You've built a real habit — well done!"
        stats.badgesUnlocked >= 3 -> "🏅" to "Your badge collection is growing. What's next?"
        stats.currentStreak == 0  -> "🌱" to "Every expert was once a beginner. Start your streak today!"
        else                      -> "💪" to "You're making progress. Consistency is the key — keep showing up!"
    }
}