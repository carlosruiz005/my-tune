package com.mytune.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.mytune.data.model.ThemeMode
import com.mytune.data.model.TunerSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository using SharedPreferences.
 * 
 * Persists user settings to SharedPreferences and provides reactive
 * access through Flow emissions on changes.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "mytune_settings"
        private const val KEY_SELECTED_TUNING = "selected_tuning"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_CALIBRATION_FREQ = "calibration_frequency"
        private const val KEY_IN_TUNE_THRESHOLD = "in_tune_threshold"
        private const val KEY_NOISE_GATE = "noise_gate_threshold"
        
        private const val DEFAULT_TUNING = "standard"
        private const val DEFAULT_THEME = "SYSTEM"
        private const val DEFAULT_HAPTIC = true
        private const val DEFAULT_CALIBRATION = 440.0
        private const val DEFAULT_THRESHOLD = 5.0
        private const val DEFAULT_NOISE_GATE = 0.1
    }
    
    override fun getSettings(): Flow<TunerSettings> = callbackFlow {
        // Emit initial value
        trySend(loadSettings())
        
        // Listen for changes
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(loadSettings())
        }
        
        prefs.registerOnSharedPreferenceChangeListener(listener)
        
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    
    override suspend fun getCurrentSettings(): TunerSettings {
        return loadSettings()
    }
    
    /**
     * Loads settings from SharedPreferences.
     */
    private fun loadSettings(): TunerSettings {
        return TunerSettings(
            selectedTuningId = prefs.getString(KEY_SELECTED_TUNING, DEFAULT_TUNING) ?: DEFAULT_TUNING,
            themeMode = ThemeMode.fromString(prefs.getString(KEY_THEME_MODE, DEFAULT_THEME) ?: DEFAULT_THEME),
            hapticFeedbackEnabled = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, DEFAULT_HAPTIC),
            calibrationFrequency = prefs.getFloat(KEY_CALIBRATION_FREQ, DEFAULT_CALIBRATION.toFloat()).toDouble(),
            inTuneThresholdCents = prefs.getFloat(KEY_IN_TUNE_THRESHOLD, DEFAULT_THRESHOLD.toFloat()).toDouble(),
            noiseGateThreshold = prefs.getFloat(KEY_NOISE_GATE, DEFAULT_NOISE_GATE.toFloat()).toDouble()
        )
    }
    
    override suspend fun setSelectedTuning(tuningId: String) {
        prefs.edit {
            putString(KEY_SELECTED_TUNING, tuningId)
        }
    }
    
    override suspend fun setThemeMode(themeMode: ThemeMode) {
        prefs.edit {
            putString(KEY_THEME_MODE, themeMode.name)
        }
    }
    
    override suspend fun setHapticFeedback(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_HAPTIC_FEEDBACK, enabled)
        }
    }
    
    override suspend fun setCalibrationFrequency(frequency: Double) {
        require(frequency in 430.0..450.0) { 
            "Calibration frequency must be between 430-450 Hz" 
        }
        prefs.edit {
            putFloat(KEY_CALIBRATION_FREQ, frequency.toFloat())
        }
    }
    
    override suspend fun setInTuneThreshold(cents: Double) {
        require(cents in 1.0..10.0) { 
            "In-tune threshold must be between 1-10 cents" 
        }
        prefs.edit {
            putFloat(KEY_IN_TUNE_THRESHOLD, cents.toFloat())
        }
    }
    
    override suspend fun setNoiseGateThreshold(threshold: Double) {
        require(threshold in 0.0..1.0) { 
            "Noise gate threshold must be between 0.0-1.0" 
        }
        prefs.edit {
            putFloat(KEY_NOISE_GATE, threshold.toFloat())
        }
    }
    
    override suspend fun resetToDefaults() {
        prefs.edit {
            clear()
            putString(KEY_SELECTED_TUNING, DEFAULT_TUNING)
            putString(KEY_THEME_MODE, DEFAULT_THEME)
            putBoolean(KEY_HAPTIC_FEEDBACK, DEFAULT_HAPTIC)
            putFloat(KEY_CALIBRATION_FREQ, DEFAULT_CALIBRATION.toFloat())
            putFloat(KEY_IN_TUNE_THRESHOLD, DEFAULT_THRESHOLD.toFloat())
            putFloat(KEY_NOISE_GATE, DEFAULT_NOISE_GATE.toFloat())
        }
    }
}
