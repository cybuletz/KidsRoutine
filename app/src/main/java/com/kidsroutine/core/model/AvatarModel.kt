package com.kidsroutine.core.model

enum class AvatarCategory {
    BODY, EYES, MOUTH, HAIRSTYLE, ACCESSORIES, CLOTHING, BACKGROUND
}

enum class AvatarRarity {
    COMMON, RARE, EPIC, LEGENDARY;

    fun xpCost(): Int = when(this) {
        COMMON -> 50
        RARE -> 150
        EPIC -> 400
        LEGENDARY -> 1000
    }

    fun color(): Long = when(this) {
        COMMON -> 0xFF95A5A6
        RARE -> 0xFF3498DB
        EPIC -> 0xFF9B59B6
        LEGENDARY -> 0xFFFF6B35
    }
}

data class AvatarItem(
    val itemId: String = "",
    val category: AvatarCategory = AvatarCategory.BODY,
    val name: String = "",
    val description: String = "",
    val rarity: AvatarRarity = AvatarRarity.COMMON,
    val iconUrl: String = "",
    val previewUrl: String = "",
    val xpCost: Int = 0,
    val requiredLevel: Int = 0,
    val requiredAchievementId: String? = null,
    val isSeasonalLimited: Boolean = false,
    val seasonalEndDate: Long? = null,
    val colorable: Boolean = false,
    val defaultColor: String = "#FF6B35"
)

data class AvatarComponent(
    val category: AvatarCategory = AvatarCategory.BODY,
    val selectedItemId: String = "",
    val selectedColor: String = "#FF6B35"
)

data class AvatarCustomization(
    val body: AvatarComponent = AvatarComponent(AvatarCategory.BODY),
    val eyes: AvatarComponent = AvatarComponent(AvatarCategory.EYES),
    val mouth: AvatarComponent = AvatarComponent(AvatarCategory.MOUTH),
    val hairstyle: AvatarComponent = AvatarComponent(AvatarCategory.HAIRSTYLE),
    val accessories: AvatarComponent = AvatarComponent(AvatarCategory.ACCESSORIES),
    val clothing: AvatarComponent = AvatarComponent(AvatarCategory.CLOTHING),
    val background: AvatarComponent = AvatarComponent(AvatarCategory.BACKGROUND),
    val unlockedItemIds: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)