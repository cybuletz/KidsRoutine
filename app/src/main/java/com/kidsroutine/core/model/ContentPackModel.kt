package com.kidsroutine.core.model

/**
 * A content pack is a curated bundle of tasks + challenges on a theme.
 * Unlocked by spending XP (free packs) or by PRO entitlement (premium packs).
 */
enum class ContentPackTier { FREE, PRO, PREMIUM }

data class ContentPack(
    val packId: String = "",
    val name: String = "",
    val emoji: String = "📦",
    val description: String = "",
    val tier: ContentPackTier = ContentPackTier.FREE,
    val xpCost: Int = 0,                // 0 = free for tier FREE
    val taskCount: Int = 0,
    val challengeCount: Int = 0,
    val previewTaskTitles: List<String> = emptyList(),
    val isUnlocked: Boolean = false,
    val accentColor: String = "#FF6B35"  // hex string
)

/** Built-in packs — seed data, also stored in Firestore `content_packs` collection */
object BuiltInContentPacks {
    val all = listOf(
        ContentPack(
            packId            = "pack_no_screen",
            name              = "No-Screen Day",
            emoji             = "📵",
            description       = "A full day of offline activities — creative, active, and mindful.",
            tier              = ContentPackTier.FREE,
            xpCost            = 0,
            taskCount         = 5,
            challengeCount    = 1,
            previewTaskTitles = listOf("Build a fort", "Draw a map", "Cook a snack"),
            accentColor       = "#E91E63"
        ),
        ContentPack(
            packId            = "pack_discipline",
            name              = "Discipline Pack",
            emoji             = "💪",
            description       = "Build unbreakable habits through structured daily routines.",
            tier              = ContentPackTier.PRO,
            xpCost            = 500,
            taskCount         = 8,
            challengeCount    = 3,
            previewTaskTitles = listOf("Wake-up protocol", "Study block", "Evening review"),
            accentColor       = "#667EEA"
        ),
        ContentPack(
            packId            = "pack_creativity",
            name              = "Creative Thinking",
            emoji             = "🎨",
            description       = "Spark imagination with art, storytelling, and invention tasks.",
            tier              = ContentPackTier.FREE,
            xpCost            = 200,
            taskCount         = 6,
            challengeCount    = 2,
            previewTaskTitles = listOf("Invent a gadget", "Write a short story", "Origami challenge"),
            accentColor       = "#FF6B35"
        ),
        ContentPack(
            packId            = "pack_outdoor_week",
            name              = "Family Outdoor Week",
            emoji             = "🌿",
            description       = "7 days of outdoor adventures for the whole family.",
            tier              = ContentPackTier.PRO,
            xpCost            = 0,
            taskCount         = 7,
            challengeCount    = 1,
            previewTaskTitles = listOf("Nature walk", "Cloud spotting", "Picnic prep"),
            accentColor       = "#4CAF50"
        ),
        ContentPack(
            packId            = "pack_mindfulness",
            name              = "Mindfulness & Sleep",
            emoji             = "🌙",
            description       = "Calming bedtime routines and morning mindfulness practices.",
            tier              = ContentPackTier.FREE,
            xpCost            = 150,
            taskCount         = 5,
            challengeCount    = 2,
            previewTaskTitles = listOf("5-breath reset", "Gratitude journal", "Screen-off countdown"),
            accentColor       = "#9B59B6"
        )
    )
}