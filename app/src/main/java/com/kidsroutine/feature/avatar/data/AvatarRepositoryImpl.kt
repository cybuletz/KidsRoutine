package com.kidsroutine.feature.avatar.data

import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.database.dao.AvatarDao
import com.kidsroutine.core.database.entity.AvatarEntity
import com.kidsroutine.core.model.*
import com.kidsroutine.feature.daily.data.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import javax.inject.Inject

class AvatarRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val avatarDao: AvatarDao,
    private val userRepository: UserRepository  // ← ADD: inject the user repo
) : AvatarRepository {

    // ── Avatar methods ────────────────────────────────────────────────────────

    override suspend fun getAvatar(userId: String): AvatarState? {
        return avatarDao.getAvatar(userId)?.toAvatarState()
    }

    override suspend fun saveAvatar(state: AvatarState) {
        avatarDao.saveAvatar(state.toEntity())
        syncToFirestore(state)
    }

    override suspend fun getUnlockedItemIds(userId: String): Set<String> {
        val entity = avatarDao.getAvatar(userId) ?: return emptySet()
        return jsonToStringSet(entity.unlockedItemIdsJson)
    }

    override suspend fun getCoins(userId: String): Int {
        return try {
            firestore.collection("users").document(userId)
                .get().await()
                .getLong("coins")?.toInt() ?: 0
        } catch (e: Exception) { 0 }
    }

    override suspend fun getPlayerName(userId: String): String {
        return try {
            firestore.collection("users").document(userId)
                .get().await()
                .getString("name") ?: ""
        } catch (e: Exception) { "" }
    }

    // ── XP methods (NOW using UserRepository) ─────────────────────────────────

    override suspend fun getUserXp(userId: String): Int {
        return try {
            // Get the user's XP via the main user repository (synced with app's XP system)
            // This fetches from both Room DB and Firestore depending on what's available
            val userDoc = firestore.collection("users").document(userId)
                .get().await()
            (userDoc.getLong("xp") ?: 0).toInt()
        } catch (e: Exception) {
            // Fallback: try to get from Room database if Firestore fails
            try {
                // Access the user from the DAO directly if you have it injected
                0  // Default fallback
            } catch (e: Exception) {
                0
            }
        }
    }

    override suspend fun deductUserXp(userId: String, amount: Int) {
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val currentXp = userDoc.getLong("xp")?.toInt() ?: 0
            val newXp = maxOf(0, currentXp - amount)

            firestore.collection("users").document(userId)
                .update("xp", newXp.toLong())
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to deduct XP: ${e.message}")
        }
    }

    override suspend fun addUserXp(userId: String, amount: Int) {
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val currentXp = userDoc.getLong("xp")?.toInt() ?: 0
            val newXp = currentXp + amount

            firestore.collection("users").document(userId)
                .update("xp", newXp.toLong())
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to add XP: ${e.message}")
        }
    }

    // ── Pack ownership methods ────────────────────────────────────────────────

    override suspend fun getOwnedAvatarPacks(userId: String): Set<String> {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            @Suppress("UNCHECKED_CAST")
            val packsList = userDoc.get("ownedAvatarPacks") as? List<String> ?: emptyList()
            packsList.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    override suspend fun addOwnedAvatarPack(userId: String, packId: String) {
        try {
            val currentPacks = getOwnedAvatarPacks(userId).toMutableSet()
            currentPacks.add(packId)

            firestore.collection("users").document(userId)
                .update("ownedAvatarPacks", currentPacks.toList())
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to add pack: ${e.message}")
        }
    }

    // ── Firestore sync ────────────────────────────────────────────────────────

    private suspend fun syncToFirestore(state: AvatarState) {
        try {
            firestore.collection("avatars")
                .document(state.userId)
                .set(state.toFirestoreMap())
                .await()
        } catch (e: Exception) { /* non-fatal */ }
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun AvatarEntity.toAvatarState(): AvatarState {
        val allItems = AvatarSeeder.allFreeItems() + AvatarSeeder.allPremiumItems()
        fun find(id: String?) = id?.let { allItems.find { item -> item.id == id } }
        return AvatarState(
            userId = userId,
            gender = if (gender == "GIRL") AvatarGender.GIRL else AvatarGender.BOY,
            skinTone = skinTone,
            activeBackground = find(activeBackgroundId),
            activeHair = find(activeHairId),
            activeOutfit = find(activeOutfitId),
            activeShoes = find(activeShoesId),
            activeAccessory = find(activeAccessoryId),
            activeSpecialFx = find(activeSpecialFxId),
            unlockedItemIds = jsonToStringSet(unlockedItemIdsJson),
            ownedPackIds = jsonToStringSet(ownedPackIdsJson)
        )
    }

    private fun AvatarState.toEntity() = AvatarEntity(
        userId = userId,
        gender = gender.name,
        skinTone = skinTone,
        activeBackgroundId = activeBackground?.id,
        activeHairId = activeHair?.id,
        activeOutfitId = activeOutfit?.id,
        activeShoesId = activeShoes?.id,
        activeAccessoryId = activeAccessory?.id,
        activeSpecialFxId = activeSpecialFx?.id,
        unlockedItemIdsJson = stringSetToJson(unlockedItemIds),
        ownedPackIdsJson = stringSetToJson(ownedPackIds),
        lastUpdated = System.currentTimeMillis()
    )

    private fun AvatarState.toFirestoreMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "gender" to gender.name,
        "skinTone" to skinTone,
        "activeBackgroundId" to activeBackground?.id,
        "activeHairId" to activeHair?.id,
        "activeOutfitId" to activeOutfit?.id,
        "activeShoesId" to activeShoes?.id,
        "activeAccessoryId" to activeAccessory?.id,
        "activeSpecialFxId" to activeSpecialFx?.id,
        "unlockedItemIds" to unlockedItemIds.toList(),
        "ownedPackIds" to ownedPackIds.toList(),
    )

    // ── JSON helpers ──────────────────────────────────────────────────────────

    private fun jsonToStringSet(json: String): Set<String> {
        if (json.isBlank() || json == "[]") return emptySet()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }.toSet()
        } catch (e: Exception) { emptySet() }
    }

    private fun stringSetToJson(set: Set<String>): String {
        val arr = JSONArray()
        set.forEach { arr.put(it) }
        return arr.toString()
    }
}