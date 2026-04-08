package com.kidsroutine.feature.boss.data

import com.kidsroutine.core.model.BossModel

interface BossRepository {
    suspend fun getActiveBoss(familyId: String): BossModel?
    suspend fun saveBoss(boss: BossModel)
    suspend fun getRecentBosses(familyId: String, limit: Int = 5): List<BossModel>
}
