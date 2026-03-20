package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
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
import com.kidsroutine.core.model.InteractionBlock
import com.kidsroutine.feature.execution.ui.ExecutionEvent

@Composable
fun DualConfirmBlock(block: InteractionBlock, onEvent: (ExecutionEvent) -> Unit) {
    var childConfirmed  by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF9B5DE5).copy(alpha = 0.1f))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.People, null, tint = Color(0xFF9B5DE5), modifier = Modifier.size(40.dp))
            Text("Co-op Task! 🤝", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF9B5DE5))
            Text("Both you and your parent need to confirm.", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ConfirmPill("You ✓", childConfirmed, Color(0xFF9B5DE5)) { childConfirmed = true }
                ConfirmPill("Parent ⏳", false, Color.Gray) { /* Parent confirms in parent flow */ }
            }

            if (childConfirmed) {
                Button(
                    onClick = { onEvent(ExecutionEvent.BlockAnswered(block.blockId, "child_confirmed")) },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
                    shape   = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Submit — Waiting for Parent", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun ConfirmPill(label: String, confirmed: Boolean, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors  = ButtonDefaults.buttonColors(
            containerColor = if (confirmed) color else Color.LightGray
        ),
        shape   = RoundedCornerShape(50.dp)
    ) { Text(label, color = Color.White, fontWeight = FontWeight.SemiBold) }
}
