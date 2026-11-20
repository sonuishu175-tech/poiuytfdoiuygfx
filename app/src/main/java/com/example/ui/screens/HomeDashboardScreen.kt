package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.AdMobNativeCard
import com.example.data.LocalAppStrings
import com.example.data.VoiceHelper
import com.example.model.AiToolType
import com.example.model.ChatSession
import com.example.model.UserProfile
import com.example.ui.theme.CreditBadgePill
import com.example.ui.theme.DarkPurple
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.ElectricIris
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GoldGlowButton
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.WarmGold

fun iconForTool(tool: AiToolType): ImageVector = when (tool) {
    AiToolType.AI_CHAT -> Icons.AutoMirrored.Filled.Chat
    AiToolType.IMAGE_ANALYZER -> Icons.Default.ImageSearch
    AiToolType.VOICE_ASSISTANT -> Icons.Default.GraphicEq
    AiToolType.TRANSLATOR -> Icons.Default.Translate
    AiToolType.CONTENT_WRITER -> Icons.Default.Create
    AiToolType.HOMEWORK_HELPER -> Icons.Default.School
    AiToolType.EMAIL_WRITER -> Icons.Default.Email
    AiToolType.STORY_GENERATOR -> Icons.Default.MenuBook
    AiToolType.CODING_ASSISTANT -> Icons.Default.Code
    AiToolType.RESUME_MAKER -> Icons.Default.Description
    AiToolType.PDF_ASSISTANT -> Icons.Default.PictureAsPdf
    AiToolType.YOUTUBE_CREATOR -> Icons.Default.PlayCircle
    AiToolType.BUSINESS_AI -> Icons.Default.BusinessCenter
    AiToolType.TEXT_SUMMARIZER -> Icons.Default.Summarize
    AiToolType.DAILY_PLANNER -> Icons.Default.CalendarMonth
}

