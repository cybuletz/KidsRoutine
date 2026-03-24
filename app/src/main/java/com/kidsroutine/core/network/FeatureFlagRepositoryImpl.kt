package com.kidsroutine.core.network

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.FeatureFlags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureFlagRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FeatureFlagRepository {

    private val _flags = MutableStateFlow(FeatureFlags())   // safe defaults
    override val flags: StateFlow<FeatureFlags> = _flags.asStateFlow()

    override suspend fun fetchFlags() {
        try {
            val doc = firestore.collection("feature_flags")
                .document("global")
                .get()
                .await()

            if (doc.exists()) {
                val data = doc.data ?: return
                _flags.value = FeatureFlags(
                    dailyTasksEnabled    = data["dailyTasksEnabled"]    as? Boolean ?: true,
                    challengesEnabled    = data["challengesEnabled"]    as? Boolean ?: true,
                    communityEnabled     = data["communityEnabled"]     as? Boolean ?: true,
                    aiGenerationEnabled  = data["aiGenerationEnabled"]  as? Boolean ?: true,
                    worldMapEnabled      = data["worldMapEnabled"]      as? Boolean ?: true,
                    lootBoxEnabled       = data["lootBoxEnabled"]       as? Boolean ?: true,
                    momentsEnabled       = data["momentsEnabled"]       as? Boolean ?: true,
                    seasonalThemesEnabled = data["seasonalThemesEnabled"] as? Boolean ?: false,
                    avatarShopEnabled    = data["avatarShopEnabled"]    as? Boolean ?: false,
                    contentPacksEnabled  = data["contentPacksEnabled"]  as? Boolean ?: false,
                    storyArcsEnabled     = data["storyArcsEnabled"]     as? Boolean ?: true,
                    weeklyPlannerEnabled = data["weeklyPlannerEnabled"] as? Boolean ?: false,
                )
                Log.d("FeatureFlags", "Loaded: ${_flags.value}")
            }
        } catch (e: Exception) {
            Log.e("FeatureFlags", "Failed to load flags — using defaults", e)
            // Keep the safe defaults already in _flags
        }
    }
}