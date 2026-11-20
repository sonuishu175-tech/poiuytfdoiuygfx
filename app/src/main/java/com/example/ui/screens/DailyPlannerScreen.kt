package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.NotificationHelper
import com.example.model.DailyTask
import com.example.model.UiState
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GoldGlowButton
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.ShimmerLoadingCard
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.ToolTopBar
import com.example.viewmodel.PlannerViewModel

@Composable
fun DailyPlannerScreen(
    plannerViewModel: PlannerViewModel,
    userCredits: Long,
    defaultLanguage: String,
    onOpenCredits: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val tasksState by plannerViewModel.tasksState.collectAsState()
    val aiAdvice by plannerViewModel.aiAdvice.collectAsState()
    val isGenerating by plannerViewModel.isGenerating.collectAsState()
    val error by plannerViewModel.error.collectAsState()

    var goalPrompt by remember { mutableStateOf("") }
    var manualTitle by remember { mutableStateOf("") }
    var manualCategory by remember { mutableStateOf("Study") }
    var manualReminder by remember { mutableStateOf("08:00 AM") }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            NotificationHelper.showLocalNotification(
                context,
                "Daily Planner Reminder Enabled",
                "You will receive task and study schedule alerts."
            )
        }
    }

    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ToolTopBar(
                    title = "AI Daily Planner",
                    subtitle = "Schedule & Goals • 1 Credit AI Plan",
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
                    // Progress Summary
                    item {
                        val tasks = (tasksState as? UiState.Success<List<DailyTask>>)?.data.orEmpty()
                        val doneCount = tasks.count { it.completed }
                        val progress = if (tasks.isEmpty()) 0f else doneCount.toFloat() / tasks.size
                        GlassCard(highlightGold = true, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Today's Progress", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "$doneCount / ${tasks.size} Completed",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PremiumGold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                color = SuccessEmerald,
                                modifier = Modifier.fillMaxWidth().height(8.dp)
                            )
                        }
                    }

                    // AI Timetable Generator
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Generate AI Schedule / Study Timetable", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = goalPrompt,
                                onValueChange = { goalPrompt = it },
                                label = { Text("e.g. NEET Physics + Organic Chem + 1 hr coding") },
                                modifier = Modifier.fillMaxWidth().testTag("planner_ai_goal_input")
                            )
                            Spacer(Modifier.height(10.dp))
                            GoldGlowButton(
                                text = "Build AI Timetable (1 Credit)",
                                enabled = goalPrompt.isNotBlank() && !isGenerating,
                                isLoading = isGenerating,
                                onClick = {
                                    plannerViewModel.generateAiPlan(goalPrompt, defaultLanguage)
                                    goalPrompt = ""
                                },
                                modifier = Modifier.fillMaxWidth().testTag("planner_ai_generate_button")
                            )
                            if (isGenerating) {
                                Spacer(Modifier.height(8.dp))
                                ShimmerLoadingCard("Structuring your daily plan...")
                            }
                            if (!aiAdvice.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text("AI Tip: $aiAdvice", color = PremiumGold, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Manual Task Adder
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Add Manual Task", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Study", "Work", "Goal").forEach { cat ->
                                    FilterChip(
                                        selected = manualCategory == cat,
                                        onClick = { manualCategory = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = manualTitle,
                                    onValueChange = { manualTitle = it },
                                    label = { Text("Task title") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.4f).testTag("planner_manual_title")
                                )
                                OutlinedTextField(
                                    value = manualReminder,
                                    onValueChange = { manualReminder = it },
                                    label = { Text("Time") },
                                    singleLine = true,
                                    modifier = Modifier.weight(0.8f)
                                )
                                IconButton(
                                    onClick = {
                                        if (manualTitle.isNotBlank()) {
                                            plannerViewModel.addTask(manualTitle, manualCategory, manualReminder)
                                            manualTitle = ""
                                        }
                                    },
                                    modifier = Modifier.testTag("planner_add_task_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add task", tint = PremiumGold)
                                }
                            }
                        }
                    }

                    if (!error.isNullOrBlank()) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Text(error!!, color = ErrorCoral)
                            }
                        }
                    }

                    // Task List
                    when (val state = tasksState) {
                        is UiState.Loading -> {
                            item { ShimmerLoadingCard("Loading your planner...") }
                        }
                        is UiState.Error -> {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(state.message, color = ErrorCoral)
                                }
                            }
                        }
                        is UiState.Success -> {
                            items(state.data, key = { it.taskId }) { task ->
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = task.completed,
                                            onCheckedChange = { checked ->
                                                plannerViewModel.toggleTask(task.taskId, checked)
                                            }
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                                                )
                                            )
                                            Text(
                                                text = "${task.category} • ${task.reminderTime}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                                    ContextCompat.checkSelfPermission(
                                                        context,
                                                        Manifest.permission.POST_NOTIFICATIONS
                                                    ) != PackageManager.PERMISSION_GRANTED
                                                ) {
                                                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                } else {
                                                    NotificationHelper.showLocalNotification(
                                                        context,
                                                        "Reminder: ${task.reminderTime}",
                                                        task.title
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.NotificationsActive, contentDescription = "Trigger reminder", tint = PremiumGold)
                                        }
                                        IconButton(onClick = { plannerViewModel.deleteTask(task.taskId) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete task", tint = ErrorCoral)
                                        }
                                    }
                                }
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}
