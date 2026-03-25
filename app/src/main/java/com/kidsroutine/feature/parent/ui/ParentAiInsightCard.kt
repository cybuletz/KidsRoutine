package com.kidsroutine.feature.parent.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.generation.ui.GenerationViewModel

private val InsightBg    = Color(0xFF1A1A2E)
private val AccentBlue   = Color(0xFF4A90E2)
private val AccentPurple = Color(0xFF9B59B6)

/**
 * Shown in ParentHomeTab under the children list.
 * Tapping "Generate for [child]" opens GenerationScreen pre-scoped to that child.
 */
@Composable
fun ParentAiInsightCard(
    children: List<UserModel>,
    onGenerateForChild: (child: UserModel) -> Unit,
    viewModel: GenerationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (children.isEmpty()) return

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = InsightBg),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header ─────────────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Brush.linearGradient(listOf(AccentBlue, AccentPurple)),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("AI Task Generator", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Generate personalised tasks for each child", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            // ── Per-child row ──────────────────────────────────────────────
            children.forEach { child ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            child.displayName.split(" ").first(),
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White
                        )
                        Text(
                            "Level ${child.level} · ${child.streak}🔥 streak",
                            fontSize = 11.sp,
                            color    = Color.White.copy(alpha = 0.5f)
                        )
                    }

                    Button(
                        onClick  = { onGenerateForChild(child) },
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.2f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            "Generate ✨",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = AccentBlue
                        )
                    }
                }
            }

            // ── Quota indicator ────────────────────────────────────────────
            AnimatedVisibility(
                visible = state.quotaRemaining < 3,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                Text(
                    "${state.quotaRemaining} generations left today",
                    fontSize = 10.sp,
                    color    = if (state.quotaRemaining == 0) Color(0xFFFF6B6B) else Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}
