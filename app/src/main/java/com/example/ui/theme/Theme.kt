package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueDeep,
    onPrimaryContainer = Color.White,
    secondary = PurpleAccent,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceCard,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueContainer,
    onPrimaryContainer = BlueDark,
    secondary = PurpleAccent,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceCard,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    outline = LightBorder
)

@Composable
fun McKaysTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
