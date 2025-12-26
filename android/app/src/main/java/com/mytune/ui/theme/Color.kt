package com.mytune.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Colors
val LightPrimary = Color(0xFF1976D2)
val LightPrimaryVariant = Color(0xFF1565C0)
val LightSecondary = Color(0xFF03DAC6)
val LightSecondaryVariant = Color(0xFF018786)
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFFFFFFF)
val LightError = Color(0xFFB00020)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightOnSecondary = Color(0xFF000000)
val LightOnBackground = Color(0xFF000000)
val LightOnSurface = Color(0xFF000000)
val LightOnError = Color(0xFFFFFFFF)

// Dark Theme Colors
val DarkPrimary = Color(0xFF90CAF9)
val DarkPrimaryVariant = Color(0xFF42A5F5)
val DarkSecondary = Color(0xFF03DAC6)
val DarkSecondaryVariant = Color(0xFF03DAC6)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkError = Color(0xFFCF6679)
val DarkOnPrimary = Color(0xFF000000)
val DarkOnSecondary = Color(0xFF000000)
val DarkOnBackground = Color(0xFFFFFFFF)
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkOnError = Color(0xFF000000)

// Tuning Indicator Colors - Light Theme
val LightInTune = Color(0xFF4CAF50)
val LightSharp = Color(0xFFFF9800)
val LightFlat = Color(0xFFFF9800)
val LightVerySharp = Color(0xFFF44336)
val LightVeryFlat = Color(0xFFF44336)
val LightNeutral = Color(0xFF9E9E9E)

// Tuning Indicator Colors - Dark Theme
val DarkInTune = Color(0xFF66BB6A)
val DarkSharp = Color(0xFFFFA726)
val DarkFlat = Color(0xFFFFA726)
val DarkVerySharp = Color(0xFFEF5350)
val DarkVeryFlat = Color(0xFFEF5350)
val DarkNeutral = Color(0xFF757575)

// Note Display Colors - Light Theme
val LightNoteDetected = Color(0xFF212121)
val LightNoteTarget = Color(0xFF757575)

// Note Display Colors - Dark Theme
val DarkNoteDetected = Color(0xFFFFFFFF)
val DarkNoteTarget = Color(0xFFBDBDBD)

/**
 * Data class containing tuning-specific colors for the indicator.
 */
data class TuningColors(
    val inTune: Color,
    val sharp: Color,
    val flat: Color,
    val verySharp: Color,
    val veryFlat: Color,
    val neutral: Color
)

/**
 * Data class containing note display colors.
 */
data class NoteColors(
    val detected: Color,
    val target: Color
)

/**
 * Returns the tuning indicator colors for light theme.
 */
val lightTuningColors = TuningColors(
    inTune = LightInTune,
    sharp = LightSharp,
    flat = LightFlat,
    verySharp = LightVerySharp,
    veryFlat = LightVeryFlat,
    neutral = LightNeutral
)

/**
 * Returns the tuning indicator colors for dark theme.
 */
val darkTuningColors = TuningColors(
    inTune = DarkInTune,
    sharp = DarkSharp,
    flat = DarkFlat,
    verySharp = DarkVerySharp,
    veryFlat = DarkVeryFlat,
    neutral = DarkNeutral
)

/**
 * Returns the note display colors for light theme.
 */
val lightNoteColors = NoteColors(
    detected = LightNoteDetected,
    target = LightNoteTarget
)

/**
 * Returns the note display colors for dark theme.
 */
val darkNoteColors = NoteColors(
    detected = DarkNoteDetected,
    target = DarkNoteTarget
)
