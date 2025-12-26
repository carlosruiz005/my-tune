//
//  IOSAudioProcessor.swift
//  MyTune
//
//  iOS implementation of audio processing interface using Combine.
//

import Foundation
import Combine

/// iOS-specific audio processor implementing the IAudioProcessor protocol
class IOSAudioProcessor {
    // Guitar frequency range (E2 to E4 for standard tuning)
    private static let minGuitarFrequency: Double = 70.0  // Below E2 (82 Hz)
    private static let maxGuitarFrequency: Double = 400.0 // Above E4 (330 Hz)
    private static let minConfidenceThreshold: Double = 0.4 // Minimum confidence for valid detection
    
    private let audioEngine: AudioEngine
    private let fftProcessor: FFTProcessor
    private let pitchDetector: HPSPitchDetector
    private let tuningRepository: TuningRepositoryProtocol
    private let settingsRepository: SettingsRepositoryProtocol
    
    private let pitchResultSubject = PassthroughSubject<PitchResult, Never>()
    private var currentTuning: Tuning
    private var settings: TunerSettings
    private var audioBuffer: [Float] = []
    private var cancellables = Set<AnyCancellable>()
    
    // Throttle to control emission rate
    private var lastEmitTime: Date = Date()
    
    var sampleRate: Int {
        return Int(audioEngine.sampleRate)
    }
    
    var bufferSize: Int {
        return 4096
    }
    
    init(tuningRepository: TuningRepositoryProtocol,
         settingsRepository: SettingsRepositoryProtocol) {
        self.tuningRepository = tuningRepository
        self.settingsRepository = settingsRepository
        
        self.audioEngine = AudioEngine()
        self.fftProcessor = FFTProcessor(bufferSize: 4096)
        self.pitchDetector = HPSPitchDetector(
            sampleRate: 44100,
            bufferSize: 4096
        )
        
        // Load initial settings and tuning
        self.settings = settingsRepository.getSettings()
        self.currentTuning = tuningRepository.getTuning(byId: settings.selectedTuningId)
            ?? tuningRepository.getStandardTuning()
        
        // Observe settings changes
        settingsRepository.settingsPublisher
            .sink { [weak self] newSettings in
                self?.settings = newSettings
                if let newTuning = self?.tuningRepository.getTuning(byId: newSettings.selectedTuningId) {
                    self?.currentTuning = newTuning
                }
            }
            .store(in: &cancellables)
    }
    
    /// Starts audio processing
    func start() async -> Result<Void, AudioError> {
        // Request permission first
        let permissionResult = await audioEngine.requestPermission()
        guard case .success = permissionResult else {
            return permissionResult
        }
        
        // Start audio capture
        audioBuffer.removeAll()
        
        let result = audioEngine.start { [weak self] samples in
            self?.processSamples(samples)
        }
        
        return result
    }
    
    /// Stops audio processing
    func stop() {
        audioEngine.stop()
        audioBuffer.removeAll()
    }
    
    /// Returns whether audio processing is active
    func isRunning() -> Bool {
        return audioEngine.isRunning
    }
    
    /// Publisher for pitch results
    func observePitchResults() -> AnyPublisher<PitchResult, Never> {
        return pitchResultSubject.eraseToAnyPublisher()
    }
    
    // MARK: - Private Methods
    
    private func processSamples(_ samples: [Float]) {
        // Accumulate samples until we have enough for FFT
        audioBuffer.append(contentsOf: samples)
        
        guard audioBuffer.count >= bufferSize else { return }
        
        // Extract buffer for processing
        let processingBuffer = Array(audioBuffer.prefix(bufferSize))
        audioBuffer.removeFirst(min(samples.count, audioBuffer.count))
        
        // Throttle based on update rate
        let now = Date()
        let minInterval = 1.0 / Double(settings.frequencyUpdateRate)
        guard now.timeIntervalSince(lastEmitTime) >= minInterval else { return }
        lastEmitTime = now
        
        // Process in background to avoid blocking audio thread
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            self?.performPitchDetection(on: processingBuffer)
        }
    }
    
    private func performPitchDetection(on samples: [Float]) {
        // 1. Perform FFT
        let magnitudes = fftProcessor.process(samples)
        
        // 2. Detect pitch using HPS
        let (frequency, confidence) = pitchDetector.detectPitch(from: magnitudes)
        
        // 3. Apply noise gate and minimum confidence threshold
        guard confidence >= max(settings.noiseGateThreshold, Self.minConfidenceThreshold) else {
            emitSilentResult(confidence: confidence)
            return
        }
        
        // 4. Validate frequency
        guard let detectedFrequency = frequency else {
            emitSilentResult(confidence: confidence)
            return
        }
        
        // 5. Check if frequency is in valid guitar range
        guard detectedFrequency >= Self.minGuitarFrequency && 
              detectedFrequency <= Self.maxGuitarFrequency else {
            // Frequency out of guitar range, emit silent result
            emitSilentResult(confidence: confidence)
            return
        }
        
        let (note, octave) = FrequencyConverter.frequencyToNote(detectedFrequency)
        let cents = FrequencyConverter.calculateCents(detectedFrequency)
        
        // 6. Match with target string
        let targetString = findTargetString(note: note, octave: octave, frequency: detectedFrequency)
        
        // 7. Create and emit result
        let result = PitchResult(
            frequency: detectedFrequency,
            note: note,
            octave: octave,
            cents: cents,
            confidence: confidence,
            timestamp: Int64(Date().timeIntervalSince1970 * 1000),
            targetString: targetString
        )
        
        DispatchQueue.main.async { [weak self] in
            self?.pitchResultSubject.send(result)
        }
    }
    
    private func emitSilentResult(confidence: Double) {
        let result = PitchResult(
            frequency: nil,
            note: nil,
            octave: nil,
            cents: nil,
            confidence: confidence,
            timestamp: Int64(Date().timeIntervalSince1970 * 1000),
            targetString: nil
        )
        
        DispatchQueue.main.async { [weak self] in
            self?.pitchResultSubject.send(result)
        }
    }
    
    private func findTargetString(note: String, octave: Int, frequency: Double) -> GuitarString? {
        // Find string with matching note and octave within ±50 cents
        for string in currentTuning.strings {
            if string.note == note && string.octave == octave {
                let cents = FrequencyConverter.centsBetween(
                    detected: frequency,
                    target: string.frequency
                )
                
                // Only match if within ±50 cents (half semitone)
                if abs(cents) <= 50 {
                    return string
                }
            }
        }
        
        return nil
    }
}
