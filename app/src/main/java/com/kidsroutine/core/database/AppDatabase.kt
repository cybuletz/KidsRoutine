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

val MIGRATION_4_TO_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE task_instances ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING'")
        database.execSQL("ALTER TABLE task_instances ADD COLUMN completedAt INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [
        TaskInstanceEntity::class,
        TaskProgressEntity::class,
        UserEntity::class,
        AvatarEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskInstanceDao(): TaskInstanceDao
    abstract fun taskProgressDao(): TaskProgressDao
    abstract fun userDao(): UserDao
    abstract fun avatarDao(): AvatarDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kidsroutine.db"
                )
                    .addMigrations(MIGRATION_1_TO_2, MIGRATION_4_TO_5)
                    .build()
                    .also { instance = it }
            }
    }
}