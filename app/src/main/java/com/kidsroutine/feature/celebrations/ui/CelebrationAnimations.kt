// File: app/src/main/java/com/kidsroutine/feature/celebrations/ui/CelebrationAnimations.kt
package com.kidsroutine.feature.celebrations.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * Confetti particle for celebration animation
 */
data class ConfettiParticle(
    val id: Int,
    val emoji: String,
    val angle: Float,
    val speed: Float,
    val rotation: Float
)

/**
 * Main Celebration Animation - Shows when user completes a task
 */
@Composable
fun TaskCompletionCelebration(
    onAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2500)
        isVisible = false
        onAnimationComplete()
    }

    if (!isVisible) return

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Animated particles
        repeat(20) { index ->
            ConfettiParticleAnimation(
                particle = ConfettiParticle(
                    id = index,
                    emoji = listOf("🎉", "✨", "⭐", "🎊", "🎈").random(),
                    angle = (index * 18f),
                    speed = (0.5f + (index % 3) * 0.3f),
                    rotation = (index * 30f).toFloat()
                )
            )
        }

        // Center celebration emoji
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(1.5f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎉",
                fontSize = 100.sp,
                modifier = Modifier.rotate(20f)
            )
        }
    }
}

@Composable
private fun ConfettiParticleAnimation(particle: ConfettiParticle) {
    val angle = particle.angle * (PI / 180.0)
    val offsetX = cos(angle) * 500f
    val offsetY = sin(angle) * 500f

    // Use animateFloatAsState instead of rememberInfiniteTransition
    var animationTrigger by remember { mutableStateOf(0f) }

    val animOffsetX by animateFloatAsState(
        targetValue = if (animationTrigger > 0) offsetX.toFloat() else 0f,
        animationSpec = tween(2500, easing = LinearEasing),
        label = "offsetX_${particle.id}"
    )

    val animOffsetY by animateFloatAsState(
        targetValue = if (animationTrigger > 0) offsetY.toFloat() else 0f,
        animationSpec = tween(2500, easing = LinearEasing),
        label = "offsetY_${particle.id}"
    )

    val rotation by animateFloatAsState(
        targetValue = if (animationTrigger > 0) 360f else 0f,
        animationSpec = tween(2500, easing = LinearEasing),
        label = "rotation_${particle.id}"
    )

    val alpha by animateFloatAsState(
        targetValue = if (animationTrigger > 0) 0f else 1f,
        animationSpec = tween(2500, easing = LinearEasing),
        label = "alpha_${particle.id}"
    )

    LaunchedEffect(Unit) {
        animationTrigger = 1f
    }

    Box(
        modifier = Modifier
            .offset(x = animOffsetX.dp, y = animOffsetY.dp)
            .alpha(alpha)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = particle.emoji,
            fontSize = 32.sp
        )
    }
}

/**
 * Achievement Unlock Animation - Shows when user unlocks an achievement
 */
@Composable
fun AchievementUnlockCelebration(
    badge: com.kidsroutine.core.model.Badge,
    onAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var animationState by remember { mutableStateOf(0f) }
    val scale by animateFloatAsState(
        targetValue = animationState,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "badge_scale"
    )

    LaunchedEffect(Unit) {
        animationState = 1f
        kotlinx.coroutines.delay(3000)
        onAnimationComplete()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Glow effect circles
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size((100 + (index * 60)).dp)
                    .scale(scale)
                    .alpha((1f - scale) * 0.5f)
            )
        }

        // Badge display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale)
        ) {
            Text(
                text = badge.icon,
                fontSize = 120.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Achievement Unlocked!",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = badge.description,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // Floating celebration emojis
        repeat(5) { index ->
            FloatingEmoji(
                emoji = listOf("⭐", "✨", "🎊", "🌟", "💫")[index],
                delay = index * 200L,
                scale = scale
            )
        }
    }
}

@Composable
private fun FloatingEmoji(
    emoji: String,
    delay: Long,
    scale: Float,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay)
        startAnimation = true
    }

    val offsetY by animateFloatAsState(
        targetValue = if (startAnimation) -300f else 0f,
        animationSpec = tween(2000, easing = LinearEasing),
        label = "float_y_$delay"
    )

    val offsetX by animateFloatAsState(
        targetValue = if (startAnimation) sin(delay.toFloat() / 1000f) * 100f else 0f,
        animationSpec = tween(2000, easing = LinearEasing),
        label = "float_x_$delay"
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 1f,
        animationSpec = tween(2000, easing = LinearEasing),
        label = "float_alpha_$delay"
    )

    Box(
        modifier = modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .alpha(alpha)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 36.sp)
    }
}

/**
 * Level Up Animation - Shows when user levels up
 */
@Composable
fun LevelUpCelebration(
    newLevel: Int,
    onAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var animationState by remember { mutableStateOf(0f) }
    val scale by animateFloatAsState(
        targetValue = animationState,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "levelup_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (animationState > 0) 360f else 0f,
        animationSpec = tween(1500, easing = LinearEasing),
        label = "levelup_rotation"
    )

    LaunchedEffect(Unit) {
        animationState = 1f
        kotlinx.coroutines.delay(3000)
        onAnimationComplete()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background burst
        repeat(12) { index ->
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .rotate((index * 30f) + rotation)
                    .scale(scale)
                    .alpha(1f - scale)
                    .align(Alignment.Center)
            ) {
                Text(
                    text = "⭐",
                    fontSize = 32.sp,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }

        // Level display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale)
                .rotate(rotation * 0.2f)
        ) {
            Text(text = "🎊", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Level Up!",
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "You reached Level $newLevel",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

/**
 * Milestone Celebration - Shows when user hits a milestone (100 XP, 1000 XP, etc)
 */
@Composable
fun MilestoneCelebration(
    milestone: String,
    icon: String = "🏆",
    onAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    var animationTrigger by remember { mutableStateOf(0f) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "milestone_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (animationTrigger > 0) 360f else 0f,
        animationSpec = tween(2000),
        label = "milestone_rotation"
    )

    LaunchedEffect(Unit) {
        animationTrigger = 1f
        kotlinx.coroutines.delay(3000)
        isVisible = false
        onAnimationComplete()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale)
                .rotate(rotation)
        ) {
            Text(text = icon, fontSize = 100.sp, modifier = Modifier.padding(bottom = 16.dp))
            Text(
                text = "Milestone Reached!",
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = milestone,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // Particle effects around milestone
        repeat(8) { index ->
            Box(
                modifier = Modifier
                    .offset(
                        x = (cos(Math.toRadians((index * 45).toDouble())) * 150).toFloat().dp,
                        y = (sin(Math.toRadians((index * 45).toDouble())) * 150).toFloat().dp
                    )
                    .scale(scale)
                    .alpha(scale)
            ) {
                Text(text = "✨", fontSize = 28.sp)
            }
        }
    }
}