package com.mytune.data.model

/**
 * Represents the result of a pitch detection operation.
 * 
 * @property detectedFrequency The detected frequency in Hz (0.0 if no pitch detected)
 * @property detectedNote The detected note name (e.g., "E", "A", "D")
 * @property detectedOctave The detected octave number
 * @property confidence Confidence level of the detection (0.0 to 1.0)
 * @property tuningState The current tuning state relative to target note
 * @property centsDeviation Deviation in cents from the target frequency (-100 to +100)
 * @property targetString The matched guitar string from current tuning preset (null if no match)
 */
data class PitchResult(
    val detectedFrequency: Double,
    val detectedNote: String,
    val detectedOctave: Int,
    val confidence: Double,
    val tuningState: TuningState,
    val centsDeviation: Double = 0.0,
    val targetString: GuitarString? = null
) {
    init {
        require(detectedFrequency >= 0.0) { "Detected frequency cannot be negative" }
        require(confidence in 0.0..1.0) { "Confidence must be between 0.0 and 1.0, got: $confidence" }
        require(centsDeviation in -200.0..200.0) { 
            "Cents deviation must be between -200 and +200, got: $centsDeviation" 
        }
    }
    
    /**
     * Returns the full detected note name with octave (e.g., "E4", "A2").
     */
    fun fullNoteName(): String = "$detectedNote$detectedOctave"
    
    /**
     * Checks if a pitch was successfully detected (non-zero frequency).
     */
    fun isPitchDetected(): Boolean = detectedFrequency > 0.0
    
    /**
     * Checks if the detection confidence is above a threshold.
     * 
     * @param threshold Minimum confidence level (default 0.5)
     * @return true if confidence >= threshold
     */
    fun isConfident(threshold: Double = 0.5): Boolean = confidence >= threshold
    
    /**
     * Checks if the detected pitch matches a string in the tuning.
     */
    fun hasTargetString(): Boolean = targetString != null
    
    /**
     * Checks if the string is currently in tune (within acceptable range).
     */
    fun isInTune(): Boolean = tuningState == TuningState.IN_TUNE
    
    companion object {
        /**
         * Creates an empty/no-pitch-detected result.
         */
        fun noPitchDetected(): PitchResult {
            return PitchResult(
                detectedFrequency = 0.0,
                detectedNote = "",
                detectedOctave = 0,
                confidence = 0.0,
                tuningState = TuningState.NO_PITCH,
                centsDeviation = 0.0,
                targetString = null
            )
        }
    }
}

/**
 * Enum representing the tuning state of a detected pitch relative to target.
 */
enum class TuningState {
    /** No pitch detected or confidence too low */
    NO_PITCH,
    
    /** In tune: deviation within ±5 cents */
    IN_TUNE,
    
    /** Slightly flat: deviation between -5 and -15 cents */
    SLIGHTLY_FLAT,
    
    /** Flat: deviation between -15 and -30 cents */
    FLAT,
    
    /** Very flat: deviation below -30 cents */
    VERY_FLAT,
    
    /** Slightly sharp: deviation between +5 and +15 cents */
    SLIGHTLY_SHARP,
    
    /** Sharp: deviation between +15 and +30 cents */
    SHARP,
    
    /** Very sharp: deviation above +30 cents */
    VERY_SHARP;
    
    /**
     * Determines if this state indicates the string needs tuning down (is sharp).
     */
    fun isSharp(): Boolean = this in listOf(SLIGHTLY_SHARP, SHARP, VERY_SHARP)
    
    /**
     * Determines if this state indicates the string needs tuning up (is flat).
     */
    fun isFlat(): Boolean = this in listOf(SLIGHTLY_FLAT, FLAT, VERY_FLAT)
    
    companion object {
        /**
         * Determines tuning state from cents deviation.
         * 
         * @param cents Deviation in cents from target frequency
         * @return The corresponding TuningState
         */
        fun fromCents(cents: Double): TuningState {
            return when {
                cents in -5.0..5.0 -> IN_TUNE
                cents in -15.0..-5.0 -> SLIGHTLY_FLAT
                cents in -30.0..-15.0 -> FLAT
                cents < -30.0 -> VERY_FLAT
                cents in 5.0..15.0 -> SLIGHTLY_SHARP
                cents in 15.0..30.0 -> SHARP
                cents > 30.0 -> VERY_SHARP
                else -> NO_PITCH
            }
        }
    }
}
