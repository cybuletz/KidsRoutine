package com.kidsroutine.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.kidsroutine.core.ai.AIGenerationService
import com.kidsroutine.core.ai.AIProviderRegistry
import com.kidsroutine.core.ai.providers.GeminiProvider
import com.kidsroutine.feature.generation.data.GenerationRepository
import com.kidsroutine.feature.generation.data.TaskSaveRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideAIProviderRegistry(
        @ApplicationContext context: Context,
        remoteConfig: FirebaseRemoteConfig
    ): AIProviderRegistry {
        val registry = AIProviderRegistry()

        try {
            // Fetch latest remote config
            remoteConfig.fetchAndActivate()

            // Get Gemini API key from Firebase Remote Config
            val geminiKey = remoteConfig.getString("GEMINI_API_KEY")

            // Register Gemini provider
            if (geminiKey.isNotEmpty()) {
                registry.register("gemini", GeminiProvider(geminiKey))
                android.util.Log.d("AIModule", "✅ Gemini provider registered")
                registry.setActive("gemini")
            } else {
                android.util.Log.e("AIModule", "❌ Gemini API key not found in Remote Config")
            }
        } catch (e: Exception) {
            android.util.Log.e("AIModule", "Error initializing AI providers: ${e.message}", e)
        }

        return registry
    }

    @Provides
    @Singleton
    fun provideAIGenerationService(
        registry: AIProviderRegistry,
        firestore: FirebaseFirestore
    ): AIGenerationService {
        return AIGenerationService(registry, firestore)
    }

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(
                com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600)  // 1 hour
                    .build()
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object GenerationModule {

    @Provides
    @Singleton
    fun provideGenerationRepository(
        functions: FirebaseFunctions,
        aiService: AIGenerationService
    ): GenerationRepository {
        return GenerationRepository(functions, aiService)
    }

    @Provides
    @Singleton
    fun provideTaskSaveRepository(
        firestore: FirebaseFirestore
    ): TaskSaveRepository {
        return TaskSaveRepository(firestore)
    }
}