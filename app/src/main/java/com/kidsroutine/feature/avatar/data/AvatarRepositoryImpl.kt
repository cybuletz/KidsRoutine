package com.kidsroutine.feature.avatar.data

import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.database.dao.AvatarDao
import com.kidsroutine.core.database.entity.AvatarEntity
import com.kidsroutine.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import javax.inject.Inject

class AvatarRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val avatarDao: AvatarDao
) : AvatarRepository {

    // ── Interface implementations ─────────────────────────────────────────────

    override suspend fun getAvatar(userId: String): AvatarState? {
        return avatarDao.getAvatar(userId)?.toAvatarState()
    }

    override fun observeAvatar(userId: String): Flow<AvatarState?> {
        return avatarDao.observeAvatar(userId).map { it?.toAvatarState() }
    }

    override suspend fun saveAvatar(state: AvatarState) {
        avatarDao.saveAvatar(state.toEntity())
        syncToFirestore(state)
    }

    override suspend fun deleteAvatar(userId: String) {
        avatarDao.deleteAvatar(userId)
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

    // ── JSON helpers (no extra dependency needed) ─────────────────────────────

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