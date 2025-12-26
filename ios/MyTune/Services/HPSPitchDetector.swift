//
//  HPSPitchDetector.swift
//  MyTune
//
//  Implements Harmonic Product Spectrum algorithm for pitch detection.
//

import Foundation

/// Detects pitch from FFT magnitude spectrum using Harmonic Product Spectrum (HPS)
class HPSPitchDetector {
    private let sampleRate: Int
    private let bufferSize: Int
    private let minFrequency: Double
    private let maxFrequency: Double
    private let harmonicsCount: Int
    
    /// Initializes the pitch detector
    /// - Parameters:
    ///   - sampleRate: Audio sample rate in Hz
    ///   - bufferSize: FFT buffer size
    ///   - minFrequency: Minimum frequency to detect (default: 70 Hz, below low E)
    ///   - maxFrequency: Maximum frequency to detect (default: 1000 Hz, above high E)
    ///   - harmonicsCount: Number of harmonics to multiply (default: 5)
    init(sampleRate: Int,
         bufferSize: Int,
         minFrequency: Double = 70.0,
         maxFrequency: Double = 1000.0,
         harmonicsCount: Int = 5) {
        self.sampleRate = sampleRate
        self.bufferSize = bufferSize
        self.minFrequency = minFrequency
        self.maxFrequency = maxFrequency
        self.harmonicsCount = harmonicsCount
    }
    
    /// Detects pitch from FFT magnitude spectrum
    /// - Parameter magnitudes: FFT magnitude spectrum from FFTProcessor
    /// - Returns: Detected pitch result with frequency and confidence
    func detectPitch(from magnitudes: [Float]) -> (frequency: Double?, confidence: Double) {
        // Calculate bin range for guitar frequencies
        let minBin = frequencyToBin(minFrequency)
        let maxBin = min(frequencyToBin(maxFrequency), magnitudes.count - 1)
        
        guard minBin < maxBin else {
            return (nil, 0.0)
        }
        
        // 1. Create HPS array by multiplying downsampled spectra
        let hpsLength = maxBin - minBin + 1
        var hps = [Float](repeating: 1.0, count: hpsLength)
        
        // Multiply fundamental and harmonics
        // For each harmonic, we downsample: take every Nth sample and write to position i
        for harmonic in 1...harmonicsCount {
            for i in 0..<hpsLength {
                let harmonicBin = minBin + (i * harmonic)
                
                // Downsample: read from harmonic position, write to fundamental position
                if harmonicBin < magnitudes.count {
                    hps[i] *= magnitudes[harmonicBin]
                }
            }
        }
        
        // 2. Find peak in HPS spectrum
        guard let peakIndex = findPeakIndex(in: hps) else {
            return (nil, 0.0)
        }
        
        let peakBin = minBin + peakIndex
        
        // 3. Refine frequency using parabolic interpolation
        let refinedBin: Double
        if peakIndex > 0 && peakIndex < hps.count - 1 {
            refinedBin = parabolicInterpolation(
                left: Double(hps[peakIndex - 1]),
                center: Double(hps[peakIndex]),
                right: Double(hps[peakIndex + 1]),
                peakBin: Double(peakBin)
            )
        } else {
            refinedBin = Double(peakBin)
        }
        
        let frequency = binToFrequency(refinedBin)
        
        // 4. Calculate confidence based on peak prominence
        let peakValue = hps[peakIndex]
        let meanValue = hps.reduce(0, +) / Float(hps.count)
        let confidence = min(1.0, Double(peakValue / (meanValue * 10.0)))
        
        // Filter out weak signals
        guard confidence >= 0.2 else {
            return (nil, confidence)
        }
        
        return (frequency, confidence)
    }
    
    // MARK: - Private Methods
    
    private func frequencyToBin(_ frequency: Double) -> Int {
        return Int(frequency * Double(bufferSize) / Double(sampleRate))
    }
    
    private func binToFrequency(_ bin: Double) -> Double {
        return bin * Double(sampleRate) / Double(bufferSize)
    }
    
    private func findPeakIndex(in array: [Float]) -> Int? {
        guard !array.isEmpty else { return nil }
        
        var maxIndex = 0
        var maxValue = array[0]
        
        for (index, value) in array.enumerated() {
            if value > maxValue {
                maxValue = value
                maxIndex = index
            }
        }
        
        return maxIndex
    }
    
    /// Parabolic interpolation for sub-bin frequency estimation
    private func parabolicInterpolation(left: Double, center: Double, right: Double, peakBin: Double) -> Double {
        let denominator = left - 2.0 * center + right
        guard abs(denominator) > 1e-10 else {
            return peakBin
        }
        
        let delta = 0.5 * (left - right) / denominator
        return peakBin + delta
    }
}
