package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.model.UiState
import com.example.ui.theme.AiOutputActionsBar
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GoldGlowButton
import com.example.ui.theme.ShimmerLoadingCard
import com.example.ui.theme.ToolTopBar
import com.example.viewmodel.ToolViewModel
import java.io.File

@Composable
fun ImageAnalyzerScreen(
    toolViewModel: ToolViewModel,
    userCredits: Long,
    onOpenCredits: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by toolViewModel.uiState.collectAsState()
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var question by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("Object Identification") }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var permissionMessage by remember { mutableStateOf<String?>(null) }

    val modes = listOf("Object Identification", "Document / OCR", "Educational Explanation", "Custom Q&A")

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                selectedBitmap = BitmapFactory.decodeStream(stream)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraUri != null) {
            context.contentResolver.openInputStream(cameraUri!!)?.use { stream ->
                selectedBitmap = BitmapFactory.decodeStream(stream)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val dir = File(context.cacheDir, "camera").apply { mkdirs() }
            val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            permissionMessage = "Camera permission is required to capture photos."
        }
    }

    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ToolTopBar(
                    title = "AI Image Analyzer",
                    subtitle = "Gemini Vision • 2 Credits",
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("image_pick_gallery")
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Gallery")
                                }
                                OutlinedButton(
                                    onClick = {
                                        permissionMessage = null
                                        val granted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (granted) {
                                            val dir = File(context.cacheDir, "camera").apply { mkdirs() }
                                            val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
                                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                            cameraUri = uri
                                            cameraLauncher.launch(uri)
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("image_capture_camera")
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Camera")
                                }
                            }

                            if (!permissionMessage.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(permissionMessage!!, color = ErrorCoral, style = MaterialTheme.typography.bodyMedium)
                            }

                            selectedBitmap?.let { bmp ->
                                Spacer(Modifier.height(12.dp))
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Selected image preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                            }
                        }
                    }

                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Analysis Mode", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                modes.chunked(2).forEach { pair ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        pair.forEach { mode ->
                                            FilterChip(
                                                selected = selectedMode == mode,
                                                onClick = { selectedMode = mode },
                                                label = { Text(mode) }
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = question,
                                onValueChange = { question = it },
                                label = { Text("Ask a question about this image (optional)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("image_question_input")
                            )
                            Spacer(Modifier.height(12.dp))
                            GoldGlowButton(
                                text = "Analyze Image (2 Credits)",
                                enabled = selectedBitmap != null && uiState !is UiState.Loading && uiState !is UiState.Streaming,
                                onClick = {
                                    val bmp = selectedBitmap ?: return@GoldGlowButton
                                    val prompt = buildString {
                                        append("Mode: $selectedMode. ")
                                        if (question.isNotBlank()) append("User Question: $question")
                                        else append("Provide a thorough, clear explanation in structured points.")
                                    }
                                    toolViewModel.runVisionTool(
                                        featureName = "Image Analyzer",
                                        creditCost = 2L,
                                        prompt = prompt,
                                        images = listOf(bmp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("image_analyze_button")
                            )
                        }
                    }

                    item {
                        when (val state = uiState) {
                            is UiState.Loading -> ShimmerLoadingCard("Inspecting image with Gemini Vision...")
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
