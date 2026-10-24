package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Rapport Design System Tokens (1:1 match with web/css/rapport.css)
// =========================================================================

// Light Theme Surface & Text Palette
val RapportBgLight = Color(0xFFEBE8E1)
val RapportSolidLight = Color(0xFFF6F4EF)
val RapportFloatLight = Color(0xFFFDFCF9)
val RapportTextLight = Color(0xFF17191A)
val RapportMutedLight = Color(0xFF676A66)
val RapportFaintLight = Color(0xFF8E918C)
val RapportLineLight = Color(0x1F17191A)
val RapportLine2Light = Color(0x2917191A)
val RapportWellLight = Color(0x0F17191A)
val RapportCreamLight = Color(0xFFFBF8F2)

// Dark Theme Surface & Text Palette
val RapportBgDark = Color(0xFF111412)
val RapportSolidDark = Color(0xFF1C201E)
val RapportFloatDark = Color(0xFF242927)
val RapportTextDark = Color(0xFFF1EEE8)
val RapportMutedDark = Color(0xFFA3A6A0)
val RapportFaintDark = Color(0xFF7D807B)
val RapportLineDark = Color(0x14FFFFFF)
val RapportLine2Dark = Color(0x29FFFFFF)
val RapportWellDark = Color(0x0FFFFFFF)

// Brand Accents
val RapportPine = Color(0xFF2F5C50)
val RapportPineDark = Color(0xFF6CC59A)
val RapportButter = Color(0xFFF2C94C)
val RapportTomato = Color(0xFFC8431F)
val RapportTomatoDark = Color(0xFFD8532D)
val RapportGood = Color(0xFF2F7A52)
val RapportGoodDark = Color(0xFF6CC59A)
val RapportBad = Color(0xFFC8431F)
val RapportBadDark = Color(0xFFF08A68)

// Pill & Dock Tokens
val RapportDockLight = Color(0xFF17191A)
val RapportOnDockLight = Color(0xFFFBF8F2)
val RapportDockDark = Color(0xFF1C201E)
val RapportOnDockDark = Color(0xFFF1EEE8)

val RapportPillLight = Color(0xFF17191A)
val RapportOnPillLight = Color(0xFFFBF8F2)
val RapportPillDark = Color(0xFFF1EEE8)
val RapportOnPillDark = Color(0xFF17191A)

// =========================================================================
// Aliases for Screen Composables (pointing directly to Rapport tokens)
// =========================================================================
val ElectricBlue = Color(0xFF17191A) // Primary ink action button
val RoyalBlue = Color(0xFF17191A)    // Primary ink brand color
val PrimaryContainerBlue = Color(0xFFF6F4EF)
val VibrantAzure = Color(0xFF2F5C50)
val SecondaryBlue = Color(0xFF2F5C50)
val SecondaryContainerBlue = Color(0xFFE8F0EC)

val SurfaceGray = RapportBgLight
val SurfaceCard = RapportSolidLight
val SurfaceContainerLow = RapportBgLight
val SurfaceContainer = RapportSolidLight
val SurfaceContainerHigh = RapportFloatLight
val OnSurfaceDark = RapportTextLight
val OnSurfaceVariant = RapportMutedLight
val OutlineVariant = RapportLineLight
val TertiaryAmber = RapportButter
val OnTertiaryAmber = Color(0xFF17191A)
val ErrorRed = RapportTomato
val ErrorContainer = Color(0xFFFDF0ED)
val SuccessGreen = RapportGood
