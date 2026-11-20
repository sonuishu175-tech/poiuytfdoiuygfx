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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AdMobBannerSection
import com.example.model.AiToolType
import com.example.ui.theme.GlassCard
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.WarmGold

@Composable
fun ToolsHubScreen(
    languageCode: String,
    isFreePlan: Boolean,
    onOpenTool: (AiToolType) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = remember {
        listOf("All", "Core", "Creator", "Study", "Tech", "Career", "Business", "Productivity", "Language", "Vision", "Writing")
    }

    val filteredTools = remember(searchQuery, selectedCategory) {
        AiToolType.entries.filter { tool ->
            val matchesCat = selectedCategory == "All" || tool.category == selectedCategory
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                tool.titleEn.lowercase().contains(q) ||
                tool.titleHi.lowercase().contains(q) ||
                tool.subtitleEn.lowercase().contains(q)
            matchesCat && matchesQuery
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (languageCode == "hi") "सभी 15 एआई टूल्स" else "All 15 AI Tools",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search AI tools...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tools_search_input")
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }

            items(filteredTools, key = { it.id }) { tool ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hub_tool_${tool.id}"),
                    onClick = { onOpenTool(tool) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = iconForTool(tool),
                            contentDescription = tool.titleEn,
                            tint = PremiumGold,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (languageCode == "hi") tool.titleHi else tool.titleEn,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (languageCode == "hi") tool.subtitleHi else tool.subtitleEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${tool.creditCost} ★",
                            style = MaterialTheme.typography.labelLarge,
                            color = WarmGold
                        )
                    }
                }
            }

            if (isFreePlan) {
                item {
                    AdMobBannerSection(showAds = true)
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}
