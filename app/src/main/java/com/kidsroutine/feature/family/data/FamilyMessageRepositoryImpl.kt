package com.kidsroutine.feature.family.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.FamilyMessage
import com.kidsroutine.core.model.MessageType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyMessageRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FamilyMessageRepository {

    override suspend fun sendMessage(message: FamilyMessage) {
        try {
            Log.d("FamilyMessageRepository", "Sending message to family: ${message.familyId}")

            val messageData = mapOf(
                "id" to message.id,
                "familyId" to message.familyId,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "senderAvatar" to message.senderAvatar,
                "content" to message.content,
                "type" to message.type.name,
                "relatedTaskId" to message.relatedTaskId,
                "relatedTaskTitle" to message.relatedTaskTitle,
                "createdAt" to message.createdAt,
                "isRead" to message.isRead
            )

            firestore.collection("familyMessages")
                .document(message.id)
                .set(messageData)
                .await()

            // Send notification to other family members
            sendNotificationsToFamily(message)

            Log.d("FamilyMessageRepository", "Message sent successfully")
        } catch (e: Exception) {
            Log.e("FamilyMessageRepository", "Error sending message", e)
        }
    }

    override fun observeFamilyMessages(familyId: String): Flow<List<FamilyMessage>> = callbackFlow {
        try {
            Log.d("FamilyMessageRepository", "Observing messages for family: $familyId")

            val listener = firestore.collection("familyMessages")
                .whereEqualTo("familyId", familyId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FamilyMessageRepository", "Error observing messages", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    val messages = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            FamilyMessage(
                                id = data["id"] as? String ?: "",
                                familyId = data["familyId"] as? String ?: "",
                                senderId = data["senderId"] as? String ?: "",
                                senderName = data["senderName"] as? String ?: "",
                                senderAvatar = data["senderAvatar"] as? String ?: "",
                                content = data["content"] as? String ?: "",
                                type = try {
                                    MessageType.valueOf(data["type"] as? String ?: "TEXT")
                                } catch (e: Exception) {
                                    MessageType.TEXT
                                },
                                relatedTaskId = data["relatedTaskId"] as? String,
                                relatedTaskTitle = data["relatedTaskTitle"] as? String,
                                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                                isRead = data["isRead"] as? Boolean ?: false
                            )
                        } catch (e: Exception) {
                            Log.e("FamilyMessageRepository", "Error parsing message", e)
                            null
                        }
                    }?.reversed() ?: emptyList()

                    Log.d("FamilyMessageRepository", "Emitted ${messages.size} messages")
                    trySend(messages).isSuccess
                }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("FamilyMessageRepository", "Error observing messages", e)
            close(e)
        }
    }

    override suspend fun markAsRead(messageId: String) {
        try {
            Log.d("FamilyMessageRepository", "Marking message as read: $messageId")
            firestore.collection("familyMessages").document(messageId)
                .update("isRead", true)
                .await()
            Log.d("FamilyMessageRepository", "Message marked as read")
        } catch (e: Exception) {
            Log.e("FamilyMessageRepository", "Error marking as read", e)
        }
    }

    override suspend fun deleteMessage(messageId: String) {
        try {
            Log.d("FamilyMessageRepository", "Deleting message: $messageId")
            firestore.collection("familyMessages").document(messageId)
                .delete()
                .await()
            Log.d("FamilyMessageRepository", "Message deleted")
        } catch (e: Exception) {
            Log.e("FamilyMessageRepository", "Error deleting message", e)
        }
    }

    private suspend fun sendNotificationsToFamily(message: FamilyMessage) {
        try {
            // Get family members
            val familyDoc = firestore.collection("families").document(message.familyId).get().await()
            val memberIds = (familyDoc.data?.get("memberIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

            // Send notification to all members except sender
            memberIds.forEach { memberId ->
                if (memberId != message.senderId) {
                    val notificationData = mapOf(
                        "id" to "notif_${System.currentTimeMillis()}_${kotlin.random.Random.nextInt(10000)}",
                        "userId" to memberId,
                        "type" to "FAMILY_MESSAGE",
                        "title" to "${message.senderName} sent a message",
                        "body" to message.content,
                        "icon" to "💬",
                        "actionUrl" to "",
                        "isRead" to false,
                        "createdAt" to System.currentTimeMillis()
                    )

                    firestore.collection("notifications")
                        .add(notificationData)
                        .await()
                }
            }
        } catch (e: Exception) {
            Log.e("FamilyMessageRepository", "Error sending notifications", e)
        }
    }
}