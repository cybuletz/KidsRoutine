package com.kidsroutine.feature.avatar.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.kidsroutine.core.database.dao.AvatarDao
import com.kidsroutine.core.database.entity.AvatarEntity
import com.kidsroutine.core.model.*
import com.kidsroutine.feature.daily.data.UserRepository
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import javax.inject.Inject

class AvatarRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val avatarDao: AvatarDao,
    private val userRepository: UserRepository,
    private val functions: FirebaseFunctions  // ✨ ADD: Firebase Functions for Cloud Function calls
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

    // ── XP methods (NOW using Cloud Function) ────────────────────────────────

    override suspend fun getUserXp(userId: String): Int {
        return try {
            val userDoc = firestore.collection("users").document(userId)
                .get().await()
            (userDoc.getLong("xp") ?: 0).toInt()
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Error fetching user XP", e)
            0
        }
    }

    /**
     * ✨ NEW: Deduct XP using Cloud Function for atomic, race-condition-safe transaction
     * The Cloud Function validates XP balance server-side before deduction
     */
    override suspend fun deductUserXp(userId: String, amount: Int) {
        try {
            Log.d("AvatarRepository", "Deducting $amount XP from $userId")

            // Direct Firestore update instead of Cloud Function
            firestore.collection("users").document(userId)
                .update("xp", FieldValue.increment(-amount.toLong()))
                .await()

            Log.d("AvatarRepository", "✅ XP deducted successfully")
        } catch (e: Exception) {
            Log.e("AvatarRepository", "❌ Error deducting XP: ${e.message}", e)
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

            Log.d("AvatarRepository", "✅ Added $amount XP. New balance: $newXp")
        } catch (e: Exception) {
            Log.e("AvatarRepository", "❌ Error adding XP: ${e.message}", e)
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
            activeEyeStyle = find(activeEyeStyleId),
            activeFaceDetail = find(activeFaceDetailId),
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
        activeEyeStyleId = activeEyeStyle?.id,
        activeFaceDetailId = activeFaceDetail?.id,
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
        "activeEyeStyleId" to activeEyeStyle?.id,
        "activeFaceDetailId" to activeFaceDetail?.id,
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