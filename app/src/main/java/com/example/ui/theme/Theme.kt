package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
  lightColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerBlue,
    onPrimaryContainer = RoyalBlue,
    secondary = VibrantAzure,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainerBlue,
    onSecondaryContainer = VibrantAzure,
    tertiary = OnTertiaryAmber,
    tertiaryContainer = TertiaryAmber,
    background = SurfaceGray,
    onBackground = OnSurfaceDark,
    surface = SurfaceCard,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = OnSurfaceVariant,
    outline = OnSurfaceVariant,
    outlineVariant = OutlineVariant,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = LightColorScheme, typography = Typography, content = content)
}


