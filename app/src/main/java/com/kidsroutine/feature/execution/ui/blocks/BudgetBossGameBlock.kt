package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsroutine.core.common.util.SoundManager
import com.kidsroutine.core.model.AgeGroup
import kotlinx.coroutines.delay

/**
 * BUDGET BOSS GAME: Plan a budget under constraints.
 * Ages 17+: Real-world financial literacy.
 * Given a budget and items with prices, select the best combination.
 */
@Composable
fun BudgetBossGameBlock(
    ageGroup: AgeGroup = AgeGroup.LEGEND,
    onSuccess: () -> Unit
) {
    data class BudgetItem(val name: String, val emoji: String, val price: Int)
    data class BudgetChallenge(
        val scenario: String,
        val budget: Int,
        val items: List<BudgetItem>,
        val mustInclude: String,  // category that must be covered
        val targetSpend: IntRange // acceptable spend range
    )

    val challenges = listOf(
        BudgetChallenge(
            scenario = "Plan meals for the week with \$50",
            budget = 50,
            items = listOf(
                BudgetItem("Rice & Beans", "🍚", 8),
                BudgetItem("Chicken", "🍗", 12),
                BudgetItem("Vegetables", "🥦", 7),
                BudgetItem("Pasta", "🍝", 5),
                BudgetItem("Bread", "🍞", 4),
                BudgetItem("Snacks", "🍪", 6),
                BudgetItem("Fruit", "🍎", 8),
                BudgetItem("Juice", "🧃", 5)
            ),
            mustInclude = "Vegetables",
            targetSpend = 35..50
        ),
        BudgetChallenge(
            scenario = "Plan a birthday party with \$100",
            budget = 100,
            items = listOf(
                BudgetItem("Cake", "🎂", 25),
                BudgetItem("Decorations", "🎈", 15),
                BudgetItem("Pizza", "🍕", 30),
                BudgetItem("Drinks", "🥤", 10),
                BudgetItem("Party Games", "🎮", 20),
                BudgetItem("Gift Bags", "🎁", 12),
                BudgetItem("DJ/Music", "🎵", 35),
                BudgetItem("Photo Booth", "📸", 25)
            ),
            mustInclude = "Cake",
            targetSpend = 70..100
        )
    )

    var challenge by remember { mutableStateOf(challenges.random()) }
    var selectedItems by remember { mutableStateOf(setOf<BudgetItem>()) }
    var isSubmitted by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val totalSpend = selectedItems.sumOf { it.price }
    val isOverBudget = totalSpend > challenge.budget
    val includesRequired = selectedItems.any { it.name == challenge.mustInclude }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "💰 Budget Boss",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF2E7D32)
        )

        // Scenario
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2E7D32).copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Text(
                challenge.scenario,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Budget tracker
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Budget", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(
                    "\$${challenge.budget}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E7D32)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Spent", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(
                    "\$$totalSpend",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isOverBudget) Color(0xFFE53935) else Color(0xFF2E7D32)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Remaining", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(
                    "\$${(challenge.budget - totalSpend).coerceAtLeast(0)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isOverBudget) Color(0xFFE53935) else Color(0xFFFF6B35)
                )
            }
        }

        // Budget progress bar
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
                    .fillMaxWidth((totalSpend.toFloat() / challenge.budget).coerceIn(0f, 1f))
                    .background(if (isOverBudget) Color(0xFFE53935) else Color(0xFF2E7D32))
            )
        }

        if (!includesRequired && selectedItems.isNotEmpty()) {
            Text(
                "⚠️ Don't forget: ${challenge.mustInclude}!",
                color = Color(0xFFE67E22),
                fontWeight = FontWeight.Bold
            )
        }

        // Items grid
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            challenge.items.chunked(2).forEach { row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { item ->
                        val isSelected = item in selectedItems
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Color(0xFF2E7D32).copy(alpha = 0.2f)
                                    else Color(0xFFF5F5F5)
                                )
                                .clickable(enabled = !isSubmitted) {
                                    selectedItems = if (isSelected) selectedItems - item
                                    else selectedItems + item
                                }
                                .padding(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(item.emoji, fontSize = 24.sp)
                                Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, textAlign = TextAlign.Center)
                                Text("\$${item.price}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        // Submit
        Button(
            onClick = {
                isSubmitted = true
                val isGood = !isOverBudget && includesRequired && totalSpend in challenge.targetSpend
                if (isGood) {
                    SoundManager.playSuccess()
                    showSuccess = true
                    onSuccess()
                } else {
                    SoundManager.playError()
                }
            },
            enabled = !isSubmitted && selectedItems.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("Submit Budget ✓", color = Color.White, fontWeight = FontWeight.Bold)
        }

        AnimatedVisibility(visible = showSuccess, enter = scaleIn() + fadeIn()) {
            Text(
                "🎉 Budget Boss!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E7D32)
            )
        }

        if (isSubmitted && !showSuccess) {
            Text(
                buildString {
                    if (isOverBudget) append("❌ Over budget! ")
                    if (!includesRequired) append("❌ Missing ${challenge.mustInclude}! ")
                    if (totalSpend !in challenge.targetSpend) append("❌ Spend at least \$${challenge.targetSpend.first}")
                },
                color = Color(0xFFE53935),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
