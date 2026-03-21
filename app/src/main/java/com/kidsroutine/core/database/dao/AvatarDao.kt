package com.kidsroutine.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kidsroutine.core.database.entity.AvatarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AvatarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomization(entity: AvatarEntity)

    @Query("SELECT * FROM avatar_customizations WHERE userId = :userId")
    suspend fun getCustomization(userId: String): AvatarEntity?

    @Query("SELECT * FROM avatar_customizations WHERE userId = :userId")
    fun observeCustomization(userId: String): Flow<AvatarEntity?>

    @Query("DELETE FROM avatar_customizations WHERE userId = :userId")
    suspend fun deleteCustomization(userId: String)
}