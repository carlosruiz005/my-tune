package com.mytune.data.audio

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round

/**
 * Frequency converter for Hz to musical note conversion.
 * 
 * Implements MIDI note calculation and note name extraction
 * based on equal temperament tuning (A4 = 440 Hz).
 */
class FrequencyConverter(
    private val calibrationFrequency: Double = 440.0
) {
    
    init {
        require(calibrationFrequency in 430.0..450.0) {
            "Calibration frequency must be between 430-450 Hz, got: $calibrationFrequency"
        }
    }
    
    // Note names for 12-tone equal temperament
    private val noteNames = arrayOf(
        "C", "C#", "D", "D#", "E", "F", 
        "F#", "G", "G#", "A", "A#", "B"
    )
    
    // Alternative note names (flats)
    private val noteNamesFlat = arrayOf(
        "C", "Db", "D", "Eb", "E", "F",
        "Gb", "G", "Ab", "A", "Bb", "B"
    )
    
    /**
     * Converts frequency to musical note information.
     * 
     * @param frequency Frequency in Hz
     * @param useFlats Whether to use flat notation (Db) instead of sharp (C#)
     * @return NoteInfo with note name, octave, MIDI number, and cents deviation
     */
    fun frequencyToNote(frequency: Double, useFlats: Boolean = false): NoteInfo {
        require(frequency > 0) { "Frequency must be positive, got: $frequency" }
        
        // Calculate MIDI note number (A4 = MIDI 69 = 440 Hz)
        val midiNote = 69.0 + 12.0 * log2(frequency / calibrationFrequency)
        
        // Round to nearest semitone
        val nearestMidi = round(midiNote).toInt()
        
        // Calculate cents deviation from nearest semitone
        val centsDeviation = 100.0 * (midiNote - nearestMidi)
        
        // Extract note name and octave
        val noteIndex = nearestMidi % 12
        val octave = (nearestMidi / 12) - 1
        
        val noteName = if (useFlats) {
            noteNamesFlat[noteIndex]
        } else {
            noteNames[noteIndex]
        }
        
        // Calculate exact frequency of the nearest semitone
        val exactFrequency = midiNoteToFrequency(nearestMidi)
        
        return NoteInfo(
            noteName = noteName,
            octave = octave,
            midiNote = nearestMidi,
            centsDeviation = centsDeviation,
            frequency = frequency,
            exactFrequency = exactFrequency
        )
    }
    
    /**
     * Converts MIDI note number to frequency.
     * 
     * @param midiNote MIDI note number (0-127)
     * @return Frequency in Hz
     */
    fun midiNoteToFrequency(midiNote: Int): Double {
        require(midiNote in 0..127) { 
            "MIDI note must be between 0 and 127, got: $midiNote" 
        }
        
        // Formula: f = A4 × 2^((n - 69) / 12)
        return calibrationFrequency * 2.0.pow((midiNote - 69) / 12.0)
    }
    
    /**
     * Calculates cents deviation between two frequencies.
     * 
     * Cents = 1200 × log2(f1 / f2)
     * 
     * @param frequency1 First frequency in Hz
     * @param frequency2 Second frequency in Hz
     * @return Cents deviation (positive if frequency1 > frequency2)
     */
    fun calculateCents(frequency1: Double, frequency2: Double): Double {
        require(frequency1 > 0 && frequency2 > 0) {
            "Both frequencies must be positive"
        }
        
        return 1200.0 * log2(frequency1 / frequency2)
    }
    
    /**
     * Gets the note name for a MIDI note number.
     * 
     * @param midiNote MIDI note number
     * @param useFlats Whether to use flat notation
     * @return Note name (e.g., "A", "C#", "Bb")
     */
    fun getNoteNameFromMidi(midiNote: Int, useFlats: Boolean = false): String {
        require(midiNote in 0..127) { "MIDI note must be between 0 and 127" }
        
        val noteIndex = midiNote % 12
        return if (useFlats) {
            noteNamesFlat[noteIndex]
        } else {
            noteNames[noteIndex]
        }
    }
    
    /**
     * Gets the octave for a MIDI note number.
     * 
     * @param midiNote MIDI note number
     * @return Octave number (e.g., 4 for A4)
     */
    fun getOctaveFromMidi(midiNote: Int): Int {
        require(midiNote in 0..127) { "MIDI note must be between 0 and 127" }
        return (midiNote / 12) - 1
    }
}

/**
 * Information about a musical note derived from frequency.
 * 
 * @property noteName Note name (e.g., "A", "C#", "Bb")
 * @property octave Octave number (e.g., 4 for A4)
 * @property midiNote MIDI note number (0-127)
 * @property centsDeviation Deviation in cents from exact semitone (-50 to +50)
 * @property frequency Detected frequency in Hz
 * @property exactFrequency Exact frequency of the nearest semitone in Hz
 */
data class NoteInfo(
    val noteName: String,
    val octave: Int,
    val midiNote: Int,
    val centsDeviation: Double,
    val frequency: Double,
    val exactFrequency: Double
) {
    /**
     * Returns the full note name with octave (e.g., "A4", "C#3").
     */
    fun fullNoteName(): String = "$noteName$octave"
    
    /**
     * Checks if the note is sharp (within ±50 cents).
     */
    fun isSharp(): Boolean = centsDeviation > 5.0
    
    /**
     * Checks if the note is flat (within ±50 cents).
     */
    fun isFlat(): Boolean = centsDeviation < -5.0
    
    /**
     * Checks if the note is in tune (within ±5 cents).
     */
    fun isInTune(thresholdCents: Double = 5.0): Boolean {
        return kotlin.math.abs(centsDeviation) <= thresholdCents
    }
    
    /**
     * Returns a rounded cents deviation for display.
     */
    fun roundedCents(): Int = round(centsDeviation).toInt()
}
