package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = PremiumGold,
    onPrimary = DarkPurple,
    primaryContainer = DeepPurple,
    onPrimaryContainer = PremiumGold,
    secondary = ElectricIris,
    onSecondary = RoyalVoid,
    secondaryContainer = ElevatedPurple,
    onSecondaryContainer = TextWhite,
    tertiary = WarmGold,
    onTertiary = DarkPurple,
    background = RoyalVoid,
    onBackground = TextWhite,
    surface = DarkPurple,
    onSurface = TextWhite,
    surfaceVariant = SurfaceGlassDark,
    onSurfaceVariant = TextLightGray,
    outline = ColorTokens.BorderGoldSoft,
    error = ErrorCoral,
    onError = RoyalVoid
)

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = TextWhite,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = LightPrimary,
    secondary = LightSecondary,
    onSecondary = TextWhite,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightOnBackground,
    tertiary = LightAccentGold,
    onTertiary = TextWhite,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightSecondaryText,
    outline = LightSecondary.copy(alpha = 0.25f),
    error = ErrorCoral,
    onError = TextWhite
)

object ColorTokens {
    val BorderGoldSoft = PremiumGold.copy(alpha = 0.35f)
}

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun AiAssistantIndiaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
