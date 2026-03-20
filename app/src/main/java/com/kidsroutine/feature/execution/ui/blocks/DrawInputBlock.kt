package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kidsroutine.core.model.InteractionBlock
import com.kidsroutine.feature.execution.ui.ExecutionEvent

@Composable
fun DrawInputBlock(block: InteractionBlock, onEvent: (ExecutionEvent) -> Unit) {
    val paths   = remember { mutableStateListOf<List<Offset>>() }
    var current = remember { mutableStateListOf<Offset>() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF9F9F9))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> current = mutableStateListOf(offset) },
                        onDrag      = { change, _ -> current.add(change.position) },
                        onDragEnd   = { paths.add(current.toList()); current = mutableStateListOf() }
                    )
                }
        ) {
            (paths + listOf(current)).forEach { pathPoints ->
                if (pathPoints.size > 1) {
                    val path = Path().apply {
                        moveTo(pathPoints.first().x, pathPoints.first().y)
                        pathPoints.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(
                        path  = path,
                        color = Color(0xFF4361EE),
                        style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick  = { paths.clear(); current.clear() },
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(14.dp)
            ) { Text("Clear") }
            Button(
                onClick  = { onEvent(ExecutionEvent.BlockAnswered(block.blockId, "drawing_done")) },
                enabled  = paths.isNotEmpty(),
                modifier = Modifier.weight(1f),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE)),
                shape    = RoundedCornerShape(14.dp)
            ) { Text("Done ✓", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}
