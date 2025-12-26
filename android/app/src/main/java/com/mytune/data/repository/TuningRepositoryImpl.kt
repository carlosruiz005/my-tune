package com.mytune.data.repository

import android.content.Context
import com.mytune.data.model.GuitarString
import com.mytune.data.model.Tuning
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TuningRepository that loads tuning presets from JSON assets.
 * 
 * Loads tuning configurations from shared/tuning-presets/presets.json
 * and provides reactive access to the currently selected tuning.
 */
@Singleton
class TuningRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : TuningRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Cache of loaded tunings
    private var tuningsCache: List<Tuning>? = null
    
    // StateFlow for the currently selected tuning
    private val _selectedTuningFlow = MutableStateFlow<Tuning?>(null)
    
    /**
     * Loads tuning presets from the assets JSON file.
     * 
     * @return List of tuning presets
     */
    private suspend fun loadTunings(): List<Tuning> {
        if (tuningsCache != null) {
            return tuningsCache!!
        }
        
        try {
            val jsonString = context.assets.open("presets.json").bufferedReader().use { it.readText() }
            val presetsData = json.decodeFromString<TuningPresetsJson>(jsonString)
            
            val tunings = presetsData.tunings.map { preset ->
                Tuning(
                    id = preset.id,
                    name = preset.name,
                    description = preset.description,
                    strings = preset.strings.map { string ->
                        GuitarString(
                            number = string.number,
                            note = string.note,
                            octave = string.octave,
                            frequency = string.frequency
                        )
                    }
                )
            }
            
            tuningsCache = tunings
            return tunings
        } catch (e: Exception) {
            // Return default standard tuning if loading fails
            return listOf(getDefaultStandardTuning())
        }
    }
    
    /**
     * Returns a default standard tuning configuration as fallback.
     */
    private fun getDefaultStandardTuning(): Tuning {
        return Tuning(
            id = "standard",
            name = "Standard",
            description = "Standard guitar tuning (E A D G B E)",
            strings = listOf(
                GuitarString(number = 1, note = "E", octave = 4, frequency = 329.63),
                GuitarString(number = 2, note = "B", octave = 3, frequency = 246.94),
                GuitarString(number = 3, note = "G", octave = 3, frequency = 196.00),
                GuitarString(number = 4, note = "D", octave = 3, frequency = 146.83),
                GuitarString(number = 5, note = "A", octave = 2, frequency = 110.00),
                GuitarString(number = 6, note = "E", octave = 2, frequency = 82.41)
            )
        )
    }
    
    override fun getAllTunings(): Flow<List<Tuning>> {
        return settingsRepository.getSettings().map { 
            loadTunings() 
        }
    }
    
    override suspend fun getTunings(): List<Tuning> {
        return loadTunings()
    }
    
    override suspend fun getTuningById(id: String): Tuning? {
        return loadTunings().find { it.id == id }
    }
    
    override fun getSelectedTuning(): Flow<Tuning> {
        return settingsRepository.getSettings().map { settings ->
            val tunings = loadTunings()
            tunings.find { it.id == settings.selectedTuningId } 
                ?: tunings.firstOrNull() 
                ?: getDefaultStandardTuning()
        }
    }
    
    override suspend fun getCurrentTuning(): Tuning {
        val settings = settingsRepository.getCurrentSettings()
        val tunings = loadTunings()
        return tunings.find { it.id == settings.selectedTuningId } 
            ?: tunings.firstOrNull() 
            ?: getDefaultStandardTuning()
    }
    
    override suspend fun setSelectedTuning(tuningId: String) {
        // Verify the tuning exists
        val tuning = getTuningById(tuningId)
        if (tuning != null) {
            settingsRepository.setSelectedTuning(tuningId)
            _selectedTuningFlow.value = tuning
        }
    }
}

/**
 * Data classes for JSON deserialization of presets.json
 */
@Serializable
private data class TuningPresetsJson(
    val tunings: List<TuningPresetJson>
)

@Serializable
private data class TuningPresetJson(
    val id: String,
    val name: String,
    val description: String,
    val strings: List<GuitarStringJson>
)

@Serializable
private data class GuitarStringJson(
    val number: Int,
    val note: String,
    val octave: Int,
    val frequency: Double
)
