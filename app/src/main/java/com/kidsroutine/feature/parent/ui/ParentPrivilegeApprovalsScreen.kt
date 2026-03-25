package com.kidsroutine.feature.parent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsroutine.core.model.UserModel

private val GradientStart = Color(0xFFFF6B35)
private val GradientEnd   = Color(0xFFFFD93D)
private val BgLight       = Color(0xFFFFFBF0)
private val TextDark      = Color(0xFF2D3436)
private val GreenApprove  = Color(0xFF06D6A0)
private val RedDeny       = Color(0xFFEF476F)

// ── Stub data class — replace with your real domain model ────────────────────
data class PrivilegeRequest(
    val id: String,
    val childName: String,
    val childEmoji: String = "👤",
    val privilegeTitle: String,
    val privilegeDescription: String,
    val requestedAt: String = "Just now"
)

@Composable
fun ParentPrivilegeApprovalsScreen(
    currentUser: UserModel,
    onBackClick: () -> Unit
) {
    // Stub list — swap with ViewModel state when your data layer is ready
    var requests by remember {
        mutableStateOf(
            listOf(
                PrivilegeRequest(
                    id                   = "1",
                    childName            = "Alex",
                    privilegeTitle       = "Extra Screen Time",
                    privilegeDescription = "Alex is requesting 30 extra minutes of screen time tonight.",
                    requestedAt          = "5 min ago"
                ),
                PrivilegeRequest(
                    id                   = "2",
                    childName            = "Mia",
                    privilegeTitle       = "Stay Up Late",
                    privilegeDescription = "Mia wants to stay up until 10 PM on Friday.",
                    requestedAt          = "1 hour ago"
                ),
                PrivilegeRequest(
                    id                   = "3",
                    childName            = "Alex",
                    privilegeTitle       = "Skip Chores",
                    privilegeDescription = "Alex wants to skip room-cleaning today because of a school project.",
                    requestedAt          = "2 hours ago"
                )
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        // ── Header ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint               = Color.White
                    )
                }
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Privilege Approvals",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    Text(
                        "${requests.size} pending",
                        fontSize = 13.sp,
                        color    = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // ── List ──────────────────────────────────────────────────────────
        if (requests.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("✅", fontSize = 52.sp)
                    Text(
                        "All caught up!",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextDark
                    )
                    Text(
                        "No pending privilege requests.",
                        fontSize = 14.sp,
                        color    = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(requests, key = { it.id }) { request ->
                    PrivilegeRequestCard(
                        request   = request,
                        onApprove = { requests = requests.filter { it.id != request.id } },
                        onDeny    = { requests = requests.filter { it.id != request.id } }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivilegeRequestCard(
    request: PrivilegeRequest,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Child info row ─────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape    = CircleShape,
                    color    = GradientStart.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(request.childEmoji, fontSize = 20.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        request.childName,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp,
                        color      = TextDark
                    )
                    Text(
                        request.requestedAt,
                        fontSize = 11.sp,
                        color    = Color.Gray
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Text(
                        "Pending",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = GradientStart,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Privilege details ──────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(10.dp),
                color    = Color(0xFFF9F9F9)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        request.privilegeTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp,
                        color      = TextDark
                    )
                    Text(
                        request.privilegeDescription,
                        fontSize = 13.sp,
                        color    = Color.Gray
                    )
                }
            }

            // ── Action buttons ─────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Deny
                OutlinedButton(
                    onClick  = onDeny,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedDeny),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, RedDeny)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Deny",
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Deny", fontWeight = FontWeight.SemiBold)
                }
                // Approve
                Button(
                    onClick  = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenApprove)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Approve",
                        tint               = Color.White,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Approve", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
