package com.kidsroutine.core.common.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand palette ──────────────────────────────────────────────────────────
val PrimaryYellow    = Color(0xFFFFD93D)
val PrimaryOrange    = Color(0xFFFF6B35)
val SecondaryBlue    = Color(0xFF4ECDC4)
val BackgroundLight  = Color(0xFFFFFBF0)
val SurfaceLight     = Color(0xFFFFFFFF)
val OnPrimary        = Color(0xFF1A1A1A)

private val LightColorScheme = lightColorScheme(
    primary          = PrimaryYellow,
    secondary        = SecondaryBlue,
    tertiary         = PrimaryOrange,
    background       = BackgroundLight,
    surface          = SurfaceLight,
    onPrimary        = OnPrimary,
    onSecondary      = Color.White,
    onBackground     = Color(0xFF1A1A1A),
    onSurface        = Color(0xFF1A1A1A),
)

@Composable
fun KidsRoutineTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = KidsRoutineTypography,
        content     = content
    )
}
