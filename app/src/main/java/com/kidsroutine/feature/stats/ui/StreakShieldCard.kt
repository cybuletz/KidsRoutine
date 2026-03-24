package com.kidsroutine.feature.stats.ui

import androidx.compose.animation.core.*
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

private val ShieldBlue   = Color(0xFF4FC3F7)
private val ShieldPurple = Color(0xFF9B59B6)

/**
 * Shows current streak with a shield overlay if shieldActive = true.
 * Drop this anywhere that shows the streak — DailyScreen header,
 * ChildProfileScreen stats section, or StatsScreen summary card.
 *
 * @param streak        Current streak count from UserModel
 * @param shieldActive  True if the user has an active streak shield from Loot Box
 * @param showLabel     Show "Day Streak" label below the number
 */
@Composable
fun StreakShieldCard(
    streak: Int,
    shieldActive: Boolean,
    showLabel: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shield")

    // Pulsing glow when shield is active
    val shieldGlowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 0.8f,
        animationSpec = if (shieldActive)
            infiniteRepeatable(tween(1000, easing = EaseInOutSine), RepeatMode.Reverse)
        else
            infiniteRepeatable(tween(99999)),   // effectively static when inactive
        label = "shieldGlow"
    )

    // Shield rotate when active
    val shieldRotate by infiniteTransition.animateFloat(
        initialValue  = -8f,
        targetValue   = 8f,
        animationSpec = if (shieldActive)
            infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse)
        else
            infiniteRepeatable(tween(99999)),
        label = "shieldRotate"
    )

    Box(
        modifier         = modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring (only when shield active)
        if (shieldActive) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .alpha(shieldGlowAlpha * 0.4f)
                    .background(
                        Brush.radialGradient(
                            listOf(ShieldBlue.copy(alpha = 0.6f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )
        }

        Surface(
            shape           = RoundedCornerShape(16.dp),
            color           = if (shieldActive)
                Color(0xFF0A1A2A)
            else
                Color(0xFFFFF3E0),
            border          = BorderStroke(
                width = 2.dp,
                brush = if (shieldActive)
                    Brush.linearGradient(listOf(ShieldBlue, ShieldPurple))
                else
                    SolidColor(Color(0xFFFFCC80))
            ),
            shadowElevation = if (shieldActive) 12.dp else 4.dp,
            modifier        = Modifier.defaultMinSize(minWidth = 80.dp)
        ) {
            Column(
                modifier            = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Streak number row
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        if (shieldActive) "🔥" else "🔥",
                        fontSize = 20.sp
                    )
                    Text(
                        "$streak",
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = if (shieldActive) ShieldBlue else Color(0xFFFF6B35)
                    )

                    // Shield badge (only when active)
                    if (shieldActive) {
                        Text(
                            "🛡️",
                            fontSize = 18.sp,
                            modifier = Modifier.rotate(shieldRotate)
                        )
                    }
                }

                if (showLabel) {
                    Text(
                        if (shieldActive) "Streak Protected!" else "Day Streak",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (shieldActive) ShieldBlue.copy(alpha = 0.8f) else Color(0xFFFF6B35)
                    )
                }

                // "Shield active" pill
                if (shieldActive) {
                    Surface(
                        shape  = RoundedCornerShape(50.dp),
                        color  = ShieldBlue.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, ShieldBlue.copy(alpha = 0.4f))
                    ) {
                        Text(
                            "🛡️ 1 miss forgiven",
                            color      = ShieldBlue,
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}