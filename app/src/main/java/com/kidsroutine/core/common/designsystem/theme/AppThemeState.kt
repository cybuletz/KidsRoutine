package com.kidsroutine.core.common.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.kidsroutine.core.model.SeasonalTheme
import com.kidsroutine.core.model.SeasonalThemes

/**
 * CompositionLocal providing the active SeasonalTheme to any composable.
 * Set once at the root of the app (in MainActivity / KidsRoutineNavGraph).
 *
 * Usage inside any @Composable:
 *   val theme = LocalSeasonalTheme.current
 *   Box(modifier = Modifier.background(theme.backgroundGradientStart))
 */
val LocalSeasonalTheme = staticCompositionLocalOf<SeasonalTheme> {
    SeasonalThemes.NONE   // safe default — never crashes
}