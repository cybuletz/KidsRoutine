package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class LootBoxModelTest {

    // ── LootBoxRarity enum ──────────────────────────────────────────

    @Test
    fun `LootBoxRarity has 4 entries`() {
        assertEquals(4, LootBoxRarity.entries.size)
    }

    @Test
    fun `LootBoxRarity includes COMMON RARE EPIC LEGENDARY`() {
        val names = LootBoxRarity.entries.map { it.name }
        assertTrue(names.contains("COMMON"))
        assertTrue(names.contains("RARE"))
        assertTrue(names.contains("EPIC"))
        assertTrue(names.contains("LEGENDARY"))
    }

    // ── LootBoxRewardType enum ──────────────────────────────────────

    @Test
    fun `LootBoxRewardType has 5 entries`() {
        assertEquals(5, LootBoxRewardType.entries.size)
    }

    @Test
    fun `LootBoxRewardType includes XP_BOOST BADGE AVATAR_ITEM STREAK_SHIELD MYSTERY`() {
        val names = LootBoxRewardType.entries.map { it.name }
        assertTrue(names.contains("XP_BOOST"))
        assertTrue(names.contains("BADGE"))
        assertTrue(names.contains("AVATAR_ITEM"))
        assertTrue(names.contains("STREAK_SHIELD"))
        assertTrue(names.contains("MYSTERY"))
    }

    // ── LootBoxReward defaults ──────────────────────────────────────

    @Test
    fun `default reward type is XP_BOOST`() {
        val reward = LootBoxReward()
        assertEquals(LootBoxRewardType.XP_BOOST, reward.type)
    }

    @Test
    fun `default reward rarity is COMMON`() {
        val reward = LootBoxReward()
        assertEquals(LootBoxRarity.COMMON, reward.rarity)
    }

    @Test
    fun `default reward emoji is gift`() {
        val reward = LootBoxReward()
        assertEquals("🎁", reward.emoji)
    }

    @Test
    fun `default xpValue is 0`() {
        val reward = LootBoxReward()
        assertEquals(0, reward.xpValue)
    }

    @Test
    fun `reward generates unique ID`() {
        val reward1 = LootBoxReward()
        val reward2 = LootBoxReward()
        assertNotEquals(reward1.rewardId, reward2.rewardId)
    }

    // ── LootBox defaults ────────────────────────────────────────────

    @Test
    fun `default lootBox is not opened`() {
        val box = LootBox()
        assertFalse(box.isOpened)
    }

    @Test
    fun `default lootBox reward is null`() {
        val box = LootBox()
        assertNull(box.reward)
    }

    @Test
    fun `lootBox generates unique boxId`() {
        val box1 = LootBox()
        val box2 = LootBox()
        assertNotEquals(box1.boxId, box2.boxId)
    }

    @Test
    fun `lootBox stores reward`() {
        val reward = LootBoxReward(
            type = LootBoxRewardType.BADGE,
            rarity = LootBoxRarity.EPIC,
            title = "Dragon Badge"
        )
        val box = LootBox(reward = reward, earnedFor = "level up")
        assertNotNull(box.reward)
        assertEquals(LootBoxRewardType.BADGE, box.reward!!.type)
        assertEquals("level up", box.earnedFor)
    }
}
