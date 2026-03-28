package com.kidsroutine.feature.avatar.di

import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.database.dao.AvatarDao
import com.kidsroutine.core.database.AppDatabase
import com.kidsroutine.feature.avatar.data.AvatarRepository
import com.kidsroutine.feature.avatar.data.AvatarRepositoryImpl
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.daily.data.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AvatarModule {

    @Provides
    @Singleton
    fun provideAvatarDao(database: AppDatabase): AvatarDao =
        database.avatarDao()

    @Provides
    @Singleton
    fun provideAvatarRepository(
        firestore: FirebaseFirestore,
        avatarDao: AvatarDao,
        userRepository: UserRepository
    ): AvatarRepository =
        AvatarRepositoryImpl(firestore, avatarDao, userRepository)
}