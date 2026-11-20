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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.ShimmerLoadingCard
import com.example.ui.theme.ToolTopBar
import com.example.viewmodel.ToolViewModel

private val TRANSLATOR_LANGUAGES = listOf(
    "Auto Detect", "English", "Hindi", "Marathi", "Bengali", "Tamil", "Telugu", "Gujarati",
    "Kannada", "Malayalam", "Punjabi", "Urdu", "Odia", "Assamese", "Sanskrit", "Nepali",
    "Arabic", "French", "Spanish", "German", "Italian", "Portuguese", "Russian", "Japanese",
    "Korean", "Chinese (Simplified)", "Chinese (Traditional)", "Turkish", "Vietnamese", "Thai",
    "Indonesian", "Malay", "Dutch", "Polish", "Swedish", "Norwegian", "Danish", "Finnish",
    "Greek", "Hebrew", "Persian", "Swahili", "Ukrainian", "Czech", "Romanian", "Hungarian",
    "Filipino", "Sinhala", "Burmese", "Khmer", "Lao", "Tibetan"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    toolViewModel: ToolViewModel,
    userCredits: Long,
    onOpenCredits: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val voiceHelper = remember(context) { VoiceHelper(context) }
    val isSpeaking by voiceHelper.isSpeaking.collectAsState()
    val uiState by toolViewModel.uiState.collectAsState()

    var sourceLang by remember { mutableStateOf("Auto Detect") }
    var targetLang by remember { mutableStateOf("Hindi") }
    var sourceExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }
    var sourceText by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose { voiceHelper.release() }
    }

    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ToolTopBar(
                    title = "AI Translator",
                    subtitle = "50+ Languages • 1 Credit",
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = sourceExpanded,
                                    onExpandedChange = { sourceExpanded = !sourceExpanded },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = sourceLang,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("From") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sourceExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = sourceExpanded,
                                        onDismissRequest = { sourceExpanded = false }
                                    ) {
                                        TRANSLATOR_LANGUAGES.forEach { lang ->
                                            DropdownMenuItem(
                                                text = { Text(lang) },
                                                onClick = {
                                                    sourceLang = lang
                                                    sourceExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        if (sourceLang != "Auto Detect") {
                                            val tmp = sourceLang
                                            sourceLang = targetLang
                                            targetLang = tmp
                                        }
                                    },
                                    modifier = Modifier.testTag("translator_swap_button")
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = "Swap languages", tint = PremiumGold)
                                }

                                ExposedDropdownMenuBox(
                                    expanded = targetExpanded,
                                    onExpandedChange = { targetExpanded = !targetExpanded },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = targetLang,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("To") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(targetExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = targetExpanded,
                                        onDismissRequest = { targetExpanded = false }
                                    ) {
                                        TRANSLATOR_LANGUAGES.drop(1).forEach { lang ->
                                            DropdownMenuItem(
                                                text = { Text(lang) },
                                                onClick = {
                                                    targetLang = lang
                                                    targetExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = sourceText,
                                onValueChange = { sourceText = it },
                                label = { Text("Enter text to translate") },
                                minLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("translator_input")
                            )

                            Spacer(Modifier.height(12.dp))

                            GoldGlowButton(
                                text = "Translate Now (1 Credit)",
                                enabled = sourceText.isNotBlank() && uiState !is UiState.Loading,
                                onClick = {
                                    val prompt = if (sourceLang == "Auto Detect") {
                                        "Detect the source language, state it briefly, then translate the following text accurately into $targetLang:\n\n$sourceText"
                                    } else {
                                        "Translate accurately from $sourceLang to $targetLang. Return natural, idiomatic translation:\n\n$sourceText"
                                    }
                                    toolViewModel.runTextTool(
                                        featureName = "AI Translator",
                                        creditCost = 1L,
                                        prompt = prompt,
                                        useFastModel = true
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("translator_submit_button")
                            )
                        }
                    }

                    item {
                        when (val state = uiState) {
                            is UiState.Loading -> ShimmerLoadingCard("Translating...")
                            is UiState.Streaming -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text(state.text, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            is UiState.Success -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text(state.data, style = MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.height(12.dp))
                                    val ttsTag = if (targetLang == "Hindi") "hi-IN" else "en-IN"
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
