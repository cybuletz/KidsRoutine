package com.kidsroutine.core.common.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Kid mode typography (rounded, playful) ─────────────────────────────────
val KidsRoutineTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold,   fontSize = 20.sp, lineHeight = 28.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,     fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,     fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.SemiBold,   fontSize = 14.sp, lineHeight = 20.sp),
)

// ── Teen mode typography (modern, sophisticated, less rounded) ─────────────
val TeenTypography = Typography(
    displayLarge   = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 22.sp, lineHeight = 30.sp, letterSpacing = (-0.25).sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 18.sp, lineHeight = 26.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.5.sp),
)
