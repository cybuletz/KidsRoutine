package com.kidsroutine.feature.onboarding.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val OrangePrimary = Color(0xFFFF6B35)
private val GradientStart = Color(0xFFFF6B35)
private val GradientEnd   = Color(0xFFFFD93D)

@Composable
fun ParentOnboardingWizard(
    parentName: String,
    inviteCode: String,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    val totalSteps = 3
    val context    = LocalContext.current
    val clipboard  = LocalClipboardManager.current

    Dialog(
        onDismissRequest = { /* prevent accidental dismiss */ },
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color         = Color.White,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Step progress dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        repeat(totalSteps) { index ->
                            val isActive = index == step
                            Surface(
                                modifier = Modifier
                                    .width(if (isActive) 24.dp else 8.dp)
                                    .height(8.dp),
                                shape = CircleShape,
                                color = if (isActive) OrangePrimary else Color(0xFFDDDDDD)
                            ) {}
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // ── Step content ───────────────────────────────────────
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        },
                        label = "wizard_step"
                    ) { currentStep ->
                        when (currentStep) {
                            0 -> WizardStep1(parentName = parentName)
                            1 -> WizardStep2(inviteCode = inviteCode, onCopy = {
                                clipboard.setText(AnnotatedString(inviteCode))
                            })
                            2 -> WizardStep3()
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // ── Action buttons ─────────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        if (step > 0) {
                            OutlinedButton(
                                onClick = { step-- },
                                modifier = Modifier
                                    .weight(0.4f)
                                    .height(52.dp),
                                shape    = RoundedCornerShape(14.dp)
                            ) {
                                Text("Back")
                            }
                        }

                        Button(
                            onClick = {
                                if (step < totalSteps - 1) step++
                                else onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape  = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text(
                                text       = if (step == totalSteps - 1) "Let's Go! 🎉" else "Next →",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 16.sp,
                                color      = Color.White
                            )
                        }
                    }

                    // Skip link on step 2 only (task creation is optional)
                    if (step == 2) {
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = onDismiss) {
                            Text("Skip for now", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardStep1(parentName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("👨‍👩‍👧", fontSize = 64.sp)
        Text(
            "Welcome, ${parentName.split(" ").first()}!",
            fontSize    = 26.sp,
            fontWeight  = FontWeight.Bold,
            color       = Color(0xFF2D3436),
            textAlign   = TextAlign.Center
        )
        Text(
            "KidsRoutine helps your family build great habits together. " +
                    "Let's get you set up in two quick steps.",
            fontSize   = 15.sp,
            color      = Color.Gray,
            textAlign  = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            WizardFeatureChip("✅", "Tasks")
            WizardFeatureChip("🏆", "Challenges")
            WizardFeatureChip("⭐", "Rewards")
        }
    }
}

@Composable
private fun WizardStep2(inviteCode: String, onCopy: () -> Unit) {
    var copied by remember { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🔗", fontSize = 56.sp)
        Text(
            "Invite Your Children",
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF2D3436),
            textAlign  = TextAlign.Center
        )
        Text(
            "Share this code with your child. They enter it when they first open the app.",
            fontSize  = 14.sp,
            color     = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )
        Spacer(Modifier.height(8.dp))
        // Code display card
        Surface(
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(16.dp),
            color         = Color(0xFFFFF3E0),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Invite Code", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(
                        inviteCode,
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = OrangePrimary,
                        letterSpacing = 4.sp
                    )
                }
                IconButton(
                    onClick = {
                        onCopy()
                        copied = true
                    }
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint     = OrangePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        if (copied) {
            Text("✓ Copied to clipboard!", color = Color(0xFF2ECC71), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun WizardStep3() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("✨", fontSize = 56.sp)
        Text(
            "You're all set!",
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF2D3436),
            textAlign  = TextAlign.Center
        )
        Text(
            "Head to the Tasks tab to create your first task, or wait for your child to join and propose one.",
            fontSize  = 14.sp,
            color     = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )
        Spacer(Modifier.height(8.dp))
        // Quick tips
        listOf(
            "📋" to "Use the Tasks tab to assign daily routines",
            "🏆" to "Start a Challenge to motivate the whole family",
            "💬" to "Tap the pink chat bubble anytime to message your kids"
        ).forEach { (emoji, tip) ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 20.sp)
                Text(tip, fontSize = 13.sp, color = Color(0xFF2D3436), lineHeight = 19.sp)
            }
        }
    }
}

@Composable
private fun WizardFeatureChip(emoji: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(shape = RoundedCornerShape(12.dp), color = OrangePrimary.copy(alpha = 0.1f)) {
            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 24.sp)
            }
        }
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}
