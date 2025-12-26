//
//  PitchResult.swift
//  MyTune
//
//  Represents the result of real-time pitch detection from audio input.
//

import Foundation

struct PitchResult: Equatable {
    let frequency: Double?
    let note: String?
    let octave: Int?
    let cents: Int?
    let confidence: Double
    let timestamp: Int64
    let targetString: GuitarString?
    
    init(frequency: Double?, note: String?, octave: Int?, cents: Int?, 
         confidence: Double, timestamp: Int64, targetString: GuitarString? = nil) {
        precondition((0...1).contains(confidence), "Confidence must be 0.0-1.0")
        precondition(timestamp > 0, "Timestamp must be positive")
        
        if let freq = frequency {
            precondition(freq > 0, "Frequency must be positive")
        }
        if let c = cents {
            precondition((-100...100).contains(c), "Cents must be -100 to +100")
        }
        
        self.frequency = frequency
        self.note = note
        self.octave = octave
        self.cents = cents
        self.confidence = confidence
        self.timestamp = timestamp
        self.targetString = targetString
    }
    
    /// Returns the full note name with octave if available
    var fullNoteName: String? {
        guard let note = note, let octave = octave else {
            return nil
        }
        return "\(note)\(octave)"
    }
    
    /// Returns the current tuning state based on confidence and cent deviation
    var tuningState: TuningState {
        switch (frequency, confidence, cents) {
        case (nil, _, _), (_, ..<0.3, _):
            return .silent
        case (_, ..<0.7, _):
            return .detecting
        case (_, _, nil):
            return .active
        case (_, _, let c?) where (-10...10).contains(c):
            return .inTune
        case (_, _, let c?) where (-25...(-11)).contains(c):
            return .slightlyFlat
        case (_, _, let c?) where c < -25:
            return .veryFlat
        case (_, _, let c?) where (11...25).contains(c):
            return .slightlySharp
        default:
            return .verySharp
        }
    }
}

enum TuningState {
    case silent        // No pitch detected or very low confidence
    case detecting     // Audio detected but pitch unclear
    case active        // Clear pitch detected
    case inTune        // Within ±10 cents
    case slightlyFlat  // -25 to -11 cents
    case veryFlat      // < -25 cents
    case slightlySharp // 11 to 25 cents
    case verySharp     // > 25 cents
    
    /// Returns a user-friendly description of the tuning state
    var description: String {
        switch self {
        case .silent:
            return "Silent"
        case .detecting:
            return "Detecting..."
        case .active:
            return "Active"
        case .inTune:
            return "In Tune"
        case .slightlyFlat:
            return "Slightly Flat"
        case .veryFlat:
            return "Very Flat"
        case .slightlySharp:
            return "Slightly Sharp"
        case .verySharp:
            return "Very Sharp"
        }
    }
}
