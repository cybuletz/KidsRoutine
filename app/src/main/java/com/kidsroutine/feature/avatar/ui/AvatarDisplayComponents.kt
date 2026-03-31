package com.kidsroutine.feature.avatar.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kidsroutine.core.model.*
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
//  Main Avatar Preview Card  (the big realistic preview)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarPreviewCard(
    avatarState: AvatarState,
    modifier: Modifier = Modifier,
    showNameBadge: Boolean = false,
    playerName: String = ""
) {
    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by shimmer.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing)),
        label = "shimmerX"
    )

    Box(
        modifier = modifier
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black)
    ) {
        // ── Layer 1: Background ───────────────────────────────────────────
        AvatarBackgroundLayer(avatarState.activeBackground)

        // ── Layer 2: Ground / floor shadow ───────────────────────────────
        AvatarGroundLayer()

        // ── Layer 3: Character SVG body ───────────────────────────────────
        AvatarCharacterBody(
            gender = avatarState.gender,
            skinTone = Color(avatarState.skinTone),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxHeight(0.85f)
                .fillMaxWidth(0.75f)
        )

        // ── Layer 4: Outfit overlay ───────────────────────────────────────
        avatarState.activeOutfit?.let { outfit ->
            AvatarOutfitLayer(outfit, avatarState.gender,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(0.65f)
                    .fillMaxWidth(0.75f)
            )
        }

        // ── Layer 5: Hair overlay ──────────────────────────────────────────
        avatarState.activeHair?.let { hair ->
            AvatarHairLayer(hair, avatarState.gender,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(0.85f)
                    .fillMaxWidth(0.75f)
            )
        }

        // ── Layer 6: Accessory overlay ────────────────────────────────────
        avatarState.activeAccessory?.let { acc ->
            AvatarAccessoryLayer(acc,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(0.85f)
                    .fillMaxWidth(0.75f)
            )
        }

        // ── Layer 7: Special FX (animated) ───────────────────────────────
        avatarState.activeSpecialFx?.let {
            AvatarSpecialFxLayer(it)
        }

        // ── Premium shimmer edge (if any premium item equipped) ───────────
        if (avatarState.activeLayers().any { it.isPremium }) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            start = Offset(shimmerX * 400f, 0f),
                            end = Offset(shimmerX * 400f + 200f, 600f)
                        )
                    )
            )
        }

        // ── Name badge ────────────────────────────────────────────────────
        if (showNameBadge && playerName.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.55f),
                tonalElevation = 0.dp
            ) {
                Text(
                    text = playerName,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Background Layer — scenic gradient + illustrated scene elements
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarBackgroundLayer(bg: AvatarLayerItem?) {
    val (topColor, bottomColor) = when (val src = bg?.source) {
        is AvatarAssetSource.GradientBackground ->
            Color(src.topColor) to Color(src.bottomColor)
        else -> Color(0xFF1A1A2E) to Color(0xFF16213E) // default deep blue
    }

    // Gradient sky
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
    )

    // Draw decorative scene elements with Canvas
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawSceneDecorations(this, topColor, bottomColor, bg?.id ?: "default")
    }
}

