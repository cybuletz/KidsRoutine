package com.kidsroutine.core.common.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Brand palette (light / kid mode) ───────────────────────────────────────
val PrimaryYellow    = Color(0xFFFFD93D)
val PrimaryOrange    = Color(0xFFFF6B35)
val SecondaryBlue    = Color(0xFF4ECDC4)
val BackgroundLight  = Color(0xFFFFFBF0)
val SurfaceLight     = Color(0xFFFFFFFF)
val OnPrimary        = Color(0xFF1A1A1A)

// ── Dark / teen palette ────────────────────────────────────────────────────
val DarkBackground   = Color(0xFF121620)   // navy/charcoal, not pure black
val DarkSurface      = Color(0xFF1C2030)
val DarkCard         = Color(0xFF232840)
val DarkAccent       = Color(0xFF6C63FF)   // modern purple accent
val DarkSecondary    = Color(0xFF4ECDC4)
val DarkOnSurface    = Color(0xFFE8E8F0)
val DarkOnBackground = Color(0xFFD0D0E0)

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

private val DarkColorScheme = darkColorScheme(
    primary          = DarkAccent,
    secondary        = DarkSecondary,
    tertiary         = PrimaryOrange,
    background       = DarkBackground,
    surface          = DarkSurface,
    surfaceVariant   = DarkCard,
    onPrimary        = Color.White,
    onSecondary      = Color.White,
    onBackground     = DarkOnBackground,
    onSurface        = DarkOnSurface,
)

/** Whether the current theme is in teen mode (age 13+) */
val LocalIsTeenMode = staticCompositionLocalOf { false }

/** Whether dark mode is enabled */
val LocalIsDarkMode = staticCompositionLocalOf { false }

@Composable
fun KidsRoutineTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = if (darkTheme) TeenTypography else KidsRoutineTypography

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = typography,
        content     = content
    )
}
