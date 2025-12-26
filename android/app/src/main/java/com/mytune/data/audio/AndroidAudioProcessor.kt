package com.mytune.data.audio

import android.content.Context
import android.util.Log
import com.mytune.data.model.PitchResult
import com.mytune.data.model.Tuning
import com.mytune.data.model.TuningState
import com.mytune.data.repository.SettingsRepository
import com.mytune.data.repository.TuningRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Android implementation of audio processor for pitch detection.
 * 
 * Coordinates AudioRecorder, FFT processing, pitch detection, and note conversion
 * to produce PitchResult emissions for the UI.
 */
@Singleton
class AndroidAudioProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tuningRepository: TuningRepository,
    private val settingsRepository: SettingsRepository
) : IAudioProcessor {
    
    override val sampleRate: Int = 44100
    override val bufferSize: Int = 4096
    
    companion object {
        private const val TAG = "AndroidAudioProcessor"
    }
    
    private val audioRecorder = AudioRecorder(context, sampleRate, bufferSize)
    private val fftProcessor = FFTProcessor(bufferSize)
    private val pitchDetector = HPSPitchDetector(sampleRate, bufferSize)
    
    private var frequencyConverter: FrequencyConverter = FrequencyConverter()
    private var currentTuning: Tuning? = null
    private var processingJob: Job? = null
    
    private val _pitchResults = MutableSharedFlow<PitchResult>(
        replay = 1,
        extraBufferCapacity = 10
    )
    
    private val _isRunning = MutableStateFlow(false)
    
    // Frequency smoothing for stable readings
    private val frequencyHistory = mutableListOf<Double>()
    private val historySize = 3 // Number of readings to average
    private var lastEmittedNote: String? = null
    private var lastEmittedOctave: Int? = null
    private var sameNoteCount = 0
    private val minSameNoteCount = 1 // Show immediately on stable detection
    
    init {
        // Observe settings to update calibration frequency
        CoroutineScope(Dispatchers.Default).launch {
            settingsRepository.getSettings().collect { settings ->
                frequencyConverter = FrequencyConverter(settings.calibrationFrequency)
            }
        }
        
        // Observe tuning changes
        CoroutineScope(Dispatchers.Default).launch {
            tuningRepository.getSelectedTuning().collect { tuning ->
                currentTuning = tuning
            }
        }
    }
    
    override suspend fun start(): Result<Unit> {
        if (_isRunning.value) {
            return Result.success(Unit)
        }
        
        // Check permission
        if (!audioRecorder.hasPermission()) {
            return Result.failure(
                SecurityException("Microphone permission not granted")
            )
        }
        
        // Start audio recording
        val recordResult = audioRecorder.start()
        if (recordResult.isFailure) {
            return recordResult
        }
        
        _isRunning.value = true
        
        // Start processing loop
        processingJob = CoroutineScope(Dispatchers.Default).launch {
            processAudioLoop()
        }
        
        return Result.success(Unit)
    }
    
    override fun stop() {
        _isRunning.value = false
        processingJob?.cancel()
        processingJob = null
        audioRecorder.stop()
        
        // Clear history
        frequencyHistory.clear()
        lastEmittedNote = null
        lastEmittedOctave = null
        sameNoteCount = 0
        
        // Emit "no pitch" result
        CoroutineScope(Dispatchers.Default).launch {
            _pitchResults.emit(PitchResult.noPitchDetected())
        }
    }
    
    override fun isRunning(): Boolean {
        return _isRunning.value
    }
    
    override fun observePitchResults(): Flow<PitchResult> {
        return _pitchResults.asSharedFlow()
    }
    
    /**
     * Main audio processing loop.
     * 
     * Continuously reads audio samples, performs FFT, detects pitch,
     * and emits PitchResult objects.
     */
    private suspend fun processAudioLoop() {
        audioRecorder.audioSamples
            .filter { it.isNotEmpty() && it.size == bufferSize }
            .collect { samples ->
                if (!_isRunning.value) return@collect
                
                try {
                    // Perform FFT
                    val fftResult = fftProcessor.process(samples)
                    
                    // Detect pitch using HPS
                    val detectedPitch = pitchDetector.detectPitch(fftResult)
                    
                    if (detectedPitch != null && detectedPitch.isConfident(0.3)) {
                        // Add to frequency history for smoothing
                        frequencyHistory.add(detectedPitch.frequency)
                        if (frequencyHistory.size > historySize) {
                            frequencyHistory.removeAt(0)
                        }
                        
                        // Use smoothed frequency (median of recent readings)
                        val smoothedFrequency = if (frequencyHistory.size >= 2) {
                            frequencyHistory.sorted()[frequencyHistory.size / 2]
                        } else {
                            detectedPitch.frequency
                        }
                        
                        // Convert frequency to note
                        val noteInfo = frequencyConverter.frequencyToNote(smoothedFrequency)
                        
                        Log.d(TAG, "Detected: ${detectedPitch.frequency} Hz (smoothed: $smoothedFrequency) -> ${noteInfo.noteName}${noteInfo.octave} (confidence: ${detectedPitch.confidence})")
                        
                        // Check if note is stable (same as previous readings)
                        val currentNoteKey = "${noteInfo.noteName}${noteInfo.octave}"
                        val lastNoteKey = if (lastEmittedNote != null && lastEmittedOctave != null) {
                            "$lastEmittedNote$lastEmittedOctave"
                        } else {
                            null
                        }
                        
                        if (currentNoteKey == lastNoteKey) {
                            sameNoteCount++
                        } else {
                            sameNoteCount = 1
                        }
                        
                        // Only emit if note is stable or this is first detection
                        if (sameNoteCount >= minSameNoteCount || lastNoteKey == null) {
                            lastEmittedNote = noteInfo.noteName
                            lastEmittedOctave = noteInfo.octave
                            
                            // Match with current tuning
                            val matchResult = matchWithTuning(noteInfo)
                            
                            // Create PitchResult
                            val pitchResult = PitchResult(
                                detectedFrequency = smoothedFrequency,
                                detectedNote = noteInfo.noteName,
                                detectedOctave = noteInfo.octave,
                                confidence = detectedPitch.confidence,
                                tuningState = matchResult.tuningState,
                                centsDeviation = matchResult.centsDeviation,
                                targetString = matchResult.targetString
                            )
                            
                            Log.d(TAG, "✅ REACHED minSameNoteCount($minSameNoteCount) - Emitting result to UI: ${noteInfo.noteName}${noteInfo.octave} @ $smoothedFrequency Hz")
                            _pitchResults.emit(pitchResult)
                        } else {
                            Log.d(TAG, "⏳ Waiting for stability: $currentNoteKey (count: $sameNoteCount/$minSameNoteCount)")
                        }
                    } else {
                        // No confident pitch detected - clear history
                        frequencyHistory.clear()
                        sameNoteCount = 0
                        
                        if (detectedPitch != null) {
                            Log.d(TAG, "Low confidence: ${detectedPitch.frequency} Hz (confidence: ${detectedPitch.confidence})")
                        }
                        _pitchResults.emit(PitchResult.noPitchDetected())
                    }
                    
                    // Throttle emissions to ~10 Hz (every 100ms) for more stable readings
                    delay(100)
                } catch (e: Exception) {
                    // Log error but continue processing
                    _pitchResults.emit(PitchResult.noPitchDetected())
                }
            }
    }
    
    /**
     * Matches detected note with current tuning preset.
     * 
     * Finds the closest string in the tuning and calculates
     * cents deviation and tuning state.
     */
    private fun matchWithTuning(noteInfo: NoteInfo): TuningMatchResult {
        val tuning = currentTuning
        
        if (tuning == null) {
            return TuningMatchResult(
                tuningState = TuningState.NO_PITCH,
                centsDeviation = 0.0,
                targetString = null
            )
        }
        
        // Find the closest string by frequency
        var closestString = tuning.strings.first()
        var minFreqDiff = abs(noteInfo.frequency - closestString.frequency)
        
        for (string in tuning.strings) {
            val freqDiff = abs(noteInfo.frequency - string.frequency)
            if (freqDiff < minFreqDiff) {
                minFreqDiff = freqDiff
                closestString = string
            }
        }
        
        // Calculate cents deviation from target string
        val centsDeviation = frequencyConverter.calculateCents(
            noteInfo.frequency,
            closestString.frequency
        )
        
        // Only match if within reasonable range (±50 cents / half semitone)
        val targetString = if (abs(centsDeviation) <= 50.0) {
            closestString
        } else {
            null
        }
        
        // Determine tuning state
        val tuningState = TuningState.fromCents(centsDeviation)
        
        return TuningMatchResult(
            tuningState = tuningState,
            centsDeviation = centsDeviation,
            targetString = targetString
        )
    }
    
    /**
     * Result of matching detected pitch with tuning.
     */
    private data class TuningMatchResult(
        val tuningState: TuningState,
        val centsDeviation: Double,
        val targetString: com.mytune.data.model.GuitarString?
    )
    
    /**
     * Releases all resources.
     */
    fun release() {
        stop()
        audioRecorder.release()
    }
}

/**
 * Interface for audio processor (for dependency injection and testing).
 */
interface IAudioProcessor {
    val sampleRate: Int
    val bufferSize: Int
    
    suspend fun start(): Result<Unit>
    fun stop()
    fun isRunning(): Boolean
    fun observePitchResults(): Flow<PitchResult>
}
