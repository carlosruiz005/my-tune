package com.mytune.data.repository

import com.mytune.data.model.Tuning
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing guitar tuning presets.
 * 
 * Provides access to tuning configurations and the currently selected tuning.
 */
interface TuningRepository {
    /**
     * Gets all available tuning presets as a Flow.
     * 
     * @return Flow of list of all tuning presets
     */
    fun getAllTunings(): Flow<List<Tuning>>
    
    /**
     * Gets all available tuning presets (suspend function).
     * 
     * @return List of all tuning presets
     */
    suspend fun getTunings(): List<Tuning>
    
    /**
     * Gets a specific tuning by its ID.
     * 
     * @param id The tuning ID to look up
     * @return The tuning with that ID, or null if not found
     */
    suspend fun getTuningById(id: String): Tuning?
    
    /**
     * Gets the currently selected tuning as a Flow.
     * 
     * @return Flow of the currently selected tuning
     */
    fun getSelectedTuning(): Flow<Tuning>
    
    /**
     * Gets the currently selected tuning (suspend function).
     * 
     * @return The currently selected tuning
     */
    suspend fun getCurrentTuning(): Tuning
    
    /**
     * Sets the currently selected tuning by ID.
     * 
     * @param tuningId The ID of the tuning to select
     */
    suspend fun setSelectedTuning(tuningId: String)
}
