//
//  TunerSettings.swift
//  MyTune
//
//  Represents user preferences and app configuration.
//

import Foundation

struct TunerSettings: Codable, Equatable {
    let selectedTuningId: String
    let themeMode: ThemeMode
    let enableHapticFeedback: Bool
    let noiseGateThreshold: Double
    let frequencyUpdateRate: Int
    
    init(selectedTuningId: String, themeMode: ThemeMode, 
         enableHapticFeedback: Bool, noiseGateThreshold: Double, 
         frequencyUpdateRate: Int) {
        precondition((0...1).contains(noiseGateThreshold), "Noise gate must be 0.0-1.0")
        precondition((10...60).contains(frequencyUpdateRate), "Update rate must be 10-60 Hz")
        
        self.selectedTuningId = selectedTuningId
        self.themeMode = themeMode
        self.enableHapticFeedback = enableHapticFeedback
        self.noiseGateThreshold = noiseGateThreshold
        self.frequencyUpdateRate = frequencyUpdateRate
    }
    
    /// Default settings for new installations
    static let `default` = TunerSettings(
        selectedTuningId: "standard",
        themeMode: .system,
        enableHapticFeedback: true,
        noiseGateThreshold: 0.3,
        frequencyUpdateRate: 20
    )
}

enum ThemeMode: String, Codable, CaseIterable {
    case light = "light"
    case dark = "dark"
    case system = "system"
    
    var displayName: String {
        switch self {
        case .light:
            return "Light"
        case .dark:
            return "Dark"
        case .system:
            return "System"
        }
    }
}
