package com.kidsroutine.feature.avatar.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsroutine.core.model.AvatarCustomization

/**
 * Large Avatar Display - Used in Profile screen
 * Displays full avatar with animation
 */
@Composable
fun AvatarDisplayLarge(
    customization: AvatarCustomization,
    displayName: String = "",
    modifier: Modifier = Modifier
) {
    var isRotating by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isRotating) 360f else 0f,
        animationSpec = tween(2000),
        label = "avatar_rotation"
    )

    LaunchedEffect(Unit) {
        isRotating = true
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.White, CircleShape)
                .clip(CircleShape)
                .rotate(rotation),
            contentAlignment = Alignment.Center
        ) {
            AvatarContent(customization = customization, size = 180.dp)
        }

        if (displayName.isNotEmpty()) {
            Text(
                text = displayName,
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

/**
 * Medium Avatar Display - Used in Family Chat
 */
@Composable
fun AvatarDisplayMedium(
    customization: AvatarCustomization,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        AvatarContent(customization = customization, size = 70.dp)
    }
}

/**
 * Small Avatar Display - Used in Leaderboard/Nav Bar
 */
@Composable
fun AvatarDisplaySmall(
    customization: AvatarCustomization,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(Color.White, CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AvatarContent(customization = customization, size = 40.dp)
    }
}

/**
 * Thumbnail Avatar Display - Used in lists
 */
@Composable
fun AvatarDisplayThumbnail(
    customization: AvatarCustomization,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .background(Color.White, CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AvatarContent(customization = customization, size = 30.dp)
    }
}

/**
 * Animated Avatar - Used when achievements/milestones are unlocked
 */
@Composable
fun AvatarDisplayAnimated(
    customization: AvatarCustomization,
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {}
) {
    var animationState by remember { mutableStateOf(0f) }
    val scale by animateFloatAsState(
        targetValue = animationState,
        animationSpec = tween(1200),
        label = "avatar_scale"
    )

    LaunchedEffect(Unit) {
        animationState = 1f
        kotlinx.coroutines.delay(1200)
        onAnimationComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Celebration circles
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size((100 + (index * 50)).dp)
                    .background(
                        Color(0xFFFF6B35).copy(alpha = (1f - animationState) * 0.3f),
                        CircleShape
                    )
            )
        }

        // Avatar
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(Color.White, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AvatarContent(customization = customization, size = 160.dp)
        }

        // Celebration emoji
        Text(
            text = "✨",
            fontSize = 60.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
        Text(
            text = "🎉",
            fontSize = 60.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
        Text(
            text = "🏆",
            fontSize = 60.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

/**
 * Avatar Badge - Small display with level indicator
 */
@Composable
fun AvatarDisplayWithBadge(
    customization: AvatarCustomization,
    level: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color.White, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AvatarContent(customization = customization, size = 55.dp)
        }

        // Level Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(24.dp)
                .background(Color(0xFFFF6B35), CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = level.toString(),
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

/**
 * Core Avatar Content - Renders the actual avatar composition
 */
@Composable
private fun AvatarContent(
    customization: AvatarCustomization,
    size: Dp
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // This is a placeholder - in production, you would render actual avatar graphics
        // For now, we show an emoji-based avatar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "👤",
                fontSize = (size.value * 0.7).sp
            )
        }
    }
}

/**
 * Avatar Comparison - Show before/after customizations
 */
@Composable
fun AvatarComparison(
    beforeCustomization: AvatarCustomization,
    afterCustomization: AvatarCustomization,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "Before", fontSize = 12.sp)
            AvatarDisplaySmall(customization = beforeCustomization)
        }

        Text(text = "→", fontSize = 24.sp)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "After", fontSize = 12.sp)
            AvatarDisplaySmall(customization = afterCustomization)
        }
    }
}