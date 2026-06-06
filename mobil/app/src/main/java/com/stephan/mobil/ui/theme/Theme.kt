package com.stephan.mobil.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

@Composable
fun MobilTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = SCpayDarkColorScheme,
        typography = Typography,
        content = content
    )
}
