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

    // ── Free Shoes ─────────────────────────────────────────────────────────
    val freeShoes = listOf(
        AvatarLayerItem(
            id = "shoes_white_sneaker",
            name = "White Sneakers",
            layerType = AvatarLayerType.SHOES,
            source = AvatarAssetSource.VectorRes(R.drawable.outfit_casual),
            tintColor = 0xFFFFFFFF,
            sortOrder = 1
        ),
        AvatarLayerItem(
            id = "shoes_red_sneaker",
            name = "Red Sneakers",
            layerType = AvatarLayerType.SHOES,
            source = AvatarAssetSource.VectorRes(R.drawable.outfit_casual),
            tintColor = 0xFFE63946,
            sortOrder = 2
        ),
        AvatarLayerItem(
            id = "shoes_black_boot",
            name = "Black Boots",
            layerType = AvatarLayerType.SHOES,
            source = AvatarAssetSource.VectorRes(R.drawable.outfit_casual),
            tintColor = 0xFF1A1A1A,
            sortOrder = 3
        ),
        AvatarLayerItem(
            id = "shoes_blue_velcro",
            name = "Blue Velcro",
            layerType = AvatarLayerType.SHOES,
            source = AvatarAssetSource.VectorRes(R.drawable.outfit_casual),
            tintColor = 0xFF1565C0,
            compatibleGenders = setOf(AvatarGender.BOY),
            sortOrder = 4
        ),
        AvatarLayerItem(
            id = "shoes_pink_trainer",
            name = "Pink Trainers",
            layerType = AvatarLayerType.SHOES,
            source = AvatarAssetSource.VectorRes(R.drawable.outfit_casual),
            tintColor = 0xFFFF6B9D,
            compatibleGenders = setOf(AvatarGender.GIRL),
            sortOrder = 5
        )
    )

    // ── Free Accessories ───────────────────────────────────────────────────
    val freeAccessories = listOf(
        AvatarLayerItem(
            id = "acc_glasses_round",
            name = "Round Glasses",
            layerType = AvatarLayerType.ACCESSORY,
            source = AvatarAssetSource.VectorRes(R.drawable.accessory_cap),
            tintColor = 0xFF4A3728,
            sortOrder = 1
        ),
        AvatarLayerItem(
            id = "acc_hair_bow",
            name = "Hair Bow",
            layerType = AvatarLayerType.ACCESSORY,
            source = AvatarAssetSource.VectorRes(R.drawable.accessory_headband),
            tintColor = 0xFFFF6B9D,
            compatibleGenders = setOf(AvatarGender.GIRL),
            sortOrder = 2
        ),
        AvatarLayerItem(
            id = "acc_star_necklace",
            name = "Star Necklace",
            layerType = AvatarLayerType.ACCESSORY,
            source = AvatarAssetSource.VectorRes(R.drawable.accessory_belt),
            tintColor = 0xFFFFD700,
            sortOrder = 3
        ),
        AvatarLayerItem(
            id = "acc_bandana",
            name = "Bandana",
            layerType = AvatarLayerType.ACCESSORY,
            source = AvatarAssetSource.VectorRes(R.drawable.accessory_headband),
            tintColor = 0xFFE63946,
            sortOrder = 4
        ),
        AvatarLayerItem(
            id = "acc_wristband",
            name = "Wristband",
            layerType = AvatarLayerType.ACCESSORY,
            source = AvatarAssetSource.VectorRes(R.drawable.accessory_belt),
            tintColor = 0xFF2DC653,
            sortOrder = 5
        )
    )

    // ── Free Eye Styles ────────────────────────────────────────────────────
    val freeEyeStyles = listOf(
        AvatarLayerItem(
            id = "eyes_brown",
            name = "Warm Brown",
            layerType = AvatarLayerType.EYE_STYLE,
            source = AvatarAssetSource.GradientBackground(0xFF5D3A1A, 0xFF3D2B1F, "Brown"),
            tintColor = 0xFF3D2B1F,
            sortOrder = 1
        ),
        AvatarLayerItem(
            id = "eyes_blue",
            name = "Sky Blue",
            layerType = AvatarLayerType.EYE_STYLE,
            source = AvatarAssetSource.GradientBackground(0xFF1565C0, 0xFF42A5F5, "Blue"),
            tintColor = 0xFF1565C0,
            sortOrder = 2
        ),
        AvatarLayerItem(
            id = "eyes_green",
            name = "Forest Green",
            layerType = AvatarLayerType.EYE_STYLE,
            source = AvatarAssetSource.GradientBackground(0xFF2E7D32, 0xFF66BB6A, "Green"),
            tintColor = 0xFF2E7D32,
            sortOrder = 3
        ),
        AvatarLayerItem(
            id = "eyes_grey",
            name = "Steel Grey",
            layerType = AvatarLayerType.EYE_STYLE,
            source = AvatarAssetSource.GradientBackground(0xFF546E7A, 0xFF90A4AE, "Grey"),
            tintColor = 0xFF546E7A,
            sortOrder = 4
        ),
        AvatarLayerItem(
            id = "eyes_hazel",
            name = "Honey Hazel",
            layerType = AvatarLayerType.EYE_STYLE,
            source = AvatarAssetSource.GradientBackground(0xFF795548, 0xFFA1887F, "Hazel"),
            tintColor = 0xFF795548,
            sortOrder = 5
        ),
        AvatarLayerItem(
            id = "eyes_purple",
            name = "Mystic Purple",
            layerType = AvatarLayerType.EYE_STYLE,
            source = AvatarAssetSource.GradientBackground(0xFF6A1B9A, 0xFFAB47BC, "Purple"),
            tintColor = 0xFF6A1B9A,
            sortOrder = 6
        )
    )

    // ── Free Face Details ──────────────────────────────────────────────────
    val freeFaceDetails = listOf(
        AvatarLayerItem(
            id = "face_freckles",
            name = "Sun Freckles",
            layerType = AvatarLayerType.FACE_DETAIL,
            source = AvatarAssetSource.GradientBackground(0xFFC68642, 0xFFE0AC69, "Freckles"),
            tintColor = 0xFFC68642,
            sortOrder = 1
        ),
        AvatarLayerItem(
            id = "face_star_sticker",
            name = "Star Sticker",
            layerType = AvatarLayerType.FACE_DETAIL,
            source = AvatarAssetSource.GradientBackground(0xFFFFD700, 0xFFFFF176, "Star"),
            tintColor = 0xFFFFD700,
            sortOrder = 2
        ),
        AvatarLayerItem(
            id = "face_extra_blush",
            name = "Rosy Cheeks",
            layerType = AvatarLayerType.FACE_DETAIL,
            source = AvatarAssetSource.GradientBackground(0xFFFFB3BA, 0xFFFF8FA3, "Blush"),
            tintColor = 0xFFFFB3BA,
            sortOrder = 3
        ),
        AvatarLayerItem(
            id = "face_heart_sticker",
            name = "Heart Sticker",
            layerType = AvatarLayerType.FACE_DETAIL,
            source = AvatarAssetSource.GradientBackground(0xFFE91E63, 0xFFF48FB1, "Heart"),
            tintColor = 0xFFE91E63,
            sortOrder = 4
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
        ),
        AvatarLayerItem(
            id = "hair_short_spiky_brown",
            name = "Spiky Brown",
            layerType = AvatarLayerType.HAIR,
            source = AvatarAssetSource.VectorRes(R.drawable.hair_short_boy),
            tintColor = 0xFF5C3317,
            compatibleGenders = setOf(AvatarGender.BOY),
            sortOrder = 6
        ),
        AvatarLayerItem(
            id = "hair_long_wavy_brown",
            name = "Wavy Brown",
            layerType = AvatarLayerType.HAIR,
            source = AvatarAssetSource.VectorRes(R.drawable.hair_long_girl),
            tintColor = 0xFF8B5E3C,
            compatibleGenders = setOf(AvatarGender.GIRL),
            sortOrder = 7
        ),
        AvatarLayerItem(
            id = "hair_bun_black",
            name = "High Bun",
            layerType = AvatarLayerType.HAIR,
            source = AvatarAssetSource.VectorRes(R.drawable.hair_ponytail),
            tintColor = 0xFF1A1A1A,
            compatibleGenders = setOf(AvatarGender.GIRL),
            sortOrder = 8
        ),
        AvatarLayerItem(
            id = "hair_bob_red",
            name = "Bob Red",
            layerType = AvatarLayerType.HAIR,
            source = AvatarAssetSource.VectorRes(R.drawable.hair_curly),
            tintColor = 0xFFB03A2E,
            compatibleGenders = setOf(AvatarGender.BOY, AvatarGender.GIRL),
            sortOrder = 9
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
        ),

        AvatarContentPack(
            id = "pack_magical_princess",
            name = "Magical Princess",
            description = "Sparkle and shine! Tiara, enchanted gown, fairy wings & castle background.",
            coverImageUrl = "https://placehold.co/400x300/FF6B9D/FFF176?text=Magical+Princess",
            accentColor = 0xFFE91E63,
            isTrending = true,
            isNew = true,
            packPrice = 500,   // Total XP cost
            billingProductId = "pack_magical_princess_v1",
            items = listOf(
                AvatarLayerItem(
                    id = "princess_outfit_sparkle",
                    name = "Enchanted Gown",
                    layerType = AvatarLayerType.OUTFIT,
                    source = AvatarAssetSource.VectorRes(R.drawable.outfit_casual),
                    tintColor = 0xFFE91E63,
                    isPremium = true,
                    packId = "pack_magical_princess",
                    coinCost = 200
                ),
                AvatarLayerItem(
                    id = "princess_accessory_tiara",
                    name = "Princess Tiara",
                    layerType = AvatarLayerType.ACCESSORY,
                    source = AvatarAssetSource.VectorRes(R.drawable.accessory_cap),
                    tintColor = 0xFFFFD700,
                    isPremium = true,
                    packId = "pack_magical_princess",
                    coinCost = 150
                ),
                AvatarLayerItem(
                    id = "princess_bg_castle",
                    name = "Enchanted Castle",
                    layerType = AvatarLayerType.BACKGROUND,
                    source = AvatarAssetSource.GradientBackground(0xFFFF6B9D, 0xFFFFF176, "Castle"),
                    isPremium = true,
                    packId = "pack_magical_princess",
                    coinCost = 100
                ),
                AvatarLayerItem(
                    id = "princess_fx_sparkle",
                    name = "Fairy Wings FX",
                    layerType = AvatarLayerType.SPECIAL_FX,
                    source = AvatarAssetSource.VectorRes(R.drawable.fx_stars),
                    tintColor = 0xFFFF6B9D,
                    isPremium = true,
                    packId = "pack_magical_princess",
                    coinCost = 180
                ),
                AvatarLayerItem(
                    id = "princess_hair_golden",
                    name = "Golden Waves",
                    layerType = AvatarLayerType.HAIR,
                    source = AvatarAssetSource.VectorRes(R.drawable.hair_long_girl),
                    tintColor = 0xFFFFD700,
                    compatibleGenders = setOf(AvatarGender.GIRL),
                    isPremium = true,
                    packId = "pack_magical_princess",
                    coinCost = 120
                ),
                AvatarLayerItem(
                    id = "princess_face_sparkle",
                    name = "Sparkle Cheeks",
                    layerType = AvatarLayerType.FACE_DETAIL,
                    source = AvatarAssetSource.GradientBackground(0xFFFFD700, 0xFFFFF9C4, "Sparkle"),
                    tintColor = 0xFFFFD700,
                    isPremium = true,
                    packId = "pack_magical_princess",
                    coinCost = 80
                )
            )
        ),

        AvatarContentPack(
            id = "pack_dino_explorer",
            name = "Dino Explorer",
            description = "Roar into adventure! Explorer hat, jungle bg, leather boots & a dino roar FX.",
            coverImageUrl = "https://placehold.co/400x300/2D6A4F/FFD93D?text=Dino+Explorer",
            accentColor = 0xFF2D6A4F,
            isTrending = false,
            isNew = true,
            packPrice = 400,   // Total XP cost
            billingProductId = "pack_dino_explorer_v1",
            items = listOf(
                AvatarLayerItem(
                    id = "dino_outfit_adventure",
                    name = "Explorer Jacket",
                    layerType = AvatarLayerType.OUTFIT,
                    source = AvatarAssetSource.VectorRes(R.drawable.outfit_trainer),
                    tintColor = 0xFF6D4C41,
                    isPremium = true,
                    packId = "pack_dino_explorer",
                    coinCost = 180
                ),
                AvatarLayerItem(
                    id = "dino_accessory_hat",
                    name = "Explorer Hat",
                    layerType = AvatarLayerType.ACCESSORY,
                    source = AvatarAssetSource.VectorRes(R.drawable.accessory_cap),
                    tintColor = 0xFF8D6E63,
                    isPremium = true,
                    packId = "pack_dino_explorer",
                    coinCost = 120
                ),
                AvatarLayerItem(
                    id = "dino_shoes_boots",
                    name = "Leather Boots",
                    layerType = AvatarLayerType.SHOES,
                    source = AvatarAssetSource.VectorRes(R.drawable.outfit_casual),
                    tintColor = 0xFF4E342E,
                    isPremium = true,
                    packId = "pack_dino_explorer",
                    coinCost = 110
                ),
                AvatarLayerItem(
                    id = "dino_bg_jungle",
                    name = "Dino Jungle",
                    layerType = AvatarLayerType.BACKGROUND,
                    source = AvatarAssetSource.GradientBackground(0xFF1B5E20, 0xFF4CAF50, "Jungle"),
                    isPremium = true,
                    packId = "pack_dino_explorer",
                    coinCost = 90
                ),
                AvatarLayerItem(
                    id = "dino_fx_roar",
                    name = "Dino Roar FX",
                    layerType = AvatarLayerType.SPECIAL_FX,
                    source = AvatarAssetSource.VectorRes(R.drawable.fx_fire),
                    tintColor = 0xFF76FF03,
                    isPremium = true,
                    packId = "pack_dino_explorer",
                    coinCost = 150
                )
            )
        )
    )

    fun allFreeItems(): List<AvatarLayerItem> =
        freeBackgrounds + freeHair + freeOutfits + freeShoes + freeAccessories + freeEyeStyles + freeFaceDetails

    fun allPremiumItems(): List<AvatarLayerItem> =
        premiumPacks.flatMap { it.items }

    fun allContentPacks(): List<AvatarContentPack> =
        premiumPacks
}