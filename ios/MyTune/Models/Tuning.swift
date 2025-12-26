//
//  Tuning.swift
//  MyTune
//
//  Represents a guitar tuning configuration with target notes for each of the six strings.
//

import Foundation

struct Tuning: Codable, Identifiable, Equatable {
    let id: String
    let name: String
    let displayName: [String: String]
    let strings: [GuitarString]
    
    init(id: String, name: String, displayName: [String: String] = [:], strings: [GuitarString]) {
        precondition(!id.isEmpty, "Tuning ID cannot be empty")
        precondition(!name.isEmpty, "Tuning name cannot be empty")
        precondition(strings.count == 6, "Tuning must have exactly 6 strings")
        
        let positions = Set(strings.map { $0.position }).sorted()
        precondition(positions == [1, 2, 3, 4, 5, 6],
                     "String positions must be 1-6 without duplicates")
        
        self.id = id
        self.name = name
        self.displayName = displayName
        // Sort strings by position descending (6 to 1) to match guitar physical layout
        self.strings = strings.sorted { $0.position > $1.position }
    }
    
    /// Returns the localized name for the current language code, or the default name if not found
    func localizedName(languageCode: String = Locale.current.languageCode ?? "en") -> String {
        return displayName[languageCode] ?? name
    }
    
    /// Returns the guitar string at the specified position (1-6)
    func string(at position: Int) -> GuitarString? {
        return strings.first { $0.position == position }
    }
}
