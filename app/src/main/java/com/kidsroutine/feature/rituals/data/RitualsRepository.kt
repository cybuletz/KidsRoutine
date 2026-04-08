package com.kidsroutine.feature.rituals.data

import com.kidsroutine.core.model.FamilyRitual

interface RitualsRepository {
    suspend fun getRituals(familyId: String): List<FamilyRitual>
    suspend fun getRitual(ritualId: String): FamilyRitual?
    suspend fun saveRitual(ritual: FamilyRitual)
    suspend fun deleteRitual(ritualId: String)
    suspend fun completeRitual(ritualId: String)
    suspend fun submitGratitude(ritualId: String, userId: String, text: String)
    suspend fun updateGoalProgress(ritualId: String, increment: Int)
}
