package com.example.esportstracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsportsColors = darkColorScheme(
    primary = Color(0xFF8B5CF6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A2350),
    onPrimaryContainer = Color(0xFFE5DBFF),
    secondary = Color(0xFF22D3EE),
    onSecondary = Color(0xFF00363D),
    background = Color(0xFF0E0F1A),
    onBackground = Color(0xFFE8E9F3),
    surface = Color(0xFF151728),
    onSurface = Color(0xFFE8E9F3),
    surfaceVariant = Color(0xFF1F2135),
    onSurfaceVariant = Color(0xFFA0A3BD),
    surfaceContainerLowest = Color(0xFF0A0B14),
    surfaceContainerLow = Color(0xFF121323),
    surfaceContainer = Color(0xFF171829),
    surfaceContainerHigh = Color(0xFF1C1E33),
    surfaceContainerHighest = Color(0xFF222440),
    outline = Color(0xFF3A3D5C),
    outlineVariant = Color(0xFF2A2C47),
    error = Color(0xFFFF4D6D),
    onError = Color.White
)

@Composable
fun EsportsTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsportsColors,
        typography = Typography,
        content = content
    )
}