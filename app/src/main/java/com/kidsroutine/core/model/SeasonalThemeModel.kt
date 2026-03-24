package com.kidsroutine.core.model

import androidx.compose.ui.graphics.Color

/**
 * Defines an app-wide seasonal visual theme.
 * Active theme is chosen by SeasonalThemeManager based on current date.
 * Feature-gated by FeatureFlags.seasonalThemesEnabled.
 */
enum class Season { SPRING, SUMMER, AUTUMN, WINTER, HALLOWEEN, CHRISTMAS, NONE }

data class SeasonalTheme(
    val season: Season = Season.NONE,
    val displayName: String = "Classic",
    val emoji: String = "✨",
    val backgroundGradientStart: Color = Color(0xFFFFFBF0),
    val backgroundGradientEnd: Color = Color(0xFFFFE4CC),
    val primaryAccent: Color = Color(0xFFFF6B35),
    val secondaryAccent: Color = Color(0xFFFFD93D),
    val cardBackground: Color = Color(0xFFFFFFFF),
    val bannerText: String = "",
    val confettiEmojis: List<String> = listOf("✨", "⭐", "🌟"),
    val isActive: Boolean = false
)

/** All built-in seasonal themes — one per season + specials */
object SeasonalThemes {
    val NONE = SeasonalTheme(
        season = Season.NONE,
        displayName = "Classic",
        emoji = "✨",
        backgroundGradientStart = Color(0xFFFFFBF0),
        backgroundGradientEnd   = Color(0xFFFFE4CC),
        primaryAccent           = Color(0xFFFF6B35),
        secondaryAccent         = Color(0xFFFFD93D),
        bannerText              = ""
    )
    val SPRING = SeasonalTheme(
        season = Season.SPRING,
        displayName = "Spring",
        emoji = "🌸",
        backgroundGradientStart = Color(0xFFFFF0F5),
        backgroundGradientEnd   = Color(0xFFE8F5E9),
        primaryAccent           = Color(0xFFE91E63),
        secondaryAccent         = Color(0xFF66BB6A),
        cardBackground          = Color(0xFFFFF8FC),
        bannerText              = "🌸 Spring is here! Bloom with your tasks!",
        confettiEmojis          = listOf("🌸", "🌷", "🌼", "🦋", "🌿")
    )
    val SUMMER = SeasonalTheme(
        season = Season.SUMMER,
        displayName = "Summer",
        emoji = "☀️",
        backgroundGradientStart = Color(0xFFFFFDE7),
        backgroundGradientEnd   = Color(0xFFE3F2FD),
        primaryAccent           = Color(0xFFFF9800),
        secondaryAccent         = Color(0xFF29B6F6),
        cardBackground          = Color(0xFFFFFDF0),
        bannerText              = "☀️ Summer vibes! Let's crush today's tasks!",
        confettiEmojis          = listOf("☀️", "🏖️", "🌊", "🍦", "⛱️")
    )
    val AUTUMN = SeasonalTheme(
        season = Season.AUTUMN,
        displayName = "Autumn",
        emoji = "🍂",
        backgroundGradientStart = Color(0xFFFFF3E0),
        backgroundGradientEnd   = Color(0xFFFBE9E7),
        primaryAccent           = Color(0xFFE64A19),
        secondaryAccent         = Color(0xFFFFB300),
        cardBackground          = Color(0xFFFFF8F0),
        bannerText              = "🍂 Autumn energy! Cosy tasks await!",
        confettiEmojis          = listOf("🍂", "🍁", "🎃", "🌰", "🍄")
    )
    val WINTER = SeasonalTheme(
        season = Season.WINTER,
        displayName = "Winter",
        emoji = "❄️",
        backgroundGradientStart = Color(0xFFE3F2FD),
        backgroundGradientEnd   = Color(0xFFF3E5F5),
        primaryAccent           = Color(0xFF1565C0),
        secondaryAccent         = Color(0xFF7B1FA2),
        cardBackground          = Color(0xFFF0F8FF),
        bannerText              = "❄️ Winter magic! Warm up with your tasks!",
        confettiEmojis          = listOf("❄️", "⛄", "🎿", "🧊", "🌨️")
    )
    val HALLOWEEN = SeasonalTheme(
        season = Season.HALLOWEEN,
        displayName = "Halloween",
        emoji = "🎃",
        backgroundGradientStart = Color(0xFF1A0A00),
        backgroundGradientEnd   = Color(0xFF2D1000),
        primaryAccent           = Color(0xFFFF6D00),
        secondaryAccent         = Color(0xFF7B1FA2),
        cardBackground          = Color(0xFF1F1108),
        bannerText              = "🎃 Spooky season! Dare to complete your tasks!",
        confettiEmojis          = listOf("🎃", "👻", "🕷️", "🦇", "🕸️")
    )
    val CHRISTMAS = SeasonalTheme(
        season = Season.CHRISTMAS,
        displayName = "Christmas",
        emoji = "🎄",
        backgroundGradientStart = Color(0xFF1B5E20),
        backgroundGradientEnd   = Color(0xFFB71C1C),
        primaryAccent           = Color(0xFFE53935),
        secondaryAccent         = Color(0xFFFFD700),
        cardBackground          = Color(0xFF1A2E1A),
        bannerText              = "🎄 Ho ho ho! Complete tasks for Santa!",
        confettiEmojis          = listOf("🎄", "🎁", "⭐", "❄️", "🦌")
    )
}