package com.kidsroutine.feature.boss.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kidsroutine.core.model.BossModel
import com.kidsroutine.core.model.BossType
import com.kidsroutine.core.model.LootBoxRarity
import com.kidsroutine.core.model.Season
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BossRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BossRepository {

    private val bossCollection get() = firestore.collection("boss_battles")

    override suspend fun getActiveBoss(familyId: String): BossModel? {
        return try {
            Log.d(TAG, "Getting active boss for family: $familyId")
            val snapshot = bossCollection
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("isDefeated", false)
                .whereEqualTo("isExpired", false)
                .orderBy("startedAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.d(TAG, "No active boss found for family: $familyId")
                return null
            }

            val doc = snapshot.documents.first()
            val boss = mapToBossModel(doc.id, doc.data ?: return null)
            Log.d(TAG, "Loaded active boss '${boss.type.displayName}' for family: $familyId")
            boss
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active boss for family: $familyId", e)
            null
        }
    }

    override suspend fun saveBoss(boss: BossModel) {
        try {
            Log.d(TAG, "Saving boss '${boss.type.displayName}' (id=${boss.bossId})")
            val data = bossToFirestoreMap(boss)
            bossCollection.document(boss.bossId).set(data).await()
            Log.d(TAG, "Boss saved successfully: ${boss.bossId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving boss: ${boss.bossId}", e)
            throw e
        }
    }

    override suspend fun getRecentBosses(familyId: String, limit: Int): List<BossModel> {
        return try {
            Log.d(TAG, "Getting recent $limit bosses for family: $familyId")
            val snapshot = bossCollection
                .whereEqualTo("familyId", familyId)
                .orderBy("startedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val bosses = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { mapToBossModel(doc.id, it) }
            }
            Log.d(TAG, "Loaded ${bosses.size} recent bosses for family: $familyId")
            bosses
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent bosses for family: $familyId", e)
            emptyList()
        }
    }

    private fun bossToFirestoreMap(boss: BossModel): Map<String, Any?> = mapOf(
        "bossId" to boss.bossId,
        "familyId" to boss.familyId,
        "type" to boss.type.name,
        "week" to boss.week,
        "season" to boss.season.name,
        "maxHp" to boss.maxHp,
        "currentHp" to boss.currentHp,
        "damageLog" to boss.damageLog,
        "totalDamage" to boss.totalDamage,
        "victoryXpBonus" to boss.victoryXpBonus,
        "victoryLootRarity" to boss.victoryLootRarity.name,
        "defeatXpPenalty" to boss.defeatXpPenalty,
        "isDefeated" to boss.isDefeated,
        "isExpired" to boss.isExpired,
        "startedAt" to boss.startedAt,
        "deadline" to boss.deadline
    )

    @Suppress("UNCHECKED_CAST")
    private fun mapToBossModel(docId: String, data: Map<String, Any?>): BossModel = BossModel(
        bossId = data["bossId"] as? String ?: docId,
        familyId = data["familyId"] as? String ?: "",
        type = runCatching {
            BossType.valueOf(data["type"] as? String ?: "HOMEWORK_HYDRA")
        }.getOrDefault(BossType.HOMEWORK_HYDRA),
        week = data["week"] as? String ?: "",
        season = runCatching {
            Season.valueOf(data["season"] as? String ?: "NONE")
        }.getOrDefault(Season.NONE),
        maxHp = (data["maxHp"] as? Number)?.toInt() ?: 200,
        currentHp = (data["currentHp"] as? Number)?.toInt() ?: 200,
        damageLog = (data["damageLog"] as? Map<String, Number>)
            ?.mapValues { it.value.toInt() } ?: emptyMap(),
        totalDamage = (data["totalDamage"] as? Number)?.toInt() ?: 0,
        victoryXpBonus = (data["victoryXpBonus"] as? Number)?.toInt() ?: 100,
        victoryLootRarity = runCatching {
            LootBoxRarity.valueOf(data["victoryLootRarity"] as? String ?: "RARE")
        }.getOrDefault(LootBoxRarity.RARE),
        defeatXpPenalty = (data["defeatXpPenalty"] as? Number)?.toInt() ?: 20,
        isDefeated = data["isDefeated"] as? Boolean ?: false,
        isExpired = data["isExpired"] as? Boolean ?: false,
        startedAt = (data["startedAt"] as? Number)?.toLong() ?: 0L,
        deadline = (data["deadline"] as? Number)?.toLong() ?: 0L
    )

    companion object {
        private const val TAG = "BossRepository"
    }
}
