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
import androidx.compose.ui.graphics.drawscope.Stroke
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
            eyeStyleItem = avatarState.activeEyeStyle,
            faceDetailItem = avatarState.activeFaceDetail,
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

        // ── Layer 6: Shoes overlay ────────────────────────────────────────
        avatarState.activeShoes?.let { shoes ->
            AvatarShoesLayer(shoes, avatarState.gender,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(0.85f)
                    .fillMaxWidth(0.75f)
            )
        }

        // ── Layer 7: Accessory overlay ────────────────────────────────────
        avatarState.activeAccessory?.let { acc ->
            AvatarAccessoryLayer(acc,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(0.85f)
                    .fillMaxWidth(0.75f)
            )
        }

        // ── Layer 8: Special FX (animated) ───────────────────────────────
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
//  Character Body — modern cartoon human figure drawn with cubic bezier paths
//  Eye colour and face details are injected here so they don't need extra layers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarCharacterBody(
    gender: AvatarGender,
    skinTone: Color,
    eyeStyleItem: AvatarLayerItem? = null,
    faceDetailItem: AvatarLayerItem? = null,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawRealisticCharacter(this, gender, skinTone, eyeStyleItem, faceDetailItem)
    }
}

private fun drawRealisticCharacter(
    scope: DrawScope,
    gender: AvatarGender,
    skinTone: Color,
    eyeStyleItem: AvatarLayerItem? = null,
    faceDetailItem: AvatarLayerItem? = null
) = with(scope) {
    val w = size.width
    val h = size.height
    val cx = w / 2f

    // ── HEAD dimensions ──────────────────────────────────────────────────────
    val headW  = w * 0.46f
    val headH  = w * 0.54f
    val headCy = h * 0.19f
    val headTop = headCy - headH / 2f
    val headBot = headCy + headH / 2f

    // ── EARS (drawn first so head covers inner ear) ──────────────────────────
    val earCy = headCy + headH * 0.06f
    val earW  = headW * 0.17f
    val earH  = headH * 0.29f
    listOf(-1f, 1f).forEach { side ->
        val earX = cx + side * headW * 0.485f
        drawOval(skinTone,
            topLeft = Offset(earX - earW / 2f, earCy - earH / 2f),
            size = androidx.compose.ui.geometry.Size(earW, earH))
        // Inner ear hollow
        drawOval(skinTone.darken(0.09f),
            topLeft = Offset(earX - earW * 0.35f, earCy - earH * 0.32f),
            size = androidx.compose.ui.geometry.Size(earW * 0.5f, earH * 0.55f))
    }

    // ── HEAD SHAPE (cubic bezier — oval top, gently pointed chin) ────────────
    val headPath = Path().apply {
        moveTo(cx, headTop)
        cubicTo(cx - headW * 0.15f, headTop,
            cx - headW / 2f, headCy - headH * 0.30f,
            cx - headW / 2f, headCy)
        cubicTo(cx - headW / 2f, headCy + headH * 0.30f,
            cx - headW * 0.26f, headBot,
            cx, headBot)
        cubicTo(cx + headW * 0.26f, headBot,
            cx + headW / 2f, headCy + headH * 0.30f,
            cx + headW / 2f, headCy)
        cubicTo(cx + headW / 2f, headCy - headH * 0.30f,
            cx + headW * 0.15f, headTop,
            cx, headTop)
        close()
    }
    drawPath(headPath, skinTone)

    // ── NECK ─────────────────────────────────────────────────────────────────
    val neckW   = w * 0.115f
    val neckTop = headBot - headH * 0.10f
    val neckBot = headBot + headH * 0.17f
    drawRoundRect(skinTone,
        topLeft = Offset(cx - neckW / 2f, neckTop),
        size = androidx.compose.ui.geometry.Size(neckW, neckBot - neckTop),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(neckW / 2f))

    // ── FOREHEAD HIGHLIGHT (subtle softness) ──────────────────────────────────
    drawOval(Color.White.copy(alpha = 0.09f),
        topLeft = Offset(cx - headW * 0.22f, headTop + headH * 0.07f),
        size = androidx.compose.ui.geometry.Size(headW * 0.44f, headH * 0.21f))

    // ── EYEBROWS ──────────────────────────────────────────────────────────────
    val eyeY    = headCy - headH * 0.07f
    val eyeOffX = headW * 0.275f
    val eyeRX   = headW * 0.135f
    val eyeRY   = headH * 0.103f
    val browY   = eyeY - eyeRY * 1.9f
    val browColor = skinTone.darken(0.52f)
    val browThick = if (gender == AvatarGender.BOY) 5.5f else 4.2f
    listOf(-1f, 1f).forEach { side ->
        val bx = cx + side * eyeOffX
        drawLine(browColor,
            Offset(bx - eyeRX * 1.05f, browY + eyeRY * 0.38f),
            Offset(bx - eyeRX * 0.05f, browY - eyeRY * 0.32f),
            strokeWidth = browThick, cap = StrokeCap.Round)
        drawLine(browColor,
            Offset(bx - eyeRX * 0.05f, browY - eyeRY * 0.32f),
            Offset(bx + eyeRX * 1.05f, browY + eyeRY * 0.38f),
            strokeWidth = browThick, cap = StrokeCap.Round)
    }

    // ── EYES (large cartoon almond shape with sparkly irises) ─────────────────
    val irisColor = when {
        eyeStyleItem?.id?.contains("blue")   == true -> Color(0xFF1565C0)
        eyeStyleItem?.id?.contains("green")  == true -> Color(0xFF2E7D32)
        eyeStyleItem?.id?.contains("grey")   == true -> Color(0xFF546E7A)
        eyeStyleItem?.id?.contains("hazel")  == true -> Color(0xFF795548)
        eyeStyleItem?.id?.contains("purple") == true -> Color(0xFF6A1B9A)
        else -> Color(0xFF3D2B1F)  // default warm brown
    }
    listOf(-1f, 1f).forEach { side ->
        val ex = cx + side * eyeOffX

        // Eye white — almond / slightly teardrop shape
        val whitePath = Path().apply {
            moveTo(ex - eyeRX, eyeY)
            cubicTo(ex - eyeRX * 0.75f, eyeY - eyeRY * 1.12f,
                ex + eyeRX * 0.75f, eyeY - eyeRY * 1.12f,
                ex + eyeRX, eyeY)
            cubicTo(ex + eyeRX * 0.75f, eyeY + eyeRY * 0.9f,
                ex - eyeRX * 0.75f, eyeY + eyeRY * 0.9f,
                ex - eyeRX, eyeY)
            close()
        }
        drawPath(whitePath, Color.White)

        // Iris
        val irisR = eyeRY * 0.78f
        drawCircle(irisColor, radius = irisR, center = Offset(ex, eyeY))
        // Iris depth ring
        drawCircle(Color.Black.copy(alpha = 0.12f), radius = irisR,
            center = Offset(ex, eyeY), style = Stroke(width = 2.5f))
        // Pupil
        drawCircle(Color(0xFF0A0605), radius = irisR * 0.55f, center = Offset(ex, eyeY))
        // Main catchlight
        drawCircle(Color.White, radius = irisR * 0.30f,
            center = Offset(ex - irisR * 0.22f, eyeY - irisR * 0.28f))
        // Small secondary catchlight
        drawCircle(Color.White, radius = irisR * 0.13f,
            center = Offset(ex + irisR * 0.22f, eyeY + irisR * 0.10f))

        // Upper eyelid lines
        drawLine(Color(0xFF2A1A0E).copy(alpha = 0.9f),
            Offset(ex - eyeRX, eyeY),
            Offset(ex - eyeRX * 0.35f, eyeY - eyeRY * 1.1f),
            strokeWidth = 3.5f, cap = StrokeCap.Round)
        drawLine(Color(0xFF2A1A0E).copy(alpha = 0.9f),
            Offset(ex - eyeRX * 0.35f, eyeY - eyeRY * 1.1f),
            Offset(ex + eyeRX * 0.45f, eyeY - eyeRY * 1.1f),
            strokeWidth = 3.5f, cap = StrokeCap.Round)
        drawLine(Color(0xFF2A1A0E).copy(alpha = 0.9f),
            Offset(ex + eyeRX * 0.45f, eyeY - eyeRY * 1.1f),
            Offset(ex + eyeRX, eyeY),
            strokeWidth = 3.5f, cap = StrokeCap.Round)

        // Girl: upper lashes
        if (gender == AvatarGender.GIRL) {
            listOf(-0.8f, -0.35f, 0.15f, 0.65f).forEach { lx ->
                val lashBaseX = ex + lx * eyeRX
                val lashBaseY = eyeY - eyeRY * (0.9f + 0.15f * (1f - abs(lx)))
                drawLine(Color(0xFF1A0A05),
                    Offset(lashBaseX, lashBaseY),
                    Offset(lashBaseX + lx * 5f, lashBaseY - 8f),
                    strokeWidth = 2.5f, cap = StrokeCap.Round)
            }
        }

        // Subtle lower lash hint
        drawLine(Color(0xFF2A1A0E).copy(alpha = 0.35f),
            Offset(ex - eyeRX * 0.55f, eyeY + eyeRY * 0.86f),
            Offset(ex + eyeRX * 0.55f, eyeY + eyeRY * 0.86f),
            strokeWidth = 2f, cap = StrokeCap.Round)
    }

    // ── NOSE (cute button-style) ───────────────────────────────────────────────
    val noseY = eyeY + eyeRY * 3.65f
    // Bridge line
    drawLine(skinTone.darken(0.1f),
        Offset(cx - eyeRX * 0.18f, eyeY + eyeRY * 1.6f),
        Offset(cx - eyeRX * 0.18f, noseY - eyeRY * 0.1f),
        strokeWidth = 2.2f, cap = StrokeCap.Round)
    // Left nostril arc
    drawArc(skinTone.darken(0.13f),
        startAngle = 180f, sweepAngle = -175f, useCenter = false,
        topLeft = Offset(cx - eyeRX * 0.56f, noseY - eyeRY * 0.36f),
        size = androidx.compose.ui.geometry.Size(eyeRX * 0.4f, eyeRY * 0.4f),
        style = Stroke(width = 2.5f))
    // Right nostril arc
    drawArc(skinTone.darken(0.13f),
        startAngle = 0f, sweepAngle = 175f, useCenter = false,
        topLeft = Offset(cx + eyeRX * 0.16f, noseY - eyeRY * 0.36f),
        size = androidx.compose.ui.geometry.Size(eyeRX * 0.4f, eyeRY * 0.4f),
        style = Stroke(width = 2.5f))

    // ── MOUTH (warm smile) ────────────────────────────────────────────────────
    val mouthY    = noseY + eyeRY * 2.4f
    val mouthW    = eyeOffX * 0.88f
    val lipColor  = if (gender == AvatarGender.GIRL) Color(0xFFD4607A) else Color(0xFFBE7070)
    val smileDepth = eyeRY * (if (gender == AvatarGender.GIRL) 1.55f else 1.25f)

    // Smile — three segments
    drawLine(lipColor,
        Offset(cx - mouthW, mouthY + smileDepth * 0.22f),
        Offset(cx - mouthW * 0.45f, mouthY + smileDepth),
        strokeWidth = 3.5f, cap = StrokeCap.Round)
    drawLine(lipColor,
        Offset(cx - mouthW * 0.45f, mouthY + smileDepth),
        Offset(cx + mouthW * 0.45f, mouthY + smileDepth),
        strokeWidth = 3.5f, cap = StrokeCap.Round)
    drawLine(lipColor,
        Offset(cx + mouthW * 0.45f, mouthY + smileDepth),
        Offset(cx + mouthW, mouthY + smileDepth * 0.22f),
        strokeWidth = 3.5f, cap = StrokeCap.Round)
    // Upper lip line
    drawLine(lipColor.copy(alpha = 0.45f),
        Offset(cx - mouthW * 0.68f, mouthY + smileDepth * 0.14f),
        Offset(cx + mouthW * 0.68f, mouthY + smileDepth * 0.14f),
        strokeWidth = 2.5f, cap = StrokeCap.Round)
    // Girl: subtle lip gloss
    if (gender == AvatarGender.GIRL) {
        drawOval(Color.White.copy(alpha = 0.22f),
            topLeft = Offset(cx - mouthW * 0.27f, mouthY + smileDepth * 0.24f),
            size = androidx.compose.ui.geometry.Size(mouthW * 0.54f, eyeRY * 0.42f))
    }

    // ── CHEEK BLUSH ───────────────────────────────────────────────────────────
    val blushAlpha = if (gender == AvatarGender.GIRL) 0.52f else 0.33f
    val faceDetailExtra = faceDetailItem?.id?.contains("extra_blush") == true
    listOf(-1f, 1f).forEach { side ->
        val bx = cx + side * eyeOffX * 0.78f
        val by = eyeY + eyeRY * 2.55f
        drawOval(Color(0xFFFFB3BA).copy(alpha = if (faceDetailExtra) blushAlpha + 0.22f else blushAlpha),
            topLeft = Offset(bx - eyeRX * 1.1f, by - eyeRY * 0.68f),
            size = androidx.compose.ui.geometry.Size(eyeRX * 2.2f, eyeRY * 1.22f))

        // Face detail overlays
        if (faceDetailItem?.id?.contains("freckle") == true) {
            listOf(-0.4f to -0.22f, -0.08f to 0.08f, 0.32f to -0.12f).forEach { (dx, dy) ->
                drawCircle(skinTone.darken(0.22f), radius = 3.5f,
                    center = Offset(bx + dx * eyeRX, by + dy * eyeRY))
            }
        }
        if (faceDetailItem?.id?.contains("star") == true) {
            drawCircle(Color(0xFFFFD700).copy(alpha = 0.88f), radius = 5.5f,
                center = Offset(bx, by + eyeRY * 0.32f))
            drawCircle(Color.White.copy(alpha = 0.7f), radius = 2.2f,
                center = Offset(bx, by + eyeRY * 0.32f))
        }
        if (faceDetailItem?.id?.contains("heart") == true) {
            drawCircle(Color(0xFFE91E63).copy(alpha = 0.82f), radius = 5.0f,
                center = Offset(bx, by + eyeRY * 0.32f))
            drawCircle(Color.White.copy(alpha = 0.6f), radius = 2f,
                center = Offset(bx, by + eyeRY * 0.32f))
        }
    }

    // ── BODY / TORSO (curved bezier sides, gender-aware) ──────────────────────
    val shoulderY = neckBot
    val waistY    = h * 0.575f
    val hipY      = h * 0.635f
    val shoulderW = if (gender == AvatarGender.BOY) w * 0.56f else w * 0.50f
    val waistW    = if (gender == AvatarGender.BOY) w * 0.44f else w * 0.40f
    val hipW      = if (gender == AvatarGender.BOY) w * 0.46f else w * 0.50f

    val torsoPath = Path().apply {
        moveTo(cx - shoulderW / 2f, shoulderY)
        cubicTo(cx - shoulderW / 2f - w * 0.025f, shoulderY + h * 0.025f,
            cx - waistW / 2f - w * 0.01f,  waistY  - h * 0.015f,
            cx - waistW / 2f, waistY)
        cubicTo(cx - waistW / 2f, waistY + h * 0.018f,
            cx - hipW / 2f,  hipY - h * 0.01f,
            cx - hipW / 2f,  hipY)
        lineTo(cx + hipW / 2f, hipY)
        cubicTo(cx + hipW / 2f,  hipY - h * 0.01f,
            cx + waistW / 2f, waistY + h * 0.018f,
            cx + waistW / 2f, waistY)
        cubicTo(cx + waistW / 2f + w * 0.01f, waistY - h * 0.015f,
            cx + shoulderW / 2f + w * 0.025f, shoulderY + h * 0.025f,
            cx + shoulderW / 2f, shoulderY)
        close()
    }
    // Base shirt colour (mostly covered by the outfit layer)
    drawPath(torsoPath, Color(0xFF7AA3FF).copy(alpha = 0.5f))

    // ── ARMS (tapered, slightly outward-angled) ────────────────────────────────
    val upperArmW = w * 0.1f
    val foreArmW  = w * 0.088f
    val armLen    = h * 0.265f
    listOf(-1f, 1f).forEach { side ->
        val axS = cx + side * (shoulderW / 2f - w * 0.015f)
        val axE = axS + side * w * 0.06f
        val ayE = shoulderY + armLen

        val armPath = Path().apply {
            moveTo(axS - side * upperArmW * 0.45f, shoulderY)
            lineTo(axS + side * upperArmW * 0.45f, shoulderY + h * 0.008f)
            cubicTo(axS + side * upperArmW * 0.48f, shoulderY + armLen * 0.48f,
                axE + side * foreArmW * 0.48f,  ayE - armLen * 0.28f,
                axE + side * foreArmW * 0.40f,  ayE)
            lineTo(axE - side * foreArmW * 0.40f, ayE)
            cubicTo(axE - side * foreArmW * 0.48f, ayE - armLen * 0.28f,
                axS - side * upperArmW * 0.48f, shoulderY + armLen * 0.48f,
                axS - side * upperArmW * 0.45f, shoulderY)
            close()
        }
        drawPath(armPath, skinTone)
        // Subtle arm shading
        drawLine(skinTone.darken(0.07f),
            Offset(axE + side * foreArmW * 0.28f, ayE - armLen * 0.16f),
            Offset(axE + side * foreArmW * 0.28f, ayE - h * 0.01f),
            strokeWidth = 3f, cap = StrokeCap.Round)

        // Hand (rounded blob)
        val handCx = axE
        val handCy = ayE + foreArmW * 0.58f
        drawCircle(skinTone, radius = foreArmW * 0.7f, center = Offset(handCx, handCy))
        // Four finger bumps
        listOf(-0.42f, -0.14f, 0.14f, 0.42f).forEach { fx ->
            drawCircle(skinTone, radius = foreArmW * 0.24f,
                center = Offset(handCx + fx * foreArmW, handCy - foreArmW * 0.55f))
        }
        // Thumb
        drawCircle(skinTone, radius = foreArmW * 0.22f,
            center = Offset(handCx + side * foreArmW * 0.72f, handCy - foreArmW * 0.22f))
    }

    // ── LEGS (rounded with knee highlight) ────────────────────────────────────
    val legW   = w * 0.145f
    val legLen = h * 0.27f
    val legGap = w * 0.03f
    val legColor = Color(0xFF2B4DAB)
    listOf(-1f, 1f).forEach { side ->
        val lx = cx + side * (legW * 0.5f + legGap * 0.5f)
        val legPath = Path().apply {
            moveTo(lx - legW * 0.5f, hipY)
            lineTo(lx + legW * 0.5f, hipY)
            cubicTo(lx + legW * 0.50f, hipY + legLen * 0.45f,
                lx + legW * 0.47f, hipY + legLen * 0.72f,
                lx + legW * 0.46f, hipY + legLen)
            lineTo(lx - legW * 0.46f, hipY + legLen)
            cubicTo(lx - legW * 0.47f, hipY + legLen * 0.72f,
                lx - legW * 0.50f, hipY + legLen * 0.45f,
                lx - legW * 0.5f, hipY)
            close()
        }
        drawPath(legPath, legColor)
        // Knee highlight
        drawOval(Color.White.copy(alpha = 0.08f),
            topLeft = Offset(lx - legW * 0.27f, hipY + legLen * 0.43f),
            size = androidx.compose.ui.geometry.Size(legW * 0.54f, legLen * 0.18f))
        // Inseam stitching line
        drawLine(legColor.darken(0.12f),
            Offset(lx, hipY + legLen * 0.08f),
            Offset(lx, hipY + legLen * 0.88f),
            strokeWidth = 2f)

        // Default shoe / foot shape (covered by AvatarShoesLayer when equipped)
        val shoeW = legW * 1.42f
        val shoeH = legLen * 0.18f
        val shoeOffX = if (side < 0f) shoeW * 0.60f else shoeW * 0.40f
        drawRoundRect(Color(0xFF1A1A1A),
            topLeft = Offset(lx - shoeOffX, hipY + legLen - shoeH * 0.32f),
            size = androidx.compose.ui.geometry.Size(shoeW, shoeH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(shoeH * 0.5f))
        drawRoundRect(Color.White.copy(alpha = 0.1f),
            topLeft = Offset(lx - shoeOffX + 4f, hipY + legLen - shoeH * 0.28f),
            size = androidx.compose.ui.geometry.Size(shoeW * 0.52f, shoeH * 0.38f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(shoeH * 0.32f))
    }
}

private fun Color.darken(by: Float): Color =
    Color(red * (1f - by), green * (1f - by), blue * (1f - by), alpha)

// ─────────────────────────────────────────────────────────────────────────────
//  Shoes Layer — drawn over the character's feet area
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AvatarShoesLayer(
    shoes: AvatarLayerItem,
    gender: AvatarGender,
    modifier: Modifier = Modifier
) {
    val shoeColor = shoes.tintColor?.let { Color(it) } ?: Color(0xFF222222)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val hipY  = h * 0.635f
        val legLen = h * 0.27f
        val legW  = w * 0.145f
        val legGap = w * 0.03f

        listOf(-1f, 1f).forEach { side ->
            val lx = cx + side * (legW * 0.5f + legGap * 0.5f)
            val shoeW = legW * 1.45f
            val shoeH = legLen * 0.20f
            val shoeY = hipY + legLen - shoeH * 0.35f
            val offX  = if (side < 0f) shoeW * 0.60f else shoeW * 0.40f

            when {
                shoes.id.contains("boot") -> {
                    // Boot shaft
                    drawRoundRect(shoeColor,
                        topLeft = Offset(lx - legW * 0.48f, shoeY - shoeH * 1.8f),
                        size = androidx.compose.ui.geometry.Size(legW * 0.96f, shoeH * 1.9f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f))
                    // Boot sole
                    drawRoundRect(shoeColor.darken(0.20f),
                        topLeft = Offset(lx - offX, shoeY),
                        size = androidx.compose.ui.geometry.Size(shoeW, shoeH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(shoeH * 0.5f))
                    // Boot highlight
                    drawRoundRect(Color.White.copy(alpha = 0.12f),
                        topLeft = Offset(lx - legW * 0.38f, shoeY - shoeH * 1.6f),
                        size = androidx.compose.ui.geometry.Size(legW * 0.42f, shoeH * 0.55f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f))
                }
                shoes.id.contains("sneaker") || shoes.id.contains("trainer") || shoes.id.contains("velcro") -> {
                    // Sneaker upper
                    drawRoundRect(shoeColor,
                        topLeft = Offset(lx - offX, shoeY),
                        size = androidx.compose.ui.geometry.Size(shoeW, shoeH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(shoeH * 0.48f))
                    // White rubber sole stripe
                    drawRoundRect(Color.White.copy(alpha = 0.72f),
                        topLeft = Offset(lx - offX + 2f, shoeY + shoeH * 0.72f),
                        size = androidx.compose.ui.geometry.Size(shoeW - 4f, shoeH * 0.30f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(shoeH * 0.18f))
                    // Lace line
                    drawLine(Color.White.copy(alpha = 0.55f),
                        Offset(lx - shoeW * 0.08f, shoeY + shoeH * 0.32f),
                        Offset(lx + shoeW * 0.08f, shoeY + shoeH * 0.32f),
                        strokeWidth = 2.5f, cap = StrokeCap.Round)
                    // Toe highlight
                    drawOval(Color.White.copy(alpha = 0.14f),
                        topLeft = Offset(lx - offX + 4f, shoeY + shoeH * 0.08f),
                        size = androidx.compose.ui.geometry.Size(shoeW * 0.42f, shoeH * 0.5f))
                }
                else -> {
                    // Classic rounded shoe
                    drawRoundRect(shoeColor,
                        topLeft = Offset(lx - offX, shoeY),
                        size = androidx.compose.ui.geometry.Size(shoeW, shoeH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(shoeH * 0.5f))
                    // Toe cap
                    drawOval(shoeColor.copy(alpha = 0.55f),
                        topLeft = Offset(lx + if (side < 0f) -offX else -offX * 0.12f, shoeY - shoeH * 0.1f),
                        size = androidx.compose.ui.geometry.Size(shoeW * 0.42f, shoeH * 0.72f))
                    drawRoundRect(Color.White.copy(alpha = 0.1f),
                        topLeft = Offset(lx - offX + 4f, shoeY + 3f),
                        size = androidx.compose.ui.geometry.Size(shoeW * 0.5f, shoeH * 0.36f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(shoeH * 0.3f))
                }
            }
        }
    }
}

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
        val hipW      = if (gender == AvatarGender.BOY) w * 0.44f else w * 0.50f
        val waistW    = if (gender == AvatarGender.BOY) w * 0.40f else w * 0.36f
        val shoulderY = h * 0.385f
        val waistY    = h * 0.62f
        val hipY      = h * 0.72f

        // ── Torso clothing — curved sides ────────────────────────────────
        val torsoPath = Path().apply {
            moveTo(cx - shoulderW / 2f, shoulderY)
            cubicTo(cx - shoulderW / 2f - w * 0.02f, shoulderY + h * 0.02f,
                cx - waistW / 2f, waistY - h * 0.01f,
                cx - waistW / 2f, waistY)
            cubicTo(cx - waistW / 2f, waistY + h * 0.01f,
                cx - hipW / 2f, hipY - h * 0.01f,
                cx - hipW / 2f, hipY)
            lineTo(cx + hipW / 2f, hipY)
            cubicTo(cx + hipW / 2f, hipY - h * 0.01f,
                cx + waistW / 2f, waistY + h * 0.01f,
                cx + waistW / 2f, waistY)
            cubicTo(cx + waistW / 2f, waistY - h * 0.01f,
                cx + shoulderW / 2f + w * 0.02f, shoulderY + h * 0.02f,
                cx + shoulderW / 2f, shoulderY)
            close()
        }
        drawPath(torsoPath, tint)

        // Subtle clothing gradient
        drawPath(torsoPath, Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.12f), Color.Black.copy(alpha = 0.08f)),
            startY = shoulderY, endY = hipY))

        // ── Collar ────────────────────────────────────────────────────────
        when {
            outfit.id.contains("ninja") || outfit.id.contains("astronaut") -> {
                // High turtleneck / suit collar
                drawRoundRect(tint.darken(0.12f),
                    topLeft = Offset(cx - w * 0.1f, shoulderY - h * 0.02f),
                    size = androidx.compose.ui.geometry.Size(w * 0.2f, h * 0.07f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f))
            }
            outfit.id.contains("sport") || outfit.id.contains("trainer") -> {
                // Sporty zip collar
                drawLine(tint.darken(0.18f),
                    Offset(cx, shoulderY + h * 0.005f),
                    Offset(cx, shoulderY + h * 0.08f),
                    strokeWidth = w * 0.025f, cap = StrokeCap.Round)
                // Collar band
                drawLine(tint.darken(0.10f),
                    Offset(cx - w * 0.1f, shoulderY),
                    Offset(cx + w * 0.1f, shoulderY),
                    strokeWidth = w * 0.035f, cap = StrokeCap.Round)
            }
            else -> {
                // Classic V-neck collar
                drawLine(tint.darken(0.15f),
                    Offset(cx - w * 0.075f, shoulderY),
                    Offset(cx, shoulderY + h * 0.065f),
                    strokeWidth = 5f, cap = StrokeCap.Round)
                drawLine(tint.darken(0.15f),
                    Offset(cx, shoulderY + h * 0.065f),
                    Offset(cx + w * 0.075f, shoulderY),
                    strokeWidth = 5f, cap = StrokeCap.Round)
            }
        }

        // ── Sleeves ────────────────────────────────────────────────────────
        listOf(-1f, 1f).forEach { side ->
            val sleeveX = cx + side * (shoulderW / 2f)
            val sleeveEnd = Offset(sleeveX + side * w * 0.065f, shoulderY + h * 0.15f)
            drawLine(tint.darken(0.07f),
                Offset(sleeveX, shoulderY),
                sleeveEnd,
                strokeWidth = w * 0.115f,
                cap = StrokeCap.Round)
            // Sleeve seam
            drawLine(tint.darken(0.13f),
                Offset(sleeveX, shoulderY + h * 0.005f),
                Offset(sleeveX + side * w * 0.04f, shoulderY + h * 0.09f),
                strokeWidth = 2.5f, cap = StrokeCap.Round)
        }

        // ── Clothing details ───────────────────────────────────────────────
        when {
            outfit.id.contains("sport") || outfit.id.contains("trainer") -> {
                // Side stripe on torso
                listOf(-1f, 1f).forEach { side ->
                    drawLine(Color.White.copy(alpha = 0.35f),
                        Offset(cx + side * shoulderW * 0.32f, shoulderY + h * 0.04f),
                        Offset(cx + side * hipW * 0.32f, hipY - h * 0.02f),
                        strokeWidth = 5f, cap = StrokeCap.Round)
                }
            }
            outfit.id.contains("ninja") -> {
                // Belt / sash
                drawLine(tint.darken(0.25f),
                    Offset(cx - waistW * 0.55f, waistY + h * 0.01f),
                    Offset(cx + waistW * 0.55f, waistY + h * 0.01f),
                    strokeWidth = 10f, cap = StrokeCap.Butt)
                drawLine(Color.White.copy(alpha = 0.3f),
                    Offset(cx - waistW * 0.3f, waistY + h * 0.01f),
                    Offset(cx + waistW * 0.3f, waistY + h * 0.01f),
                    strokeWidth = 4f, cap = StrokeCap.Round)
            }
            outfit.id.contains("astronaut") -> {
                // Suit panel lines
                drawLine(tint.darken(0.12f),
                    Offset(cx, shoulderY + h * 0.09f),
                    Offset(cx, hipY - h * 0.02f),
                    strokeWidth = 3f, cap = StrokeCap.Round)
                drawLine(tint.darken(0.12f),
                    Offset(cx - waistW * 0.38f, waistY - h * 0.04f),
                    Offset(cx + waistW * 0.38f, waistY - h * 0.04f),
                    strokeWidth = 3f, cap = StrokeCap.Round)
                // Badge circle
                drawCircle(Color.White.copy(alpha = 0.3f), radius = w * 0.06f,
                    center = Offset(cx - w * 0.1f, shoulderY + h * 0.1f))
            }
            else -> {
                // Classic shirt buttons
                listOf(0.3f, 0.52f, 0.72f).forEach { yRatio ->
                    drawCircle(Color.White.copy(alpha = 0.55f), radius = 3.5f,
                        center = Offset(cx, shoulderY + (hipY - shoulderY) * yRatio))
                }
            }
        }

        // ── Premium glow outline ──────────────────────────────────────────
        if (outfit.isPremium) {
            drawPath(torsoPath,
                Brush.linearGradient(listOf(
                    Color(0xFFFFD700).copy(alpha = 0.18f),
                    Color.Transparent
                )))
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
        val headR  = w * 0.23f
        val headCy = h * 0.19f

        when {
            hair.id.contains("spiky") -> {
                // Spiky hair — multiple upward triangular spikes
                val capPath = Path().apply {
                    moveTo(cx - headR * 1.05f, headCy + headR * 0.1f)
                    quadraticBezierTo(cx - headR * 0.9f, headCy - headR * 1.3f,
                        cx, headCy - headR * 1.1f)
                    quadraticBezierTo(cx + headR * 0.9f, headCy - headR * 1.3f,
                        cx + headR * 1.05f, headCy + headR * 0.1f)
                    close()
                }
                drawPath(capPath, hairColor)
                // Spikes
                listOf(-0.55f, -0.15f, 0.25f, 0.65f).forEach { xOff ->
                    val spikeBase = cx + xOff * headR * 1.4f
                    drawLine(hairColor,
                        Offset(spikeBase, headCy - headR * 1.05f),
                        Offset(spikeBase + headR * 0.18f, headCy - headR * 1.72f),
                        strokeWidth = w * 0.06f, cap = StrokeCap.Round)
                }
                // Side-cuts
                listOf(-1f, 1f).forEach { side ->
                    drawRoundRect(hairColor,
                        topLeft = Offset(cx + side * headR * 0.72f, headCy - headR * 0.08f),
                        size = androidx.compose.ui.geometry.Size(headR * 0.3f, headR * 0.65f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f))
                }
            }
            hair.id.contains("short") -> {
                // Short hair cap with side detail
                val capPath = Path().apply {
                    moveTo(cx - headR * 1.05f, headCy + headR * 0.08f)
                    quadraticBezierTo(cx - headR * 0.9f, headCy - headR * 1.38f,
                        cx, headCy - headR * 1.15f)
                    quadraticBezierTo(cx + headR * 0.9f, headCy - headR * 1.38f,
                        cx + headR * 1.05f, headCy + headR * 0.08f)
                    close()
                }
                drawPath(capPath, hairColor)
                // Side parting line
                drawLine(hairColor.darken(0.18f),
                    Offset(cx - headR * 0.05f, headCy - headR * 1.12f),
                    Offset(cx - headR * 0.3f, headCy - headR * 0.4f),
                    strokeWidth = 4f, cap = StrokeCap.Round)
                // Side burns
                listOf(-1f, 1f).forEach { side ->
                    drawRoundRect(hairColor,
                        topLeft = Offset(cx + side * headR * 0.73f, headCy - headR * 0.12f),
                        size = androidx.compose.ui.geometry.Size(headR * 0.29f, headR * 0.68f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f))
                }
            }
            hair.id.contains("bun") -> {
                // High bun: base cap + top bun shape
                val capPath = Path().apply {
                    moveTo(cx - headR * 1.05f, headCy + headR * 0.08f)
                    quadraticBezierTo(cx - headR * 0.9f, headCy - headR * 1.3f,
                        cx, headCy - headR * 1.1f)
                    quadraticBezierTo(cx + headR * 0.9f, headCy - headR * 1.3f,
                        cx + headR * 1.05f, headCy + headR * 0.08f)
                    close()
                }
                drawPath(capPath, hairColor)
                // Bun circle on top
                drawCircle(hairColor, radius = headR * 0.42f,
                    center = Offset(cx, headCy - headR * 1.42f))
                drawCircle(hairColor.darken(0.12f), radius = headR * 0.42f,
                    center = Offset(cx, headCy - headR * 1.42f),
                    style = Stroke(width = 3f))
                // Bun donut highlight
                drawCircle(Color.White.copy(alpha = 0.15f), radius = headR * 0.20f,
                    center = Offset(cx - headR * 0.1f, headCy - headR * 1.55f))
                // Hair tie
                drawCircle(Color(0xFFFF6B9D), radius = 5f,
                    center = Offset(cx, headCy - headR * 1.06f))
            }
            hair.id.contains("bob") -> {
                // Bob cut — full cap with flat bottom at jaw level
                val bobPath = Path().apply {
                    moveTo(cx - headR * 1.12f, headCy + headR * 0.55f)
                    quadraticBezierTo(cx - headR * 1.1f, headCy - headR * 1.4f,
                        cx, headCy - headR * 1.18f)
                    quadraticBezierTo(cx + headR * 1.1f, headCy - headR * 1.4f,
                        cx + headR * 1.12f, headCy + headR * 0.55f)
                    // Flat-ish bottom
                    quadraticBezierTo(cx, headCy + headR * 0.72f,
                        cx - headR * 1.12f, headCy + headR * 0.55f)
                    close()
                }
                drawPath(bobPath, hairColor)
                // Subtle inner parting
                drawLine(hairColor.darken(0.15f),
                    Offset(cx, headCy - headR * 1.15f),
                    Offset(cx - headR * 0.2f, headCy - headR * 0.3f),
                    strokeWidth = 3.5f, cap = StrokeCap.Round)
            }
            hair.id.contains("wavy") -> {
                // Wavy long hair
                val capPath = Path().apply {
                    moveTo(cx - headR * 1.05f, headCy + headR * 0.08f)
                    quadraticBezierTo(cx - headR * 0.9f, headCy - headR * 1.38f,
                        cx, headCy - headR * 1.15f)
                    quadraticBezierTo(cx + headR * 0.9f, headCy - headR * 1.38f,
                        cx + headR * 1.05f, headCy + headR * 0.08f)
                    close()
                }
                drawPath(capPath, hairColor)
                // Wavy side strands
                listOf(-1f, 1f).forEach { side ->
                    val strandPath = Path().apply {
                        moveTo(cx + side * headR * 0.88f, headCy + headR * 0.3f)
                        cubicTo(cx + side * headR * 1.28f, headCy + headR * 0.9f,
                            cx + side * headR * 0.95f, headCy + headR * 1.6f,
                            cx + side * headR * 1.15f, headCy + headR * 2.3f)
                        cubicTo(cx + side * headR * 1.3f,  headCy + headR * 3.0f,
                            cx + side * headR * 0.9f,  headCy + headR * 3.4f,
                            cx + side * headR * 0.85f, headCy + headR * 3.6f)
                        cubicTo(cx + side * headR * 0.72f, headCy + headR * 2.5f,
                            cx + side * headR * 0.82f, headCy + headR * 1.5f,
                            cx + side * headR * 0.72f, headCy + headR * 0.4f)
                        close()
                    }
                    drawPath(strandPath, hairColor)
                    // Wave crest highlight
                    drawLine(hairColor.copy(alpha = 0.35f),
                        Offset(cx + side * headR * 0.92f, headCy + headR * 1.0f),
                        Offset(cx + side * headR * 1.1f, headCy + headR * 1.8f),
                        strokeWidth = 4f, cap = StrokeCap.Round)
                }
            }
            hair.id.contains("long") -> {
                // Long straight hair
                val capPath = Path().apply {
                    moveTo(cx - headR * 1.05f, headCy + headR * 0.08f)
                    quadraticBezierTo(cx - headR * 0.9f, headCy - headR * 1.38f,
                        cx, headCy - headR * 1.15f)
                    quadraticBezierTo(cx + headR * 0.9f, headCy - headR * 1.38f,
                        cx + headR * 1.05f, headCy + headR * 0.08f)
                    close()
                }
                drawPath(capPath, hairColor)
                // Long straight side strands
                listOf(-1f, 1f).forEach { side ->
                    val strandPath = Path().apply {
                        moveTo(cx + side * headR * 0.90f, headCy + headR * 0.35f)
                        quadraticBezierTo(cx + side * headR * 1.2f, headCy + headR * 1.8f,
                            cx + side * headR * 0.95f, headCy + headR * 3.3f)
                        quadraticBezierTo(cx + side * headR * 1.1f, headCy + headR * 3.55f,
                            cx + side * headR * 0.85f, headCy + headR * 3.65f)
                        quadraticBezierTo(cx + side * headR * 0.68f, headCy + headR * 2.6f,
                            cx + side * headR * 0.74f, headCy + headR * 0.48f)
                        close()
                    }
                    drawPath(strandPath, hairColor)
                }
            }
            hair.id.contains("ponytail") -> {
                // Base cap
                val capPath = Path().apply {
                    moveTo(cx - headR * 1.05f, headCy + headR * 0.08f)
                    quadraticBezierTo(cx - headR * 0.9f, headCy - headR * 1.38f,
                        cx, headCy - headR * 1.15f)
                    quadraticBezierTo(cx + headR * 0.9f, headCy - headR * 1.38f,
                        cx + headR * 1.05f, headCy + headR * 0.08f)
                    close()
                }
                drawPath(capPath, hairColor)
                // Ponytail
                val ponytailPath = Path().apply {
                    moveTo(cx + headR * 0.30f, headCy - headR * 0.82f)
                    quadraticBezierTo(cx + headR * 1.62f, headCy,
                        cx + headR * 1.32f, headCy + headR * 1.85f)
                    quadraticBezierTo(cx + headR * 1.52f, headCy + headR * 2.05f,
                        cx + headR * 1.02f, headCy + headR * 1.95f)
                    quadraticBezierTo(cx + headR * 1.1f, headCy + headR * 0.32f,
                        cx + headR * 0.10f, headCy - headR * 0.72f)
                    close()
                }
                drawPath(ponytailPath, hairColor)
                // Strand highlight in ponytail
                drawLine(hairColor.copy(alpha = 0.3f),
                    Offset(cx + headR * 0.85f, headCy + headR * 0.3f),
                    Offset(cx + headR * 1.2f, headCy + headR * 1.6f),
                    strokeWidth = 4f, cap = StrokeCap.Round)
                // Hair tie
                drawCircle(Color(0xFFFF6B9D), radius = 7f,
                    center = Offset(cx + headR * 0.88f, headCy - headR * 0.50f))
                drawCircle(Color.White.copy(alpha = 0.4f), radius = 3f,
                    center = Offset(cx + headR * 0.88f, headCy - headR * 0.50f))
            }
            hair.id.contains("curly") -> {
                // Curly puff — cluster of overlapping circles
                val curlPositions = listOf(
                    Offset(cx,            headCy - headR * 1.38f) to headR * 0.58f,
                    Offset(cx - headR * 0.60f, headCy - headR * 1.22f) to headR * 0.48f,
                    Offset(cx + headR * 0.60f, headCy - headR * 1.22f) to headR * 0.48f,
                    Offset(cx - headR * 0.98f, headCy - headR * 0.70f) to headR * 0.43f,
                    Offset(cx + headR * 0.98f, headCy - headR * 0.70f) to headR * 0.43f,
                    Offset(cx - headR * 1.02f, headCy - headR * 0.10f) to headR * 0.38f,
                    Offset(cx + headR * 1.02f, headCy - headR * 0.10f) to headR * 0.38f
                )
                curlPositions.forEach { (pos, r) ->
                    drawCircle(hairColor, radius = r, center = pos)
                    // Curl highlight
                    drawCircle(Color.White.copy(alpha = 0.10f), radius = r * 0.5f,
                        center = Offset(pos.x - r * 0.25f, pos.y - r * 0.25f))
                }
            }
            else -> {
                // Fallback: clean short cap
                val fallbackPath = Path().apply {
                    moveTo(cx - headR * 1.05f, headCy + headR * 0.08f)
                    quadraticBezierTo(cx - headR * 0.9f, headCy - headR * 1.3f,
                        cx, headCy - headR * 1.1f)
                    quadraticBezierTo(cx + headR * 0.9f, headCy - headR * 1.3f,
                        cx + headR * 1.05f, headCy + headR * 0.08f)
                    close()
                }
                drawPath(fallbackPath, hairColor)
            }
        }

        // Highlight streak on all hair types
        drawLine(Color.White.copy(alpha = 0.28f),
            Offset(cx - headR * 0.18f, headCy - headR * 1.08f),
            Offset(cx + headR * 0.22f, headCy - headR * 0.32f),
            strokeWidth = 5f, cap = StrokeCap.Round)
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
        val headR  = w * 0.23f
        val headCy = h * 0.19f
        val eyeY   = headCy - headR * 0.14f
        val eyeOffX = headR * 0.60f

        when {
            accessory.id.contains("headband") -> {
                // Ninja headband — curved band with metal plate
                drawLine(tint,
                    Offset(cx - headR * 1.1f, headCy - headR * 0.33f),
                    Offset(cx + headR * 1.1f, headCy - headR * 0.33f),
                    strokeWidth = 14f, cap = StrokeCap.Round)
                // Metal plate
                drawRoundRect(tint.darken(0.12f),
                    topLeft = Offset(cx - 19f, headCy - headR * 0.47f),
                    size = androidx.compose.ui.geometry.Size(38f, 24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f))
                // Engraved symbol
                drawLine(Color.White.copy(alpha = 0.45f),
                    Offset(cx - 8f, headCy - headR * 0.38f),
                    Offset(cx + 8f, headCy - headR * 0.28f),
                    strokeWidth = 2f)
                // Trailing knot tails
                listOf(-1f, 1f).forEach { side ->
                    drawLine(tint.darken(0.1f),
                        Offset(cx + side * headR * 1.1f, headCy - headR * 0.33f),
                        Offset(cx + side * headR * 1.3f, headCy + headR * 0.22f),
                        strokeWidth = 8f, cap = StrokeCap.Round)
                }
            }
            accessory.id.contains("tiara") || accessory.id.contains("crown") -> {
                // Princess tiara / crown
                val crownBase = headCy - headR * 1.35f
                // Base band
                drawRoundRect(tint,
                    topLeft = Offset(cx - headR * 0.88f, crownBase),
                    size = androidx.compose.ui.geometry.Size(headR * 1.76f, headR * 0.22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(headR * 0.1f))
                // Three crown points
                listOf(-0.6f, 0f, 0.6f).forEachIndexed { idx, xOff ->
                    val pointH = if (idx == 1) headR * 0.52f else headR * 0.36f
                    drawLine(tint,
                        Offset(cx + xOff * headR * 0.88f, crownBase),
                        Offset(cx + xOff * headR * 0.88f, crownBase - pointH),
                        strokeWidth = if (idx == 1) 10f else 8f, cap = StrokeCap.Round)
                    // Gem at tip
                    val gemColor = listOf(Color(0xFFE91E63), Color(0xFFFFD700), Color(0xFF2196F3))[idx]
                    drawCircle(gemColor, radius = if (idx == 1) 7f else 5.5f,
                        center = Offset(cx + xOff * headR * 0.88f, crownBase - pointH - 2f))
                    drawCircle(Color.White.copy(alpha = 0.5f), radius = if (idx == 1) 3f else 2.2f,
                        center = Offset(cx + xOff * headR * 0.88f - 2f, crownBase - pointH - 4f))
                }
                // Band sparkles
                listOf(-0.4f, 0.4f).forEach { xOff ->
                    drawCircle(Color.White.copy(alpha = 0.65f), radius = 3.5f,
                        center = Offset(cx + xOff * headR, crownBase + headR * 0.1f))
                }
            }
            accessory.id.contains("glasses") -> {
                // Round glasses
                val glassR = eyeOffX * 0.52f
                val frameThick = 3.5f
                listOf(-1f, 1f).forEach { side ->
                    val ex = cx + side * eyeOffX
                    drawCircle(Color.Transparent, radius = glassR, center = Offset(ex, eyeY),
                        style = Stroke(width = frameThick))
                    drawCircle(tint, radius = glassR, center = Offset(ex, eyeY),
                        style = Stroke(width = frameThick))
                    // Lens tint
                    drawCircle(tint.copy(alpha = 0.12f), radius = glassR - 1f, center = Offset(ex, eyeY))
                    // Lens flare
                    drawLine(Color.White.copy(alpha = 0.4f),
                        Offset(ex - glassR * 0.55f, eyeY - glassR * 0.5f),
                        Offset(ex - glassR * 0.25f, eyeY - glassR * 0.2f),
                        strokeWidth = 2.5f, cap = StrokeCap.Round)
                }
                // Bridge piece
                drawLine(tint,
                    Offset(cx - eyeOffX + glassR, eyeY),
                    Offset(cx + eyeOffX - glassR, eyeY),
                    strokeWidth = frameThick, cap = StrokeCap.Round)
                // Temple arms
                listOf(-1f, 1f).forEach { side ->
                    drawLine(tint,
                        Offset(cx + side * (eyeOffX + glassR - 2f), eyeY),
                        Offset(cx + side * (eyeOffX + glassR + headR * 0.32f), eyeY + headR * 0.05f),
                        strokeWidth = frameThick, cap = StrokeCap.Round)
                }
            }
            accessory.id.contains("bow") -> {
                // Hair bow — two wing lobes + knot
                val bowY = headCy - headR * 1.28f
                val bowX = cx + headR * 0.55f
                listOf(-1f, 1f).forEach { side ->
                    val wingPath = Path().apply {
                        moveTo(bowX, bowY)
                        cubicTo(bowX + side * headR * 0.12f, bowY - headR * 0.32f,
                            bowX + side * headR * 0.55f, bowY - headR * 0.28f,
                            bowX + side * headR * 0.5f, bowY)
                        cubicTo(bowX + side * headR * 0.55f, bowY + headR * 0.28f,
                            bowX + side * headR * 0.12f, bowY + headR * 0.32f,
                            bowX, bowY)
                        close()
                    }
                    drawPath(wingPath, tint)
                    drawPath(wingPath, tint.darken(0.08f),
                        alpha = 1f, style = Stroke(width = 1.5f))
                }
                // Knot circle
                drawCircle(tint.darken(0.15f), radius = 7f, center = Offset(bowX, bowY))
                // Bow shimmer
                drawCircle(Color.White.copy(alpha = 0.3f), radius = 3f,
                    center = Offset(bowX - 2f, bowY - 2f))
            }
            accessory.id.contains("necklace") -> {
                // Star necklace chain + pendant
                val chainY = h * 0.41f
                drawArc(tint.copy(alpha = 0.7f),
                    startAngle = 200f, sweepAngle = 140f, useCenter = false,
                    topLeft = Offset(cx - headR * 0.75f, chainY - headR * 0.5f),
                    size = androidx.compose.ui.geometry.Size(headR * 1.5f, headR * 0.9f),
                    style = Stroke(width = 2.5f))
                // Star pendant
                val pendantCx = cx
                val pendantCy = chainY + headR * 0.22f
                repeat(5) { i ->
                    val angle = (i * 72f - 90f) * (kotlin.math.PI / 180f).toFloat()
                    val inner = (i * 72f + 36f - 90f) * (kotlin.math.PI / 180f).toFloat()
                    val outerR = 8f; val innerR = 3.5f
                    // outer point
                    val ox = pendantCx + outerR * cos(angle)
                    val oy = pendantCy + outerR * sin(angle)
                    // inner point
                    val ix = pendantCx + innerR * cos(inner)
                    val iy = pendantCy + innerR * sin(inner)
                    if (i == 0) {
                        // draw just a filled star using drawPath
                    }
                    drawLine(tint, Offset(ox, oy), Offset(pendantCx, pendantCy), strokeWidth = 3f)
                }
                drawCircle(tint, radius = 8f, center = Offset(pendantCx, pendantCy))
                drawCircle(Color.White.copy(alpha = 0.4f), radius = 3f,
                    center = Offset(pendantCx - 2f, pendantCy - 2f))
            }
            accessory.id.contains("bandana") -> {
                // Bandana tied around head
                val bandY = headCy - headR * 0.28f
                drawLine(tint,
                    Offset(cx - headR * 1.12f, bandY),
                    Offset(cx + headR * 1.12f, bandY),
                    strokeWidth = 18f, cap = StrokeCap.Round)
                // Bandana dot pattern
                listOf(-0.55f, -0.15f, 0.25f, 0.65f).forEach { xOff ->
                    drawCircle(Color.White.copy(alpha = 0.25f), radius = 3f,
                        center = Offset(cx + xOff * headR * 1.2f, bandY))
                }
                // Knot at side
                drawCircle(tint.darken(0.15f), radius = 9f,
                    center = Offset(cx + headR * 1.0f, bandY + headR * 0.1f))
                drawLine(tint.darken(0.1f),
                    Offset(cx + headR * 1.0f, bandY + headR * 0.18f),
                    Offset(cx + headR * 1.15f, bandY + headR * 0.42f),
                    strokeWidth = 7f, cap = StrokeCap.Round)
            }
            accessory.id.contains("wristband") -> {
                // Wristband on the right wrist
                val wristY = h * 0.595f
                val wristX = cx + w * 0.36f
                drawRoundRect(tint,
                    topLeft = Offset(wristX - 14f, wristY - 8f),
                    size = androidx.compose.ui.geometry.Size(22f, 16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f))
                drawLine(Color.White.copy(alpha = 0.35f),
                    Offset(wristX - 10f, wristY),
                    Offset(wristX + 6f, wristY),
                    strokeWidth = 2f, cap = StrokeCap.Round)
            }
            accessory.id.contains("cap") || accessory.id.contains("hat")
                    || accessory.id.contains("explorer") -> {
                // Baseball cap / explorer hat with brim
                val brimY = headCy - headR * 0.52f
                drawRoundRect(tint,
                    topLeft = Offset(cx - headR * 1.18f, brimY),
                    size = androidx.compose.ui.geometry.Size(headR * 2.36f, headR * 0.36f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f))
                // Hat dome
                val domePath = Path().apply {
                    moveTo(cx - headR * 0.96f, brimY)
                    quadraticBezierTo(cx - headR * 0.82f, headCy - headR * 1.6f,
                        cx, headCy - headR * 1.52f)
                    quadraticBezierTo(cx + headR * 0.82f, headCy - headR * 1.6f,
                        cx + headR * 0.96f, brimY)
                    close()
                }
                drawPath(domePath, tint)
                // Cap band
                drawLine(tint.darken(0.18f),
                    Offset(cx - headR * 0.92f, brimY + headR * 0.05f),
                    Offset(cx + headR * 0.92f, brimY + headR * 0.05f),
                    strokeWidth = 5f, cap = StrokeCap.Round)
                // Dome highlight
                drawLine(Color.White.copy(alpha = 0.22f),
                    Offset(cx - headR * 0.3f, headCy - headR * 1.5f),
                    Offset(cx - headR * 0.1f, brimY + headR * 0.1f),
                    strokeWidth = 4f, cap = StrokeCap.Round)
                // Explorer hat: add cord detail
                if (accessory.id.contains("explorer")) {
                    drawLine(Color(0xFF8D6E63),
                        Offset(cx - headR * 0.82f, brimY + headR * 0.18f),
                        Offset(cx + headR * 0.82f, brimY + headR * 0.18f),
                        strokeWidth = 3f)
                }
            }
            accessory.id.contains("mask") -> {
                // Hero eye mask
                listOf(-1f, 1f).forEach { side ->
                    val ex  = cx + side * eyeOffX
                    val maskPath = Path().apply {
                        moveTo(ex - headR * 0.30f, eyeY - headR * 0.22f)
                        lineTo(ex + headR * 0.30f, eyeY - headR * 0.22f)
                        lineTo(ex + headR * 0.36f, eyeY + headR * 0.18f)
                        lineTo(ex - headR * 0.36f, eyeY + headR * 0.18f)
                        close()
                    }
                    drawPath(maskPath, tint)
                    // Eye cut-out
                    drawOval(Color.Transparent,
                        topLeft = Offset(ex - headR * 0.2f, eyeY - headR * 0.15f),
                        size = androidx.compose.ui.geometry.Size(headR * 0.4f, headR * 0.26f))
                }
                // Bridge
                drawLine(tint,
                    Offset(cx - headR * 0.06f, eyeY - headR * 0.02f),
                    Offset(cx + headR * 0.06f, eyeY - headR * 0.02f),
                    strokeWidth = 10f)
            }
            accessory.id.contains("pokeball") || accessory.id.contains("belt") -> {
                // Belt with Pokéball buckle
                drawLine(Color(0xFF4A3728),
                    Offset(cx - headR * 1.42f, h * 0.56f),
                    Offset(cx + headR * 1.42f, h * 0.56f),
                    strokeWidth = 16f, cap = StrokeCap.Round)
                // Buckle
                drawCircle(Color.White, radius = 14f, center = Offset(cx, h * 0.56f))
                drawCircle(Color.Red, radius = 9f, center = Offset(cx, h * 0.56f - 7f))
                drawLine(Color(0xFF333333), Offset(cx - 14f, h * 0.56f),
                    Offset(cx + 14f, h * 0.56f), strokeWidth = 2.5f)
                drawCircle(Color(0xFF333333), radius = 4f, center = Offset(cx, h * 0.56f))
                drawCircle(Color.White, radius = 2.2f, center = Offset(cx, h * 0.56f))
            }
            else -> { /* No fallback accessory */ }
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
            eyeStyleItem = avatarState.activeEyeStyle,
            faceDetailItem = avatarState.activeFaceDetail,
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