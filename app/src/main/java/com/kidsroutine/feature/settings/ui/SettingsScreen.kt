package com.kidsroutine.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsroutine.core.model.UserModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalClipboard
import com.kidsroutine.feature.avatar.data.AvatarShopSeeder
import kotlinx.coroutines.launch
import android.content.ClipData
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.ClipEntry


private val OrangePrimary = Color(0xFFFF6B35)
private val BgLight = Color(0xFFFFFBF0)
private val TextDark = Color(0xFF2D3436)
private val GradientStart = Color(0xFFFF6B35)
private val GradientEnd = Color(0xFFFFD93D)

@Composable
fun SettingsScreen(
    currentUser: UserModel,
    familyInviteCode: String,
    onSignOutClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    onContentPacksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var codeCopied by remember { mutableStateOf(false) }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

// Display Name row:
    SettingsRow(
        icon = Icons.Default.Person,
        iconColor = Color(0xFF4A90E2),
        title = "Display Name",
        subtitle = currentUser.displayName,
        onClick = { showEditNameDialog = true }
    )

// Change Password row:
    SettingsRow(
        icon = Icons.Default.Lock,
        iconColor = Color(0xFF2ECC71),
        title = "Change Password",
        subtitle = "Update your login password",
        onClick = { showPasswordDialog = true }
    )

// Privacy Policy row:
    SettingsRow(
        icon = Icons.Default.Security,
        iconColor = Color(0xFF3498DB),
        title = "Privacy Policy",
        subtitle = "How we protect your family's data",
        onClick = {
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("https://kidsroutine.app/privacy")
            )
            context.startActivity(intent)
        }
    )

    // Add dialogs at the end of the composable (before final Spacer):
    if (showEditNameDialog) {
        var nameInput by remember { mutableStateOf(currentUser.displayName) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Display Name") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nameInput.isNotBlank()) {
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users").document(currentUser.userId)
                            .update("displayName", nameInput.trim())
                        showEditNameDialog = false
                    }
                }) { Text("Save", color = OrangePrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Reset Password") },
            text = { Text("A password reset email will be sent to ${currentUser.email}") },
            confirmButton = {
                TextButton(onClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance()
                        .sendPasswordResetEmail(currentUser.email)
                    showPasswordDialog = false
                }) { Text("Send Email", color = OrangePrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    "Settings",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    currentUser.displayName,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Account ───────────────────────────────────────────────────────
        SettingsSection(title = "Account") {
            SettingsRow(
                icon = Icons.Default.Person,
                iconColor = Color(0xFF4A90E2),
                title = "Display Name",
                subtitle = currentUser.displayName,
                onClick = { /* TODO: edit name */ }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Default.Email,
                iconColor = Color(0xFF9B59B6),
                title = "Email",
                subtitle = currentUser.email.ifEmpty { "Not set" },
                onClick = { }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Default.Lock,
                iconColor = Color(0xFF2ECC71),
                title = "Change Password",
                subtitle = "Update your login password",
                onClick = { /* TODO */ }
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Family ────────────────────────────────────────────────────────
        SettingsSection(title = "Family") {
            SettingsRow(
                icon = Icons.Default.People,
                iconColor = OrangePrimary,
                title = "Invite Code",
                subtitle = familyInviteCode.ifEmpty { "Loading..." },
                trailingContent = {
                    if (familyInviteCode.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                clipboardManager.setClip(
                                    ClipEntry(
                                        ClipData.newPlainText("Invite Code", familyInviteCode)
                                    )
                                )
                                codeCopied = true
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = OrangePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                onClick = { }
            )
            if (codeCopied) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    codeCopied = false
                }
                Text(
                    "✓ Copied!",
                    color = Color(0xFF2ECC71),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 56.dp, bottom = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Notifications ─────────────────────────────────────────────────
        SettingsSection(title = "Notifications") {
            var taskReminders by remember { mutableStateOf(true) }
            var achievements by remember { mutableStateOf(true) }
            var familyMessages by remember { mutableStateOf(true) }

            SettingsToggleRow(
                icon = Icons.Default.Notifications,
                iconColor = Color(0xFFFF6B35),
                title = "Task Reminders",
                checked = taskReminders,
                onCheckedChange = { taskReminders = it }
            )
            SettingsDivider()
            SettingsToggleRow(
                icon = Icons.Default.EmojiEvents,
                iconColor = Color(0xFFFFD700),
                title = "Achievement Alerts",
                checked = achievements,
                onCheckedChange = { achievements = it }
            )
            SettingsDivider()
            SettingsToggleRow(
                icon = Icons.AutoMirrored.Filled.Message,
                iconColor = Color(0xFFEC407A),
                title = "Family Messages",
                checked = familyMessages,
                onCheckedChange = { familyMessages = it }
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Subscription ──────────────────────────────────────────────────
        SettingsSection(title = "Subscription") {
            SettingsRow(
                icon = Icons.Default.Star,
                iconColor = Color(0xFFFFD700),
                title = "Upgrade to PRO",
                subtitle = "Unlock story arcs, unlimited AI & more",
                onClick = onUpgradeClick
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Default.ShoppingCart,
                iconColor = Color(0xFF667EEA),
                title = "Content Packs",
                subtitle = "Browse & unlock themed task packs",
                onClick = onContentPacksClick
            )
        }

        Spacer(Modifier.height(16.dp))

        val scope = rememberCoroutineScope()
        if (currentUser.isAdmin) {
            Button(onClick = { scope.launch { AvatarShopSeeder.seed() } }) {
                Text("Seed Avatar Shop (run once)")
            }
        }

        // ── About ─────────────────────────────────────────────────────────
        SettingsSection(title = "About") {
            SettingsRow(
                icon = Icons.Default.Info,
                iconColor = Color(0xFF95A5A6),
                title = "Version",
                subtitle = "1.0.0",
                onClick = { }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Default.Security,
                iconColor = Color(0xFF3498DB),
                title = "Privacy Policy",
                subtitle = "How we protect your family's data",
                onClick = { /* TODO: open URL */ }
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Sign Out ──────────────────────────────────────────────────────
        Button(
            onClick = onSignOutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(40.dp))
    }
}

// ── Reusable components ───────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
            letterSpacing = 0.8.sp
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = iconColor.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextDark)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = iconColor.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
        }
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = OrangePrimary)
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 66.dp, end = 16.dp),
        color = Color(0xFFF0F0F0),
        thickness = 1.dp
    )
}
