package com.kidsroutine.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val role: String,
    val familyId: String,
    val displayName: String,
    val xp: Int,
    val level: Int,
    val streak: Int,
    val lastActiveAt: Long
)
