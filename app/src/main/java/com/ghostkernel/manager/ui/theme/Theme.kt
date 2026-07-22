package com.ghostkernel.manager.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GhostCyan,
    onPrimary = GhostBlack,
    primaryContainer = GhostCyanDim,
    onPrimaryContainer = GhostWhite,
    secondary = GhostPurple,
    onSecondary = GhostBlack,
    secondaryContainer = GhostCardBg,
    onSecondaryContainer = GhostPurple,
    tertiary = GhostAmber,
    onTertiary = GhostBlack,
    background = GhostBlack,
    onBackground = GhostWhite,
    surface = GhostDarkGray,
    onSurface = GhostWhite,
    surfaceVariant = GhostCardBg,
    onSurfaceVariant = GhostGray,
    error = GhostRed,
    onError = GhostBlack,
    outline = GhostGray,
    outlineVariant = GhostCyanDim,
)

@Composable
fun GhostKernelTheme(content: @Composable () -> Unit) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