@Composable
fun HomeDashboardScreen(
    userProfile: UserProfile?,
    recentChats: List<ChatSession>,
    unreadNotifications: Int,
    languageCode: String,
    onLanguageToggle: () -> Unit,
    onAskAiSubmit: (String) -> Unit,
    onOpenTool: (AiToolType) -> Unit,
    onOpenChatSession: (String) -> Unit,
    onOpenCredits: () -> Unit,
    onOpenSubscriptions: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    var askQuery by remember { mutableStateOf("") }
    val voiceHelper = remember(context) { VoiceHelper(context) }

    DisposableEffect(Unit) {
        onDispose { voiceHelper.release() }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceHelper.startListening(if (languageCode == "hi") "hi-IN" else "en-IN") { spoken ->
                askQuery = spoken
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onOpenTool(AiToolType.IMAGE_ANALYZER)
        }
    }

    val isFreePlan = (userProfile?.membership ?: "free") == "free"
    val allTools = remember { AiToolType.entries }
    val popularTools = remember { allTools.filter { it.popular } }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_robot_emblem),
                            contentDescription = "AI Assistant India Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, PremiumGold, CircleShape)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${strings.welcomeBack},",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = userProfile?.name ?: "Explorer",
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = onLanguageToggle,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.testTag("home_language_toggle")
                        ) {
                            Text(
                                text = if (languageCode == "hi") "हिं/EN" else "EN/हिं",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier.testTag("home_notifications_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifications > 0) {
                                        Badge { Text("$unreadNotifications") }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications"
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, PremiumGold, CircleShape)
                                .clickable(onClick = onOpenProfile)
                                .testTag("home_profile_avatar"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!userProfile?.photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = userProfile?.photoUrl,
                                    contentDescription = "User profile picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = (userProfile?.name?.firstOrNull() ?: 'A').uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PremiumGold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Main AI Search / Ask Box
            item {
                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PremiumGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Gemini AI Command Bar",
                                style = MaterialTheme.typography.labelLarge,
                                color = PremiumGold
                            )
                        }
                        CreditBadgePill(
                            credits = userProfile?.credits ?: 0L,
                            onClick = onOpenCredits,
                            modifier = Modifier.testTag("home_credits_pill")
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = askQuery,
                        onValueChange = { askQuery = it },
                        placeholder = { Text(strings.askAnythingHint) },
                        maxLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PremiumGold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_ask_ai_input")
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                onClick = {
                                    val perm = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                    if (perm == PackageManager.PERMISSION_GRANTED) {
                                        voiceHelper.startListening(if (languageCode == "hi") "hi-IN" else "en-IN") { spoken ->
                                            askQuery = spoken
                                        }
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice input"
                                    )
                                }
                            }

                            Surface(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Image upload"
                                    )
                                }
                            }
                        }

                        GoldGlowButton(
                            text = "Ask AI",
                            onClick = {
                                if (askQuery.isNotBlank()) {
                                    val q = askQuery
                                    askQuery = ""
                                    onAskAiSubmit(q)
                                } else {
                                    onOpenTool(AiToolType.AI_CHAT)
                                }
                            },
                            modifier = Modifier.testTag("home_ask_send_button")
                        )
                    }
                }
            }

            // 3. Credits & Rewarded Ad Strip + Premium Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenCredits
                    ) {
                        Text(
                            text = strings.dailyCreditsTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = PremiumGold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${userProfile?.credits ?: 0} Credits",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = strings.watchAdForCredits,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    GlassCard(
                        modifier = Modifier.weight(1f),
                        highlightGold = true,
                        onClick = onOpenSubscriptions
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = PremiumGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Premium",
                                style = MaterialTheme.typography.labelLarge,
                                color = PremiumGold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = (userProfile?.membership ?: "free").replaceFirstChar { it.uppercase() } + " Plan",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "2,000 Credits • No Ads",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 4. Visual Hero Banner for Premium Membership
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(118.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, PremiumGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable(onClick = onOpenSubscriptions)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_premium_banner),
                        contentDescription = "Premium AI Membership Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(DarkPurple.copy(alpha = 0.88f), Color.Transparent)
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            Text(
                                text = "AI ASSISTANT INDIA PRO",
                                style = MaterialTheme.typography.labelLarge,
                                color = PremiumGold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = strings.upgradeToPremium,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Weekly ₹99 • Monthly ₹299 • Yearly ₹2,499",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // 5. Popular AI Tools Carousel
            item {
                Text(
                    text = strings.popularTools,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(popularTools, key = { it.id }) { tool ->
                        GlassCard(
                            modifier = Modifier.width(164.dp),
                            onClick = { onOpenTool(tool) }
                        ) {
                            Icon(
                                imageVector = iconForTool(tool),
                                contentDescription = tool.titleEn,
                                tint = PremiumGold,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = if (languageCode == "hi") tool.titleHi else tool.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (languageCode == "hi") tool.subtitleHi else tool.subtitleEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 6. Native Ad between sections (only for Free plan)
            if (isFreePlan) {
                item {
                    AdMobNativeCard(showAds = true)
                }
            }

            // 7. All 15 Quick Access Cards (2-column grid rows)
            item {
                Text(
                    text = strings.quickAccessTools,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(allTools.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { tool ->
                        GlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_tool_${tool.id}"),
                            onClick = { onOpenTool(tool) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DeepPurple.copy(alpha = 0.8f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconForTool(tool),
                                        contentDescription = tool.titleEn,
                                        tint = PremiumGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "${tool.creditCost}★",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WarmGold
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = if (languageCode == "hi") tool.titleHi else tool.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (languageCode == "hi") tool.subtitleHi else tool.subtitleEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            // 8. Recent Conversations
            item {
                Text(
                    text = strings.recentConversations,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (recentChats.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpenTool(AiToolType.AI_CHAT) }
                    ) {
                        Text(
                            text = "No conversations yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Tap here or use the Ask AI bar above to start your first conversation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(recentChats.take(5), key = { it.chatId }) { chat ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpenChatSession(chat.chatId) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = ElectricIris,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = chat.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Open chat",
                                tint = PremiumGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
