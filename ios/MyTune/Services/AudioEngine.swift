//
//  AudioEngine.swift
//  MyTune
//
//  Manages audio capture using AVAudioEngine.
//

import Foundation
import AVFoundation

/// Errors that can occur during audio engine operations
enum AudioError: Error {
    case microphonePermissionDenied
    case audioHardwareUnavailable
    case audioEngineInitializationFailed
    case audioSessionConfigurationFailed
    
    var localizedDescription: String {
        switch self {
        case .microphonePermissionDenied:
            return "Microphone permission denied. Please enable microphone access in Settings."
        case .audioHardwareUnavailable:
            return "Microphone unavailable or in use by another app."
        case .audioEngineInitializationFailed:
            return "Failed to initialize audio engine."
        case .audioSessionConfigurationFailed:
            return "Failed to configure audio session."
        }
    }
}

/// Manages audio capture and provides raw audio samples
class AudioEngine {
    private let engine: AVAudioEngine
    private let inputNode: AVAudioInputNode
    private var audioFormat: AVAudioFormat?
    
    var sampleRate: Double {
        return audioFormat?.sampleRate ?? 44100.0
    }
    
    var isRunning: Bool {
        return engine.isRunning
    }
    
    init() {
        self.engine = AVAudioEngine()
        self.inputNode = engine.inputNode
    }
    
    /// Requests microphone permission
    /// - Returns: Result indicating success or permission denial
    func requestPermission() async -> Result<Void, AudioError> {
        let status = AVAudioSession.sharedInstance().recordPermission
        
        switch status {
        case .granted:
            return .success(())
        case .denied:
            return .failure(.microphonePermissionDenied)
        case .undetermined:
            return await withCheckedContinuation { continuation in
                AVAudioSession.sharedInstance().requestRecordPermission { granted in
                    if granted {
                        continuation.resume(returning: .success(()))
                    } else {
                        continuation.resume(returning: .failure(.microphonePermissionDenied))
                    }
                }
            }
        @unknown default:
            return .failure(.microphonePermissionDenied)
        }
    }
    
    /// Starts audio capture with a callback for audio samples
    /// - Parameter onSamplesReceived: Callback invoked with audio samples
    /// - Returns: Result indicating success or error
    func start(onSamplesReceived: @escaping ([Float]) -> Void) -> Result<Void, AudioError> {
        // Configure audio session
        do {
            let audioSession = AVAudioSession.sharedInstance()
            try audioSession.setCategory(.record, mode: .measurement, options: [])
            try audioSession.setActive(true)
        } catch {
            return .failure(.audioSessionConfigurationFailed)
        }
        
        // Prepare the engine (this initializes the input node properly)
        engine.prepare()
        
        // Get input format after preparing
        let inputFormat = inputNode.inputFormat(forBus: 0)
        
        // Create format for audio processing
        // Use hardware format if valid, otherwise use standard 44.1kHz
        let format: AVAudioFormat
        if inputFormat.sampleRate > 0 && inputFormat.channelCount > 0 {
            // Use hardware format
            format = inputFormat
        } else {
            // Fallback to standard format
            guard let fallbackFormat = AVAudioFormat(commonFormat: .pcmFormatFloat32, 
                                                     sampleRate: 44100, 
                                                     channels: 1, 
                                                     interleaved: false) else {
                return .failure(.audioEngineInitializationFailed)
            }
            format = fallbackFormat
        }
        
        self.audioFormat = format
        
        // Install tap to capture audio
        let bufferSize: AVAudioFrameCount = 4096
        
        inputNode.installTap(onBus: 0, bufferSize: bufferSize, format: format) { buffer, _ in
            // Convert audio buffer to Float array
            guard let channelData = buffer.floatChannelData else { return }
            let samples = Array(UnsafeBufferPointer(start: channelData[0], count: Int(buffer.frameLength)))
            onSamplesReceived(samples)
        }
        
        // Start the engine
        do {
            try engine.start()
            return .success(())
        } catch {
            return .failure(.audioEngineInitializationFailed)
        }
    }
    
    /// Stops audio capture
    func stop() {
        if engine.isRunning {
            inputNode.removeTap(onBus: 0)
            engine.stop()
            
            // Deactivate audio session
            try? AVAudioSession.sharedInstance().setActive(false)
        }
    }
}
