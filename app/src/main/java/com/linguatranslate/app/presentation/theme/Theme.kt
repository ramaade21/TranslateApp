package com.linguatranslate.app.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = IndigoPrimary,
    secondary = CoralAccent,
    background = SurfaceLight,
    surface = CardLight,
    onSurface = OnSurfaceLight,
    error = ErrorRed,
)

private val DarkColors = darkColorScheme(
    primary = IndigoPrimaryDark,
    secondary = CoralAccent,
    background = SurfaceDark,
    surface = CardDark,
    onSurface = OnSurfaceDark,
    error = ErrorRed,
)

@Composable
fun LinguaTranslateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LinguaTypography,
        content = content,
    )
}
