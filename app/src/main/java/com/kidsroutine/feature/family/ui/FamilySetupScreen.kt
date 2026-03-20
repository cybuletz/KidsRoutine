package com.kidsroutine.feature.family.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsroutine.core.model.FamilyModel
import com.kidsroutine.core.model.UserModel

private val GradientStart = Color(0xFFFF6B35)
private val GradientEnd = Color(0xFFFFD93D)
private val BgLight = Color(0xFFFFFBF0)
private val TextDark = Color(0xFF2D3436)

@Composable
fun FamilySetupScreen(
    currentUser: UserModel,
    onFamilyCreated: (FamilyModel) -> Unit,
    viewModel: FamilySetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.success) {
        if (uiState.success && uiState.family != null) {
            onFamilyCreated(uiState.family!!)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Text(
                text = "👨‍👩‍👧‍👦",
                fontSize = 72.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Create Your Family",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 28.sp
            )

            Text(
                text = "Set up your family routine management",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Welcome message
                    Text(
                        text = "Welcome, ${currentUser.displayName}!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Let's set up your family. Choose a name that represents your household.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Family Name Input
                    OutlinedTextField(
                        value = uiState.familyName,
                        onValueChange = { viewModel.updateFamilyName(it) },
                        label = { Text("Family Name") },
                        placeholder = { Text("e.g., The Smiths") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = uiState.error != null,
                        supportingText = if (uiState.error != null) {
                            { Text(uiState.error!!, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GradientStart,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    // Create Family Button
                    Button(
                        onClick = {
                            viewModel.createFamily(currentUser.userId, uiState.familyName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GradientStart
                        ),
                        enabled = !uiState.isLoading && uiState.familyName.isNotEmpty()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Create Family",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Info Cards
                    FamilySetupInfoCard(
                        emoji = "📱",
                        title = "Invite Children",
                        description = "Share a code with your kids to invite them to the family"
                    )

                    Spacer(Modifier.height(12.dp))

                    FamilySetupInfoCard(
                        emoji = "⭐",
                        title = "Track Progress",
                        description = "Monitor XP, streaks, and achievements for each child"
                    )

                    Spacer(Modifier.height(12.dp))

                    FamilySetupInfoCard(
                        emoji = "🎯",
                        title = "Create Tasks",
                        description = "Set up routines and tasks tailored to your family"
                    )
                }
            }
        }
    }
}

@Composable
private fun FamilySetupInfoCard(
    emoji: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp,
                modifier = Modifier.width(48.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}