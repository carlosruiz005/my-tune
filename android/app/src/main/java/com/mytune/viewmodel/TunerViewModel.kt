package com.mytune.viewmodel

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytune.data.audio.IAudioProcessor
import com.mytune.data.model.PitchResult
import com.mytune.data.model.Tuning
import com.mytune.data.repository.SettingsRepository
import com.mytune.data.repository.TuningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the tuner screen.
 * 
 * Manages audio processing state, pitch detection results,
 * and coordinates between audio processor and UI.
 */
@HiltViewModel
class TunerViewModel @Inject constructor(
    private val audioProcessor: IAudioProcessor,
    private val tuningRepository: TuningRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState: StateFlow<TunerUiState> = _uiState.asStateFlow()
    
    // Current tuning
    val currentTuning: StateFlow<Tuning?> = tuningRepository.getSelectedTuning()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Pitch detection results
    val pitchResult: StateFlow<PitchResult> = audioProcessor.observePitchResults()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PitchResult.noPitchDetected()
        )
    
    // Haptic feedback setting
    val hapticFeedbackEnabled: StateFlow<Boolean> = settingsRepository.getSettings()
        .map { it.hapticFeedbackEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    init {
        // Observe audio processor state
        viewModelScope.launch {
            snapshotFlow { audioProcessor.isRunning() }
                .collect { isRunning ->
                    _uiState.update { it.copy(isProcessing = isRunning) }
                }
        }
    }
    
    /**
     * Starts audio processing and pitch detection.
     */
    fun startTuner() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = audioProcessor.start()
            
            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isProcessing = true,
                        errorMessage = null
                    )
                }
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = when (error) {
                    is SecurityException -> "Microphone permission required"
                    else -> "Failed to start audio: ${error?.message}"
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isProcessing = false,
                        errorMessage = errorMessage
                    )
                }
            }
        }
    }
    
    /**
     * Stops audio processing.
     */
    fun stopTuner() {
        audioProcessor.stop()
        _uiState.update { 
            it.copy(
                isProcessing = false,
                errorMessage = null
            )
        }
    }
    
    /**
     * Clears any error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Checks if microphone permission is likely needed.
     * (Actual permission check happens in the UI layer)
     */
    fun needsPermission(): Boolean {
        return !audioProcessor.isRunning()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopTuner()
    }
}

/**
 * UI state for the tuner screen.
 * 
 * @property isLoading Whether the tuner is starting up
 * @property isProcessing Whether audio is currently being processed
 * @property errorMessage Error message to display, or null if no error
 */
data class TunerUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null
)
