package com.mytune.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Light color scheme for MyTune app.
 */
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    primaryContainer = LightPrimaryVariant,
    secondary = LightSecondary,
    secondaryContainer = LightSecondaryVariant,
    background = LightBackground,
    surface = LightSurface,
    error = LightError,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnSecondary,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onError = LightOnError
)

/**
 * Dark color scheme for MyTune app.
 */
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    primaryContainer = DarkPrimaryVariant,
    secondary = DarkSecondary,
    secondaryContainer = DarkSecondaryVariant,
    background = DarkBackground,
    surface = DarkSurface,
    error = DarkError,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onError = DarkOnError
)

/**
 * CompositionLocal for tuning-specific colors.
 */
val LocalTuningColors = staticCompositionLocalOf { lightTuningColors }

/**
 * CompositionLocal for note display colors.
 */
val LocalNoteColors = staticCompositionLocalOf { lightNoteColors }

/**
 * Main theme composable for MyTune app.
 * 
 * Supports:
 * - Light and dark themes
 * - Dynamic color (Android 12+)
 * - Custom tuning indicator colors
 * - System bars color matching
 * 
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param dynamicColor Whether to use dynamic color (Android 12+)
 * @param content The content to be themed
 */
@Composable
fun MyTuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
    
    // Select tuning and note colors based on theme
    val tuningColors = if (darkTheme) darkTuningColors else lightTuningColors
    val noteColors = if (darkTheme) darkNoteColors else lightNoteColors
    
    // Update system bars to match theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalTuningColors provides tuningColors,
        LocalNoteColors provides noteColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Object to access custom theme colors from composables.
 */
object MyTuneTheme {
    /**
     * Retrieves the current tuning indicator colors.
     */
    val tuningColors: TuningColors
        @Composable
        get() = LocalTuningColors.current
    
    /**
     * Retrieves the current note display colors.
     */
    val noteColors: NoteColors
        @Composable
        get() = LocalNoteColors.current
}
