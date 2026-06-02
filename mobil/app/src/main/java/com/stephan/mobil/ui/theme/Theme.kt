package com.stephan.mobil.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PremiumDarkColorScheme = darkColorScheme(
    primary = NeonEmerald,
    onPrimary = ObsidianBlack,
    primaryContainer = MutedSlate,
    onPrimaryContainer = PremiumWhite,
    secondary = LightSlate,
    onSecondary = PremiumWhite,
    background = ObsidianBlack,
    onBackground = PremiumWhite,
    surface = DarkSlate,
    onSurface = PremiumWhite,
    surfaceVariant = MutedSlate,
    onSurfaceVariant = LightSlate,
    error = BrandCrimson,
    onError = PremiumWhite
)

@Composable
fun MobilTheme(
    darkTheme: Boolean = true, // Force premium dark theme by default
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our brand color scheme
    content: @Composable () -> Unit
) {
    val colorScheme = PremiumDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ObsidianBlack.toArgb()
            window.navigationBarColor = ObsidianBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}