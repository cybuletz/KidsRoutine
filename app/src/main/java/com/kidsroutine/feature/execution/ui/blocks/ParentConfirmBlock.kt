package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun ParentConfirmBlock(block: InteractionBlock, onEvent: (ExecutionEvent) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFF3CD))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.HourglassTop, null, tint = Color(0xFFFF9F1C), modifier = Modifier.size(40.dp))
            Text("Waiting for Parent ⏳", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                "Ask your parent to confirm this task is done!",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
            Button(
                onClick = { onEvent(ExecutionEvent.BlockAnswered(block.blockId, "pending_parent")) },
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F1C)),
                shape   = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Submit for Review", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}
