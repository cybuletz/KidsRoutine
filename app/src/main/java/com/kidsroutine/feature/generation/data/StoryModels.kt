package com.kidsroutine.feature.generation.data

import com.kidsroutine.core.model.StoryArc

/**
 * Parsed response from generateStoryTaskAI Cloud Function.
 */
data class GeneratedStoryResponse(
    val success: Boolean,
    val arc: StoryArc?,
    val cached: Boolean,
    val quotaRemaining: Int
)