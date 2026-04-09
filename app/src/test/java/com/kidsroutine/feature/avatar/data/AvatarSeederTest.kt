package com.kidsroutine.feature.avatar.data

import com.kidsroutine.core.model.AvatarLayerType
import org.junit.Assert.*
import org.junit.Test

class AvatarSeederTest {

    // ── Free item collections non-empty ─────────────────────────────

    @Test
    fun `freeBackgrounds is not empty`() {
        assertTrue(AvatarSeeder.freeBackgrounds.isNotEmpty())
    }

    @Test
    fun `freeHair is not empty`() {
        assertTrue(AvatarSeeder.freeHair.isNotEmpty())
    }

    @Test
    fun `freeOutfits is not empty`() {
        assertTrue(AvatarSeeder.freeOutfits.isNotEmpty())
    }

    @Test
    fun `freeShoes is not empty`() {
        assertTrue(AvatarSeeder.freeShoes.isNotEmpty())
    }

    @Test
    fun `freeAccessories is not empty`() {
        assertTrue(AvatarSeeder.freeAccessories.isNotEmpty())
    }

    @Test
    fun `freeEyeStyles is not empty`() {
        assertTrue(AvatarSeeder.freeEyeStyles.isNotEmpty())
    }

    @Test
    fun `freeFaceDetails is not empty`() {
        assertTrue(AvatarSeeder.freeFaceDetails.isNotEmpty())
    }

    @Test
    fun `freeSpecialFx is not empty`() {
        assertTrue(AvatarSeeder.freeSpecialFx.isNotEmpty())
    }

    // ── Free items have correct layer types ─────────────────────────

    @Test
    fun `freeBackgrounds all have BACKGROUND type`() {
        AvatarSeeder.freeBackgrounds.forEach {
            assertEquals("${it.id} should be BACKGROUND", AvatarLayerType.BACKGROUND, it.layerType)
        }
    }

    @Test
    fun `freeHair all have HAIR type`() {
        AvatarSeeder.freeHair.forEach {
            assertEquals("${it.id} should be HAIR", AvatarLayerType.HAIR, it.layerType)
        }
    }

    @Test
    fun `freeOutfits all have OUTFIT type`() {
        AvatarSeeder.freeOutfits.forEach {
            assertEquals("${it.id} should be OUTFIT", AvatarLayerType.OUTFIT, it.layerType)
        }
    }

    @Test
    fun `freeShoes all have SHOES type`() {
        AvatarSeeder.freeShoes.forEach {
            assertEquals("${it.id} should be SHOES", AvatarLayerType.SHOES, it.layerType)
        }
    }

    @Test
    fun `freeAccessories all have ACCESSORY type`() {
        AvatarSeeder.freeAccessories.forEach {
            assertEquals("${it.id} should be ACCESSORY", AvatarLayerType.ACCESSORY, it.layerType)
        }
    }

    @Test
    fun `freeEyeStyles all have EYE_STYLE type`() {
        AvatarSeeder.freeEyeStyles.forEach {
            assertEquals("${it.id} should be EYE_STYLE", AvatarLayerType.EYE_STYLE, it.layerType)
        }
    }

    @Test
    fun `freeFaceDetails all have FACE_DETAIL type`() {
        AvatarSeeder.freeFaceDetails.forEach {
            assertEquals("${it.id} should be FACE_DETAIL", AvatarLayerType.FACE_DETAIL, it.layerType)
        }
    }

    @Test
    fun `freeSpecialFx all have SPECIAL_FX type`() {
        AvatarSeeder.freeSpecialFx.forEach {
            assertEquals("${it.id} should be SPECIAL_FX", AvatarLayerType.SPECIAL_FX, it.layerType)
        }
    }

    // ── Free items are not premium ──────────────────────────────────

    @Test
    fun `free items have isPremium false`() {
        val allFree = AvatarSeeder.allFreeItems()
        allFree.forEach {
            assertFalse("Free item ${it.id} should have isPremium=false", it.isPremium)
        }
    }

    // ── Unique IDs across all free items ────────────────────────────

    @Test
    fun `all free items have unique ids`() {
        val allFree = AvatarSeeder.allFreeItems()
        val ids = allFree.map { it.id }
        assertEquals("Free items should have unique IDs", ids.size, ids.toSet().size)
    }

    @Test
    fun `all free items have non-empty ids`() {
        AvatarSeeder.allFreeItems().forEach {
            assertTrue("Item should have non-empty id", it.id.isNotBlank())
        }
    }

    @Test
    fun `all free items have non-empty names`() {
        AvatarSeeder.allFreeItems().forEach {
            assertTrue("Item ${it.id} should have non-empty name", it.name.isNotBlank())
        }
    }

    // ── Premium packs ───────────────────────────────────────────────

