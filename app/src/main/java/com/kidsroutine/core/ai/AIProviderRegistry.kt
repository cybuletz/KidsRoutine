package com.kidsroutine.core.ai

import android.util.Log
import javax.inject.Singleton

/**
 * Registry to manage AI providers (Gemini-only).
 * Simplified for single provider model.
 */
@Singleton
class AIProviderRegistry {

    private val providers = mutableMapOf<String, AIProvider>()
    private var activeProviderId: String? = null

    /**
     * Register Gemini provider
     */
    fun register(providerId: String, provider: AIProvider) {
        providers[providerId] = provider
        Log.d("AIRegistry", "✅ Registered: ${provider.name}")

        // Set as active if first provider
        if (activeProviderId == null) {
            activeProviderId = providerId
            Log.d("AIRegistry", "✅ Set as active: ${provider.name}")
        }
    }

    /**
     * Switch active provider (usually just Gemini)
     */
    fun setActive(providerId: String): Boolean {
        return if (providers.containsKey(providerId)) {
            activeProviderId = providerId
            Log.d("AIRegistry", "✅ Active provider: ${providers[providerId]?.name}")
            true
        } else {
            Log.e("AIRegistry", "❌ Provider not found: $providerId")
            false
        }
    }

    /**
     * Get active provider (Gemini)
     */
    fun getActive(): AIProvider? {
        return activeProviderId?.let { providers[it] }
    }

    /**
     * Get specific provider
     */
    fun get(providerId: String): AIProvider? {
        return providers[providerId]
    }

    /**
     * List all registered providers
     */
    fun listProviders(): Map<String, AIProvider> {
        return providers.toMap()
    }
}