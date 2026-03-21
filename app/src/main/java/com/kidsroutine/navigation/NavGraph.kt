// File: app/src/main/java/com/kidsroutine/navigation/NavGraph.kt
package com.kidsroutine.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.kidsroutine.core.model.Role
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.family.ui.ParentDashboardViewModel
import com.kidsroutine.feature.celebrations.ui.CelebrationOverlay
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

@Composable
fun KidsRoutineNavGraph(currentUser: UserModel) {
    val navController = rememberNavController()

    // Load family members for parent navigation using existing ViewModel
    val parentDashboardViewModel: ParentDashboardViewModel = hiltViewModel()
    val parentDashboardUiState by parentDashboardViewModel.uiState.collectAsState()

    // Load family when parent user logs in
    LaunchedEffect(currentUser.familyId) {
        if (currentUser.role == Role.PARENT && currentUser.familyId.isNotEmpty()) {
            parentDashboardViewModel.loadFamily(currentUser.familyId)
        }
    }

    // Get family members from family
    val familyMembers = parentDashboardUiState.family?.memberIds?.map { memberId ->
        // Convert member IDs to UserModel objects
        // For now, return an empty list and fetch on-demand in ParentProfileScreen
        currentUser.copy(userId = memberId)  // Placeholder
    } ?: emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = if (currentUser.role == Role.PARENT) "parent_graph" else "child_graph"
        ) {
            // Child routes
            childNavGraph(currentUser, navController)

            // Parent routes - with family members from ParentDashboardViewModel
            parentNavGraph(currentUser, familyMembers, navController)
        }

        // Celebration overlay - on TOP of everything
        CelebrationOverlay()
    }
}