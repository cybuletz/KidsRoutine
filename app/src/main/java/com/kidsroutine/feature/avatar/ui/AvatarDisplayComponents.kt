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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kidsroutine.core.model.*
import kotlin.math.*

// ═══════════════════════════════════════════════════════════════════════════════
//  Modern 3D-Style Avatar System — Portrait Rendering Engine
//  Produces cute, detailed cartoon avatars with 3D depth via layered gradients
// ═══════════════════════════════════════════════════════════════════════════════

// ─────────────────────────────────────────────────────────────────────────────
//  Main Avatar Preview Card
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
        // Layer 1: Background
        AvatarBackgroundLayer(avatarState.activeBackground)

        // Layer 2: Character portrait
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            drawPortraitCharacter(
                avatarState = avatarState,
                gender = avatarState.gender,
                skinTone = Color(avatarState.skinTone),
                eyeStyleItem = avatarState.activeEyeStyle,
                faceDetailItem = avatarState.activeFaceDetail,
                eyeShape = avatarState.eyeShape,
                mouthShape = avatarState.mouthShape,
                eyebrowStyle = avatarState.eyebrowStyle,
                hairItem = avatarState.activeHair,
                hairColor = Color(avatarState.resolvedHairColor),
                outfitItem = avatarState.activeOutfit,
                accessoryItem = avatarState.activeAccessory
            )
        }

        // Layer 3: Animated Special FX
        avatarState.activeSpecialFx?.let {
            AvatarSpecialFxLayer(it)
        }

        // Premium shimmer edge
        if (avatarState.activeLayers().any { it.isPremium }) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            start = Offset(shimmerX * 400f, 0f),
                            end = Offset(shimmerX * 400f + 200f, 600f)
                        )
                    )
            )
        }

        // Name badge
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
//  Background Layer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AvatarBackgroundLayer(bg: AvatarLayerItem?) {
    val (topColor, bottomColor) = when (val src = bg?.source) {
        is AvatarAssetSource.GradientBackground ->
            Color(src.topColor) to Color(src.bottomColor)
        else -> Color(0xFF87CEEB) to Color(0xFFE0F7FA)
    }
    val label = (bg?.source as? AvatarAssetSource.GradientBackground)?.label ?: ""

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Main gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(topColor, bottomColor)
            )
        )

        // Scene-specific decorative elements
        val w = size.width
        val h = size.height
        when {
            label.contains("Space", true) -> {
                // Twinkling stars
                val starPositions = listOf(
                    0.1f to 0.08f, 0.85f to 0.12f, 0.3f to 0.05f,
                    0.7f to 0.18f, 0.15f to 0.22f, 0.55f to 0.03f,
                    0.92f to 0.25f, 0.4f to 0.15f, 0.78f to 0.08f,
                    0.05f to 0.15f, 0.65f to 0.22f, 0.48f to 0.10f
                )
                starPositions.forEach { (sx, sy) ->
                    val starSize = (2f + (sx * 37).toInt() % 4) * (w / 300f)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f + (sy * 13).toInt() % 5 * 0.1f),
                        radius = starSize,
                        center = Offset(sx * w, sy * h)
                    )
                }
                // Subtle nebula glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x30FF69B4),
                            Color.Transparent
                        )
                    ),
                    radius = w * 0.35f,
                    center = Offset(w * 0.2f, h * 0.15f)
                )
            }
            label.contains("Forest", true) -> {
                // Distant tree silhouettes
                val treeColor = bottomColor.darken(0.25f).copy(alpha = 0.3f)
                for (i in 0..6) {
                    val tx = w * (0.05f + i * 0.15f)
                    val th = h * (0.08f + (i % 3) * 0.04f)
                    val treePath = Path().apply {
                        moveTo(tx, h * 0.35f)
                        lineTo(tx - w * 0.04f, h * 0.35f + th)
                        lineTo(tx + w * 0.04f, h * 0.35f + th)
                        close()
                    }
                    drawPath(treePath, treeColor)
                }
            }
            label.contains("Ocean", true) -> {
                // Gentle waves
                for (i in 0..3) {
                    val waveY = h * (0.30f + i * 0.05f)
                    val wavePath = Path().apply {
                        moveTo(0f, waveY)
                        for (j in 0..8) {
                            val wx = w * j / 8f
                            val wy = waveY + sin(j * 0.8f + i) * h * 0.012f
                            lineTo(wx, wy)
                        }
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(
                        wavePath,
                        bottomColor.copy(alpha = 0.15f + i * 0.05f)
                    )
                }
            }
            label.contains("Sunset", true) -> {
                // Sun disk
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x60FFFFFF),
                            Color(0x30FFD93D),
                            Color.Transparent
                        )
                    ),
                    radius = w * 0.2f,
                    center = Offset(w * 0.5f, h * 0.18f)
                )
                // Horizon glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x20FFFFFF)),
                        startY = h * 0.25f,
                        endY = h * 0.45f
                    )
                )
            }
            label.contains("Candy", true) -> {
                // Floating candy shapes
                val candyColors = listOf(
                    Color(0x40FF69B4), Color(0x4000CED1),
                    Color(0x40FFD700), Color(0x40FF6347)
                )
                val positions = listOf(
                    0.12f to 0.10f, 0.82f to 0.08f,
                    0.25f to 0.25f, 0.72f to 0.20f
                )
                positions.forEachIndexed { idx, (px, py) ->
                    drawCircle(
                        color = candyColors[idx % candyColors.size],
                        radius = w * 0.035f,
                        center = Offset(px * w, py * h)
                    )
                }
            }
            label.contains("Volcano", true) || label.contains("Fire", true) -> {
                // Lava glow at bottom
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x30FF4500)),
                        startY = h * 0.7f,
                        endY = h
                    )
                )
            }
        }

        // Soft vignette for depth
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.15f)),
                center = Offset(w / 2f, h / 2f),
                radius = maxOf(w, h) * 0.7f
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Main Portrait Drawing Function
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawPortraitCharacter(
    avatarState: AvatarState,
    gender: AvatarGender,
    skinTone: Color,
    eyeStyleItem: AvatarLayerItem?,
    faceDetailItem: AvatarLayerItem?,
    eyeShape: String?,
    mouthShape: String?,
    eyebrowStyle: String?,
    hairItem: AvatarLayerItem?,
    hairColor: Color,
    outfitItem: AvatarLayerItem?,
    accessoryItem: AvatarLayerItem?
) {
    val w = size.width
    val h = size.height
    val faceShape = avatarState.faceShape ?: "oval"

    // Portrait proportions — big head, small shoulders
    val headCX = w * 0.5f
    val headCY = h * 0.38f

    // Face shape affects head radii
    val (baseRX, baseRY) = when (faceShape) {
        "round" -> w * 0.34f to h * 0.27f
        "heart" -> w * 0.33f to h * 0.265f
        "square" -> w * 0.335f to h * 0.258f
        "diamond" -> w * 0.32f to h * 0.265f
        "petite" -> w * 0.30f to h * 0.24f
        else -> w * 0.33f to h * 0.26f // oval (default)
    }
    // Girls get slightly softer/rounder proportions
    val headRX = if (gender == AvatarGender.GIRL) baseRX * 0.97f else baseRX
    val headRY = if (gender == AvatarGender.GIRL) baseRY * 1.01f else baseRY

    // Neck and body positions — gender-differentiated
    val neckTopY = headCY + headRY * 0.88f
    val neckW = headRX * (if (gender == AvatarGender.GIRL) 0.36f else 0.44f)
    val shoulderY = h * 0.72f
    val shoulderW = w * (if (gender == AvatarGender.GIRL) 0.38f else 0.44f)

    // ── 1. Draw neck and shoulders (behind head) ──────────────────────────
    drawNeckAndShoulders(headCX, neckTopY, neckW, shoulderY, shoulderW, h, skinTone, gender)

    // ── 2. Draw outfit (upper body clothing) ──────────────────────────────
    drawOutfitUpper(outfitItem, headCX, neckTopY, shoulderY, shoulderW, h, w, gender)

    // ── 3. Draw back hair layer ───────────────────────────────────────────
    drawHairBack(hairItem, headCX, headCY, headRX, headRY, hairColor, gender)

    // ── 4. Draw ears ─────────────────────────────────────────────────────
    drawEars(headCX, headCY, headRX, headRY, skinTone)

    // ── 5. Draw head/face shape with 3D gradients ────────────────────────
    drawHead(headCX, headCY, headRX, headRY, skinTone, gender, faceShape)

    // ── 6. Draw eyes ─────────────────────────────────────────────────────
    val eyeColor = eyeStyleItem?.tintColor?.let { Color(it) } ?: Color(0xFF6B4226)
    drawEyes(headCX, headCY, headRX, headRY, eyeColor, eyeShape, gender)

    // ── 7. Draw eyebrows ─────────────────────────────────────────────────
    drawEyebrows(headCX, headCY, headRX, headRY, hairColor, gender, eyebrowStyle)

    // ── 8. Draw nose ─────────────────────────────────────────────────────
    drawNose(headCX, headCY, headRY, skinTone, gender)

    // ── 9. Draw mouth ────────────────────────────────────────────────────
    drawMouth(headCX, headCY, headRY, skinTone, gender, mouthShape)

    // ── 10. Draw face details (freckles, blush, dimples, etc.) ───────────
    drawFaceDetails(faceDetailItem, headCX, headCY, headRX, headRY, skinTone)

    // ── 11. Draw front hair layer ────────────────────────────────────────
    drawHairFront(hairItem, headCX, headCY, headRX, headRY, hairColor, gender)

    // ── 12. Draw accessories ─────────────────────────────────────────────
    drawAccessory(accessoryItem, headCX, headCY, headRX, headRY, shoulderY, w, h, gender)
}

// ─────────────────────────────────────────────────────────────────────────────
//  Head / Face Shape with 3D Gradients
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawHead(
    cx: Float, cy: Float, rx: Float, ry: Float,
    skin: Color, gender: AvatarGender, faceShape: String = "oval"
) {
    val headPath = buildHeadPath(cx, cy, rx, ry, gender, faceShape)

    // 1. Base skin fill
    drawPath(headPath, skin)

    // 2. Edge darkening for 3D roundness (radial gradient)
    drawPath(
        headPath,
        Brush.radialGradient(
            colors = listOf(Color.Transparent, skin.darken(0.06f), skin.darken(0.14f)),
            center = Offset(cx, cy - ry * 0.12f),
            radius = rx * 1.4f
        )
    )

    // 3. Forehead highlight — soft bright oval
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(skin.lighten(0.10f), Color.Transparent),
            center = Offset(cx + rx * 0.05f, cy - ry * 0.42f),
            radius = rx * 0.5f
        ),
        topLeft = Offset(cx - rx * 0.35f, cy - ry * 0.7f),
        size = Size(rx * 0.7f, ry * 0.5f)
    )

    // 4. Left cheek warm glow — stronger for girls (cuter look)
    val cheekGlow = if (gender == AvatarGender.GIRL) 0.10f else 0.07f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(skin.lighten(cheekGlow), Color.Transparent),
            center = Offset(cx - rx * 0.5f, cy + ry * 0.15f),
            radius = rx * 0.3f
        ),
        radius = rx * 0.3f,
        center = Offset(cx - rx * 0.5f, cy + ry * 0.15f)
    )

    // 5. Right cheek warm glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(skin.lighten(cheekGlow), Color.Transparent),
            center = Offset(cx + rx * 0.5f, cy + ry * 0.15f),
            radius = rx * 0.3f
        ),
        radius = rx * 0.3f,
        center = Offset(cx + rx * 0.5f, cy + ry * 0.15f)
    )

    // 6. Chin shadow
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(skin.darken(0.10f), Color.Transparent),
            center = Offset(cx, cy + ry * 0.8f),
            radius = rx * 0.5f
        ),
        topLeft = Offset(cx - rx * 0.4f, cy + ry * 0.6f),
        size = Size(rx * 0.8f, ry * 0.35f)
    )

    // 7. Subtle nose bridge shadow line
    drawLine(
        color = skin.darken(0.06f),
        start = Offset(cx, cy - ry * 0.05f),
        end = Offset(cx, cy + ry * 0.28f),
        strokeWidth = rx * 0.015f
    )

    // 8. Rim light on left edge (simulates directional lighting)
    val rimPath = Path().apply {
        val startAngle = 140f
        val sweepAngle = 120f
        addArc(
            oval = androidx.compose.ui.geometry.Rect(
                cx - rx, cy - ry, cx + rx, cy + ry
            ),
            startAngleDegrees = startAngle,
            sweepAngleDegrees = sweepAngle
        )
    }
    drawPath(
        rimPath,
        color = Color.White.copy(alpha = 0.06f),
        style = Stroke(width = rx * 0.06f)
    )

    // 9. Girls get a subtle pink cheek blush built-in
    if (gender == AvatarGender.GIRL) {
        val blushColor = Color(0xFFFFB6C1)
        for (side in listOf(-1f, 1f)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(blushColor.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(cx + side * rx * 0.45f, cy + ry * 0.25f),
                    radius = rx * 0.16f
                ),
                radius = rx * 0.16f,
                center = Offset(cx + side * rx * 0.45f, cy + ry * 0.25f)
            )
        }
    }
}

private fun buildHeadPath(
    cx: Float, cy: Float, rx: Float, ry: Float,
    gender: AvatarGender, faceShape: String = "oval"
): Path {
    // Each face shape gets unique chin, cheek, forehead proportions
    val (chinNarrow, cheekWidth, foreheadWidth, chinLength, jawCurve) = when (faceShape) {
        "round" -> FaceParams(0.92f, 1.0f, 0.95f, 0.95f, 0.50f)
        "heart" -> FaceParams(0.72f, 0.98f, 1.02f, 1.0f, 0.70f)
        "square" -> FaceParams(0.92f, 1.02f, 1.0f, 0.95f, 0.35f)
        "diamond" -> FaceParams(0.78f, 0.95f, 0.92f, 1.02f, 0.60f)
        "petite" -> FaceParams(0.80f, 0.92f, 0.90f, 0.95f, 0.55f)
        else -> FaceParams(0.85f, 0.98f, 0.98f, 1.0f, 0.45f) // oval
    }

    // Apply gender modifier — girls get slightly softer chin
    val finalChin = if (gender == AvatarGender.GIRL) chinNarrow * 0.96f else chinNarrow

    return Path().apply {
        // Organic shape: forehead → cheeks → chin with shape-specific curves
        val fhW = foreheadWidth
        val chW = cheekWidth
        val cLen = chinLength

        moveTo(cx, cy - ry)
        // Right side (forehead to ear level)
        cubicTo(
            cx + rx * 0.72f * fhW, cy - ry,
            cx + rx * chW, cy - ry * 0.38f,
            cx + rx * chW, cy + ry * 0.05f
        )
        // Right side (ear level to jaw)
        cubicTo(
            cx + rx * chW, cy + ry * (0.30f + jawCurve * 0.20f),
            cx + rx * finalChin, cy + ry * 0.78f * cLen,
            cx, cy + ry * cLen
        )
        // Left side (jaw to ear level)
        cubicTo(
            cx - rx * finalChin, cy + ry * 0.78f * cLen,
            cx - rx * chW, cy + ry * (0.30f + jawCurve * 0.20f),
            cx - rx * chW, cy + ry * 0.05f
        )
        // Left side (ear level to forehead)
        cubicTo(
            cx - rx * chW, cy - ry * 0.38f,
            cx - rx * 0.72f * fhW, cy - ry,
            cx, cy - ry
        )
        close()
    }
}

/** Data holder for face shape parameters */
private data class FaceParams(
    val chinNarrow: Float,
    val cheekWidth: Float,
    val foreheadWidth: Float,
    val chinLength: Float,
    val jawCurve: Float
)

