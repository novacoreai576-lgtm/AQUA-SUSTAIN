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
    primary = DarkPrimary,
    onPrimary = Color(0xFF003833),
    primaryContainer = Color(0xFF00504A),
    onPrimaryContainer = AquaPrimaryContainer,
    secondary = DarkEcoGreen,
    onSecondary = Color(0xFF003912),
    secondaryContainer = Color(0xFF0D5322),
    onSecondaryContainer = EcoGreenContainer,
    tertiary = DarkWaterBlue,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AquaPrimary,
    onPrimary = AquaOnPrimary,
    primaryContainer = AquaPrimaryContainer,
    onPrimaryContainer = AquaOnPrimaryContainer,
    secondary = EcoGreen,
    onSecondary = Color.White,
    secondaryContainer = EcoGreenContainer,
    onSecondaryContainer = EcoOnGreenContainer,
    tertiary = WaterBlue,
    background = BackgroundCanvas,
    surface = SurfaceCard,
    onBackground = NeutralDark,
    onSurface = NeutralDark,
  )

@Composable
fun AquaSustainTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use intentional brand colors
  content: @Composable () -> Unit,
) {
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

