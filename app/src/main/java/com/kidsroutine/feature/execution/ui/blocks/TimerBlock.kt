package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsroutine.core.model.InteractionBlock
import com.kidsroutine.feature.execution.ui.ExecutionEvent
import kotlinx.coroutines.delay

@Composable
fun TimerBlock(block: InteractionBlock, onEvent: (ExecutionEvent) -> Unit) {
    val totalSec = (block.config["durationSec"] as? Int) ?: 30
    var secondsLeft by remember { mutableIntStateOf(totalSec) }
    var started by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue   = secondsLeft.toFloat() / totalSec.toFloat(),
        animationSpec = tween(500),
        label         = "timer_arc"
    )
    val arcColor = when {
        progress > 0.5f -> Color(0xFF06D6A0)
        progress > 0.25f -> Color(0xFFFFD93D)
        else             -> Color(0xFFEF476F)
    }

    LaunchedEffect(started) {
        if (!started) return@LaunchedEffect
        onEvent(ExecutionEvent.TimerStarted)
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
            onEvent(ExecutionEvent.TimerTick(secondsLeft))
        }
        onEvent(ExecutionEvent.TimerFinished)
        onEvent(ExecutionEvent.BlockAnswered(block.blockId, true))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(160.dp)) {
                val stroke = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                drawArc(color = arcColor.copy(alpha = 0.15f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
                drawArc(color = arcColor, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, style = stroke)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = "$secondsLeft",
                    fontSize   = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = arcColor
                )
                Text("seconds", color = Color.Gray, fontSize = 14.sp)
            }
        }

        if (!started) {
            Button(
                onClick = { started = true },
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE)),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape   = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text("Start Timer ▶", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
