package com.kidsroutine.feature.generation.data

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import com.kidsroutine.core.ai.AIGenerationService
import com.kidsroutine.core.model.DifficultyLevel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.kidsroutine.core.model.StoryArc
import com.kidsroutine.core.model.StoryChapter
import com.kidsroutine.core.model.TaskCategory
import com.kidsroutine.core.model.TaskCreator
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.TaskReward
import com.kidsroutine.core.model.TaskType

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

    /**
     * Generate a 3-day story arc via Cloud Function.
     */
    suspend fun generateStoryArc(
        familyId: String,
        childAge: Int,
        tier: String = "FREE"
    ): Result<GeneratedStoryResponse> {
        return try {
            Log.d("GenerationRepo", "⏳ Calling generateStoryTaskAI Cloud Function...")

            val result = functions.getHttpsCallable("generateStoryTaskAI")
                .call(mapOf(
                    "familyId"  to familyId,
                    "childAge"  to childAge,
                    "tier"      to tier
                ))
                .await()

            val data = result.data as? Map<*, *>
                ?: return Result.failure(Exception("Empty response"))

            val success = data["success"] as? Boolean ?: false
            if (!success) return Result.failure(Exception("Story generation failed"))

            val arcMap      = data["arc"] as? Map<*, *> ?: return Result.failure(Exception("No arc in response"))
            val chaptersRaw = arcMap["chapters"] as? List<*> ?: emptyList<Any>()

            val chapters = chaptersRaw.mapNotNull { ch ->
                (ch as? Map<*, *>)?.let { m ->
                    com.kidsroutine.core.model.StoryChapter(
                        day                  = (m["day"] as? Number)?.toInt() ?: 1,
                        chapterTitle         = m["chapterTitle"] as? String ?: "",
                        narrative            = m["narrative"]    as? String ?: "",
                        taskTitle            = m["taskTitle"]    as? String ?: "",
                        taskDescription      = m["taskDescription"] as? String ?: "",
                        estimatedDurationSec = (m["estimatedDurationSec"] as? Number)?.toInt() ?: 60,
                        category             = m["category"]    as? String ?: "CREATIVITY",
                        difficulty           = m["difficulty"]  as? String ?: "MEDIUM",
                        xpReward             = (m["xpReward"]   as? Number)?.toInt() ?: 50,
                        type                 = "STORY"
                    )
                }
            }

            val arc = com.kidsroutine.core.model.StoryArc(
                arcId      = arcMap["arcId"]      as? String ?: "",
                arcTitle   = arcMap["arcTitle"]   as? String ?: "",
                arcEmoji   = arcMap["arcEmoji"]   as? String ?: "📖",
                theme      = arcMap["theme"]      as? String ?: "",
                childAge   = (arcMap["childAge"]  as? Number)?.toInt() ?: childAge,
                familyId   = arcMap["familyId"]   as? String ?: familyId,
                chapters   = chapters,
                startDate  = arcMap["startDate"]  as? String ?: "",
                currentDay = (arcMap["currentDay"] as? Number)?.toInt() ?: 1,
                isComplete = arcMap["isComplete"] as? Boolean ?: false,
                createdAt  = (arcMap["createdAt"] as? Number)?.toLong() ?: 0L
            )

            Result.success(
                GeneratedStoryResponse(
                    success        = true,
                    arc            = arc,
                    cached         = data["cached"] as? Boolean ?: false,
                    quotaRemaining = (data["quotaRemaining"] as? Number)?.toInt() ?: 0
                )
            )
        } catch (e: Exception) {
            Log.e("GenerationRepo", "❌ Story arc error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Generate a full AI daily plan via Cloud Function.
     * PRO tier only. Reuses same FirebaseFunctions instance.
     */
    suspend fun generateDailyPlan(
        familyId: String,
        childAge: Int,
        preferences: List<String> = emptyList(),
        goals: List<String> = emptyList(),
        tier: String = "PRO",
        mood: String = "NORMAL"
    ): Result<DailyPlanResponse> {
        return try {
            Log.d("GenerationRepo", "⏳ Calling generateDailyPlanAI Cloud Function...")

            val result = functions.getHttpsCallable("generateDailyPlanAI")
                .call(mapOf(
                    "familyId"    to familyId,
                    "childAge"    to childAge,
                    "preferences" to preferences,
                    "goals"       to goals,
                    "tier"        to tier,
                    "mood"        to mood
                ))
                .await()

            val data = result.data as? Map<*, *>
                ?: throw Exception("Invalid response from Cloud Function")

            val planMap = data["plan"] as? Map<*, *>
                ?: throw Exception("Missing plan in response")

            val tasksRaw = planMap["tasks"] as? List<*> ?: emptyList<Any>()
            val tasks = tasksRaw.mapNotNull { t ->
                (t as? Map<*, *>)?.let { parsePlanTaskMap(it) }
            }

            val plan = GeneratedDailyPlan(
                theme    = planMap["theme"] as? String ?: "Adventure Day",
                totalXp  = (planMap["totalXp"] as? Number)?.toInt() ?: tasks.sumOf { it.xpReward },
                mood     = planMap["mood"] as? String ?: mood,
                tasks    = tasks
            )

            val response = DailyPlanResponse(
                success        = data["success"] as? Boolean ?: false,
                plan           = plan,
                cached         = data["cached"] as? Boolean ?: false,
                quotaRemaining = (data["quotaRemaining"] as? Number)?.toInt() ?: 0
            )

            if (response.success) {
                Log.d("GenerationRepo", "✅ Daily plan: \"${plan.theme}\", ${plan.tasks.size} tasks")
                Result.success(response)
            } else {
                Result.failure(Exception("Failed to generate daily plan"))
            }
        } catch (e: Exception) {
            Log.e("GenerationRepo", "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Generate a 7-day family plan via Cloud Function.
     * PRO tier only. children = list of maps with "name" and "age".
     */
    suspend fun generateWeeklyPlan(
        familyId: String,
        children: List<Map<String, Any>>,
        familyGoals: List<String> = emptyList(),
        tier: String = "PRO",
        weekTheme: String = "ADVENTURE"
    ): Result<WeeklyPlanResponse> {
        return try {
            Log.d("GenerationRepo", "⏳ Calling generateWeeklyPlanAI...")

            val result = functions.getHttpsCallable("generateWeeklyPlanAI")
                .call(mapOf(
                    "familyId"    to familyId,
                    "children"    to children,
                    "familyGoals" to familyGoals,
                    "tier"        to tier,
                    "weekTheme"   to weekTheme
                ))
                .await()

            val data = result.data as? Map<*, *>
                ?: throw Exception("Invalid response from Cloud Function")

            val planMap = data["weeklyPlan"] as? Map<*, *>
                ?: throw Exception("Missing weeklyPlan in response")

            val daysRaw = planMap["days"] as? List<*> ?: emptyList<Any>()
            val days = daysRaw.mapNotNull { d ->
                (d as? Map<*, *>)?.let { parseWeeklyDayMap(it) }
            }

            val plan = GeneratedWeeklyPlan(
                weekTheme      = planMap["weekTheme"] as? String ?: weekTheme,
                totalFamilyXp  = (planMap["totalFamilyXp"] as? Number)?.toInt()
                    ?: days.sumOf { day -> day.tasks.sumOf { it.xpReward } },
                days           = days
            )

            val response = WeeklyPlanResponse(
                success        = data["success"] as? Boolean ?: false,
                weeklyPlan     = plan,
                cached         = data["cached"] as? Boolean ?: false,
                quotaRemaining = (data["quotaRemaining"] as? Number)?.toInt() ?: 0
            )

            if (response.success) {
                Log.d("GenerationRepo", "✅ Weekly plan: ${plan.days.size} days, ${plan.totalFamilyXp} XP")
                Result.success(response)
            } else {
                Result.failure(Exception("Failed to generate weekly plan"))
            }
        } catch (e: Exception) {
            Log.e("GenerationRepo", "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun parseWeeklyDayMap(map: Map<*, *>): WeeklyDayPlan? {
        return try {
            val tasksRaw = map["tasks"] as? List<*> ?: emptyList<Any>()
            val tasks = tasksRaw.mapNotNull { t ->
                (t as? Map<*, *>)?.let { parseWeeklyTaskMap(it) }
            }
            WeeklyDayPlan(
                dayName  = map["dayName"] as? String ?: "Day",
                dayEmoji = map["dayEmoji"] as? String ?: "📅",
                tasks    = tasks
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseWeeklyTaskMap(map: Map<*, *>): WeeklyTask? {
        return try {
            WeeklyTask(
                childName            = map["childName"] as? String ?: "",
                title                = map["title"] as? String ?: return null,
                description          = map["description"] as? String ?: "",
                estimatedDurationSec = (map["estimatedDurationSec"] as? Number)?.toInt() ?: 30,
                category             = map["category"] as? String ?: "HEALTH",
                difficulty           = map["difficulty"] as? String ?: "EASY",
                xpReward             = (map["xpReward"] as? Number)?.toInt() ?: 10,
                type                 = map["type"] as? String ?: "REAL_LIFE",
                requiresCoop         = map["requiresCoop"] as? Boolean ?: false
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parsePlanTaskMap(map: Map<*, *>): GeneratedPlanTask? {
        return try {
            GeneratedPlanTask(
                title                = map["title"] as? String ?: return null,
                description          = map["description"] as? String ?: "",
                estimatedDurationSec = (map["estimatedDurationSec"] as? Number)?.toInt() ?: 30,
                category             = map["category"] as? String ?: "HEALTH",
                difficulty           = map["difficulty"] as? String ?: "EASY",
                xpReward             = (map["xpReward"] as? Number)?.toInt() ?: 10,
                type                 = map["type"] as? String ?: "REAL_LIFE",
                timeSlot             = map["timeSlot"] as? String ?: "MORNING",
                requiresCoop         = map["requiresCoop"] as? Boolean ?: false
            )
        } catch (e: Exception) {
            Log.e("GenerationRepo", "Failed to parse plan task: ${e.message}")
            null
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

fun GeneratedTask.toTaskModel(familyId: String): TaskModel {
    return TaskModel(
        id          = "gen_${System.currentTimeMillis()}",
        type        = TaskType.valueOf(this.type.takeIf { it.isNotBlank() } ?: "REAL_LIFE"),
        title       = this.title,
        description = this.description,
        category    = TaskCategory.valueOf(this.category.takeIf { it.isNotBlank() } ?: "CREATIVITY"),
        difficulty  = DifficultyLevel.valueOf(this.difficulty.takeIf { it.isNotBlank() } ?: "MEDIUM"),
        estimatedDurationSec = this.estimatedDurationSec,
        reward      = TaskReward(xp = this.xpReward),
        createdBy   = TaskCreator.PARENT_AI,
        familyId    = familyId
    )
}