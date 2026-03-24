package com.kidsroutine.feature.world.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.core.model.WorldNode
import com.kidsroutine.core.model.WorldNodeStatus
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
// WORLD PALETTE — deep cinematic dark + vivid accents
// ─────────────────────────────────────────────────────────────────────────────
private val SkyTop       = Color(0xFF0D0D2B)
private val SkyMid       = Color(0xFF1A1042)
private val SkyBottom    = Color(0xFF0F2B4A)
private val StarColor    = Color(0xFFFFFFFF)
private val NodeUnlocked = Color(0xFFFF6B35)
private val NodeDone     = Color(0xFF2ECC71)
private val NodeLocked   = Color(0xFF4A4A6A)
private val NodeSpecial  = Color(0xFFFFD700)
private val PathColor    = Color(0x55FFFFFF)
private val PathDone     = Color(0x882ECC71)
private val XpGold       = Color(0xFFFFD700)
private val GlowUnlocked = Color(0x33FF6B35)
private val GlowDone     = Color(0x332ECC71)
private val GlowSpecial  = Color(0x55FFD700)
private val CardBg       = Color(0xFF1E1E3F)
private val CardBorder   = Color(0xFF3A3A6A)

// ─────────────────────────────────────────────────────────────────────────────
// MAIN SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun WorldScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit = {},
    viewModel: WorldViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // ✅ THIS IS IT - Just pass userId and let it observe
    LaunchedEffect(currentUser.userId) {
        viewModel.loadWorld(currentUser.userId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to SkyTop,
                        0.45f to SkyMid,
                        1f to SkyBottom
                    )
                )
        )

        StarField()
        NebulaClouds()

        when {
            uiState.isLoading -> WorldLoadingSkeleton()
            uiState.world != null -> {
                WorldMapCanvas(
                    world = uiState.world!!,
                    userXp = uiState.currentUser.xp,  // ← Get from viewModel state, not parameter
                    onNodeTapped = { node -> viewModel.onNodeTapped(node) }
                )
            }
        }

        WorldHud(
            displayName = currentUser.displayName,
            userXp = uiState.currentUser.xp,  // ← Get from viewModel state
            totalXp = uiState.world?.totalXpRequired ?: 1000,
            onBackClick = onBackClick
        )

        AnimatedVisibility(
            visible = uiState.showNodeDetail && uiState.selectedNode != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).zIndex(10f)
        ) {
            uiState.selectedNode?.let { node ->
                NodeDetailCard(
                    node = node,
                    userXp = uiState.currentUser.xp,  // ← Get from viewModel state
                    onDismiss = { viewModel.dismissNodeDetail() }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STAR FIELD — 60 independently twinkling stars
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StarField() {
    val stars = remember {
        (0 until 60).map {
            Triple(
                (0..100).random() / 100f,   // x fraction
                (0..100).random() / 100f,   // y fraction
                (6..22).random().toFloat()  // size dp
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        stars.forEachIndexed { index, (xFrac, yFrac, sizeDp) ->
            TwinklingStar(
                xFrac = xFrac,
                yFrac = yFrac,
                sizeDp = sizeDp,
                delayMs = (index * 137L) % 3000L  // golden angle distribution
            )
        }
    }
}

@Composable
private fun TwinklingStar(
    xFrac: Float,
    yFrac: Float,
    sizeDp: Float,
    delayMs: Long
) {
    val infiniteTransition = rememberInfiniteTransition(label = "star_$xFrac")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1200..2800).random(),
                easing = FastOutSlowInEasing,
                delayMillis = delayMs.toInt()
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1500..3000).random(),
                easing = EaseInOutSine,
                delayMillis = (delayMs + 200).toInt()
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starScale"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val x = maxWidth * xFrac
        val y = maxHeight * yFrac
        Box(
            modifier = Modifier
                .offset(x = x - (sizeDp / 2).dp, y = y - (sizeDp / 2).dp)
                .size(sizeDp.dp)
                .alpha(alpha)
                .scale(scale)
                .background(StarColor, CircleShape)
                .blur(if (sizeDp > 14) 2.dp else 0.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEBULA BLOBS — soft glowing background shapes
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NebulaClouds() {
    val infiniteTransition = rememberInfiniteTransition(label = "nebula")
    val drift by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            tween(8000, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "nebulaDrift"
    )
    val alphaA by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.14f,
        animationSpec = infiniteRepeatable(
            tween(5000, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "nebulaAlpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Purple nebula top-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF9B5DE5).copy(alpha = alphaA), Color.Transparent),
                center = Offset(size.width * 0.15f + drift, size.height * 0.2f),
                radius = size.width * 0.45f
            ),
            radius = size.width * 0.45f,
            center = Offset(size.width * 0.15f + drift, size.height * 0.2f)
        )
        // Teal nebula bottom-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF4ECDC4).copy(alpha = alphaA * 0.8f), Color.Transparent),
                center = Offset(size.width * 0.82f - drift, size.height * 0.75f),
                radius = size.width * 0.4f
            ),
            radius = size.width * 0.4f,
            center = Offset(size.width * 0.82f - drift, size.height * 0.75f)
        )
        // Orange nebula centre
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF6B35).copy(alpha = alphaA * 0.5f), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.5f + drift),
                radius = size.width * 0.35f
            ),
            radius = size.width * 0.35f,
            center = Offset(size.width * 0.5f, size.height * 0.5f + drift)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WORLD MAP CANVAS — paths + nodes
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WorldMapCanvas(
    world: com.kidsroutine.core.model.WorldModel,
    userXp: Int,
    onNodeTapped: (WorldNode) -> Unit
) {
    // Animate each node in with a staggered delay
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        revealed = true
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val canvasW = maxWidth
        val canvasH = maxHeight

        // ── Draw connecting paths ─────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val nodes = world.nodes
            for (i in 0 until nodes.size - 1) {
                val from = nodes[i]
                val to   = nodes[i + 1]
                val fromX = from.positionX * size.width
                val fromY = from.positionY * size.height
                val toX   = to.positionX * size.width
                val toY   = to.positionY * size.height

                // Dotted path
                val pathDone = from.status == WorldNodeStatus.COMPLETED
                val color = if (pathDone) PathDone else PathColor
                val dashOn  = 18f
                val dashOff = 14f
                val dist = sqrt((toX - fromX).pow(2) + (toY - fromY).pow(2))
                val steps = (dist / (dashOn + dashOff)).toInt()
                val dx = (toX - fromX) / dist
                val dy = (toY - fromY) / dist
                for (s in 0..steps) {
                    val startFrac = s * (dashOn + dashOff)
                    val endFrac   = startFrac + dashOn
                    drawLine(
                        color = color,
                        start = Offset(fromX + dx * startFrac, fromY + dy * startFrac),
                        end   = Offset(fromX + dx * endFrac.coerceAtMost(dist), fromY + dy * endFrac.coerceAtMost(dist)),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // ── Draw each node ────────────────────────────────────────────────
        world.nodes.forEachIndexed { index, node ->
            val nodeX = canvasW * node.positionX
            val nodeY = canvasH * node.positionY

            val enterDelay = 150L + index * 120L

            AnimatedVisibility(
                visible = revealed,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    initialScale = 0f
                ) + fadeIn(tween(300, delayMillis = enterDelay.toInt())),
                modifier = Modifier
                    .offset(
                        x = nodeX - 36.dp,
                        y = nodeY - 36.dp
                    )
            ) {
                WorldNodeItem(
                    node = node,
                    onTap = { onNodeTapped(node) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SINGLE WORLD NODE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WorldNodeItem(
    node: WorldNode,
    onTap: () -> Unit
) {
    // Continuous float animation for unlocked/special nodes
    val infiniteTransition = rememberInfiniteTransition(label = "node_${node.nodeId}")

    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (node.status != WorldNodeStatus.LOCKED) -8f else 0f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "float"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (node.status == WorldNodeStatus.UNLOCKED) 0.9f else
            if (node.status == WorldNodeStatus.COMPLETED) 0.6f else 0.1f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "glow"
    )

    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (node.status == WorldNodeStatus.UNLOCKED) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "ring"
    )

    // Press scale
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press"
    )

    val nodeColor = when {
        node.isSpecial && node.status != WorldNodeStatus.LOCKED -> NodeSpecial
        node.status == WorldNodeStatus.COMPLETED -> NodeDone
        node.status == WorldNodeStatus.UNLOCKED  -> NodeUnlocked
        else                                     -> NodeLocked
    }

    val glowColor = when {
        node.isSpecial && node.status != WorldNodeStatus.LOCKED -> GlowSpecial
        node.status == WorldNodeStatus.COMPLETED -> GlowDone
        node.status == WorldNodeStatus.UNLOCKED  -> GlowUnlocked
        else                                     -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .offset(y = floatY.dp)
            .scale(pressScale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = {
                        if (node.status != WorldNodeStatus.LOCKED) onTap()
                        else onTap()  // allow tap on locked to show "locked" info
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring (only for active/done nodes)
        if (node.status != WorldNodeStatus.LOCKED) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(ringScale)
                    .alpha(glowAlpha)
                    .background(glowColor, CircleShape)
            )
        }

        // Node body
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = if (node.status != WorldNodeStatus.LOCKED) 12.dp else 4.dp,
                    shape = CircleShape,
                    ambientColor = nodeColor,
                    spotColor = nodeColor
                )
                .background(
                    Brush.radialGradient(
                        listOf(
                            nodeColor.copy(alpha = 0.9f),
                            nodeColor.copy(alpha = 0.6f)
                        )
                    ),
                    CircleShape
                )
                .border(
                    width = if (node.isSpecial && node.status != WorldNodeStatus.LOCKED) 2.dp else 1.dp,
                    color = nodeColor.copy(alpha = 0.8f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (node.status == WorldNodeStatus.LOCKED) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = node.emoji,
                    fontSize = 26.sp
                )
            }
        }

        // Special sparkle crown for boss/milestone nodes
        if (node.isSpecial && node.status != WorldNodeStatus.LOCKED) {
            Text(
                text = "👑",
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-2).dp)
            )
        }

        // Checkmark badge for completed nodes
        if (node.status == WorldNodeStatus.COMPLETED) {
            Surface(
                shape = CircleShape,
                color = NodeDone,
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP HUD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WorldHud(
    displayName: String,
    userXp: Int,
    totalXp: Int,
    onBackClick: () -> Unit
) {
    val progress = (userXp.toFloat() / totalXp.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "xpProgress"
    )

    // HUD shimmer on XP bar
    val shimmerInfinite = rememberInfiniteTransition(label = "hudShimmer")
    val shimmerX by shimmerInfinite.animateFloat(
        initialValue = -200f, targetValue = 600f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "shimmerX"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .zIndex(5f)
    ) {
        // Frosted glass card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0x99000000),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Back button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                ) {
                    Text("←", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🌍 Your World",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = XpGold, modifier = Modifier.size(14.dp))
                            Text(
                                text = "$userXp XP",
                                color = XpGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Animated XP progress bar with shimmer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFFFF6B35),
                                            Color(0xFFFFD700),
                                            Color(0xFFFF6B35)
                                        ),
                                        startX = shimmerX,
                                        endX = shimmerX + 400f
                                    ),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }

                    Text(
                        text = "$userXp / $totalXp XP to master",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NODE DETAIL CARD (bottom sheet)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NodeDetailCard(
    node: WorldNode,
    userXp: Int,
    onDismiss: () -> Unit
) {
    val isLocked = node.status == WorldNodeStatus.LOCKED
    val nodeColor = when {
        node.isSpecial && !isLocked -> NodeSpecial
        node.status == WorldNodeStatus.COMPLETED -> NodeDone
        node.status == WorldNodeStatus.UNLOCKED  -> NodeUnlocked
        else                                     -> NodeLocked
    }

    // Bounce-in animation for the emoji
    var bounced by remember { mutableStateOf(false) }
    val emojiScale by animateFloatAsState(
        targetValue = if (bounced) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "emojiScale"
    )
    LaunchedEffect(Unit) { bounced = true }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
        color = CardBg,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 24.dp,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
            )

            // Big emoji
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(emojiScale)
                    .background(nodeColor.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, nodeColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLocked) "🔒" else node.emoji,
                    fontSize = 40.sp
                )
            }

            // Title
            Text(
                text = node.title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = node.subtitle,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            // Status chip
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = nodeColor.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, nodeColor.copy(alpha = 0.5f))
            ) {
                Text(
                    text = when (node.status) {
                        WorldNodeStatus.COMPLETED -> "✅ Completed"
                        WorldNodeStatus.UNLOCKED  -> "🔓 Unlocked — Ready!"
                        WorldNodeStatus.LOCKED    -> "🔒 Requires ${node.requiredXp} XP"
                    },
                    color = nodeColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // XP reward row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Your XP", color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp)
                    Text("$userXp XP", color = XpGold, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Reward", color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp)
                    Text("+${node.rewardXp} XP", color = NodeDone, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // XP needed if locked
            if (isLocked) {
                val xpNeeded = node.requiredXp - userXp
                val lockProgress = (userXp.toFloat() / node.requiredXp.toFloat()).coerceIn(0f, 1f)
                val animLockProg by animateFloatAsState(
                    targetValue = lockProgress,
                    animationSpec = tween(900, easing = EaseOutCubic),
                    label = "lockProg"
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Progress", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        Text("$xpNeeded XP to unlock", color = NodeLocked.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animLockProg)
                                .fillMaxHeight()
                                .background(NodeLocked.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            // Dismiss
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Close", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LOADING SKELETON
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WorldLoadingSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "skeletonAlpha"
    )
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .alpha(alpha)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            )
            Text(
                "Loading your world…",
                color = Color.White.copy(alpha = alpha),
                fontSize = 15.sp
            )
        }
    }
}