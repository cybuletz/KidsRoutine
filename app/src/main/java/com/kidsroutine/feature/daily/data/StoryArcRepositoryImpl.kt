package com.kidsroutine.feature.daily.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.StoryArc
import com.kidsroutine.core.model.StoryChapter
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryArcRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : StoryArcRepository {

    companion object {
        private const val TAG = "StoryArcRepo"
        private const val COLLECTION = "story_arcs"
    }

    override suspend fun getActiveArc(familyId: String): StoryArc? {
        return try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("isComplete", false)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull() ?: return null
            doc.toStoryArc()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching active arc for family $familyId", e)
            null
        }
    }

    override suspend fun saveArc(arc: StoryArc) {
        try {
            firestore.collection(COLLECTION)
                .document(arc.arcId)
                .set(arcToMap(arc))
                .await()
            Log.d(TAG, "Saved arc: ${arc.arcId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving arc ${arc.arcId}", e)
            throw e
        }
    }

    override suspend fun advanceDay(arcId: String) {
        try {
            val ref = firestore.collection(COLLECTION).document(arcId)
            firestore.runTransaction { tx ->
                val snap  = tx.get(ref)
                val day   = (snap.getLong("currentDay") ?: 1L).toInt()
                val total = (snap.getLong("totalDays")  ?: 3L).toInt()
                val next  = (day + 1).coerceAtMost(total)
                tx.update(ref, mapOf(
                    "currentDay" to next,
                    "isComplete" to (next >= total)
                ))
            }.await()
            Log.d(TAG, "Advanced arc $arcId to next day")
        } catch (e: Exception) {
            Log.e(TAG, "Error advancing arc $arcId", e)
        }
    }

    override suspend fun completeArc(arcId: String) {
        try {
            firestore.collection(COLLECTION)
                .document(arcId)
                .update("isComplete", true)
                .await()
            Log.d(TAG, "Completed arc $arcId")
        } catch (e: Exception) {
            Log.e(TAG, "Error completing arc $arcId", e)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun arcToMap(arc: StoryArc): Map<String, Any> = mapOf(
        "arcId"      to arc.arcId,
        "arcTitle"   to arc.arcTitle,
        "arcEmoji"   to arc.arcEmoji,
        "theme"      to arc.theme,
        "childAge"   to arc.childAge,
        "familyId"   to arc.familyId,
        "chapters"   to arc.chapters.map { chapterToMap(it) },
        "startDate"  to arc.startDate,
        "currentDay" to arc.currentDay,
        "totalDays"  to arc.chapters.size,
        "isComplete" to arc.isComplete,
        "createdAt"  to arc.createdAt
    )

    private fun chapterToMap(c: StoryChapter): Map<String, Any> = mapOf(
        "day"                  to c.day,
        "chapterTitle"         to c.chapterTitle,
        "narrative"            to c.narrative,
        "taskTitle"            to c.taskTitle,
        "taskDescription"      to c.taskDescription,
        "estimatedDurationSec" to c.estimatedDurationSec,
        "category"             to c.category,
        "difficulty"           to c.difficulty,
        "xpReward"             to c.xpReward,
        "type"                 to c.type
    )

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toStoryArc(): StoryArc? {
        return try {
            val d = data ?: return null
            val chaptersRaw = d["chapters"] as? List<Map<String, Any>> ?: emptyList()
            val chapters = chaptersRaw.map { m ->
                StoryChapter(
                    day                  = (m["day"] as? Number)?.toInt() ?: 1,
                    chapterTitle         = m["chapterTitle"]    as? String ?: "",
                    narrative            = m["narrative"]       as? String ?: "",
                    taskTitle            = m["taskTitle"]       as? String ?: "",
                    taskDescription      = m["taskDescription"] as? String ?: "",
                    estimatedDurationSec = (m["estimatedDurationSec"] as? Number)?.toInt() ?: 60,
                    category             = m["category"]  as? String ?: "CREATIVITY",
                    difficulty           = m["difficulty"] as? String ?: "MEDIUM",
                    xpReward             = (m["xpReward"] as? Number)?.toInt() ?: 50,
                    type                 = m["type"] as? String ?: "STORY"
                )
            }
            StoryArc(
                arcId      = d["arcId"]      as? String ?: id,
                arcTitle   = d["arcTitle"]   as? String ?: "",
                arcEmoji   = d["arcEmoji"]   as? String ?: "📖",
                theme      = d["theme"]      as? String ?: "",
                childAge   = (d["childAge"]  as? Number)?.toInt() ?: 0,
                familyId   = d["familyId"]   as? String ?: "",
                chapters   = chapters,
                startDate  = d["startDate"]  as? String ?: "",
                currentDay = (d["currentDay"] as? Number)?.toInt() ?: 1,
                isComplete = d["isComplete"] as? Boolean ?: false,
                createdAt  = (d["createdAt"] as? Number)?.toLong() ?: 0L
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing StoryArc document $id", e)
            null
        }
    }
}