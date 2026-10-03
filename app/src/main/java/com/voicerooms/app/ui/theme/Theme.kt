package com.voicerooms.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VoiceRoomsColors = darkColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = PurpleDark,
    onPrimaryContainer = Color.White,
    secondary = Cyan,
    onSecondary = Color.Black,
    tertiary = Pink,
    background = BgDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    outline = Border,
    error = Red,
    onError = Color.White,
)

@Composable
fun VoiceRoomsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // التطبيق داكن دائمًا (تصميم مقصود)
    MaterialTheme(
        colorScheme = VoiceRoomsColors,
        typography = AppTypography,
        content = content,
    )
}
