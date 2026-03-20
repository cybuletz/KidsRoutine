package com.kidsroutine.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kidsroutine.core.database.dao.TaskInstanceDao
import com.kidsroutine.core.database.dao.TaskProgressDao
import com.kidsroutine.core.database.dao.UserDao
import com.kidsroutine.core.database.entity.TaskInstanceEntity
import com.kidsroutine.core.database.entity.TaskProgressEntity
import com.kidsroutine.core.database.entity.UserEntity

@Database(
    entities = [
        TaskInstanceEntity::class,
        TaskProgressEntity::class,
        UserEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskInstanceDao(): TaskInstanceDao
    abstract fun taskProgressDao(): TaskProgressDao
    abstract fun userDao(): UserDao
}
