package com.kidsroutine.di

import android.content.Context
import androidx.room.Room
import com.kidsroutine.core.database.AppDatabase
import com.kidsroutine.core.database.dao.TaskInstanceDao
import com.kidsroutine.core.database.dao.TaskProgressDao
import com.kidsroutine.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "kidsroutine.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideTaskInstanceDao(db: AppDatabase): TaskInstanceDao = db.taskInstanceDao()
    @Provides fun provideTaskProgressDao(db: AppDatabase): TaskProgressDao = db.taskProgressDao()
    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
}