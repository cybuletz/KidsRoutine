package com.kidsroutine.feature.events.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.EventProgress
import com.kidsroutine.core.model.EventShopItem
import com.kidsroutine.core.model.EventType
import com.kidsroutine.core.model.Season
import com.kidsroutine.core.model.TimedEvent
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EventRepository {

    companion object {
        private const val TAG = "EventRepository"
        private const val EVENTS_COLLECTION = "timed_events"
        private const val PROGRESS_COLLECTION = "event_progress"
    }

    override suspend fun getActiveEvents(): List<TimedEvent> {
        return try {
            val now = System.currentTimeMillis()
            Log.d(TAG, "Fetching active events at timestamp: $now")

            val snapshot = firestore.collection(EVENTS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereLessThanOrEqualTo("startTimestamp", now)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val event = doc.toTimedEvent()
                    // Filter: endTimestamp must be in the future
                    if (event.endTimestamp >= now) event else null
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing event ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching active events", e)
            emptyList()
        }
    }

    override suspend fun getEventProgress(eventId: String, userId: String): EventProgress? {
        return try {
            val docId = "${eventId}_${userId}"
            Log.d(TAG, "Fetching progress: $docId")

            val doc = firestore.collection(PROGRESS_COLLECTION)
                .document(docId)
                .get()
                .await()

            if (doc.exists()) doc.toEventProgress() else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching event progress", e)
            null
        }
    }

    override suspend fun saveEventProgress(progress: EventProgress) {
        try {
            val docId = "${progress.eventId}_${progress.userId}"
            Log.d(TAG, "Saving progress: $docId")

            val data = mapOf(
                "eventId" to progress.eventId,
                "userId" to progress.userId,
                "tasksCompleted" to progress.tasksCompleted,
                "xpEarned" to progress.xpEarned,
                "tokensEarned" to progress.tokensEarned,
                "tokensSpent" to progress.tokensSpent,
                "rewardsClaimed" to progress.rewardsClaimed,
                "isComplete" to progress.isComplete,
                "lastActivityAt" to progress.lastActivityAt
            )

            firestore.collection(PROGRESS_COLLECTION)
                .document(docId)
                .set(data)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving event progress", e)
        }
    }

    override suspend fun getShopItems(eventId: String): List<EventShopItem> {
        return try {
            Log.d(TAG, "Fetching shop items for event: $eventId")

            val doc = firestore.collection(EVENTS_COLLECTION)
                .document(eventId)
                .get()
                .await()

            if (!doc.exists()) return emptyList()

            @Suppress("UNCHECKED_CAST")
            val itemsList = doc.get("tokenShopItems") as? List<Map<String, Any>> ?: emptyList()

            itemsList.map { map ->
                EventShopItem(
                    itemId = map["itemId"] as? String ?: "",
                    name = map["name"] as? String ?: "",
                    emoji = map["emoji"] as? String ?: "",
                    tokenCost = (map["tokenCost"] as? Number)?.toInt() ?: 0,
                    type = map["type"] as? String ?: "avatar_item"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching shop items", e)
            emptyList()
        }
    }

    // ── Firestore document mappers ──────────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toTimedEvent(): TimedEvent {
        @Suppress("UNCHECKED_CAST")
        val shopItemsList = get("tokenShopItems") as? List<Map<String, Any>> ?: emptyList()

        return TimedEvent(
            eventId = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            type = getString("type")?.let { typeName ->
                try { EventType.valueOf(typeName) } catch (_: Exception) { EventType.SEASONAL }
            } ?: EventType.SEASONAL,
            season = getString("season")?.let { seasonName ->
                try { Season.valueOf(seasonName) } catch (_: Exception) { Season.NONE }
            } ?: Season.NONE,
            emoji = getString("emoji") ?: "🎯",
            startDate = getString("startDate") ?: "",
            endDate = getString("endDate") ?: "",
            startTimestamp = getLong("startTimestamp") ?: 0L,
            endTimestamp = getLong("endTimestamp") ?: 0L,
            targetTaskCount = getLong("targetTaskCount")?.toInt() ?: 20,
            targetXp = getLong("targetXp")?.toInt() ?: 0,
            rewardAvatarItemIds = get("rewardAvatarItemIds") as? List<String> ?: emptyList(),
            rewardXp = getLong("rewardXp")?.toInt() ?: 200,
            rewardBadgeId = getString("rewardBadgeId") ?: "",
            eventTokenName = getString("eventTokenName") ?: "🎃 Tokens",
            tokenShopItems = shopItemsList.map { map ->
                EventShopItem(
                    itemId = map["itemId"] as? String ?: "",
                    name = map["name"] as? String ?: "",
                    emoji = map["emoji"] as? String ?: "",
                    tokenCost = (map["tokenCost"] as? Number)?.toInt() ?: 0,
                    type = map["type"] as? String ?: "avatar_item"
                )
            },
            isActive = getBoolean("isActive") ?: false,
            isGlobal = getBoolean("isGlobal") ?: true,
            hasLeaderboard = getBoolean("hasLeaderboard") ?: true
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toEventProgress(): EventProgress {
        return EventProgress(
            eventId = getString("eventId") ?: "",
            userId = getString("userId") ?: "",
            tasksCompleted = getLong("tasksCompleted")?.toInt() ?: 0,
            xpEarned = getLong("xpEarned")?.toInt() ?: 0,
            tokensEarned = getLong("tokensEarned")?.toInt() ?: 0,
            tokensSpent = getLong("tokensSpent")?.toInt() ?: 0,
            rewardsClaimed = get("rewardsClaimed") as? List<String> ?: emptyList(),
            isComplete = getBoolean("isComplete") ?: false,
            lastActivityAt = getLong("lastActivityAt") ?: 0L
        )
    }
}
