package com.kidsroutine.core.model

import androidx.compose.ui.graphics.Color

// ─── Gender ────────────────────────────────────────────────────────────────
enum class AvatarGender { BOY, GIRL }

/** Default warm-brown hair colour used when no override or item tint is set */
const val DEFAULT_HAIR_COLOR: Long = 0xFF3D2B1F

// ─── Layer Types ───────────────────────────────────────────────────────────
enum class AvatarLayerType {
    BACKGROUND,
    SKIN_BASE,
    HAIR,
    OUTFIT,
    SHOES,
    ACCESSORY,
    SPECIAL_FX,
    EYE_STYLE,    // custom eye colour / shape
    FACE_DETAIL   // freckles, blush patterns, face stickers
}

// ─── Asset Source ──────────────────────────────────────────────────────────
sealed class AvatarAssetSource {
    /** A vector drawable resource ID (SVG via VectorDrawable) */
    data class VectorRes(val resId: Int) : AvatarAssetSource()
    /** A remote URL (for premium/downloaded packs) */
    data class RemoteUrl(val url: String) : AvatarAssetSource()
    /** A gradient background defined in-app */
    data class GradientBackground(
        val topColor: Long,
        val bottomColor: Long,
        val label: String
    ) : AvatarAssetSource()
}

// ─── Single Avatar Layer Item ───────────────────────────────────────────────
data class AvatarLayerItem(
    val id: String,
    val name: String,
    val layerType: AvatarLayerType,
    val source: AvatarAssetSource,
    val tintColor: Long? = null,          // optional recolour (for hair, skin)
    val compatibleGenders: Set<AvatarGender> = setOf(AvatarGender.BOY, AvatarGender.GIRL),
    val isPremium: Boolean = false,
    val packId: String? = null,           // which content pack this belongs to
    val coinCost: Int = 0,
    val sortOrder: Int = 0
)

// ─── Content Pack (e.g. "Ninja Warriors", "Space Explorer") ─────────────────
data class AvatarContentPack(
    val id: String,
    val name: String,
    val description: String,
    val coverImageUrl: String,            // shown in shop grid
    val accentColor: Long,
    val items: List<AvatarLayerItem>,
    val packPrice: Int,                   // total coin price for the whole pack
    val isTrending: Boolean = false,
    val isNew: Boolean = false,
    val isPremiumPack: Boolean = true,    // requires real-money purchase
    val billingProductId: String? = null  // Google Play product ID
)

// ─── The full Avatar State (what a child has equipped) ──────────────────────
data class AvatarState(
    val userId: String,
    val gender: AvatarGender = AvatarGender.BOY,
    val skinTone: Long = 0xFFFFDBAD,     // hex ARGB
    val activeBackground: AvatarLayerItem? = null,
    val activeHair: AvatarLayerItem? = null,
    val hairColor: Long? = null,          // override hair tint (separate colour picker)
    val activeOutfit: AvatarLayerItem? = null,
    val activeShoes: AvatarLayerItem? = null,
    val activeAccessory: AvatarLayerItem? = null,
    val activeSpecialFx: AvatarLayerItem? = null,
    val activeEyeStyle: AvatarLayerItem? = null,    // eye colour
    val eyeShape: String? = null,                   // eye shape id (almond, round, cat, wide, narrow)
    val mouthShape: String? = null,                 // mouth shape id (smile, grin, open, smirk, pout, laugh)
    val eyebrowStyle: String? = null,               // eyebrow style id (natural, arched, thick, thin, flat, curved)
    val activeFaceDetail: AvatarLayerItem? = null,  // face variations (dimples, freckles, beauty mark)
    val unlockedItemIds: Set<String> = emptySet(),
    val ownedPackIds: Set<String> = emptySet()
) {
    fun activeLayers(): List<AvatarLayerItem> = listOfNotNull(
        activeBackground,
        activeHair,
        activeOutfit,
        activeShoes,
        activeAccessory,
        activeSpecialFx,
        activeEyeStyle,
        activeFaceDetail
    ).sortedBy { it.layerType.ordinal }

    /** Resolved hair colour: explicit override → item tint → default brown */
    val resolvedHairColor: Long get() = hairColor ?: activeHair?.tintColor ?: DEFAULT_HAIR_COLOR
}

// ─── UI tab for the customization screen ────────────────────────────────────
enum class AvatarCustomizationTab(val label: String, val emoji: String) {
    BACKGROUND("Scenes", "🌄"),
    HAIR("Hair", "💇"),
    EYES("Eyes", "👁️"),
    FACE("Face", "😊"),
    OUTFIT("Outfit", "👕"),
    SHOES("Shoes", "👟"),
    ACCESSORY("Extras", "🎩"),
    SPECIAL_FX("FX", "✨")
}