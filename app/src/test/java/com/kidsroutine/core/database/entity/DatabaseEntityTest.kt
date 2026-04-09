package com.kidsroutine.core.database.entity

import org.junit.Assert.*
import org.junit.Test

class DatabaseEntityTest {

    // ── AvatarEntity defaults ───────────────────────────────────────

    @Test
    fun `AvatarEntity default gender is BOY`() {
        val entity = AvatarEntity(userId = "u1")
        assertEquals("BOY", entity.gender)
    }

    @Test
    fun `AvatarEntity default skinTone`() {
        val entity = AvatarEntity(userId = "u1")
        assertEquals(0xFFFFDBAD, entity.skinTone)
    }

    @Test
    fun `AvatarEntity all optional fields default to null`() {
        val entity = AvatarEntity(userId = "u1")
        assertNull(entity.activeBackgroundId)
        assertNull(entity.activeHairId)
        assertNull(entity.activeOutfitId)
        assertNull(entity.activeShoesId)
        assertNull(entity.activeAccessoryId)
        assertNull(entity.activeSpecialFxId)
        assertNull(entity.activeEyeStyleId)
        assertNull(entity.eyeShapeId)
        assertNull(entity.mouthShapeId)
        assertNull(entity.eyebrowStyleId)
        assertNull(entity.faceShapeId)
        assertNull(entity.activeFaceDetailId)
        assertNull(entity.hairColorOverride)
    }

    @Test
    fun `AvatarEntity default unlocked items is empty JSON array`() {
        val entity = AvatarEntity(userId = "u1")
        assertEquals("[]", entity.unlockedItemIdsJson)
    }

    @Test
    fun `AvatarEntity default owned packs is empty JSON array`() {
        val entity = AvatarEntity(userId = "u1")
        assertEquals("[]", entity.ownedPackIdsJson)
    }

    @Test
    fun `AvatarEntity stores all fields`() {
        val entity = AvatarEntity(
            userId = "u1",
            gender = "GIRL",
            skinTone = 0xFF8D5524,
            activeHairId = "hair_1",
            activeOutfitId = "outfit_2",
            hairColorOverride = 0xFF000000,
            unlockedItemIdsJson = "[\"item1\",\"item2\"]"
        )
        assertEquals("GIRL", entity.gender)
        assertEquals("hair_1", entity.activeHairId)
        assertEquals(0xFF000000, entity.hairColorOverride)
    }

    // ── PetEntity defaults ──────────────────────────────────────────

    @Test
    fun `PetEntity default name is empty`() {
        val entity = PetEntity(petId = "p1", userId = "u1", species = "DOG")
        assertEquals("", entity.name)
    }

    @Test
    fun `PetEntity default stage is EGG`() {
        val entity = PetEntity(petId = "p1", userId = "u1", species = "CAT")
        assertEquals("EGG", entity.stage)
    }

    @Test
    fun `PetEntity default happiness is 80`() {
        val entity = PetEntity(petId = "p1", userId = "u1", species = "DOG")
        assertEquals(80, entity.happiness)
    }

    @Test
    fun `PetEntity default energy is 80`() {
        val entity = PetEntity(petId = "p1", userId = "u1", species = "DOG")
        assertEquals(80, entity.energy)
    }

    @Test
    fun `PetEntity default isPremium is false`() {
        val entity = PetEntity(petId = "p1", userId = "u1", species = "DOG")
        assertFalse(entity.isPremium)
    }

    @Test
    fun `PetEntity stores all fields`() {
        val entity = PetEntity(
            petId = "p1",
            userId = "u1",
            species = "DRAGON",
            name = "Fido",
            stage = "TEEN",
            happiness = 60,
            energy = 40,
            totalFed = 100,
            daysAlive = 30,
            longestHappyStreak = 15,
            isPremium = true,
            accessoryId = "acc_hat"
        )
        assertEquals("DRAGON", entity.species)
        assertEquals("Fido", entity.name)
        assertEquals("TEEN", entity.stage)
        assertEquals(100, entity.totalFed)
        assertEquals("acc_hat", entity.accessoryId)
    }

    // ── TaskInstanceEntity defaults ─────────────────────────────────

    @Test
    fun `TaskInstanceEntity default status is PENDING`() {
        val entity = TaskInstanceEntity(
            instanceId = "i1",
            templateId = "t1",
            taskJson = "{}",
            assignedDate = "2026-01-01",
            userId = "u1",
            familyId = "f1"
        )
        assertEquals("PENDING", entity.status)
    }

    @Test
    fun `TaskInstanceEntity default completedAt is 0`() {
        val entity = TaskInstanceEntity(
            instanceId = "i1",
            templateId = "t1",
            taskJson = "{}",
            assignedDate = "2026-01-01",
            userId = "u1",
            familyId = "f1"
        )
        assertEquals(0L, entity.completedAt)
    }