private fun drawSceneDecorations(
    scope: DrawScope,
    topColor: Color,
    bottomColor: Color,
    bgId: String
) = with(scope) {
    val w = size.width
    val h = size.height

    when {
        bgId.contains("space") || bgId.contains("galaxy") || bgId.contains("nebula") -> {
            // Draw stars
            val starPositions = listOf(
                0.1f to 0.08f, 0.3f to 0.05f, 0.55f to 0.12f, 0.75f to 0.04f,
                0.9f to 0.15f, 0.2f to 0.2f, 0.6f to 0.22f, 0.85f to 0.3f,
                0.05f to 0.35f, 0.45f to 0.18f
            )
            starPositions.forEach { (x, y) ->
                drawCircle(Color.White, radius = 3f, center = Offset(x * w, y * h))
                drawCircle(Color.White.copy(alpha = 0.3f), radius = 7f, center = Offset(x * w, y * h))
            }
        }
        bgId.contains("forest") || bgId.contains("grass") || bgId.contains("gym") -> {
            // Draw simple tree silhouettes
            val treeColor = topColor.copy(alpha = 0.6f)
            listOf(0.05f, 0.18f, 0.82f, 0.93f).forEach { x ->
                drawRect(
                    treeColor,
                    topLeft = Offset(x * w - 6f, h * 0.45f),
                    size = androidx.compose.ui.geometry.Size(12f, h * 0.4f)
                )
                drawCircle(treeColor, radius = 30f, center = Offset(x * w, h * 0.42f))
            }
        }
        bgId.contains("ocean") -> {
            // Draw wave lines
            val waveColor = Color.White.copy(alpha = 0.15f)
            listOf(0.6f, 0.7f, 0.8f).forEach { y ->
                drawLine(waveColor, Offset(0f, y * h), Offset(w, y * h), strokeWidth = 6f)
            }
        }
        bgId.contains("city") -> {
            // Draw city skyline silhouette
            val buildingColor = Color.Black.copy(alpha = 0.5f)
            listOf(
                Triple(0f, 0.55f, 0.12f),
                Triple(0.1f, 0.45f, 0.15f),
                Triple(0.22f, 0.5f, 0.1f),
                Triple(0.75f, 0.42f, 0.18f),
                Triple(0.88f, 0.5f, 0.12f)
            ).forEach { (x, y, width) ->
                drawRect(buildingColor,
                    topLeft = Offset(x * w, y * h),
                    size = androidx.compose.ui.geometry.Size(width * w, (1f - y) * h)
                )
            }
        }
        bgId.contains("sunset") || bgId.contains("beach") -> {
            // Draw sun
            drawCircle(
                Color.White.copy(alpha = 0.9f),
                radius = 35f, center = Offset(w * 0.75f, h * 0.2f)
            )
            drawCircle(
                Color.Yellow.copy(alpha = 0.3f),
                radius = 60f, center = Offset(w * 0.75f, h * 0.2f)
            )
        }
    }
    // Ground line
    drawRect(
        bottomColor.copy(alpha = 0.4f),
        topLeft = Offset(0f, h * 0.82f),
        size = androidx.compose.ui.geometry.Size(w, h * 0.18f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Ground Layer — subtle shadow under the character
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarGroundLayer() {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ellipseW = size.width * 0.55f
            val ellipseH = 20f
            drawOval(
                color = Color.Black.copy(alpha = 0.25f),
                topLeft = Offset((size.width - ellipseW) / 2f, size.height * 0.84f),
                size = androidx.compose.ui.geometry.Size(ellipseW, ellipseH)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Character Body — SVG-style drawn human figure
//  (In production: use VectorDrawable loaded via Coil or painterResource)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarCharacterBody(
    gender: AvatarGender,
    skinTone: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawRealisticCharacter(this, gender, skinTone)
    }
}

private fun drawRealisticCharacter(scope: DrawScope, gender: AvatarGender, skinTone: Color) = with(scope) {
    val w = size.width
    val h = size.height
    val cx = w / 2f

    // ── Neck ──────────────────────────────────────────────────────────────
    val neckW = w * 0.12f
    val neckTop = h * 0.26f
    val neckBot = h * 0.33f
    drawRect(skinTone,
        topLeft = Offset(cx - neckW / 2f, neckTop),
        size = androidx.compose.ui.geometry.Size(neckW, neckBot - neckTop)
    )

    // ── Head ──────────────────────────────────────────────────────────────
    val headR = w * 0.21f
    val headCy = h * 0.18f
    drawCircle(skinTone, radius = headR, center = Offset(cx, headCy))

    // Jaw / chin shape
    val jawPath = Path().apply {
        moveTo(cx - headR * 0.7f, headCy + headR * 0.6f)
        quadraticBezierTo(cx, headCy + headR * 1.25f, cx + headR * 0.7f, headCy + headR * 0.6f)
        close()
    }
    drawPath(jawPath, skinTone)

    // ── Eyes ──────────────────────────────────────────────────────────────
    val eyeY = headCy - headR * 0.05f
    val eyeOffX = headR * 0.38f
    val irisColor = Color(0xFF3D2B1F)
    val eyeRadius = headR * 0.12f
    listOf(-eyeOffX, eyeOffX).forEach { ox ->
        // White
        drawCircle(Color.White, radius = eyeRadius * 1.4f, center = Offset(cx + ox, eyeY))
        // Iris
        drawCircle(irisColor, radius = eyeRadius, center = Offset(cx + ox, eyeY))
        // Pupil
        drawCircle(Color.Black, radius = eyeRadius * 0.55f, center = Offset(cx + ox, eyeY))
        // Highlight
        drawCircle(Color.White, radius = eyeRadius * 0.25f,
            center = Offset(cx + ox + eyeRadius * 0.2f, eyeY - eyeRadius * 0.2f))
    }

    // ── Eyebrows ──────────────────────────────────────────────────────────
    val browY = eyeY - eyeRadius * 2.2f
    val browColor = Color(0xFF3D2B1F)
    listOf(-eyeOffX, eyeOffX).forEach { ox ->
        drawLine(browColor,
            Offset(cx + ox - eyeRadius * 1.1f, browY + eyeRadius * 0.3f),
            Offset(cx + ox + eyeRadius * 1.1f, browY - eyeRadius * 0.3f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }

    // ── Nose ──────────────────────────────────────────────────────────────
    val nosePath = Path().apply {
        moveTo(cx, eyeY + eyeRadius * 1.5f)
        quadraticBezierTo(cx + eyeRadius * 0.9f, eyeY + eyeRadius * 3.5f,
            cx, eyeY + eyeRadius * 3.8f)
        quadraticBezierTo(cx - eyeRadius * 0.9f, eyeY + eyeRadius * 3.5f,
            cx, eyeY + eyeRadius * 1.5f)
    }
    drawPath(nosePath, skinTone.copy(alpha = 0.0f))
    drawLine(skinTone.darken(0.15f),
        Offset(cx, eyeY + eyeRadius * 1.5f),
        Offset(cx + eyeRadius * 0.7f, eyeY + eyeRadius * 3.5f),
        strokeWidth = 3.5f, cap = StrokeCap.Round)
    drawLine(skinTone.darken(0.15f),
        Offset(cx - eyeRadius * 0.7f, eyeY + eyeRadius * 3.5f),
        Offset(cx, eyeY + eyeRadius * 1.5f),
        strokeWidth = 3.5f, cap = StrokeCap.Round)

    // ── Mouth ─────────────────────────────────────────────────────────────
    val mouthY = eyeY + eyeRadius * 5.5f
    val mouthPath = Path().apply {
        moveTo(cx - eyeRadius * 1.1f, mouthY)
        quadraticBezierTo(cx, mouthY + eyeRadius * 1.4f, cx + eyeRadius * 1.1f, mouthY)
    }
    drawPath(mouthPath, Color.Transparent)
    // Lips
    drawLine(Color(0xFFCC7C78),
        Offset(cx - eyeRadius * 1.1f, mouthY),
        Offset(cx + eyeRadius * 1.1f, mouthY),
        strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(Color(0xFFCC7C78).copy(alpha = 0.7f),
        Offset(cx - eyeRadius * 0.8f, mouthY + 3f),
        Offset(cx + eyeRadius * 0.8f, mouthY + 7f),
        strokeWidth = 4f, cap = StrokeCap.Round)

    // ── Ears ──────────────────────────────────────────────────────────────
    listOf(-1f, 1f).forEach { side ->
        drawCircle(skinTone, radius = headR * 0.18f,
            center = Offset(cx + side * headR * 0.95f, headCy + headR * 0.1f))
    }

    // ── Cheek blush ───────────────────────────────────────────────────────
    listOf(-eyeOffX, eyeOffX).forEach { ox ->
        drawCircle(Color(0xFFFFB3BA).copy(alpha = 0.4f),
            radius = eyeRadius * 1.6f,
            center = Offset(cx + ox, eyeY + eyeRadius * 2.2f))
    }

    // ── Body / Torso (will be covered by outfit layer) ────────────────────
    val shoulderY = neckBot
    val hipY = h * 0.62f
    val shoulderW = if (gender == AvatarGender.BOY) w * 0.52f else w * 0.46f
    val hipW = if (gender == AvatarGender.BOY) w * 0.44f else w * 0.50f

    val torsoPath = Path().apply {
        moveTo(cx - shoulderW / 2f, shoulderY)
        lineTo(cx + shoulderW / 2f, shoulderY)
        lineTo(cx + hipW / 2f, hipY)
        lineTo(cx - hipW / 2f, hipY)
        close()
    }
    // Draw base torso in skin tone (outfit layer covers this)
    drawPath(torsoPath, Color(0xFF6B8CFF).copy(alpha = 0.6f)) // placeholder outfit colour

    // ── Arms ──────────────────────────────────────────────────────────────
    val armW = w * 0.1f
    val armLen = h * 0.28f
    listOf(-1f, 1f).forEach { side ->
        val armX = cx + side * (shoulderW / 2f + armW * 0.3f)
        drawRoundRect(
            skinTone,
            topLeft = Offset(armX - armW / 2f, shoulderY + h * 0.01f),
            size = androidx.compose.ui.geometry.Size(armW, armLen),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(armW / 2f)
        )
        // Hand
        drawCircle(skinTone, radius = armW * 0.6f,
            center = Offset(armX, shoulderY + armLen + h * 0.015f))
    }

    // ── Legs ──────────────────────────────────────────────────────────────
    val legW = w * 0.155f
    val legLen = h * 0.30f
    val legColor = Color(0xFF2B4DAB) // dark jeans colour (shoes layer covers bottom)
    listOf(-1f, 1f).forEach { side ->
        val legX = cx + side * legW * 0.6f
        drawRoundRect(
            legColor,
            topLeft = Offset(legX - legW / 2f, hipY),
            size = androidx.compose.ui.geometry.Size(legW, legLen),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(legW * 0.3f)
        )
        // Shoes / feet
        drawRoundRect(
            Color(0xFF1A1A1A),
            topLeft = Offset(legX - legW * 0.65f, hipY + legLen - 5f),
            size = androidx.compose.ui.geometry.Size(legW * 1.3f, legLen * 0.18f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
        )
    }
}

private fun Color.darken(by: Float): Color =
    Color(red * (1f - by), green * (1f - by), blue * (1f - by), alpha)

// ─────────────────────────────────────────────────────────────────────────────
//  Outfit Layer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarOutfitLayer(
    outfit: AvatarLayerItem,
    gender: AvatarGender,
    modifier: Modifier = Modifier
) {
    val tint = outfit.tintColor?.let { Color(it) } ?: Color(0xFF5272F2)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val shoulderW = if (gender == AvatarGender.BOY) w * 0.52f else w * 0.46f
        val hipW = if (gender == AvatarGender.BOY) w * 0.44f else w * 0.50f
        val shoulderY = h * 0.385f
        val hipY = h * 0.72f

        // Torso clothing
        val torsoPath = Path().apply {
            moveTo(cx - shoulderW / 2f, shoulderY)
            lineTo(cx + shoulderW / 2f, shoulderY)
            lineTo(cx + hipW / 2f, hipY)
            lineTo(cx - hipW / 2f, hipY)
            close()
        }
        drawPath(torsoPath, tint)

        // Collar
        val collarPath = Path().apply {
            moveTo(cx - w * 0.08f, shoulderY)
            lineTo(cx, shoulderY + h * 0.06f)
            lineTo(cx + w * 0.08f, shoulderY)
        }
        drawPath(collarPath, tint.darken(0.15f))

        // Sleeves
        listOf(-1f, 1f).forEach { side ->
            val sleeveX = cx + side * (shoulderW / 2f)
            val sleeveEnd = Offset(sleeveX + side * w * 0.06f, shoulderY + h * 0.14f)
            drawLine(tint.darken(0.08f),
                Offset(sleeveX, shoulderY),
                sleeveEnd,
                strokeWidth = w * 0.12f,
                cap = StrokeCap.Round
            )
        }

        // Shirt buttons (if not a sporty outfit)
        if (!outfit.id.contains("sport") && !outfit.id.contains("ninja") && !outfit.id.contains("astronaut")) {
            listOf(0.45f, 0.55f, 0.65f).forEach { yRatio ->
                drawCircle(
                    Color.White.copy(alpha = 0.6f),
                    radius = 4f,
                    center = Offset(cx, shoulderY + (hipY - shoulderY) * yRatio)
                )
            }
        }

        // Premium glow outline
        if (outfit.isPremium) {
            drawPath(torsoPath, Color.White.copy(alpha = 0.15f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Hair Layer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarHairLayer(
    hair: AvatarLayerItem,
    gender: AvatarGender,
    modifier: Modifier = Modifier
) {
    val hairColor = hair.tintColor?.let { Color(it) } ?: Color(0xFF3D2B1F)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val headR = w * 0.21f
        val headCy = h * 0.18f

        when {
            hair.id.contains("short") -> {
                // Short hair cap
                val capPath = Path().apply {
                    moveTo(cx - headR * 1.05f, headCy)
                    quadraticBezierTo(cx - headR * 0.9f, headCy - headR * 1.4f,
                        cx, headCy - headR * 1.15f)
                    quadraticBezierTo(cx + headR * 0.9f, headCy - headR * 1.4f,
                        cx + headR * 1.05f, headCy)
                    close()
                }
                drawPath(capPath, hairColor)
                // Side burns / sidecut
                listOf(-1f, 1f).forEach { side ->
                    drawRoundRect(
                        hairColor,
                        topLeft = Offset(cx + side * headR * 0.75f, headCy - headR * 0.1f),
                        size = androidx.compose.ui.geometry.Size(headR * 0.28f, headR * 0.7f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                    )
                }
            }
            hair.id.contains("long") -> {
                // Long flowing hair
                val capPath = Path().apply {
                    moveTo(cx - headR * 1.05f, headCy)
                    quadraticBezierTo(cx - headR * 0.9f, headCy - headR * 1.4f,
                        cx, headCy - headR * 1.15f)
                    quadraticBezierTo(cx + headR * 0.9f, headCy - headR * 1.4f,
                        cx + headR * 1.05f, headCy)
                    close()
                }
                drawPath(capPath, hairColor)
                // Long strands down both sides
                listOf(-1f, 1f).forEach { side ->
                    val strandPath = Path().apply {
                        moveTo(cx + side * headR * 0.9f, headCy + headR * 0.4f)
                        quadraticBezierTo(
                            cx + side * headR * 1.2f, headCy + headR * 1.8f,
                            cx + side * headR * 0.95f, headCy + headR * 3.2f
                        )
                        quadraticBezierTo(
                            cx + side * headR * 1.1f, headCy + headR * 3.5f,
                            cx + side * headR * 0.85f, headCy + headR * 3.6f
                        )
                        quadraticBezierTo(
                            cx + side * headR * 0.7f, headCy + headR * 2.5f,
                            cx + side * headR * 0.75f, headCy + headR * 0.5f
                        )
                        close()
                    }
                    drawPath(strandPath, hairColor)
                }
            }
            hair.id.contains("ponytail") -> {
                // Base cap
                val capPath = Path().apply {
                    moveTo(cx - headR * 1.05f, headCy)
                    quadraticBezierTo(cx - headR * 0.9f, headCy - headR * 1.4f,
                        cx, headCy - headR * 1.15f)
                    quadraticBezierTo(cx + headR * 0.9f, headCy - headR * 1.4f,
                        cx + headR * 1.05f, headCy)
                    close()
                }
                drawPath(capPath, hairColor)
                // Ponytail at back
                val ponytailPath = Path().apply {
                    moveTo(cx + headR * 0.3f, headCy - headR * 0.8f)
                    quadraticBezierTo(cx + headR * 1.6f, headCy,
                        cx + headR * 1.3f, headCy + headR * 1.8f)
                    quadraticBezierTo(cx + headR * 1.5f, headCy + headR * 2.0f,
                        cx + headR * 1.0f, headCy + headR * 1.9f)
                    quadraticBezierTo(cx + headR * 1.1f, headCy + headR * 0.3f,
                        cx + headR * 0.1f, headCy - headR * 0.7f)
                    close()
                }
                drawPath(ponytailPath, hairColor)
                // Hairband
                drawCircle(Color(0xFFFF6B9D), radius = 6f,
                    center = Offset(cx + headR * 0.85f, headCy - headR * 0.5f))
            }
            hair.id.contains("curly") -> {
                // Curly afro-style
                val curlPositions = listOf(
                    Offset(cx, headCy - headR * 1.35f) to headR * 0.55f,
                    Offset(cx - headR * 0.6f, headCy - headR * 1.2f) to headR * 0.45f,
                    Offset(cx + headR * 0.6f, headCy - headR * 1.2f) to headR * 0.45f,
                    Offset(cx - headR * 0.95f, headCy - headR * 0.7f) to headR * 0.4f,
                    Offset(cx + headR * 0.95f, headCy - headR * 0.7f) to headR * 0.4f,
                    Offset(cx - headR * 1.0f, headCy - headR * 0.1f) to headR * 0.35f,
                    Offset(cx + headR * 1.0f, headCy - headR * 0.1f) to headR * 0.35f
                )
                curlPositions.forEach { (pos, r) ->
                    drawCircle(hairColor, radius = r, center = pos)
                }
            }
            else -> {
                // Fallback: short cap
                val fallbackPath = Path().apply {
                    moveTo(cx - headR * 1.05f, headCy)
                    quadraticBezierTo(cx - headR * 0.9f, headCy - headR * 1.3f,
                        cx, headCy - headR * 1.1f)
                    quadraticBezierTo(cx + headR * 0.9f, headCy - headR * 1.3f,
                        cx + headR * 1.05f, headCy)
                    close()
                }
                drawPath(fallbackPath, hairColor)
            }
        }

        // Highlight streak on all hair types
        drawLine(
            hairColor.copy(alpha = 0.35f),
            Offset(cx - headR * 0.15f, headCy - headR * 1.1f),
            Offset(cx + headR * 0.2f, headCy - headR * 0.3f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Accessory Layer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarAccessoryLayer(
    accessory: AvatarLayerItem,
    modifier: Modifier = Modifier
) {
    val tint = accessory.tintColor?.let { Color(it) } ?: Color(0xFFFFD700)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val headR = w * 0.21f
        val headCy = h * 0.18f

        when {
            accessory.id.contains("headband") -> {
                // Ninja headband
                val bandPath = Path().apply {
                    moveTo(cx - headR * 1.1f, headCy - headR * 0.35f)
                    quadraticBezierTo(cx, headCy - headR * 0.55f,
                        cx + headR * 1.1f, headCy - headR * 0.35f)
                }
                drawPath(bandPath, Color.Transparent)
                drawLine(tint, Offset(cx - headR * 1.1f, headCy - headR * 0.35f),
                    Offset(cx + headR * 1.1f, headCy - headR * 0.35f),
                    strokeWidth = 14f, cap = StrokeCap.Round)
                // Metal plate
                drawRoundRect(tint.darken(0.1f),
                    topLeft = Offset(cx - 18f, headCy - headR * 0.45f),
                    size = androidx.compose.ui.geometry.Size(36f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f))
                // Scratched symbol on plate
                drawLine(Color.White.copy(alpha = 0.5f),
                    Offset(cx - 8f, headCy - headR * 0.38f),
                    Offset(cx + 8f, headCy - headR * 0.28f),
                    strokeWidth = 2f)
            }
            accessory.id.contains("cap") || accessory.id.contains("hat") -> {
                // Baseball cap / hat brim
                drawRoundRect(tint,
                    topLeft = Offset(cx - headR * 1.15f, headCy - headR * 0.55f),
                    size = androidx.compose.ui.geometry.Size(headR * 2.3f, headR * 0.35f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f))
                // Hat dome
                val domePath = Path().apply {
                    moveTo(cx - headR * 0.95f, headCy - headR * 0.55f)
                    quadraticBezierTo(cx - headR * 0.8f, headCy - headR * 1.6f,
                        cx, headCy - headR * 1.5f)
                    quadraticBezierTo(cx + headR * 0.8f, headCy - headR * 1.6f,
                        cx + headR * 0.95f, headCy - headR * 0.55f)
                    close()
                }
                drawPath(domePath, tint)
            }
            accessory.id.contains("mask") -> {
                // Hero mask
                listOf(-1f, 1f).forEach { side ->
                    val eyeX = cx + side * headR * 0.38f
                    val eyeY = headCy - headR * 0.05f
                    val maskPath = Path().apply {
                        moveTo(eyeX - headR * 0.28f, eyeY - headR * 0.22f)
                        lineTo(eyeX + headR * 0.28f, eyeY - headR * 0.22f)
                        lineTo(eyeX + headR * 0.35f, eyeY + headR * 0.18f)
                        lineTo(eyeX - headR * 0.35f, eyeY + headR * 0.18f)
                        close()
                    }
                    drawPath(maskPath, tint)
                }
                // Bridge of mask
                drawLine(tint,
                    Offset(cx - headR * 0.1f, headCy - headR * 0.05f),
                    Offset(cx + headR * 0.1f, headCy - headR * 0.05f),
                    strokeWidth = 10f)
            }
            accessory.id.contains("pokeball") || accessory.id.contains("belt") -> {
                // Belt with Pokéball buckle
                drawLine(Color(0xFF4A3728),
                    Offset(cx - headR * 1.4f, h * 0.56f),
                    Offset(cx + headR * 1.4f, h * 0.56f),
                    strokeWidth = 16f, cap = StrokeCap.Round)
                // Buckle circle (Pokéball style)
                drawCircle(Color.White, radius = 13f, center = Offset(cx, h * 0.56f))
                drawCircle(Color.Red, radius = 8f, center = Offset(cx, h * 0.56f - 6f))
                drawLine(Color(0xFF333333), Offset(cx - 13f, h * 0.56f),
                    Offset(cx + 13f, h * 0.56f), strokeWidth = 2.5f)
                drawCircle(Color(0xFF333333), radius = 3.5f, center = Offset(cx, h * 0.56f))
                drawCircle(Color.White, radius = 2f, center = Offset(cx, h * 0.56f))
            }
            else -> { /* No default accessory */ }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Special FX Layer — animated overlays (fire, stars, lightning)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarSpecialFxLayer(fx: AvatarLayerItem) {
    val infiniteTransition = rememberInfiniteTransition(label = "fx")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse),
        label = "pulse"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "rotation"
    )

    Canvas(modifier = Modifier
        .fillMaxSize()
        .graphicsLayer { rotationZ = if (fx.id.contains("star") || fx.id.contains("lightning")) rotation else 0f }
    ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        when {
            fx.id.contains("fire") -> {
                // Fire aura around the character base
                val fireColors = listOf(
                    Color(0xFFFF4500).copy(alpha = 0.7f * pulse),
                    Color(0xFFFF8C00).copy(alpha = 0.5f * pulse),
                    Color(0xFFFFD700).copy(alpha = 0.3f * pulse)
                )
                fireColors.forEachIndexed { i, color ->
                    val radius = (w * 0.38f) + (i * w * 0.06f)
                    drawOval(color,
                        topLeft = Offset(cx - radius * 0.85f, h * 0.2f),
                        size = androidx.compose.ui.geometry.Size(radius * 1.7f, h * 0.7f)
                    )
                }
                // Flame tips
                listOf(0.3f, 0.5f, 0.7f).forEachIndexed { i, xRatio ->
                    val flameH = h * (0.12f + i * 0.03f) * pulse
                    val flameX = cx + (xRatio - 0.5f) * w * 0.8f
                    val flamePath = Path().apply {
                        moveTo(flameX, h * 0.18f)
                        quadraticBezierTo(flameX + 15f, h * 0.18f - flameH * 0.5f,
                            flameX, h * 0.18f - flameH)
                        quadraticBezierTo(flameX - 15f, h * 0.18f - flameH * 0.5f,
                            flameX, h * 0.18f)
                    }
                    drawPath(flamePath, Color(0xFFFF4500).copy(alpha = 0.8f * pulse))
                }
            }
            fx.id.contains("star") || fx.id.contains("stars") -> {
                // Orbiting star particles
                val orbitR = w * 0.48f
                val starCount = 6
                repeat(starCount) { i ->
                    val angle = (i * 60f) * (Math.PI / 180f).toFloat()
                    val sx = cx + orbitR * cos(angle)
                    val sy = h * 0.45f + orbitR * 0.6f * sin(angle)
                    drawCircle(Color(0xFFFFD700).copy(alpha = 0.7f * pulse),
                        radius = 8f * pulse, center = Offset(sx, sy))
                    drawCircle(Color.White.copy(alpha = 0.5f),
                        radius = 3f, center = Offset(sx, sy))
                }
            }
            fx.id.contains("lightning") -> {
                // Electric lightning bolts around character
                val boltColor = Color(0xFF00CFFF).copy(alpha = 0.8f * pulse)
                listOf(-1f, 1f).forEach { side ->
                    val startX = cx + side * w * 0.3f
                    val boltPath = Path().apply {
                        moveTo(startX, h * 0.15f)
                        lineTo(startX + side * 12f, h * 0.3f)
                        lineTo(startX - side * 6f, h * 0.35f)
                        lineTo(startX + side * 18f, h * 0.55f)
                    }
                    drawPath(boltPath, Color.Transparent)
                    drawLine(boltColor, Offset(startX, h * 0.15f),
                        Offset(startX + side * 12f, h * 0.3f), strokeWidth = 5f * pulse)
                    drawLine(boltColor, Offset(startX + side * 12f, h * 0.3f),
                        Offset(startX - side * 6f, h * 0.35f), strokeWidth = 5f * pulse)
                    drawLine(boltColor, Offset(startX - side * 6f, h * 0.35f),
                        Offset(startX + side * 18f, h * 0.55f), strokeWidth = 5f * pulse)
                    // Glow
                    drawCircle(boltColor.copy(alpha = 0.3f), radius = 22f * pulse,
                        center = Offset(startX + side * 8f, h * 0.35f))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Skin Tone Picker Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SkinTonePicker(
    selectedTone: Long,
    onToneSelected: (Long) -> Unit
) {
    val skinTones = listOf(
        0xFFFFDBAD to "Light",
        0xFFF1C27D to "Medium Light",
        0xFFE0AC69 to "Medium",
        0xFFC68642 to "Medium Dark",
        0xFF8D5524 to "Dark",
        0xFF4A2912 to "Deep"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        skinTones.forEach { (tone, label) ->
            val isSelected = selectedTone == tone
            Box(
                modifier = Modifier
                    .size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer selection ring
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .border(2.5.dp, Color(0xFF5272F2), CircleShape)
                    )
                }
                // Swatch circle
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 34.dp else 30.dp)
                        .clip(CircleShape)
                        .background(Color(tone))
                        .border(
                            width = 1.5.dp,
                            color = Color(0xFF000000).copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                        .clickable { onToneSelected(tone) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Gender Selector
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GenderSelector(
    selected: AvatarGender,
    onSelect: (AvatarGender) -> Unit
) {
    // Segmented pill container
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AvatarGender.entries.forEach { gender ->
            val isSelected = gender == selected
            Surface(
                onClick = { onSelect(gender) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Color(0xFF5272F2) else Color(0xFFE2DEFF),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color(0xFF5272F2) else Color(0xFF5272F2).copy(alpha = 0.25f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        if (gender == AvatarGender.BOY) "👦" else "👧",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        gender.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFF3D3A5C)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Compact Avatar Chip — used in task cards, leaderboard, etc.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarChip(
    avatarState: AvatarState,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Black)
    ) {
        AvatarBackgroundLayer(avatarState.activeBackground)
        AvatarCharacterBody(
            gender = avatarState.gender,
            skinTone = Color(avatarState.skinTone),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxHeight(0.9f)
                .fillMaxWidth(0.85f)
        )
        avatarState.activeHair?.let {
            AvatarHairLayer(it, avatarState.gender,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(0.9f)
                    .fillMaxWidth(0.85f)
            )
        }
    }
}