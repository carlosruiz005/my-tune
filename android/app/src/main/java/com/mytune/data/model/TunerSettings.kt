package com.mytune.data.model

import kotlinx.serialization.Serializable

/**
 * Represents user settings and preferences for the tuner app.
 * 
 * @property selectedTuningId The ID of the currently selected tuning preset
 * @property themeMode The app theme mode (light, dark, or system)
 * @property hapticFeedbackEnabled Whether to vibrate when string is in tune
 * @property calibrationFrequency A440 calibration frequency in Hz (default 440.0)
 * @property inTuneThresholdCents Threshold in cents for considering a string "in tune" (default 5.0)
 * @property noiseGateThreshold Minimum amplitude threshold to trigger pitch detection (0.0-1.0)
 */
@Serializable
data class TunerSettings(
    val selectedTuningId: String = "standard",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hapticFeedbackEnabled: Boolean = true,
    val calibrationFrequency: Double = 440.0,
    val inTuneThresholdCents: Double = 5.0,
    val noiseGateThreshold: Double = 0.1
) {
    init {
        require(selectedTuningId.isNotBlank()) { "Selected tuning ID cannot be blank" }
        require(calibrationFrequency in 430.0..450.0) { 
            "Calibration frequency must be between 430-450 Hz, got: $calibrationFrequency" 
        }
        require(inTuneThresholdCents in 1.0..10.0) { 
            "In-tune threshold must be between 1-10 cents, got: $inTuneThresholdCents" 
        }
        require(noiseGateThreshold in 0.0..1.0) { 
            "Noise gate threshold must be between 0.0-1.0, got: $noiseGateThreshold" 
        }
    }
    
    /**
     * Creates a copy of these settings with a new selected tuning.
     */
    fun withTuning(tuningId: String): TunerSettings {
        return copy(selectedTuningId = tuningId)
    }
    
    /**
     * Creates a copy of these settings with a new theme mode.
     */
    fun withTheme(mode: ThemeMode): TunerSettings {
        return copy(themeMode = mode)
    }
    
    /**
     * Creates a copy of these settings with haptic feedback toggled.
     */
    fun withHapticFeedback(enabled: Boolean): TunerSettings {
        return copy(hapticFeedbackEnabled = enabled)
    }
    
    /**
     * Validates if these settings are correct.
     */
    fun isValid(): Boolean {
        return try {
            require(selectedTuningId.isNotBlank())
            require(calibrationFrequency in 430.0..450.0)
            require(inTuneThresholdCents in 1.0..10.0)
            require(noiseGateThreshold in 0.0..1.0)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}

/**
 * Enum representing the app theme mode.
 */
@Serializable
enum class ThemeMode {
    /** Light theme */
    LIGHT,
    
    /** Dark theme */
    DARK,
    
    /** Follow system theme setting */
    SYSTEM;
    
    companion object {
        /**
         * Parses a string to ThemeMode, with fallback to SYSTEM.
         */
        fun fromString(value: String): ThemeMode {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                SYSTEM
            }
        }
    }
}
