package com.kidsroutine.core.di

import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.feature.challenges.data.ChallengeRepository
import com.kidsroutine.feature.challenges.data.ChallengeRepositoryImpl
import com.kidsroutine.feature.daily.data.DailyRepository
import com.kidsroutine.feature.daily.data.DailyRepositoryImpl
import com.kidsroutine.feature.execution.data.TaskProgressRepository
import com.kidsroutine.feature.execution.data.TaskProgressRepositoryImpl
import com.kidsroutine.feature.family.data.FamilyRepository
import com.kidsroutine.feature.family.data.FamilyRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFamilyRepository(impl: FamilyRepositoryImpl): FamilyRepository

    @Binds
    @Singleton
    abstract fun bindDailyRepository(impl: DailyRepositoryImpl): DailyRepository

    @Binds
    @Singleton
    abstract fun bindTaskProgressRepository(impl: TaskProgressRepositoryImpl): TaskProgressRepository

    @Binds
    @Singleton
    abstract fun bindChallengeRepository(impl: ChallengeRepositoryImpl): ChallengeRepository
}