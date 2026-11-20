package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.UiState
import com.example.ui.theme.AiOutputActionsBar
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GoldGlowButton
import com.example.ui.theme.ShimmerLoadingCard
import com.example.ui.theme.ToolTopBar
import com.example.viewmodel.ToolViewModel

@Composable
fun YouTubeCreatorScreen(
    toolViewModel: ToolViewModel,
    userCredits: Long,
    defaultLanguage: String,
    onOpenCredits: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by toolViewModel.uiState.collectAsState()
    val tools = listOf(
        "Video Ideas", "Viral Titles", "SEO Description", "Hashtags",
        "Keyword Tags", "Full Video Script", "Thumbnail Prompt", "Shorts Ideas", "30-Day Content Planner"
    )
    var selectedTool by remember { mutableStateOf(tools.first()) }
    var channelNiche by remember { mutableStateOf("") }
    var language by remember(defaultLanguage) {
        mutableStateOf(if (defaultLanguage == "hi") "Hindi" else "English")
    }

    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ToolTopBar(
                    title = "YouTube Creator AI",
                    subtitle = "Growth & Scripts • 1 Credit",
                    credits = userCredits,
                    onBack = onBack,
                    onCreditsClick = onOpenCredits
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 720.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Creator Tool", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(tools) { item ->
                                    FilterChip(
                                        selected = selectedTool == item,
                                        onClick = { selectedTool = item },
                                        label = { Text(item) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listOf("English", "Hindi", "Hinglish")) { lang ->
                                    FilterChip(
                                        selected = language == lang,
                                        onClick = { language = lang },
                                        label = { Text(lang) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = channelNiche,
                                onValueChange = { channelNiche = it },
                                label = { Text("Channel niche, video topic, or target audience") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth().testTag("youtube_topic_input")
                            )
                            Spacer(Modifier.height(12.dp))
                            GoldGlowButton(
                                text = "Generate $selectedTool (1 Credit)",
                                enabled = channelNiche.isNotBlank() && uiState !is UiState.Loading,
                                onClick = {
                                    toolViewModel.runTextTool(
                                        featureName = "YouTube Creator AI",
                                        creditCost = 1L,
                                        prompt = "Act as an expert YouTube growth strategist for Indian and global audiences. Generate '$selectedTool' in $language for: $channelNiche"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().testTag("youtube_generate_button")
                            )
                        }
                    }

                    item {
                        when (val state = uiState) {
                            is UiState.Loading -> ShimmerLoadingCard("Generating $selectedTool...")
                            is UiState.Streaming -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text(state.text, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            is UiState.Success -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text(state.data, style = MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.height(12.dp))
                                    AiOutputActionsBar(outputText = state.data)
                                }
                            }
                            is UiState.Error -> {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(state.message, color = ErrorCoral)
                                }
                            }
                            UiState.Idle -> Unit
                        }
                    }
                }
            }
        }
    }
}
