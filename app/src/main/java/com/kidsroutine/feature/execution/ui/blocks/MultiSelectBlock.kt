package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsroutine.core.model.InteractionBlock
import com.kidsroutine.feature.execution.ui.ExecutionEvent

@Composable
fun MultiSelectBlock(block: InteractionBlock, onEvent: (ExecutionEvent) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    val options  = block.config["options"] as? List<String> ?: emptyList()
    val minSelect = (block.config["minSelect"] as? Int) ?: 1
    val selected  = remember { mutableStateListOf<String>() }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { option ->
            val isSelected = option in selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) Color(0xFF4361EE).copy(0.15f) else Color(0xFFF5F5F5))
                    .clickable {
                        if (isSelected) selected.remove(option) else selected.add(option)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(option, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                if (isSelected) Icon(Icons.Default.Check, null, tint = Color(0xFF4361EE))
            }
        }
        if (selected.size >= minSelect) {
            Button(
                onClick = { onEvent(ExecutionEvent.BlockAnswered(block.blockId, selected.toList())) },
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
            ) { Text("Confirm", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}
