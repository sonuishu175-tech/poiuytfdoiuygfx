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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.VoiceHelper
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
fun StoryGeneratorScreen(
    toolViewModel: ToolViewModel,
    userCredits: Long,
    defaultLanguage: String,
    onOpenCredits: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val voiceHelper = remember(context) { VoiceHelper(context) }
    val isSpeaking by voiceHelper.isSpeaking.collectAsState()
    val uiState by toolViewModel.uiState.collectAsState()

    val categories = listOf(
        "Horror", "Funny", "Adventure", "Mystery", "Moral Stories",
        "Kids Stories", "Romantic Fiction", "YouTube Story Script"
    )
    val lengths = listOf("Short (2 min)", "Medium (5 min)", "Detailed (8 min)")
    val styles = listOf("Cinematic", "Emotional", "Suspenseful", "Bedtime")

    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var selectedLength by remember { mutableStateOf(lengths.first()) }
    var selectedStyle by remember { mutableStateOf(styles.first()) }
    var language by remember(defaultLanguage) {
        mutableStateOf(if (defaultLanguage == "hi") "Hindi" else "English")
    }
    var plotPrompt by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose { voiceHelper.release() }
    }

    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ToolTopBar(
                    title = "AI Story Generator",
                    subtitle = "Creative Fiction • 1 Credit",
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
                            Text("Genre", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(categories) { cat ->
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = { selectedCategory = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Text("Length & Style", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(lengths) { len ->
                                    FilterChip(
                                        selected = selectedLength == len,
                                        onClick = { selectedLength = len },
                                        label = { Text(len) }
                                    )
                                }
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(styles) { style ->
                                    FilterChip(
                                        selected = selectedStyle == style,
                                        onClick = { selectedStyle = style },
                                        label = { Text(style) }
                                    )
                                }
                            }
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
                                value = plotPrompt,
                                onValueChange = { plotPrompt = it },
                                label = { Text("Characters, setting, or theme") },
                                minLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("story_plot_input")
                            )

                            Spacer(Modifier.height(12.dp))
                            GoldGlowButton(
                                text = "Generate Story (1 Credit)",
                                enabled = plotPrompt.isNotBlank() && uiState !is UiState.Loading,
                                onClick = {
                                    toolViewModel.runTextTool(
                                        featureName = "Story Generator",
                                        creditCost = 1L,
                                        prompt = "Write an original $selectedCategory story in $language, length $selectedLength, style $selectedStyle. Premise: $plotPrompt"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("story_generate_button")
                            )
                        }
                    }

                    item {
                        when (val state = uiState) {
                            is UiState.Loading -> ShimmerLoadingCard("Weaving your story...")
                            is UiState.Streaming -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text(state.text, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            is UiState.Success -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text(state.data, style = MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.height(12.dp))
                                    val ttsTag = if (language == "Hindi") "hi-IN" else "en-IN"
                                    AiOutputActionsBar(
                                        outputText = state.data,
                                        onSpeak = { voiceHelper.speak(state.data, ttsTag) },
                                        isSpeaking = isSpeaking,
                                        onStopSpeak = { voiceHelper.stopSpeaking() }
                                    )
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
