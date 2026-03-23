package com.kidsroutine.core.ai.providers

import android.util.Log
import com.kidsroutine.core.ai.AIProvider
import com.kidsroutine.core.ai.Severity
import com.kidsroutine.core.ai.ValidationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/**
 * Google Gemini API provider implementation
 * Generates tasks and challenges using Gemini 2.5 Flash
 */
class GeminiProvider @Inject constructor(
    private val apiKey: String
) : AIProvider {

    override val name = "Google Gemini 2.5 Flash"
    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

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
            Log.d("Gemini", "Generating text with Gemini 2.5 Flash...")

            val fullPrompt = if (systemPrompt != null) {
                "$systemPrompt\n\n$prompt"
            } else {
                prompt
            }

            val request = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", fullPrompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("maxOutputTokens", maxTokens)
                    put("temperature", temperature)
                    put("responseMimeType", "application/json")
                })
                put("safetySettings", JSONArray().apply {
                    put(JSONObject().apply {
                        put("category", "HARM_CATEGORY_DANGEROUS_CONTENT")
                        put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                    })
                    put(JSONObject().apply {
                        put("category", "HARM_CATEGORY_HATE_SPEECH")
                        put("threshold", "BLOCK_LOW_AND_ABOVE")
                    })
                })
            }

            val response = makeRequest(request.toString())
            Log.d("Gemini", "Response received: ${response.take(100)}...")

            // Validate response
            val validation = validateResponse(response)
            if (!validation.isSafe && validation.severity == Severity.CRITICAL) {
                return Result.failure(Exception("Response failed safety check: ${validation.reason}"))
            }

            Result.success(response)
        } catch (e: Exception) {
            Log.e("Gemini", "Error: ${e.message}", e)
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
            Log.e("Gemini", "Stream error: ${e.message}")
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

    private suspend fun makeRequest(body: String): String {
        return try {
            val url = URL("$apiUrl?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 30000
                readTimeout = 30000
            }

            connection.outputStream.use { output ->
                output.write(body.toByteArray())
            }

            if (connection.responseCode == 200) {
                connection.inputStream.bufferedReader().use { reader ->
                    val json = JSONObject(reader.readText())
                    val candidates = json.getJSONArray("candidates")
                    candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                }
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.readText() ?: ""
                throw Exception("HTTP ${connection.responseCode}: $errorStream")
            }
        } catch (e: Exception) {
            Log.e("Gemini", "Request error: ${e.message}")
            throw Exception("Gemini API error: ${e.message}", e)
        }
    }
}