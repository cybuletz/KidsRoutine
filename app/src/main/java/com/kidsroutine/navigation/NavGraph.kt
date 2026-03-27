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

    fun observeLiveUser(userId: String, initial: UserModel): StateFlow<UserModel> =
        userRepository.observeUser(userId)
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.WhileSubscribed(5_000),
                initialValue = initial
            )
}

@Composable
fun KidsRoutineNavGraph(
    currentUser: UserModel,
    onSignOut: () -> Unit          // ← NEW: wired from MainActivity's authViewModel.signOut()
) {
    val navController = rememberNavController()
    val navGraphViewModel: NavGraphViewModel = hiltViewModel()

    val liveUserFlow = remember(currentUser.userId) {
        navGraphViewModel.observeLiveUser(currentUser.userId, currentUser)
    }
    val liveUser by liveUserFlow.collectAsState()

    val seasonalTheme = remember { SeasonalThemeManager().getActiveTheme() }

    val parentDashboardViewModel: ParentDashboardViewModel = hiltViewModel()
    val parentDashboardUiState by parentDashboardViewModel.uiState.collectAsState()

    LaunchedEffect(liveUser.familyId) {
        if (liveUser.role == Role.PARENT && liveUser.familyId.isNotEmpty()) {
            parentDashboardViewModel.loadFamily(liveUser.familyId)
        }
    }

    // Also reload family whenever the ViewModel's family is null but liveUser now has a familyId
    LaunchedEffect(liveUser.familyId, parentDashboardUiState.family) {
        if (liveUser.role == Role.PARENT
            && liveUser.familyId.isNotEmpty()
            && parentDashboardUiState.family == null) {
            parentDashboardViewModel.loadFamily(liveUser.familyId)
        }
    }

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
                        userId      = doc.getString("userId") ?: memberId,
                        displayName = doc.getString("displayName") ?: "",
                        role        = Role.valueOf(doc.getString("role") ?: "CHILD"),
                        familyId    = doc.getString("familyId") ?: "",
                        xp          = (doc.getLong("xp") ?: 0L).toInt(),
                        level       = (doc.getLong("level") ?: 1L).toInt(),
                        streak      = (doc.getLong("streak") ?: 0L).toInt(),
                        avatarUrl   = doc.getString("avatarUrl") ?: "",
                        email       = doc.getString("email") ?: "",
                        lastActiveAt = doc.getLong("lastActiveAt") ?: 0L
                    )
                } else null
            } catch (e: Exception) { null }
        }
        value = fetched.ifEmpty { familyMemberIds.map { liveUser.copy(userId = it) } }
    }

    // Pre-compute children-only list for tabs that only need children
    val childrenOnly = remember(familyMembers) {
        familyMembers.filter { it.role == Role.CHILD }
    }

    CompositionLocalProvider(LocalSeasonalTheme provides seasonalTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController    = navController,
                startDestination = if (liveUser.role == Role.PARENT) "parent_graph" else "child_graph"
            ) {
                childNavGraph(liveUser, navController, onSignOut)
                parentNavGraph(
                    currentUser     = liveUser,
                    familyMembers   = familyMembers,
                    navController   = navController,
                    onSignOut       = onSignOut,
                    onSwitchToChild = { child ->
                        navController.navigate("child_graph") {
                            popUpTo("parent_graph") { inclusive = false }
                        }
                    }
                )
            }

            CelebrationOverlay()
            LootBoxOverlay()
        }
    }
}