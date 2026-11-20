package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.AppNotification
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.GlassCard
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.ToolTopBar

@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    onMarkRead: (String) -> Unit,
    onBack: () -> Unit
) {
    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { ToolTopBar(title = "Notifications", onBack = onBack) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                if (notifications.isEmpty()) {
                    GlassCard(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                    ) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = PremiumGold)
                        Spacer(Modifier.height(8.dp))
                        Text("You're all caught up!", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 720.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications, key = { it.notificationId }) { item ->
                            GlassCard(
                                highlightGold = !item.read,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { if (!item.read) onMarkRead(item.notificationId) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                                    if (!item.read) {
                                        Text("NEW", style = MaterialTheme.typography.labelSmall, color = PremiumGold)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
