package com.kidsroutine.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val role: String,
    val familyId: String,
    val displayName: String,
    val email: String = "",  // ADD THIS
    val avatarUrl: String = "",  // ADD THIS
    val isAdmin: Boolean = false,  // ADD THIS
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastActiveAt: Long = 0L,
    val createdAt: Long = 0L,  // ADD THIS if missing
    val totalXpEarned: Int = 0   // lifetime XP earned (never decremented)
)