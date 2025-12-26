//
//  GuitarString.swift
//  MyTune
//
//  Represents a single guitar string with its target pitch information.
//

import Foundation

struct GuitarString: Codable, Identifiable, Equatable {
    let position: Int
    let note: String
    let octave: Int
    let frequency: Double
    
    var id: Int { position }
    
    init(position: Int, note: String, octave: Int, frequency: Double) {
        precondition((1...6).contains(position), "String position must be 1-6")
        precondition(note.range(of: "^[A-G][#b]?$", options: .regularExpression) != nil,
                     "Invalid note name: \(note)")
        precondition((0...9).contains(octave), "Octave must be 0-9")
        precondition(frequency > 0, "Frequency must be positive")
        
        self.position = position
        self.note = note
        self.octave = octave
        self.frequency = frequency
    }
    
    /// Returns the full note name with octave (e.g., "E4", "A2")
    var fullNoteName: String {
        return "\(note)\(octave)"
    }
    
    /// Returns a display name for the string (e.g., "1st string (E4)")
    var displayName: String {
        let ordinal = position.ordinalString
        return "\(ordinal) string (\(fullNoteName))"
    }
}

// MARK: - Helper Extension
private extension Int {
    var ordinalString: String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .ordinal
        return formatter.string(from: NSNumber(value: self)) ?? "\(self)th"
    }
}
