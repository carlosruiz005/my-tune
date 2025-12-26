package com.mytune.data.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Audio recorder using Android AudioRecord API.
 * 
 * Captures raw audio samples from the microphone on a background thread
 * and provides them for FFT processing.
 */
class AudioRecorder(
    private val context: Context,
    val sampleRate: Int = 44100,
    val bufferSize: Int = 4096
) {
    
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _audioSamples = MutableStateFlow<FloatArray>(FloatArray(0))
    val audioSamples: StateFlow<FloatArray> = _audioSamples.asStateFlow()
    
    // Calculate minimum buffer size required by Android
    private val minBufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    
    // Use the larger of our desired buffer size and Android's minimum
    private val actualBufferSize = maxOf(bufferSize * 2, minBufferSize)
    
    /**
     * Checks if microphone permission is granted.
     */
    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Starts audio recording.
     * 
     * @return Result indicating success or failure with error message
     */
    suspend fun start(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check permission
            if (!hasPermission()) {
                return@withContext Result.failure(
                    SecurityException("Microphone permission not granted")
                )
            }
            
            // Stop existing recording if any
            stop()
            
            // Initialize AudioRecord
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                actualBufferSize
            )
            
            val recorder = audioRecord
            if (recorder == null || recorder.state != AudioRecord.STATE_INITIALIZED) {
                return@withContext Result.failure(
                    IllegalStateException("Failed to initialize AudioRecord")
                )
            }
            
            // Start recording
            recorder.startRecording()
            _isRecording.value = true
            
            // Start recording loop on background thread
            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                recordLoop(recorder)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Stops audio recording.
     */
    fun stop() {
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null
        
        audioRecord?.apply {
            try {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                }
                release()
            } catch (e: Exception) {
                // Ignore errors during cleanup
            }
        }
        audioRecord = null
    }
    
    /**
     * Main recording loop that reads audio samples continuously.
     */
    private suspend fun recordLoop(recorder: AudioRecord) = coroutineScope {
        val buffer = ShortArray(bufferSize)
        val floatBuffer = FloatArray(bufferSize)
        
        while (isActive && _isRecording.value) {
            // Read audio data
            val samplesRead = recorder.read(buffer, 0, bufferSize)
            
            if (samplesRead > 0) {
                // Convert short samples to float (-1.0 to 1.0)
                for (i in 0 until samplesRead) {
                    floatBuffer[i] = buffer[i] / 32768f
                }
                
                // Only emit full buffers for FFT processing
                if (samplesRead == bufferSize) {
                    _audioSamples.value = floatBuffer.copyOf()
                }
            } else if (samplesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                // Recording stopped or error occurred
                break
            }
            
            // Delay to control processing rate (~20 updates per second)
            delay(50)
        }
    }
    
    /**
     * Releases all resources.
     */
    fun release() {
        stop()
    }
}
