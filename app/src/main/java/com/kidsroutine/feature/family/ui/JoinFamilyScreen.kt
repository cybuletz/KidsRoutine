package com.kidsroutine.feature.family.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.Role
import com.kidsroutine.core.model.UserModel

private val GradientStart = Color(0xFF667EEA)
private val GradientEnd   = Color(0xFF764BA2)

@Composable
fun JoinFamilyScreen(
    currentUser: UserModel,
    onJoinSuccess: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: JoinFamilyViewModel = hiltViewModel()
) {
    val uiState    by viewModel.uiState.collectAsState()
    val isParent   = currentUser.role == Role.PARENT

    var inviteCode by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            Log.d("JoinFamilyScreen", "Join successful, calling onJoinSuccess with familyId")
            onJoinSuccess(uiState.family?.familyId ?: "")
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) localError = uiState.error!!
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = if (isParent) "👨‍👩‍👧‍👦" else "👨‍👩‍👧",
                fontSize = 60.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = if (isParent) "Join Your Partner's Family" else "Join Your Family",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = if (isParent)
                    "Ask the other parent for their invite code"
                else
                    "Enter the invite code to join",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement   = Arrangement.spacedBy(16.dp),
                    horizontalAlignment   = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = "Invite Code",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF2D3436)
                    )

                    OutlinedTextField(
                        value         = inviteCode,
                        onValueChange = { newValue ->
                            inviteCode = newValue.take(6).uppercase()
                            localError = ""
                        },
                        placeholder    = { Text("e.g., ABC123") },
                        modifier       = Modifier.fillMaxWidth().height(56.dp),
                        shape          = RoundedCornerShape(12.dp),
                        textStyle      = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        singleLine = true,
                        enabled    = !uiState.isLoading
                    )

                    if (localError.isNotEmpty()) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFEBEE)) {
                            Text(
                                text  = localError,
                                color = Color(0xFFC62828),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            when {
                                inviteCode.isEmpty()    -> localError = "Please enter the invite code"
                                inviteCode.length < 6   -> localError = "Invite code must be 6 characters"
                                else -> {
                                    Log.d("JoinFamilyScreen", "Joining with code: $inviteCode, userId: ${currentUser.userId}")
                                    viewModel.joinFamily(currentUser.userId, inviteCode)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = GradientStart),
                        enabled  = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                color       = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (isParent) "Join Family" else "Join Family",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Hint shown only to parents
                    if (isParent) {
                        Text(
                            text  = "💡 Find the code in Settings → Family Invite Code",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}