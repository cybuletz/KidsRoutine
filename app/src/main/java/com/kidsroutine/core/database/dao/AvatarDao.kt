package com.kidsroutine.core.database.dao   // ← was wrong package, this is the fix

import androidx.room.*
import com.kidsroutine.core.database.entity.AvatarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AvatarDao {

    @Query("SELECT * FROM avatar WHERE userId = :userId LIMIT 1")
    suspend fun getAvatar(userId: String): AvatarEntity?

    @Query("SELECT * FROM avatar WHERE userId = :userId")
    fun observeAvatar(userId: String): Flow<AvatarEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAvatar(avatar: AvatarEntity)

    @Query("DELETE FROM avatar WHERE userId = :userId")
    suspend fun deleteAvatar(userId: String)

    @Query("UPDATE avatar SET unlockedItemIdsJson = :json WHERE userId = :userId")
    suspend fun updateUnlockedItems(userId: String, json: String)

    @Query("UPDATE avatar SET ownedPackIdsJson = :json WHERE userId = :userId")
    suspend fun updateOwnedPacks(userId: String, json: String)
}