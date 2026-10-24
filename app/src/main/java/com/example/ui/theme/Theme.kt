package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = RapportPine,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E3831),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = RapportButter,
    tertiaryContainer = Color(0xFF78350F),
    background = RapportBgDark,
    onBackground = RapportTextDark,
    surface = RapportSolidDark,
    onSurface = RapportTextDark,
    surfaceVariant = RapportFloatDark,
    onSurfaceVariant = RapportMutedDark,
    outline = RapportMutedDark,
    outlineVariant = RapportLineDark,
    error = RapportBadDark,
    errorContainer = Color(0xFF7F1D1D),
    surfaceContainerLow = RapportBgDark,
    surfaceContainer = RapportSolidDark,
    surfaceContainerHigh = RapportFloatDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = RapportPine,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6F4EA),
    onSecondaryContainer = RapportPine,
    tertiary = RapportButter,
    tertiaryContainer = Color(0xFFFEF3C7),
    background = RapportBgLight,
    onBackground = RapportTextLight,
    surface = RapportSolidLight,
    onSurface = RapportTextLight,
    surfaceVariant = RapportFloatLight,
    onSurfaceVariant = RapportMutedLight,
    outline = RapportMutedLight,
    outlineVariant = RapportLineLight,
    error = RapportTomato,
    errorContainer = Color(0xFFFEE2E2),
    surfaceContainerLow = RapportBgLight,
    surfaceContainer = RapportSolidLight,
    surfaceContainerHigh = RapportFloatLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
