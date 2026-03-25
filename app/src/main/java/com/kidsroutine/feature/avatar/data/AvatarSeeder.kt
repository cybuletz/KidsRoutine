package com.kidsroutine.feature.avatar.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.AvatarCategory
import com.kidsroutine.core.model.AvatarItem
import com.kidsroutine.core.model.AvatarRarity
import kotlinx.coroutines.tasks.await

object AvatarShopSeeder {

    private val items = listOf(
        // BODY
        AvatarItem(itemId = "body_blue",     category = AvatarCategory.BODY,        name = "Blue Body",      description = "Classic blue body",         rarity = AvatarRarity.COMMON,    xpCost = 50,   colorable = true,  defaultColor = "#4A90E2"),
        AvatarItem(itemId = "body_green",    category = AvatarCategory.BODY,        name = "Green Body",     description = "Nature green body",          rarity = AvatarRarity.COMMON,    xpCost = 50,   colorable = true,  defaultColor = "#2ECC71"),
        AvatarItem(itemId = "body_purple",   category = AvatarCategory.BODY,        name = "Purple Body",    description = "Royal purple body",          rarity = AvatarRarity.RARE,      xpCost = 150,  colorable = true,  defaultColor = "#9B59B6"),
        AvatarItem(itemId = "body_gold",     category = AvatarCategory.BODY,        name = "Golden Body",    description = "Legendary golden body",      rarity = AvatarRarity.LEGENDARY, xpCost = 1000, colorable = true,  defaultColor = "#FFD700"),
        // EYES
        AvatarItem(itemId = "eyes_star",     category = AvatarCategory.EYES,        name = "Star Eyes",      description = "Sparkly star eyes",          rarity = AvatarRarity.COMMON,    xpCost = 50,   colorable = false, defaultColor = "#FFD700"),
        AvatarItem(itemId = "eyes_heart",    category = AvatarCategory.EYES,        name = "Heart Eyes",     description = "Adorable heart eyes",        rarity = AvatarRarity.RARE,      xpCost = 150,  colorable = false, defaultColor = "#E74C3C"),
        AvatarItem(itemId = "eyes_cool",     category = AvatarCategory.EYES,        name = "Cool Shades",    description = "Super cool shades",          rarity = AvatarRarity.EPIC,      xpCost = 400,  colorable = true,  defaultColor = "#2C3E50"),
        // MOUTH
        AvatarItem(itemId = "mouth_smile",   category = AvatarCategory.MOUTH,       name = "Big Smile",      description = "Big friendly smile",         rarity = AvatarRarity.COMMON,    xpCost = 50,   colorable = false, defaultColor = "#FF6B35"),
        AvatarItem(itemId = "mouth_grin",    category = AvatarCategory.MOUTH,       name = "Cheeky Grin",   description = "Cheeky grin",                rarity = AvatarRarity.RARE,      xpCost = 150,  colorable = false, defaultColor = "#E67E22"),
        // HAIRSTYLE
        AvatarItem(itemId = "hair_spiky",    category = AvatarCategory.HAIRSTYLE,   name = "Spiky Hair",     description = "Wild spiky hair",            rarity = AvatarRarity.COMMON,    xpCost = 50,   colorable = true,  defaultColor = "#8B4513"),
        AvatarItem(itemId = "hair_curly",    category = AvatarCategory.HAIRSTYLE,   name = "Curly Hair",     description = "Bouncy curly hair",          rarity = AvatarRarity.RARE,      xpCost = 150,  colorable = true,  defaultColor = "#D4AC0D"),
        AvatarItem(itemId = "hair_rainbow",  category = AvatarCategory.HAIRSTYLE,   name = "Rainbow Hair",   description = "Rainbow gradient hair",      rarity = AvatarRarity.EPIC,      xpCost = 400,  colorable = false, defaultColor = "#FF69B4"),
        // ACCESSORIES
        AvatarItem(itemId = "acc_crown",     category = AvatarCategory.ACCESSORIES, name = "Gold Crown",     description = "Royal gold crown",           rarity = AvatarRarity.LEGENDARY, xpCost = 1000, colorable = false, defaultColor = "#FFD700"),
        AvatarItem(itemId = "acc_glasses",   category = AvatarCategory.ACCESSORIES, name = "Glasses",        description = "Smart glasses",              rarity = AvatarRarity.COMMON,    xpCost = 50,   colorable = true,  defaultColor = "#2C3E50"),
        AvatarItem(itemId = "acc_bow",       category = AvatarCategory.ACCESSORIES, name = "Bow Tie",        description = "Fancy bow tie",              rarity = AvatarRarity.RARE,      xpCost = 150,  colorable = true,  defaultColor = "#E74C3C"),
        // CLOTHING
        AvatarItem(itemId = "cloth_cape",    category = AvatarCategory.CLOTHING,    name = "Hero Cape",      description = "Legendary hero cape",        rarity = AvatarRarity.EPIC,      xpCost = 400,  colorable = true,  defaultColor = "#9B59B6"),
        AvatarItem(itemId = "cloth_uniform", category = AvatarCategory.CLOTHING,    name = "Star Uniform",   description = "Star student uniform",       rarity = AvatarRarity.RARE,      xpCost = 150,  colorable = true,  defaultColor = "#4A90E2"),
        AvatarItem(itemId = "cloth_hoodie",  category = AvatarCategory.CLOTHING,    name = "Cool Hoodie",    description = "Cozy cool hoodie",           rarity = AvatarRarity.COMMON,    xpCost = 50,   colorable = true,  defaultColor = "#2ECC71"),
        // BACKGROUND
        AvatarItem(itemId = "bg_space",      category = AvatarCategory.BACKGROUND,  name = "Space",          description = "Outer space background",     rarity = AvatarRarity.EPIC,      xpCost = 400,  colorable = false, defaultColor = "#0D1B2A"),
        AvatarItem(itemId = "bg_rainbow",    category = AvatarCategory.BACKGROUND,  name = "Rainbow",        description = "Rainbow sky background",     rarity = AvatarRarity.LEGENDARY, xpCost = 1000, colorable = false, defaultColor = "#FF6B35"),
        AvatarItem(itemId = "bg_forest",     category = AvatarCategory.BACKGROUND,  name = "Forest",         description = "Calm forest background",     rarity = AvatarRarity.RARE,      xpCost = 150,  colorable = false, defaultColor = "#27AE60"),
        AvatarItem(itemId = "bg_beach",      category = AvatarCategory.BACKGROUND,  name = "Beach",          description = "Sunny beach background",     rarity = AvatarRarity.COMMON,    xpCost = 50,   colorable = false, defaultColor = "#3498DB"),
    )

    suspend fun seed() {
        val db = FirebaseFirestore.getInstance()
        val collection = db.collection("avatar_items")

        val existing = collection.limit(1).get().await()
        if (!existing.isEmpty) {
            Log.d("AvatarShopSeeder", "Already seeded, skipping.")
            return
        }

        items.forEach { item ->
            collection.document(item.itemId).set(item).await()
            Log.d("AvatarShopSeeder", "Seeded: ${item.name}")
        }
        Log.d("AvatarShopSeeder", "Done — ${items.size} items added.")
    }
}
