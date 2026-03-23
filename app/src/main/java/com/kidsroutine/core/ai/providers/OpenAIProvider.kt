package com.kidsroutine.core.ai.providers

import android.util.Log
import com.kidsroutine.core.ai.AIProvider
import com.kidsroutine.core.ai.Severity
import com.kidsroutine.core.ai.ValidationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Inject
import com.google.gson.annotations.SerializedName

/**
 * OpenAI GPT provider implementation using Retrofit
 */
class OpenAIProvider @Inject constructor(
    private val apiKey: String,
    private val model: String = "gpt-3.5-turbo"
) : AIProvider {

    override val name = "OpenAI ($model)"

    private val api: OpenAIService = Retrofit.Builder()
        .baseUrl("https://api.openai.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenAIService::class.java)

    override suspend fun isConfigured(): Boolean {
        return apiKey.isNotEmpty()
    }

    override suspend fun generateText(
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        systemPrompt: String?
    ): Result<String> {
        return try {
            Log.d("OpenAI", "Generating text with $model...")

            val messages = mutableListOf<ChatMessage>()

            if (systemPrompt != null) {
                messages.add(ChatMessage("system", systemPrompt))
            }

            messages.add(ChatMessage("user", prompt))

            val request = ChatCompletionRequest(
                model = model,
                messages = messages,
                maxTokens = maxTokens,
                temperature = temperature,
                topP = 0.9
            )

            val response = api.createCompletion(
                "Bearer $apiKey",
                request
            )

            if (response.choices.isEmpty()) {
                throw Exception("No choices in response")
            }

            val content = response.choices[0].message.content

            // Validate response
            val validation = validateResponse(content)
            if (!validation.isSafe && validation.severity == Severity.CRITICAL) {
                return Result.failure(Exception("Response failed safety check: ${validation.reason}"))
            }

            Result.success(content)
        } catch (e: Exception) {
            Log.e("OpenAI", "Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun streamText(
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        systemPrompt: String?
    ): Flow<Result<String>> = flow {
        try {
            val result = generateText(prompt, maxTokens, temperature, systemPrompt)
            emit(result)
        } catch (e: Exception) {
            Log.e("OpenAI", "Stream error: ${e.message}")
            emit(Result.failure(e))
        }
    }

    override suspend fun validateResponse(text: String): ValidationResult {
        val dangerousPatterns = listOf(
            "violence", "harm", "abuse", "illegal",
            "explicit", "inappropriate", "dangerous"
        )

        val lowerText = text.lowercase()
        val found = dangerousPatterns.find { lowerText.contains(it) }

        return if (found != null) {
            ValidationResult(
                isSafe = false,
                reason = "Contains unsafe content: $found",
                severity = Severity.CRITICAL
            )
        } else {
            ValidationResult(isSafe = true)
        }
    }
}

// ── RETROFIT SERVICE ────────────────────────────────────────────────

interface OpenAIService {
    @POST("chat/completions")
    suspend fun createCompletion(
        @Header("Authorization") auth: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

// ── DATA CLASSES ────────────────────────────────────────────────

data class ChatCompletionRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<ChatMessage>,
    @SerializedName("max_tokens")
    val maxTokens: Int,
    @SerializedName("temperature")
    val temperature: Float,
    @SerializedName("top_p")
    val topP: Float
)

data class ChatMessage(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)

data class ChatCompletionResponse(
    @SerializedName("choices")
    val choices: List<Choice>
)

data class Choice(
    @SerializedName("message")
    val message: ChatMessage
)