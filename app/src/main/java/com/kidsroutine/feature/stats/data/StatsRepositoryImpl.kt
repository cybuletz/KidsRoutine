package com.kidsroutine.feature.stats.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*

@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : StatsRepository {

    override suspend fun getUserStats(userId: String): UserStatsModel? {
        return try {
            Log.d("StatsRepository", "Fetching user stats for: $userId")
            val userDoc = firestore.collection("users").document(userId).get().await()

            if (!userDoc.exists()) {
                Log.w("StatsRepository", "User not found: $userId")
                return null
            }

            val data = userDoc.data ?: return null

            // Count badges
            val badgesArray = data["badges"] as? List<*> ?: emptyList<Any>()

            // Calculate this week's XP
            val thisWeekXp = calculateWeeklyXp(userId)
            val thisMonthXp = calculateMonthlyXp(userId)

            UserStatsModel(
                userId = userId,
                displayName = data["displayName"] as? String ?: "Unknown",
                totalXp = (data["xp"] as? Number)?.toInt() ?: 0,
                level = calculateLevel((data["xp"] as? Number)?.toInt() ?: 0),
                currentStreak = (data["streak"] as? Number)?.toInt() ?: 0,
                longestStreak = (data["longestStreak"] as? Number)?.toInt() ?: 0,
                tasksCompleted = (data["tasksCompleted"] as? Number)?.toInt() ?: 0,
                badgesUnlocked = badgesArray.size,
                thisWeekXp = thisWeekXp,
                thisMonthXp = thisMonthXp
            )
        } catch (e: Exception) {
            Log.e("StatsRepository", "Error fetching user stats", e)
            null
        }
    }

    override suspend fun getFamilyStats(familyId: String): FamilyStatsModel? {
        return try {
            Log.d("StatsRepository", "Fetching family stats for: $familyId")
            val familyDoc = firestore.collection("families").document(familyId).get().await()

            if (!familyDoc.exists()) {
                Log.w("StatsRepository", "Family not found: $familyId")
                return null
            }

            val data = familyDoc.data ?: return null
            val memberIds = (data["memberIds"] as? List<*>)?.map { it.toString() } ?: emptyList()
            val familyXp = (data["familyXp"] as? Number)?.toInt() ?: 0

            // Count total tasks from all members
            val totalTasksCompleted = memberIds.sumOf { memberId ->
                val userDoc = firestore.collection("users").document(memberId).get().await()
                (userDoc.data?.get("tasksCompleted") as? Number)?.toInt() ?: 0
            }

            FamilyStatsModel(
                familyId = familyId,
                familyName = data["familyName"] as? String ?: "Family",
                memberCount = memberIds.size,
                familyXp = familyXp,
                familyStreak = (data["familyStreak"] as? Number)?.toInt() ?: 0,
                totalTasksCompleted = totalTasksCompleted,
                avgXpPerMember = if (memberIds.isNotEmpty()) familyXp / memberIds.size else 0
            )
        } catch (e: Exception) {
            Log.e("StatsRepository", "Error fetching family stats", e)
            null
        }
    }

    override fun observeUserStats(userId: String): Flow<UserStatsModel?> = flow {
        try {
            firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("StatsRepository", "Error observing user stats", error)
                        return@addSnapshotListener
                    }

                    if (snapshot?.exists() == true) {
                        val data = snapshot.data ?: return@addSnapshotListener
                        val badgesArray = data["badges"] as? List<*> ?: emptyList<Any>()

                        // Note: this is non-blocking but doesn't calculate weekly/monthly
                        // Use async if you need real-time weekly calculations
                    }
                }
        } catch (e: Exception) {
            Log.e("StatsRepository", "Error observing user stats", e)
        }
    }

    override suspend fun getWeeklyProgress(userId: String): List<Int> {
        return try {
            Log.d("StatsRepository", "Calculating weekly progress for: $userId")
            val dailyXp = mutableListOf<Int>()
            val calendar = Calendar.getInstance()

            // Get last 7 days of progress
            repeat(7) {
                val date = calendar.time
                val dateStr = formatDate(date)

                // Query taskProgress for this day
                val snapshot = firestore.collection("taskProgress")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("date", dateStr)
                    .get()
                    .await()

                val dayXp = snapshot.documents.sumOf { doc ->
                    (doc.data?.get("xpGained") as? Number)?.toInt() ?: 0
                }

                dailyXp.add(dayXp)
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }

            dailyXp.reverse()
            Log.d("StatsRepository", "Weekly progress: $dailyXp")
            dailyXp
        } catch (e: Exception) {
            Log.e("StatsRepository", "Error calculating weekly progress", e)
            emptyList()
        }
    }

    override suspend fun getMonthlyProgress(userId: String): List<Int> {
        return try {
            Log.d("StatsRepository", "Calculating monthly progress for: $userId")
            val weeklyXp = mutableListOf<Int>()
            val calendar = Calendar.getInstance()

            // Get last 4 weeks
            repeat(4) {
                var weekTotal = 0
                repeat(7) {
                    val date = calendar.time
                    val dateStr = formatDate(date)

                    val snapshot = firestore.collection("taskProgress")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("date", dateStr)
                        .get()
                        .await()

                    val dayXp = snapshot.documents.sumOf { doc ->
                        (doc.data?.get("xpGained") as? Number)?.toInt() ?: 0
                    }

                    weekTotal += dayXp
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                }

                weeklyXp.add(weekTotal)
            }

            weeklyXp.reverse()
            Log.d("StatsRepository", "Monthly progress: $weeklyXp")
            weeklyXp
        } catch (e: Exception) {
            Log.e("StatsRepository", "Error calculating monthly progress", e)
            emptyList()
        }
    }

    private fun calculateLevel(xp: Int): Int {
        // Each level requires 100 XP (adjust as needed)
        return (xp / 100) + 1
    }

    private suspend fun calculateWeeklyXp(userId: String): Int {
        return try {
            val calendar = Calendar.getInstance()
            var weekXp = 0

            repeat(7) {
                val dateStr = formatDate(calendar.time)
                val snapshot = firestore.collection("taskProgress")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("date", dateStr)
                    .get()
                    .await()

                weekXp += snapshot.documents.sumOf { doc ->
                    (doc.data?.get("xpGained") as? Number)?.toInt() ?: 0
                }

                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }

            weekXp
        } catch (e: Exception) {
            Log.e("StatsRepository", "Error calculating weekly XP", e)
            0
        }
    }

    private suspend fun calculateMonthlyXp(userId: String): Int {
        return try {
            val calendar = Calendar.getInstance()
            var monthXp = 0

            repeat(30) {
                val dateStr = formatDate(calendar.time)
                val snapshot = firestore.collection("taskProgress")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("date", dateStr)
                    .get()
                    .await()

                monthXp += snapshot.documents.sumOf { doc ->
                    (doc.data?.get("xpGained") as? Number)?.toInt() ?: 0
                }

                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }

            monthXp
        } catch (e: Exception) {
            Log.e("StatsRepository", "Error calculating monthly XP", e)
            0
        }
    }

    private fun formatDate(date: Date): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(date)
    }
}