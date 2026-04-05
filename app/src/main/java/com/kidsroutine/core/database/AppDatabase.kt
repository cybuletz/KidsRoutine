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
// REMOVED: import com.kidsroutine.core.database.converter.AvatarTypeConverters
// REMOVED: import androidx.room.TypeConverters

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

val MIGRATION_5_TO_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS `avatar_customizations`")
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `avatar` (
                `userId` TEXT NOT NULL PRIMARY KEY,
                `gender` TEXT NOT NULL DEFAULT 'BOY',
                `skinTone` INTEGER NOT NULL DEFAULT -3220563,
                `activeBackgroundId` TEXT,
                `activeHairId` TEXT,
                `activeOutfitId` TEXT,
                `activeShoesId` TEXT,
                `activeAccessoryId` TEXT,
                `activeSpecialFxId` TEXT,
                `unlockedItemIdsJson` TEXT NOT NULL DEFAULT '[]',
                `ownedPackIdsJson` TEXT NOT NULL DEFAULT '[]',
                `lastUpdated` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}

val MIGRATION_6_TO_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // ✅ Add familyId column to task_instances
        database.execSQL("ALTER TABLE task_instances ADD COLUMN familyId TEXT NOT NULL DEFAULT ''")
        // ✅ Add familyId column to task_progress
        database.execSQL("ALTER TABLE task_progress ADD COLUMN familyId TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_7_TO_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add eye style and face detail columns to avatar table
        database.execSQL("ALTER TABLE `avatar` ADD COLUMN `activeEyeStyleId` TEXT")
        database.execSQL("ALTER TABLE `avatar` ADD COLUMN `activeFaceDetailId` TEXT")
    }
}

val MIGRATION_8_TO_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add eye shape and hair colour override columns to avatar table
        database.execSQL("ALTER TABLE `avatar` ADD COLUMN `eyeShapeId` TEXT")
        database.execSQL("ALTER TABLE `avatar` ADD COLUMN `hairColorOverride` INTEGER")
    }
}

val MIGRATION_9_TO_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add mouth shape and eyebrow style columns to avatar table
        database.execSQL("ALTER TABLE `avatar` ADD COLUMN `mouthShapeId` TEXT")
        database.execSQL("ALTER TABLE `avatar` ADD COLUMN `eyebrowStyleId` TEXT")
    }
}

@Database(
    entities = [
        TaskInstanceEntity::class,
        TaskProgressEntity::class,
        UserEntity::class,
        AvatarEntity::class
    ],
    version = 10,
    exportSchema = false
)

// REMOVED: @TypeConverters(AvatarTypeConverters::class) — not needed, AvatarEntity has only primitives/Strings
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
                    .addMigrations(
                        MIGRATION_1_TO_2,
                        MIGRATION_4_TO_5,
                        MIGRATION_5_TO_6,
                        MIGRATION_6_TO_7,
                        MIGRATION_7_TO_8,
                        MIGRATION_8_TO_9,
                        MIGRATION_9_TO_10
                    )
                    .build()
                    .also { instance = it }
            }
    }
}