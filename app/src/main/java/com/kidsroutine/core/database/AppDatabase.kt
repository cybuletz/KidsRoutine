package com.kidsroutine.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kidsroutine.core.database.dao.AvatarDao
import com.kidsroutine.core.database.dao.TaskInstanceDao
import com.kidsroutine.core.database.dao.TaskProgressDao
import com.kidsroutine.core.database.dao.UserDao
import com.kidsroutine.core.database.entity.AvatarEntity
import com.kidsroutine.core.database.entity.TaskInstanceEntity
import com.kidsroutine.core.database.entity.TaskProgressEntity
import com.kidsroutine.core.database.entity.UserEntity

// Migration from version 1 to 2 - Add avatar customization table
val MIGRATION_1_TO_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS avatar_customizations (
                userId TEXT PRIMARY KEY NOT NULL,
                bodyItemId TEXT NOT NULL DEFAULT '',
                bodyColor TEXT NOT NULL DEFAULT '#FF6B35',
                eyesItemId TEXT NOT NULL DEFAULT '',
                eyesColor TEXT NOT NULL DEFAULT '#FF6B35',
                mouthItemId TEXT NOT NULL DEFAULT '',
                mouthColor TEXT NOT NULL DEFAULT '#FF6B35',
                hairstyleItemId TEXT NOT NULL DEFAULT '',
                hairstyleColor TEXT NOT NULL DEFAULT '#FF6B35',
                accessoriesItemId TEXT NOT NULL DEFAULT '',
                accessoriesColor TEXT NOT NULL DEFAULT '#FF6B35',
                clothingItemId TEXT NOT NULL DEFAULT '',
                clothingColor TEXT NOT NULL DEFAULT '#FF6B35',
                backgroundItemId TEXT NOT NULL DEFAULT '',
                backgroundColor TEXT NOT NULL DEFAULT '#FFFFFF',
                unlockedItemIds TEXT NOT NULL DEFAULT '',
                lastUpdated INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

@Database(
    entities = [
        TaskInstanceEntity::class,
        TaskProgressEntity::class,
        UserEntity::class,
        AvatarEntity::class  // ← ADD THIS
    ],
    version = 3,  // ← CHANGE FROM 1 TO 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskInstanceDao(): TaskInstanceDao
    abstract fun taskProgressDao(): TaskProgressDao
    abstract fun userDao(): UserDao
    abstract fun avatarDao(): AvatarDao  // ← ADD THIS

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kidsroutine.db"
                )
                    .addMigrations(MIGRATION_1_TO_2)  // ← ADD THIS
                    .build()
                    .also { instance = it }
            }
    }
}