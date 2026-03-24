package com.kidsroutine.core.network

import com.kidsroutine.core.model.FeatureFlags
import kotlinx.coroutines.flow.StateFlow

interface FeatureFlagRepository {
    val flags: StateFlow<FeatureFlags>
    suspend fun fetchFlags()
}