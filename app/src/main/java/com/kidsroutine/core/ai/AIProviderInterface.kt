package com.kidsroutine.core.ai

import kotlinx.coroutines.flow.Flow

/**
 * AI provider interface (Gemini implementation)
 */
interface AIProvider {

    val name: String

    suspend fun isConfigured(): Boolean

    suspend fun generateText(
        prompt: String,
        maxTokens: Int = 1000,
        temperature: Float = 0.7f,
        systemPrompt: String? = null
    ): Result<String>

    fun streamText(
        prompt: String,
        maxTokens: Int = 1000,
        temperature: Float = 0.7f,
        systemPrompt: String? = null
    ): Flow<Result<String>>

    suspend fun validateResponse(text: String): ValidationResult
}

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