package com.kidsroutine.feature.avatar.data

import com.kidsroutine.core.model.*

object AvatarSeeder {

    // ── Free Backgrounds ──────────────────────────────────────────────────
    val freeBackgrounds = listOf(
        AvatarLayerItem(
            id = "bg_sunset",
            name = "Sunset Beach",
            layerType = AvatarLayerType.BACKGROUND,
            source = AvatarAssetSource.GradientBackground(
                topColor = 0xFFFF6B35, bottomColor = 0xFFFFD93D, label = "Sunset"
            ),
            sortOrder = 1
        ),
        AvatarLayerItem(
            id = "bg_forest",
            name = "Magic Forest",
            layerType = AvatarLayerType.BACKGROUND,
            source = AvatarAssetSource.GradientBackground(
                topColor = 0xFF2D6A4F, bottomColor = 0xFF95D5B2, label = "Forest"
            ),
            sortOrder = 2
        ),
        AvatarLayerItem(
            id = "bg_ocean",
            name = "Deep Ocean",
            layerType = AvatarLayerType.BACKGROUND,
            source = AvatarAssetSource.GradientBackground(
                topColor = 0xFF023E8A, bottomColor = 0xFF90E0EF, label = "Ocean"
            ),
            sortOrder = 3
        ),
        AvatarLayerItem(
            id = "bg_space",
            name = "Outer Space",
            layerType = AvatarLayerType.BACKGROUND,
            source = AvatarAssetSource.GradientBackground(
                topColor = 0xFF10002B, bottomColor = 0xFF3C096C, label = "Space"
            ),
            sortOrder = 4
        ),
        AvatarLayerItem(
            id = "bg_candy",
            name = "Candy World",
            layerType = AvatarLayerType.BACKGROUND,
            source = AvatarAssetSource.GradientBackground(
                topColor = 0xFFFF99C8, bottomColor = 0xFFFCF6BD, label = "Candy"
            ),
            sortOrder = 5
        ),
        AvatarLayerItem(
            id = "bg_volcano",
            name = "Volcano Realm",
            layerType = AvatarLayerType.BACKGROUND,
            source = AvatarAssetSource.GradientBackground(
                topColor = 0xFF6A040F, bottomColor = 0xFFFFBA08, label = "Volcano"
            ),
            sortOrder = 6
        )
    )

    // ── Free Hair Options ──────────────────────────────────────────────────
    val freeHair = listOf(
        AvatarLayerItem("hair_short_brown", "Short Brown", AvatarLayerType.HAIR,
            AvatarAssetSource.VectorRes(0 /* R.drawable.hair_short_boy */),
            tintColor = 0xFF5C3317, compatibleGenders = setOf(AvatarGender.BOY)),
        AvatarLayerItem("hair_short_black", "Short Black", AvatarLayerType.HAIR,
            AvatarAssetSource.VectorRes(0), tintColor = 0xFF1A1A1A,
            compatibleGenders = setOf(AvatarGender.BOY)),
        AvatarLayerItem("hair_long_brown", "Long Brown", AvatarLayerType.HAIR,
            AvatarAssetSource.VectorRes(0 /* R.drawable.hair_long_girl */),
            tintColor = 0xFF5C3317, compatibleGenders = setOf(AvatarGender.GIRL)),
        AvatarLayerItem("hair_ponytail_black", "Ponytail Black", AvatarLayerType.HAIR,
            AvatarAssetSource.VectorRes(0), tintColor = 0xFF1A1A1A,
            compatibleGenders = setOf(AvatarGender.GIRL)),
        AvatarLayerItem("hair_curly_auburn", "Curly Auburn", AvatarLayerType.HAIR,
            AvatarAssetSource.VectorRes(0), tintColor = 0xFFC1440E,
            compatibleGenders = setOf(AvatarGender.BOY, AvatarGender.GIRL))
    )

