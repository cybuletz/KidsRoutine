package com.kidsroutine.feature.rituals.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kidsroutine.core.model.FamilyRitual
import com.kidsroutine.core.model.RitualFrequency
import com.kidsroutine.core.model.RitualType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RitualsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RitualsRepository {

    private val collection = "family_rituals"

    override suspend fun getRituals(familyId: String): List<FamilyRitual> {
        return try {
            val snap = firestore.collection(collection)
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snap.documents.mapNotNull { doc -> docToRitual(doc) }
        } catch (e: Exception) {
            Log.e("RitualsRepo", "getRituals error", e)
            emptyList()
        }
    }

    override suspend fun getRitual(ritualId: String): FamilyRitual? {
        return try {
            val doc = firestore.collection(collection).document(ritualId).get().await()
            if (doc.exists()) docToRitual(doc) else null
        } catch (e: Exception) {
            Log.e("RitualsRepo", "getRitual error", e)
            null
        }
    }

    override suspend fun saveRitual(ritual: FamilyRitual) {
        try {
            val data = ritualToMap(ritual)
            if (ritual.ritualId.isBlank()) {
                firestore.collection(collection).add(data).await()
            } else {
                firestore.collection(collection).document(ritual.ritualId).set(data).await()
            }
        } catch (e: Exception) {
            Log.e("RitualsRepo", "saveRitual error", e)
        }
    }

    override suspend fun deleteRitual(ritualId: String) {
        try {
            firestore.collection(collection).document(ritualId)
                .update("isActive", false).await()
        } catch (e: Exception) {
            Log.e("RitualsRepo", "deleteRitual error", e)
        }
    }

    override suspend fun completeRitual(ritualId: String) {
        try {
            firestore.collection(collection).document(ritualId)
                .update("lastCompletedAt", System.currentTimeMillis()).await()
        } catch (e: Exception) {
            Log.e("RitualsRepo", "completeRitual error", e)
        }
    }

    override suspend fun submitGratitude(ritualId: String, userId: String, text: String) {
        try {
            firestore.collection(collection).document(ritualId)
                .update("gratitudeEntries.$userId", text).await()
        } catch (e: Exception) {
            Log.e("RitualsRepo", "submitGratitude error", e)
        }
    }

    override suspend fun updateGoalProgress(ritualId: String, increment: Int) {
        try {
            val doc = firestore.collection(collection).document(ritualId).get().await()
            val current = (doc.getLong("goalProgress") ?: 0).toInt()
            firestore.collection(collection).document(ritualId)
                .update("goalProgress", current + increment).await()
        } catch (e: Exception) {
            Log.e("RitualsRepo", "updateGoalProgress error", e)
        }
    }

    private fun ritualToMap(ritual: FamilyRitual): Map<String, Any?> = mapOf(
        "familyId" to ritual.familyId,
        "type" to ritual.type.name,
        "title" to ritual.title,
        "description" to ritual.description,
        "frequency" to ritual.frequency.name,
        "gratitudeEntries" to ritual.gratitudeEntries,
        "meetingDurationMin" to ritual.meetingDurationMin,
        "agendaItems" to ritual.agendaItems,
        "goalTitle" to ritual.goalTitle,
        "goalTarget" to ritual.goalTarget,
        "goalProgress" to ritual.goalProgress,
        "goalUnit" to ritual.goalUnit,
        "completionXp" to ritual.completionXp,
        "isActive" to ritual.isActive,
        "createdAt" to ritual.createdAt,
        "lastCompletedAt" to ritual.lastCompletedAt,
        "scheduledDay" to ritual.scheduledDay,
        "scheduledTime" to ritual.scheduledTime
    )

    @Suppress("UNCHECKED_CAST")
    private fun docToRitual(doc: com.google.firebase.firestore.DocumentSnapshot): FamilyRitual? {
        return try {
            FamilyRitual(
                ritualId = doc.id,
                familyId = doc.getString("familyId") ?: "",
                type = try { RitualType.valueOf(doc.getString("type") ?: "GRATITUDE_CIRCLE") } catch (_: Exception) { RitualType.GRATITUDE_CIRCLE },
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                frequency = try { RitualFrequency.valueOf(doc.getString("frequency") ?: "DAILY") } catch (_: Exception) { RitualFrequency.DAILY },
                gratitudeEntries = (doc.get("gratitudeEntries") as? Map<String, String>) ?: emptyMap(),
                meetingDurationMin = (doc.getLong("meetingDurationMin") ?: 15).toInt(),
                agendaItems = (doc.get("agendaItems") as? List<String>) ?: emptyList(),
                goalTitle = doc.getString("goalTitle") ?: "",
                goalTarget = (doc.getLong("goalTarget") ?: 0).toInt(),
                goalProgress = (doc.getLong("goalProgress") ?: 0).toInt(),
                goalUnit = doc.getString("goalUnit") ?: "times",
                completionXp = (doc.getLong("completionXp") ?: 25).toInt(),
                isActive = doc.getBoolean("isActive") ?: true,
                createdAt = doc.getLong("createdAt") ?: 0L,
                lastCompletedAt = doc.getLong("lastCompletedAt") ?: 0L,
                scheduledDay = doc.getString("scheduledDay") ?: "",
                scheduledTime = doc.getString("scheduledTime") ?: ""
            )
        } catch (e: Exception) {
            Log.e("RitualsRepo", "docToRitual error", e)
            null
        }
    }
}
