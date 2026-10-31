package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = BrandGold,
    onPrimary = BrandNavy,
    primaryContainer = BrandNavyVariant,
    onPrimaryContainer = BrandGoldLight,
    secondary = Periwinkle,
    onSecondary = BrandNavy,
    secondaryContainer = Color(0xFF292645),
    onSecondaryContainer = Color(0xFFDFDBF7),
    tertiary = SageTeal,
    onTertiary = BrandNavy,
    tertiaryContainer = Color(0xFF1C3630),
    onTertiaryContainer = Color(0xFFAFE0D5),
    error = CrimsonRed,
    onError = Color.White,
    errorContainer = Color(0xFF4C181A),
    onErrorContainer = Color(0xFFFFB3B4),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD2CCE0),
    outline = DarkOutline
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BrandNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6E9F5),
    onPrimaryContainer = BrandNavy,
    secondary = BrandGold,
    onSecondary = Color(0xFF2E2600),
    secondaryContainer = Color(0xFFF7F2DF),
    onSecondaryContainer = Color(0xFF4A3E00),
    tertiary = SageTeal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDF1EB),
    onTertiaryContainer = Color(0xFF15483C),
    error = CrimsonRed,
    onError = Color.White,
    errorContainer = Color(0xFFFCE8E8),
    onErrorContainer = CrimsonRed,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = OliveBrown,
    outline = LightOutline
  )

@Composable
fun MyApplicationTheme(
  themePreference: String = "SYSTEM",
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val systemInDark = isSystemInDarkTheme()
  val darkTheme = when (themePreference) {
    "LIGHT" -> false
    "DARK" -> true
    else -> systemInDark
  }

  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