// ─────────────────────────────────────────────────────────────────────────────
//  Ears
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawEars(
    cx: Float, cy: Float, rx: Float, ry: Float, skin: Color
) {
    val earY = cy + ry * 0.05f
    val earRX = rx * 0.14f
    val earRY = ry * 0.18f

    // Left ear
    val leftEarX = cx - rx * 0.96f
    drawOval(
        color = skin,
        topLeft = Offset(leftEarX - earRX, earY - earRY),
        size = Size(earRX * 2, earRY * 2)
    )
    // Inner ear shadow
    drawOval(
        color = skin.darken(0.12f),
        topLeft = Offset(leftEarX - earRX * 0.55f, earY - earRY * 0.6f),
        size = Size(earRX * 1.1f, earRY * 1.2f)
    )
    // Inner ear pink highlight
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x25FF9999), Color.Transparent),
            center = Offset(leftEarX, earY),
            radius = earRX * 0.8f
        ),
        topLeft = Offset(leftEarX - earRX * 0.4f, earY - earRY * 0.4f),
        size = Size(earRX * 0.8f, earRY * 0.8f)
    )

    // Right ear (mirrored)
    val rightEarX = cx + rx * 0.96f
    drawOval(
        color = skin,
        topLeft = Offset(rightEarX - earRX, earY - earRY),
        size = Size(earRX * 2, earRY * 2)
    )
    drawOval(
        color = skin.darken(0.12f),
        topLeft = Offset(rightEarX - earRX * 0.55f, earY - earRY * 0.6f),
        size = Size(earRX * 1.1f, earRY * 1.2f)
    )
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x25FF9999), Color.Transparent),
            center = Offset(rightEarX, earY),
            radius = earRX * 0.8f
        ),
        topLeft = Offset(rightEarX - earRX * 0.4f, earY - earRY * 0.4f),
        size = Size(earRX * 0.8f, earRY * 0.8f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Eyes — detailed iris, pupil, catchlights, lashes
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawEyes(
    cx: Float, cy: Float, rx: Float, ry: Float,
    irisColor: Color, eyeShape: String?, gender: AvatarGender
) {
    val eyeSpacing = rx * 0.33f
    // Girls' eyes are slightly larger and placed a touch lower for cute look
    val eyeY = cy + ry * (if (gender == AvatarGender.GIRL) 0.0f else 0.02f)

    drawSingleEye(cx - eyeSpacing, eyeY, rx, ry, irisColor, eyeShape, gender, isLeft = true)
    drawSingleEye(cx + eyeSpacing, eyeY, rx, ry, irisColor, eyeShape, gender, isLeft = false)
}

private fun DrawScope.drawSingleEye(
    ex: Float, ey: Float, headRX: Float, headRY: Float,
    irisColor: Color, eyeShape: String?, gender: AvatarGender,
    isLeft: Boolean
) {
    val shape = eyeShape ?: "round"

    // Girls get bigger, rounder eyes for a cuter look
    val genderScale = if (gender == AvatarGender.GIRL) 1.12f else 1.0f

    // Eye dimensions vary by shape
    val (eyeW, eyeH) = when (shape) {
        "round" -> (headRX * 0.24f * genderScale) to (headRY * 0.23f * genderScale)
        "cat" -> (headRX * 0.23f * genderScale) to (headRY * 0.18f * genderScale)
        "wide" -> (headRX * 0.26f * genderScale) to (headRY * 0.25f * genderScale)
        "narrow" -> (headRX * 0.23f * genderScale) to (headRY * 0.15f * genderScale)
        "downturned" -> (headRX * 0.23f * genderScale) to (headRY * 0.19f * genderScale)
        else -> (headRX * 0.22f * genderScale) to (headRY * 0.19f * genderScale) // almond
    }

    // White sclera path (shape-dependent)
    val scleraPath = buildEyeScleraPath(ex, ey, eyeW, eyeH, shape, isLeft)

    // 1. Sclera fill — off-white with subtle blue
    drawPath(scleraPath, Color(0xFFFBFBFF))

    // 2. Upper sclera shadow (eyelid shadow)
    clipPath(scleraPath) {
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x18000000), Color.Transparent),
                startY = ey - eyeH,
                endY = ey - eyeH * 0.2f
            ),
            topLeft = Offset(ex - eyeW, ey - eyeH),
            size = Size(eyeW * 2, eyeH * 2)
        )
    }

    // Iris dimensions
    val irisR = when (shape) {
        "round" -> eyeH * 0.68f
        "wide" -> eyeH * 0.62f
        "narrow" -> eyeH * 0.78f
        else -> eyeH * 0.70f
    }
    val irisY = ey + eyeH * 0.05f

    // 3. Iris — outer dark ring
    drawCircle(color = irisColor.darken(0.35f), radius = irisR, center = Offset(ex, irisY))

    // 4. Iris — main color with radial gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(irisColor.lighten(0.15f), irisColor, irisColor.darken(0.2f)),
            center = Offset(ex, irisY - irisR * 0.15f),
            radius = irisR * 0.9f
        ),
        radius = irisR * 0.88f,
        center = Offset(ex, irisY)
    )

    // 5. Iris — radial pattern lines
    for (angle in 0 until 360 step 20) {
        val rad = Math.toRadians(angle.toDouble())
        val innerR = irisR * 0.3f
        val outerR = irisR * 0.82f
        drawLine(
            color = irisColor.darken(0.12f).copy(alpha = 0.3f),
            start = Offset(
                ex + cos(rad).toFloat() * innerR,
                irisY + sin(rad).toFloat() * innerR
            ),
            end = Offset(
                ex + cos(rad).toFloat() * outerR,
                irisY + sin(rad).toFloat() * outerR
            ),
            strokeWidth = irisR * 0.02f
        )
    }

    // 6. Pupil
    val pupilR = irisR * 0.42f
    drawCircle(color = Color(0xFF0A0A0A), radius = pupilR, center = Offset(ex, irisY))

    // 7. Pupil gradient edge (subtle)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A1A2E)),
            center = Offset(ex, irisY),
            radius = pupilR
        ),
        radius = pupilR,
        center = Offset(ex, irisY)
    )

    // 8. Main catchlight — large, upper-right
    val catchX = ex + irisR * 0.28f
    val catchY = irisY - irisR * 0.32f
    drawOval(
        color = Color.White.copy(alpha = 0.92f),
        topLeft = Offset(catchX - irisR * 0.22f, catchY - irisR * 0.20f),
        size = Size(irisR * 0.38f, irisR * 0.32f)
    )

    // 9. Secondary catchlight — small, lower-left
    drawCircle(
        color = Color.White.copy(alpha = 0.65f),
        radius = irisR * 0.10f,
        center = Offset(ex - irisR * 0.22f, irisY + irisR * 0.28f)
    )

    // 10. Upper eyelid line
    val lidPath = buildUpperEyelidPath(ex, ey, eyeW, eyeH, shape, isLeft)
    drawPath(
        lidPath,
        color = Color(0xFF3A2A1A),
        style = Stroke(width = headRX * 0.025f, cap = StrokeCap.Round)
    )

    // 11. Eyelashes
    if (gender == AvatarGender.GIRL) {
        drawGirlLashes(ex, ey, eyeW, eyeH, isLeft)
    } else {
        // Boys get subtle tiny corner lashes
        drawBoyLashes(ex, ey, eyeW, eyeH, isLeft)
    }

    // 12. Lower eyelid hint
    val lowerLidY = ey + eyeH * 0.85f
    drawArc(
        color = Color(0xFF3A2A1A).copy(alpha = 0.2f),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(ex - eyeW * 0.7f, lowerLidY - eyeH * 0.3f),
        size = Size(eyeW * 1.4f, eyeH * 0.5f),
        style = Stroke(width = headRX * 0.012f, cap = StrokeCap.Round)
    )
}

private fun buildEyeScleraPath(
    ex: Float, ey: Float, eyeW: Float, eyeH: Float,
    shape: String, isLeft: Boolean
): Path = Path().apply {
    when (shape) {
        "round" -> {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    ex - eyeW, ey - eyeH, ex + eyeW, ey + eyeH
                )
            )
        }
        "cat" -> {
            // Upturned outer corner
            val outerTilt = if (isLeft) -eyeH * 0.25f else -eyeH * 0.25f
            val innerX = if (isLeft) ex + eyeW else ex - eyeW
            val outerX = if (isLeft) ex - eyeW else ex + eyeW
            moveTo(innerX, ey)
            cubicTo(innerX, ey - eyeH * 0.8f, outerX, ey - eyeH * 0.8f + outerTilt, outerX, ey + outerTilt * 0.3f)
            cubicTo(outerX, ey + eyeH * 0.7f + outerTilt * 0.2f, innerX, ey + eyeH * 0.8f, innerX, ey)
            close()
        }
        "wide" -> {
            // Larger, rounder eye
            addOval(
                androidx.compose.ui.geometry.Rect(
                    ex - eyeW, ey - eyeH * 1.05f, ex + eyeW, ey + eyeH * 1.05f
                )
            )
        }
        "narrow" -> {
            // Squinted/sleepy
            moveTo(ex - eyeW, ey)
            cubicTo(ex - eyeW * 0.5f, ey - eyeH * 0.7f, ex + eyeW * 0.5f, ey - eyeH * 0.7f, ex + eyeW, ey)
            cubicTo(ex + eyeW * 0.5f, ey + eyeH * 0.6f, ex - eyeW * 0.5f, ey + eyeH * 0.6f, ex - eyeW, ey)
            close()
        }
        "downturned" -> {
            val innerX = if (isLeft) ex + eyeW else ex - eyeW
            val outerX = if (isLeft) ex - eyeW else ex + eyeW
            moveTo(innerX, ey - eyeH * 0.1f)
            cubicTo(innerX, ey - eyeH * 0.9f, outerX, ey - eyeH * 0.6f, outerX, ey + eyeH * 0.2f)
            cubicTo(outerX, ey + eyeH * 0.8f, innerX, ey + eyeH * 0.8f, innerX, ey - eyeH * 0.1f)
            close()
        }
        else -> {
            // Almond — elegant, slightly pointed at corners
            moveTo(ex - eyeW, ey)
            cubicTo(ex - eyeW * 0.6f, ey - eyeH * 0.95f, ex + eyeW * 0.6f, ey - eyeH * 0.95f, ex + eyeW, ey)
            cubicTo(ex + eyeW * 0.6f, ey + eyeH * 0.85f, ex - eyeW * 0.6f, ey + eyeH * 0.85f, ex - eyeW, ey)
            close()
        }
    }
}

private fun buildUpperEyelidPath(
    ex: Float, ey: Float, eyeW: Float, eyeH: Float,
    shape: String, isLeft: Boolean
): Path = Path().apply {
    when (shape) {
        "cat" -> {
            val outerTilt = -eyeH * 0.25f
            val innerX = if (isLeft) ex + eyeW else ex - eyeW
            val outerX = if (isLeft) ex - eyeW else ex + eyeW
            moveTo(innerX, ey - eyeH * 0.002f)
            cubicTo(innerX, ey - eyeH * 0.8f, outerX, ey - eyeH * 0.8f + outerTilt, outerX, ey + outerTilt * 0.3f)
        }
        "downturned" -> {
            val innerX = if (isLeft) ex + eyeW else ex - eyeW
            val outerX = if (isLeft) ex - eyeW else ex + eyeW
            moveTo(innerX, ey - eyeH * 0.1f)
            cubicTo(innerX, ey - eyeH * 0.9f, outerX, ey - eyeH * 0.6f, outerX, ey + eyeH * 0.2f)
        }
        "narrow" -> {
            moveTo(ex - eyeW, ey)
            cubicTo(ex - eyeW * 0.5f, ey - eyeH * 0.7f, ex + eyeW * 0.5f, ey - eyeH * 0.7f, ex + eyeW, ey)
        }
        else -> {
            // Round, wide, almond
            moveTo(ex - eyeW, ey)
            cubicTo(ex - eyeW * 0.5f, ey - eyeH * 0.95f, ex + eyeW * 0.5f, ey - eyeH * 0.95f, ex + eyeW, ey)
        }
    }
}

