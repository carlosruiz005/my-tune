package com.mytune.data.repository

import com.mytune.data.model.ThemeMode
import com.mytune.data.model.TunerSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing app settings and preferences.
 * 
 * Provides reactive access to user settings with persistence.
 */
interface SettingsRepository {
    /**
     * Gets the current settings as a Flow that emits on changes.
     * 
     * @return Flow of current tuner settings
     */
    fun getSettings(): Flow<TunerSettings>
    
    /**
     * Gets the current settings (suspend function).
     * 
     * @return Current tuner settings
     */
    suspend fun getCurrentSettings(): TunerSettings
    
    /**
     * Sets the selected tuning ID.
     * 
     * @param tuningId The ID of the tuning to select
     */
    suspend fun setSelectedTuning(tuningId: String)
    
    /**
     * Sets the app theme mode.
     * 
     * @param themeMode The theme mode to set
     */
    suspend fun setThemeMode(themeMode: ThemeMode)
    
    /**
     * Enables or disables haptic feedback.
     * 
     * @param enabled Whether haptic feedback should be enabled
     */
    suspend fun setHapticFeedback(enabled: Boolean)
    
    /**
     * Sets the A440 calibration frequency.
     * 
     * @param frequency The calibration frequency in Hz (430-450)
     */
    suspend fun setCalibrationFrequency(frequency: Double)
    
    /**
     * Sets the in-tune threshold in cents.
     * 
     * @param cents The threshold in cents (1-10)
     */
    suspend fun setInTuneThreshold(cents: Double)
    
    /**
     * Sets the noise gate threshold.
     * 
     * @param threshold The amplitude threshold (0.0-1.0)
     */
    suspend fun setNoiseGateThreshold(threshold: Double)
    
    /**
     * Resets all settings to defaults.
     */
    suspend fun resetToDefaults()
}
