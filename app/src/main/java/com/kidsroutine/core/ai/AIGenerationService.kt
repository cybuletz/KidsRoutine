package com.kidsroutine.core.ai

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIGenerationService @Inject constructor(
    private val registry: AIProviderRegistry,
    private val firestore: FirebaseFirestore
) {

    suspend fun generate(
        prompt: String,
        contentType: GenerationType,
        context: GenerationContext,
        maxTokens: Int = 1000,
        temperature: Float = 0.7f
    ): Result<String> {
        return try {
            val provider = registry.getActive()
                ?: return Result.failure(Exception("Gemini provider not configured"))

            Log.d("AIGeneration", "Using provider: ${provider.name}")

            // Check quota
            val quota = getQuota(context.userId, contentType)
            if (quota.isExceeded) {
                return Result.failure(
                    Exception("Quota exceeded. Remaining: ${quota.remaining}/${quota.limit}")
                )
            }

            // Check cache first
            val cached = getCached(prompt, contentType)
            if (cached != null) {
                Log.d("AIGeneration", "✅ Using cached result")
                return Result.success(cached)
            }

            // Generate with Gemini
            Log.d("AIGeneration", "⏳ Generating with ${provider.name}...")
            val systemPrompt = getSystemPrompt(contentType, context)
            val result = provider.generateText(prompt, maxTokens, temperature, systemPrompt)

            // Cache on success
            if (result.isSuccess) {
                cacheResult(prompt, contentType, result.getOrNull() ?: "")
                updateQuota(context.userId, contentType)
                Log.d("AIGeneration", "✅ Generation complete and cached")
            }

            result
        } catch (e: Exception) {
            Log.e("AIGeneration", "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun streamGenerate(
        prompt: String,
        contentType: GenerationType,
        context: GenerationContext
    ): Flow<Result<String>> {
        val provider = registry.getActive()
            ?: return kotlinx.coroutines.flow.flow {
                emit(Result.failure(Exception("Gemini provider not configured")))
            }

        val systemPrompt = getSystemPrompt(contentType, context)
        return provider.streamText(prompt, 1000, 0.7f, systemPrompt)
    }

    // ── PRIVATE HELPERS ────────────────────────────────────────────────

    private fun getSystemPrompt(contentType: GenerationType, context: GenerationContext): String {
        return when (contentType) {
            GenerationType.TASK -> getTaskSystemPrompt(context)
            GenerationType.CHALLENGE -> getChallengeSystemPrompt(context)
            GenerationType.CUSTOM -> "You are a helpful assistant for children. Generate age-appropriate content."
        }
    }

    private fun getTaskSystemPrompt(context: GenerationContext): String {
        return """
            You are a children's task generator for ages ${context.childAge}+.
            Generate a fun, engaging task that is age-appropriate and safe.
            
            TASK REQUIREMENTS:
            - Title: Short, fun, emoji-enabled (max 50 chars)
            - Description: Clear, child-friendly (max 100 chars)
            - Duration: 5-60 seconds
            - Category: MORNING_ROUTINE, HEALTH, LEARNING, CREATIVE, SOCIAL, EMOTIONAL, REAL_LIFE
            - Difficulty: EASY, MEDIUM, HARD
            - XP Reward: 10-50 points based on difficulty
            - Type: LOGIC, REAL_LIFE, CREATIVE, LEARNING, EMOTIONAL, CO_OP
            
            ${if (context.preferences.isNotEmpty()) "Child prefers: ${context.preferences.joinToString(", ")}" else ""}
            ${if (context.recentCompletions.isNotEmpty()) "Recently completed: ${context.recentCompletions.joinToString(", ")}. Don't repeat similar tasks." else ""}
            
            Return ONLY valid JSON (no markdown, no extra text):
            {
              "title": "string",
              "description": "string",
              "estimatedDurationSec": number,
              "category": "string",
              "difficulty": "string",
              "xpReward": number,
              "type": "string"
            }
        """.trimIndent()
    }

    private fun getChallengeSystemPrompt(context: GenerationContext): String {
        return """
            You are a children's challenge (habit) generator for ages ${context.childAge}+.
            Generate a multi-day challenge that builds healthy habits.
            
            CHALLENGE REQUIREMENTS:
            - Title: Short, motivating (max 50 chars)
            - Description: Clear, achievable (max 100 chars)
            - Duration: 3-30 days
            - Category: SLEEP, SCREEN_TIME, HEALTH, SOCIAL, LEARNING
            - Success condition: Clear, measurable per day
            
            ${if (context.goals.isNotEmpty()) "Parent goals: ${context.goals.joinToString(", ")}" else ""}
            
            Return ONLY valid JSON (no markdown, no extra text):
            {
              "title": "string",
              "description": "string",
              "durationDays": number,
              "category": "string",
              "successCondition": "string"
            }
        """.trimIndent()
    }

    private suspend fun getQuota(userId: String, type: GenerationType): QuotaInfo {
        return try {
            val doc = firestore.collection("ai_quotas").document(userId).get().await()
            if (doc.exists()) {
                val data = doc.data ?: return QuotaInfo(limit = 1, remaining = 1, isExceeded = false)

                val tier = data["tier"] as? String ?: "FREE"
                val tasksGenerated = (data["tasksGenerated"] as? Number)?.toInt() ?: 0
                val tasksLimit = when (tier) {
                    "FREE" -> 1
                    "PRO" -> 20
                    "PREMIUM" -> 999
                    else -> 1
                }

                val isExceeded = tasksGenerated >= tasksLimit
                val remaining = maxOf(0, tasksLimit - tasksGenerated)

                QuotaInfo(limit = tasksLimit, remaining = remaining, isExceeded = isExceeded)
            } else {
                QuotaInfo(limit = 1, remaining = 1, isExceeded = false)
            }
        } catch (e: Exception) {
            Log.e("AIGeneration", "❌ Error getting quota: ${e.message}")
            QuotaInfo(limit = 1, remaining = 0, isExceeded = true)
        }
    }

    private suspend fun updateQuota(userId: String, type: GenerationType) {
        try {
            val doc = firestore.collection("ai_quotas").document(userId).get().await()
            val currentCount = (doc.data?.get("tasksGenerated") as? Number)?.toInt() ?: 0

            firestore.collection("ai_quotas").document(userId).update(
                mapOf("tasksGenerated" to currentCount + 1)
            ).await()

            Log.d("AIGeneration", "✅ Updated quota for $userId")
        } catch (e: Exception) {
            Log.e("AIGeneration", "❌ Error updating quota: ${e.message}")
        }
    }

    private suspend fun getCached(prompt: String, type: GenerationType): String? {
        return try {
            val hash = prompt.hashCode().toString()
            val doc = firestore.collection("ai_cache")
                .document("${type.name}_$hash")
                .get()
                .await()

            if (doc.exists()) {
                doc.data?.get("result") as? String
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AIGeneration", "❌ Error getting cache: ${e.message}")
            null
        }
    }

    private suspend fun cacheResult(prompt: String, type: GenerationType, result: String) {
        try {
            val hash = prompt.hashCode().toString()
            firestore.collection("ai_cache").document("${type.name}_$hash").set(
                mapOf(
                    "prompt" to prompt,
                    "result" to result,
                    "type" to type.name,
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()

            Log.d("AIGeneration", "✅ Result cached")
        } catch (e: Exception) {
            Log.e("AIGeneration", "❌ Error caching: ${e.message}")
        }
    }
}

enum class GenerationType {
    TASK,
    CHALLENGE,
    CUSTOM
}

data class GenerationContext(
    val userId: String,
    val familyId: String,
    val childAge: Int,
    val preferences: List<String> = emptyList(),
    val recentCompletions: List<String> = emptyList(),
    val goals: List<String> = emptyList(),
    val tier: SubscriptionTier = SubscriptionTier.FREE
)

data class QuotaInfo(
    val limit: Int,
    val remaining: Int,
    val isExceeded: Boolean
)

enum class SubscriptionTier {
    FREE,
    PRO,
    PREMIUM
}