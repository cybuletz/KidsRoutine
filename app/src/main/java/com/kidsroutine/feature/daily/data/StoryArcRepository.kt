package com.kidsroutine.feature.daily.data

import com.kidsroutine.core.model.StoryArc

/**
 * Reads and writes active story arcs for a family.
 * Firestore path: story_arcs/{arcId}  (field familyId for queries)
 */
interface StoryArcRepository {
    /** Returns the currently active (non-complete) arc for this family, or null. */
    suspend fun getActiveArc(familyId: String): StoryArc?

    /** Saves or updates a story arc (called after generation). */
    suspend fun saveArc(arc: StoryArc)

    /** Advances currentDay by 1. Called when child completes the story task. */
    suspend fun advanceDay(arcId: String)

    /** Marks arc as complete. */
    suspend fun completeArc(arcId: String)
}