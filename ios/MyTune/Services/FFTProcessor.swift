//
//  FFTProcessor.swift
//  MyTune
//
//  Performs Fast Fourier Transform using Apple's Accelerate framework.
//

import Foundation
import Accelerate

/// Performs FFT analysis on audio samples using Cooley-Tukey algorithm via vDSP
class FFTProcessor {
    private let bufferSize: Int
    private let log2n: vDSP_Length
    private let fftSetup: FFTSetup
    
    // Reusable buffers to minimize allocations
    private var window: [Float]
    private var realBuffer: [Float]
    private var imagBuffer: [Float]
    private var complexBuffer: DSPSplitComplex
    private var magnitudes: [Float]
    
    init(bufferSize: Int = 4096) {
        precondition(bufferSize.nonzeroBitCount == 1, "Buffer size must be power of 2")
        
        self.bufferSize = bufferSize
        self.log2n = vDSP_Length(log2(Float(bufferSize)))
        
        // Create FFT setup for reuse
        guard let setup = vDSP_create_fftsetup(log2n, FFTRadix(kFFTRadix2)) else {
            fatalError("Failed to create FFT setup")
        }
        self.fftSetup = setup
        
        // Pre-compute Hann window
        self.window = [Float](repeating: 0, count: bufferSize)
        vDSP_hann_window(&window, vDSP_Length(bufferSize), Int32(vDSP_HANN_NORM))
        
        // Allocate reusable buffers
        self.realBuffer = [Float](repeating: 0, count: bufferSize / 2)
        self.imagBuffer = [Float](repeating: 0, count: bufferSize / 2)
        self.complexBuffer = DSPSplitComplex(realp: &realBuffer, imagp: &imagBuffer)
        self.magnitudes = [Float](repeating: 0, count: bufferSize / 2)
    }
    
    deinit {
        vDSP_destroy_fftsetup(fftSetup)
    }
    
    /// Performs FFT on audio samples and returns magnitude spectrum
    /// - Parameter samples: Input audio samples (must match bufferSize)
    /// - Returns: Array of magnitudes for each frequency bin [0...bufferSize/2]
    func process(_ samples: [Float]) -> [Float] {
        precondition(samples.count == bufferSize, "Sample count must match buffer size")
        
        // 1. Apply Hann window to reduce spectral leakage
        var windowedSamples = [Float](repeating: 0, count: bufferSize)
        vDSP_vmul(samples, 1, window, 1, &windowedSamples, 1, vDSP_Length(bufferSize))
        
        // 2. Convert to split complex format (interleaved real/imag)
        windowedSamples.withUnsafeBytes { ptr in
            let complexPtr = ptr.bindMemory(to: DSPComplex.self)
            vDSP_ctoz(complexPtr.baseAddress!, 2, &complexBuffer, 1, vDSP_Length(bufferSize / 2))
        }
        
        // 3. Perform in-place FFT
        vDSP_fft_zrip(fftSetup, &complexBuffer, 1, log2n, FFTDirection(FFT_FORWARD))
        
        // 4. Calculate magnitudes: sqrt(real^2 + imag^2)
        vDSP_zvmags(&complexBuffer, 1, &magnitudes, 1, vDSP_Length(bufferSize / 2))
        
        // 5. Convert to actual magnitude (sqrt of power)
        var sqrtMagnitudes = [Float](repeating: 0, count: bufferSize / 2)
        var length = Int32(bufferSize / 2)
        vvsqrtf(&sqrtMagnitudes, magnitudes, &length)
        
        // 6. Scale by buffer size for proper amplitude
        var scale = 2.0 / Float(bufferSize)
        vDSP_vsmul(sqrtMagnitudes, 1, &scale, &sqrtMagnitudes, 1, vDSP_Length(bufferSize / 2))
        
        return sqrtMagnitudes
    }
    
    /// Returns the frequency in Hz for a given FFT bin index
    /// - Parameters:
    ///   - bin: FFT bin index
    ///   - sampleRate: Audio sample rate in Hz
    /// - Returns: Frequency in Hz
    func binToFrequency(bin: Int, sampleRate: Int) -> Double {
        return Double(bin) * Double(sampleRate) / Double(bufferSize)
    }
    
    /// Returns the FFT bin index for a given frequency
    /// - Parameters:
    ///   - frequency: Frequency in Hz
    ///   - sampleRate: Audio sample rate in Hz
    /// - Returns: FFT bin index
    func frequencyToBin(frequency: Double, sampleRate: Int) -> Int {
        return Int(frequency * Double(bufferSize) / Double(sampleRate))
    }
}
