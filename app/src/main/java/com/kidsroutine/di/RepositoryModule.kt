package com.kidsroutine.core.di

import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.EntitlementsRepository
import com.kidsroutine.core.network.FeatureFlagRepository
import com.kidsroutine.core.network.FeatureFlagRepositoryImpl
import com.kidsroutine.feature.achievements.data.AchievementRepository
import com.kidsroutine.feature.achievements.data.AchievementRepositoryImpl
import com.kidsroutine.feature.boss.data.BossRepository
import com.kidsroutine.feature.boss.data.BossRepositoryImpl
import com.kidsroutine.feature.challenges.data.ChallengeRepository
import com.kidsroutine.feature.challenges.data.ChallengeRepositoryImpl
import com.kidsroutine.feature.community.data.CommunityRepository
import com.kidsroutine.feature.community.data.CommunityRepositoryImpl
import com.kidsroutine.feature.daily.data.DailyRepository
import com.kidsroutine.feature.daily.data.DailyRepositoryImpl
import com.kidsroutine.feature.daily.data.StoryArcRepository
import com.kidsroutine.feature.daily.data.StoryArcRepositoryImpl
import com.kidsroutine.feature.pet.data.PetRepository
import com.kidsroutine.feature.pet.data.PetRepositoryImpl
import com.kidsroutine.feature.execution.data.TaskProgressRepository
import com.kidsroutine.feature.execution.data.TaskProgressRepositoryImpl
import com.kidsroutine.feature.family.data.FamilyMessageRepository
import com.kidsroutine.feature.family.data.FamilyMessageRepositoryImpl
import com.kidsroutine.feature.family.data.FamilyRepository
import com.kidsroutine.feature.family.data.FamilyRepositoryImpl
import com.kidsroutine.feature.moments.data.MomentsRepository
import com.kidsroutine.feature.moments.data.MomentsRepositoryImpl
import com.kidsroutine.feature.notifications.data.NotificationRepository
import com.kidsroutine.feature.notifications.data.NotificationRepositoryImpl
import com.kidsroutine.feature.stats.data.StatsRepository
import com.kidsroutine.feature.stats.data.StatsRepositoryImpl
import com.kidsroutine.feature.events.data.EventRepository
import com.kidsroutine.feature.events.data.EventRepositoryImpl
import com.kidsroutine.feature.spinwheel.data.SpinWheelRepository
import com.kidsroutine.feature.spinwheel.data.SpinWheelRepositoryImpl
import com.kidsroutine.feature.wallet.data.WalletRepository
import com.kidsroutine.feature.wallet.data.WalletRepositoryImpl
import com.kidsroutine.feature.skilltree.data.SkillTreeRepository
import com.kidsroutine.feature.skilltree.data.SkillTreeRepositoryImpl
import com.kidsroutine.feature.rituals.data.RitualsRepository
import com.kidsroutine.feature.rituals.data.RitualsRepositoryImpl
import com.kidsroutine.feature.world.data.WorldRepository
import com.kidsroutine.feature.world.data.WorldRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindFamilyRepository(impl: FamilyRepositoryImpl): FamilyRepository

    @Binds @Singleton
    abstract fun bindDailyRepository(impl: DailyRepositoryImpl): DailyRepository

    @Binds @Singleton
    abstract fun bindTaskProgressRepository(impl: TaskProgressRepositoryImpl): TaskProgressRepository

    @Binds @Singleton
    abstract fun bindChallengeRepository(impl: ChallengeRepositoryImpl): ChallengeRepository

    @Binds @Singleton
    abstract fun bindCommunityRepository(impl: CommunityRepositoryImpl): CommunityRepository

    @Binds @Singleton
    abstract fun bindAchievementRepository(impl: AchievementRepositoryImpl): AchievementRepository

    @Binds @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds @Singleton
    abstract fun bindFamilyMessageRepository(impl: FamilyMessageRepositoryImpl): FamilyMessageRepository

    @Binds @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository

    @Binds @Singleton
    abstract fun bindWorldRepository(impl: WorldRepositoryImpl): WorldRepository

    @Binds @Singleton
    abstract fun bindMomentsRepository(impl: MomentsRepositoryImpl): MomentsRepository

    @Binds @Singleton
    abstract fun bindFeatureFlagRepository(impl: FeatureFlagRepositoryImpl): FeatureFlagRepository

    @Binds @Singleton
    abstract fun bindStoryArcRepository(impl: StoryArcRepositoryImpl): StoryArcRepository

    @Binds @Singleton
    abstract fun bindPetRepository(impl: PetRepositoryImpl): PetRepository

    @Binds @Singleton
    abstract fun bindBossRepository(impl: BossRepositoryImpl): BossRepository

    @Binds @Singleton
    abstract fun bindSpinWheelRepository(impl: SpinWheelRepositoryImpl): SpinWheelRepository

    @Binds @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds @Singleton
    abstract fun bindSkillTreeRepository(impl: SkillTreeRepositoryImpl): SkillTreeRepository

    @Binds @Singleton
    abstract fun bindRitualsRepository(impl: RitualsRepositoryImpl): RitualsRepository

    companion object {

        @Provides
        @Singleton
        fun provideEntitlementsRepository(
            firestore: FirebaseFirestore
        ): EntitlementsRepository {
            return EntitlementsRepository(firestore)
        }
    }
}