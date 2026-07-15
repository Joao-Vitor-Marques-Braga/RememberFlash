package com.rememberflash.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnSurfaceDark,
    primaryContainer = PrimaryDark.copy(alpha = 0.3f),
    secondary = SecondaryDark,
    onSecondary = OnSurfaceDark,
    secondaryContainer = SecondaryDark.copy(alpha = 0.3f),
    tertiary = AccentOrange,
    background = SurfaceDark,
    surface = SurfaceVariantDark,
    surfaceVariant = SurfaceElevatedDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorRed,
    onError = OnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = SurfaceLight,
    primaryContainer = PrimaryLight.copy(alpha = 0.3f),
    secondary = SecondaryDark,
    onSecondary = SurfaceLight,
    secondaryContainer = SecondaryLight.copy(alpha = 0.3f),
    tertiary = AccentOrange,
    background = SurfaceLight,
    surface = SurfaceVariantLight,
    surfaceVariant = SurfaceElevatedLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorRed,
    onError = SurfaceLight
)

@Composable
fun RememberFlashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
