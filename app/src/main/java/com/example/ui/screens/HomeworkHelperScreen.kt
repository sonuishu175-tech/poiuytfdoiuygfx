package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.ToolViewModel

@Composable
fun HomeworkHelperScreen(
    toolViewModel: ToolViewModel,
    userCredits: Long,
    onOpenCredits: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by toolViewModel.uiState.collectAsState()
    val subjects = listOf(
        "Mathematics", "Physics", "Chemistry", "Biology", "English", "General Knowledge", "NEET Preparation"
    )
    val modes = listOf("Step-by-Step Solution", "Formula Breakdown", "Practice Questions")

    var selectedSubject by remember { mutableStateOf(subjects.first()) }
    var selectedMode by remember { mutableStateOf(modes.first()) }
    var questionText by remember { mutableStateOf("") }
    var questionBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                questionBitmap = BitmapFactory.decodeStream(stream)
            }
        }
    }

    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ToolTopBar(
                    title = "AI Homework Helper",
                    subtitle = "STEM & NEET Tutor • 2 Credits",
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
                            Text("Subject", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(subjects) { subject ->
                                    FilterChip(
                                        selected = selectedSubject == subject,
                                        onClick = { selectedSubject = subject },
                                        label = { Text(subject) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Text("Study Mode", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(modes) { mode ->
                                    FilterChip(
                                        selected = selectedMode == mode,
                                        onClick = { selectedMode = mode },
                                        label = { Text(mode) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = questionText,
                                onValueChange = { questionText = it },
                                label = { Text("Type your question or topic") },
                                minLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("homework_question_input")
                            )

                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    imagePicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (questionBitmap == null) "Upload Question Image (Optional)" else "Change Question Image")
                            }

                            questionBitmap?.let { bmp ->
                                Spacer(Modifier.height(10.dp))
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Question image",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            GoldGlowButton(
                                text = "Solve & Explain (2 Credits)",
                                enabled = (questionText.isNotBlank() || questionBitmap != null) && uiState !is UiState.Loading,
                                onClick = {
                                    val prompt = "Subject: $selectedSubject. Mode: $selectedMode. " +
                                        "Provide clear step-by-step educational reasoning and highlight formulas. " +
                                        "If any part of the question is ambiguous or unverified, explicitly note uncertainty rather than guessing. " +
                                        "Question: ${questionText.ifBlank { "Solve the problem shown in the attached image." }}"
                                    val bmp = questionBitmap
                                    if (bmp != null) {
                                        toolViewModel.runVisionTool("Homework Helper", 2L, prompt, listOf(bmp))
                                    } else {
                                        toolViewModel.runTextTool("Homework Helper", 2L, prompt)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("homework_solve_button")
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Note: AI explanations are educational study aids. Always verify critical exam formulas and answers with standard textbooks.",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningAmber
                        )
                    }

                    item {
                        when (val state = uiState) {
                            is UiState.Loading -> ShimmerLoadingCard("Solving $selectedSubject problem...")
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
