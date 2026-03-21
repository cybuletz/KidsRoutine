package com.kidsroutine.feature.achievements.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.database.dao.UserDao
import com.kidsroutine.core.model.AchievementType
import com.kidsroutine.core.model.Badge
import com.kidsroutine.core.model.UserAchievements
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AchievementRepository {

    override suspend fun checkAndUnlockAchievements(userId: String): List<Badge> {
        try {
            Log.d("AchievementRepository", "Checking achievements for user: $userId")

            // Get user data
            val userEntity = userDao.getUserSync(userId) ?: return emptyList()
            val userDoc = firestore.collection("users").document(userId).get().await()
            val userData = userDoc.data ?: return emptyList()

            val currentXp = userEntity.xp
            val tasksCompleted = (userData["tasksCompleted"] as? Number)?.toInt() ?: 0
            val challengesCompleted = (userData["challengesCompleted"] as? Number)?.toInt() ?: 0
            val currentStreak = userEntity.streak

            val existingBadges = (userData["badges"] as? List<*>)?.mapNotNull {
                (it as? Map<*, *>)?.let { badge ->
                    Badge(
                        id = badge["id"] as? String ?: "",
                        type = AchievementType.valueOf(badge["type"] as? String ?: "TASKS_COMPLETED_10"),
                        title = badge["title"] as? String ?: "",
                        description = badge["description"] as? String ?: "",
                        icon = badge["icon"] as? String ?: "",
                        unlockedAt = (badge["unlockedAt"] as? Number)?.toLong() ?: 0L,
                        isUnlocked = badge["isUnlocked"] as? Boolean ?: false
                    )
                }
            } ?: emptyList()

            val newBadges = mutableListOf<Badge>()

            // Check achievement conditions
            if (tasksCompleted >= 10 && !existingBadges.any { it.type == AchievementType.TASKS_COMPLETED_10 }) {
                newBadges.add(createBadge(AchievementType.TASKS_COMPLETED_10, "🎯 Task Master", "Complete 10 tasks"))
            }

            if (tasksCompleted >= 50 && !existingBadges.any { it.type == AchievementType.TASKS_COMPLETED_50 }) {
                newBadges.add(createBadge(AchievementType.TASKS_COMPLETED_50, "⭐ Task Legend", "Complete 50 tasks"))
            }

            if (currentXp >= 100 && !existingBadges.any { it.type == AchievementType.XP_EARNED_100 }) {
                newBadges.add(createBadge(AchievementType.XP_EARNED_100, "💪 XP Hunter", "Earn 100 XP"))
            }

            if (currentXp >= 500 && !existingBadges.any { it.type == AchievementType.XP_EARNED_500 }) {
                newBadges.add(createBadge(AchievementType.XP_EARNED_500, "🔥 XP Legend", "Earn 500 XP"))
            }

            if (currentStreak >= 7 && !existingBadges.any { it.type == AchievementType.STREAK_7_DAYS }) {
                newBadges.add(createBadge(AchievementType.STREAK_7_DAYS, "🌟 On Fire!", "7-day streak"))
            }

            if (currentStreak >= 30 && !existingBadges.any { it.type == AchievementType.STREAK_30_DAYS }) {
                newBadges.add(createBadge(AchievementType.STREAK_30_DAYS, "🏆 Unstoppable", "30-day streak"))
            }

            if (challengesCompleted >= 1 && !existingBadges.any { it.type == AchievementType.FIRST_CHALLENGE }) {
                newBadges.add(createBadge(AchievementType.FIRST_CHALLENGE, "🎪 Challenge Starter", "Complete first challenge"))
            }

            if (challengesCompleted >= 5 && !existingBadges.any { it.type == AchievementType.CHALLENGE_MASTER }) {
                newBadges.add(createBadge(AchievementType.CHALLENGE_MASTER, "👑 Challenge Master", "Complete 5 challenges"))
            }

            // Save new badges to Firestore
            if (newBadges.isNotEmpty()) {
                val allBadges = existingBadges + newBadges
                val badgesMap = allBadges.map { badge ->
                    mapOf(
                        "id" to badge.id,
                        "type" to badge.type.name,
                        "title" to badge.title,
                        "description" to badge.description,
                        "icon" to badge.icon,
                        "unlockedAt" to badge.unlockedAt,
                        "isUnlocked" to badge.isUnlocked
                    )
                }

                firestore.collection("users").document(userId)
                    .update(mapOf(
                        "badges" to badgesMap
                    ))
                    .await()
                Log.d("AchievementRepository", "Unlocked ${newBadges.size} new badges!")
            }

            return newBadges
        } catch (e: Exception) {
            Log.e("AchievementRepository", "Error checking achievements", e)
            return emptyList()
        }
    }

    override suspend fun getUserAchievements(userId: String): UserAchievements {
        return try {
            Log.d("AchievementRepository", "Getting achievements for user: $userId")
            val doc = firestore.collection("users").document(userId).get().await()
            val data = doc.data ?: return UserAchievements(userId = userId)

            val badges = (data["badges"] as? List<*>)?.mapNotNull {
                (it as? Map<*, *>)?.let { badge ->
                    Badge(
                        id = badge["id"] as? String ?: "",
                        type = AchievementType.valueOf(badge["type"] as? String ?: "TASKS_COMPLETED_10"),
                        title = badge["title"] as? String ?: "",
                        description = badge["description"] as? String ?: "",
                        icon = badge["icon"] as? String ?: "",
                        unlockedAt = (badge["unlockedAt"] as? Number)?.toLong() ?: 0L,
                        isUnlocked = badge["isUnlocked"] as? Boolean ?: false
                    )
                }
            } ?: emptyList()

            UserAchievements(
                userId = userId,
                badges = badges,
                totalBadgesUnlocked = badges.count { it.isUnlocked },
                lastUnlockedAt = badges.maxOfOrNull { it.unlockedAt } ?: 0L
            )
        } catch (e: Exception) {
            Log.e("AchievementRepository", "Error getting achievements", e)
            UserAchievements(userId = userId)
        }
    }

    override fun observeUserAchievements(userId: String): Flow<UserAchievements> = flow {
        try {
            Log.d("AchievementRepository", "Observing achievements for user: $userId")
            firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("AchievementRepository", "Error observing achievements", error)
                        return@addSnapshotListener
                    }

                    val data = snapshot?.data ?: return@addSnapshotListener
                    val badges = (data["badges"] as? List<*>)?.mapNotNull {
                        (it as? Map<*, *>)?.let { badge ->
                            Badge(
                                id = badge["id"] as? String ?: "",
                                type = AchievementType.valueOf(badge["type"] as? String ?: "TASKS_COMPLETED_10"),
                                title = badge["title"] as? String ?: "",
                                description = badge["description"] as? String ?: "",
                                icon = badge["icon"] as? String ?: "",
                                unlockedAt = (badge["unlockedAt"] as? Number)?.toLong() ?: 0L,
                                isUnlocked = badge["isUnlocked"] as? Boolean ?: false
                            )
                        }
                    } ?: emptyList()
                }
        } catch (e: Exception) {
            Log.e("AchievementRepository", "Error observing achievements", e)
        }
    }

    override suspend fun updateBadges(userId: String, badges: List<Badge>) {
        try {
            Log.d("AchievementRepository", "Updating ${badges.size} badges for user: $userId")

            val badgesMap = badges.map { badge ->
                mapOf(
                    "id" to badge.id,
                    "type" to badge.type.name,
                    "title" to badge.title,
                    "description" to badge.description,
                    "icon" to badge.icon,
                    "unlockedAt" to badge.unlockedAt,
                    "isUnlocked" to badge.isUnlocked
                )
            }

            firestore.collection("users").document(userId)
                .update(mapOf(
                    "badges" to badgesMap
                ))
                .await()

            Log.d("AchievementRepository", "Badges updated successfully")
        } catch (e: Exception) {
            Log.e("AchievementRepository", "Error updating badges", e)
        }
    }

    private fun createBadge(type: AchievementType, title: String, description: String): Badge {
        return Badge(
            id = "badge_${System.currentTimeMillis()}",
            type = type,
            title = title,
            description = description,
            icon = getIconForType(type),
            unlockedAt = System.currentTimeMillis(),
            isUnlocked = true
        )
    }

    private fun getIconForType(type: AchievementType): String = when (type) {
        AchievementType.TASKS_COMPLETED_10 -> "🎯"
        AchievementType.TASKS_COMPLETED_50 -> "⭐"
        AchievementType.XP_EARNED_100 -> "💪"
        AchievementType.XP_EARNED_500 -> "🔥"
        AchievementType.STREAK_7_DAYS -> "🌟"
        AchievementType.STREAK_30_DAYS -> "🏆"
        AchievementType.FIRST_CHALLENGE -> "🎪"
        AchievementType.CHALLENGE_MASTER -> "👑"
        AchievementType.COMMUNITY_CONTRIBUTOR -> "💝"
        AchievementType.FAMILY_HERO -> "🦸"
    }
}