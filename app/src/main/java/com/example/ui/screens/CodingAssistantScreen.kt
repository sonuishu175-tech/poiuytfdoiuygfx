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
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.ShimmerLoadingCard
import com.example.ui.theme.ToolTopBar
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.ToolViewModel

@Composable
fun CodingAssistantScreen(
    toolViewModel: ToolViewModel,
    userCredits: Long,
    onOpenCredits: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by toolViewModel.uiState.collectAsState()
    val languages = listOf("Kotlin", "Java", "Python", "HTML", "CSS", "JavaScript", "C", "C++")
    val tasks = listOf(
        "Code Generation", "Code Explanation", "Error Analysis",
        "Debugging Suggestions", "Code Formatting", "Programming Tutorial"
    )

    var selectedLang by remember { mutableStateOf(languages.first()) }
    var selectedTask by remember { mutableStateOf(tasks.first()) }
    var codePrompt by remember { mutableStateOf("") }

    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ToolTopBar(
                    title = "AI Coding Assistant",
                    subtitle = "8 Languages • 2 Credits",
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
                        .widthIn(max = 760.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Programming Language", style = MaterialTheme.typography.titleMedium)
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
                            Text("Mode", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(tasks) { task ->
                                    FilterChip(
                                        selected = selectedTask == task,
                                        onClick = { selectedTask = task },
                                        label = { Text(task) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = codePrompt,
                                onValueChange = { codePrompt = it },
                                label = { Text("Paste code, error log, or describe feature") },
                                minLines = 5,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("coding_prompt_input")
                            )

                            Spacer(Modifier.height(12.dp))
                            GoldGlowButton(
                                text = "Run $selectedTask (2 Credits)",
                                enabled = codePrompt.isNotBlank() && uiState !is UiState.Loading,
                                onClick = {
                                    toolViewModel.runTextTool(
                                        featureName = "Coding Assistant",
                                        creditCost = 2L,
                                        prompt = "Language: $selectedLang. Task: $selectedTask. Provide clean, safe, production-quality code and explanation.\n\nInput:\n$codePrompt"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("coding_submit_button")
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Security Note: Generated code is static text for review and never executed automatically on your device.",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningAmber
                        )
                    }

                    item {
                        when (val state = uiState) {
                            is UiState.Loading -> ShimmerLoadingCard("Analyzing $selectedLang code...")
                            is UiState.Streaming -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = state.text,
                                        fontFamily = JetBrainsMonoFamily,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            is UiState.Success -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = state.data,
                                        fontFamily = JetBrainsMonoFamily,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
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
