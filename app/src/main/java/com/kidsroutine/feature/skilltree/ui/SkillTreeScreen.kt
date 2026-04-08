package com.kidsroutine.feature.skilltree.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.SkillBranch
import com.kidsroutine.core.model.SkillNode
import com.kidsroutine.core.model.UserModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillTreeScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit
) {
    val viewModel: SkillTreeViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.userId) {
        viewModel.loadSkillTree(currentUser.userId)
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
                .background(
                    Brush.verticalGradient(
                        listOf(Color(state.selectedBranch.color), Color(state.selectedBranch.color).copy(alpha = 0.7f))
                    )
                )
                .padding(top = 48.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.TopStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🌳", fontSize = 36.sp)
                Spacer(Modifier.height(4.dp))
                Text("Skill Tree", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                val tree = state.tree
                if (tree != null) {
                    Text(
                        "${tree.totalNodesUnlocked} skills unlocked · +${tree.totalXpBonusPercent}% XP bonus",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        val tree = state.tree
        if (tree == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Could not load skill tree", color = Color.Gray)
            }
            return@Column
        }

        // Branch Tabs
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SkillBranch.entries.forEach { branch ->
                val isSelected = branch == state.selectedBranch
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(branch.color).copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { viewModel.selectBranch(branch) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(branch.emoji, fontSize = 20.sp)
                    Text(
                        branch.displayName,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(branch.color) else Color.Gray
                    )
                }
            }
        }

        // Nodes for selected branch
        val branchNodes = tree.branches[state.selectedBranch] ?: emptyList()
        val branchColor = Color(state.selectedBranch.color)

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "${state.selectedBranch.emoji} ${state.selectedBranch.displayName} Path",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = branchColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(branchNodes) { node ->
                val isPrereqMet = node.prerequisiteNodeId == null ||
                    branchNodes.find { it.nodeId == node.prerequisiteNodeId }?.isUnlocked == true
                val canUnlock = isPrereqMet && node.currentProgress >= node.requiredTaskCount && !node.isUnlocked

                SkillNodeCard(
                    node = node,
                    branchColor = branchColor,
                    isPrereqMet = isPrereqMet,
                    canUnlock = canUnlock,
                    onClick = { viewModel.selectNode(node) }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        // Node Detail Dialog
        state.selectedNode?.let { node ->
            val branchNodes2 = tree.branches[state.selectedBranch] ?: emptyList()
            val isPrereqMet = node.prerequisiteNodeId == null ||
                branchNodes2.find { it.nodeId == node.prerequisiteNodeId }?.isUnlocked == true
            val canUnlock = isPrereqMet && node.currentProgress >= node.requiredTaskCount && !node.isUnlocked

            AlertDialog(
                onDismissRequest = { viewModel.dismissNodeDetail() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(node.emoji, fontSize = 28.sp)
                        Text(node.title, fontWeight = FontWeight.ExtraBold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (node.description.isNotBlank()) {
                            Text(node.description, color = Color.Gray)
                        }
                        Divider()
                        Text("Progress: ${node.currentProgress}/${node.requiredTaskCount} tasks", fontWeight = FontWeight.SemiBold)
                        val progress by animateFloatAsState(node.progressPercent, tween(500), label = "nodeProgress")
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.LightGray)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(branchColor)
                            )
                        }
                        Text("Reward: +${node.xpBonusPercent}% XP on ${state.selectedBranch.displayName} tasks",
                            color = branchColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        if (node.avatarItemId != null) {
                            Text("🎨 Unlocks avatar item!", fontSize = 13.sp, color = Color(0xFF9B59B6))
                        }
                        if (node.isUnlocked) {
                            Text("✅ Unlocked!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        } else if (!isPrereqMet) {
                            Text("🔒 Complete prerequisite first", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    if (canUnlock) {
                        Button(
                            onClick = { viewModel.unlockNode(currentUser.userId, node.nodeId) },
                            colors = ButtonDefaults.buttonColors(containerColor = branchColor)
                        ) {
                            Text("Unlock! 🔓", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        TextButton(onClick = { viewModel.dismissNodeDetail() }) {
                            Text("Close")
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun SkillNodeCard(
    node: SkillNode,
    branchColor: Color,
    isPrereqMet: Boolean,
    canUnlock: Boolean,
    onClick: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = node.progressPercent,
        animationSpec = tween(500),
        label = "nodeProgress"
    )

    val cardAlpha = if (!isPrereqMet && !node.isUnlocked) 0.5f else 1f

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .then(
                if (node.isUnlocked) Modifier.border(2.dp, branchColor, RoundedCornerShape(16.dp))
                else if (canUnlock) Modifier.border(2.dp, branchColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(enabled = isPrereqMet || node.isUnlocked) { onClick() }
            .padding(16.dp)
            .scale(1f) // ensures no visual glitches
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Node Icon
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (node.isUnlocked) branchColor.copy(alpha = 0.2f)
                        else Color(0xFFF5F5F5)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!isPrereqMet && !node.isUnlocked) {
                    Icon(Icons.Default.Lock, "Locked", tint = Color.Gray, modifier = Modifier.size(24.dp))
                } else {
                    Text(node.emoji, fontSize = 24.sp)
                }
            }

            Column(Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(node.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1F2937))
                    if (node.isUnlocked) {
                        Icon(Icons.Default.Star, "Unlocked", tint = branchColor, modifier = Modifier.size(16.dp))
                    }
                }

                Text(
                    "${node.currentProgress}/${node.requiredTaskCount} tasks",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(4.dp))

                // Progress bar
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (node.isUnlocked) branchColor else branchColor.copy(alpha = 0.5f))
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("+${node.xpBonusPercent}%", fontWeight = FontWeight.ExtraBold, color = branchColor, fontSize = 14.sp)
                Text("XP", fontSize = 10.sp, color = Color.Gray)
                if (canUnlock) {
                    Spacer(Modifier.height(4.dp))
                    Text("READY!", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = branchColor)
                }
            }
        }
    }
}
