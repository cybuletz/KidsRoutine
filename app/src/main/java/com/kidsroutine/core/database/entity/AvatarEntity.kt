package com.kidsroutine.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// No imports of AvatarComponent / AvatarCustomization / AvatarCategory
// Room only handles primitives — all complex types stored as plain strings

@Entity(tableName = "avatar")   // ← was "avatar_customizations", now matches DAO queries
data class AvatarEntity(
    @PrimaryKey
    val userId: String,
    val gender: String = "BOY",
    val skinTone: Long = 0xFFFFDBAD,
    val activeBackgroundId: String? = null,
    val activeHairId: String? = null,
    val activeOutfitId: String? = null,
    val activeShoesId: String? = null,
    val activeAccessoryId: String? = null,
    val activeSpecialFxId: String? = null,
    val unlockedItemIdsJson: String = "[]",
    val ownedPackIdsJson: String = "[]",
    val lastUpdated: Long = System.currentTimeMillis()
)