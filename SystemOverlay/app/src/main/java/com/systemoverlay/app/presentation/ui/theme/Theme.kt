package com.systemoverlay.app.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF2196F3)
val PrimaryDark = Color(0xFF1976D2)
val Accent = Color(0xFF03A9F4)
val Background = Color(0xFF121212)
val Surface = Color(0xFF1E1E1E)
val OnSurface = Color(0xFFFFFFFF)
val CpuColor = Color(0xFF4CAF50)
val GpuColor = Color(0xFFFF9800)
val RamColor = Color(0xFF9C27B0)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Accent,
    background = Background,
    surface = Surface,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onPrimary = OnSurface
)

@Composable
fun SystemOverlayTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
