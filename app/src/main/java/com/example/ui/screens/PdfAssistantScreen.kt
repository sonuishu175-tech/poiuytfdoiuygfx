package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.PdfHelper
import com.example.model.UiState
import com.example.model.UserFileRecord
import com.example.ui.theme.AiOutputActionsBar
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GoldGlowButton
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.ShimmerLoadingCard
import com.example.ui.theme.ToolTopBar
import com.example.viewmodel.ToolViewModel

@Composable
fun PdfAssistantScreen(
    toolViewModel: ToolViewModel,
    userCredits: Long,
    savedFiles: List<UserFileRecord>,
    onSaveFileRecord: (String, String, Long, String?) -> Unit,
    onDeleteFileRecord: (String) -> Unit,
    onOpenCredits: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by toolViewModel.uiState.collectAsState()
    val modes = listOf("PDF Summary", "Important Points", "Create Study Notes", "Ask Question")

    var selectedMode by remember { mutableStateOf(modes.first()) }
    var fileName by remember { mutableStateOf("") }
    var sizeBytes by remember { mutableStateOf(0L) }
    var pageCount by remember { mutableStateOf(0) }
    var renderedPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var customQuestion by remember { mutableStateOf("") }
    var loadError by remember { mutableStateOf<String?>(null) }
    var savedForThisRun by remember { mutableStateOf(false) }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            loadError = null
            PdfHelper.inspectAndRenderPdf(context, it).onSuccess { result ->
                fileName = result.fileName
                sizeBytes = result.sizeBytes
                pageCount = result.pageCount
                renderedPages = result.pageBitmaps
                savedForThisRun = false
            }.onFailure { err ->
                loadError = err.localizedMessage ?: "Failed to read PDF"
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success && !savedForThisRun && fileName.isNotBlank()) {
            savedForThisRun = true
            onSaveFileRecord(
                fileName,
                "device_pdf/$fileName",
                sizeBytes,
                (uiState as UiState.Success<String>).data.take(1200)
            )
        }
    }

    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ToolTopBar(
                    title = "AI PDF Assistant",
                    subtitle = "Max 10 MB • 2 Credits",
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
                            OutlinedButton(
                                onClick = { pdfPicker.launch(arrayOf("application/pdf")) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("pdf_select_button")
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (fileName.isBlank()) "Select PDF Document (Max 10 MB)" else "Change PDF ($fileName)")
                            }

                            if (!loadError.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(loadError!!, color = ErrorCoral)
                            }

                            if (fileName.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Loaded: $fileName • $pageCount pages (${sizeBytes / 1024} KB) • Inspecting first ${renderedPages.size} pages",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PremiumGold
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(modes) { mode ->
                                    FilterChip(
                                        selected = selectedMode == mode,
                                        onClick = { selectedMode = mode },
                                        label = { Text(mode) }
                                    )
                                }
                            }

                            if (selectedMode == "Ask Question") {
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customQuestion,
                                    onValueChange = { customQuestion = it },
                                    label = { Text("What would you like to ask about this PDF?") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            GoldGlowButton(
                                text = "Analyze PDF (2 Credits)",
                                enabled = renderedPages.isNotEmpty() && uiState !is UiState.Loading,
                                onClick = {
                                    savedForThisRun = false
                                    val prompt = when (selectedMode) {
                                        "PDF Summary" -> "Extract key concepts and produce a clean executive summary of this PDF ($fileName)."
                                        "Important Points" -> "List the most important bullet points, dates, and figures from this PDF ($fileName)."
                                        "Create Study Notes" -> "Turn this PDF ($fileName) into structured revision study notes with headings and key takeaways."
                                        else -> "Answer this question based strictly on the attached PDF pages ($fileName): $customQuestion"
                                    }
                                    toolViewModel.runVisionTool(
                                        featureName = "PDF Assistant",
                                        creditCost = 2L,
                                        prompt = prompt,
                                        images = renderedPages
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pdf_analyze_button")
                            )
                        }
                    }

                    item {
                        when (val state = uiState) {
                            is UiState.Loading -> ShimmerLoadingCard("Reading PDF pages with Gemini...")
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

                    if (savedFiles.isNotEmpty()) {
                        item {
                            Text("Analyzed PDF History", style = MaterialTheme.typography.titleLarge)
                        }
                        items(savedFiles, key = { it.fileId }) { file ->
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = PremiumGold)
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(file.fileName, style = MaterialTheme.typography.titleMedium)
                                        if (!file.summary.isNullOrBlank()) {
                                            Text(
                                                text = file.summary.take(120) + "...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(onClick = { onDeleteFileRecord(file.fileId) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete PDF record", tint = ErrorCoral)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
