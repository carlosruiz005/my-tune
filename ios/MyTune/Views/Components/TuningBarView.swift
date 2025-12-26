//
//  TuningBarView.swift
//  MyTune
//
//  Visual tuning indicator with horizontal bar and animated marker.
//

import SwiftUI

/// Visual tuning bar with marker showing pitch deviation
struct TuningBarView: View {
    let pitchResult: PitchResult?
    let movementThreshold: Int = 10 // ±10 cents threshold to prevent jitter
    
    @State private var markerPosition: CGFloat = 0.5
    @State private var lastStableCents: Int?
    
    private var currentCents: Int? {
        pitchResult?.cents
    }
    
    private var tuningState: TuningState {
        pitchResult?.tuningState ?? .silent
    }
    
    private var markerColor: Color {
        switch tuningState {
        case .inTune:
            return .green
        case .slightlyFlat, .slightlySharp:
            return .yellow
        case .veryFlat, .verySharp:
            return .red
        default:
            return .gray
        }
    }
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                // Background bar
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.secondary.opacity(0.2))
                    .frame(height: 60)
                
                // Center line
                Rectangle()
                    .fill(Color.secondary.opacity(0.5))
                    .frame(width: 2, height: 60)
                    .position(x: geometry.size.width / 2, y: 30)
                
                // Tick marks for -50, -25, +25, +50 cents
                HStack(spacing: 0) {
                    ForEach([0.1, 0.25, 0.5, 0.75, 0.9], id: \.self) { position in
                        Rectangle()
                            .fill(Color.secondary.opacity(0.3))
                            .frame(width: 1, height: 20)
                            .position(x: geometry.size.width * CGFloat(position), y: 30)
                    }
                }
                
                // Marker
                Circle()
                    .fill(markerColor)
                    .frame(width: 24, height: 24)
                    .overlay(
                        Circle()
                            .stroke(Color.white, lineWidth: 2)
                    )
                    .shadow(color: markerColor.opacity(0.5), radius: 4, x: 0, y: 2)
                    .position(
                        x: calculateMarkerPosition(width: geometry.size.width),
                        y: 30
                    )
                    .animation(.spring(response: 0.3, dampingFraction: 0.7), value: markerPosition)
            }
            .frame(height: 60)
        }
        .frame(height: 60)
        .onChange(of: currentCents) { newCents in
            updateMarkerPosition(newCents: newCents)
        }
    }
    
    // MARK: - Private Methods
    
    private func calculateMarkerPosition(width: CGFloat) -> CGFloat {
        return width * markerPosition
    }
    
    private func updateMarkerPosition(newCents: Int?) {
        guard let cents = newCents else {
            // No pitch detected, move to center
            markerPosition = 0.5
            lastStableCents = nil
            return
        }
        
        // Apply movement threshold to prevent jitter
        if let lastCents = lastStableCents {
            let difference = abs(cents - lastCents)
            if difference < movementThreshold {
                // Change is too small, keep current position
                return
            }
        }
        
        // Update stable cents and calculate new position
        lastStableCents = cents
        
        // Map cents (-100 to +100) to position (0.0 to 1.0)
        // Center (0 cents) = 0.5
        // -100 cents = 0.0 (left edge)
        // +100 cents = 1.0 (right edge)
        let normalizedCents = Double(cents) / 100.0 // -1.0 to +1.0
        let position = 0.5 + (normalizedCents * 0.5) // 0.0 to 1.0
        
        markerPosition = CGFloat(position.clamped(to: 0.0...1.0))
    }
}

// MARK: - Helper Extensions

private extension Double {
    func clamped(to range: ClosedRange<Double>) -> Double {
        return min(max(self, range.lowerBound), range.upperBound)
    }
}

// MARK: - Preview

struct TuningBarView_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 40) {
            // In-tune
            TuningBarView(pitchResult: PitchResult(
                frequency: 440.0,
                note: "A",
                octave: 4,
                cents: 0,
                confidence: 0.9,
                timestamp: 1000,
                targetString: nil
            ))
            .padding()
            
            // Slightly flat
            TuningBarView(pitchResult: PitchResult(
                frequency: 438.0,
                note: "A",
                octave: 4,
                cents: -15,
                confidence: 0.9,
                timestamp: 1000,
                targetString: nil
            ))
            .padding()
            
            // Slightly sharp
            TuningBarView(pitchResult: PitchResult(
                frequency: 442.0,
                note: "A",
                octave: 4,
                cents: 15,
                confidence: 0.9,
                timestamp: 1000,
                targetString: nil
            ))
            .padding()
            
            // Very flat
            TuningBarView(pitchResult: PitchResult(
                frequency: 430.0,
                note: "A",
                octave: 4,
                cents: -40,
                confidence: 0.9,
                timestamp: 1000,
                targetString: nil
            ))
            .padding()
            
            // No pitch
            TuningBarView(pitchResult: nil)
                .padding()
        }
        .previewLayout(.sizeThatFits)
    }
}
