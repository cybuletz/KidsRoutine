package com.kidsroutine.feature.challenges.data

import com.kidsroutine.core.model.ChallengeModel
import com.kidsroutine.core.model.ChallengeProgress
import kotlinx.coroutines.flow.Flow

interface ChallengeRepository {
    suspend fun createChallenge(challenge: ChallengeModel): ChallengeModel
    suspend fun getChallenge(challengeId: String): ChallengeModel?
    suspend fun getFamilyChallenges(familyId: String): List<ChallengeModel>
    suspend fun getSystemChallenges(): List<ChallengeModel>
    suspend fun startChallenge(userId: String, familyId: String, challengeId: String): ChallengeProgress
    suspend fun getActiveChallenges(userId: String, familyId: String): List<ChallengeProgress>
    suspend fun getChallengeProgress(userId: String, familyId: String, challengeId: String): ChallengeProgress?
    suspend fun updateChallengeProgress(progress: ChallengeProgress, familyId: String)
    fun observeActiveChallenges(userId: String, familyId: String): Flow<List<ChallengeProgress>>
    suspend fun seedDefaultChallengesIfEmpty()

    suspend fun getAllChallengeProgress(userId: String, familyId: String): List<ChallengeProgress>


}