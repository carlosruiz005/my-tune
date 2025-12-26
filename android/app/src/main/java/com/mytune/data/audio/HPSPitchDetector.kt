package com.mytune.data.audio

import android.util.Log
import kotlin.math.abs

/**
 * Harmonic Product Spectrum (HPS) pitch detector.
 * 
 * Implements HPS algorithm for robust fundamental frequency detection
 * in the presence of harmonics. Particularly effective for guitar strings
 * which have strong harmonic content.
 */
class HPSPitchDetector(
    private val sampleRate: Int,
    private val bufferSize: Int
) {
    
    companion object {
        private const val TAG = "HPSPitchDetector"
    }
    
    init {
        require(sampleRate > 0) { "Sample rate must be positive" }
        require(bufferSize > 0) { "Buffer size must be positive" }
    }
    
    private val frequencyResolution = sampleRate.toDouble() / bufferSize
    
    // Number of harmonics to consider (reduce for better performance and stability)
    private val numHarmonics = 3
    
    // Minimum confidence threshold for valid detection
    private val minConfidence = 0.3
    
    // Guitar frequency range (low E2 = 82.41 Hz, high E4 = 329.63 Hz + margin)
    private val minFrequency = 75.0  // Hz (below E2 for dropped tunings)
    private val maxFrequency = 400.0 // Hz (including harmonics)
    
    /**
     * Detects pitch from FFT magnitude spectrum using HPS algorithm.
     * 
     * @param fftResult FFT result containing magnitude spectrum
     * @return DetectedPitch with frequency and confidence, or null if no pitch detected
     */
    fun detectPitch(fftResult: FFTResult): DetectedPitch? {
        val magnitudes = fftResult.magnitudes
        
        // Calculate frequency bin range for guitar
        val minBin = (minFrequency / frequencyResolution).toInt().coerceAtLeast(1)
        val maxBin = (maxFrequency / frequencyResolution).toInt().coerceAtMost(magnitudes.size - 1)
        
        if (minBin >= maxBin) {
            return null
        }
        
        // Apply Harmonic Product Spectrum
        val hpsSpectrum = computeHPS(magnitudes, minBin, maxBin)
        
        // Find peak in HPS spectrum
        val peakBin = findPeak(hpsSpectrum, minBin, maxBin)
        if (peakBin == -1) {
            return null
        }
        
        // Refine frequency estimation using parabolic interpolation
        val refinedBin = parabolicInterpolation(hpsSpectrum, peakBin, minBin)
        val frequency = refinedBin * frequencyResolution
        
        // Calculate confidence based on peak prominence
        val confidence = calculateConfidence(hpsSpectrum, peakBin, minBin, maxBin)
        
        Log.d(TAG, "Pitch detected: $frequency Hz (bin: $peakBin, refined: $refinedBin, confidence: $confidence)")
        
        if (confidence < minConfidence) {
            Log.d(TAG, "Confidence $confidence below threshold $minConfidence")
            return null
        }
        
        return DetectedPitch(
            frequency = frequency,
            confidence = confidence,
            peakBin = peakBin
        )
    }
    
    /**
     * Computes Harmonic Product Spectrum by downsampling and multiplying.
     * 
     * HPS enhances the fundamental frequency by exploiting harmonic relationships.
     * For a fundamental at bin k, its harmonics appear at 2k, 3k, 4k, etc.
     * By downsampling and multiplying, harmonics reinforce the fundamental.
     */
    private fun computeHPS(
        magnitudes: FloatArray,
        minBin: Int,
        maxBin: Int
    ): FloatArray {
        val length = maxBin - minBin
        val hps = FloatArray(length)
        
        // Initialize HPS with the first harmonic (original spectrum)
        for (i in 0 until length) {
            val bin = minBin + i
            if (bin < magnitudes.size) {
                hps[i] = magnitudes[bin]
            }
        }
        
        // Multiply downsampled versions of the spectrum
        for (harmonic in 2..numHarmonics) {
            for (i in 0 until length) {
                val bin = minBin + i
                val harmonicBin = bin * harmonic
                
                if (harmonicBin < magnitudes.size) {
                    hps[i] *= magnitudes[harmonicBin]
                } else {
                    // If harmonic is out of range, this bin can't be the fundamental
                    hps[i] = 0f
                }
            }
        }
        
        return hps
    }
    
    /**
     * Finds the peak in the HPS spectrum.
     */
    private fun findPeak(spectrum: FloatArray, minBin: Int, maxBin: Int): Int {
        var peakValue = 0f
        var peakIndex = -1
        
        for (i in spectrum.indices) {
            if (spectrum[i] > peakValue) {
                peakValue = spectrum[i]
                peakIndex = i
            }
        }
        
        return if (peakIndex >= 0) peakIndex + minBin else -1
    }
    
    /**
     * Parabolic interpolation for sub-bin frequency estimation.
     * 
     * Fits a parabola through the peak and its neighbors to estimate
     * the true peak position with sub-bin accuracy.
     */
    private fun parabolicInterpolation(hpsSpectrum: FloatArray, peakBin: Int, minBin: Int): Double {
        // Convert peak bin to relative index in HPS spectrum
        val relativeIndex = peakBin - minBin
        
        // Need neighbors for interpolation
        if (relativeIndex <= 0 || relativeIndex >= hpsSpectrum.size - 1) {
            return peakBin.toDouble()
        }
        
        val alpha = hpsSpectrum[relativeIndex - 1]
        val beta = hpsSpectrum[relativeIndex]
        val gamma = hpsSpectrum[relativeIndex + 1]
        
        // Parabolic interpolation formula
        val offset = 0.5 * (alpha - gamma) / (alpha - 2 * beta + gamma)
        
        // Check for numerical stability
        if (offset.isNaN() || abs(offset) > 1.0) {
            return peakBin.toDouble()
        }
        
        return peakBin + offset
    }
    
    /**
     * Calculates detection confidence based on peak prominence.
     * 
     * Confidence is high when:
     * - Peak is significantly higher than neighbors
     * - Peak is significantly higher than average background level
     */
    private fun calculateConfidence(
        spectrum: FloatArray,
        peakBin: Int,
        minBin: Int,
        maxBin: Int
    ): Double {
        val relativeIndex = peakBin - minBin
        
        if (relativeIndex < 0 || relativeIndex >= spectrum.size) {
            return 0.0
        }
        
        val peakValue = spectrum[relativeIndex]
        if (peakValue <= 0) {
            return 0.0
        }
        
        // Calculate average background level (excluding peak region)
        var sum = 0.0
        var count = 0
        val peakWidth = 5 // Wider exclusion zone around peak
        
        for (i in spectrum.indices) {
            if (abs(i - relativeIndex) > peakWidth) {
                sum += spectrum[i]
                count++
            }
        }
        
        val avgBackground = if (count > 0) sum / count else 0.0
        
        // Confidence based on signal-to-noise ratio
        val snr = if (avgBackground > 0.0001) {
            peakValue / avgBackground.toFloat()
        } else {
            peakValue * 1000f // High confidence if background is very low
        }
        
        // Map SNR to confidence (0.0 to 1.0)
        // SNR > 8 gives confidence near 1.0 (more strict)
        val confidence = (snr / (snr + 8.0)).coerceIn(0.0, 1.0)
        
        return confidence
    }
}

/**
 * Result of pitch detection.
 * 
 * @property frequency Detected fundamental frequency in Hz
 * @property confidence Confidence level (0.0 to 1.0)
 * @property peakBin FFT bin index of the detected peak
 */
data class DetectedPitch(
    val frequency: Double,
    val confidence: Double,
    val peakBin: Int
) {
    init {
        require(frequency >= 0) { "Frequency must be non-negative" }
        require(confidence in 0.0..1.0) { "Confidence must be between 0.0 and 1.0" }
    }
    
    /**
     * Checks if this detection meets a confidence threshold.
     */
    fun isConfident(threshold: Double = 0.5): Boolean {
        return confidence >= threshold
    }
}
