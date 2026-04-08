package com.kidsroutine.feature.storyarc.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.UserModel

private val StoryPurple = Color(0xFF8B5CF6)
private val StoryPurpleDark = Color(0xFF6D28D9)
private val StoryPurpleLight = Color(0xFFDDD6FE)

@Composable
fun StoryArcScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit
) {
    val viewModel: StoryArcViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.familyId) {
        viewModel.loadArc(currentUser.familyId)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF0))
    ) {
        // Header
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(StoryPurple, StoryPurpleDark)))
                .padding(top = 48.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.TopStart)) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AutoStories, null, tint = Color.White, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(8.dp))
                Text("Story Adventures", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StoryPurple)
            }
            return@Column
        }

        val arc = state.arc
        if (arc == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("📖", fontSize = 64.sp)
                    Text("No Active Story", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(
                        "Generate a story arc from the AI Generation screen to start your adventure!",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
            return@Column
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Arc Title Card
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(StoryPurpleLight)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(arc.arcEmoji, fontSize = 32.sp)
                        Text(arc.arcTitle, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = StoryPurpleDark)
                    }
                    Text("Theme: ${arc.theme}", color = StoryPurple, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (arc.isComplete) "✅ Story Complete!" else "Day ${arc.currentDay} of ${arc.totalDays}",
                        fontWeight = FontWeight.Bold,
                        color = if (arc.isComplete) Color(0xFF2E7D32) else StoryPurple
                    )
                    // Progress dots
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(arc.totalDays) { day ->
                            val isCurrent = day == arc.currentDay - 1
                            val isCompleted = day < arc.currentDay - 1
                            Box(
                                Modifier
                                    .size(if (isCurrent) 14.dp else 10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCompleted -> Color(0xFF2E7D32)
                                            isCurrent -> StoryPurple
                                            else -> Color.LightGray
                                        }
                                    )
                            )
                        }
                    }
                }
            }

            // Chapter Tabs
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                arc.chapters.forEachIndexed { index, chapter ->
                    val isSelected = index == state.currentChapterIndex
                    val isLocked = index > arc.currentDay - 1
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isSelected -> StoryPurple
                                    isLocked -> Color.LightGray
                                    else -> StoryPurpleLight
                                }
                            )
                            .clickable(enabled = !isLocked) { viewModel.selectChapter(index) }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isLocked) "🔒" else "Day ${index + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else StoryPurpleDark
                        )
                    }
                }
            }

            // Selected Chapter Detail
            val chapter = arc.chapters.getOrNull(state.currentChapterIndex)
            if (chapter != null) {
                val glowTransition = rememberInfiniteTransition(label = "chapterGlow")
                val glowAlpha by glowTransition.animateFloat(
                    initialValue = 0.4f, targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "glowAlpha"
                )

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .graphicsLayer { shadowElevation = 4f }
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "📖 ${chapter.chapterTitle}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StoryPurpleDark
                        )

                        // Narrative text with italic styling
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(StoryPurpleLight.copy(alpha = glowAlpha))
                                .padding(16.dp)
                        ) {
                            Text(
                                chapter.narrative,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFF374151),
                                lineHeight = 22.sp
                            )
                        }

                        Divider(color = StoryPurpleLight, thickness = 1.dp)

                        // Task info
                        Text("Today's Quest:", fontWeight = FontWeight.Bold, color = StoryPurple)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, null, tint = StoryPurple)
                            Column(Modifier.weight(1f)) {
                                Text(chapter.taskTitle, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                                Text(chapter.taskDescription, fontSize = 13.sp, color = Color.Gray)
                            }
                        }

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "⏱ ~${chapter.estimatedDurationSec / 60} min",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                "⭐ ${chapter.xpReward} XP",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StoryPurple
                            )
                        }
                    }
                }
            }

            // Advance Day Button
            if (!arc.isComplete && state.currentChapterIndex == arc.currentDay - 1) {
                Button(
                    onClick = { viewModel.advanceDay() },
                    enabled = !state.isAdvancing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StoryPurple),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (state.isAdvancing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            if (arc.currentDay >= arc.totalDays) "Complete Story ✨" else "Continue to Day ${arc.currentDay + 1} →",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(140.dp))
        }
    }
}
