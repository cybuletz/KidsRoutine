package com.kidsroutine.feature.execution.ui.blocks

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.kidsroutine.core.model.InteractionBlock
import com.kidsroutine.feature.execution.ui.ExecutionEvent
import java.io.File

@Composable
fun PhotoCaptureBlock(block: InteractionBlock, onEvent: (ExecutionEvent) -> Unit) {
    val context = LocalContext.current
    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    // Camera capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedUri != null) {
            onEvent(ExecutionEvent.PhotoCaptured(capturedUri.toString()))
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            capturedUri = uri
            onEvent(ExecutionEvent.PhotoCaptured(uri.toString()))
        }
    }

    // Function to create temp file and launch camera
    fun launchCamera() {
        try {
            val file = File.createTempFile("photo_", ".jpg", context.cacheDir)
            photoFile = file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            capturedUri = uri
            cameraLauncher.launch(uri)
        } catch (_: Exception) {
            // If FileProvider fails, fall back to gallery
            galleryLauncher.launch("image/*")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                )
            )
            .padding(20.dp)
    ) {
        if (capturedUri != null) {
            // Photo captured state
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(visible = true, enter = scaleIn() + fadeIn()) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )
                }
                Text("Photo captured! ✨", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Great job documenting your progress!", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)

                // Retake option
                OutlinedButton(
                    onClick = {
                        capturedUri = null
                        if (hasCameraPermission) launchCamera()
                        else permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Retake 📷", color = Color.White)
                }
            }
        } else {
            // Ready to capture state
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Text("Show your progress! 📸", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Take a photo or pick from gallery", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Camera button
                    Button(
                        onClick = {
                            if (hasCameraPermission) {
                                launchCamera()
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Camera", fontWeight = FontWeight.Bold)
                    }

                    // Gallery button
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Gallery", color = Color.White)
                    }
                }
            }
        }
    }
}

