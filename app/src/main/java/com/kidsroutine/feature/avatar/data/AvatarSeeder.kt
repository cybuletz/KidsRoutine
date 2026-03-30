package com.kidsroutine.feature.avatar.data

import com.kidsroutine.R
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
        AvatarLayerItem(
            id = "hair_short_brown",
            name = "Short Brown",
            layerType = AvatarLayerType.HAIR,
            source = AvatarAssetSource.VectorRes(R.drawable.hair_short_boy),
            tintColor = 0xFF5C3317,
            compatibleGenders = setOf(AvatarGender.BOY),
            sortOrder = 1
        ),
        AvatarLayerItem(
            id = "hair_short_black",
            name = "Short Black",
            layerType = AvatarLayerType.HAIR,
            source = AvatarAssetSource.VectorRes(R.drawable.hair_short_boy),
            tintColor = 0xFF1A1A1A,
            compatibleGenders = setOf(AvatarGender.BOY),
            sortOrder = 2
        ),
        AvatarLayerItem(
            id = "hair_long_brown",
            name = "Long Brown",
            layerType = AvatarLayerType.HAIR,
            source = AvatarAssetSource.VectorRes(R.drawable.hair_long_girl),
            tintColor = 0xFF5C3317,
            compatibleGenders = setOf(AvatarGender.GIRL),
            sortOrder = 3
        ),
        AvatarLayerItem(
            id = "hair_ponytail_black",
            name = "Ponytail Black",
            layerType = AvatarLayerType.HAIR,
            source = AvatarAssetSource.VectorRes(R.drawable.hair_ponytail),
            tintColor = 0xFF1A1A1A,
            compatibleGenders = setOf(AvatarGender.GIRL),
            sortOrder = 4
        ),
        AvatarLayerItem(
            id = "hair_curly_auburn",
            name = "Curly Auburn",
            layerType = AvatarLayerType.HAIR,
            source = AvatarAssetSource.VectorRes(R.drawable.hair_curly),
            tintColor = 0xFFC1440E,
            compatibleGenders = setOf(AvatarGender.BOY, AvatarGender.GIRL),
            sortOrder = 5
        )
    )

    // ── Free Outfits ───────────────────────────────────────────────────────
    val freeOutfits = listOf(
        AvatarLayerItem(
            id = "outfit_casual_blue",
            name = "Casual Blue",
            layerType = AvatarLayerType.OUTFIT,
            source = AvatarAssetSource.VectorRes(R.drawable.outfit_casual),
            tintColor = 0xFF1D3557,
            sortOrder = 1
        ),
        AvatarLayerItem(
            id = "outfit_casual_red",
            name = "Casual Red",
            layerType = AvatarLayerType.OUTFIT,
            source = AvatarAssetSource.VectorRes(R.drawable.outfit_casual),
            tintColor = 0xFFE63946,
            sortOrder = 2
        ),
        AvatarLayerItem(
            id = "outfit_school",
            name = "School Uniform",
            layerType = AvatarLayerType.OUTFIT,
            source = AvatarAssetSource.VectorRes(R.drawable.outfit_casual),
            tintColor = 0xFF457B9D,
            sortOrder = 3
        ),
        AvatarLayerItem(
            id = "outfit_sport",
            name = "Sports Gear",
            layerType = AvatarLayerType.OUTFIT,
            source = AvatarAssetSource.VectorRes(R.drawable.outfit_sport),
            tintColor = 0xFF2DC653,
            sortOrder = 4
        )
    )

    // ── Premium Content Packs (NOW XP-BASED) ──────────────────────────────
    val premiumPacks = listOf(

        AvatarContentPack(
            id = "pack_ninja_warriors",
            name = "Ninja Warriors",
            description = "Train like a true ninja warrior! Epic outfits, fire & water FX.",
            coverImageUrl = "https://placehold.co/400x300/1a1a2e/ffffff?text=Ninja+Warriors",
            accentColor = 0xFF16213E,
            isTrending = true,
            isNew = false,
            packPrice = 150,  // Total XP cost for entire pack
            billingProductId = "pack_ninja_warriors_v1",
            items = listOf(
                AvatarLayerItem(
                    id = "ninja_outfit_dark",
                    name = "Dark Ninja Gi",
                    layerType = AvatarLayerType.OUTFIT,
                    source = AvatarAssetSource.VectorRes(R.drawable.outfit_ninja),
                    tintColor = 0xFF1A1A2E,
                    isPremium = true,
                    packId = "pack_ninja_warriors",
                    coinCost = 500  // XP cost per item
                ),
                AvatarLayerItem(
                    id = "ninja_outfit_red",
                    name = "Red Demon Gi",
                    layerType = AvatarLayerType.OUTFIT,
                    source = AvatarAssetSource.VectorRes(R.drawable.outfit_ninja),
                    tintColor = 0xFFC1121F,
                    isPremium = true,
                    packId = "pack_ninja_warriors",
                    coinCost = 75
                ),
                AvatarLayerItem(
                    id = "ninja_bg_dojo",
                    name = "Ancient Dojo",
                    layerType = AvatarLayerType.BACKGROUND,
                    source = AvatarAssetSource.GradientBackground(0xFF1A0000, 0xFF8B0000, "Dojo"),
                    isPremium = true,
                    packId = "pack_ninja_warriors",
                    coinCost = 30
                ),
                AvatarLayerItem(
                    id = "ninja_fx_fire",
                    name = "Fire Aura",
                    layerType = AvatarLayerType.SPECIAL_FX,
                    source = AvatarAssetSource.VectorRes(R.drawable.fx_fire),
                    isPremium = true,
                    packId = "pack_ninja_warriors",
                    coinCost = 60
                ),
                AvatarLayerItem(
                    id = "ninja_accessory_headband",
                    name = "Ninja Headband",
                    layerType = AvatarLayerType.ACCESSORY,
                    source = AvatarAssetSource.VectorRes(R.drawable.accessory_headband),
                    tintColor = 0xFFC1121F,
                    isPremium = true,
                    packId = "pack_ninja_warriors",
                    coinCost = 35
                )
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
            packPrice = 100,  // Total XP cost
            billingProductId = "pack_monster_trainers_v1",
            items = listOf(
                AvatarLayerItem(
                    id = "mt_accessory_cap",
                    name = "Trainer Red Cap",
                    layerType = AvatarLayerType.ACCESSORY,
                    source = AvatarAssetSource.VectorRes(R.drawable.accessory_cap),
                    tintColor = 0xFFCC0000,
                    isPremium = true,
                    packId = "pack_monster_trainers",
                    coinCost = 40
                ),
                AvatarLayerItem(
                    id = "mt_outfit_trainer",
                    name = "Trainer Jacket",
                    layerType = AvatarLayerType.OUTFIT,
                    source = AvatarAssetSource.VectorRes(R.drawable.outfit_trainer),
                    tintColor = 0xFF003087,
                    isPremium = true,
                    packId = "pack_monster_trainers",
                    coinCost = 70
                ),
                AvatarLayerItem(
                    id = "mt_bg_tall_grass",
                    name = "Tall Grass Plains",
                    layerType = AvatarLayerType.BACKGROUND,
                    source = AvatarAssetSource.GradientBackground(0xFF2D6A4F, 0xFF74C69D, "Plains"),
                    isPremium = true,
                    packId = "pack_monster_trainers",
                    coinCost = 25
                ),
                AvatarLayerItem(
                    id = "mt_bg_gym",
                    name = "Trainer Gym",
                    layerType = AvatarLayerType.BACKGROUND,
                    source = AvatarAssetSource.GradientBackground(0xFF1B4332, 0xFF40916C, "Gym"),
                    isPremium = true,
                    packId = "pack_monster_trainers",
                    coinCost = 25
                ),
                AvatarLayerItem(
                    id = "mt_accessory_belt",
                    name = "Monster Ball Belt",
                    layerType = AvatarLayerType.ACCESSORY,
                    source = AvatarAssetSource.VectorRes(R.drawable.accessory_belt),
                    isPremium = true,
                    packId = "pack_monster_trainers",
                    coinCost = 60
                )
            )
        ),

        AvatarContentPack(
            id = "pack_space_explorer",
            name = "Space Explorer",
            description = "Blast off into the cosmos! Astronaut suit, galaxy backgrounds & star FX.",
            coverImageUrl = "https://placehold.co/400x300/10002b/c77dff?text=Space+Explorer",
            accentColor = 0xFF7B2FBE,
            isTrending = false,
            isNew = true,
            packPrice = 2000,  // Total XP cost
            billingProductId = "pack_space_explorer_v1",
            items = listOf(
                AvatarLayerItem(
                    id = "space_outfit_astronaut",
                    name = "Astronaut Suit",
                    layerType = AvatarLayerType.OUTFIT,
                    source = AvatarAssetSource.VectorRes(R.drawable.outfit_astronaut),
                    tintColor = 0xFFE0E0E0,
                    isPremium = true,
                    packId = "pack_space_explorer",
                    coinCost = 800
                ),
                AvatarLayerItem(
                    id = "space_bg_galaxy",
                    name = "Galaxy",
                    layerType = AvatarLayerType.BACKGROUND,
                    source = AvatarAssetSource.GradientBackground(0xFF10002B, 0xFF7B2FBE, "Galaxy"),
                    isPremium = true,
                    packId = "pack_space_explorer",
                    coinCost = 300
                ),
                AvatarLayerItem(
                    id = "space_bg_nebula",
                    name = "Nebula Storm",
                    layerType = AvatarLayerType.BACKGROUND,
                    source = AvatarAssetSource.GradientBackground(0xFF1B263B, 0xFF415A77, "Nebula"),
                    isPremium = true,
                    packId = "pack_space_explorer",
                    coinCost = 300
                ),
                AvatarLayerItem(
                    id = "space_fx_stars",
                    name = "Star Trail FX",
                    layerType = AvatarLayerType.SPECIAL_FX,
                    source = AvatarAssetSource.VectorRes(R.drawable.fx_stars),
                    isPremium = true,
                    packId = "pack_space_explorer",
                    coinCost = 600
                )
            )
        ),

        AvatarContentPack(
            id = "pack_superhero",
            name = "Superheroes",
            description = "Save the world! Epic costumes, lightning FX & hero masks.",
            coverImageUrl = "https://placehold.co/400x300/d62828/f7b731?text=Superheroes",
            accentColor = 0xFFD62828,
            isTrending = false,
            isNew = false,
            packPrice = 2300,  // Total XP cost
            billingProductId = "pack_superhero_v1",
            items = listOf(
                AvatarLayerItem(
                    id = "hero_outfit_red",
                    name = "Red Cape Suit",
                    layerType = AvatarLayerType.OUTFIT,
                    source = AvatarAssetSource.VectorRes(R.drawable.outfit_ninja),
                    tintColor = 0xFFD62828,
                    isPremium = true,
                    packId = "pack_superhero",
                    coinCost = 800
                ),
                AvatarLayerItem(
                    id = "hero_outfit_blue",
                    name = "Blue Cape Suit",
                    layerType = AvatarLayerType.OUTFIT,
                    source = AvatarAssetSource.VectorRes(R.drawable.outfit_ninja),
                    tintColor = 0xFF023E8A,
                    isPremium = true,
                    packId = "pack_superhero",
                    coinCost = 800
                ),
                AvatarLayerItem(
                    id = "hero_bg_city",
                    name = "City Skyline",
                    layerType = AvatarLayerType.BACKGROUND,
                    source = AvatarAssetSource.GradientBackground(0xFF03071E, 0xFF370617, "City Night"),
                    isPremium = true,
                    packId = "pack_superhero",
                    coinCost = 300
                ),
                AvatarLayerItem(
                    id = "hero_fx_lightning",
                    name = "Lightning Aura",
                    layerType = AvatarLayerType.SPECIAL_FX,
                    source = AvatarAssetSource.VectorRes(R.drawable.fx_lightning),
                    isPremium = true,
                    packId = "pack_superhero",
                    coinCost = 250
                ),
                AvatarLayerItem(
                    id = "hero_accessory_mask",
                    name = "Hero Mask",
                    layerType = AvatarLayerType.ACCESSORY,
                    source = AvatarAssetSource.VectorRes(R.drawable.accessory_mask),
                    tintColor = 0xFFD62828,
                    isPremium = true,
                    packId = "pack_superhero",
                    coinCost = 150
                )
            )
        )
    )

    fun allFreeItems(): List<AvatarLayerItem> =
        freeBackgrounds + freeHair + freeOutfits

    fun allPremiumItems(): List<AvatarLayerItem> =
        premiumPacks.flatMap { it.items }

    fun allContentPacks(): List<AvatarContentPack> =
        premiumPacks
}