package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OmyraColorScheme = darkColorScheme(
    primary = OmyraOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B1E12),
    onPrimaryContainer = Color(0xFFFFDBCE),
    secondary = OmyraOrangeLight,
    onSecondary = Color.White,
    background = OmyraDarkBg,
    onBackground = OmyraTextPrimary,
    surface = OmyraSurface,
    onSurface = OmyraTextPrimary,
    surfaceVariant = OmyraBorder,
    onSurfaceVariant = OmyraTextSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OmyraColorScheme,
        typography = Typography,
        content = content
    )
}
