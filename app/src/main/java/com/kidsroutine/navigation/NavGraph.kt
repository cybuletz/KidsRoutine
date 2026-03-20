package com.kidsroutine.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.kidsroutine.core.model.Role
import com.kidsroutine.core.model.UserModel

@Composable
fun KidsRoutineNavGraph(currentUser: UserModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (currentUser.role == Role.PARENT) "parent_graph" else "child_graph"
    ) {
        // Child routes
        childNavGraph(currentUser, navController)

        // Parent routes
        parentNavGraph(currentUser, navController)
    }
}