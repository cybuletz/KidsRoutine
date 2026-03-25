package com.kidsroutine.feature.avatar.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.database.dao.AvatarDao
import com.kidsroutine.core.database.entity.AvatarEntity
import com.kidsroutine.core.model.AvatarCustomization
import com.kidsroutine.core.model.AvatarItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvatarRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val avatarDao: AvatarDao
) : AvatarRepository {

    override suspend fun deductUserXp(userId: String, amount: Int) {
        try {
            firestore.collection("users").document(userId)
                .update("xp", com.google.firebase.firestore.FieldValue.increment(-amount.toLong()))
                .await()
            Log.d("AvatarRepository", "Deducted $amount XP from user $userId")
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Error deducting XP", e)
            throw e
        }
    }


    override suspend fun getAllAvatarItems(): List<AvatarItem> {
        return try {
            val snapshot = firestore.collection("avatar_items").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(AvatarItem::class.java)?.copy(itemId = doc.id)
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Error getting avatar items", e)
            emptyList()
        }
    }

    override suspend fun getAvatarItemsByCategory(category: String): List<AvatarItem> {
        return try {
            val snapshot = firestore.collection("avatar_items")
                .whereEqualTo("category", category)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(AvatarItem::class.java)?.copy(itemId = doc.id)
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Error getting items by category", e)
            emptyList()
        }
    }

    override suspend fun getUserAvatarCustomization(userId: String): AvatarCustomization {
        return try {
            val local = avatarDao.getCustomization(userId)
            if (local != null) {
                Log.d("AvatarRepository", "Got customization from local DB")
                return local.toCustomization()
            }
            val doc = firestore.collection("users").document(userId)
                .collection("avatar").document("customization").get().await()
            val customization = doc.toObject(AvatarCustomization::class.java) ?: AvatarCustomization()
            avatarDao.insertCustomization(AvatarEntity.fromCustomization(userId, customization))
            customization
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Error getting customization", e)
            AvatarCustomization()
        }
    }

    override suspend fun updateAvatarCustomization(userId: String, customization: AvatarCustomization) {
        try {
            avatarDao.insertCustomization(AvatarEntity.fromCustomization(userId, customization))
            firestore.collection("users").document(userId)
                .collection("avatar").document("customization")
                .set(customization.copy(lastUpdated = System.currentTimeMillis()))
                .await()
            Log.d("AvatarRepository", "Avatar customization updated")
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Error updating customization", e)
            throw e
        }
    }

    override suspend fun unlockAvatarItem(
        userId: String,
        itemId: String,
        currentCustomization: AvatarCustomization
    ) {
        try {
            val updated = currentCustomization.copy(
                unlockedItemIds = currentCustomization.unlockedItemIds + itemId
            )
            updateAvatarCustomization(userId, updated)
            Log.d("AvatarRepository", "Item unlocked: $itemId")
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Error unlocking item", e)
            throw e
        }
    }

    override suspend fun saveAvatarPreset(
        userId: String,
        presetName: String,
        customization: AvatarCustomization
    ) {
        try {
            firestore.collection("users").document(userId)
                .collection("avatar_presets").document(presetName)
                .set(customization)
                .await()
            Log.d("AvatarRepository", "Preset saved: $presetName")
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Error saving preset", e)
            throw e
        }
    }

    override fun observeAvatarCustomization(userId: String): Flow<AvatarCustomization> {
        return avatarDao.observeCustomization(userId)
            .map { it?.toCustomization() ?: AvatarCustomization() }
    }
}
