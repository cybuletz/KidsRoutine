package com.kidsroutine.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.common.designsystem.theme.LocalSeasonalTheme
import com.kidsroutine.core.engine.SeasonalThemeManager
import com.kidsroutine.core.model.Role
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.celebrations.ui.CelebrationOverlay
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.family.ui.ParentDashboardViewModel
import com.kidsroutine.feature.lootbox.ui.LootBoxOverlay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class NavGraphViewModel @Inject constructor(
    val userRepository: UserRepository
) : ViewModel() {

    // Converts cold Room flow → hot StateFlow, subscribed ONCE for the VM lifetime
    fun observeLiveUser(userId: String, initial: UserModel): StateFlow<UserModel> =
        userRepository.observeUser(userId)
            .stateIn(
                scope         = viewModelScope,
                started       = SharingStarted.WhileSubscribed(5_000),
                initialValue  = initial
            )
}

@Composable
fun KidsRoutineNavGraph(currentUser: UserModel) {  // ← signature UNCHANGED — no new param
    val navController = rememberNavController()

    // Inject UserRepository through Hilt ViewModel — no parameter needed from MainActivity
    val navGraphViewModel: NavGraphViewModel = hiltViewModel()

    // ✅ FIX D: Observe live user from Room (updated after every task completion)
    val liveUserFlow = remember(currentUser.userId) {
        navGraphViewModel.observeLiveUser(currentUser.userId, currentUser)
    }
    val liveUser by liveUserFlow.collectAsState()

    // Seasonal theme active for the entire session
    val seasonalTheme = remember { SeasonalThemeManager().getActiveTheme() }

    // Parent family members
    val parentDashboardViewModel: ParentDashboardViewModel = hiltViewModel()
    val parentDashboardUiState by parentDashboardViewModel.uiState.collectAsState()

    LaunchedEffect(liveUser.familyId) {
        if (liveUser.role == Role.PARENT && liveUser.familyId.isNotEmpty()) {
            parentDashboardViewModel.loadFamily(liveUser.familyId)
        }
    }

    // ✅ FIX E: Real family members fetched from Firestore
    val familyMemberIds = parentDashboardUiState.family?.memberIds ?: emptyList()
    val familyMembers by produceState<List<UserModel>>(
        initialValue = familyMemberIds.map { liveUser.copy(userId = it) },
        key1         = familyMemberIds
    ) {
        if (familyMemberIds.isEmpty()) return@produceState
        val db = FirebaseFirestore.getInstance()
        val fetched = familyMemberIds.mapNotNull { memberId ->
            try {
                val doc = db.collection("users").document(memberId).get().await()
                if (doc.exists()) {
                    UserModel(
                        userId       = doc.getString("userId") ?: memberId,
                        displayName  = doc.getString("displayName") ?: "",
                        role         = Role.valueOf(doc.getString("role") ?: "CHILD"),
                        familyId     = doc.getString("familyId") ?: "",
                        xp           = (doc.getLong("xp") ?: 0L).toInt(),
                        level        = (doc.getLong("level") ?: 1L).toInt(),
                        streak       = (doc.getLong("streak") ?: 0L).toInt(),
                        avatarUrl    = doc.getString("avatarUrl") ?: "",
                        email        = doc.getString("email") ?: "",
                        lastActiveAt = doc.getLong("lastActiveAt") ?: 0L
                    )
                } else null
            } catch (e: Exception) { null }
        }
        value = fetched.ifEmpty { familyMemberIds.map { liveUser.copy(userId = it) } }
    }

    // ✅ FIX: Seasonal theme wraps entire NavHost
    CompositionLocalProvider(LocalSeasonalTheme provides seasonalTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController    = navController,
                startDestination = if (currentUser.role == Role.PARENT) "parent_graph" else "child_graph"
            ) {
                childNavGraph(currentUser, navController)    // ← stable, never changes
                parentNavGraph(currentUser, familyMembers, navController)
            }

            CelebrationOverlay()
            LootBoxOverlay()
        }
    }
}