    @Test
    fun `TaskInstanceEntity default injectedByChallengeId is null`() {
        val entity = TaskInstanceEntity(
            instanceId = "i1",
            templateId = "t1",
            taskJson = "{}",
            assignedDate = "2026-01-01",
            userId = "u1",
            familyId = "f1"
        )
        assertNull(entity.injectedByChallengeId)
    }

    @Test
    fun `TaskInstanceEntity stores challenge injection`() {
        val entity = TaskInstanceEntity(
            instanceId = "i1",
            templateId = "t1",
            taskJson = "{\"title\":\"test\"}",
            assignedDate = "2026-01-01",
            userId = "u1",
            familyId = "f1",
            injectedByChallengeId = "ch1",
            status = "COMPLETED",
            completedAt = 12345L
        )
        assertEquals("ch1", entity.injectedByChallengeId)
        assertEquals("COMPLETED", entity.status)
        assertEquals(12345L, entity.completedAt)
    }

    // ── TaskProgressEntity defaults ─────────────────────────────────

    @Test
    fun `TaskProgressEntity default syncedToFirestore is false`() {
        val entity = TaskProgressEntity(
            taskInstanceId = "t1",
            userId = "u1",
            familyId = "f1",
            date = "2026-01-01",
            status = "PENDING",
            completionTime = null,
            validationStatus = "PENDING",
            photoUrl = null
        )
        assertFalse(entity.syncedToFirestore)
    }

    @Test
    fun `TaskProgressEntity default taskTitle is empty`() {
        val entity = TaskProgressEntity(
            taskInstanceId = "t1",
            userId = "u1",
            familyId = "f1",
            date = "2026-01-01",
            status = "PENDING",
            completionTime = null,
            validationStatus = "PENDING",
            photoUrl = null
        )
        assertEquals("", entity.taskTitle)
    }

    @Test
    fun `TaskProgressEntity stores all fields`() {
        val entity = TaskProgressEntity(
            taskInstanceId = "t1",
            userId = "u1",
            familyId = "f1",
            date = "2026-04-09",
            status = "COMPLETED",
            completionTime = 99999L,
            validationStatus = "APPROVED",
            photoUrl = "https://example.com/photo.jpg",
            taskTitle = "Brush teeth",
            syncedToFirestore = true
        )
        assertEquals("COMPLETED", entity.status)
        assertEquals(99999L, entity.completionTime)
        assertEquals("APPROVED", entity.validationStatus)
        assertTrue(entity.syncedToFirestore)
    }

    // ── UserEntity defaults ─────────────────────────────────────────

    @Test
    fun `UserEntity default email is empty`() {
        val entity = UserEntity(
            userId = "u1",
            role = "CHILD",
            familyId = "f1",
            displayName = "Alice"
        )
        assertEquals("", entity.email)
    }

    @Test
    fun `UserEntity default xp is 0`() {
        val entity = UserEntity(
            userId = "u1",
            role = "CHILD",
            familyId = "f1",
            displayName = "Alice"
        )
        assertEquals(0, entity.xp)
    }

    @Test
    fun `UserEntity default level is 1`() {
        val entity = UserEntity(
            userId = "u1",
            role = "CHILD",
            familyId = "f1",
            displayName = "Alice"
        )
        assertEquals(1, entity.level)
    }

    @Test
    fun `UserEntity default isAdmin is false`() {
        val entity = UserEntity(
            userId = "u1",
            role = "CHILD",
            familyId = "f1",
            displayName = "Alice"
        )
        assertFalse(entity.isAdmin)
    }

    @Test
    fun `UserEntity stores all fields`() {
        val entity = UserEntity(
            userId = "u1",
            role = "PARENT",
            familyId = "f1",
            displayName = "Bob",
            email = "bob@example.com",
            avatarUrl = "https://example.com/avatar.png",
            isAdmin = true,
            xp = 5000,
            level = 15,
            streak = 30,
            lastActiveAt = 12345L,
            createdAt = 67890L,
            totalXpEarned = 8000
        )
        assertEquals("PARENT", entity.role)
        assertEquals("bob@example.com", entity.email)
        assertTrue(entity.isAdmin)
        assertEquals(5000, entity.xp)
        assertEquals(15, entity.level)
        assertEquals(30, entity.streak)
        assertEquals(8000, entity.totalXpEarned)
    }

    @Test
    fun `UserEntity default totalXpEarned is 0`() {
        val entity = UserEntity(
            userId = "u1",
            role = "CHILD",
            familyId = "f1",
            displayName = "Alice"
        )
        assertEquals(0, entity.totalXpEarned)
    }
}
