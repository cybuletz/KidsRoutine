package com.kidsroutine.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for the companion pet system.
 * One pet per child user, evolves over time.
 */
@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey val petId: String,
    val userId: String,
    val species: String,          // PetSpecies enum name
    val name: String = "",
    val stage: String = "EGG",    // PetEvolutionStage enum name
    val happiness: Int = 80,
    val energy: Int = 80,
    val style: Int = 0,
    val totalFed: Int = 0,
    val daysAlive: Int = 0,
    val longestHappyStreak: Int = 0,
    val hatchedAt: Long = 0L,
    val lastFedAt: Long = 0L,
    val lastInteractedAt: Long = 0L,
    val accessoryId: String? = null,
    val isPremium: Boolean = false
)
