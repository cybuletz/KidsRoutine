package com.kidsroutine.feature.generation.data

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import com.kidsroutine.core.ai.AIGenerationService
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for AI generation operations
 * Calls Cloud Functions (Gemini-only) for task and challenge generation
 */
@Singleton
class GenerationRepository @Inject constructor(
    private val functions: FirebaseFunctions,
    private val aiService: AIGenerationService
) {

    /**
     * Generate tasks via Cloud Function (Gemini API)
     */
    suspend fun generateTasks(
        familyId: String,
        childAge: Int,
        preferences: List<String> = emptyList(),
        recentCompletions: List<String> = emptyList(),
        tier: String = "FREE",
        count: Int = 1
    ): Result<GeneratedTasksResponse> {
        return try {
            Log.d("GenerationRepo", "⏳ Calling generateTasksAI Cloud Function...")

            val result = functions.getHttpsCallable("generateTasksAI")
                .call(mapOf(
                    "familyId" to familyId,
                    "childAge" to childAge,
                    "preferences" to preferences,
                    "recentCompletions" to recentCompletions,
                    "tier" to tier,
                    "count" to count
                ))
                .await()

            val data = result.data as? Map<*, *>
                ?: throw Exception("Invalid response from Cloud Function")

            val response = GeneratedTasksResponse(
                success = data["success"] as? Boolean ?: false,
                tasks = (data["tasks"] as? List<*>)?.mapNotNull { task ->
                    (task as? Map<*, *>)?.let { parseTaskMap(it) }
                } ?: emptyList(),
                cached = data["cached"] as? Boolean ?: false,
                quotaRemaining = (data["quotaRemaining"] as? Number)?.toInt() ?: 0
            )

            if (response.success) {
                Log.d("GenerationRepo", "✅ Generated ${response.tasks.size} tasks (Cached: ${response.cached})")
                Result.success(response)
            } else {
                Log.e("GenerationRepo", "❌ Cloud Function returned success=false")
                Result.failure(Exception("Failed to generate tasks"))
            }
        } catch (e: Exception) {
            Log.e("GenerationRepo", "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Generate challenges via Cloud Function (Gemini API)
     * PRO+ tier only
     */
    suspend fun generateChallenges(
        familyId: String,
        childAge: Int,
        goals: List<String> = emptyList(),
        tier: String = "PRO",
        count: Int = 1
    ): Result<GeneratedChallengesResponse> {
        return try {
            Log.d("GenerationRepo", "⏳ Calling generateChallengesAI Cloud Function...")

            val result = functions.getHttpsCallable("generateChallengesAI")
                .call(mapOf(
                    "familyId" to familyId,
                    "childAge" to childAge,
                    "goals" to goals,
                    "tier" to tier,
                    "count" to count
                ))
                .await()

            val data = result.data as? Map<*, *>
                ?: throw Exception("Invalid response from Cloud Function")

            val response = GeneratedChallengesResponse(
                success = data["success"] as? Boolean ?: false,
                challenges = (data["challenges"] as? List<*>)?.mapNotNull { challenge ->
                    (challenge as? Map<*, *>)?.let { parseChallengeMap(it) }
                } ?: emptyList(),
                cached = data["cached"] as? Boolean ?: false,
                quotaRemaining = (data["quotaRemaining"] as? Number)?.toInt() ?: 0
            )

            if (response.success) {
                Log.d("GenerationRepo", "✅ Generated ${response.challenges.size} challenges (Cached: ${response.cached})")
                Result.success(response)
            } else {
                Log.e("GenerationRepo", "❌ Cloud Function returned success=false")
                Result.failure(Exception("Failed to generate challenges"))
            }
        } catch (e: Exception) {
            Log.e("GenerationRepo", "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ── PRIVATE PARSING HELPERS ────────────────────────────────────────────────

    private fun parseTaskMap(map: Map<*, *>): GeneratedTask? {
        return try {
            GeneratedTask(
                title = map["title"] as? String ?: return null,
                description = map["description"] as? String ?: "",
                estimatedDurationSec = (map["estimatedDurationSec"] as? Number)?.toInt() ?: 30,
                category = map["category"] as? String ?: "MORNING_ROUTINE",
                difficulty = map["difficulty"] as? String ?: "EASY",
                xpReward = (map["xpReward"] as? Number)?.toInt() ?: 10,
                type = map["type"] as? String ?: "LOGIC"
            )
        } catch (e: Exception) {
            Log.e("GenerationRepo", "❌ Failed to parse task: ${e.message}", e)
            null
        }
    }

    private fun parseChallengeMap(map: Map<*, *>): GeneratedChallenge? {
        return try {
            GeneratedChallenge(
                title = map["title"] as? String ?: return null,
                description = map["description"] as? String ?: "",
                durationDays = (map["durationDays"] as? Number)?.toInt() ?: 7,
                category = map["category"] as? String ?: "SLEEP",
                successCondition = map["successCondition"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e("GenerationRepo", "❌ Failed to parse challenge: ${e.message}", e)
            null
        }
    }
}

// ── DATA CLASSES ────────────────────────────────────────────────

/**
 * Generated task from Gemini API via Cloud Function
 */
data class GeneratedTask(
    val title: String,
    val description: String,
    val estimatedDurationSec: Int,
    val category: String,
    val difficulty: String,
    val xpReward: Int,
    val type: String
)

/**
 * Generated challenge from Gemini API via Cloud Function
 */
data class GeneratedChallenge(
    val title: String,
    val description: String,
    val durationDays: Int,
    val category: String,
    val successCondition: String
)

/**
 * Response from generateTasksAI Cloud Function
 */
data class GeneratedTasksResponse(
    val success: Boolean,
    val tasks: List<GeneratedTask>,
    val cached: Boolean,
    val quotaRemaining: Int
)

/**
 * Response from generateChallengesAI Cloud Function
 */
data class GeneratedChallengesResponse(
    val success: Boolean,
    val challenges: List<GeneratedChallenge>,
    val cached: Boolean,
    val quotaRemaining: Int
)