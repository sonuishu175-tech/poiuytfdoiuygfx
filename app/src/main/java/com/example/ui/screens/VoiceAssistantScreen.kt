package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.VoiceHelper
import com.example.model.UiState
import com.example.ui.theme.AiOutputActionsBar
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.DarkPurple
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassCard
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.ShimmerLoadingCard
import com.example.ui.theme.ToolTopBar
import com.example.ui.theme.WarmGold
import com.example.viewmodel.ToolViewModel

@Composable
fun VoiceAssistantScreen(
    toolViewModel: ToolViewModel,
    userCredits: Long,
    defaultLanguage: String,
    onOpenCredits: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val voiceHelper = remember(context) { VoiceHelper(context) }
    val isListening by voiceHelper.isListening.collectAsState()
    val isSpeaking by voiceHelper.isSpeaking.collectAsState()
    val voiceError by voiceHelper.voiceError.collectAsState()
    val uiState by toolViewModel.uiState.collectAsState()

    var voiceLang by remember(defaultLanguage) { mutableStateOf(if (defaultLanguage == "hi") "hi-IN" else "en-IN") }
    var spokenTranscript by remember { mutableStateOf("") }
    var autoSpeak by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose { voiceHelper.release() }
    }

    LaunchedEffect(uiState) {
        if (autoSpeak && uiState is UiState.Success) {
            voiceHelper.speak((uiState as UiState.Success<String>).data, voiceLang)
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceHelper.startListening(voiceLang) { text ->
                spokenTranscript = text
                val langName = if (voiceLang == "hi-IN") "Hindi" else "English"
                toolViewModel.runTextTool(
                    featureName = "Voice Assistant",
                    creditCost = 1L,
                    prompt = "Respond conversationally and concisely in $langName (ideal for voice playback, avoid markdown symbols) to: $text"
                )
            }
        }
    }

    val pulseAnim = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1.02f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "scale"
    )

    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ToolTopBar(
                    title = "AI Voice Assistant",
                    subtitle = "Speech & Audio • 1 Credit",
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
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = voiceLang == "en-IN",
                                onClick = { voiceLang = "en-IN" },
                                label = { Text("English (India)") }
                            )
                            FilterChip(
                                selected = voiceLang == "hi-IN",
                                onClick = { voiceLang = "hi-IN" },
                                label = { Text("हिन्दी (Hindi)") }
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .size(144.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(PremiumGold, WarmGold, DeepPurple)
                                    )
                                )
                                .border(3.dp, PremiumGold, CircleShape)
                                .clickable {
                                    if (isListening) {
                                        voiceHelper.stopListening()
                                    } else {
                                        voiceHelper.stopSpeaking()
                                        val granted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (granted) {
                                            voiceHelper.startListening(voiceLang) { text ->
                                                spokenTranscript = text
                                                val langName = if (voiceLang == "hi-IN") "Hindi" else "English"
                                                toolViewModel.runTextTool(
                                                    featureName = "Voice Assistant",
                                                    creditCost = 1L,
                                                    prompt = "Respond concisely in $langName for voice playback (no markdown bullets/stars) to: $text"
                                                )
                                            }
                                        } else {
                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                                .testTag("voice_mic_orb"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isListening) "Stop listening" else "Tap to speak",
                                tint = DarkPurple,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (isListening) "Listening... Speak now" else "Tap the Golden Orb to Speak",
                            style = MaterialTheme.typography.titleMedium,
                            color = PremiumGold
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (isSpeaking) {
                                OutlinedButton(
                                    onClick = { voiceHelper.stopSpeaking() },
                                    modifier = Modifier.testTag("voice_stop_tts_button")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Stop Audio")
                                }
                            } else if (uiState is UiState.Success) {
                                OutlinedButton(
                                    onClick = {
                                        voiceHelper.speak((uiState as UiState.Success<String>).data, voiceLang)
                                    }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Replay Voice")
                                }
                            }
                            FilterChip(
                                selected = autoSpeak,
                                onClick = { autoSpeak = !autoSpeak },
                                label = { Text(if (autoSpeak) "Auto-Speak ON" else "Auto-Speak OFF") }
                            )
                        }
                    }

                    if (!voiceError.isNullOrBlank()) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Text(voiceError!!, color = ErrorCoral)
                            }
                        }
                    }

                    if (spokenTranscript.isNotBlank()) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Text("You said:", style = MaterialTheme.typography.labelSmall, color = PremiumGold)
                                Spacer(Modifier.height(4.dp))
                                Text(spokenTranscript, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    item {
                        when (val state = uiState) {
                            is UiState.Loading -> ShimmerLoadingCard("Preparing voice reply...")
                            is UiState.Streaming -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text(state.text, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            is UiState.Success -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text(state.data, style = MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.height(10.dp))
                                    AiOutputActionsBar(
                                        outputText = state.data,
                                        onSpeak = { voiceHelper.speak(state.data, voiceLang) },
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
