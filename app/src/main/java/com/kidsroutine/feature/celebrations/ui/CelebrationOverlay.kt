package com.kidsroutine.feature.celebrations.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.AchievementType
import com.kidsroutine.core.model.Badge

/**
 * Global celebration overlay - Place this at the top level of your app
 * It will display celebrations over all other content
 */
@Composable
fun CelebrationOverlay(
    viewModel: CelebrationViewModel = hiltViewModel()  // Keep this - hilt will inject singleton now
) {
    val celebrationEvent by viewModel.celebrationEvent.collectAsState()
    val isVisible = celebrationEvent != null

    Log.d("CelebrationOverlay", "Event: $celebrationEvent, isVisible: $isVisible")

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Log.d("CelebrationOverlay", "Rendering celebration: ${celebrationEvent?.type}")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            when (celebrationEvent?.type) {
                CelebrationType.TASK_COMPLETION -> {
                    Log.d("CelebrationOverlay", "Showing TaskCompletionCelebration")
                    TaskCompletionCelebration(
                        onAnimationComplete = {
                            Log.d("CelebrationOverlay", "Animation complete, dismissing")
                            viewModel.dismissCelebration()
                        }
                    )
                }

                CelebrationType.ACHIEVEMENT_UNLOCK -> {
                    val badge = Badge(
                        id = "",
                        type = AchievementType.TASKS_COMPLETED_10,
                        title = "Achievement Unlocked",
                        description = celebrationEvent?.data ?: "Great job!",
                        icon = "⭐",
                        unlockedAt = System.currentTimeMillis(),
                        isUnlocked = true
                    )
                    AchievementUnlockCelebration(
                        badge = badge,
                        onAnimationComplete = { viewModel.dismissCelebration() }
                    )
                }

                CelebrationType.LEVEL_UP -> {
                    val level = celebrationEvent?.data?.toIntOrNull() ?: 1
                    LevelUpCelebration(
                        newLevel = level,
                        onAnimationComplete = { viewModel.dismissCelebration() }
                    )
                }

                CelebrationType.MILESTONE -> {
                    MilestoneCelebration(
                        milestone = celebrationEvent?.data ?: "Milestone!",
                        onAnimationComplete = { viewModel.dismissCelebration() }
                    )
                }

                null -> {
                    Log.d("CelebrationOverlay", "No celebration event")
                }
            }
        }
    }
}