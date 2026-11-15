package io.github.jpsutton.shakeout.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Felt = Color(0xFF1B5E3B)

private val Light = lightColorScheme(
    primary = Felt,
    onPrimary = Color.White,
    secondaryContainer = Color(0xFFCDE8D6),
    onSecondaryContainer = Color(0xFF0B2A19),
    tertiaryContainer = Color(0xFFFFE9A8),
    onTertiaryContainer = Color(0xFF3B2F00),
    background = Color(0xFFF4F8F5),
    surface = Color(0xFFF4F8F5),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF7FD6A1),
    onPrimary = Color(0xFF00391D),
    secondaryContainer = Color(0xFF1F4D33),
    onSecondaryContainer = Color(0xFFCDE8D6),
    tertiaryContainer = Color(0xFF5A4700),
    onTertiaryContainer = Color(0xFFFFE9A8),
    background = Color(0xFF101A14),
    surface = Color(0xFF101A14),
)

@Composable
fun ShakeoutTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
