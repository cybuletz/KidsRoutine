package com.kidsroutine.feature.family.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.FamilyMessage
import com.kidsroutine.core.model.MessageType
import com.kidsroutine.feature.family.data.FamilyMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilyMessagingUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val messages: List<FamilyMessage> = emptyList(),
    val messageInput: String = "",
    val error: String? = null
)

@HiltViewModel
class FamilyMessagingViewModel @Inject constructor(
    private val messageRepository: FamilyMessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyMessagingUiState())
    val uiState: StateFlow<FamilyMessagingUiState> = _uiState.asStateFlow()

    fun loadMessages(familyId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                Log.d("FamilyMessagingVM", "Loading messages for family: $familyId")
                messageRepository.observeFamilyMessages(familyId)
                    .collect { messages ->
                        Log.d("FamilyMessagingVM", "Loaded ${messages.size} messages")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            messages = messages
                        )
                    }
            } catch (e: Exception) {
                Log.e("FamilyMessagingVM", "Error loading messages", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load messages"
                )
            }
        }
    }

    fun sendMessage(
        familyId: String,
        senderId: String,
        senderName: String,
        senderAvatar: String,
        content: String,
        messageType: MessageType = MessageType.TEXT,
        relatedTaskId: String? = null,
        relatedTaskTitle: String? = null
    ) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }  // ← START LOADING
            try {
                Log.d("FamilyMessagingVM", "Sending message from $senderName")
                val message = FamilyMessage(
                    familyId = familyId,
                    senderId = senderId,
                    senderName = senderName,
                    senderAvatar = senderAvatar,
                    content = content,
                    type = messageType,
                    relatedTaskId = relatedTaskId,
                    relatedTaskTitle = relatedTaskTitle,
                    createdAt = System.currentTimeMillis()
                )
                messageRepository.sendMessage(message)
                _uiState.value = _uiState.value.copy(messageInput = "")
                Log.d("FamilyMessagingVM", "Message sent successfully")
            } catch (e: Exception) {
                Log.e("FamilyMessagingVM", "Error sending message", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to send message"
                )
            } finally {
                _uiState.update { it.copy(isSending = false) }  // ← STOP LOADING
            }
        }
    }

    fun updateMessageInput(input: String) {
        _uiState.value = _uiState.value.copy(messageInput = input)
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                messageRepository.deleteMessage(messageId)
            } catch (e: Exception) {
                Log.e("FamilyMessagingVM", "Error deleting message", e)
            }
        }
    }
}