package com.mytune.di

import android.content.Context
import com.mytune.data.audio.AndroidAudioProcessor
import com.mytune.data.audio.IAudioProcessor
import com.mytune.data.repository.SettingsRepository
import com.mytune.data.repository.SettingsRepositoryImpl
import com.mytune.data.repository.TuningRepository
import com.mytune.data.repository.TuningRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides app-level dependencies.
 * 
 * Configures dependency injection for:
 * - Repositories (Settings, Tuning)
 * - Audio Processor (pitch detection)
 * - Future dependencies (ViewModels, etc.)
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    /**
     * Provides the SettingsRepository implementation.
     * 
     * @param context Application context
     * @return SettingsRepository instance
     */
    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }
    
    /**
     * Provides the TuningRepository implementation.
     * 
     * @param context Application context
     * @param settingsRepository Settings repository for accessing selected tuning
     * @return TuningRepository instance
     */
    @Provides
    @Singleton
    fun provideTuningRepository(
        @ApplicationContext context: Context,
        settingsRepository: SettingsRepository
    ): TuningRepository {
        return TuningRepositoryImpl(context, settingsRepository)
    }
    
    /**
     * Provides the AudioProcessor implementation.
     * 
     * @param context Application context
     * @param tuningRepository Tuning repository for current tuning
     * @param settingsRepository Settings repository for audio settings
     * @return IAudioProcessor instance
     */
    @Provides
    @Singleton
    fun provideAudioProcessor(
        @ApplicationContext context: Context,
        tuningRepository: TuningRepository,
        settingsRepository: SettingsRepository
    ): IAudioProcessor {
        return AndroidAudioProcessor(context, tuningRepository, settingsRepository)
    }
}
