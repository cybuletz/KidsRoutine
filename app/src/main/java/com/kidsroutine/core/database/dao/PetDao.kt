package com.kidsroutine.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kidsroutine.core.database.entity.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity)

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Query("SELECT * FROM pets WHERE userId = :userId LIMIT 1")
    suspend fun getPetByUserId(userId: String): PetEntity?

    @Query("SELECT * FROM pets WHERE userId = :userId LIMIT 1")
    fun observePetByUserId(userId: String): Flow<PetEntity?>

    @Query("SELECT * FROM pets WHERE petId = :petId LIMIT 1")
    suspend fun getPetById(petId: String): PetEntity?

    @Query("DELETE FROM pets WHERE userId = :userId")
    suspend fun deletePetByUserId(userId: String)

    @Query("UPDATE pets SET happiness = :happiness, energy = :energy, lastFedAt = :lastFedAt, totalFed = totalFed + 1 WHERE petId = :petId")
    suspend fun feedPet(petId: String, happiness: Int, energy: Int, lastFedAt: Long)

    @Query("UPDATE pets SET happiness = :happiness, energy = :energy, daysAlive = daysAlive + 1 WHERE petId = :petId")
    suspend fun applyDailyDecay(petId: String, happiness: Int, energy: Int)

    @Query("UPDATE pets SET stage = :stage, happiness = 100, energy = 100 WHERE petId = :petId")
    suspend fun evolvePet(petId: String, stage: String)
}
