package com.kidsroutine.di

import com.kidsroutine.core.engine.SeasonalThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SeasonalThemeModule {

    @Provides
    @Singleton
    fun provideSeasonalThemeManager(): SeasonalThemeManager = SeasonalThemeManager()
}