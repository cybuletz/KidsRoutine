package com.kidsroutine.feature.moments.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.MomentModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MomentsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MomentsRepository {

    override suspend fun getMoments(familyId: String): List<MomentModel> {
        return try {
            val snapshot = firestore.collection("moments")
                .whereEqualTo("familyId", familyId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(MomentModel::class.java) }
        } catch (e: Exception) {
            Log.e("MomentsRepo", "Failed to load moments", e)
            emptyList()
        }
    }

    override suspend fun addMoment(moment: MomentModel) {
        try {
            firestore.collection("moments")
                .document(moment.momentId)
                .set(moment)
                .await()
        } catch (e: Exception) {
            Log.e("MomentsRepo", "Failed to add moment", e)
        }
    }

    override suspend fun addReaction(momentId: String, userId: String, emoji: String) {
        try {
            firestore.collection("moments")
                .document(momentId)
                .update("reactions.$userId", emoji)
                .await()
        } catch (e: Exception) {
            Log.e("MomentsRepo", "Failed to add reaction", e)
        }
    }
}