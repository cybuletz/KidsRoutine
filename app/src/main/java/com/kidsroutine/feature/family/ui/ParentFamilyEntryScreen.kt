package com.kidsroutine.feature.family.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsroutine.core.model.UserModel

@Composable
fun ParentFamilyEntryScreen(
    currentUser: UserModel,
    onFamilySet: () -> Unit
) {
    var showCreate by remember { mutableStateOf(false) }
    var showJoin   by remember { mutableStateOf(false) }

    if (showCreate) {
        // FamilySetupScreen has NO onBackClick param — back is handled by showCreate flag
        // onFamilyCreated receives a FamilyModel, we ignore it and just call onFamilySet
        FamilySetupScreen(
            currentUser     = currentUser,
            onFamilyCreated = { _ -> onFamilySet() }
        )
        return
    }

    if (showJoin) {
        // JoinFamilyScreen uses onJoinSuccess (receives familyId String), not onFamilyJoined
        JoinFamilyScreen(
            currentUser  = currentUser,
            onJoinSuccess = { _ -> onFamilySet() },
            onBackClick  = { showJoin = false }
        )
        return
    }

    // ── Entry screen — choose Create or Join ──────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFF6B35), Color(0xFFFFD93D))))
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Welcome, ${currentUser.displayName.split(" ").first()}! 👋",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "To get started, create a new family\nor join an existing one.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))

        Button(
            onClick  = { showCreate = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text(
                "🏠  Create a New Family",
                fontWeight = FontWeight.Bold,
                color      = Color(0xFFFF6B35),
                fontSize   = 16.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick  = { showJoin = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(16.dp),
            border   = BorderStroke(2.dp, Color.White)
        ) {
            Text(
                "🔗  Join an Existing Family",
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                fontSize   = 16.sp
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "A family needs at least 2 parents to be complete.",
            fontSize = 11.sp,
            color    = Color.White.copy(alpha = 0.65f)
        )
    }
}