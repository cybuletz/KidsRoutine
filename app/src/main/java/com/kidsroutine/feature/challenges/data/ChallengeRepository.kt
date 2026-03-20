package com.kidsroutine.feature.challenges.data

import com.kidsroutine.core.model.ChallengeModel
import com.kidsroutine.core.model.ChallengeProgress
import kotlinx.coroutines.flow.Flow

interface ChallengeRepository {
    suspend fun createChallenge(challenge: ChallengeModel): ChallengeModel
    suspend fun getChallenge(challengeId: String): ChallengeModel?
    suspend fun getFamilyChallenges(familyId: String): List<ChallengeModel>
    suspend fun getSystemChallenges(): List<ChallengeModel>

    suspend fun startChallenge(userId: String, challengeId: String): ChallengeProgress
    suspend fun getActiveChallenges(userId: String): List<ChallengeProgress>
    suspend fun getChallengeProgress(userId: String, challengeId: String): ChallengeProgress?
    suspend fun updateChallengeProgress(progress: ChallengeProgress)

    fun observeActiveChallenges(userId: String): Flow<List<ChallengeProgress>>
}