    @Test
    fun `premiumPacks is not empty`() {
        assertTrue(AvatarSeeder.allContentPacks().isNotEmpty())
    }

    @Test
    fun `premium packs have unique ids`() {
        val packs = AvatarSeeder.allContentPacks()
        val ids = packs.map { it.id }
        assertEquals("Packs should have unique IDs", ids.size, ids.toSet().size)
    }

    @Test
    fun `each premium pack has at least 1 item`() {
        AvatarSeeder.allContentPacks().forEach { pack ->
            assertTrue("Pack ${pack.id} should have items", pack.items.isNotEmpty())
        }
    }

    @Test
    fun `all premium pack items have isPremium true`() {
        AvatarSeeder.allContentPacks().forEach { pack ->
            pack.items.forEach { item ->
                assertTrue("Item ${item.id} in pack ${pack.id} should be premium", item.isPremium)
            }
        }
    }

    @Test
    fun `all premium pack items have correct packId`() {
        AvatarSeeder.allContentPacks().forEach { pack ->
            pack.items.forEach { item ->
                assertEquals(
                    "Item ${item.id} should reference pack ${pack.id}",
                    pack.id, item.packId
                )
            }
        }
    }

    @Test
    fun `all premium items have unique ids`() {
        val allPremium = AvatarSeeder.allPremiumItems()
        val ids = allPremium.map { it.id }
        assertEquals("Premium items should have unique IDs", ids.size, ids.toSet().size)
    }

    @Test
    fun `no id collision between free and premium items`() {
        val freeIds = AvatarSeeder.allFreeItems().map { it.id }.toSet()
        val premiumIds = AvatarSeeder.allPremiumItems().map { it.id }.toSet()
        val overlap = freeIds.intersect(premiumIds)
        assertTrue("Free and premium should have no overlapping IDs: $overlap", overlap.isEmpty())
    }

    // ── Pack metadata ───────────────────────────────────────────────

    @Test
    fun `all packs have non-empty name`() {
        AvatarSeeder.allContentPacks().forEach { pack ->
            assertTrue("Pack ${pack.id} should have non-empty name", pack.name.isNotBlank())
        }
    }

    @Test
    fun `all packs have non-empty description`() {
        AvatarSeeder.allContentPacks().forEach { pack ->
            assertTrue("Pack ${pack.id} should have description", pack.description.isNotBlank())
        }
    }

    @Test
    fun `all packs have positive price`() {
        AvatarSeeder.allContentPacks().forEach { pack ->
            assertTrue("Pack ${pack.id} should have positive price", pack.packPrice > 0)
        }
    }

    @Test
    fun `all premium items have positive coin cost`() {
        AvatarSeeder.allPremiumItems().forEach { item ->
            assertTrue("Item ${item.id} should have positive coinCost", item.coinCost > 0)
        }
    }

    // ── allFreeItems and allPremiumItems ─────────────────────────────

    @Test
    fun `allFreeItems includes all 8 collections`() {
        val all = AvatarSeeder.allFreeItems()
        val types = all.map { it.layerType }.toSet()
        assertTrue(types.contains(AvatarLayerType.BACKGROUND))
        assertTrue(types.contains(AvatarLayerType.HAIR))
        assertTrue(types.contains(AvatarLayerType.OUTFIT))
        assertTrue(types.contains(AvatarLayerType.SHOES))
        assertTrue(types.contains(AvatarLayerType.ACCESSORY))
        assertTrue(types.contains(AvatarLayerType.EYE_STYLE))
        assertTrue(types.contains(AvatarLayerType.FACE_DETAIL))
        assertTrue(types.contains(AvatarLayerType.SPECIAL_FX))
    }

    @Test
    fun `allFreeItems count matches sum of individual collections`() {
        val expected = AvatarSeeder.freeBackgrounds.size +
                AvatarSeeder.freeHair.size +
                AvatarSeeder.freeOutfits.size +
                AvatarSeeder.freeShoes.size +
                AvatarSeeder.freeAccessories.size +
                AvatarSeeder.freeEyeStyles.size +
                AvatarSeeder.freeFaceDetails.size +
                AvatarSeeder.freeSpecialFx.size
        assertEquals(expected, AvatarSeeder.allFreeItems().size)
    }

    @Test
    fun `allPremiumItems count matches pack items sum`() {
        val expected = AvatarSeeder.allContentPacks().sumOf { it.items.size }
        assertEquals(expected, AvatarSeeder.allPremiumItems().size)
    }

    // ── sortOrder integrity ─────────────────────────────────────────

    @Test
    fun `free backgrounds have ascending sort order`() {
        val orders = AvatarSeeder.freeBackgrounds.map { it.sortOrder }
        assertEquals(orders.sorted(), orders)
    }
}
