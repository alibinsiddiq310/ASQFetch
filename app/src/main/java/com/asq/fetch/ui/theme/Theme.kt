package com.asq.fetch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF3478F6),
    onPrimary = Color.White,
    background = Color(0xFF101827),
    onBackground = Color(0xFFF5F7FA),
    surface = Color(0xFF182334),
    onSurface = Color(0xFFF5F7FA),
    surfaceVariant = Color(0xFF202D40),
    secondary = Color(0xFFA8B2C1),
    error = Color(0xFFD94C4C)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF3478F6),
    onPrimary = Color.White,
    background = Color(0xFFF5F6F8),
    onBackground = Color(0xFF202938),
    surface = Color.White,
    onSurface = Color(0xFF202938),
    surfaceVariant = Color(0xFFF5F6F8),
    secondary = Color(0xFF697586),
    error = Color(0xFFD94C4C)
)

@Composable
fun ASQTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
