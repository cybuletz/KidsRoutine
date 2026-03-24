package com.kidsroutine.di

import com.kidsroutine.feature.billing.BillingManager
import com.kidsroutine.feature.billing.BillingRepository
import com.kidsroutine.core.model.EntitlementsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides
    @Singleton
    fun provideBillingRepository(
        billingManager: BillingManager,
        entitlementsRepository: EntitlementsRepository
    ): BillingRepository = BillingRepository(billingManager, entitlementsRepository)
}