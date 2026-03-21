package com.kidsroutine.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kidsroutine.core.model.AvatarComponent
import com.kidsroutine.core.model.AvatarCustomization
import com.kidsroutine.core.model.AvatarCategory

@Entity(tableName = "avatar_customizations")
data class AvatarEntity(
    @PrimaryKey
    val userId: String,
    val bodyItemId: String = "",
    val bodyColor: String = "#FF6B35",
    val eyesItemId: String = "",
    val eyesColor: String = "#FF6B35",
    val mouthItemId: String = "",
    val mouthColor: String = "#FF6B35",
    val hairstyleItemId: String = "",
    val hairstyleColor: String = "#FF6B35",
    val accessoriesItemId: String = "",
    val accessoriesColor: String = "#FF6B35",
    val clothingItemId: String = "",
    val clothingColor: String = "#FF6B35",
    val backgroundItemId: String = "",
    val backgroundColor: String = "#FFFFFF",
    val unlockedItemIds: String = "", // JSON serialized list
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toCustomization(): AvatarCustomization {
        val unlockedList = if (unlockedItemIds.isBlank()) emptyList()
        else unlockedItemIds.split(",")

        return AvatarCustomization(
            body = AvatarComponent(AvatarCategory.BODY, bodyItemId, bodyColor),
            eyes = AvatarComponent(AvatarCategory.EYES, eyesItemId, eyesColor),
            mouth = AvatarComponent(AvatarCategory.MOUTH, mouthItemId, mouthColor),
            hairstyle = AvatarComponent(AvatarCategory.HAIRSTYLE, hairstyleItemId, hairstyleColor),
            accessories = AvatarComponent(AvatarCategory.ACCESSORIES, accessoriesItemId, accessoriesColor),
            clothing = AvatarComponent(AvatarCategory.CLOTHING, clothingItemId, clothingColor),
            background = AvatarComponent(AvatarCategory.BACKGROUND, backgroundItemId, backgroundColor),
            unlockedItemIds = unlockedList,
            lastUpdated = lastUpdated
        )
    }

    companion object {
        fun fromCustomization(userId: String, customization: AvatarCustomization): AvatarEntity {
            return AvatarEntity(
                userId = userId,
                bodyItemId = customization.body.selectedItemId,
                bodyColor = customization.body.selectedColor,
                eyesItemId = customization.eyes.selectedItemId,
                eyesColor = customization.eyes.selectedColor,
                mouthItemId = customization.mouth.selectedItemId,
                mouthColor = customization.mouth.selectedColor,
                hairstyleItemId = customization.hairstyle.selectedItemId,
                hairstyleColor = customization.hairstyle.selectedColor,
                accessoriesItemId = customization.accessories.selectedItemId,
                accessoriesColor = customization.accessories.selectedColor,
                clothingItemId = customization.clothing.selectedItemId,
                clothingColor = customization.clothing.selectedColor,
                backgroundItemId = customization.background.selectedItemId,
                backgroundColor = customization.background.selectedColor,
                unlockedItemIds = customization.unlockedItemIds.joinToString(","),
                lastUpdated = customization.lastUpdated
            )
        }
    }
}