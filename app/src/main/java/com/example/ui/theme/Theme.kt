package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.config.DynamicThemeManager

private val ZadiraRoyalDarkColorScheme = darkColorScheme(
    primary = DynamicThemeManager.currentStyle.goldPrimary,
    onPrimary = Color(0xFF1E0738),
    secondary = DynamicThemeManager.currentStyle.crystalCyan,
    onSecondary = Color(0xFF0A2E6E),
    tertiary = DynamicThemeManager.currentStyle.neonMagenta,
    background = DynamicThemeManager.currentStyle.deepNightBg,
    onBackground = Color.White,
    surface = DynamicThemeManager.currentStyle.velvetCardBg,
    onSurface = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZadiraRoyalDarkColorScheme,
        typography = Typography,
        content = content
    )
}
