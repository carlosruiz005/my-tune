package com.mytune.data.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Fast Fourier Transform (FFT) processor using Cooley-Tukey radix-2 algorithm.
 * 
 * Implements in-place FFT with Hann windowing for audio signal analysis.
 * Optimized for pitch detection with guitar frequency range (80-1000 Hz).
 */
class FFTProcessor(private val bufferSize: Int) {
    
    init {
        require(isPowerOfTwo(bufferSize)) { 
            "Buffer size must be a power of 2, got: $bufferSize" 
        }
        require(bufferSize >= 512) { 
            "Buffer size must be at least 512 samples for guitar pitch detection" 
        }
    }
    
    // Pre-computed Hann window coefficients
    private val hannWindow: FloatArray = FloatArray(bufferSize) { n ->
        (0.5 * (1.0 - cos(2.0 * PI * n / (bufferSize - 1)))).toFloat()
    }
    
    // Pre-computed bit-reversal lookup table
    private val bitReversalTable: IntArray = computeBitReversalTable()
    
    /**
     * Performs FFT on input audio samples.
     * 
     * @param samples Input audio samples (time domain)
     * @return FFTResult containing magnitude spectrum and dominant frequency
     */
    fun process(samples: FloatArray): FFTResult {
        require(samples.size == bufferSize) { 
            "Input size ${samples.size} must match buffer size $bufferSize" 
        }
        
        // Normalize input signal to improve detection of quiet sounds
        val maxAmplitude = samples.maxOfOrNull { kotlin.math.abs(it) } ?: 1f
        val normalizedSamples = if (maxAmplitude > 0.01f) {
            FloatArray(bufferSize) { i -> samples[i] / maxAmplitude }
        } else {
            samples
        }
        
        // Apply Hann window
        val windowed = FloatArray(bufferSize) { i ->
            normalizedSamples[i] * hannWindow[i]
        }
        
        // Prepare complex arrays (real and imaginary parts)
        val real = windowed.copyOf()
        val imag = FloatArray(bufferSize) { 0f }
        
        // Perform in-place FFT
        fft(real, imag)
        
        // Calculate magnitude spectrum
        val magnitudes = FloatArray(bufferSize / 2) { i ->
            sqrt(real[i] * real[i] + imag[i] * imag[i])
        }
        
        return FFTResult(magnitudes)
    }
    
    /**
     * In-place Cooley-Tukey FFT algorithm.
     * 
     * @param real Real part of complex numbers (input/output)
     * @param imag Imaginary part of complex numbers (input/output)
     */
    private fun fft(real: FloatArray, imag: FloatArray) {
        val n = real.size
        
        // Bit-reversal permutation
        for (i in 0 until n) {
            val j = bitReversalTable[i]
            if (i < j) {
                // Swap real parts
                val tempReal = real[i]
                real[i] = real[j]
                real[j] = tempReal
                
                // Swap imaginary parts
                val tempImag = imag[i]
                imag[i] = imag[j]
                imag[j] = tempImag
            }
        }
        
        // Cooley-Tukey decimation-in-time
        var size = 2
        while (size <= n) {
            val halfSize = size / 2
            val step = n / size
            
            for (i in 0 until n step size) {
                for (j in 0 until halfSize) {
                    val k = j * step
                    
                    // Twiddle factor: W = e^(-2πik/n)
                    val angle = -2.0 * PI * k / n
                    val twiddleReal = cos(angle).toFloat()
                    val twiddleImag = kotlin.math.sin(angle).toFloat()
                    
                    // Butterfly operation
                    val evenIdx = i + j
                    val oddIdx = i + j + halfSize
                    
                    val oddReal = real[oddIdx]
                    val oddImag = imag[oddIdx]
                    
                    val productReal = oddReal * twiddleReal - oddImag * twiddleImag
                    val productImag = oddReal * twiddleImag + oddImag * twiddleReal
                    
                    real[oddIdx] = real[evenIdx] - productReal
                    imag[oddIdx] = imag[evenIdx] - productImag
                    
                    real[evenIdx] = real[evenIdx] + productReal
                    imag[evenIdx] = imag[evenIdx] + productImag
                }
            }
            
            size *= 2
        }
    }
    
    /**
     * Computes bit-reversal permutation table for FFT.
     */
    private fun computeBitReversalTable(): IntArray {
        val bits = Integer.numberOfTrailingZeros(bufferSize)
        return IntArray(bufferSize) { i ->
            reverseBits(i, bits)
        }
    }
    
    /**
     * Reverses the lowest 'bits' bits of an integer.
     */
    private fun reverseBits(num: Int, bits: Int): Int {
        var result = 0
        var n = num
        for (i in 0 until bits) {
            result = (result shl 1) or (n and 1)
            n = n shr 1
        }
        return result
    }
    
    /**
     * Checks if a number is a power of 2.
     */
    private fun isPowerOfTwo(n: Int): Boolean {
        return n > 0 && (n and (n - 1)) == 0
    }
}

/**
 * Result of FFT processing.
 * 
 * @property magnitudes Magnitude spectrum (only positive frequencies, size = bufferSize/2)
 */
data class FFTResult(
    val magnitudes: FloatArray
) {
    /**
     * Finds the index of the peak magnitude in the spectrum.
     * 
     * @param startBin Starting bin index (to ignore DC component and low frequencies)
     * @param endBin Ending bin index (to focus on guitar range)
     * @return Index of the bin with maximum magnitude
     */
    fun findPeakBin(startBin: Int = 1, endBin: Int = magnitudes.size): Int {
        require(startBin >= 0 && startBin < magnitudes.size) { 
            "startBin must be in range [0, ${magnitudes.size})" 
        }
        require(endBin > startBin && endBin <= magnitudes.size) { 
            "endBin must be in range ($startBin, ${magnitudes.size}]" 
        }
        
        var peakIndex = startBin
        var peakValue = magnitudes[startBin]
        
        for (i in startBin until endBin) {
            if (magnitudes[i] > peakValue) {
                peakValue = magnitudes[i]
                peakIndex = i
            }
        }
        
        return peakIndex
    }
    
    /**
     * Gets the magnitude at a specific bin index.
     */
    fun getMagnitude(bin: Int): Float {
        return magnitudes.getOrElse(bin) { 0f }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FFTResult) return false
        return magnitudes.contentEquals(other.magnitudes)
    }
    
    override fun hashCode(): Int {
        return magnitudes.contentHashCode()
    }
}
