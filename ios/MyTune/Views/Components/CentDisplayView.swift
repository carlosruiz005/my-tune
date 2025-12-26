//
//  CentDisplayView.swift
//  MyTune
//
//  Displays numeric cent deviation from target pitch.
//

import SwiftUI

/// Displays the numeric cent deviation (e.g., "+15" or "-5")
struct CentDisplayView: View {
    let pitchResult: PitchResult?
    
    private var cents: Int? {
        pitchResult?.cents
    }
    
    private var tuningState: TuningState {
        pitchResult?.tuningState ?? .silent
    }
    
    private var centsColor: Color {
        switch tuningState {
        case .inTune:
            return .green
        case .slightlyFlat, .slightlySharp:
            return .yellow
        case .veryFlat, .verySharp:
            return .red
        default:
            return .secondary
        }
    }
    
    private var centsText: String {
        guard let cents = cents else {
            return "—"
        }
        
        let sign = cents > 0 ? "+" : ""
        return "\(sign)\(cents)"
    }
    
    var body: some View {
        VStack(spacing: 4) {
            Text(centsText)
                .font(.system(size: 48, weight: .bold, design: .rounded))
                .foregroundColor(centsColor)
                .animation(.easeInOut(duration: 0.2), value: cents)
            
            Text("cents")
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}

// MARK: - Preview

struct CentDisplayView_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 40) {
            // In-tune
            CentDisplayView(pitchResult: PitchResult(
                frequency: 440.0,
                note: "A",
                octave: 4,
                cents: 0,
                confidence: 0.9,
                timestamp: 1000,
                targetString: nil
            ))
            
            // Slightly flat
            CentDisplayView(pitchResult: PitchResult(
                frequency: 438.0,
                note: "A",
                octave: 4,
                cents: -15,
                confidence: 0.9,
                timestamp: 1000,
                targetString: nil
            ))
            
            // Sharp
            CentDisplayView(pitchResult: PitchResult(
                frequency: 445.0,
                note: "A",
                octave: 4,
                cents: 25,
                confidence: 0.9,
                timestamp: 1000,
                targetString: nil
            ))
            
            // No pitch
            CentDisplayView(pitchResult: nil)
        }
        .padding()
        .previewLayout(.sizeThatFits)
    }
}
