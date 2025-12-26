//
//  FrequencyConverter.swift
//  MyTune
//
//  Converts between frequency (Hz) and musical note names.
//

import Foundation

/// Converts frequencies to musical notes using equal temperament tuning
class FrequencyConverter {
    /// Standard reference pitch: A4 = 440 Hz
    private static let referenceFrequency: Double = 440.0
    private static let referenceMidiNote: Double = 69.0
    
    /// Note names in chromatic scale
    private static let noteNames = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"]
    
    /// Converts frequency in Hz to MIDI note number (fractional)
    /// - Parameter frequency: Frequency in Hz
    /// - Returns: MIDI note number (69 = A4)
    static func frequencyToMidiNote(_ frequency: Double) -> Double {
        return referenceMidiNote + 12.0 * log2(frequency / referenceFrequency)
    }
    
    /// Converts MIDI note number to frequency in Hz
    /// - Parameter midiNote: MIDI note number
    /// - Returns: Frequency in Hz
    static func midiNoteToFrequency(_ midiNote: Double) -> Double {
        return referenceFrequency * pow(2.0, (midiNote - referenceMidiNote) / 12.0)
    }
    
    /// Converts frequency to note name and octave
    /// - Parameter frequency: Frequency in Hz
    /// - Returns: Tuple of (note name, octave number)
    static func frequencyToNote(_ frequency: Double) -> (note: String, octave: Int) {
        let midiNote = frequencyToMidiNote(frequency)
        let roundedMidiNote = Int(round(midiNote))
        
        let noteIndex = roundedMidiNote % 12
        let octave = (roundedMidiNote / 12) - 1
        
        return (noteNames[noteIndex], octave)
    }
    
    /// Calculates the deviation in cents from the nearest note
    /// - Parameter frequency: Detected frequency in Hz
    /// - Returns: Deviation in cents (-50 to +50, where 100 cents = 1 semitone)
    static func calculateCents(_ frequency: Double) -> Int {
        let midiNote = frequencyToMidiNote(frequency)
        let roundedMidiNote = round(midiNote)
        let cents = (midiNote - roundedMidiNote) * 100.0
        return Int(round(cents))
    }
    
    /// Finds the target frequency for a given note and octave
    /// - Parameters:
    ///   - note: Note name (e.g., "E", "A#", "Bb")
    ///   - octave: Octave number
    /// - Returns: Target frequency in Hz, or nil if note name is invalid
    static func noteToFrequency(note: String, octave: Int) -> Double? {
        // Normalize note name (handle flats)
        let normalizedNote = normalizeNoteName(note)
        
        guard let noteIndex = noteNames.firstIndex(of: normalizedNote) else {
            return nil
        }
        
        let midiNote = (octave + 1) * 12 + noteIndex
        return midiNoteToFrequency(Double(midiNote))
    }
    
    /// Normalizes note names (converts flats to sharps)
    /// - Parameter note: Note name (e.g., "Bb", "A#")
    /// - Returns: Normalized note name using sharps
    private static func normalizeNoteName(_ note: String) -> String {
        let flatToSharp: [String: String] = [
            "Db": "C#",
            "Eb": "D#",
            "Gb": "F#",
            "Ab": "G#",
            "Bb": "A#"
        ]
        return flatToSharp[note] ?? note
    }
    
    /// Calculates the difference in cents between two frequencies
    /// - Parameters:
    ///   - detected: Detected frequency in Hz
    ///   - target: Target frequency in Hz
    /// - Returns: Deviation in cents (positive = sharp, negative = flat)
    static func centsBetween(detected: Double, target: Double) -> Int {
        let detectedMidi = frequencyToMidiNote(detected)
        let targetMidi = frequencyToMidiNote(target)
        let cents = (detectedMidi - targetMidi) * 100.0
        return Int(round(cents))
    }
}