    // ── Free Outfits ───────────────────────────────────────────────────────
    val freeOutfits = listOf(
        AvatarLayerItem("outfit_casual_blue", "Casual Blue", AvatarLayerType.OUTFIT,
            AvatarAssetSource.VectorRes(0), tintColor = 0xFF1D3557),
        AvatarLayerItem("outfit_casual_red", "Casual Red", AvatarLayerType.OUTFIT,
            AvatarAssetSource.VectorRes(0), tintColor = 0xFFE63946),
        AvatarLayerItem("outfit_school", "School Uniform", AvatarLayerType.OUTFIT,
            AvatarAssetSource.VectorRes(0), tintColor = 0xFF457B9D),
        AvatarLayerItem("outfit_sport", "Sports Gear", AvatarLayerType.OUTFIT,
            AvatarAssetSource.VectorRes(0), tintColor = 0xFF2DC653)
    )

    // ── Premium Content Packs ──────────────────────────────────────────────
    val premiumPacks = listOf(

        AvatarContentPack(
            id = "pack_ninja_warriors",
            name = "Ninja Warriors",
            description = "Train like a true ninja warrior! Epic outfits, fire & water FX.",
            // Use a placeholder URL — replace with your actual CDN assets
            coverImageUrl = "https://placehold.co/400x300/1a1a2e/ffffff?text=Ninja+Warriors",
            accentColor = 0xFF16213E,
            isTrending = true,
            isNew = false,
            packPrice = 500,
            billingProductId = "pack_ninja_warriors_v1",
            items = listOf(
                AvatarLayerItem("ninja_outfit_dark", "Dark Ninja Gi", AvatarLayerType.OUTFIT,
                    AvatarAssetSource.VectorRes(0), tintColor = 0xFF1A1A2E,
                    isPremium = true, packId = "pack_ninja_warriors", coinCost = 150),
                AvatarLayerItem("ninja_outfit_red", "Red Demon Gi", AvatarLayerType.OUTFIT,
                    AvatarAssetSource.VectorRes(0), tintColor = 0xFFC1121F,
                    isPremium = true, packId = "pack_ninja_warriors", coinCost = 200),
                AvatarLayerItem("ninja_bg_dojo", "Ancient Dojo", AvatarLayerType.BACKGROUND,
                    AvatarAssetSource.GradientBackground(0xFF1A0000, 0xFF8B0000, "Dojo"),
                    isPremium = true, packId = "pack_ninja_warriors"),
                AvatarLayerItem("ninja_fx_fire", "Fire Aura", AvatarLayerType.SPECIAL_FX,
                    AvatarAssetSource.VectorRes(0),
                    isPremium = true, packId = "pack_ninja_warriors", coinCost = 300),
                AvatarLayerItem("ninja_accessory_headband", "Ninja Headband",
                    AvatarLayerType.ACCESSORY, AvatarAssetSource.VectorRes(0),
                    tintColor = 0xFFC1121F, isPremium = true, packId = "pack_ninja_warriors")
            )
        ),

        AvatarContentPack(
            id = "pack_monster_trainers",
            name = "Monster Trainers",
            description = "Catch 'em all! Trainer outfits, Pokéball accessories & adventure backgrounds.",
            coverImageUrl = "https://placehold.co/400x300/FFCB05/003087?text=Monster+Trainers",
            accentColor = 0xFFFFCB05,
            isTrending = true,
            isNew = true,
            packPrice = 500,
            billingProductId = "pack_monster_trainers_v1",
            items = listOf(
                AvatarLayerItem("mt_outfit_red_cap", "Trainer Red Cap", AvatarLayerType.ACCESSORY,
                    AvatarAssetSource.VectorRes(0), tintColor = 0xFFCC0000,
                    isPremium = true, packId = "pack_monster_trainers", coinCost = 120),
                AvatarLayerItem("mt_outfit_trainer", "Trainer Jacket", AvatarLayerType.OUTFIT,
                    AvatarAssetSource.VectorRes(0), tintColor = 0xFF003087,
                    isPremium = true, packId = "pack_monster_trainers", coinCost = 180),
                AvatarLayerItem("mt_bg_tall_grass", "Tall Grass Plains", AvatarLayerType.BACKGROUND,
                    AvatarAssetSource.GradientBackground(0xFF2D6A4F, 0xFF74C69D, "Plains"),
                    isPremium = true, packId = "pack_monster_trainers"),
                AvatarLayerItem("mt_bg_gym", "Trainer Gym", AvatarLayerType.BACKGROUND,
                    AvatarAssetSource.GradientBackground(0xFF1B4332, 0xFF40916C, "Gym"),
                    isPremium = true, packId = "pack_monster_trainers"),
                AvatarLayerItem("mt_accessory_pokeball", "Monster Ball Belt",
                    AvatarLayerType.ACCESSORY, AvatarAssetSource.VectorRes(0),
                    isPremium = true, packId = "pack_monster_trainers", coinCost = 100)
            )
        ),

        AvatarContentPack(
            id = "pack_space_explorer",
            name = "Space Explorer",
            description = "Blast off into the cosmos! Astronaut suits, galaxy backgrounds & cosmic FX.",
            coverImageUrl = "https://placehold.co/400x300/10002b/c77dff?text=Space+Explorer",
            accentColor = 0xFF7B2FBE,
            isTrending = false,
            isNew = true,
            packPrice = 450,
            billingProductId = "pack_space_explorer_v1",
            items = listOf(
                AvatarLayerItem("space_outfit_astronaut", "Astronaut Suit", AvatarLayerType.OUTFIT,
                    AvatarAssetSource.VectorRes(0), tintColor = 0xFFE0E0E0,
                    isPremium = true, packId = "pack_space_explorer", coinCost = 200),
                AvatarLayerItem("space_bg_galaxy", "Galaxy", AvatarLayerType.BACKGROUND,
                    AvatarAssetSource.GradientBackground(0xFF10002B, 0xFF7B2FBE, "Galaxy"),
                    isPremium = true, packId = "pack_space_explorer"),
                AvatarLayerItem("space_bg_nebula", "Nebula Storm", AvatarLayerType.BACKGROUND,
                    AvatarAssetSource.GradientBackground(0xFF1B263B, 0xFF415A77, "Nebula"),
                    isPremium = true, packId = "pack_space_explorer"),
                AvatarLayerItem("space_fx_stars", "Star Trail FX", AvatarLayerType.SPECIAL_FX,
                    AvatarAssetSource.VectorRes(0),
                    isPremium = true, packId = "pack_space_explorer", coinCost = 250)
            )
        ),

        AvatarContentPack(
            id = "pack_superhero",
            name = "Superheroes",
            description = "Save the world! Cape outfits, city skyline backgrounds & lightning FX.",
            coverImageUrl = "https://placehold.co/400x300/d62828/f7b731?text=Superheroes",
            accentColor = 0xFFD62828,
            isTrending = false,
            isNew = false,
            packPrice = 450,
            billingProductId = "pack_superhero_v1",
            items = listOf(
                AvatarLayerItem("hero_outfit_cape_red", "Red Cape Suit", AvatarLayerType.OUTFIT,
                    AvatarAssetSource.VectorRes(0), tintColor = 0xFFD62828,
                    isPremium = true, packId = "pack_superhero", coinCost = 200),
                AvatarLayerItem("hero_outfit_cape_blue", "Blue Cape Suit", AvatarLayerType.OUTFIT,
                    AvatarAssetSource.VectorRes(0), tintColor = 0xFF023E8A,
                    isPremium = true, packId = "pack_superhero", coinCost = 200),
                AvatarLayerItem("hero_bg_city", "City Skyline", AvatarLayerType.BACKGROUND,
                    AvatarAssetSource.GradientBackground(0xFF03071E, 0xFF370617, "City Night"),
                    isPremium = true, packId = "pack_superhero"),
                AvatarLayerItem("hero_fx_lightning", "Lightning Aura", AvatarLayerType.SPECIAL_FX,
                    AvatarAssetSource.VectorRes(0),
                    isPremium = true, packId = "pack_superhero", coinCost = 300),
                AvatarLayerItem("hero_accessory_mask", "Hero Mask", AvatarLayerType.ACCESSORY,
                    AvatarAssetSource.VectorRes(0), tintColor = 0xFFD62828,
                    isPremium = true, packId = "pack_superhero", coinCost = 100)
            )
        )
    )

    fun allFreeItems(): List<AvatarLayerItem> =
        freeBackgrounds + freeHair + freeOutfits

    fun allPremiumItems(): List<AvatarLayerItem> =
        premiumPacks.flatMap { it.items }
}