package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    themeColor: AppThemeColor = AppThemeColor.BLUE,
    content: @Composable () -> Unit
) {
    val darkColorScheme = darkColorScheme(
        primary = themeColor.primary,
        onPrimary = Color.White,
        primaryContainer = themeColor.primaryVariant,
        onPrimaryContainer = Color.White,
        secondary = themeColor.primary,
        onSecondary = Color.Black,
        secondaryContainer = themeColor.glowColor,
        onSecondaryContainer = Color.White,
        tertiary = CineYellow,
        onTertiary = Color.Black,
        error = CineRed,
        onError = Color.White,
        background = DarkBg,
        onBackground = DarkTextPrimary,
        surface = DarkSurface,
        onSurface = DarkTextPrimary,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkTextSecondary
    )

    val lightColorScheme = lightColorScheme(
        primary = themeColor.primary,
        onPrimary = Color.White,
        primaryContainer = themeColor.primaryVariant,
        onPrimaryContainer = Color.White,
        secondary = themeColor.primary,
        onSecondary = Color.White,
        secondaryContainer = themeColor.glowColor,
        onSecondaryContainer = Color.Black,
        tertiary = CineYellow,
        onTertiary = Color.Black,
        error = CineRed,
        onError = Color.White,
        background = LightBg,
        onBackground = LightTextPrimary,
        surface = LightSurface,
        onSurface = LightTextPrimary,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightTextSecondary
    )

    val colorScheme = if (darkTheme) darkColorScheme else lightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
