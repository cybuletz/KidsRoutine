package com.kidsroutine.feature.community.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.ChildLeaderboardEntry
import com.kidsroutine.core.model.FamilyLeaderboardEntry
import com.kidsroutine.core.model.ChallengeLeaderboardEntry
import com.kidsroutine.core.model.LeaderboardEntry
import com.kidsroutine.feature.community.data.CommunityRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val activeTab: LeaderboardTab = LeaderboardTab.CHILDREN,
    val childLeaderboard: List<ChildLeaderboardEntry> = emptyList(),
    val familyLeaderboard: List<FamilyLeaderboardEntry> = emptyList(),
    val challengeLeaderboard: List<ChallengeLeaderboardEntry> = emptyList(),
    val myFamilyLeaderboard: List<LeaderboardEntry> = emptyList(),
    val lastUpdated: String = "",   // human-readable "Updated at HH:mm"
    val error: String? = null
)

enum class LeaderboardTab {
    CHILDREN,
    FAMILIES,
    CHALLENGES,
    MY_FAMILY
}

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    // ── Read pre-computed snapshots written by the Cloud Function ──────────
    fun loadLeaderboards() {
        Log.d("LeaderboardVM", "Loading leaderboard snapshots")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // ── Children ─────────────────────────────────────────────
                val childDoc = firestore
                    .collection("leaderboard_snapshots")
                    .document("children")
                    .get()
                    .await()

                val childEntries: List<ChildLeaderboardEntry> = if (childDoc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val raw = childDoc.get("entries") as? List<Map<String, Any>> ?: emptyList()
                    raw.map { m ->
                        ChildLeaderboardEntry(
                            userId      = m["userId"]      as? String ?: "",
                            displayName = m["displayName"] as? String ?: "Unknown",
                            familyId    = m["familyId"]    as? String ?: "",
                            avatarUrl   = m["avatarUrl"]   as? String ?: "",
                            xp          = (m["xp"]         as? Number)?.toInt() ?: 0,
                            rank        = (m["rank"]       as? Number)?.toInt() ?: 0,
                            level       = (m["level"]      as? Number)?.toInt() ?: 1,
                            streak      = (m["streak"]     as? Number)?.toInt() ?: 0,
                            badges      = (m["badges"]     as? Number)?.toInt() ?: 0
                        )
                    }
                } else {
                    // Snapshot not yet generated — fall back to live query (first run)
                    Log.w("LeaderboardVM", "No snapshot yet — falling back to live query")
                    communityRepository.getChildLeaderboard(limit = 100)
                }

                // ── Families ─────────────────────────────────────────────
                val familyDoc = firestore
                    .collection("leaderboard_snapshots")
                    .document("families")
                    .get()
                    .await()

                val familyEntries: List<FamilyLeaderboardEntry> = if (familyDoc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val raw = familyDoc.get("entries") as? List<Map<String, Any>> ?: emptyList()
                    raw.map { m ->
                        FamilyLeaderboardEntry(
                            familyId    = m["familyId"]   as? String ?: "",
                            familyName  = m["familyName"] as? String ?: "Unknown Family",
                            streak      = (m["streak"]    as? Number)?.toInt() ?: 0,
                            totalXp     = (m["familyXp"]  as? Number)?.toInt() ?: 0,
                            memberCount = (m["memberCount"] as? Number)?.toInt() ?: 0,
                            rank        = (m["rank"]      as? Number)?.toInt() ?: 0
                        )
                    }
                } else {
                    communityRepository.getFamilyLeaderboard(limit = 50)
                }

                // ── Challenges ────────────────────────────────────────────
                val challengeDoc = firestore
                    .collection("leaderboard_snapshots")
                    .document("challenges")
                    .get()
                    .await()

                val challengeEntries: List<ChallengeLeaderboardEntry> = if (challengeDoc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val raw = challengeDoc.get("entries") as? List<Map<String, Any>> ?: emptyList()
                    raw.map { m ->
                        ChallengeLeaderboardEntry(
                            challengeId  = m["challengeId"]  as? String ?: "",
                            title        = m["title"]        as? String ?: "",
                            completedByCount = (m["completions"] as? Number)?.toInt() ?: 0,
                            rank         = (m["rank"]        as? Number)?.toInt() ?: 0
                        )
                    }
                } else {
                    communityRepository.getChallengeLeaderboard(limit = 50)
                }

                // ── Compute "last updated" time ───────────────────────────
                val computedAt = childDoc.getTimestamp("computedAt")
                val updatedText = if (computedAt != null) {
                    val date = computedAt.toDate()
                    val sdf  = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    "Updated at ${sdf.format(date)}"
                } else "Live data"

                _uiState.value = _uiState.value.copy(
                    isLoading          = false,
                    childLeaderboard   = childEntries,
                    familyLeaderboard  = familyEntries,
                    challengeLeaderboard = challengeEntries,
                    lastUpdated        = updatedText,
                    error              = null
                )
                Log.d("LeaderboardVM", "Snapshots loaded. Children: ${childEntries.size}")

            } catch (e: Exception) {
                Log.e("LeaderboardVM", "Error loading snapshots", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = "Failed to load leaderboard: ${e.message}"
                )
            }
        }
    }

    // ── My Family leaderboard — still live (small dataset per family) ──────
    fun loadMyFamilyLeaderboard(familyId: String) {
        viewModelScope.launch {
            try {
                val familyLeaderboard = communityRepository.getWeeklyFamilyLeaderboard(familyId)
                val entries = familyLeaderboard.entries
                _uiState.value = _uiState.value.copy(myFamilyLeaderboard = entries)
            } catch (e: Exception) {
                Log.e("LeaderboardVM", "Error loading family leaderboard", e)
            }
        }
    }

    fun selectTab(tab: LeaderboardTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }
}