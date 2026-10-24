package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
    darkColorScheme(
        primary = RapportPillDark,
        onPrimary = RapportOnPillDark,
        primaryContainer = RapportSolidDark,
        onPrimaryContainer = RapportTextDark,
        secondary = RapportPineDark,
        onSecondary = Color(0xFF17191A),
        secondaryContainer = Color(0xFF1E3831),
        onSecondaryContainer = Color(0xFFD1FAE5),
        tertiary = RapportButter,
        onTertiary = Color(0xFF17191A),
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
        onError = Color(0xFF17191A),
        errorContainer = Color(0xFF7F1D1D),
        surfaceContainerLow = RapportBgDark,
        surfaceContainer = RapportSolidDark,
        surfaceContainerHigh = RapportFloatDark,
        inverseSurface = RapportDockDark,
        inverseOnSurface = RapportOnDockDark
    )

private val LightColorScheme =
    lightColorScheme(
        primary = RapportPillLight,
        onPrimary = RapportOnPillLight,
        primaryContainer = RapportSolidLight,
        onPrimaryContainer = RapportTextLight,
        secondary = RapportPine,
        onSecondary = Color(0xFFFBF8F2),
        secondaryContainer = Color(0xFFE8F0EC),
        onSecondaryContainer = RapportPine,
        tertiary = RapportButter,
        onTertiary = Color(0xFF17191A),
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
        onError = Color(0xFFFBF8F2),
        errorContainer = Color(0xFFFDF0ED),
        surfaceContainerLow = RapportBgLight,
        surfaceContainer = RapportSolidLight,
        surfaceContainerHigh = RapportFloatLight,
        inverseSurface = RapportDockLight,
        inverseOnSurface = RapportOnDockLight
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
