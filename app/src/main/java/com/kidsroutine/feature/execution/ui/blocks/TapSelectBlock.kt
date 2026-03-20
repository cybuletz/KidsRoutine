package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.animation.core.spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
fun TapSelectBlock(block: InteractionBlock, onEvent: (ExecutionEvent) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    val options       = block.config["options"] as? List<String> ?: emptyList()
    val correctAnswer = block.config["correctAnswer"] as? String ?: ""
    var selected by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { option ->
            val isSelected = selected == option
            val isCorrect  = selected != null && option == correctAnswer
            val isWrong    = isSelected && option != correctAnswer
            val bgColor by animateColorAsState(
                targetValue = when {
                    isCorrect -> Color(0xFF06D6A0)
                    isWrong   -> Color(0xFFEF476F)
                    isSelected -> Color(0xFFFFD93D)
                    else      -> Color(0xFFF5F5F5)
                },
                animationSpec = spring(),
                label = "option_color"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .clickable(enabled = selected == null) {
                        selected = option
                        val correct = option == correctAnswer
                        onEvent(ExecutionEvent.BlockAnswered(block.blockId, correct))
                    }
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(option, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else Color.DarkGray)
            }
        }
    }
}
