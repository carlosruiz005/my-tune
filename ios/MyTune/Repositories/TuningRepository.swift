//
//  TuningRepository.swift
//  MyTune
//
//  Repository for managing guitar tuning presets.
//

import Foundation
import Combine

/// Protocol defining tuning repository operations
protocol TuningRepositoryProtocol {
    /// Returns all available tuning presets
    func getAllTunings() -> [Tuning]
    
    /// Returns a specific tuning by its ID
    func getTuning(byId id: String) -> Tuning?
    
    /// Returns the standard tuning as default
    func getStandardTuning() -> Tuning
}

/// Repository implementation that loads tuning presets from JSON
class TuningRepository: TuningRepositoryProtocol {
    private var tunings: [Tuning] = []
    
    init() {
        loadTunings()
    }
    
    func getAllTunings() -> [Tuning] {
        return tunings
    }
    
    func getTuning(byId id: String) -> Tuning? {
        return tunings.first { $0.id == id }
    }
    
    func getStandardTuning() -> Tuning {
        return getTuning(byId: "standard") ?? createFallbackStandardTuning()
    }
    
    // MARK: - Private Methods
    
    private func loadTunings() {
        // Try to load from shared presets.json
        guard let url = Bundle.main.url(forResource: "presets", withExtension: "json", subdirectory: "Resources") else {
            print("Warning: presets.json not found, using fallback tunings")
            tunings = createFallbackTunings()
            return
        }
        
        do {
            let data = try Data(contentsOf: url)
            let decoder = JSONDecoder()
            let presetsWrapper = try decoder.decode(PresetsWrapper.self, from: data)
            
            // Convert from JSON format to our model format
            tunings = presetsWrapper.tunings.compactMap { convertJSONTuning($0) }
            
            if tunings.isEmpty {
                print("Warning: No tunings loaded from JSON, using fallback")
                tunings = createFallbackTunings()
            }
        } catch {
            print("Error loading tunings: \(error)")
            tunings = createFallbackTunings()
        }
    }
    
    private func convertJSONTuning(_ jsonTuning: JSONTuning) -> Tuning? {
        let guitarStrings = jsonTuning.strings.map { jsonString in
            GuitarString(
                position: jsonString.number,
                note: jsonString.note,
                octave: jsonString.octave,
                frequency: jsonString.frequency
            )
        }
        
        guard guitarStrings.count == 6 else {
            return nil
        }
        
        return Tuning(
            id: jsonTuning.id,
            name: jsonTuning.name,
            displayName: [:],
            strings: guitarStrings
        )
    }
    
    private func createFallbackTunings() -> [Tuning] {
        return [
            createFallbackStandardTuning(),
            createDropDTuning(),
            createHalfStepDownTuning(),
            createFullStepDownTuning(),
            createDropCTuning()
        ]
    }
    
    private func createFallbackStandardTuning() -> Tuning {
        return Tuning(
            id: "standard",
            name: "Standard",
            strings: [
                GuitarString(position: 6, note: "E", octave: 2, frequency: 82.41),
                GuitarString(position: 5, note: "A", octave: 2, frequency: 110.00),
                GuitarString(position: 4, note: "D", octave: 3, frequency: 146.83),
                GuitarString(position: 3, note: "G", octave: 3, frequency: 196.00),
                GuitarString(position: 2, note: "B", octave: 3, frequency: 246.94),
                GuitarString(position: 1, note: "E", octave: 4, frequency: 329.63)
            ]
        )
    }
    
    private func createDropDTuning() -> Tuning {
        return Tuning(
            id: "drop-d",
            name: "Drop D",
            strings: [
                GuitarString(position: 6, note: "D", octave: 2, frequency: 73.42),
                GuitarString(position: 5, note: "A", octave: 2, frequency: 110.00),
                GuitarString(position: 4, note: "D", octave: 3, frequency: 146.83),
                GuitarString(position: 3, note: "G", octave: 3, frequency: 196.00),
                GuitarString(position: 2, note: "B", octave: 3, frequency: 246.94),
                GuitarString(position: 1, note: "E", octave: 4, frequency: 329.63)
            ]
        )
    }
    
    private func createHalfStepDownTuning() -> Tuning {
        return Tuning(
            id: "half-step-down",
            name: "Half Step Down",
            strings: [
                GuitarString(position: 6, note: "Eb", octave: 2, frequency: 77.78),
                GuitarString(position: 5, note: "Ab", octave: 2, frequency: 103.83),
                GuitarString(position: 4, note: "Db", octave: 3, frequency: 138.59),
                GuitarString(position: 3, note: "Gb", octave: 3, frequency: 185.00),
                GuitarString(position: 2, note: "Bb", octave: 3, frequency: 233.08),
                GuitarString(position: 1, note: "Eb", octave: 4, frequency: 311.13)
            ]
        )
    }
    
    private func createFullStepDownTuning() -> Tuning {
        return Tuning(
            id: "full-step-down",
            name: "Full Step Down",
            strings: [
                GuitarString(position: 6, note: "D", octave: 2, frequency: 73.42),
                GuitarString(position: 5, note: "G", octave: 2, frequency: 98.00),
                GuitarString(position: 4, note: "C", octave: 3, frequency: 130.81),
                GuitarString(position: 3, note: "F", octave: 3, frequency: 174.61),
                GuitarString(position: 2, note: "A", octave: 3, frequency: 220.00),
                GuitarString(position: 1, note: "D", octave: 4, frequency: 293.66)
            ]
        )
    }
    
    private func createDropCTuning() -> Tuning {
        return Tuning(
            id: "drop-c",
            name: "Drop C",
            strings: [
                GuitarString(position: 6, note: "C", octave: 2, frequency: 65.41),
                GuitarString(position: 5, note: "G", octave: 2, frequency: 98.00),
                GuitarString(position: 4, note: "C", octave: 3, frequency: 130.81),
                GuitarString(position: 3, note: "F", octave: 3, frequency: 174.61),
                GuitarString(position: 2, note: "A", octave: 3, frequency: 220.00),
                GuitarString(position: 1, note: "D", octave: 4, frequency: 293.66)
            ]
        )
    }
}

// MARK: - JSON Decoding Models

private struct PresetsWrapper: Codable {
    let tunings: [JSONTuning]
}

private struct JSONTuning: Codable {
    let id: String
    let name: String
    let description: String?
    let strings: [JSONString]
}

private struct JSONString: Codable {
    let number: Int
    let note: String
    let octave: Int
    let frequency: Double
}
