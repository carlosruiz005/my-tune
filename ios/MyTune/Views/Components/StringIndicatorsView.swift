//
//  StringIndicatorsView.swift
//  MyTune
//
//  Displays the 6 target notes for the current tuning.
//

import SwiftUI

/// Displays all 6 target notes for the current tuning
struct StringIndicatorsView: View {
    let tuning: Tuning
    let pitchResult: PitchResult?
    
    private var currentTargetString: GuitarString? {
        pitchResult?.targetString
    }
    
    var body: some View {
        HStack(spacing: 16) {
            ForEach(tuning.strings.sorted(by: { $0.position > $1.position })) { string in
                StringIndicatorItem(
                    string: string,
                    isActive: string.id == currentTargetString?.id
                )
            }
        }
    }
}

/// Individual string indicator showing note and highlight state
struct StringIndicatorItem: View {
    let string: GuitarString
    let isActive: Bool
    
    var body: some View {
        VStack(spacing: 4) {
            // String number
            Text("\(string.position)")
                .font(.caption2)
                .foregroundColor(isActive ? .appPrimary : .secondary)
                .fontWeight(isActive ? .bold : .regular)
            
            // Note circle
            ZStack {
                Circle()
                    .fill(isActive ? Color.appPrimary.opacity(0.2) : Color.secondary.opacity(0.1))
                    .frame(width: 44, height: 44)
                
                if isActive {
                    Circle()
                        .stroke(Color.appPrimary, lineWidth: 2)
                        .frame(width: 44, height: 44)
                }
                
                Text(string.note)
                    .font(.system(size: 18, weight: isActive ? .bold : .regular))
                    .foregroundColor(isActive ? .appPrimary : .secondary)
            }
            .animation(.easeInOut(duration: 0.2), value: isActive)
            
            // Octave number
            Text("\(string.octave)")
                .font(.caption2)
                .foregroundColor(isActive ? .appPrimary : .secondary)
                .fontWeight(isActive ? .bold : .regular)
        }
    }
}

// MARK: - Preview

struct StringIndicatorsView_Previews: PreviewProvider {
    static let standardTuning = Tuning(
        id: "standard",
        name: "Standard",
        displayName: ["en": "Standard"],
        strings: [
            GuitarString(position: 6, note: "E", octave: 2, frequency: 82.41),
            GuitarString(position: 5, note: "A", octave: 2, frequency: 110.00),
            GuitarString(position: 4, note: "D", octave: 3, frequency: 146.83),
            GuitarString(position: 3, note: "G", octave: 3, frequency: 196.00),
            GuitarString(position: 2, note: "B", octave: 3, frequency: 246.94),
            GuitarString(position: 1, note: "E", octave: 4, frequency: 329.63)
        ]
    )
    
    static var previews: some View {
        VStack(spacing: 40) {
            // No active string
            StringIndicatorsView(
                tuning: standardTuning,
                pitchResult: nil
            )
            
            // Active string (E2 - 6th string)
            StringIndicatorsView(
                tuning: standardTuning,
                pitchResult: PitchResult(
                    frequency: 82.41,
                    note: "E",
                    octave: 2,
                    cents: 0,
                    confidence: 0.9,
                    timestamp: 1000,
                    targetString: standardTuning.strings.first { $0.position == 6 }
                )
            )
            
            // Active string (B3 - 2nd string)
            StringIndicatorsView(
                tuning: standardTuning,
                pitchResult: PitchResult(
                    frequency: 246.94,
                    note: "B",
                    octave: 3,
                    cents: 5,
                    confidence: 0.9,
                    timestamp: 1000,
                    targetString: standardTuning.strings.first { $0.position == 2 }
                )
            )
        }
        .padding()
        .previewLayout(.sizeThatFits)
    }
}
