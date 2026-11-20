package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ChatSession
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassCard
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.sharePlainText
import com.example.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    recentChats: List<ChatSession>,
    userCredits: Long,
    defaultLanguage: String,
    initialPrompt: String? = null,
    initialChatId: String? = null,
    onOpenCredits: () -> Unit
) {
    val messages by chatViewModel.messages.collectAsState()
    val streamingText by chatViewModel.streamingText.collectAsState()
    val isGenerating by chatViewModel.isGenerating.collectAsState()
    val errorMessage by chatViewModel.errorMessage.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var chatLanguage by remember(defaultLanguage) { mutableStateOf(defaultLanguage) }
    var showHistorySheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(initialChatId) {
        if (!initialChatId.isNullOrBlank()) {
            chatViewModel.selectChat(initialChatId)
        }
    }

    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            chatViewModel.startNewChat()
            chatViewModel.sendMessage(initialPrompt, chatLanguage)
        }
    }

    LaunchedEffect(messages.size, streamingText) {
        if (messages.isNotEmpty() || streamingText.isNotEmpty()) {
            listState.animateScrollToItem((messages.size + if (streamingText.isNotEmpty()) 1 else 0).coerceAtLeast(1) - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .padding(horizontal = 12.dp)
        ) {
            // Top controls bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = chatLanguage == "en",
                        onClick = { chatLanguage = "en" },
                        label = { Text("English") }
                    )
                    FilterChip(
                        selected = chatLanguage == "hi",
                        onClick = { chatLanguage = "hi" },
                        label = { Text("हिन्दी") }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("chat_history_button")
                    ) {
                        Icon(Icons.Default.History, contentDescription = "Conversation history")
                    }
                    IconButton(
                        onClick = { chatViewModel.startNewChat() },
                        modifier = Modifier.testTag("new_chat_button")
                    ) {
                        Icon(Icons.Default.AddComment, contentDescription = "New Chat", tint = PremiumGold)
                    }
                }
            }

            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty() && streamingText.isEmpty()) {
                    item {
                        GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                            Text("Namaste! Ask anything in Hindi, English, or Hinglish.", style = MaterialTheme.typography.titleMedium, color = PremiumGold)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Try: \"Explain UPSC prelims strategy\", \"Write a Python script\", or \"Hindi me ek motivational speech likho\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(messages, key = { it.messageId }) { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        GlassCard(
                            highlightGold = isUser,
                            modifier = Modifier.widthIn(max = 560.dp),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Text(
                                text = if (isUser) "You" else "AI Assistant India",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUser) PremiumGold else MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(text = msg.content, style = MaterialTheme.typography.bodyLarge)
                            if (!isUser) {
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { clipboard.setText(AnnotatedString(msg.content)) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { sharePlainText(context, msg.content) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                    }
                                    if (msg == messages.lastOrNull() && !isGenerating) {
                                        IconButton(
                                            onClick = { chatViewModel.regenerateLastAnswer(chatLanguage) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (streamingText.isNotEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("AI Assistant India • Streaming", style = MaterialTheme.typography.labelSmall, color = PremiumGold)
                            Spacer(Modifier.height(4.dp))
                            Text(streamingText, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(errorMessage!!, color = ErrorCoral, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (errorMessage!!.contains("credit", ignoreCase = true)) {
                            TextButton(onClick = onOpenCredits) { Text("Get Credits") }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Message AI ($userCredits ★)...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input")
                )

                if (isGenerating) {
                    Surface(
                        onClick = { chatViewModel.stopGeneration() },
                        shape = CircleShape,
                        color = ErrorCoral,
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("chat_stop_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop generation")
                        }
                    }
                } else {
                    Surface(
                        onClick = {
                            val prompt = inputText
                            inputText = ""
                            chatViewModel.sendMessage(prompt, chatLanguage)
                        },
                        shape = CircleShape,
                        color = PremiumGold,
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("chat_send_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message", tint = DeepPurple)
                        }
                    }
                }
            }
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(onDismissRequest = { showHistorySheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Conversation History", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                if (recentChats.isEmpty()) {
                    Text("No saved conversations yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    recentChats.forEach { session ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    chatViewModel.selectChat(session.chatId)
                                    showHistorySheet = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(session.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { chatViewModel.deleteChat(session.chatId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete chat", tint = ErrorCoral)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
