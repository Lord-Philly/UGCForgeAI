package com.ugcforge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object UgcColors {
    val Background = Color(0xFF0B0E14)
    val Surface = Color(0xFF131722)
    val SurfaceHigh = Color(0xFF1B2130)
    val Primary = Color(0xFF7C4DFF)
    val Secondary = Color(0xFF00BFA5)
    val Accent = Color(0xFF2979FF)
    val TextPrimary = Color(0xFFE8EAF0)
    val TextSecondary = Color(0xFF9AA4B7)
    val Error = Color(0xFFFF6E6E)
    val Border = Color(0xFF232B3D)
}

private val Dark = darkColorScheme(
    primary = UgcColors.Primary,
    secondary = UgcColors.Secondary,
    tertiary = UgcColors.Accent,
    background = UgcColors.Background,
    surface = UgcColors.Surface,
    surfaceVariant = UgcColors.SurfaceHigh,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = UgcColors.TextPrimary,
    onSurface = UgcColors.TextPrimary,
    onSurfaceVariant = UgcColors.TextSecondary,
    error = UgcColors.Error,
    outline = UgcColors.Border,
    outlineVariant = UgcColors.Border,
)

@Composable
fun UgcForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Dark, content = content)
}