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
fun ContentWriterScreen(
    toolViewModel: ToolViewModel,
    userCredits: Long,
    defaultLanguage: String,
    onOpenCredits: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by toolViewModel.uiState.collectAsState()
    val contentTypes = listOf(
        "YouTube Titles", "YouTube Description", "Hashtags", "Instagram Caption",
        "Facebook Post", "Blog Article", "Professional Content", "Product Description"
    )
    val tones = listOf("Engaging", "Professional", "Witty", "Persuasive", "Viral")
    val languages = listOf("English", "Hindi", "Hinglish")

    var selectedType by remember { mutableStateOf(contentTypes.first()) }
    var selectedTone by remember { mutableStateOf(tones.first()) }
    var selectedLang by remember(defaultLanguage) {
        mutableStateOf(if (defaultLanguage == "hi") "Hindi" else "English")
    }
    var topic by remember { mutableStateOf("") }

    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ToolTopBar(
                    title = "AI Content Writer",
                    subtitle = "Creator Studio • 1 Credit",
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
                            Text("Content Format", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(contentTypes) { type ->
                                    FilterChip(
                                        selected = selectedType == type,
                                        onClick = { selectedType = type },
                                        label = { Text(type) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Text("Tone", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(tones) { tone ->
                                    FilterChip(
                                        selected = selectedTone == tone,
                                        onClick = { selectedTone = tone },
                                        label = { Text(tone) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Text("Language", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(languages) { lang ->
                                    FilterChip(
                                        selected = selectedLang == lang,
                                        onClick = { selectedLang = lang },
                                        label = { Text(lang) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = topic,
                                onValueChange = { topic = it },
                                label = { Text("Topic, product, or niche details") },
                                minLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("content_topic_input")
                            )

                            Spacer(Modifier.height(12.dp))
                            GoldGlowButton(
                                text = "Generate $selectedType (1 Credit)",
                                enabled = topic.isNotBlank() && uiState !is UiState.Loading,
                                onClick = {
                                    toolViewModel.runTextTool(
                                        featureName = "Content Writer",
                                        creditCost = 1L,
                                        prompt = "Write high-converting $selectedType in $selectedLang with a $selectedTone tone for: $topic"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("content_generate_button")
                            )
                        }
                    }

                    item {
                        when (val state = uiState) {
                            is UiState.Loading -> ShimmerLoadingCard("Crafting your $selectedType...")
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
