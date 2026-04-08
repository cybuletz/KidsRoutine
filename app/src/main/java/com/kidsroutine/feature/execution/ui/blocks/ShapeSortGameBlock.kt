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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsroutine.core.common.util.SoundManager
import com.kidsroutine.core.model.AgeGroup
import kotlinx.coroutines.delay

/**
 * SHAPE SORT GAME: Sort shapes into correct categories.
 * Features:
 * - Tap shape then tap category to sort
 * - Age-adaptive: 2D shapes → multi-property → 3D → geometric proofs
 * - Animated placement with bounce
 * - Progress tracking with colored feedback
 */
@Composable
fun ShapeSortGameBlock(
    ageGroup: AgeGroup = AgeGroup.EXPLORER,
    onSuccess: () -> Unit
) {
    data class SortItem(val label: String, val category: String)

    val items: List<SortItem> = remember {
        when (ageGroup) {
            AgeGroup.SPROUT -> listOf(
                SortItem("🔴", "Circle"),
                SortItem("🟦", "Square"),
                SortItem("🔺", "Triangle"),
                SortItem("⚪", "Circle"),
                SortItem("🟧", "Square"),
                SortItem("📐", "Triangle")
            )
            AgeGroup.EXPLORER -> listOf(
                SortItem("🔴 Circle", "Red Shapes"),
                SortItem("🟦 Square", "Blue Shapes"),
                SortItem("🔵 Circle", "Blue Shapes"),
                SortItem("🟥 Square", "Red Shapes"),
                SortItem("🔺 Triangle", "Red Shapes"),
                SortItem("🔷 Diamond", "Blue Shapes"),
                SortItem("🟢 Circle", "Green Shapes")
            )
            AgeGroup.TRAILBLAZER -> listOf(
                SortItem("🧊 Cube", "Has Flat Faces"),
                SortItem("⚽ Sphere", "Curved Surface"),
                SortItem("🥫 Cylinder", "Curved Surface"),
                SortItem("🔺 Cone", "Curved Surface"),
                SortItem("📦 Cuboid", "Has Flat Faces"),
                SortItem("🔶 Prism", "Has Flat Faces"),
                SortItem("⚾ Ball", "Curved Surface")
            )
            AgeGroup.LEGEND -> listOf(
                SortItem("∥ Lines AB & CD", "Parallel"),
                SortItem("⊥ Lines EF & GH", "Perpendicular"),
                SortItem("△ABC ≅ △DEF", "Congruent"),
                SortItem("△PQR ~ △STU", "Similar"),
                SortItem("∥ Lines MN & OP", "Parallel"),
                SortItem("⊥ Lines JK & LM", "Perpendicular"),
                SortItem("□ABCD ≅ □EFGH", "Congruent")
            )
        }
    }

    val categories = remember { items.map { it.category }.distinct() }
    var unsorted by remember { mutableStateOf(items.shuffled()) }
    var sorted by remember { mutableStateOf<Map<String, List<SortItem>>>(categories.associateWith { emptyList() }) }
    var selectedItem by remember { mutableStateOf<SortItem?>(null) }
    var lastResult by remember { mutableStateOf<Boolean?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    // Win check
    LaunchedEffect(unsorted) {
        if (unsorted.isEmpty() && !showSuccess) {
            showSuccess = true
            SoundManager.playSuccess()
            delay(1500)
            onSuccess()
        }
    }

    // Clear result feedback
    LaunchedEffect(lastResult) {
        if (lastResult != null) {
            delay(600)
            lastResult = null
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            "🔷 Shape Sort",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF6C5CE7)
        )

        // Progress
        Box(
            Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFF6C5CE7).copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                "Sorted: ${items.size - unsorted.size} / ${items.size}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6C5CE7)
            )
        }

        // Unsorted items at top
        if (unsorted.isNotEmpty()) {
            Text(
                if (selectedItem != null) "Now tap a category below ⬇"
                else "Tap a shape to select it",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0F0F0))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val columns = if (ageGroup == AgeGroup.SPROUT) 3 else 2
                val rows = (unsorted.size + columns - 1) / columns
                for (row in 0 until rows) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 0 until columns) {
                            val idx = row * columns + col
                            if (idx < unsorted.size) {
                                val item = unsorted[idx]
                                ShapeChip(
                                    label = item.label,
                                    isSelected = selectedItem == item,
                                    onClick = {
                                        SoundManager.playTap()
                                        selectedItem = if (selectedItem == item) null else item
                                    }
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Feedback
        AnimatedVisibility(
            visible = lastResult != null,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(300))
        ) {
            Text(
                if (lastResult == true) "✅ Correct!" else "❌ Try again!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (lastResult == true) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }

        // Category bins
        Text(
            "Categories:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        categories.forEach { category ->
            CategoryBin(
                name = category,
                items = sorted[category] ?: emptyList(),
                isHighlighted = selectedItem != null,
                onClick = {
                    val current = selectedItem ?: return@CategoryBin
                    if (current.category == category) {
                        SoundManager.playSuccess()
                        sorted = sorted.toMutableMap().also { map ->
                            map[category] = (map[category] ?: emptyList()) + current
                        }
                        unsorted = unsorted - current
                        lastResult = true
                    } else {
                        SoundManager.playError()
                        lastResult = false
                    }
                    selectedItem = null
                }
            )
        }

        // Success banner
        AnimatedVisibility(
            visible = showSuccess,
            enter = scaleIn(animationSpec = tween(500)) + fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF90EE90))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "🎉 All Sorted!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text("Every shape in the right place!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ShapeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "shape_chip_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF6C5CE7) else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color(0xFF333333),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CategoryBin(
    name: String,
    items: List<Any>,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    val borderAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0.3f,
        animationSpec = tween(300),
        label = "bin_highlight"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF6C5CE7).copy(alpha = 0.05f * borderAlpha * 3))
            .clickable(enabled = isHighlighted) { onClick() }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    name,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6C5CE7),
                    fontSize = 16.sp
                )
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFF6C5CE7).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "${items.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6C5CE7)
                    )
                }
            }
            if (items.isNotEmpty()) {
                Text(
                    "✓ ${items.size} sorted here",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}
