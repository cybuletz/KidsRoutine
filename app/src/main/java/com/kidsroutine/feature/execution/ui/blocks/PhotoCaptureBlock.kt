package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun PhotoCaptureBlock(block: InteractionBlock, onEvent: (ExecutionEvent) -> Unit) {
    // Full CameraX integration is Phase 2. This scaffold triggers the flow.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A1A2E))
            .clickable {
                // TODO Phase 2: launch CameraX capture, on result call:
                // onEvent(ExecutionEvent.PhotoCaptured(uri.toString()))
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(48.dp))
            Text("Tap to take a photo 📸", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Show your progress!", color = Color.White.copy(0.7f), fontSize = 13.sp)
        }
    }
}
