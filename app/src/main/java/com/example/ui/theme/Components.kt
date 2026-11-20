package com.example.ui.theme

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun isAppDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.2f

@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isAppDarkTheme()
    val brush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(DarkPurple, RoyalVoid, Color(0xFF140B29))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF3EDFC), LightBackground, Color(0xFFFAF8FE))
        )
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush),
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    highlightGold: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isAppDarkTheme()
    val containerColor = if (isDark) {
        if (highlightGold) DeepPurple.copy(alpha = 0.85f) else SurfaceGlassDark.copy(alpha = 0.78f)
    } else {
        if (highlightGold) Color(0xFFFFF8E6) else LightSurface
    }
    val borderBrush = if (highlightGold) {
        Brush.linearGradient(listOf(PremiumGold.copy(alpha = 0.8f), WarmGold.copy(alpha = 0.35f)))
    } else if (isDark) {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.16f), ElectricIris.copy(alpha = 0.15f)))
    } else {
        Brush.linearGradient(listOf(LightSecondary.copy(alpha = 0.20f), LightAccentGold.copy(alpha = 0.25f)))
    }

    val shape = RoundedCornerShape(20.dp)
    val baseModifier = modifier
        .clip(shape)
        .border(BorderStroke(1.dp, borderBrush), shape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    Surface(
        modifier = baseModifier,
        shape = shape,
        color = containerColor,
        tonalElevation = if (isDark) 4.dp else 2.dp
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun GoldGlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)
    val gradient = Brush.horizontalGradient(
        colors = if (enabled) listOf(PremiumGold, WarmGold) else listOf(Color.Gray, Color.DarkGray)
    )
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = DarkPurple,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .heightIn(min = 50.dp)
            .shadow(if (enabled) 10.dp else 0.dp, shape, ambientColor = PremiumGold, spotColor = PremiumGold)
            .background(gradient, shape)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = DarkPurple,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) DarkPurple else Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CreditBadgePill(
    credits: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 38.dp),
        shape = CircleShape,
        color = SoftGoldTint,
        border = BorderStroke(1.dp, PremiumGold.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "★",
                color = PremiumGold,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "$credits",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                color = if (isAppDarkTheme()) PremiumGold else LightPrimary,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolTopBar(
    title: String,
    subtitle: String? = null,
    credits: Long? = null,
    onBack: () -> Unit,
    onCreditsClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back"
                )
            }
        },
        actions = {
            if (credits != null) {
                CreditBadgePill(
                    credits = credits,
                    onClick = onCreditsClick,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun AiOutputActionsBar(
    outputText: String,
    onSpeak: (() -> Unit)? = null,
    isSpeaking: Boolean = false,
    onStopSpeak: (() -> Unit)? = null
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var copied by remember(outputText) { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = {
                clipboard.setText(AnnotatedString(outputText))
                copied = true
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = "Copy response",
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(if (copied) "Copied" else "Copy")
        }

        OutlinedButton(
            onClick = { sharePlainText(context, outputText) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share response",
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Share")
        }

        if (onSpeak != null) {
            OutlinedButton(
                onClick = { if (isSpeaking) onStopSpeak?.invoke() else onSpeak() },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isSpeaking) "Stop speech" else "Listen",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ShimmerLoadingCard(label: String = "Generating with AI...") {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    GlassCard(modifier = Modifier.fillMaxWidth(), highlightGold = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.5.dp
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
        }
    }
}

fun sharePlainText(context: Context, text: String, title: String = "Share AI Output") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, title))
}
