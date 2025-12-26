package com.mytune.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a single guitar string with its target note, octave, and frequency.
 * 
 * @property number String number (1 = highest pitch E, 6 = lowest pitch E in standard tuning)
 * @property note The musical note name (e.g., "E", "A", "D", "G", "B", "Eb", "F#")
 * @property octave The octave number (scientific pitch notation)
 * @property frequency The target frequency in Hz
 */
@Serializable
data class GuitarString(
    val number: Int,
    val note: String,
    val octave: Int,
    val frequency: Double
) {
    init {
        require(number in 1..6) { "String number must be between 1 and 6, got: $number" }
        require(note.isNotBlank()) { "Note name cannot be blank" }
        require(octave in 0..8) { "Octave must be between 0 and 8, got: $octave" }
        require(frequency > 0) { "Frequency must be positive, got: $frequency" }
        require(frequency in 20.0..2000.0) { 
            "Frequency must be in audible guitar range (20-2000 Hz), got: $frequency" 
        }
    }
    
    /**
     * Returns the full note name with octave (e.g., "E4", "A2", "Bb3").
     */
    fun fullNoteName(): String = "$note$octave"
    
    /**
     * Validates if this guitar string is structurally correct.
     * 
     * @return true if valid, false otherwise
     */
    fun isValid(): Boolean {
        return try {
            require(number in 1..6)
            require(note.isNotBlank())
            require(octave in 0..8)
            require(frequency > 0 && frequency in 20.0..2000.0)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    
    /**
     * Checks if this string is in the lower register (strings 4-6).
     */
    fun isLowString(): Boolean = number in 4..6
    
    /**
     * Checks if this string is in the higher register (strings 1-3).
     */
    fun isHighString(): Boolean = number in 1..3
}
