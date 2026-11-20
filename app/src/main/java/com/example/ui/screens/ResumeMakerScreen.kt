package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.PdfHelper
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

@Composable
fun ResumeMakerScreen(
    toolViewModel: ToolViewModel,
    userCredits: Long,
    onOpenCredits: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by toolViewModel.uiState.collectAsState()
    val templates = listOf("Modern Executive", "Tech & Engineering", "Fresher Academic")

    var selectedTemplate by remember { mutableStateOf(templates.first()) }
    var fullName by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var projects by remember { mutableStateOf("") }
    var exportMessage by remember { mutableStateOf<String?>(null) }

    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ToolTopBar(
                    title = "AI Resume Maker",
                    subtitle = "CV Builder & PDF Export • 2 Credits",
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
                            Text("Resume Template", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(templates) { tpl ->
                                    FilterChip(
                                        selected = selectedTemplate == tpl,
                                        onClick = { selectedTemplate = tpl },
                                        label = { Text(tpl) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("resume_name_input")
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = contactInfo,
                                onValueChange = { contactInfo = it },
                                label = { Text("Phone, Email, City & LinkedIn") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = education,
                                onValueChange = { education = it },
                                label = { Text("Education (Degree, College, Year)") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = skills,
                                onValueChange = { skills = it },
                                label = { Text("Core Skills") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = experience,
                                onValueChange = { experience = it },
                                label = { Text("Work Experience / Internships") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = projects,
                                onValueChange = { projects = it },
                                label = { Text("Key Projects & Achievements") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(12.dp))
                            GoldGlowButton(
                                text = "Build Professional Resume (2 Credits)",
                                enabled = fullName.isNotBlank() && uiState !is UiState.Loading,
                                onClick = {
                                    exportMessage = null
                                    toolViewModel.runTextTool(
                                        featureName = "Resume Maker",
                                        creditCost = 2L,
                                        prompt = """
                                            Create an ATS-friendly professional resume using the '$selectedTemplate' layout style in plain, cleanly formatted text (no markdown # or ** symbols so it prints cleanly in PDF).
                                            Include:
                                            1. Header with Name and Contact Info
                                            2. Strong Professional Summary
                                            3. Core Skills
                                            4. Experience
                                            5. Projects
                                            6. Education

                                            Candidate Details:
                                            Name: $fullName
                                            Contact: $contactInfo
                                            Education: $education
                                            Skills: $skills
                                            Experience: $experience
                                            Projects: $projects
                                        """.trimIndent()
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().testTag("resume_generate_button")
                            )
                        }
                    }

                    item {
                        when (val state = uiState) {
                            is UiState.Loading -> ShimmerLoadingCard("Drafting ATS-ready resume...")
                            is UiState.Streaming -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text(state.text, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            is UiState.Success -> {
                                GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                                    Text(state.data, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            val pdfRes = PdfHelper.exportResumeToPdf(context, fullName, state.data)
                                            pdfRes.onSuccess { uri ->
                                                exportMessage = "PDF ready! Opening share sheet..."
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "application/pdf"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Resume PDF"))
                                            }.onFailure { err ->
                                                exportMessage = err.localizedMessage ?: "PDF export failed"
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("resume_export_pdf_button")
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Export & Share as PDF")
                                    }
                                    if (!exportMessage.isNullOrBlank()) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(exportMessage!!, color = PremiumGold, style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(Modifier.height(10.dp))
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
