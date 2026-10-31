package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Brand Palette (from Brand Identity Guide)
// ==========================================
val OliveBrown = Color(0xFF7F745B)         // #7F745B - Earth Olive / Muted Outline
val BrandGold = Color(0xFFBFA95B)          // #BFA95B - Primary Gem Gold
val BrandGoldLight = Color(0xFFD4C787)     // #D4C787 - Soft Sand Gold / Highlights
val SageTeal = Color(0xFF82C0AF)           // #82C0AF - Sage Mint / Fresh Accent
val Periwinkle = Color(0xFF8980BF)         // #8980BF - Periwinkle Lavender Accent
val BrandNavy = Color(0xFF171F40)          // #171F40 - Dominant Midnight Navy
val CrimsonRed = Color(0xFFC34043)         // #C34043 - Crimson / Error / Alert

// Additional tonal shades for surfaces and cards
val BrandNavyElevated = Color(0xFF1D264F)
val BrandNavyVariant = Color(0xFF273263)
val BrandNavyOutline = Color(0xFF38467D)

// Backward-compatibility aliases mapped directly to brand colors
val AmberAccent = BrandGold
val EmeraldScience = SageTeal
val BlueCoding = BrandNavy
val PurpleMath = Periwinkle
val RoseNews = CrimsonRed
val PrimaryIndigo = BrandNavy
val PrimaryIndigoLight = BrandGold

// Dark Theme Surfaces (Deep Midnight Navy #171F40)
val DarkBackground = BrandNavy
val DarkSurface = BrandNavyElevated
val DarkSurfaceVariant = BrandNavyVariant
val DarkOnBackground = Color(0xFFFAF9F5)
val DarkOnSurface = Color(0xFFFAF9F5)
val DarkOutline = OliveBrown

// Light Theme Surfaces (Warm Sand & Clean Paper)
val LightBackground = Color(0xFFF9F8F4)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEDE9DC)
val LightOnBackground = BrandNavy
val LightOnSurface = BrandNavy
val LightOutline = Color(0xFFC5BFA8)
