package com.personal.callscribe.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = DeepTeal,
    secondary = Clay,
    tertiary = Moss,
    background = Sand,
    surface = Card,
    surfaceVariant = Mist,
    secondaryContainer = Sun,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = Slate,
    error = Danger,
)

private val DarkScheme = darkColorScheme(
    primary = Mist,
    secondary = Clay,
    tertiary = Moss,
    background = DeepTealDark,
    surface = NightCard,
    surfaceVariant = Color(0xFF223C3A),
    secondaryContainer = Color(0xFF5A3B22),
    onPrimary = DeepTealDark,
    onSecondary = Color.White,
    onBackground = Sand,
    onSurface = Sand,
    onSurfaceVariant = Color(0xFFB5C5C1),
    error = Color(0xFFF7B8BE),
)

/**
 * App Material 3 theme.
 */
@Composable
fun CallScribeTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkScheme else LightScheme,
        typography = CallScribeTypography,
        content = content,
    )
}
