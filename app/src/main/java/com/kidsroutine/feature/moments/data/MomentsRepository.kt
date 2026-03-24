package com.kidsroutine.feature.moments.data

import com.kidsroutine.core.model.MomentModel

interface MomentsRepository {
    suspend fun getMoments(familyId: String): List<MomentModel>
    suspend fun addMoment(moment: MomentModel)
    suspend fun addReaction(momentId: String, userId: String, emoji: String)
}