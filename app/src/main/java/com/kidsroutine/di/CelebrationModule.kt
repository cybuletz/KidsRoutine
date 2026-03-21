package com.kidsroutine.feature.celebrations.di

import com.kidsroutine.feature.celebrations.ui.CelebrationViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CelebrationModule {

    @Singleton
    @Provides
    fun provideCelebrationViewModel(): CelebrationViewModel {
        return CelebrationViewModel()
    }
}