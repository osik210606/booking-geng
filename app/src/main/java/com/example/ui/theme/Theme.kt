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

private val DarkColorScheme = darkColorScheme(
    primary = LightGold,
    onPrimary = DarkNavy,
    primaryContainer = RoyalNavy,
    onPrimaryContainer = PaleGold,
    secondary = PaleGold,
    onSecondary = DarkNavy,
    tertiary = LightGold,
    background = DarkNavy,
    surface = DarkSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = RoyalNavy,
    onSurfaceVariant = PaleGold
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalNavy,
    onPrimary = Color.White,
    primaryContainer = SandSoft,
    onPrimaryContainer = RoyalNavy,
    secondary = LuxuryGold,
    onSecondary = Color.White,
    tertiary = DeepSapphire,
    background = OffWhite,
    surface = Color.White,
    onBackground = RoyalNavy,
    onSurface = RoyalNavy,
    surfaceVariant = SandSoft,
    onSurfaceVariant = RoyalNavy
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
