package com.stephan.mobil.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalDarkMode = staticCompositionLocalOf { false }
val LocalBrandColor = staticCompositionLocalOf { BrandPrimary }

private val SCpayDarkColorScheme = darkColorScheme(
    primary                = BrandPrimary,
    onPrimary              = BrandOnPrimary,
    primaryContainer       = BrandPrimaryDark,
    onPrimaryContainer     = TextOnPrimary,
    secondary              = TextSecondary,
    onSecondary            = TextPrimary,
    secondaryContainer     = BgSurfaceHigh,
    onSecondaryContainer   = TextPrimary,
    background             = BgBase,
    onBackground           = TextPrimary,
    surface                = BgSurface,
    onSurface              = TextPrimary,
    surfaceVariant         = BgSurfaceElevated,
    onSurfaceVariant       = TextSecondary,
    error                  = SemanticDanger,
    onError                = TextOnPrimary,
    outline                = BgSurfaceTop,
)

private val SCpayLightColorScheme = lightColorScheme(
    primary                = BrandPrimary,
    onPrimary              = BrandOnPrimary,
    primaryContainer       = BrandPrimaryLight,
    onPrimaryContainer     = TextOnPrimary,
    secondary              = Color(0xFF737780),
    onSecondary            = Color(0xFF17181C),
    secondaryContainer     = LineColor,
    onSecondaryContainer   = Color(0xFF17181C),
    background             = Color.White,
    onBackground           = Color(0xFF17181C),
    surface                = SoftBackground,
    onSurface              = Color(0xFF17181C),
    surfaceVariant         = LightBackground,
    onSurfaceVariant       = Color(0xFF737780),
    error                  = SemanticDanger,
    onError                = TextOnPrimary,
    outline                = Color(0xFFE5E7EB),
)

@Composable
fun MobilTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SCpayDarkColorScheme else SCpayLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkMode provides darkTheme,
        LocalBrandColor provides if (darkTheme) BrandPrimary else Color(0xFF17181C)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
