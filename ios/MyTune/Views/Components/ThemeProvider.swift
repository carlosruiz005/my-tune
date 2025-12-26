//
//  ThemeProvider.swift
//  MyTune
//
//  Provides theme management and color access for the application.
//

import SwiftUI
import Foundation

/// Theme provider that manages light/dark mode and provides color access
class ThemeProvider: ObservableObject {
    @Published var themeMode: ThemeMode = .system
    
    init(themeMode: ThemeMode = .system) {
        self.themeMode = themeMode
    }
}

/// Extension to provide easy access to theme colors
extension Color {
    // MARK: - Base Colors
    static let appPrimary = Color("Primary")
    static let appSecondary = Color("Secondary")
    static let appBackground = Color("Background")
    static let appSurface = Color("Surface")
    
    // MARK: - Tuning Indicator Colors
    static let tuningInTune = Color("InTune")
    static let tuningSharp = Color("Sharp")
    static let tuningFlat = Color("Sharp") // Same color for flat and sharp
    static let tuningVerySharp = Color("VerySharp")
    static let tuningVeryFlat = Color("VerySharp") // Same color for very flat and very sharp
    static let tuningNeutral = Color("Neutral")
    
    // MARK: - Semantic Colors
    /// Returns the appropriate color for a tuning state
    static func tuningColor(for state: TuningState) -> Color {
        switch state {
        case .inTune:
            return .tuningInTune
        case .slightlyFlat, .slightlySharp:
            return .tuningSharp
        case .veryFlat, .verySharp:
            return .tuningVerySharp
        case .silent, .detecting, .active:
            return .tuningNeutral
        }
    }
}