private fun DrawScope.drawGirlLashes(
    ex: Float, ey: Float, eyeW: Float, eyeH: Float, isLeft: Boolean
) {
    val lashColor = Color(0xFF2A1A0A)
    val lashLen = eyeH * 0.35f  // longer lashes for girls

    // Upper lashes — fan out from upper eyelid
    val lashCount = 6
    val startAngle = if (isLeft) 200.0 else 340.0
    val endAngle = if (isLeft) 320.0 else 220.0
    val step = (endAngle - startAngle) / (lashCount - 1)

    for (i in 0 until lashCount) {
        val angleDeg = startAngle + step * i
        val angleRad = Math.toRadians(angleDeg)
        val baseX = ex + cos(angleRad).toFloat() * eyeW * 0.92f
        val baseY = ey + sin(angleRad).toFloat() * eyeH * 0.85f
        val lengthMul = 0.6f + i * 0.12f
        val tipX = baseX + cos(angleRad).toFloat() * lashLen * lengthMul
        val tipY = baseY + sin(angleRad).toFloat() * lashLen * lengthMul
        drawLine(
            color = lashColor,
            start = Offset(baseX, baseY),
            end = Offset(tipX, tipY),
            strokeWidth = eyeW * 0.028f,
            cap = StrokeCap.Round
        )
    }

    // Lower lashes — subtle, 2-3 small ones at outer corner
    val lowerStartAngle = if (isLeft) 190.0 else 350.0
    val lowerEndAngle = if (isLeft) 220.0 else 320.0
    val lowerStep = (lowerEndAngle - lowerStartAngle) / 2.0
    for (i in 0..2) {
        val angleDeg = lowerStartAngle + lowerStep * i
        val angleRad = Math.toRadians(angleDeg)
        val baseX = ex + cos(angleRad).toFloat() * eyeW * 0.88f
        val baseY = ey + sin(angleRad).toFloat() * eyeH * 0.75f + eyeH * 0.3f
        val tipX = baseX + cos(angleRad).toFloat() * lashLen * 0.3f
        val tipY = baseY + lashLen * 0.2f
        drawLine(
            color = lashColor.copy(alpha = 0.5f),
            start = Offset(baseX, baseY),
            end = Offset(tipX, tipY),
            strokeWidth = eyeW * 0.018f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawBoyLashes(
    ex: Float, ey: Float, eyeW: Float, eyeH: Float, isLeft: Boolean
) {
    // Just subtle corner accents
    val outerX = if (isLeft) ex - eyeW * 0.85f else ex + eyeW * 0.85f
    val outerY = ey - eyeH * 0.4f
    val tipX = if (isLeft) outerX - eyeW * 0.08f else outerX + eyeW * 0.08f
    drawLine(
        color = Color(0xFF3A2A1A).copy(alpha = 0.5f),
        start = Offset(outerX, outerY),
        end = Offset(tipX, outerY - eyeH * 0.12f),
        strokeWidth = eyeW * 0.02f,
        cap = StrokeCap.Round
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Eyebrows
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawEyebrows(
    cx: Float, cy: Float, rx: Float, ry: Float,
    hairColor: Color, gender: AvatarGender, eyebrowStyle: String?
) {
    val browColor = hairColor.darken(0.15f)
    val browY = cy - ry * (if (gender == AvatarGender.GIRL) 0.24f else 0.20f) // girls higher
    val browW = rx * (if (gender == AvatarGender.GIRL) 0.26f else 0.30f) // girls narrower
    val style = eyebrowStyle ?: "natural"

    // Boys get thicker eyebrows across all styles
    val browThick = when (style) {
        "thick" -> if (gender == AvatarGender.BOY) ry * 0.065f else ry * 0.045f
        "thin" -> if (gender == AvatarGender.BOY) ry * 0.028f else ry * 0.018f
        else -> if (gender == AvatarGender.BOY) ry * 0.052f else ry * 0.028f
    }

    val archHeight = when (style) {
        "arched" -> ry * (if (gender == AvatarGender.GIRL) 0.11f else 0.08f) // girls higher arch
        "flat" -> ry * 0.02f
        "curved" -> ry * (if (gender == AvatarGender.GIRL) 0.09f else 0.07f)
        else -> ry * (if (gender == AvatarGender.GIRL) 0.07f else 0.05f) // natural
    }

    // Left eyebrow
    val leftBrowPath = Path().apply {
        moveTo(cx - rx * 0.33f - browW * 0.6f, browY + ry * 0.04f)
        cubicTo(
            cx - rx * 0.33f - browW * 0.2f, browY - archHeight,
            cx - rx * 0.33f + browW * 0.3f, browY - archHeight,
            cx - rx * 0.33f + browW * 0.7f, browY + ry * 0.02f
        )
    }
    drawPath(
        leftBrowPath,
        color = browColor,
        style = Stroke(width = browThick, cap = StrokeCap.Round)
    )

    // Right eyebrow (mirrored)
    val rightBrowPath = Path().apply {
        moveTo(cx + rx * 0.33f + browW * 0.6f, browY + ry * 0.04f)
        cubicTo(
            cx + rx * 0.33f + browW * 0.2f, browY - archHeight,
            cx + rx * 0.33f - browW * 0.3f, browY - archHeight,
            cx + rx * 0.33f - browW * 0.7f, browY + ry * 0.02f
        )
    }
    drawPath(
        rightBrowPath,
        color = browColor,
        style = Stroke(width = browThick, cap = StrokeCap.Round)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Nose — cute button style
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawNose(
    cx: Float, cy: Float, ry: Float,
    skin: Color, gender: AvatarGender
) {
    val noseY = cy + ry * 0.30f
    // Girls get a smaller, more button-like nose; boys get slightly wider
    val noseScale = if (gender == AvatarGender.GIRL) 0.75f else 1.0f
    val noseW = ry * 0.08f * noseScale
    val noseH = ry * 0.06f * noseScale

    // Subtle nose tip shadow — small rounded shape
    val nosePath = Path().apply {
        moveTo(cx - noseW, noseY)
        cubicTo(
            cx - noseW, noseY - noseH * 0.5f,
            cx - noseW * 0.3f, noseY - noseH,
            cx, noseY - noseH * 0.4f
        )
        cubicTo(
            cx + noseW * 0.3f, noseY - noseH,
            cx + noseW, noseY - noseH * 0.5f,
            cx + noseW, noseY
        )
        cubicTo(
            cx + noseW * 0.7f, noseY + noseH * 0.6f,
            cx - noseW * 0.7f, noseY + noseH * 0.6f,
            cx - noseW, noseY
        )
        close()
    }

    // Nose shadow
    drawPath(nosePath, skin.darken(0.15f))

    // Tiny highlight on tip
    drawCircle(
        color = skin.lighten(0.08f),
        radius = noseW * 0.35f,
        center = Offset(cx, noseY - noseH * 0.2f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Mouth — cute smile with optional teeth
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawMouth(
    cx: Float, cy: Float, ry: Float,
    skin: Color, gender: AvatarGender, mouthShape: String?
) {
    val shape = mouthShape ?: "smile"
    val mouthY = cy + ry * 0.52f
    // Girls get a slightly wider, more defined mouth
    val mouthW = ry * (if (gender == AvatarGender.GIRL) 0.20f else 0.17f)
    val mouthH = ry * (if (gender == AvatarGender.GIRL) 0.11f else 0.09f)

    // Lip color — girls get distinctly pinkish lips, boys get subtle skin-tone based
    val lipColor = if (gender == AvatarGender.GIRL) {
        Color(0xFFE8707E)
    } else {
        skin.darken(0.10f).copy(
            red = minOf(1f, skin.darken(0.10f).red + 0.05f)
        )
    }

    when (shape) {
        "grin" -> {
            // Big wide grin showing teeth
            val grinW = mouthW * 1.3f
            val grinH = mouthH * 1.3f
            val grinPath = Path().apply {
                moveTo(cx - grinW, mouthY - grinH * 0.1f)
                cubicTo(cx - grinW * 0.4f, mouthY - grinH * 0.5f, cx + grinW * 0.4f, mouthY - grinH * 0.5f, cx + grinW, mouthY - grinH * 0.1f)
                cubicTo(cx + grinW * 0.5f, mouthY + grinH, cx - grinW * 0.5f, mouthY + grinH, cx - grinW, mouthY - grinH * 0.1f)
                close()
            }
            drawPath(grinPath, Color(0xFF6B2A3A))
            clipPath(grinPath) {
                drawRect(Color(0xFFFAFAFA), Offset(cx - grinW * 0.6f, mouthY - grinH * 0.3f), Size(grinW * 1.2f, grinH * 0.4f))
            }
            drawPath(grinPath, lipColor, style = Stroke(width = ry * 0.015f))
        }
        "open" -> {
            // Open mouth, more circular
            val openPath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(cx - mouthW * 0.6f, mouthY - mouthH * 0.5f, cx + mouthW * 0.6f, mouthY + mouthH * 0.7f))
            }
            drawPath(openPath, Color(0xFF6B2A3A))
            clipPath(openPath) {
                drawRect(Color(0xFFFAFAFA), Offset(cx - mouthW * 0.4f, mouthY - mouthH * 0.3f), Size(mouthW * 0.8f, mouthH * 0.35f))
                // Tongue hint
                drawOval(Color(0xFFE57373), Offset(cx - mouthW * 0.25f, mouthY + mouthH * 0.1f), Size(mouthW * 0.5f, mouthH * 0.4f))
            }
        }
        "smirk" -> {
            // Asymmetric smile
            val smirkPath = Path().apply {
                moveTo(cx - mouthW * 0.8f, mouthY)
                cubicTo(cx - mouthW * 0.3f, mouthY - mouthH * 0.1f, cx + mouthW * 0.3f, mouthY - mouthH * 0.4f, cx + mouthW, mouthY - mouthH * 0.3f)
            }
            drawPath(smirkPath, lipColor, style = Stroke(width = ry * 0.02f, cap = StrokeCap.Round))
        }
        "pout" -> {
            // Pouty lips
            // Upper lip
            val upperLip = Path().apply {
                moveTo(cx - mouthW * 0.6f, mouthY)
                cubicTo(cx - mouthW * 0.3f, mouthY - mouthH * 0.5f, cx + mouthW * 0.3f, mouthY - mouthH * 0.5f, cx + mouthW * 0.6f, mouthY)
            }
            // Lower lip
            val lowerLip = Path().apply {
                moveTo(cx - mouthW * 0.6f, mouthY)
                cubicTo(cx - mouthW * 0.3f, mouthY + mouthH * 0.7f, cx + mouthW * 0.3f, mouthY + mouthH * 0.7f, cx + mouthW * 0.6f, mouthY)
            }
            drawPath(upperLip, lipColor, style = Stroke(width = ry * 0.02f, cap = StrokeCap.Round))
            drawPath(lowerLip, lipColor.darken(0.08f), style = Stroke(width = ry * 0.025f, cap = StrokeCap.Round))
        }
        "laugh" -> {
            // Big laughing mouth, wide open
            val laughW = mouthW * 1.4f
            val laughH = mouthH * 1.5f
            val laughPath = Path().apply {
                moveTo(cx - laughW, mouthY)
                cubicTo(cx - laughW * 0.3f, mouthY - laughH * 0.3f, cx + laughW * 0.3f, mouthY - laughH * 0.3f, cx + laughW, mouthY)
                cubicTo(cx + laughW * 0.5f, mouthY + laughH, cx - laughW * 0.5f, mouthY + laughH, cx - laughW, mouthY)
                close()
            }
            drawPath(laughPath, Color(0xFF6B2A3A))
            clipPath(laughPath) {
                drawRect(Color(0xFFFAFAFA), Offset(cx - laughW * 0.55f, mouthY - laughH * 0.15f), Size(laughW * 1.1f, laughH * 0.35f))
                drawOval(Color(0xFFE57373), Offset(cx - laughW * 0.3f, mouthY + laughH * 0.2f), Size(laughW * 0.6f, laughH * 0.5f))
            }
            drawPath(laughPath, lipColor, style = Stroke(width = ry * 0.012f))
        }
        else -> {
            // Default smile
            val smilePath = Path().apply {
                moveTo(cx - mouthW, mouthY - mouthH * 0.1f)
                cubicTo(cx - mouthW * 0.5f, mouthY - mouthH * 0.4f, cx + mouthW * 0.5f, mouthY - mouthH * 0.4f, cx + mouthW, mouthY - mouthH * 0.1f)
                cubicTo(cx + mouthW * 0.6f, mouthY + mouthH * 0.8f, cx - mouthW * 0.6f, mouthY + mouthH * 0.8f, cx - mouthW, mouthY - mouthH * 0.1f)
                close()
            }
            drawPath(smilePath, Color(0xFF6B2A3A))
            clipPath(smilePath) {
                drawPath(Path().apply {
                    moveTo(cx - mouthW * 0.5f, mouthY - mouthH * 0.15f)
                    lineTo(cx + mouthW * 0.5f, mouthY - mouthH * 0.15f)
                    lineTo(cx + mouthW * 0.45f, mouthY + mouthH * 0.15f)
                    lineTo(cx - mouthW * 0.45f, mouthY + mouthH * 0.15f)
                    close()
                }, Color(0xFFFAFAFA))
                drawLine(Color(0x20000000), Offset(cx, mouthY - mouthH * 0.15f), Offset(cx, mouthY + mouthH * 0.15f), strokeWidth = 0.8f)
            }
            val upperLipPath = Path().apply {
                moveTo(cx - mouthW, mouthY - mouthH * 0.1f)
                cubicTo(cx - mouthW * 0.5f, mouthY - mouthH * 0.4f, cx + mouthW * 0.5f, mouthY - mouthH * 0.4f, cx + mouthW, mouthY - mouthH * 0.1f)
            }
            drawPath(upperLipPath, color = lipColor, style = Stroke(width = ry * 0.018f, cap = StrokeCap.Round))
        }
    }

    // Lower lip highlight (for all shapes except smirk)
    if (shape != "smirk") {
        drawArc(
            brush = Brush.radialGradient(
                colors = listOf(lipColor.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(cx, mouthY + mouthH * 0.4f),
                radius = mouthW * 0.6f
            ),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - mouthW * 0.5f, mouthY + mouthH * 0.1f),
            size = Size(mouthW, mouthH * 0.6f),
            style = Stroke(width = ry * 0.02f, cap = StrokeCap.Round)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Face Details (freckles, blush, dimples, beauty mark, etc.)
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawFaceDetails(
    detailItem: AvatarLayerItem?, cx: Float, cy: Float,
    rx: Float, ry: Float, skin: Color
) {
    val detailId = detailItem?.id ?: return

    when {
        detailId.contains("freckles", true) -> {
            val freckleColor = skin.darken(0.22f).copy(alpha = 0.55f)
            val freckleR = rx * 0.018f
            // Left cheek freckles
            val leftCheekX = cx - rx * 0.42f
            val leftCheekY = cy + ry * 0.15f
            val leftPositions = listOf(
                -0.04f to -0.03f, 0.02f to -0.05f, 0.06f to -0.01f,
                -0.02f to 0.02f, 0.04f to 0.03f, -0.05f to 0.01f,
                0.01f to 0.05f
            )
            leftPositions.forEach { (dx, dy) ->
                drawCircle(
                    freckleColor,
                    freckleR * (0.8f + ((dx * 100).toInt() % 5) * 0.1f),
                    Offset(leftCheekX + rx * dx, leftCheekY + ry * dy)
                )
            }
            // Right cheek freckles (mirrored)
            val rightCheekX = cx + rx * 0.42f
            leftPositions.forEach { (dx, dy) ->
                drawCircle(
                    freckleColor,
                    freckleR * (0.8f + ((dx * 100).toInt() % 5) * 0.1f),
                    Offset(rightCheekX - rx * dx, leftCheekY + ry * dy)
                )
            }
        }

        detailId.contains("blush", true) -> {
            // Rosy cheek blush — two warm pink radial circles
            val blushColor = Color(0xFFFF8A9E)
            val blushY = cy + ry * 0.18f
            val blushR = rx * 0.18f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(blushColor.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(cx - rx * 0.48f, blushY),
                    radius = blushR
                ),
                radius = blushR,
                center = Offset(cx - rx * 0.48f, blushY)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(blushColor.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(cx + rx * 0.48f, blushY),
                    radius = blushR
                ),
                radius = blushR,
                center = Offset(cx + rx * 0.48f, blushY)
            )
        }

        detailId.contains("dimple", true) -> {
            val dimpleColor = skin.darken(0.12f)
            val dimpleY = cy + ry * 0.42f
            // Left dimple
            drawArc(
                color = dimpleColor,
                startAngle = 30f, sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(cx - rx * 0.42f, dimpleY - ry * 0.03f),
                size = Size(rx * 0.08f, ry * 0.06f),
                style = Stroke(width = rx * 0.015f, cap = StrokeCap.Round)
            )
            // Right dimple
            drawArc(
                color = dimpleColor,
                startAngle = 30f, sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(cx + rx * 0.34f, dimpleY - ry * 0.03f),
                size = Size(rx * 0.08f, ry * 0.06f),
                style = Stroke(width = rx * 0.015f, cap = StrokeCap.Round)
            )
        }

        detailId.contains("beauty", true) -> {
            drawCircle(
                color = skin.darken(0.35f),
                radius = rx * 0.022f,
                center = Offset(cx + rx * 0.38f, cy + ry * 0.32f)
            )
        }

        detailId.contains("smile_lines", true) || detailId.contains("laugh", true) -> {
            val lineColor = skin.darken(0.10f)
            // Nose-to-mouth lines
            val lineStart = cy + ry * 0.18f
            val lineEnd = cy + ry * 0.48f
            drawArc(
                color = lineColor,
                startAngle = 10f, sweepAngle = 60f,
                useCenter = false,
                topLeft = Offset(cx - rx * 0.32f, lineStart),
                size = Size(rx * 0.15f, lineEnd - lineStart),
                style = Stroke(width = rx * 0.01f, cap = StrokeCap.Round)
            )
            drawArc(
                color = lineColor,
                startAngle = 110f, sweepAngle = 60f,
                useCenter = false,
                topLeft = Offset(cx + rx * 0.17f, lineStart),
                size = Size(rx * 0.15f, lineEnd - lineStart),
                style = Stroke(width = rx * 0.01f, cap = StrokeCap.Round)
            )
        }

        detailId.contains("chin_cleft", true) -> {
            val cleftY = cy + ry * 0.78f
            drawLine(
                color = skin.darken(0.12f),
                start = Offset(cx, cleftY - ry * 0.04f),
                end = Offset(cx, cleftY + ry * 0.04f),
                strokeWidth = rx * 0.015f,
                cap = StrokeCap.Round
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(skin.darken(0.08f), Color.Transparent),
                    center = Offset(cx, cleftY),
                    radius = rx * 0.04f
                ),
                radius = rx * 0.04f,
                center = Offset(cx, cleftY)
            )
        }

        detailId.contains("sparkle_cheeks", true) -> {
            val sparkleColor = Color(0xFFFFD700)
            val cheekY = cy + ry * 0.2f
            listOf(cx - rx * 0.5f, cx + rx * 0.5f).forEach { sparkleX ->
                for (i in 0..2) {
                    val dx = (i - 1) * rx * 0.05f
                    val dy = (i % 2) * ry * 0.03f
                    drawCircle(
                        color = sparkleColor.copy(alpha = 0.5f),
                        radius = rx * 0.015f,
                        center = Offset(sparkleX + dx, cheekY + dy)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Neck & Shoulders
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawNeckAndShoulders(
    cx: Float, neckTopY: Float, neckW: Float,
    shoulderY: Float, shoulderW: Float, canvasH: Float,
    skin: Color, gender: AvatarGender
) {
    // Girls: narrower shoulders, slimmer neck, rounder curves
    // Boys: broader shoulders, thicker neck, more angular
    val shoulderRound = if (gender == AvatarGender.GIRL) 0.88f else 0.95f
    val shoulderCurve = if (gender == AvatarGender.GIRL) 0.45f else 0.62f

    // Shoulder roundness
    val shoulderPath = Path().apply {
        moveTo(cx - neckW, neckTopY)
        // Left shoulder curve
        cubicTo(
            cx - neckW, shoulderY * shoulderRound,
            cx - shoulderW * shoulderCurve, shoulderY * 0.92f,
            cx - shoulderW, shoulderY
        )
        // Left body
        lineTo(cx - shoulderW * 1.05f, canvasH)
        // Bottom
        lineTo(cx + shoulderW * 1.05f, canvasH)
        // Right body
        lineTo(cx + shoulderW, shoulderY)
        // Right shoulder curve
        cubicTo(
            cx + shoulderW * shoulderCurve, shoulderY * 0.92f,
            cx + neckW, shoulderY * shoulderRound,
            cx + neckW, neckTopY
        )
        close()
    }

    // Base skin
    drawPath(shoulderPath, skin)

    // 3D depth gradient on body
    drawPath(
        shoulderPath,
        Brush.verticalGradient(
            colors = listOf(skin.darken(0.05f), skin, skin.darken(0.08f)),
            startY = neckTopY,
            endY = canvasH
        )
    )

    // Neck shadow under jaw
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(skin.darken(0.15f), Color.Transparent),
            center = Offset(cx, neckTopY),
            radius = neckW * 1.5f
        ),
        topLeft = Offset(cx - neckW * 1.2f, neckTopY - neckW * 0.3f),
        size = Size(neckW * 2.4f, neckW * 1.2f)
    )

    // Shoulder highlights
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(skin.lighten(0.06f), Color.Transparent)
        ),
        radius = shoulderW * 0.25f,
        center = Offset(cx - shoulderW * 0.5f, shoulderY + shoulderW * 0.1f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(skin.lighten(0.06f), Color.Transparent)
        ),
        radius = shoulderW * 0.25f,
        center = Offset(cx + shoulderW * 0.5f, shoulderY + shoulderW * 0.1f)
    )
}


// ─────────────────────────────────────────────────────────────────────────────
//  Hair — Back Layer (behind head)
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawHairBack(
    hairItem: AvatarLayerItem?, cx: Float, cy: Float,
    rx: Float, ry: Float, hairColor: Color, gender: AvatarGender
) {
    if (hairItem == null) return
    val hairId = hairItem.id

    val darkHair = hairColor.darken(0.15f)

    when {
        hairId.contains("ponytail", true) -> {
            // Ponytail flowing behind
            val ponytailPath = Path().apply {
                moveTo(cx + rx * 0.05f, cy - ry * 0.4f)
                cubicTo(
                    cx + rx * 0.5f, cy - ry * 0.2f,
                    cx + rx * 0.7f, cy + ry * 0.3f,
                    cx + rx * 0.4f, cy + ry * 0.85f
                )
                cubicTo(
                    cx + rx * 0.2f, cy + ry * 1.0f,
                    cx - rx * 0.1f, cy + ry * 0.95f,
                    cx - rx * 0.05f, cy + ry * 0.8f
                )
                cubicTo(
                    cx, cy + ry * 0.5f,
                    cx + rx * 0.3f, cy + ry * 0.1f,
                    cx + rx * 0.05f, cy - ry * 0.4f
                )
                close()
            }
            drawPath(ponytailPath, darkHair)
            // Highlight streak
            val highlightPath = Path().apply {
                moveTo(cx + rx * 0.15f, cy - ry * 0.1f)
                cubicTo(
                    cx + rx * 0.45f, cy + ry * 0.2f,
                    cx + rx * 0.35f, cy + ry * 0.7f,
                    cx + rx * 0.2f, cy + ry * 0.85f
                )
            }
            drawPath(highlightPath, hairColor.lighten(0.15f).copy(alpha = 0.3f),
                style = Stroke(width = rx * 0.06f, cap = StrokeCap.Round))
        }

        hairId.contains("long", true) -> {
            // Long hair flowing behind shoulders
            val longPath = Path().apply {
                moveTo(cx - rx * 0.85f, cy - ry * 0.2f)
                cubicTo(
                    cx - rx * 1.0f, cy + ry * 0.4f,
                    cx - rx * 0.9f, cy + ry * 1.0f,
                    cx - rx * 0.6f, cy + ry * 1.0f
                )
                lineTo(cx + rx * 0.6f, cy + ry * 1.0f)
                cubicTo(
                    cx + rx * 0.9f, cy + ry * 1.0f,
                    cx + rx * 1.0f, cy + ry * 0.4f,
                    cx + rx * 0.85f, cy - ry * 0.2f
                )
                close()
            }
            drawPath(longPath, darkHair)
            // Highlight streaks
            for (i in -2..2) {
                val sx = cx + i * rx * 0.18f
                drawLine(
                    color = hairColor.lighten(0.12f).copy(alpha = 0.2f),
                    start = Offset(sx, cy),
                    end = Offset(sx + rx * 0.05f, cy + ry * 0.85f),
                    strokeWidth = rx * 0.04f,
                    cap = StrokeCap.Round
                )
            }
        }

        hairId.contains("pigtails", true) -> {
            // Two pigtails behind
            for (side in listOf(-1f, 1f)) {
                val pigX = cx + side * rx * 0.7f
                val pigPath = Path().apply {
                    moveTo(pigX - rx * 0.12f, cy - ry * 0.1f)
                    cubicTo(
                        pigX - rx * 0.2f * side, cy + ry * 0.3f,
                        pigX + rx * 0.1f * side, cy + ry * 0.8f,
                        pigX + rx * 0.05f * side, cy + ry * 0.85f
                    )
                    cubicTo(
                        pigX + rx * 0.15f * side, cy + ry * 0.9f,
                        pigX + rx * 0.25f, cy + ry * 1.0f,
                        pigX + rx * 0.15f, cy + ry * 0.5f
                    )
                    cubicTo(
                        pigX + rx * 0.1f, cy + ry * 0.2f,
                        pigX + rx * 0.18f, cy - ry * 0.05f,
                        pigX - rx * 0.12f, cy - ry * 0.1f
                    )
                    close()
                }
                drawPath(pigPath, darkHair)
            }
        }

        hairId.contains("bun", true) -> {
            // Bun on top/back of head
            val bunCY = cy - ry * 0.85f
            drawCircle(color = darkHair, radius = rx * 0.22f, center = Offset(cx, bunCY))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(hairColor.lighten(0.1f), Color.Transparent),
                    center = Offset(cx + rx * 0.05f, bunCY - rx * 0.05f),
                    radius = rx * 0.12f
                ),
                radius = rx * 0.18f,
                center = Offset(cx, bunCY)
            )
        }

        hairId.contains("wavy", true) -> {
            // Wavy hair behind
            val wavyPath = Path().apply {
                moveTo(cx - rx * 0.8f, cy - ry * 0.15f)
                cubicTo(
                    cx - rx * 0.95f, cy + ry * 0.3f,
                    cx - rx * 0.7f, cy + ry * 0.7f,
                    cx - rx * 0.8f, cy + ry * 0.85f
                )
                lineTo(cx + rx * 0.8f, cy + ry * 0.85f)
                cubicTo(
                    cx + rx * 0.7f, cy + ry * 0.7f,
                    cx + rx * 0.95f, cy + ry * 0.3f,
                    cx + rx * 0.8f, cy - ry * 0.15f
                )
                close()
            }
            drawPath(wavyPath, darkHair)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Hair — Front Layer (in front of face, bangs, etc.)
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawHairFront(
    hairItem: AvatarLayerItem?, cx: Float, cy: Float,
    rx: Float, ry: Float, hairColor: Color, gender: AvatarGender
) {
    if (hairItem == null) return
    val hairId = hairItem.id
    val darkHair = hairColor.darken(0.10f)
    val highlightHair = hairColor.lighten(0.18f)

    when {
        hairId.contains("buzz", true) -> drawBuzzCut(cx, cy, rx, ry, hairColor)
        hairId.contains("sidepart", true) -> drawSidePartHair(cx, cy, rx, ry, hairColor, highlightHair)
        hairId.contains("mohawk", true) -> drawMohawkHair(cx, cy, rx, ry, hairColor, highlightHair)
        hairId.contains("curly", true) -> drawCurlyHair(cx, cy, rx, ry, hairColor, highlightHair, gender)
        hairId.contains("pigtails", true) -> drawPigtailsFront(cx, cy, rx, ry, hairColor, highlightHair)
        hairId.contains("ponytail", true) -> drawPonytailFront(cx, cy, rx, ry, hairColor, highlightHair)
        hairId.contains("long", true) -> drawLongHairFront(cx, cy, rx, ry, hairColor, highlightHair)
        hairId.contains("wavy", true) -> drawWavyHairFront(cx, cy, rx, ry, hairColor, highlightHair)
        hairId.contains("bun", true) -> drawBunHairFront(cx, cy, rx, ry, hairColor, highlightHair)
        hairId.contains("bob", true) -> drawBobHair(cx, cy, rx, ry, hairColor, highlightHair)
        hairId.contains("short", true) -> drawShortHair(cx, cy, rx, ry, hairColor, highlightHair)
        else -> drawShortHair(cx, cy, rx, ry, hairColor, highlightHair)
    }
}

private fun DrawScope.drawBuzzCut(
    cx: Float, cy: Float, rx: Float, ry: Float, hairColor: Color
) {
    // Thin stubble cap over head — very short hair
    val capPath = Path().apply {
        moveTo(cx - rx * 0.85f, cy - ry * 0.1f)
        cubicTo(
            cx - rx * 0.9f, cy - ry * 0.5f,
            cx - rx * 0.7f, cy - ry * 0.95f,
            cx, cy - ry * 1.02f
        )
        cubicTo(
            cx + rx * 0.7f, cy - ry * 0.95f,
            cx + rx * 0.9f, cy - ry * 0.5f,
            cx + rx * 0.85f, cy - ry * 0.1f
        )
        // Lower edge following head shape
        cubicTo(
            cx + rx * 0.75f, cy - ry * 0.15f,
            cx + rx * 0.5f, cy - ry * 0.22f,
            cx, cy - ry * 0.25f
        )
        cubicTo(
            cx - rx * 0.5f, cy - ry * 0.22f,
            cx - rx * 0.75f, cy - ry * 0.15f,
            cx - rx * 0.85f, cy - ry * 0.1f
        )
        close()
    }
    drawPath(capPath, hairColor.copy(alpha = 0.55f))

    // Stubble texture dots
    val random = listOf(
        0.3f to 0.6f, 0.7f to 0.5f, 0.4f to 0.35f,
        0.6f to 0.4f, 0.5f to 0.7f, 0.35f to 0.5f,
        0.65f to 0.65f, 0.45f to 0.55f, 0.55f to 0.45f
    )
    random.forEach { (fx, fy) ->
        val sx = cx + (fx - 0.5f) * rx * 1.4f
        val sy = cy - ry * (0.2f + fy * 0.7f)
        drawCircle(
            color = hairColor.copy(alpha = 0.3f),
            radius = rx * 0.012f,
            center = Offset(sx, sy)
        )
    }
}

private fun DrawScope.drawShortHair(
    cx: Float, cy: Float, rx: Float, ry: Float,
    hairColor: Color, highlight: Color
) {
    // Classic short hair — voluminous cap over head with natural sides
    val mainPath = Path().apply {
        moveTo(cx - rx * 0.96f, cy + ry * 0.05f)
        // Left side flows past ear
        cubicTo(
            cx - rx * 1.0f, cy - ry * 0.45f,
            cx - rx * 0.75f, cy - ry * 1.05f,
            cx, cy - ry * 1.10f
        )
        // Top crown and right side
        cubicTo(
            cx + rx * 0.75f, cy - ry * 1.05f,
            cx + rx * 1.0f, cy - ry * 0.45f,
            cx + rx * 0.96f, cy + ry * 0.05f
        )
        // Natural hairline across forehead
        cubicTo(
            cx + rx * 0.82f, cy - ry * 0.02f,
            cx + rx * 0.45f, cy - ry * 0.18f,
            cx + rx * 0.1f, cy - ry * 0.24f
        )
        cubicTo(
            cx - rx * 0.15f, cy - ry * 0.22f,
            cx - rx * 0.55f, cy - ry * 0.12f,
            cx - rx * 0.82f, cy + ry * 0.0f
        )
        close()
    }

    // Base hair
    drawPath(mainPath, hairColor)

    // 3D gradient — top-lit dome
    drawPath(
        mainPath,
        Brush.radialGradient(
            colors = listOf(highlight.copy(alpha = 0.10f), Color.Transparent, hairColor.darken(0.12f)),
            center = Offset(cx - rx * 0.05f, cy - ry * 0.65f),
            radius = rx * 1.3f
        )
    )

    // Highlight streak — sweeping across top
    val streakPath = Path().apply {
        moveTo(cx - rx * 0.2f, cy - ry * 1.0f)
        cubicTo(
            cx + rx * 0.05f, cy - ry * 0.85f,
            cx + rx * 0.2f, cy - ry * 0.6f,
            cx + rx * 0.12f, cy - ry * 0.25f
        )
    }
    drawPath(streakPath, highlight.copy(alpha = 0.25f),
        style = Stroke(width = rx * 0.09f, cap = StrokeCap.Round))

    // Secondary thin highlight
    val streak2 = Path().apply {
        moveTo(cx - rx * 0.4f, cy - ry * 0.95f)
        cubicTo(
            cx - rx * 0.25f, cy - ry * 0.8f,
            cx - rx * 0.15f, cy - ry * 0.5f,
            cx - rx * 0.2f, cy - ry * 0.2f
        )
    }
    drawPath(streak2, highlight.copy(alpha = 0.15f),
        style = Stroke(width = rx * 0.05f, cap = StrokeCap.Round))
}

private fun DrawScope.drawSidePartHair(
    cx: Float, cy: Float, rx: Float, ry: Float,
    hairColor: Color, highlight: Color
) {
    // Side-parted hair — parting on left, volume swept right
    val mainPath = Path().apply {
        moveTo(cx - rx * 0.94f, cy + ry * 0.08f)
        cubicTo(
            cx - rx * 0.98f, cy - ry * 0.45f,
            cx - rx * 0.65f, cy - ry * 1.06f,
            cx, cy - ry * 1.10f
        )
        cubicTo(
            cx + rx * 0.68f, cy - ry * 1.06f,
            cx + rx * 1.05f, cy - ry * 0.45f,
            cx + rx * 0.98f, cy + ry * 0.08f
        )
        // Swooping bangs line — dramatic sweep to right
        cubicTo(
            cx + rx * 0.88f, cy - ry * 0.02f,
            cx + rx * 0.35f, cy - ry * 0.12f,
            cx - rx * 0.08f, cy - ry * 0.35f
        )
        // Part line — sharp angle back
        lineTo(cx - rx * 0.22f, cy - ry * 0.72f)
        cubicTo(
            cx - rx * 0.48f, cy - ry * 0.48f,
            cx - rx * 0.72f, cy - ry * 0.12f,
            cx - rx * 0.94f, cy + ry * 0.08f
        )
        close()
    }

    drawPath(mainPath, hairColor)

    // Swept volume on right side (extra volume, bigger)
    val volumePath = Path().apply {
        moveTo(cx + rx * 0.28f, cy - ry * 0.92f)
        cubicTo(
            cx + rx * 0.78f, cy - ry * 1.12f,
            cx + rx * 1.12f, cy - ry * 0.55f,
            cx + rx * 0.98f, cy + ry * 0.08f
        )
        cubicTo(
            cx + rx * 0.82f, cy - ry * 0.08f,
            cx + rx * 0.52f, cy - ry * 0.25f,
            cx + rx * 0.28f, cy - ry * 0.92f
        )
        close()
    }
    drawPath(volumePath, hairColor.darken(0.06f))

    // Highlight — following the sweep direction
    val streakPath = Path().apply {
        moveTo(cx + rx * 0.18f, cy - ry * 0.88f)
        cubicTo(
            cx + rx * 0.48f, cy - ry * 0.72f,
            cx + rx * 0.62f, cy - ry * 0.4f,
            cx + rx * 0.52f, cy - ry * 0.08f
        )
    }
    drawPath(streakPath, highlight.copy(alpha = 0.28f),
        style = Stroke(width = rx * 0.08f, cap = StrokeCap.Round))

    // Secondary highlight
    val streak2 = Path().apply {
        moveTo(cx + rx * 0.4f, cy - ry * 0.95f)
        cubicTo(
            cx + rx * 0.6f, cy - ry * 0.75f,
            cx + rx * 0.72f, cy - ry * 0.45f,
            cx + rx * 0.65f, cy - ry * 0.1f
        )
    }
    drawPath(streak2, highlight.copy(alpha = 0.15f),
        style = Stroke(width = rx * 0.05f, cap = StrokeCap.Round))

    // Part line — visible
    drawLine(
        color = hairColor.darken(0.22f),
        start = Offset(cx - rx * 0.18f, cy - ry * 0.42f),
        end = Offset(cx - rx * 0.26f, cy - ry * 0.92f),
        strokeWidth = rx * 0.018f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawMohawkHair(
    cx: Float, cy: Float, rx: Float, ry: Float,
    hairColor: Color, highlight: Color
) {
    // Faded sides
    val fadePath = Path().apply {
        moveTo(cx - rx * 0.85f, cy + ry * 0.0f)
        cubicTo(
            cx - rx * 0.9f, cy - ry * 0.4f,
            cx - rx * 0.65f, cy - ry * 0.85f,
            cx - rx * 0.3f, cy - ry * 0.95f
        )
        lineTo(cx + rx * 0.3f, cy - ry * 0.95f)
        cubicTo(
            cx + rx * 0.65f, cy - ry * 0.85f,
            cx + rx * 0.9f, cy - ry * 0.4f,
            cx + rx * 0.85f, cy + ry * 0.0f
        )
        cubicTo(cx + rx * 0.6f, cy - ry * 0.1f, cx - rx * 0.6f, cy - ry * 0.1f, cx - rx * 0.85f, cy + ry * 0.0f)
        close()
    }
    drawPath(fadePath, hairColor.copy(alpha = 0.30f))

    // Central ridge — tall mohawk
    val ridgePath = Path().apply {
        moveTo(cx - rx * 0.15f, cy - ry * 0.1f)
        cubicTo(
            cx - rx * 0.2f, cy - ry * 0.6f,
            cx - rx * 0.15f, cy - ry * 1.2f,
            cx, cy - ry * 1.35f
        )
        cubicTo(
            cx + rx * 0.15f, cy - ry * 1.2f,
            cx + rx * 0.2f, cy - ry * 0.6f,
            cx + rx * 0.15f, cy - ry * 0.1f
        )
        close()
    }
    drawPath(ridgePath, hairColor)

    // Ridge gradient for depth
    drawPath(
        ridgePath,
        Brush.horizontalGradient(
            colors = listOf(hairColor.darken(0.15f), hairColor, hairColor.darken(0.15f)),
            startX = cx - rx * 0.15f,
            endX = cx + rx * 0.15f
        )
    )

    // Highlight stripe
    drawLine(
        color = highlight.copy(alpha = 0.3f),
        start = Offset(cx, cy - ry * 0.15f),
        end = Offset(cx, cy - ry * 1.25f),
        strokeWidth = rx * 0.06f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawCurlyHair(
    cx: Float, cy: Float, rx: Float, ry: Float,
    hairColor: Color, highlight: Color, gender: AvatarGender
) {
    // Voluminous curly hair — lots of circular puffs with more volume
    val volume = if (gender == AvatarGender.GIRL) 1.08f else 1.0f
    val mainPath = Path().apply {
        moveTo(cx - rx * 0.96f * volume, cy + ry * 0.15f)
        cubicTo(
            cx - rx * 1.08f * volume, cy - ry * 0.45f,
            cx - rx * 0.78f * volume, cy - ry * 1.18f,
            cx, cy - ry * 1.22f * volume
        )
        cubicTo(
            cx + rx * 0.78f * volume, cy - ry * 1.18f,
            cx + rx * 1.08f * volume, cy - ry * 0.45f,
            cx + rx * 0.96f * volume, cy + ry * 0.15f
        )
        // Lower edge with more defined curl bumps
        cubicTo(cx + rx * 0.88f, cy + ry * 0.0f, cx + rx * 0.6f, cy - ry * 0.12f, cx + rx * 0.35f, cy - ry * 0.08f)
        cubicTo(cx + rx * 0.2f, cy - ry * 0.14f, cx - rx * 0.2f, cy - ry * 0.10f, cx - rx * 0.35f, cy - ry * 0.12f)
        cubicTo(cx - rx * 0.6f, cy - ry * 0.08f, cx - rx * 0.88f, cy + ry * 0.0f, cx - rx * 0.96f * volume, cy + ry * 0.15f)
        close()
    }
    drawPath(mainPath, hairColor)

    // Curl texture — circular curl highlights with depth
    val curlPositions = listOf(
        -0.5f to -0.72f, 0.0f to -0.92f, 0.45f to -0.74f,
        -0.65f to -0.42f, 0.6f to -0.44f, -0.3f to -0.88f,
        0.25f to -0.90f, -0.15f to -0.52f, 0.15f to -0.56f,
        -0.72f to -0.18f, 0.68f to -0.20f, -0.45f to -0.58f,
        0.42f to -0.60f, 0.0f to -0.65f
    )
    curlPositions.forEach { (dx, dy) ->
        val curlX = cx + dx * rx * volume
        val curlY = cy + dy * ry
        val curlR = rx * 0.11f

        // Dark ring around each curl
        drawCircle(
            color = hairColor.darken(0.08f).copy(alpha = 0.2f),
            radius = curlR,
            center = Offset(curlX, curlY),
            style = Stroke(width = rx * 0.015f)
        )

        // Highlight in center of curl
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(highlight.copy(alpha = 0.20f), Color.Transparent),
                center = Offset(curlX - rx * 0.02f, curlY - ry * 0.02f),
                radius = curlR * 0.7f
            ),
            radius = curlR * 0.7f,
            center = Offset(curlX, curlY)
        )
    }

    // Main dome highlight
    drawPath(
        mainPath,
        Brush.radialGradient(
            colors = listOf(highlight.copy(alpha = 0.14f), Color.Transparent),
            center = Offset(cx + rx * 0.08f, cy - ry * 0.65f),
            radius = rx * 0.65f
        )
    )
}

private fun DrawScope.drawPigtailsFront(
    cx: Float, cy: Float, rx: Float, ry: Float,
    hairColor: Color, highlight: Color
) {
    // Front bangs cap — cute and full
    val bangsPath = Path().apply {
        moveTo(cx - rx * 0.92f, cy + ry * 0.0f)
        cubicTo(
            cx - rx * 0.96f, cy - ry * 0.5f,
            cx - rx * 0.68f, cy - ry * 1.02f,
            cx, cy - ry * 1.08f
        )
        cubicTo(
            cx + rx * 0.68f, cy - ry * 1.02f,
            cx + rx * 0.96f, cy - ry * 0.5f,
            cx + rx * 0.92f, cy + ry * 0.0f
        )
        // Straight-cut bangs with slight unevenness for natural look
        cubicTo(cx + rx * 0.6f, cy - ry * 0.1f, cx + rx * 0.3f, cy - ry * 0.2f, cx + rx * 0.05f, cy - ry * 0.22f)
        cubicTo(cx - rx * 0.15f, cy - ry * 0.24f, cx - rx * 0.45f, cy - ry * 0.18f, cx - rx * 0.6f, cy - ry * 0.12f)
        close()
    }
    drawPath(bangsPath, hairColor)

    // Hair ties (cute colored circles at pigtail bases)
    for (side in listOf(-1f, 1f)) {
        val tieX = cx + side * rx * 0.72f
        val tieY = cy - ry * 0.12f
        // Shadow ring
        drawCircle(
            color = Color(0xFFE5507A),
            radius = rx * 0.065f,
            center = Offset(tieX, tieY)
        )
        // Main tie
        drawCircle(
            color = Color(0xFFFF6B8A),
            radius = rx * 0.055f,
            center = Offset(tieX, tieY)
        )
        // Shine on hair tie
        drawCircle(
            color = Color(0xFFFF8FAA),
            radius = rx * 0.025f,
            center = Offset(tieX - rx * 0.015f, tieY - rx * 0.02f)
        )
    }

    // Bangs highlight
    val streakPath = Path().apply {
        moveTo(cx - rx * 0.15f, cy - ry * 0.98f)
        cubicTo(cx, cy - ry * 0.75f, cx + rx * 0.12f, cy - ry * 0.5f, cx + rx * 0.08f, cy - ry * 0.25f)
    }
    drawPath(streakPath, highlight.copy(alpha = 0.25f),
        style = Stroke(width = rx * 0.07f, cap = StrokeCap.Round))

    // Bang texture lines
    for (i in -2..2) {
        val bx = cx + i * rx * 0.12f
        drawLine(
            color = hairColor.darken(0.08f).copy(alpha = 0.2f),
            start = Offset(bx, cy - ry * 0.85f),
            end = Offset(bx + rx * 0.02f, cy - ry * 0.2f),
            strokeWidth = rx * 0.008f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawPonytailFront(
    cx: Float, cy: Float, rx: Float, ry: Float,
    hairColor: Color, highlight: Color
) {
    // Front hair cap with smooth top, pulled back
    val frontPath = Path().apply {
        moveTo(cx - rx * 0.92f, cy + ry * 0.05f)
        cubicTo(
            cx - rx * 0.96f, cy - ry * 0.45f,
            cx - rx * 0.68f, cy - ry * 1.02f,
            cx, cy - ry * 1.08f
        )
        cubicTo(
            cx + rx * 0.68f, cy - ry * 1.02f,
            cx + rx * 0.96f, cy - ry * 0.45f,
            cx + rx * 0.92f, cy + ry * 0.05f
        )
        // Swept-back hairline
        cubicTo(cx + rx * 0.72f, cy - ry * 0.02f, cx + rx * 0.35f, cy - ry * 0.18f, cx, cy - ry * 0.26f)
        cubicTo(cx - rx * 0.35f, cy - ry * 0.18f, cx - rx * 0.72f, cy - ry * 0.02f, cx - rx * 0.92f, cy + ry * 0.05f)
        close()
    }
    drawPath(frontPath, hairColor)

    // Hair tie at back (higher, where ponytail originates)
    drawOval(
        color = Color(0xFFFF6B8A),
        topLeft = Offset(cx - rx * 0.06f, cy - ry * 0.55f),
        size = Size(rx * 0.12f, ry * 0.08f)
    )
    // Hair tie highlight
    drawOval(
        color = Color(0xFFFF8FAA),
        topLeft = Offset(cx - rx * 0.03f, cy - ry * 0.53f),
        size = Size(rx * 0.06f, ry * 0.04f)
    )

    // Top highlight — smooth pulled-back shine
    drawPath(
        frontPath,
        Brush.radialGradient(
            colors = listOf(highlight.copy(alpha = 0.18f), Color.Transparent),
            center = Offset(cx + rx * 0.08f, cy - ry * 0.62f),
            radius = rx * 0.55f
        )
    )

    // Strand lines showing hair direction (swept back)
    for (i in -2..2) {
        val sx = cx + i * rx * 0.15f
        val strandPath = Path().apply {
            moveTo(sx, cy - ry * 0.2f)
            cubicTo(sx + rx * 0.02f, cy - ry * 0.5f, sx - rx * 0.01f, cy - ry * 0.8f, sx + rx * 0.03f, cy - ry * 0.98f)
        }
        drawPath(strandPath, hairColor.darken(0.08f).copy(alpha = 0.3f),
            style = Stroke(width = rx * 0.012f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawLongHairFront(
    cx: Float, cy: Float, rx: Float, ry: Float,
    hairColor: Color, highlight: Color
) {
    // Long hair front — cap with side curtains framing the face
    val frontPath = Path().apply {
        moveTo(cx - rx * 0.96f, cy + ry * 0.1f)
        cubicTo(
            cx - rx * 1.0f, cy - ry * 0.45f,
            cx - rx * 0.72f, cy - ry * 1.05f,
            cx, cy - ry * 1.12f
        )
        cubicTo(
            cx + rx * 0.72f, cy - ry * 1.05f,
            cx + rx * 1.0f, cy - ry * 0.45f,
            cx + rx * 0.96f, cy + ry * 0.1f
        )
        // Bangs — soft curtain, slightly parted
        cubicTo(cx + rx * 0.72f, cy - ry * 0.02f, cx + rx * 0.35f, cy - ry * 0.18f, cx + rx * 0.08f, cy - ry * 0.30f)
        cubicTo(cx - rx * 0.08f, cy - ry * 0.32f, cx - rx * 0.4f, cy - ry * 0.15f, cx - rx * 0.75f, cy - ry * 0.02f)
        close()
    }
    drawPath(frontPath, hairColor)

    // Side strands framing face — thicker, more visible
    for (side in listOf(-1f, 1f)) {
        val strandPath = Path().apply {
            moveTo(cx + side * rx * 0.92f, cy - ry * 0.05f)
            cubicTo(
                cx + side * rx * 1.0f, cy + ry * 0.2f,
                cx + side * rx * 0.92f, cy + ry * 0.5f,
                cx + side * rx * 0.80f, cy + ry * 0.65f
            )
            lineTo(cx + side * rx * 0.65f, cy + ry * 0.60f)
            cubicTo(
                cx + side * rx * 0.75f, cy + ry * 0.45f,
                cx + side * rx * 0.85f, cy + ry * 0.15f,
                cx + side * rx * 0.82f, cy - ry * 0.0f
            )
            close()
        }
        drawPath(strandPath, hairColor.darken(0.05f))

        // Individual strand highlight
        val strandHighlight = Path().apply {
            moveTo(cx + side * rx * 0.88f, cy + ry * 0.0f)
            cubicTo(
                cx + side * rx * 0.92f, cy + ry * 0.25f,
                cx + side * rx * 0.85f, cy + ry * 0.45f,
                cx + side * rx * 0.78f, cy + ry * 0.55f
            )
        }
        drawPath(strandHighlight, highlight.copy(alpha = 0.15f),
            style = Stroke(width = rx * 0.04f, cap = StrokeCap.Round))
    }

    // Main top highlight
    drawPath(
        frontPath,
        Brush.radialGradient(
            colors = listOf(highlight.copy(alpha = 0.15f), Color.Transparent),
            center = Offset(cx, cy - ry * 0.6f),
            radius = rx * 0.7f
        )
    )

    // Part line
    drawLine(
        color = hairColor.darken(0.18f),
        start = Offset(cx, cy - ry * 1.08f),
        end = Offset(cx, cy - ry * 0.3f),
        strokeWidth = rx * 0.012f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawWavyHairFront(
    cx: Float, cy: Float, rx: Float, ry: Float,
    hairColor: Color, highlight: Color
) {
    // Wavy front bangs
    val frontPath = Path().apply {
        moveTo(cx - rx * 0.9f, cy + ry * 0.05f)
        cubicTo(cx - rx * 0.95f, cy - ry * 0.5f, cx - rx * 0.7f, cy - ry * 1.05f, cx, cy - ry * 1.1f)
        cubicTo(cx + rx * 0.7f, cy - ry * 1.05f, cx + rx * 0.95f, cy - ry * 0.5f, cx + rx * 0.9f, cy + ry * 0.05f)
        // Wavy bangs edge
        cubicTo(cx + rx * 0.7f, cy - ry * 0.1f, cx + rx * 0.4f, cy - ry * 0.25f, cx + rx * 0.2f, cy - ry * 0.18f)
        cubicTo(cx, cy - ry * 0.28f, cx - rx * 0.2f, cy - ry * 0.15f, cx - rx * 0.4f, cy - ry * 0.22f)
        cubicTo(cx - rx * 0.6f, cy - ry * 0.12f, cx - rx * 0.8f, cy - ry * 0.02f, cx - rx * 0.9f, cy + ry * 0.05f)
        close()
    }
    drawPath(frontPath, hairColor)

    // Wave texture highlights
    for (i in -2..2) {
        val waveX = cx + i * rx * 0.2f
        val wavePath = Path().apply {
            moveTo(waveX, cy - ry * 0.9f)
            cubicTo(
                waveX + rx * 0.08f, cy - ry * 0.7f,
                waveX - rx * 0.08f, cy - ry * 0.5f,
                waveX + rx * 0.05f, cy - ry * 0.3f
            )
        }
        drawPath(wavePath, highlight.copy(alpha = 0.15f),
            style = Stroke(width = rx * 0.035f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawBunHairFront(
    cx: Float, cy: Float, rx: Float, ry: Float,
    hairColor: Color, highlight: Color
) {
    // Smooth pulled-back hair
    val frontPath = Path().apply {
        moveTo(cx - rx * 0.88f, cy - ry * 0.02f)
        cubicTo(cx - rx * 0.92f, cy - ry * 0.5f, cx - rx * 0.65f, cy - ry * 1.0f, cx, cy - ry * 1.05f)
        cubicTo(cx + rx * 0.65f, cy - ry * 1.0f, cx + rx * 0.92f, cy - ry * 0.5f, cx + rx * 0.88f, cy - ry * 0.02f)
        cubicTo(cx + rx * 0.65f, cy - ry * 0.12f, cx + rx * 0.3f, cy - ry * 0.25f, cx, cy - ry * 0.28f)
        cubicTo(cx - rx * 0.3f, cy - ry * 0.25f, cx - rx * 0.65f, cy - ry * 0.12f, cx - rx * 0.88f, cy - ry * 0.02f)
        close()
    }
    drawPath(frontPath, hairColor)

    // Smooth gradient
    drawPath(
        frontPath,
        Brush.radialGradient(
            colors = listOf(highlight.copy(alpha = 0.12f), Color.Transparent),
            center = Offset(cx, cy - ry * 0.65f),
            radius = rx * 0.6f
        )
    )
}

private fun DrawScope.drawBobHair(
    cx: Float, cy: Float, rx: Float, ry: Float,
    hairColor: Color, highlight: Color
) {
    // Bob cut — chin-length, fuller, framing the face
    val mainPath = Path().apply {
        moveTo(cx - rx * 1.0f, cy + ry * 0.40f)
        cubicTo(cx - rx * 1.05f, cy - ry * 0.35f, cx - rx * 0.72f, cy - ry * 1.06f, cx, cy - ry * 1.12f)
        cubicTo(cx + rx * 0.72f, cy - ry * 1.06f, cx + rx * 1.05f, cy - ry * 0.35f, cx + rx * 1.0f, cy + ry * 0.40f)
        // Curved bottom — natural bob ends
        cubicTo(cx + rx * 0.85f, cy + ry * 0.42f, cx + rx * 0.5f, cy + ry * 0.46f, cx, cy + ry * 0.42f)
        cubicTo(cx - rx * 0.5f, cy + ry * 0.46f, cx - rx * 0.85f, cy + ry * 0.42f, cx - rx * 1.0f, cy + ry * 0.40f)
        close()
    }
    drawPath(mainPath, hairColor)

    // Face opening — reveal the face
    val faceOpenPath = Path().apply {
        moveTo(cx - rx * 0.78f, cy + ry * 0.18f)
        cubicTo(cx - rx * 0.6f, cy - ry * 0.02f, cx - rx * 0.3f, cy - ry * 0.2f, cx, cy - ry * 0.25f)
        cubicTo(cx + rx * 0.3f, cy - ry * 0.2f, cx + rx * 0.6f, cy - ry * 0.02f, cx + rx * 0.78f, cy + ry * 0.18f)
        cubicTo(cx + rx * 0.68f, cy + ry * 0.28f, cx + rx * 0.4f, cy + ry * 0.32f, cx, cy + ry * 0.30f)
        cubicTo(cx - rx * 0.4f, cy + ry * 0.32f, cx - rx * 0.68f, cy + ry * 0.28f, cx - rx * 0.78f, cy + ry * 0.18f)
        close()
    }

    // Side panels — thicker side hair that frames face
    for (side in listOf(-1f, 1f)) {
        val panelPath = Path().apply {
            moveTo(cx + side * rx * 1.0f, cy + ry * 0.40f)
            cubicTo(cx + side * rx * 1.04f, cy - ry * 0.05f, cx + side * rx * 0.95f, cy - ry * 0.45f, cx + side * rx * 0.88f, cy - ry * 0.7f)
            lineTo(cx + side * rx * 0.68f, cy - ry * 0.1f)
            cubicTo(cx + side * rx * 0.72f, cy + ry * 0.1f, cx + side * rx * 0.78f, cy + ry * 0.28f, cx + side * rx * 0.85f, cy + ry * 0.40f)
            close()
        }
        drawPath(panelPath, hairColor.darken(0.06f))
    }

    // Highlight — top dome
    drawPath(
        mainPath,
        Brush.radialGradient(
            colors = listOf(highlight.copy(alpha = 0.16f), Color.Transparent),
            center = Offset(cx + rx * 0.08f, cy - ry * 0.55f),
            radius = rx * 0.7f
        )
    )

    // Individual strand highlights
    for (i in -1..1) {
        val sx = cx + i * rx * 0.25f
        val strandHighlight = Path().apply {
            moveTo(sx, cy - ry * 0.9f)
            cubicTo(sx + rx * 0.05f, cy - ry * 0.6f, sx - rx * 0.03f, cy - ry * 0.3f, sx + rx * 0.02f, cy - ry * 0.1f)
        }
        drawPath(strandHighlight, highlight.copy(alpha = 0.12f),
            style = Stroke(width = rx * 0.04f, cap = StrokeCap.Round))
    }
}


// ─────────────────────────────────────────────────────────────────────────────
//  Outfit — Upper Body Clothing (collar, neckline, shoulders)
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawOutfitUpper(
    outfitItem: AvatarLayerItem?, cx: Float, neckTopY: Float,
    shoulderY: Float, shoulderW: Float, canvasH: Float,
    canvasW: Float, gender: AvatarGender
) {
    if (outfitItem == null) return

    val outfitColor = outfitItem.tintColor?.let { Color(it) } ?: Color(0xFF4A90D9)
    val darkOutfit = outfitColor.darken(0.15f)
    val lightOutfit = outfitColor.lighten(0.12f)
    // shoulderW and neckTopY are already gender-differentiated from the caller
    val genderShoulderW = shoulderW
    val neckW = shoulderW * 0.28f
    val outfitId = outfitItem.id

    // Base clothing shape covering shoulders and chest
    val clothingPath = Path().apply {
        moveTo(cx - neckW * 0.85f, neckTopY + neckW * 0.3f)
        // Left collar to shoulder
        cubicTo(
            cx - neckW * 1.2f, shoulderY * 0.96f,
            cx - genderShoulderW * 0.5f, shoulderY * 0.94f,
            cx - genderShoulderW, shoulderY
        )
        // Left body
        lineTo(cx - genderShoulderW * 1.05f, canvasH)
        // Bottom
        lineTo(cx + genderShoulderW * 1.05f, canvasH)
        // Right body
        lineTo(cx + genderShoulderW, shoulderY)
        // Right collar to shoulder
        cubicTo(
            cx + genderShoulderW * 0.5f, shoulderY * 0.94f,
            cx + neckW * 1.2f, shoulderY * 0.96f,
            cx + neckW * 0.85f, neckTopY + neckW * 0.3f
        )
        close()
    }

    // Base fill
    drawPath(clothingPath, outfitColor)

    // 3D fabric gradient
    drawPath(
        clothingPath,
        Brush.verticalGradient(
            colors = listOf(lightOutfit.copy(alpha = 0.3f), Color.Transparent, darkOutfit.copy(alpha = 0.3f)),
            startY = shoulderY,
            endY = canvasH
        )
    )

    // Side shadows
    drawPath(
        clothingPath,
        Brush.horizontalGradient(
            colors = listOf(darkOutfit.copy(alpha = 0.2f), Color.Transparent, Color.Transparent, darkOutfit.copy(alpha = 0.2f)),
            startX = cx - genderShoulderW * 1.05f,
            endX = cx + genderShoulderW * 1.05f
        )
    )

    // Outfit-specific details
    when {
        outfitId.contains("hoodie", true) -> {
            // Hood shape behind neck
            val hoodPath = Path().apply {
                moveTo(cx - neckW * 1.5f, neckTopY)
                cubicTo(
                    cx - neckW * 2f, neckTopY - neckW * 0.5f,
                    cx - neckW * 1.2f, neckTopY - neckW * 1.0f,
                    cx, neckTopY - neckW * 0.6f
                )
                cubicTo(
                    cx + neckW * 1.2f, neckTopY - neckW * 1.0f,
                    cx + neckW * 2f, neckTopY - neckW * 0.5f,
                    cx + neckW * 1.5f, neckTopY
                )
            }
            drawPath(hoodPath, darkOutfit, style = Stroke(width = neckW * 0.25f, cap = StrokeCap.Round))

            // Drawstrings
            for (side in listOf(-1f, 1f)) {
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(cx + side * neckW * 0.3f, neckTopY + neckW * 0.4f),
                    end = Offset(cx + side * neckW * 0.35f, shoulderY + shoulderW * 0.15f),
                    strokeWidth = neckW * 0.04f,
                    cap = StrokeCap.Round
                )
                // Drawstring tip
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = neckW * 0.04f,
                    center = Offset(cx + side * neckW * 0.35f, shoulderY + shoulderW * 0.15f)
                )
            }

            // Kangaroo pocket
            val pocketY = shoulderY + (canvasH - shoulderY) * 0.45f
            val pocketW = genderShoulderW * 0.65f
            drawRoundRect(
                color = darkOutfit.copy(alpha = 0.3f),
                topLeft = Offset(cx - pocketW, pocketY),
                size = Size(pocketW * 2, (canvasH - shoulderY) * 0.3f),
                cornerRadius = CornerRadius(neckW * 0.15f)
            )
        }

        outfitId.contains("polo", true) -> {
            // Collar
            for (side in listOf(-1f, 1f)) {
                val collarPath = Path().apply {
                    moveTo(cx + side * neckW * 0.15f, neckTopY + neckW * 0.2f)
                    cubicTo(
                        cx + side * neckW * 0.5f, neckTopY + neckW * 0.1f,
                        cx + side * neckW * 1.2f, neckTopY + neckW * 0.0f,
                        cx + side * neckW * 1.4f, neckTopY + neckW * 0.5f
                    )
                    cubicTo(
                        cx + side * neckW * 1.1f, neckTopY + neckW * 0.6f,
                        cx + side * neckW * 0.6f, neckTopY + neckW * 0.55f,
                        cx + side * neckW * 0.15f, neckTopY + neckW * 0.5f
                    )
                    close()
                }
                drawPath(collarPath, outfitColor.lighten(0.08f))
                drawPath(collarPath, darkOutfit.copy(alpha = 0.1f),
                    style = Stroke(width = neckW * 0.02f))
            }

            // Button placket
            val buttonTop = neckTopY + neckW * 0.5f
            val buttonSpacing = neckW * 0.35f
            for (i in 0..2) {
                val by = buttonTop + i * buttonSpacing
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = neckW * 0.04f,
                    center = Offset(cx, by)
                )
            }
        }

        outfitId.contains("dress", true) -> {
            // V-neckline
            val vPath = Path().apply {
                moveTo(cx - neckW * 0.6f, neckTopY + neckW * 0.2f)
                lineTo(cx, shoulderY + (canvasH - shoulderY) * 0.15f)
                lineTo(cx + neckW * 0.6f, neckTopY + neckW * 0.2f)
            }
            drawPath(vPath, darkOutfit, style = Stroke(width = neckW * 0.03f))

            // Flowy bottom gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, outfitColor.lighten(0.08f)),
                    startY = shoulderY + (canvasH - shoulderY) * 0.3f,
                    endY = canvasH
                ),
                topLeft = Offset(cx - genderShoulderW * 1.05f, shoulderY),
                size = Size(genderShoulderW * 2.1f, canvasH - shoulderY)
            )
        }

        outfitId.contains("denim", true) || outfitId.contains("jacket", true) -> {
            // Center zipper/button line
            drawLine(
                color = darkOutfit.copy(alpha = 0.4f),
                start = Offset(cx, neckTopY + neckW * 0.3f),
                end = Offset(cx, canvasH),
                strokeWidth = neckW * 0.025f
            )

            // Buttons
            for (i in 0..3) {
                val by = shoulderY + i * (canvasH - shoulderY) * 0.2f
                drawCircle(
                    color = Color(0xFFB8860B).copy(alpha = 0.6f),
                    radius = neckW * 0.035f,
                    center = Offset(cx, by)
                )
            }

            // Collar fold
            for (side in listOf(-1f, 1f)) {
                val collarPath = Path().apply {
                    moveTo(cx, neckTopY + neckW * 0.3f)
                    lineTo(cx + side * neckW * 1.0f, neckTopY + neckW * 0.6f)
                    lineTo(cx + side * neckW * 0.8f, neckTopY + neckW * 0.9f)
                    lineTo(cx, neckTopY + neckW * 0.55f)
                    close()
                }
                drawPath(collarPath, outfitColor.lighten(0.05f))
            }

            // Stitching lines
            for (side in listOf(-1f, 1f)) {
                val dashLen = neckW * 0.06f
                drawLine(
                    color = outfitColor.lighten(0.15f).copy(alpha = 0.3f),
                    start = Offset(cx + side * genderShoulderW * 0.45f, shoulderY),
                    end = Offset(cx + side * genderShoulderW * 0.45f, canvasH),
                    strokeWidth = neckW * 0.012f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLen, dashLen))
                )
            }
        }

        outfitId.contains("sport", true) || outfitId.contains("trainer", true) -> {
            // Sport stripe on shoulders
            for (side in listOf(-1f, 1f)) {
                val stripePath = Path().apply {
                    moveTo(cx + side * genderShoulderW * 0.6f, shoulderY * 0.97f)
                    cubicTo(
                        cx + side * genderShoulderW * 0.7f, shoulderY,
                        cx + side * genderShoulderW * 0.85f, shoulderY * 1.01f,
                        cx + side * genderShoulderW * 0.95f, shoulderY * 1.02f
                    )
                }
                drawPath(stripePath, Color.White.copy(alpha = 0.5f),
                    style = Stroke(width = neckW * 0.08f, cap = StrokeCap.Round))
            }

            // Sport collar
            drawArc(
                color = darkOutfit,
                startAngle = 0f, sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - neckW * 0.6f, neckTopY + neckW * 0.15f),
                size = Size(neckW * 1.2f, neckW * 0.5f),
                style = Stroke(width = neckW * 0.06f)
            )
        }

        outfitId.contains("school", true) -> {
            // School uniform collar
            for (side in listOf(-1f, 1f)) {
                val collarPath = Path().apply {
                    moveTo(cx, neckTopY + neckW * 0.35f)
                    lineTo(cx + side * neckW * 1.3f, neckTopY + neckW * 0.8f)
                    lineTo(cx + side * neckW * 1.1f, neckTopY + neckW * 1.1f)
                    lineTo(cx, neckTopY + neckW * 0.65f)
                    close()
                }
                drawPath(collarPath, Color.White.copy(alpha = 0.85f))
            }
        }

        outfitId.contains("ninja", true) || outfitId.contains("gi", true) -> {
            // High collar
            val collarPath = Path().apply {
                moveTo(cx - neckW * 0.7f, neckTopY + neckW * 0.1f)
                lineTo(cx - neckW * 0.8f, neckTopY - neckW * 0.3f)
                cubicTo(cx - neckW * 0.5f, neckTopY - neckW * 0.5f, cx + neckW * 0.5f, neckTopY - neckW * 0.5f, cx + neckW * 0.8f, neckTopY - neckW * 0.3f)
                lineTo(cx + neckW * 0.7f, neckTopY + neckW * 0.1f)
                close()
            }
            drawPath(collarPath, darkOutfit)

            // Cross-wrap
            drawLine(
                color = darkOutfit.darken(0.1f),
                start = Offset(cx - neckW * 0.5f, neckTopY + neckW * 0.2f),
                end = Offset(cx + neckW * 0.3f, shoulderY + (canvasH - shoulderY) * 0.2f),
                strokeWidth = neckW * 0.04f
            )

            // Belt line
            val beltY = shoulderY + (canvasH - shoulderY) * 0.35f
            drawLine(
                color = Color(0xFF2A1A0A),
                start = Offset(cx - genderShoulderW * 0.8f, beltY),
                end = Offset(cx + genderShoulderW * 0.8f, beltY),
                strokeWidth = neckW * 0.08f
            )
        }

        outfitId.contains("astronaut", true) || outfitId.contains("suit", true) -> {
            // Space suit details
            // Center zipper
            drawLine(
                color = Color(0xFFC0C0C0),
                start = Offset(cx, neckTopY + neckW * 0.3f),
                end = Offset(cx, canvasH),
                strokeWidth = neckW * 0.04f
            )

            // Chest belt
            val beltY = shoulderY + (canvasH - shoulderY) * 0.2f
            drawLine(
                color = Color(0xFF808080),
                start = Offset(cx - genderShoulderW * 0.7f, beltY),
                end = Offset(cx + genderShoulderW * 0.7f, beltY),
                strokeWidth = neckW * 0.06f
            )

            // Shoulder patches
            for (side in listOf(-1f, 1f)) {
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.5f),
                    radius = neckW * 0.12f,
                    center = Offset(cx + side * genderShoulderW * 0.6f, shoulderY + genderShoulderW * 0.05f)
                )
            }
        }

        outfitId.contains("striped", true) || outfitId.contains("tee", true) -> {
            // Horizontal stripes
            val stripeSpacing = (canvasH - shoulderY) * 0.12f
            for (i in 0..5) {
                val stripeY = shoulderY + i * stripeSpacing + stripeSpacing * 0.5f
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(cx - genderShoulderW * 0.9f, stripeY),
                    end = Offset(cx + genderShoulderW * 0.9f, stripeY),
                    strokeWidth = stripeSpacing * 0.35f,
                    cap = StrokeCap.Round
                )
            }
            // Simple crew neckline
            drawArc(
                color = darkOutfit.copy(alpha = 0.4f),
                startAngle = 0f, sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - neckW * 0.55f, neckTopY + neckW * 0.15f),
                size = Size(neckW * 1.1f, neckW * 0.45f),
                style = Stroke(width = neckW * 0.04f, cap = StrokeCap.Round)
            )
        }

        outfitId.contains("turtleneck", true) -> {
            // High collar
            val collarPath = Path().apply {
                moveTo(cx - neckW * 0.75f, neckTopY + neckW * 0.15f)
                lineTo(cx - neckW * 0.8f, neckTopY - neckW * 0.25f)
                cubicTo(cx - neckW * 0.5f, neckTopY - neckW * 0.4f, cx + neckW * 0.5f, neckTopY - neckW * 0.4f, cx + neckW * 0.8f, neckTopY - neckW * 0.25f)
                lineTo(cx + neckW * 0.75f, neckTopY + neckW * 0.15f)
                close()
            }
            drawPath(collarPath, outfitColor.darken(0.08f))
            // Ribbing texture lines
            for (i in 0..3) {
                val ribY = neckTopY - neckW * 0.2f + i * neckW * 0.08f
                drawLine(
                    color = darkOutfit.copy(alpha = 0.15f),
                    start = Offset(cx - neckW * 0.6f, ribY),
                    end = Offset(cx + neckW * 0.6f, ribY),
                    strokeWidth = neckW * 0.01f
                )
            }
        }

        outfitId.contains("overalls", true) -> {
            // Bib front
            val bibPath = Path().apply {
                moveTo(cx - genderShoulderW * 0.35f, shoulderY + (canvasH - shoulderY) * 0.05f)
                lineTo(cx - genderShoulderW * 0.35f, canvasH)
                lineTo(cx + genderShoulderW * 0.35f, canvasH)
                lineTo(cx + genderShoulderW * 0.35f, shoulderY + (canvasH - shoulderY) * 0.05f)
                cubicTo(cx + genderShoulderW * 0.2f, shoulderY - (canvasH - shoulderY) * 0.02f, cx - genderShoulderW * 0.2f, shoulderY - (canvasH - shoulderY) * 0.02f, cx - genderShoulderW * 0.35f, shoulderY + (canvasH - shoulderY) * 0.05f)
                close()
            }
            drawPath(bibPath, darkOutfit.copy(alpha = 0.3f))
            // Straps
            for (side in listOf(-1f, 1f)) {
                drawLine(
                    color = darkOutfit,
                    start = Offset(cx + side * genderShoulderW * 0.25f, shoulderY + (canvasH - shoulderY) * 0.05f),
                    end = Offset(cx + side * genderShoulderW * 0.55f, shoulderY * 0.98f),
                    strokeWidth = neckW * 0.1f,
                    cap = StrokeCap.Round
                )
                // Button
                drawCircle(
                    color = Color(0xFFC0C0C0),
                    radius = neckW * 0.05f,
                    center = Offset(cx + side * genderShoulderW * 0.25f, shoulderY + (canvasH - shoulderY) * 0.07f)
                )
            }
            // Pocket
            val pocketY = shoulderY + (canvasH - shoulderY) * 0.4f
            drawRoundRect(
                color = darkOutfit.copy(alpha = 0.2f),
                topLeft = Offset(cx - genderShoulderW * 0.18f, pocketY),
                size = Size(genderShoulderW * 0.36f, (canvasH - shoulderY) * 0.18f),
                cornerRadius = CornerRadius(neckW * 0.05f)
            )
        }

        outfitId.contains("raincoat", true) -> {
            // Center zipper
            drawLine(
                color = darkOutfit.copy(alpha = 0.5f),
                start = Offset(cx, neckTopY + neckW * 0.3f),
                end = Offset(cx, canvasH),
                strokeWidth = neckW * 0.03f
            )
            // Hood collar
            val hoodPath = Path().apply {
                moveTo(cx - neckW * 1.3f, neckTopY + neckW * 0.1f)
                cubicTo(
                    cx - neckW * 1.6f, neckTopY - neckW * 0.4f,
                    cx - neckW * 1.0f, neckTopY - neckW * 0.7f,
                    cx, neckTopY - neckW * 0.45f
                )
                cubicTo(
                    cx + neckW * 1.0f, neckTopY - neckW * 0.7f,
                    cx + neckW * 1.6f, neckTopY - neckW * 0.4f,
                    cx + neckW * 1.3f, neckTopY + neckW * 0.1f
                )
            }
            drawPath(hoodPath, darkOutfit, style = Stroke(width = neckW * 0.2f, cap = StrokeCap.Round))
            // Snap buttons
            for (i in 0..3) {
                val by = shoulderY + i * (canvasH - shoulderY) * 0.2f
                drawCircle(
                    color = Color(0xFF808080).copy(alpha = 0.5f),
                    radius = neckW * 0.04f,
                    center = Offset(cx + neckW * 0.08f, by)
                )
            }
        }

        else -> {
            // Default casual — simple round neckline
            drawArc(
                color = darkOutfit.copy(alpha = 0.3f),
                startAngle = 0f, sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - neckW * 0.55f, neckTopY + neckW * 0.15f),
                size = Size(neckW * 1.1f, neckW * 0.5f),
                style = Stroke(width = neckW * 0.04f, cap = StrokeCap.Round)
            )

            // Subtle shoulder seams
            for (side in listOf(-1f, 1f)) {
                drawLine(
                    color = darkOutfit.copy(alpha = 0.15f),
                    start = Offset(cx + side * neckW * 0.9f, neckTopY + neckW * 0.4f),
                    end = Offset(cx + side * genderShoulderW * 0.85f, shoulderY * 1.01f),
                    strokeWidth = neckW * 0.015f
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Accessories
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawAccessory(
    accessoryItem: AvatarLayerItem?, cx: Float, cy: Float,
    rx: Float, ry: Float, shoulderY: Float,
    canvasW: Float, canvasH: Float, gender: AvatarGender
) {
    if (accessoryItem == null) return
    val accId = accessoryItem.id
    val accColor = accessoryItem.tintColor?.let { Color(it) } ?: Color(0xFFFFD700)

    when {
        accId.contains("glasses", true) -> {
            val glassY = cy + ry * 0.02f
            val glassR = rx * 0.16f
            val bridgeW = rx * 0.10f

            // Left lens
            drawCircle(
                color = Color(0x25000000),
                radius = glassR,
                center = Offset(cx - rx * 0.33f, glassY)
            )
            drawCircle(
                color = Color(0xFF2A2A2A),
                radius = glassR,
                center = Offset(cx - rx * 0.33f, glassY),
                style = Stroke(width = rx * 0.025f)
            )
            // Lens flare
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = glassR * 0.3f,
                center = Offset(cx - rx * 0.38f, glassY - glassR * 0.25f)
            )

            // Right lens
            drawCircle(
                color = Color(0x25000000),
                radius = glassR,
                center = Offset(cx + rx * 0.33f, glassY)
            )
            drawCircle(
                color = Color(0xFF2A2A2A),
                radius = glassR,
                center = Offset(cx + rx * 0.33f, glassY),
                style = Stroke(width = rx * 0.025f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = glassR * 0.3f,
                center = Offset(cx + rx * 0.28f, glassY - glassR * 0.25f)
            )

            // Bridge
            drawLine(
                color = Color(0xFF2A2A2A),
                start = Offset(cx - rx * 0.13f, glassY - glassR * 0.1f),
                end = Offset(cx + rx * 0.13f, glassY - glassR * 0.1f),
                strokeWidth = rx * 0.025f,
                cap = StrokeCap.Round
            )

            // Temple arms
            for (side in listOf(-1f, 1f)) {
                drawLine(
                    color = Color(0xFF2A2A2A),
                    start = Offset(cx + side * (rx * 0.33f + glassR * 0.9f), glassY - glassR * 0.1f),
                    end = Offset(cx + side * rx * 0.92f, glassY),
                    strokeWidth = rx * 0.02f,
                    cap = StrokeCap.Round
                )
            }
        }

        accId.contains("bow", true) -> {
            val bowX = cx + rx * 0.55f
            val bowY = cy - ry * 0.75f
            val bowSize = rx * 0.18f
            val bowColor = accColor

            // Left wing
            val leftWing = Path().apply {
                moveTo(bowX, bowY)
                cubicTo(bowX - bowSize * 1.5f, bowY - bowSize, bowX - bowSize * 1.8f, bowY + bowSize * 0.5f, bowX, bowY)
            }
            drawPath(leftWing, bowColor)

            // Right wing
            val rightWing = Path().apply {
                moveTo(bowX, bowY)
                cubicTo(bowX + bowSize * 1.5f, bowY - bowSize, bowX + bowSize * 1.8f, bowY + bowSize * 0.5f, bowX, bowY)
            }
            drawPath(rightWing, bowColor)

            // Center knot
            drawCircle(color = bowColor.darken(0.15f), radius = bowSize * 0.35f, center = Offset(bowX, bowY))
            drawCircle(color = bowColor.lighten(0.1f), radius = bowSize * 0.15f, center = Offset(bowX - bowSize * 0.05f, bowY - bowSize * 0.08f))
        }

        accId.contains("necklace", true) -> {
            val neckY = cy + ry * 0.92f
            // Chain arc
            drawArc(
                color = Color(0xFFDAA520),
                startAngle = 0f, sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - rx * 0.3f, neckY - ry * 0.1f),
                size = Size(rx * 0.6f, ry * 0.35f),
                style = Stroke(width = rx * 0.02f, cap = StrokeCap.Round)
            )
            // Star pendant
            val pendantY = neckY + ry * 0.1f
            drawCircle(
                color = Color(0xFFFFD700),
                radius = rx * 0.04f,
                center = Offset(cx, pendantY)
            )
            drawCircle(
                color = Color(0xFFFFF8DC),
                radius = rx * 0.02f,
                center = Offset(cx - rx * 0.008f, pendantY - rx * 0.01f)
            )
        }

        accId.contains("bandana", true) -> {
            val bandY = cy - ry * 0.40f
            val bandH = ry * 0.12f
            // Headband
            val bandPath = Path().apply {
                moveTo(cx - rx * 0.92f, bandY)
                cubicTo(cx - rx * 0.7f, bandY - bandH, cx + rx * 0.7f, bandY - bandH, cx + rx * 0.92f, bandY)
                cubicTo(cx + rx * 0.7f, bandY + bandH * 0.3f, cx - rx * 0.7f, bandY + bandH * 0.3f, cx - rx * 0.92f, bandY)
                close()
            }
            drawPath(bandPath, accColor)
            drawPath(bandPath, accColor.darken(0.1f), style = Stroke(width = rx * 0.01f))

            // Dot pattern
            for (i in -3..3) {
                drawCircle(
                    color = accColor.lighten(0.2f).copy(alpha = 0.4f),
                    radius = rx * 0.015f,
                    center = Offset(cx + i * rx * 0.13f, bandY)
                )
            }

            // Side knot
            val knotX = cx + rx * 0.88f
            drawCircle(color = accColor.darken(0.1f), radius = rx * 0.04f, center = Offset(knotX, bandY))
            // Trailing ends
            drawLine(
                color = accColor,
                start = Offset(knotX, bandY + rx * 0.04f),
                end = Offset(knotX + rx * 0.08f, bandY + ry * 0.15f),
                strokeWidth = rx * 0.025f,
                cap = StrokeCap.Round
            )
        }

        accId.contains("wristband", true) -> {
            // Wristband on visible shoulder area
            val wristY = shoulderY + (canvasH - shoulderY) * 0.3f
            val wristX = cx + canvasW * 0.28f
            drawRoundRect(
                color = accColor,
                topLeft = Offset(wristX - rx * 0.08f, wristY),
                size = Size(rx * 0.16f, ry * 0.06f),
                cornerRadius = CornerRadius(rx * 0.03f)
            )
            // Shine
            drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(wristX - rx * 0.06f, wristY + ry * 0.005f),
                size = Size(rx * 0.08f, ry * 0.02f),
                cornerRadius = CornerRadius(rx * 0.01f)
            )
        }

        accId.contains("headband", true) -> {
            val bandY = cy - ry * 0.45f
            // Curved headband
            val bandPath = Path().apply {
                moveTo(cx - rx * 0.85f, bandY + ry * 0.08f)
                cubicTo(cx - rx * 0.6f, bandY - ry * 0.05f, cx + rx * 0.6f, bandY - ry * 0.05f, cx + rx * 0.85f, bandY + ry * 0.08f)
                cubicTo(cx + rx * 0.6f, bandY + ry * 0.02f, cx - rx * 0.6f, bandY + ry * 0.02f, cx - rx * 0.85f, bandY + ry * 0.08f)
                close()
            }
            drawPath(bandPath, accColor)

            // Metal plate with symbol
            drawRoundRect(
                color = Color(0xFFC0C0C0),
                topLeft = Offset(cx - rx * 0.08f, bandY - ry * 0.02f),
                size = Size(rx * 0.16f, ry * 0.06f),
                cornerRadius = CornerRadius(rx * 0.02f)
            )
            // Engraved line
            drawLine(
                color = Color(0xFF808080),
                start = Offset(cx - rx * 0.04f, bandY + ry * 0.01f),
                end = Offset(cx + rx * 0.04f, bandY + ry * 0.01f),
                strokeWidth = rx * 0.01f
            )

            // Trailing knots on side
            for (i in 0..1) {
                drawLine(
                    color = accColor.darken(0.1f),
                    start = Offset(cx + rx * 0.85f, bandY + ry * 0.08f),
                    end = Offset(cx + rx * 0.9f + i * rx * 0.03f, bandY + ry * (0.2f + i * 0.1f)),
                    strokeWidth = rx * 0.02f,
                    cap = StrokeCap.Round
                )
            }
        }

        accId.contains("tiara", true) || accId.contains("crown", true) -> {
            val crownY = cy - ry * 0.85f
            val crownW = rx * 0.35f
            val crownH = ry * 0.15f

            // Base band
            drawRoundRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(cx - crownW, crownY),
                size = Size(crownW * 2, crownH * 0.35f),
                cornerRadius = CornerRadius(rx * 0.02f)
            )

            // Crown points
            val points = listOf(-0.6f, 0f, 0.6f)
            points.forEachIndexed { idx, px ->
                val pointH = if (idx == 1) crownH else crownH * 0.75f
                val pointPath = Path().apply {
                    moveTo(cx + px * crownW - crownW * 0.18f, crownY)
                    lineTo(cx + px * crownW, crownY - pointH)
                    lineTo(cx + px * crownW + crownW * 0.18f, crownY)
                    close()
                }
                drawPath(pointPath, Color(0xFFFFD700))
            }

            // Gems at point tips
            points.forEachIndexed { idx, px ->
                val pointH = if (idx == 1) crownH else crownH * 0.75f
                val gemColor = when (idx) {
                    0 -> Color(0xFFFF4444)
                    1 -> Color(0xFF4488FF)
                    else -> Color(0xFF44FF44)
                }
                drawCircle(
                    color = gemColor,
                    radius = rx * 0.025f,
                    center = Offset(cx + px * crownW, crownY - pointH + rx * 0.03f)
                )
                // Gem sparkle
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = rx * 0.01f,
                    center = Offset(cx + px * crownW - rx * 0.008f, crownY - pointH + rx * 0.022f)
                )
            }

            // Crown highlight
            drawRoundRect(
                color = Color.White.copy(alpha = 0.2f),
                topLeft = Offset(cx - crownW * 0.8f, crownY + crownH * 0.05f),
                size = Size(crownW * 1.2f, crownH * 0.12f),
                cornerRadius = CornerRadius(rx * 0.01f)
            )
        }

        accId.contains("cap", true) || accId.contains("hat", true) -> {
            val capY = cy - ry * 0.60f
            val capW = rx * 0.95f
            val capH = ry * 0.45f

            // Cap dome
            val domePath = Path().apply {
                moveTo(cx - capW, capY + capH * 0.3f)
                cubicTo(cx - capW * 1.05f, capY - capH * 0.5f, cx + capW * 1.05f, capY - capH * 0.5f, cx + capW, capY + capH * 0.3f)
                cubicTo(cx + capW * 0.7f, capY + capH * 0.35f, cx - capW * 0.7f, capY + capH * 0.35f, cx - capW, capY + capH * 0.3f)
                close()
            }
            drawPath(domePath, accColor)

            // Cap brim
            val brimPath = Path().apply {
                moveTo(cx - capW * 0.3f, capY + capH * 0.32f)
                cubicTo(cx + capW * 0.2f, capY + capH * 0.28f, cx + capW * 0.8f, capY + capH * 0.3f, cx + capW * 1.15f, capY + capH * 0.42f)
                cubicTo(cx + capW * 0.8f, capY + capH * 0.48f, cx + capW * 0.2f, capY + capH * 0.44f, cx - capW * 0.3f, capY + capH * 0.38f)
                close()
            }
            drawPath(brimPath, accColor.darken(0.1f))

            // Cap highlight
            drawPath(
                domePath,
                Brush.radialGradient(
                    colors = listOf(accColor.lighten(0.12f).copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(cx - capW * 0.15f, capY - capH * 0.1f),
                    radius = capW * 0.5f
                )
            )

            // Band
            drawLine(
                color = accColor.darken(0.2f),
                start = Offset(cx - capW * 0.85f, capY + capH * 0.25f),
                end = Offset(cx + capW * 0.85f, capY + capH * 0.25f),
                strokeWidth = capH * 0.06f
            )
        }

        accId.contains("mask", true) -> {
            val maskY = cy + ry * 0.02f
            val maskW = rx * 0.7f
            val maskH = ry * 0.14f

            // Mask body
            val maskPath = Path().apply {
                moveTo(cx - maskW, maskY)
                cubicTo(cx - maskW * 0.8f, maskY - maskH, cx + maskW * 0.8f, maskY - maskH, cx + maskW, maskY)
                cubicTo(cx + maskW * 0.8f, maskY + maskH * 0.6f, cx - maskW * 0.8f, maskY + maskH * 0.6f, cx - maskW, maskY)
                close()
            }
            drawPath(maskPath, accColor)

            // Eye holes
            for (side in listOf(-1f, 1f)) {
                val holeX = cx + side * rx * 0.28f
                drawOval(
                    color = Color(0xFF1A1A1A),
                    topLeft = Offset(holeX - rx * 0.12f, maskY - maskH * 0.35f),
                    size = Size(rx * 0.24f, maskH * 0.65f)
                )
            }

            // Bridge connection
            drawLine(
                color = accColor.darken(0.15f),
                start = Offset(cx - rx * 0.05f, maskY - maskH * 0.1f),
                end = Offset(cx + rx * 0.05f, maskY - maskH * 0.1f),
                strokeWidth = rx * 0.03f
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Special FX — Animated overlay effects
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AvatarSpecialFxLayer(fxItem: AvatarLayerItem) {
    val transition = rememberInfiniteTransition(label = "fx")
    val pulse by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val rotation by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "rotation"
    )
    val fxId = fxItem.id

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when {
            fxId.contains("sparkle", true) -> {
                val sparklePositions = listOf(
                    0.15f to 0.12f, 0.82f to 0.18f, 0.25f to 0.75f,
                    0.78f to 0.65f, 0.5f to 0.08f, 0.12f to 0.45f,
                    0.88f to 0.40f, 0.45f to 0.85f
                )
                sparklePositions.forEachIndexed { idx, (sx, sy) ->
                    val phase = (pulse + idx * 0.15f) % 1f
                    val size = w * 0.015f * (0.5f + phase * 0.5f)
                    val alpha = 0.3f + phase * 0.5f
                    val sparkColor = Color(0xFFFFD700).copy(alpha = alpha)
                    val center = Offset(sx * w, sy * h)

                    // 4-pointed star
                    drawLine(sparkColor, Offset(center.x - size, center.y), Offset(center.x + size, center.y), strokeWidth = size * 0.3f)
                    drawLine(sparkColor, Offset(center.x, center.y - size), Offset(center.x, center.y + size), strokeWidth = size * 0.3f)
                    // Diagonal points
                    val dSize = size * 0.6f
                    drawLine(sparkColor, Offset(center.x - dSize, center.y - dSize), Offset(center.x + dSize, center.y + dSize), strokeWidth = size * 0.2f)
                    drawLine(sparkColor, Offset(center.x + dSize, center.y - dSize), Offset(center.x - dSize, center.y + dSize), strokeWidth = size * 0.2f)
                }
            }

            fxId.contains("glow", true) -> {
                val glowR = w * 0.35f * (0.85f + pulse * 0.15f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x2500BFFF),
                            Color(0x1500BFFF),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f, h * 0.4f),
                        radius = glowR
                    ),
                    radius = glowR,
                    center = Offset(w * 0.5f, h * 0.4f)
                )
            }

            fxId.contains("bubble", true) -> {
                val bubblePositions = listOf(
                    0.2f to 0.8f, 0.35f to 0.7f, 0.55f to 0.85f,
                    0.75f to 0.75f, 0.15f to 0.6f, 0.65f to 0.65f,
                    0.45f to 0.55f, 0.85f to 0.5f
                )
                bubblePositions.forEachIndexed { idx, (bx, by) ->
                    val phase = (pulse + idx * 0.12f) % 1f
                    val bubbleR = w * 0.018f * (0.7f + phase * 0.3f)
                    val bubbleAlpha = 0.25f + phase * 0.25f
                    val bubbleCenter = Offset(bx * w, by * h - phase * h * 0.05f)

                    // Bubble circle
                    drawCircle(
                        color = Color(0xFF87CEEB).copy(alpha = bubbleAlpha),
                        radius = bubbleR,
                        center = bubbleCenter,
                        style = Stroke(width = bubbleR * 0.2f)
                    )
                    // Highlight
                    drawCircle(
                        color = Color.White.copy(alpha = bubbleAlpha * 0.8f),
                        radius = bubbleR * 0.25f,
                        center = Offset(bubbleCenter.x - bubbleR * 0.3f, bubbleCenter.y - bubbleR * 0.3f)
                    )
                }
            }

            fxId.contains("confetti", true) -> {
                val confettiColors = listOf(
                    Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFD93D),
                    Color(0xFF6BCB77), Color(0xFFFF69B4), Color(0xFF4A90D9)
                )
                val confettiPositions = listOf(
                    0.1f to 0.1f, 0.3f to 0.15f, 0.5f to 0.08f,
                    0.7f to 0.12f, 0.9f to 0.18f, 0.2f to 0.25f,
                    0.45f to 0.22f, 0.65f to 0.28f, 0.85f to 0.05f,
                    0.15f to 0.32f, 0.55f to 0.35f, 0.8f to 0.3f
                )
                confettiPositions.forEachIndexed { idx, (cx, cy) ->
                    val confettiY = cy * h + pulse * h * 0.06f
                    val confettiSize = w * 0.012f
                    val color = confettiColors[idx % confettiColors.size]
                    rotate(
                        degrees = rotation + idx * 30f,
                        pivot = Offset(cx * w, confettiY)
                    ) {
                        drawRect(
                            color = color.copy(alpha = 0.7f),
                            topLeft = Offset(cx * w - confettiSize, confettiY - confettiSize * 0.4f),
                            size = Size(confettiSize * 2, confettiSize * 0.8f)
                        )
                    }
                }
            }

            fxId.contains("fire", true) -> {
                val fireColors = listOf(Color(0xFFFF4500), Color(0xFFFF6347), Color(0xFFFFD700), Color(0xFFFF8C00))
                val firePositions = listOf(0.3f, 0.45f, 0.55f, 0.7f)
                firePositions.forEachIndexed { idx, fx ->
                    val flameH = h * 0.12f * (0.7f + pulse * 0.3f + (idx % 2) * 0.15f)
                    val baseY = h * 0.88f
                    val flamePath = Path().apply {
                        moveTo(fx * w - w * 0.03f, baseY)
                        cubicTo(
                            fx * w - w * 0.02f, baseY - flameH * 0.5f,
                            fx * w - w * 0.015f, baseY - flameH * 0.8f,
                            fx * w, baseY - flameH
                        )
                        cubicTo(
                            fx * w + w * 0.015f, baseY - flameH * 0.8f,
                            fx * w + w * 0.02f, baseY - flameH * 0.5f,
                            fx * w + w * 0.03f, baseY
                        )
                        close()
                    }
                    drawPath(flamePath, fireColors[idx].copy(alpha = 0.6f))
                    // Inner bright core
                    val innerPath = Path().apply {
                        moveTo(fx * w - w * 0.015f, baseY)
                        cubicTo(
                            fx * w - w * 0.01f, baseY - flameH * 0.3f,
                            fx * w, baseY - flameH * 0.6f,
                            fx * w, baseY - flameH * 0.7f
                        )
                        cubicTo(
                            fx * w, baseY - flameH * 0.6f,
                            fx * w + w * 0.01f, baseY - flameH * 0.3f,
                            fx * w + w * 0.015f, baseY
                        )
                        close()
                    }
                    drawPath(innerPath, Color(0xFFFFFF00).copy(alpha = 0.4f))
                }
            }

            fxId.contains("star", true) -> {
                val starCount = 6
                for (i in 0 until starCount) {
                    val angle = Math.toRadians((rotation + i * 60.0))
                    val orbitR = w * 0.32f
                    val starX = w * 0.5f + cos(angle).toFloat() * orbitR
                    val starY = h * 0.4f + sin(angle).toFloat() * orbitR * 0.6f
                    val starAlpha = 0.4f + pulse * 0.3f
                    val starSize = w * 0.012f

                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = starAlpha),
                        radius = starSize,
                        center = Offset(starX, starY)
                    )
                    // Star glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x40FFD700), Color.Transparent),
                            center = Offset(starX, starY),
                            radius = starSize * 2.5f
                        ),
                        radius = starSize * 2.5f,
                        center = Offset(starX, starY)
                    )
                }
            }

            fxId.contains("lightning", true) -> {
                val boltAlpha = 0.35f + pulse * 0.35f
                for (boltIdx in 0..1) {
                    val boltX = w * (0.25f + boltIdx * 0.5f)
                    val boltPath = Path().apply {
                        moveTo(boltX, h * 0.05f)
                        lineTo(boltX - w * 0.03f, h * 0.15f)
                        lineTo(boltX + w * 0.02f, h * 0.15f)
                        lineTo(boltX - w * 0.02f, h * 0.28f)
                        lineTo(boltX + w * 0.015f, h * 0.28f)
                        lineTo(boltX - w * 0.01f, h * 0.38f)
                    }
                    // Glow halo
                    drawPath(boltPath, Color(0xFFFFFF00).copy(alpha = boltAlpha * 0.3f),
                        style = Stroke(width = w * 0.02f, cap = StrokeCap.Round))
                    // Main bolt
                    drawPath(boltPath, Color(0xFFFFFF00).copy(alpha = boltAlpha),
                        style = Stroke(width = w * 0.008f, cap = StrokeCap.Round))
                }
            }

            fxId.contains("wing", true) || fxId.contains("fairy", true) -> {
                val wingAlpha = 0.2f + pulse * 0.15f
                for (side in listOf(-1f, 1f)) {
                    val wingPath = Path().apply {
                        moveTo(w * 0.5f + side * w * 0.08f, h * 0.45f)
                        cubicTo(
                            w * 0.5f + side * w * 0.25f, h * 0.25f,
                            w * 0.5f + side * w * 0.4f, h * 0.3f,
                            w * 0.5f + side * w * 0.35f, h * 0.5f
                        )
                        cubicTo(
                            w * 0.5f + side * w * 0.3f, h * 0.55f,
                            w * 0.5f + side * w * 0.15f, h * 0.55f,
                            w * 0.5f + side * w * 0.08f, h * 0.45f
                        )
                        close()
                    }
                    drawPath(wingPath, Color(0xFFE8D5F5).copy(alpha = wingAlpha))
                    drawPath(wingPath, Color(0xFFD4A5F5).copy(alpha = wingAlpha * 0.5f),
                        style = Stroke(width = w * 0.005f))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Utility Composables — SkinTonePicker, GenderSelector, AvatarChip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SkinTonePicker(
    selectedTone: Long,
    onToneSelected: (Long) -> Unit
) {
    val tones = listOf(
        0xFFFFF0DB to "Light",
        0xFFFFDBAD to "Fair",
        0xFFE8B88A to "Medium",
        0xFFD4956B to "Tan",
        0xFFA56B42 to "Brown",
        0xFF6B3A20 to "Deep",
        0xFF4A2912 to "Dark Brown",
        0xFF2C1A0E to "Black"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tones.forEach { (tone, label) ->
            val isSelected = selectedTone == tone
            Box(
                modifier = Modifier
                    .size(if (isSelected) 40.dp else 34.dp)
                    .clip(CircleShape)
                    .background(Color(tone))
                    .then(
                        if (isSelected)
                            Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        else
                            Modifier.border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                    )
                    .clickable { onToneSelected(tone) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Text("✓", fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun GenderSelector(
    selected: AvatarGender,
    onSelect: (AvatarGender) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(AvatarGender.BOY to "👦 Boy", AvatarGender.GIRL to "👧 Girl").forEach { (gender, label) ->
            val isSelected = selected == gender
            Surface(
                modifier = Modifier.clickable { onSelect(gender) },
                shape = RoundedCornerShape(50),
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

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
            .background(Color(0xFFE0F7FA))
    ) {
        // Mini background
        AvatarBackgroundLayer(avatarState.activeBackground)

        // Mini portrait in chip
        Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
            val w = this.size.width
            val h = this.size.height
            val headCX = w * 0.5f
            val headCY = h * 0.42f
            val headRX = w * 0.38f
            val headRY = h * 0.32f
            val skinColor = Color(avatarState.skinTone)
            val hairColor = Color(avatarState.resolvedHairColor)
            val eyeColor = avatarState.activeEyeStyle?.tintColor?.let { Color(it) } ?: Color(0xFF6B4226)

            // Simple mini head
            val headPath = buildHeadPath(headCX, headCY, headRX, headRY, avatarState.gender, avatarState.faceShape ?: "oval")
            drawPath(headPath, skinColor)
            drawPath(
                headPath,
                Brush.radialGradient(
                    colors = listOf(Color.Transparent, skinColor.darken(0.1f)),
                    center = Offset(headCX, headCY),
                    radius = headRX * 1.2f
                )
            )

            // Mini eyes
            val eyeR = headRX * 0.12f
            for (side in listOf(-1f, 1f)) {
                val ex = headCX + side * headRX * 0.35f
                val ey = headCY + headRY * 0.05f
                drawCircle(Color(0xFFFBFBFF), eyeR * 1.4f, Offset(ex, ey))
                drawCircle(eyeColor, eyeR, Offset(ex, ey))
                drawCircle(Color(0xFF0A0A0A), eyeR * 0.45f, Offset(ex, ey))
                drawCircle(Color.White.copy(alpha = 0.8f), eyeR * 0.25f, Offset(ex - eyeR * 0.15f, ey - eyeR * 0.2f))
            }

            // Mini nose
            drawCircle(skinColor.darken(0.12f), headRX * 0.04f, Offset(headCX, headCY + headRY * 0.3f))

            // Mini mouth
            drawArc(
                color = Color(0xFF6B2A3A),
                startAngle = 0f, sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(headCX - headRX * 0.12f, headCY + headRY * 0.42f),
                size = Size(headRX * 0.24f, headRY * 0.12f)
            )

            // Mini hair (simplified)
            if (avatarState.activeHair != null) {
                val capPath = Path().apply {
                    moveTo(headCX - headRX * 0.92f, headCY - headRY * 0.05f)
                    cubicTo(
                        headCX - headRX * 0.95f, headCY - headRY * 0.55f,
                        headCX - headRX * 0.7f, headCY - headRY * 1.0f,
                        headCX, headCY - headRY * 1.05f
                    )
                    cubicTo(
                        headCX + headRX * 0.7f, headCY - headRY * 1.0f,
                        headCX + headRX * 0.95f, headCY - headRY * 0.55f,
                        headCX + headRX * 0.92f, headCY - headRY * 0.05f
                    )
                    cubicTo(headCX + headRX * 0.65f, headCY - headRY * 0.15f, headCX - headRX * 0.65f, headCY - headRY * 0.15f, headCX - headRX * 0.92f, headCY - headRY * 0.05f)
                    close()
                }
                drawPath(capPath, hairColor)
            }

            // Mini shoulders
            val miniShoulderY = headCY + headRY * 0.95f
            val miniShoulderPath = Path().apply {
                moveTo(headCX - headRX * 0.3f, miniShoulderY)
                cubicTo(
                    headCX - headRX * 0.6f, miniShoulderY + headRY * 0.1f,
                    headCX - headRX * 0.9f, miniShoulderY + headRY * 0.2f,
                    headCX - headRX * 1.1f, miniShoulderY + headRY * 0.4f
                )
                lineTo(headCX - headRX * 1.1f, h)
                lineTo(headCX + headRX * 1.1f, h)
                lineTo(headCX + headRX * 1.1f, miniShoulderY + headRY * 0.4f)
                cubicTo(
                    headCX + headRX * 0.9f, miniShoulderY + headRY * 0.2f,
                    headCX + headRX * 0.6f, miniShoulderY + headRY * 0.1f,
                    headCX + headRX * 0.3f, miniShoulderY
                )
                close()
            }
            val outfitColor = avatarState.activeOutfit?.tintColor?.let { Color(it) } ?: skinColor
            drawPath(miniShoulderPath, outfitColor)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Color Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun Color.darken(by: Float): Color = Color(
    red = (red * (1f - by)).coerceIn(0f, 1f),
    green = (green * (1f - by)).coerceIn(0f, 1f),
    blue = (blue * (1f - by)).coerceIn(0f, 1f),
    alpha = alpha
)

private fun Color.lighten(by: Float): Color = Color(
    red = (red + (1f - red) * by).coerceIn(0f, 1f),
    green = (green + (1f - green) * by).coerceIn(0f, 1f),
    blue = (blue + (1f - blue) * by).coerceIn(0f, 1f),
    alpha = alpha
)
