package com.mytune.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a guitar tuning configuration with target notes for each of the six strings.
 * 
 * @property id Unique identifier for the tuning (e.g., "standard", "drop_d")
 * @property name Display name for the tuning (e.g., "Standard", "Drop D")
 * @property description Brief description of the tuning
 * @property strings Ordered list of 6 guitar strings from highest to lowest pitch
 */
@Serializable
data class Tuning(
    val id: String,
    val name: String,
    val description: String = "",
    val strings: List<GuitarString>
) {
    init {
        require(id.isNotBlank()) { "Tuning ID cannot be blank" }
        require(name.isNotBlank()) { "Tuning name cannot be blank" }
        require(strings.size == 6) { "Tuning must have exactly 6 strings" }
        
        val stringNumbers = strings.map { it.number }.sorted()
        require(stringNumbers == (1..6).toList()) {
            "String numbers must be 1-6 without duplicates, got: $stringNumbers"
        }
    }
    
    /**
     * Gets the target note for a specific string number (1-6).
     * 
     * @param stringNumber The string number (1 = highest, 6 = lowest)
     * @return The GuitarString for that position, or null if not found
     */
    fun getStringByNumber(stringNumber: Int): GuitarString? {
        return strings.find { it.number == stringNumber }
    }
    
    /**
     * Validates if this tuning is structurally correct.
     * 
     * @return true if valid, false otherwise
     */
    fun isValid(): Boolean {
        return try {
            require(id.isNotBlank())
            require(name.isNotBlank())
            require(strings.size == 6)
            require(strings.all { it.isValid() })
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
