package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
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
fun CheckboxBlock(block: InteractionBlock, onEvent: (ExecutionEvent) -> Unit) {
    val label = block.config["label"] as? String ?: "I did it!"
    var checked by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(
        targetValue   = if (checked) Color(0xFF06D6A0) else Color(0xFFF0F0F0),
        animationSpec = spring(),
        label         = "checkbox_color"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable {
                checked = true
                onEvent(ExecutionEvent.BlockAnswered(block.blockId, true))
            }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector  = Icons.Default.Check,
                contentDescription = null,
                tint   = if (checked) Color.White else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text       = label,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = if (checked) Color.White else Color.DarkGray
            )
        }
    }
}
