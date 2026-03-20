package com.kidsroutine.di

import com.kidsroutine.feature.daily.data.DailyRepository
import com.kidsroutine.feature.daily.data.DailyRepositoryImpl
import com.kidsroutine.feature.execution.data.TaskProgressRepository
import com.kidsroutine.feature.execution.data.TaskProgressRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindDailyRepository(impl: DailyRepositoryImpl): DailyRepository

    @Binds @Singleton
    abstract fun bindTaskProgressRepository(impl: TaskProgressRepositoryImpl): TaskProgressRepository
}
