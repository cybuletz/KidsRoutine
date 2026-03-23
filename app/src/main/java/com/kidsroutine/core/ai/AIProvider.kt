package com.kidsroutine.core.ai

import kotlinx.coroutines.flow.Flow

/**
 * Generic AI provider interface.
 * Implement this to add any AI provider (OpenAI, Gemini, Claude, etc.)
 */
interface AIProvider {

    /**
     * Provider name (for logging/analytics)
     */
    val name: String

    /**
     * Whether provider is configured and ready
     */
    suspend fun isConfigured(): Boolean

    /**
     * Generate text from prompt
     *
     * @param prompt The input prompt
     * @param maxTokens Maximum tokens in response
     * @param temperature Creativity (0.0-1.0)
     * @param systemPrompt System instruction
     * @return Generated text
     */
    suspend fun generateText(
        prompt: String,
        maxTokens: Int = 1000,
        temperature: Float = 0.7f,
        systemPrompt: String? = null
    ): Result<String>

    /**
     * Stream text generation (for long responses)
     */
    fun streamText(
        prompt: String,
        maxTokens: Int = 1000,
        temperature: Float = 0.7f,
        systemPrompt: String? = null
    ): Flow<Result<String>>

    /**
     * Validate response is safe (child-friendly, non-harmful)
     */
    suspend fun validateResponse(text: String): ValidationResult
}

/**
 * Validation result for safety checking
 */
data class ValidationResult(
    val isSafe: Boolean,
    val reason: String? = null,
    val severity: Severity = Severity.NONE
)

enum class Severity {
    NONE,
    WARNING,
    CRITICAL
}