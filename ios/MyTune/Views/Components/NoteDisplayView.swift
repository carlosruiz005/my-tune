//
//  NoteDisplayView.swift
//  MyTune
//
//  Displays the detected note name and octave in large text.
//

import SwiftUI

/// Displays the currently detected musical note
struct NoteDisplayView: View {
    let pitchResult: PitchResult?
    
    var body: some View {
        VStack(spacing: 8) {
            // Large note display
            Text(displayNote)
                .font(.system(size: 80, weight: .bold, design: .rounded))
                .foregroundColor(noteColor)
                .animation(.easeInOut(duration: 0.2), value: displayNote)
            
            // Frequency display
            if let frequency = pitchResult?.frequency {
                Text(String(format: "%.2f Hz", frequency))
                    .font(.system(size: 20, weight: .medium, design: .monospaced))
                    .foregroundColor(.secondary)
            }
            
            // State indicator
            if let result = pitchResult {
                Text(result.tuningState.description)
                    .font(.system(size: 16, weight: .regular))
                    .foregroundColor(.secondary)
                    .textCase(.uppercase)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 40)
    }
    
    private var displayNote: String {
        guard let result = pitchResult,
              let note = result.note,
              let octave = result.octave else {
            return "—"
        }
        
        return "\(note)\(octave)"
    }
    
    private var noteColor: Color {
        guard let result = pitchResult else {
            return .tuningNeutral
        }
        
        return Color.tuningColor(for: result.tuningState)
    }
}

#Preview("Detecting") {
    NoteDisplayView(
        pitchResult: PitchResult(
            frequency: nil,
            note: nil,
            octave: nil,
            cents: nil,
            confidence: 0.5,
            timestamp: 0
        )
    )
}

#Preview("Note Detected") {
    NoteDisplayView(
        pitchResult: PitchResult(
            frequency: 329.63,
            note: "E",
            octave: 4,
            cents: 0,
            confidence: 0.9,
            timestamp: 0
        )
    )
}

#Preview("In Tune") {
    NoteDisplayView(
        pitchResult: PitchResult(
            frequency: 110.0,
            note: "A",
            octave: 2,
            cents: 0,
            confidence: 0.95,
            timestamp: 0,
            targetString: GuitarString(position: 5, note: "A", octave: 2, frequency: 110.0)
        )
    )
}
