package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.AppLanguage
import com.example.data.LocalAppStrings
import com.example.model.UserProfile
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassCard
import com.example.ui.theme.PremiumGold

@Composable
fun ProfileSettingsScreen(
    userProfile: UserProfile?,
    isDarkMode: Boolean,
    languageCode: String,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onUpdateName: (String) -> Unit,
    onOpenSubscriptions: () -> Unit,
    onOpenChatHistory: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val strings = LocalAppStrings.current
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var infoDialogTitle by remember { mutableStateOf<String?>(null) }
    var infoDialogBody by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Profile & Settings", style = MaterialTheme.typography.headlineMedium)
            }

            // User Card
            item {
                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(2.dp, PremiumGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!userProfile?.photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = userProfile?.photoUrl,
                                    contentDescription = "Profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = (userProfile?.name?.firstOrNull() ?: 'A').uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = PremiumGold
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(userProfile?.name ?: "User", style = MaterialTheme.typography.titleLarge)
                            if (!userProfile?.email.isNullOrBlank()) {
                                Text(
                                    text = userProfile!!.email!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!userProfile?.phone.isNullOrBlank()) {
                                Text(
                                    text = userProfile!!.phone!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${userProfile?.credits ?: 0} Credits • ${(userProfile?.membership ?: "free").uppercase()}",
                                style = MaterialTheme.typography.labelLarge,
                                color = PremiumGold
                            )
                        }
                        IconButton(onClick = { showEditNameDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit name")
                        }
                    }
                }
            }

            // Preferences Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = PremiumGold)
                            Spacer(Modifier.width(10.dp))
                            Text(strings.darkMode, style = MaterialTheme.typography.titleMedium)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onToggleDarkMode,
                            modifier = Modifier.testTag("profile_dark_mode_switch")
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = PremiumGold)
                            Spacer(Modifier.width(10.dp))
                            Text(strings.notifications, style = MaterialTheme.typography.titleMedium)
                        }
                        Switch(
                            checked = userProfile?.notificationsEnabled ?: true,
                            onCheckedChange = onToggleNotifications
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = PremiumGold)
                        Spacer(Modifier.width(10.dp))
                        Text("${strings.languageLabel} (EN / हिन्दी + Indian)", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(AppLanguage.ENGLISH, AppLanguage.HINDI).forEach { lang ->
                            FilterChip(
                                selected = languageCode == lang.code,
                                onClick = { onSelectLanguage(lang.code) },
                                label = { Text(lang.nativeName) }
                            )
                        }
                    }
                }
            }

            // Quick Actions
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onOpenSubscriptions,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Membership & Plans")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onOpenChatHistory,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("View Chat History")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            infoDialogTitle = "Privacy Policy"
                            infoDialogBody = "AI Assistant India stores your chats, tasks, and credit ledger privately in your authenticated Firebase account. No passwords or OTPs are stored in plain text. Images and PDFs are processed securely for your active query."
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Policy, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Privacy Policy")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            infoDialogTitle = "Terms & Help Support"
                            infoDialogBody = "AI Assistant India uses Gemini AI via Firebase AI Logic. Educational and medical-prep outputs should always be cross-verified. Support: biharkrantidal76@gmail.com"
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Help, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Terms & Help Support")
                    }
                }
            }

            // Account Security
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_logout_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.logout)
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorCoral),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_delete_account_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.deleteAccount)
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showEditNameDialog) {
        var tempName by remember { mutableStateOf(userProfile?.name.orEmpty()) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Update Name") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it.take(80) },
                    label = { Text("Full Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempName.isNotBlank()) onUpdateName(tempName)
                    showEditNameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account Permanently?") },
            text = { Text("This deletes your Firebase profile and signs you out permanently.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteAccount()
                }) { Text("Delete", color = ErrorCoral) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (infoDialogTitle != null) {
        AlertDialog(
            onDismissRequest = { infoDialogTitle = null },
            title = { Text(infoDialogTitle!!) },
            text = { Text(infoDialogBody) },
            confirmButton = {
                TextButton(onClick = { infoDialogTitle = null }) { Text("Close") }
            }
        )
    }
}
