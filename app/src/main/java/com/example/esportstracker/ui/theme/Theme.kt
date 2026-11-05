package com.example.esportstracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EsportsColors = darkColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.PrimaryTint,
    onPrimaryContainer = AppColors.Primary,
    secondary = AppColors.Cyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = AppColors.CyanTint,
    onSecondaryContainer = AppColors.Cyan,
    tertiary = AppColors.Success,
    tertiaryContainer = AppColors.SuccessTint,
    onTertiaryContainer = AppColors.Success,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceHigh,
    onSurfaceVariant = AppColors.TextMuted,
    surfaceContainerLowest = AppColors.Background,
    surfaceContainerLow = AppColors.Surface,
    surfaceContainer = AppColors.NavBar,
    surfaceContainerHigh = AppColors.SurfaceHigh,
    surfaceContainerHighest = AppColors.SurfaceHigh,
    outline = AppColors.Border,
    outlineVariant = AppColors.Border,
    error = AppColors.Danger,
    onError = Color.White,
    errorContainer = AppColors.DangerTint,
    onErrorContainer = AppColors.Danger
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun EsportsTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsportsColors,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}