//
//  SettingsRepository.swift
//  MyTune
//
//  Repository for managing user settings persistence using UserDefaults.
//

import Foundation
import Combine

/// Protocol defining settings repository operations
protocol SettingsRepositoryProtocol {
    /// Publisher for observing settings changes
    var settingsPublisher: AnyPublisher<TunerSettings, Never> { get }
    
    /// Returns the current settings
    func getSettings() -> TunerSettings
    
    /// Updates the settings
    func updateSettings(_ settings: TunerSettings)
    
    /// Updates only the selected tuning ID
    func setSelectedTuning(_ tuningId: String)
    
    /// Updates only the theme mode
    func setThemeMode(_ mode: ThemeMode)
    
    /// Updates only the haptic feedback preference
    func setHapticFeedback(_ enabled: Bool)
}

/// Repository implementation using UserDefaults for persistence
class SettingsRepository: SettingsRepositoryProtocol {
    private let userDefaults: UserDefaults
    private let settingsKey = "TunerSettings"
    private let settingsSubject: CurrentValueSubject<TunerSettings, Never>
    
    var settingsPublisher: AnyPublisher<TunerSettings, Never> {
        return settingsSubject.eraseToAnyPublisher()
    }
    
    init(userDefaults: UserDefaults = .standard) {
        self.userDefaults = userDefaults
        
        // Load settings or use default
        let loadedSettings = Self.loadSettings(from: userDefaults, key: settingsKey)
        self.settingsSubject = CurrentValueSubject<TunerSettings, Never>(loadedSettings)
    }
    
    func getSettings() -> TunerSettings {
        return settingsSubject.value
    }
    
    func updateSettings(_ settings: TunerSettings) {
        saveSettings(settings)
        settingsSubject.send(settings)
    }
    
    func setSelectedTuning(_ tuningId: String) {
        var settings = getSettings()
        settings = TunerSettings(
            selectedTuningId: tuningId,
            themeMode: settings.themeMode,
            enableHapticFeedback: settings.enableHapticFeedback,
            noiseGateThreshold: settings.noiseGateThreshold,
            frequencyUpdateRate: settings.frequencyUpdateRate
        )
        updateSettings(settings)
    }
    
    func setThemeMode(_ mode: ThemeMode) {
        var settings = getSettings()
        settings = TunerSettings(
            selectedTuningId: settings.selectedTuningId,
            themeMode: mode,
            enableHapticFeedback: settings.enableHapticFeedback,
            noiseGateThreshold: settings.noiseGateThreshold,
            frequencyUpdateRate: settings.frequencyUpdateRate
        )
        updateSettings(settings)
    }
    
    func setHapticFeedback(_ enabled: Bool) {
        var settings = getSettings()
        settings = TunerSettings(
            selectedTuningId: settings.selectedTuningId,
            themeMode: settings.themeMode,
            enableHapticFeedback: enabled,
            noiseGateThreshold: settings.noiseGateThreshold,
            frequencyUpdateRate: settings.frequencyUpdateRate
        )
        updateSettings(settings)
    }
    
    // MARK: - Private Methods
    
    private static func loadSettings(from userDefaults: UserDefaults, key: String) -> TunerSettings {
        guard let data = userDefaults.data(forKey: key) else {
            return .default
        }
        
        do {
            let decoder = JSONDecoder()
            let settings = try decoder.decode(TunerSettings.self, from: data)
            return settings
        } catch {
            print("Error decoding settings: \(error)")
            return .default
        }
    }
    
    private func saveSettings(_ settings: TunerSettings) {
        do {
            let encoder = JSONEncoder()
            let data = try encoder.encode(settings)
            userDefaults.set(data, forKey: settingsKey)
        } catch {
            print("Error encoding settings: \(error)")
        }
    }
}
