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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
// WORLD PALETTE
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
    onLootBoxClick: () -> Unit = {},        // ← NEW: wired from ChildMainScreen
    viewModel: WorldViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.userId) {
        viewModel.loadWorld(currentUser.userId, fallbackUser = currentUser)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background gradient
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
                // ── Scrollable map canvas ─────────────────────────────────
                WorldMapCanvas(
                    world        = uiState.world!!,
                    userXp       = uiState.currentUser.xp,
                    onNodeTapped = { node -> viewModel.onNodeTapped(node) }
                )
            }
        }

        // HUD always on top
        WorldHud(
            displayName = currentUser.displayName,
            userXp      = uiState.currentUser.xp,
            totalXp     = uiState.world?.totalXpRequired ?: 1000,
            nodes       = uiState.world?.nodes ?: emptyList(),
            onBackClick = onBackClick
        )

        // Node detail bottom sheet
        AnimatedVisibility(
            visible  = uiState.showNodeDetail && uiState.selectedNode != null,
            enter    = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit     = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).zIndex(10f)
        ) {
            uiState.selectedNode?.let { node ->
                NodeDetailCard(
                    node          = node,
                    userXp        = uiState.currentUser.xp,
                    onLootBoxClick = onLootBoxClick,    // ← wired
                    onDismiss     = { viewModel.dismissNodeDetail() }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STAR FIELD — 60 twinkling stars
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StarField() {
    val stars = remember {
        (0 until 60).map {
            Triple(
                (0..100).random() / 100f,
                (0..100).random() / 100f,
                (6..22).random().toFloat()
            )
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        stars.forEachIndexed { index, (xFrac, yFrac, sizeDp) ->
            TwinklingStar(xFrac = xFrac, yFrac = yFrac, sizeDp = sizeDp, delayMs = (index * 137L) % 3000L)
        }
    }
}

@Composable
private fun TwinklingStar(xFrac: Float, yFrac: Float, sizeDp: Float, delayMs: Long) {
    val infiniteTransition = rememberInfiniteTransition(label = "star_$xFrac")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween((1200..2800).random(), easing = FastOutSlowInEasing, delayMillis = delayMs.toInt()),
            repeatMode = RepeatMode.Reverse
        ), label = "starAlpha"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween((1500..3000).random(), easing = EaseInOutSine, delayMillis = (delayMs + 200).toInt()),
            repeatMode = RepeatMode.Reverse
        ), label = "starScale"
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
// NEBULA BLOBS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NebulaClouds() {
    val infiniteTransition = rememberInfiniteTransition(label = "nebula")
    val drift by infiniteTransition.animateFloat(
        initialValue = -12f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(8000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "nebulaDrift"
    )
    val alphaA by infiniteTransition.animateFloat(
        initialValue = 0.06f, targetValue = 0.14f,
        animationSpec = infiniteRepeatable(tween(5000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "nebulaAlpha"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF9B5DE5).copy(alpha = alphaA), Color.Transparent),
                center = Offset(size.width * 0.15f + drift, size.height * 0.2f),
                radius = size.width * 0.45f
            ),
            radius = size.width * 0.45f,
            center = Offset(size.width * 0.15f + drift, size.height * 0.2f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF4ECDC4).copy(alpha = alphaA * 0.8f), Color.Transparent),
                center = Offset(size.width * 0.82f - drift, size.height * 0.75f),
                radius = size.width * 0.4f
            ),
            radius = size.width * 0.4f,
            center = Offset(size.width * 0.82f - drift, size.height * 0.75f)
        )
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
// WORLD MAP CANVAS — SCROLLABLE
// nodes.size * 160.dp tall so the full map is explorable
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WorldMapCanvas(
    world: com.kidsroutine.core.model.WorldModel,
    userXp: Int,
    onNodeTapped: (WorldNode) -> Unit
) {
    var revealed by remember { mutableStateOf(true) }


    val scrollState = rememberScrollState()
    val nodes       = world.nodes
    // Each node occupies 160.dp vertically; add top/bottom padding
    val mapHeightDp = (nodes.size * 160).dp + 160.dp

    // Auto-scroll to the current unlocked node on first load
    val firstUnlockedIndex = nodes.indexOfFirst { it.status == WorldNodeStatus.UNLOCKED }
    val density = androidx.compose.ui.platform.LocalDensity.current
    LaunchedEffect(firstUnlockedIndex) {
        if (firstUnlockedIndex >= 0) {
            val nodeHeightPx = with(density) { 160.dp.toPx() }.toInt()
            val targetPx = ((firstUnlockedIndex * nodeHeightPx) - nodeHeightPx * 2).coerceAtLeast(0)
            scrollState.animateScrollTo(targetPx)
        }
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        val canvasW = maxWidth

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(mapHeightDp)
        ) {
            // ── Connecting paths drawn on a Canvas ────────────────────────
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeightDp)
            ) {
                for (i in 0 until nodes.size - 1) {
                    val from = nodes[i]
                    val to   = nodes[i + 1]
                    // positionX is 0..1 fraction of width
                    // positionY is used as a fraction of mapHeightDp
                    val fromX = from.positionX * size.width
                    val fromY = from.positionY * size.height
                    val toX   = to.positionX * size.width
                    val toY   = to.positionY * size.height

                    val pathDone  = from.status == WorldNodeStatus.COMPLETED
                    val color     = if (pathDone) PathDone else PathColor
                    val dashOn    = 18f
                    val dashOff   = 14f
                    val dist      = sqrt((toX - fromX).pow(2) + (toY - fromY).pow(2))
                    val steps     = (dist / (dashOn + dashOff)).toInt()
                    val dx        = (toX - fromX) / dist
                    val dy        = (toY - fromY) / dist
                    for (s in 0..steps) {
                        val startFrac = s * (dashOn + dashOff)
                        val endFrac   = startFrac + dashOn
                        drawLine(
                            color       = color,
                            start       = Offset(fromX + dx * startFrac, fromY + dy * startFrac),
                            end         = Offset(
                                fromX + dx * endFrac.coerceAtMost(dist),
                                fromY + dy * endFrac.coerceAtMost(dist)
                            ),
                            strokeWidth = 4f,
                            cap         = StrokeCap.Round
                        )
                    }
                }
            }

            // ── Node items positioned absolutely ──────────────────────────
            nodes.forEachIndexed { index, node ->
                // Convert positionX/Y fractions to absolute offsets within the scrollable box
                val nodeX = canvasW * node.positionX
                // positionY fraction is relative to mapHeightDp
                val nodeY = mapHeightDp * node.positionY
                val enterDelay = 150L + index * 80L   // faster stagger for large maps

                AnimatedVisibility(
                    visible  = revealed,
                    enter    = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness    = Spring.StiffnessMedium
                        ),
                        initialScale = 0f
                    ) + fadeIn(tween(300, delayMillis = enterDelay.toInt())),
                    modifier = Modifier.offset(x = nodeX - 36.dp, y = nodeY - 36.dp)
                ) {
                    WorldNodeItem(node = node, onTap = { onNodeTapped(node) })
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SINGLE WORLD NODE — unchanged from original
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WorldNodeItem(node: WorldNode, onTap: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "node_${node.nodeId}")

    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = if (node.status != WorldNodeStatus.LOCKED) -8f else 0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "float"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue  = if (node.status == WorldNodeStatus.UNLOCKED) 0.9f
        else if (node.status == WorldNodeStatus.COMPLETED) 0.6f
        else 0.1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = if (node.status == WorldNodeStatus.UNLOCKED) 1.25f else 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ring"
    )

    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "press"
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
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap   = { onTap() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (node.status != WorldNodeStatus.LOCKED) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(ringScale)
                    .alpha(glowAlpha)
                    .background(glowColor, CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation    = if (node.status != WorldNodeStatus.LOCKED) 12.dp else 4.dp,
                    shape        = CircleShape,
                    ambientColor = nodeColor,
                    spotColor    = nodeColor
                )
                .background(
                    Brush.radialGradient(listOf(nodeColor.copy(alpha = 0.9f), nodeColor.copy(alpha = 0.6f))),
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
                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
            } else {
                Text(node.emoji, fontSize = 26.sp)
            }
        }
        if (node.isSpecial && node.status != WorldNodeStatus.LOCKED) {
            Text("👑", fontSize = 14.sp, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-2).dp))
        }
        if (node.status == WorldNodeStatus.COMPLETED) {
            Surface(
                shape  = CircleShape,
                color  = NodeDone,
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier.size(18.dp).align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP HUD — unchanged
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WorldHud(
    displayName: String,
    userXp: Int,
    totalXp: Int,
    nodes: List<WorldNode>,      // ADD this parameter
    onBackClick: () -> Unit
) {
    // Find next node to unlock
    val nextLockedNode = nodes.firstOrNull { it.status == WorldNodeStatus.LOCKED }
    val xpToNext = if (nextLockedNode != null) (nextLockedNode.requiredXp - userXp).coerceAtLeast(0) else 0
    val currentNode = nodes.lastOrNull { it.status != WorldNodeStatus.LOCKED }
    val progressToNext = if (nextLockedNode != null && nextLockedNode.requiredXp > 0) {
        (userXp.toFloat() / nextLockedNode.requiredXp.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progressToNext,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "xpProgress"
    )
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
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0x99000000),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.12f), CircleShape)
                ) {
                    Text("←", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = currentNode?.title?.let { "🌍 $it" } ?: "🌍 Your World",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = XpGold, modifier = Modifier.size(14.dp))
                            Text("$userXp XP", color = XpGold, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF6B35), Color(0xFFFFD700), Color(0xFFFF6B35)),
                                        startX = shimmerX, endX = shimmerX + 400f
                                    ),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                    if (nextLockedNode != null) {
                        Text(
                            "$xpToNext XP to unlock ${nextLockedNode.emoji} ${nextLockedNode.title}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    } else {
                        Text("🏆 All nodes unlocked!", color = XpGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NODE DETAIL CARD — TODO loot box is now wired to onLootBoxClick
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NodeDetailCard(
    node: WorldNode,
    userXp: Int,
    onLootBoxClick: () -> Unit,   // ← NEW parameter
    onDismiss: () -> Unit
) {
    val isLocked  = node.status == WorldNodeStatus.LOCKED
    val nodeColor = when {
        node.isSpecial && !isLocked          -> NodeSpecial
        node.status == WorldNodeStatus.COMPLETED -> NodeDone
        node.status == WorldNodeStatus.UNLOCKED  -> NodeUnlocked
        else                                     -> NodeLocked
    }

    var bounced by remember { mutableStateOf(false) }
    val emojiScale by animateFloatAsState(
        targetValue   = if (bounced) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label         = "emojiScale"
    )
    LaunchedEffect(Unit) { bounced = true }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        shape    = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
        color    = CardBg,
        border   = BorderStroke(1.dp, CardBorder),
        shadowElevation = 24.dp,
        tonalElevation  = 4.dp
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Drag handle
            Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp)))

            // Big emoji
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(emojiScale)
                    .background(nodeColor.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, nodeColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isLocked) "🔒" else node.emoji, fontSize = 40.sp)
            }

            Text(node.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Text(node.subtitle, color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp, textAlign = TextAlign.Center)

            // Status chip
            Surface(
                shape  = RoundedCornerShape(50.dp),
                color  = nodeColor.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, nodeColor.copy(alpha = 0.5f))
            ) {
                Text(
                    text = when (node.status) {
                        WorldNodeStatus.COMPLETED -> "✅ Completed"
                        WorldNodeStatus.UNLOCKED  -> "🔓 Unlocked — Ready!"
                        WorldNodeStatus.LOCKED    -> "🔒 Requires ${node.requiredXp} XP"
                    },
                    color = nodeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold,
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
                verticalAlignment     = Alignment.CenterVertically
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

            // XP progress bar if locked
            if (isLocked) {
                val xpNeeded     = node.requiredXp - userXp
                val lockProgress = (userXp.toFloat() / node.requiredXp.toFloat()).coerceIn(0f, 1f)
                val animLockProg by animateFloatAsState(
                    targetValue   = lockProgress,
                    animationSpec = tween(900, easing = EaseOutCubic),
                    label         = "lockProg"
                )
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progress", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        Text("$xpNeeded XP to unlock", color = NodeLocked.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.1f))
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

            // Quests hint for unlocked nodes
            if (!isLocked) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    color    = Color.White.copy(alpha = 0.06f),
                    border   = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("✨ Quests in this world", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            node.subtitle.ifBlank { "Complete daily tasks to progress through this world node and unlock the next one!" },
                            color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp
                        )
                    }
                }
            }

            // ── Loot Box button — WIRED (no more TODO) ────────────────────
            if (node.status == WorldNodeStatus.COMPLETED) {
                Button(
                    onClick  = {
                        onDismiss()          // close the detail card first
                        onLootBoxClick()     // then navigate to loot box
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("🎁 Open Loot Box", fontWeight = FontWeight.ExtraBold, color = Color(0xFF7B4F00), fontSize = 15.sp)
                }
            }

            // Dismiss
            OutlinedButton(
                onClick  = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(14.dp),
                border   = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Close", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LOADING SKELETON — unchanged
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WorldLoadingSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "skeletonAlpha"
    )
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(80.dp).alpha(alpha).background(Color.White.copy(alpha = 0.15f), CircleShape))
            Text("Loading your world…", color = Color.White.copy(alpha = alpha), fontSize = 15.sp)
        }
    }
}
