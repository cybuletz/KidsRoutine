package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kidsroutine.core.model.InteractionBlock
import com.kidsroutine.feature.execution.ui.ExecutionEvent

@Composable
fun TextInputBlock(block: InteractionBlock, onEvent: (ExecutionEvent) -> Unit) {
    val hint    = block.config["hint"] as? String ?: "Write here…"
    val maxLen  = (block.config["maxLength"] as? Int) ?: 100
    var text by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value         = text,
            onValueChange = { if (it.length <= maxLen) text = it },
            placeholder   = { Text(hint, color = Color.Gray) },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(16.dp),
            singleLine    = false,
            maxLines      = 3
        )
        Button(
            onClick  = { onEvent(ExecutionEvent.BlockAnswered(block.blockId, text)) },
            enabled  = text.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE)),
            shape    = RoundedCornerShape(14.dp)
        ) {
            Text("Done ✓", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
