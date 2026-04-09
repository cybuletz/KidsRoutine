package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class AvatarModelTest {

    private fun makeItem(
        id: String,
        layerType: AvatarLayerType,
        tintColor: Long? = null
    ) = AvatarLayerItem(
        id = id,
        name = id,
        layerType = layerType,
        source = AvatarAssetSource.VectorRes(0),
        tintColor = tintColor
    )

    // ── activeLayers ────────────────────────────────────────────────

    @Test
    fun `activeLayers returns empty when nothing equipped`() {
        val state = AvatarState(userId = "u1")
        assertTrue(state.activeLayers().isEmpty())
    }

    @Test
    fun `activeLayers includes all equipped items`() {
        val hair = makeItem("hair1", AvatarLayerType.HAIR)
        val outfit = makeItem("outfit1", AvatarLayerType.OUTFIT)
        val state = AvatarState(
            userId = "u1",
            activeHair = hair,
            activeOutfit = outfit
        )
        val layers = state.activeLayers()
        assertEquals(2, layers.size)
        assertTrue(layers.contains(hair))
        assertTrue(layers.contains(outfit))
    }

    @Test
    fun `activeLayers sorted by layer type ordinal`() {
        val accessory = makeItem("acc1", AvatarLayerType.ACCESSORY)
        val background = makeItem("bg1", AvatarLayerType.BACKGROUND)
        val hair = makeItem("hair1", AvatarLayerType.HAIR)
        val state = AvatarState(
            userId = "u1",
            activeBackground = background,
            activeHair = hair,
            activeAccessory = accessory
        )
        val layers = state.activeLayers()
        assertEquals(3, layers.size)
        assertEquals(AvatarLayerType.BACKGROUND, layers[0].layerType)
        assertEquals(AvatarLayerType.HAIR, layers[1].layerType)
        assertEquals(AvatarLayerType.ACCESSORY, layers[2].layerType)
    }

    @Test
    fun `activeLayers includes all 8 slots when full`() {
        val state = AvatarState(
            userId = "u1",
            activeBackground = makeItem("bg", AvatarLayerType.BACKGROUND),
            activeHair = makeItem("h", AvatarLayerType.HAIR),
            activeOutfit = makeItem("o", AvatarLayerType.OUTFIT),
            activeShoes = makeItem("s", AvatarLayerType.SHOES),
            activeAccessory = makeItem("a", AvatarLayerType.ACCESSORY),
            activeSpecialFx = makeItem("fx", AvatarLayerType.SPECIAL_FX),
            activeEyeStyle = makeItem("eye", AvatarLayerType.EYE_STYLE),
            activeFaceDetail = makeItem("face", AvatarLayerType.FACE_DETAIL)
        )
        assertEquals(8, state.activeLayers().size)
    }

    // ── resolvedHairColor ───────────────────────────────────────────

    @Test
    fun `resolvedHairColor uses explicit override first`() {
        val hair = makeItem("h", AvatarLayerType.HAIR, tintColor = 0xFFFF0000)
        val state = AvatarState(
            userId = "u1",
            activeHair = hair,
            hairColor = 0xFF00FF00
        )
        assertEquals(0xFF00FF00, state.resolvedHairColor)
    }

    @Test
    fun `resolvedHairColor falls back to item tint`() {
        val hair = makeItem("h", AvatarLayerType.HAIR, tintColor = 0xFFFF0000)
        val state = AvatarState(
            userId = "u1",
            activeHair = hair,
            hairColor = null
        )
        assertEquals(0xFFFF0000, state.resolvedHairColor)
    }

    @Test
    fun `resolvedHairColor falls back to default brown`() {
        val state = AvatarState(userId = "u1", activeHair = null, hairColor = null)
        assertEquals(DEFAULT_HAIR_COLOR, state.resolvedHairColor)
    }

    @Test
    fun `resolvedHairColor falls back to default when hair has no tint`() {
        val hair = makeItem("h", AvatarLayerType.HAIR, tintColor = null)
        val state = AvatarState(userId = "u1", activeHair = hair, hairColor = null)
        assertEquals(DEFAULT_HAIR_COLOR, state.resolvedHairColor)
    }

    // ── AvatarGender enum ───────────────────────────────────────────

    @Test
    fun `AvatarGender has 2 entries`() {
        assertEquals(2, AvatarGender.entries.size)
    }

    // ── AvatarLayerType enum ────────────────────────────────────────

    @Test
    fun `AvatarLayerType has 9 entries`() {
        assertEquals(9, AvatarLayerType.entries.size)
    }

    // ── AvatarCustomizationTab ──────────────────────────────────────

    @Test
    fun `AvatarCustomizationTab has 7 entries`() {
        assertEquals(7, AvatarCustomizationTab.entries.size)
    }

    @Test
    fun `all tabs have non-empty labels`() {
        AvatarCustomizationTab.entries.forEach {
            assertTrue("${it.name} label", it.label.isNotEmpty())
        }
    }

    @Test
    fun `all tabs have non-empty emojis`() {
        AvatarCustomizationTab.entries.forEach {
            assertTrue("${it.name} emoji", it.emoji.isNotEmpty())
        }
    }

    // ── AvatarAssetSource sealed class ──────────────────────────────

    @Test
    fun `VectorRes stores resId`() {
        val source = AvatarAssetSource.VectorRes(42)
        assertEquals(42, source.resId)
    }

    @Test
    fun `RemoteUrl stores url`() {
        val source = AvatarAssetSource.RemoteUrl("https://example.com/img.png")
        assertEquals("https://example.com/img.png", source.url)
    }

    @Test
    fun `GradientBackground stores colors and label`() {
        val source = AvatarAssetSource.GradientBackground(0xFF000000, 0xFFFFFFFF, "Night Sky")
        assertEquals(0xFF000000, source.topColor)
        assertEquals(0xFFFFFFFF, source.bottomColor)
        assertEquals("Night Sky", source.label)
    }

    // ── AvatarContentPack defaults ──────────────────────────────────

    @Test
    fun `content pack defaults isPremiumPack to true`() {
        val pack = AvatarContentPack(
            id = "p1", name = "Test", description = "desc",
            coverImageUrl = "", accentColor = 0, items = emptyList(), packPrice = 100
        )
        assertTrue(pack.isPremiumPack)
    }

    // ── AvatarState defaults ────────────────────────────────────────

    @Test
    fun `default gender is BOY`() {
        val state = AvatarState(userId = "u1")
        assertEquals(AvatarGender.BOY, state.gender)
    }

    @Test
    fun `default unlockedItemIds is empty`() {
        val state = AvatarState(userId = "u1")
        assertTrue(state.unlockedItemIds.isEmpty())
    }
}
