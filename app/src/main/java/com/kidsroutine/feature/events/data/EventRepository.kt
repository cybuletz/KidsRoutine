package com.kidsroutine.feature.events.data

import com.kidsroutine.core.model.EventProgress
import com.kidsroutine.core.model.EventShopItem
import com.kidsroutine.core.model.TimedEvent

interface EventRepository {
    suspend fun getActiveEvents(): List<TimedEvent>
    suspend fun getEventProgress(eventId: String, userId: String): EventProgress?
    suspend fun saveEventProgress(progress: EventProgress)
    suspend fun getShopItems(eventId: String): List<EventShopItem>
}
