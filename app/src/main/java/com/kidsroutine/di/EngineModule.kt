package com.kidsroutine.di

import com.google.gson.Gson
import com.kidsroutine.core.engine.challenge_engine.ChallengeEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {
    // TaskEngine, ProgressionEngine are @Singleton @Inject constructor — Hilt auto-provides them.
    // Add explicit @Provides here only if constructor injection isn't possible.

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideChallengeEngine(): ChallengeEngine = ChallengeEngine()
}