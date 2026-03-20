package com.kidsroutine.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.feature.daily.ui.DailyScreen
import com.kidsroutine.feature.execution.ui.TaskExecutionScreen

// Shared in-memory task store (replaced by proper nav args / shared VM in MVP2)
object TaskPassthrough {
    var pendingTask: TaskModel? = null
}

@Composable
fun KidsRoutineNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController   = navController,
        startDestination = Routes.DAILY
    ) {
        composable(Routes.DAILY) {
            DailyScreen(
                onTaskClick = { instance ->
                    TaskPassthrough.pendingTask = instance.task
                    navController.navigate(Routes.execution(instance.instanceId))
                }
            )
        }

        composable(
            route     = Routes.EXECUTION,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) {
            val task = TaskPassthrough.pendingTask
            if (task != null) {
                TaskExecutionScreen(
                    task      = task,
                    onBack    = { navController.popBackStack() },
                    onCompleted = { _ ->
                        TaskPassthrough.pendingTask = null
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
