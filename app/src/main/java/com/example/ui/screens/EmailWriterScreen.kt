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
import androidx.compose.runtime.LaunchedEffect
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
fun EmailWriterScreen(
    toolViewModel: ToolViewModel,
    userCredits: Long,
    defaultLanguage: String,
    onOpenCredits: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by toolViewModel.uiState.collectAsState()
    val letterTypes = listOf(
        "Leave Application", "School Application", "College Application",
        "Government Application", "Professional Email", "Job Application", "Formal Letter"
    )
    var selectedType by remember { mutableStateOf(letterTypes.first()) }
    var recipient by remember { mutableStateOf("") }
    var contextDetails by remember { mutableStateOf("") }
    var language by remember(defaultLanguage) {
        mutableStateOf(if (defaultLanguage == "hi") "Hindi" else "English")
    }
    var editableDraft by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            editableDraft = (uiState as UiState.Success<String>).data
        }
    }

    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ToolTopBar(
                    title = "Email & Letter Writer",
                    subtitle = "Official Drafts • 1 Credit",
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
                            Text("Document Type", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(letterTypes) { type ->
                                    FilterChip(
                                        selected = selectedType == type,
                                        onClick = { selectedType = type },
                                        label = { Text(type) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listOf("English", "Hindi")) { lang ->
                                    FilterChip(
                                        selected = language == lang,
                                        onClick = { language = lang },
                                        label = { Text(lang) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = recipient,
                                onValueChange = { recipient = it },
                                label = { Text("Recipient / Authority (e.g. Principal, HR Manager)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = contextDetails,
                                onValueChange = { contextDetails = it },
                                label = { Text("Reason, dates, and key details") },
                                minLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_details_input")
                            )

                            Spacer(Modifier.height(12.dp))
                            GoldGlowButton(
                                text = "Write $selectedType (1 Credit)",
                                enabled = contextDetails.isNotBlank() && uiState !is UiState.Loading,
                                onClick = {
                                    toolViewModel.runTextTool(
                                        featureName = "Email & Application Writer",
                                        creditCost = 1L,
                                        prompt = "Write a properly formatted $selectedType in $language addressed to '${recipient.ifBlank { "Concerned Authority" }}'. Details: $contextDetails"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_generate_button")
                            )
                        }
                    }

                    item {
                        when (val state = uiState) {
                            is UiState.Loading -> ShimmerLoadingCard("Drafting $selectedType...")
                            is UiState.Streaming -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text(state.text, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            is UiState.Success -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text("Edit Before Copying / Sharing", style = MaterialTheme.typography.labelLarge)
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = editableDraft,
                                        onValueChange = { editableDraft = it },
                                        minLines = 6,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    AiOutputActionsBar(outputText = editableDraft)
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
