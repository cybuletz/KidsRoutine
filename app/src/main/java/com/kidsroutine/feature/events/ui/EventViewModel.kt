package com.kidsroutine.feature.events.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.engine.event_engine.EventEngine
import com.kidsroutine.core.model.EventProgress
import com.kidsroutine.core.model.EventShopItem
import com.kidsroutine.core.model.TimedEvent
import com.kidsroutine.feature.events.data.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventUiState(
    val events: List<TimedEvent> = emptyList(),
    val selectedEvent: TimedEvent? = null,
    val progress: EventProgress? = null,
    val shopItems: List<EventShopItem> = emptyList(),
    val isLoading: Boolean = false,
    val timeRemaining: Long = 0L,
    val showShop: Boolean = false,
    val error: String? = null,
    val purchaseMessage: String? = null
)

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val eventEngine: EventEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var currentUserId: String = ""

    fun loadEvents(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val events = eventRepository.getActiveEvents()
                val selected = events.firstOrNull()
                val progress = selected?.let {
                    eventRepository.getEventProgress(it.eventId, userId)
                }
                val shopItems = selected?.let {
                    eventRepository.getShopItems(it.eventId)
                } ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    events = events,
                    selectedEvent = selected,
                    progress = progress ?: selected?.let {
                        EventProgress(eventId = it.eventId, userId = userId)
                    },
                    shopItems = shopItems.ifEmpty { selected?.tokenShopItems ?: emptyList() },
                    isLoading = false
                )

                selected?.let { startCountdownTimer(it) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load events"
                )
            }
        }
    }

    fun selectEvent(event: TimedEvent) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedEvent = event,
                showShop = false,
                purchaseMessage = null
            )
            try {
                val progress = eventRepository.getEventProgress(event.eventId, currentUserId)
                val shopItems = eventRepository.getShopItems(event.eventId)

                _uiState.value = _uiState.value.copy(
                    progress = progress ?: EventProgress(
                        eventId = event.eventId,
                        userId = currentUserId
                    ),
                    shopItems = shopItems.ifEmpty { event.tokenShopItems }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    progress = EventProgress(
                        eventId = event.eventId,
                        userId = currentUserId
                    ),
                    shopItems = event.tokenShopItems
                )
            }
            startCountdownTimer(event)
        }
    }

    fun purchaseShopItem(item: EventShopItem) {
        val state = _uiState.value
        val progress = state.progress ?: return
        val event = state.selectedEvent ?: return

        val updatedProgress = eventEngine.spendTokens(progress, item.tokenCost)
        if (updatedProgress == null) {
            _uiState.value = state.copy(purchaseMessage = "Not enough tokens!")
            return
        }

        val claimed = updatedProgress.copy(
            rewardsClaimed = updatedProgress.rewardsClaimed + item.itemId
        )

        _uiState.value = state.copy(
            progress = claimed,
            purchaseMessage = "Purchased ${item.emoji} ${item.name}!"
        )

        viewModelScope.launch {
            eventRepository.saveEventProgress(claimed)
        }
    }

    fun toggleShop() {
        _uiState.value = _uiState.value.copy(
            showShop = !_uiState.value.showShop,
            purchaseMessage = null
        )
    }

    fun refreshProgress() {
        val event = _uiState.value.selectedEvent ?: return
        viewModelScope.launch {
            try {
                val progress = eventRepository.getEventProgress(event.eventId, currentUserId)
                _uiState.value = _uiState.value.copy(progress = progress)
            } catch (_: Exception) { }
        }
    }

    fun dismissPurchaseMessage() {
        _uiState.value = _uiState.value.copy(purchaseMessage = null)
    }

    fun getCompletionPercent(): Float {
        val state = _uiState.value
        val event = state.selectedEvent ?: return 0f
        val progress = state.progress ?: return 0f
        return eventEngine.completionPercent(progress, event)
    }

    private fun startCountdownTimer(event: TimedEvent) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val remaining = eventEngine.timeRemainingSeconds(
                    event, System.currentTimeMillis()
                )
                _uiState.value = _uiState.value.copy(timeRemaining = remaining)
                if (remaining <= 0) break
                delay(1000L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
