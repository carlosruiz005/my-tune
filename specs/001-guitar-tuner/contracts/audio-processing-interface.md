# Audio Processing Interface Contract

**Module**: Audio Engine  
**Version**: 1.0.0  
**Last Updated**: 2025-12-08

## Overview

Defines the interface contract between the audio processing layer (FFT, pitch detection) and the application layer (ViewModels, UI state management). This contract is implemented consistently across all platforms with platform-specific adaptations.

---

## Interface: IAudioProcessor

Responsible for capturing audio input, performing FFT analysis, and detecting pitch.

### Methods

#### `start()`

Begins audio capture and processing.

**Parameters**: None

**Returns**: 
- **Android**: `Result<Unit>` (Success or Failure with error)
- **iOS**: `Result<Void, AudioError>` (Swift Result type)
- **WebApp**: `Promise<void>` (resolves on success, rejects on error)

**Errors**:
- `MicrophonePermissionDenied`: User denied microphone access
- `AudioHardwareUnavailable`: Microphone not available or in use
- `AudioEngineInitializationFailed`: Failed to initialize audio engine

**Side Effects**:
- Requests microphone permission if not granted
- Starts background audio capture thread/worklet
- Begins emitting `PitchResult` events to observers

**Pre-conditions**:
- Audio engine not already running
- Device has microphone capability

**Post-conditions**:
- Audio capture active and emitting results at configured rate
- `isRunning()` returns `true`

---

#### `stop()`

Stops audio capture and processing.

**Parameters**: None

**Returns**: `void` (all platforms)

**Side Effects**:
- Halts audio capture
- Releases microphone hardware
- Stops emitting `PitchResult` events
- Cleans up background threads/worklets

**Pre-conditions**:
- Audio engine is currently running

**Post-conditions**:
- Audio capture inactive
- `isRunning()` returns `false`
- Microphone released for other apps

---

#### `isRunning()`

Returns whether audio processing is currently active.

**Parameters**: None

**Returns**: `Boolean` (all platforms)

**Side Effects**: None (read-only)

---

#### `observePitchResults()`

Provides a stream of pitch detection results.

**Parameters**: None

**Returns**:
- **Android**: `Flow<PitchResult>` (Kotlin Flow)
- **iOS**: `AnyPublisher<PitchResult, Never>` (Combine Publisher)
- **WebApp**: `Observable<PitchResult>` (RxJS) or callback-based stream

**Emission Rate**: Controlled by `TunerSettings.frequencyUpdateRate` (default 20 Hz = every 50ms)

**Behavior**:
- Emits `PitchResult` objects continuously while running
- Emissions throttled to configured rate to prevent UI overload
- Only emits results with `confidence >= TunerSettings.noiseGateThreshold`
- Stops emitting when `stop()` is called

**Error Handling**:
- Does not emit errors in stream (errors handled by `start()` method)
- Silent periods (no pitch detected) emit `PitchResult` with `frequency = null`, `confidence < 0.3`

---

### Properties

#### `sampleRate`

The audio sample rate in Hz.

**Type**: `Int` (read-only)

**Typical Values**: 44100 or 48000 Hz (platform-dependent)

**Usage**: Information only, determined by device audio capabilities

---

#### `bufferSize`

The FFT buffer size in samples.

**Type**: `Int` (read-only)

**Typical Values**: 4096, 8192, or 16384 samples

**Usage**: Determines frequency resolution (`sampleRate / bufferSize` Hz per bin)

---

### Platform-Specific Implementations

#### Android (Kotlin Interface)

```kotlin
interface IAudioProcessor {
    suspend fun start(): Result<Unit>
    fun stop()
    fun isRunning(): Boolean
    fun observePitchResults(): Flow<PitchResult>
    
    val sampleRate: Int
    val bufferSize: Int
}

sealed class AudioError : Exception() {
    object MicrophonePermissionDenied : AudioError()
    object AudioHardwareUnavailable : AudioError()
    object AudioEngineInitializationFailed : AudioError()
}
```

**Implementation Class**: `AndroidAudioProcessor` (uses `AudioRecord`)

---

#### iOS (Swift Protocol)

```swift
protocol IAudioProcessor {
    func start() async -> Result<Void, AudioError>
    func stop()
    func isRunning() -> Bool
    func observePitchResults() -> AnyPublisher<PitchResult, Never>
    
    var sampleRate: Int { get }
    var bufferSize: Int { get }
}

enum AudioError: Error {
    case microphonePermissionDenied
    case audioHardwareUnavailable
    case audioEngineInitializationFailed
}
```

**Implementation Class**: `IOSAudioProcessor` (uses `AVAudioEngine`)

---

#### WebApp (TypeScript Interface)

```typescript
export interface IAudioProcessor {
  start(): Promise<void>;
  stop(): void;
  isRunning(): boolean;
  observePitchResults(callback: (result: PitchResult) => void): () => void; // Returns unsubscribe function
  
  readonly sampleRate: number;
  readonly bufferSize: number;
}

export enum AudioErrorType {
  MicrophonePermissionDenied = 'MICROPHONE_PERMISSION_DENIED',
  AudioHardwareUnavailable = 'AUDIO_HARDWARE_UNAVAILABLE',
  AudioEngineInitializationFailed = 'AUDIO_ENGINE_INITIALIZATION_FAILED',
}

export class AudioError extends Error {
  constructor(public type: AudioErrorType, message: string) {
    super(message);
  }
}
```

**Implementation Class**: `WebAudioProcessor` (uses Web Audio API + AudioWorklet)

---

## Interface: IPitchDetector

Responsible for analyzing frequency spectrum and detecting pitch.

### Methods

#### `detectPitch(frequencySpectrum: FloatArray, sampleRate: Int)`

Analyzes FFT output and returns detected pitch information.

**Parameters**:
- `frequencySpectrum`: Array of FFT magnitude values (half of FFT size, due to Nyquist)
  - Type: `FloatArray` (Android), `[Float]` (iOS), `Float32Array` (WebApp)
  - Length: `bufferSize / 2 + 1` (e.g., 4097 for 8192 FFT)
- `sampleRate`: Audio sample rate in Hz (e.g., 44100)
  - Type: `Int` (Android/iOS), `number` (WebApp)

**Returns**: `PitchResult` object with detected pitch information

**Algorithm Steps**:
1. Apply Harmonic Product Spectrum (HPS) to reduce harmonic confusion
2. Find peak bin within guitar frequency range (60-400 Hz)
3. Apply parabolic interpolation for sub-bin accuracy
4. Convert bin index to frequency: `frequency = (bin * sampleRate) / bufferSize`
5. Calculate confidence based on peak amplitude and spectral clarity
6. Return `PitchResult` with `frequency`, `confidence`, `timestamp`

**Performance**: 
- Must complete within 10ms on target devices
- Average execution time: 3-5ms on mid-range devices

**Edge Cases**:
- No clear peak detected → Return `PitchResult(frequency=null, confidence=0.0)`
- Multiple peaks of similar amplitude → Return highest confidence peak
- Peak outside guitar range → Return null result

---

### Platform-Specific Implementations

#### Android (Kotlin Interface)

```kotlin
interface IPitchDetector {
    fun detectPitch(frequencySpectrum: FloatArray, sampleRate: Int): PitchResult
}
```

**Implementation Class**: `HPSPitchDetector`

---

#### iOS (Swift Protocol)

```swift
protocol IPitchDetector {
    func detectPitch(frequencySpectrum: [Float], sampleRate: Int) -> PitchResult
}
```

**Implementation Class**: `HPSPitchDetector`

---

#### WebApp (TypeScript Interface)

```typescript
export interface IPitchDetector {
  detectPitch(frequencySpectrum: Float32Array, sampleRate: number): PitchResult;
}
```

**Implementation Class**: `HPSPitchDetector`

---

## Interface: IFrequencyConverter

Converts raw frequency (Hz) to musical note information (note name, octave, cents deviation).

### Methods

#### `frequencyToNote(frequency: Double)`

Converts frequency in Hz to note name, octave, and MIDI note number.

**Parameters**:
- `frequency`: Frequency in Hertz
  - Type: `Double` (Android/iOS), `number` (WebApp)
  - Range: 20-20,000 Hz (human hearing range)

**Returns**: `NoteInfo` object (or tuple/struct depending on platform)

**Algorithm**:
```
midiNote = 12 * log2(frequency / 440) + 69
noteIndex = round(midiNote) % 12
noteName = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"][noteIndex]
octave = floor(round(midiNote) / 12) - 1
```

**Example**:
- Input: `110.0` Hz → Output: `NoteInfo(note="A", octave=2, midiNote=45)`
- Input: `440.0` Hz → Output: `NoteInfo(note="A", octave=4, midiNote=69)`

---

#### `calculateCents(frequency: Double, targetFrequency: Double)`

Calculates cent deviation between detected frequency and target frequency.

**Parameters**:
- `frequency`: Detected frequency in Hz
- `targetFrequency`: Target note frequency in Hz

**Returns**: `Int` (cents deviation, -100 to +100)

**Algorithm**:
```
cents = 1200 * log2(frequency / targetFrequency)
```

**Example**:
- Input: `frequency=112`, `targetFrequency=110` → Output: `+31` cents (sharp)
- Input: `frequency=108`, `targetFrequency=110` → Output: `-31` cents (flat)

**Precision**: Rounded to nearest integer cent

---

### Data Structures

#### `NoteInfo`

**Android (Kotlin)**:
```kotlin
data class NoteInfo(
    val note: String,        // "A", "D#", "Bb"
    val octave: Int,         // 0-9
    val midiNote: Int        // 0-127
)
```

**iOS (Swift)**:
```swift
struct NoteInfo {
    let note: String
    let octave: Int
    let midiNote: Int
}
```

**WebApp (TypeScript)**:
```typescript
export interface NoteInfo {
  note: string;
  octave: number;
  midiNote: number;
}
```

---

### Platform-Specific Implementations

#### Android (Kotlin Interface)

```kotlin
interface IFrequencyConverter {
    fun frequencyToNote(frequency: Double): NoteInfo
    fun calculateCents(frequency: Double, targetFrequency: Double): Int
}
```

**Implementation Class**: `StandardFrequencyConverter`

---

#### iOS (Swift Protocol)

```swift
protocol IFrequencyConverter {
    func frequencyToNote(frequency: Double) -> NoteInfo
    func calculateCents(frequency: Double, targetFrequency: Double) -> Int
}
```

**Implementation Class**: `StandardFrequencyConverter`

---

#### WebApp (TypeScript Interface)

```typescript
export interface IFrequencyConverter {
  frequencyToNote(frequency: number): NoteInfo;
  calculateCents(frequency: number, targetFrequency: number): number;
}
```

**Implementation Class**: `StandardFrequencyConverter`

---

## Interface: ITuningRepository

Manages tuning presets and user's selected tuning.

### Methods

#### `getAllTunings()`

Retrieves all available tuning presets.

**Parameters**: None

**Returns**: 
- **Android**: `List<Tuning>`
- **iOS**: `[Tuning]`
- **WebApp**: `Tuning[]`

**Data Source**: Hardcoded presets loaded from `shared/tuning-presets/presets.json`

**Caching**: Results cached in memory after first load (static data)

---

#### `getTuningById(id: String)`

Retrieves a specific tuning by its ID.

**Parameters**:
- `id`: Tuning identifier (e.g., "standard", "drop_d")

**Returns**: `Tuning?` (nullable/optional)

**Behavior**:
- Returns tuning if found
- Returns `null`/`nil` if ID doesn't exist

---

#### `getSelectedTuning()`

Retrieves the user's currently selected tuning.

**Parameters**: None

**Returns**: `Tuning`

**Data Source**: Reads from `TunerSettings.selectedTuningId`, looks up corresponding `Tuning`

**Default**: Returns "standard" tuning if no selection saved or ID invalid

---

#### `setSelectedTuning(tuningId: String)`

Sets the user's selected tuning and persists to storage.

**Parameters**:
- `tuningId`: ID of tuning to select

**Returns**: 
- **Android**: `Result<Unit>` (Success or Failure if ID invalid)
- **iOS**: `Result<Void, RepositoryError>` 
- **WebApp**: `Promise<void>` (rejects if ID invalid)

**Side Effects**:
- Updates `TunerSettings.selectedTuningId`
- Persists to SharedPreferences/UserDefaults/localStorage
- Notifies observers of tuning change

**Validation**: Ensures `tuningId` exists in preset list

---

### Platform-Specific Implementations

#### Android (Kotlin Interface)

```kotlin
interface ITuningRepository {
    fun getAllTunings(): List<Tuning>
    fun getTuningById(id: String): Tuning?
    fun getSelectedTuning(): Tuning
    suspend fun setSelectedTuning(tuningId: String): Result<Unit>
    fun observeSelectedTuning(): Flow<Tuning>
}
```

**Implementation Class**: `TuningRepositoryImpl`

---

#### iOS (Swift Protocol)

```swift
protocol ITuningRepository {
    func getAllTunings() -> [Tuning]
    func getTuningById(id: String) -> Tuning?
    func getSelectedTuning() -> Tuning
    func setSelectedTuning(tuningId: String) async -> Result<Void, RepositoryError>
    func observeSelectedTuning() -> AnyPublisher<Tuning, Never>
}
```

**Implementation Class**: `TuningRepository`

---

#### WebApp (TypeScript Interface)

```typescript
export interface ITuningRepository {
  getAllTunings(): Tuning[];
  getTuningById(id: string): Tuning | null;
  getSelectedTuning(): Tuning;
  setSelectedTuning(tuningId: string): Promise<void>;
  observeSelectedTuning(callback: (tuning: Tuning) => void): () => void; // Returns unsubscribe
}
```

**Implementation Class**: `TuningRepository`

---

## Interface: ISettingsRepository

Manages user settings persistence and retrieval.

### Methods

#### `getSettings()`

Retrieves current user settings.

**Parameters**: None

**Returns**: `TunerSettings`

**Data Source**: Reads from SharedPreferences/UserDefaults/localStorage

**Default**: Returns `TunerSettings.DEFAULT` if no settings saved

---

#### `saveSettings(settings: TunerSettings)`

Persists user settings to storage.

**Parameters**:
- `settings`: `TunerSettings` object to save

**Returns**: 
- **Android**: `Result<Unit>`
- **iOS**: `Result<Void, RepositoryError>`
- **WebApp**: `Promise<void>`

**Side Effects**:
- Writes to platform-specific storage
- Notifies observers of settings change

---

#### `observeSettings()`

Provides reactive stream of settings changes.

**Parameters**: None

**Returns**:
- **Android**: `Flow<TunerSettings>`
- **iOS**: `AnyPublisher<TunerSettings, Never>`
- **WebApp**: Callback-based observer or RxJS Observable

---

### Platform-Specific Implementations

#### Android (Kotlin Interface)

```kotlin
interface ISettingsRepository {
    fun getSettings(): TunerSettings
    suspend fun saveSettings(settings: TunerSettings): Result<Unit>
    fun observeSettings(): Flow<TunerSettings>
}
```

**Implementation Class**: `SettingsRepositoryImpl` (uses SharedPreferences)

---

#### iOS (Swift Protocol)

```swift
protocol ISettingsRepository {
    func getSettings() -> TunerSettings
    func saveSettings(settings: TunerSettings) async -> Result<Void, RepositoryError>
    func observeSettings() -> AnyPublisher<TunerSettings, Never>
}
```

**Implementation Class**: `SettingsRepository` (uses UserDefaults)

---

#### WebApp (TypeScript Interface)

```typescript
export interface ISettingsRepository {
  getSettings(): TunerSettings;
  saveSettings(settings: TunerSettings): Promise<void>;
  observeSettings(callback: (settings: TunerSettings) => void): () => void;
}
```

**Implementation Class**: `SettingsRepository` (uses localStorage)

---

## Error Handling Contract

### Standard Error Types

All interfaces must handle and propagate the following error types:

1. **PermissionError**: Microphone access denied
2. **HardwareError**: Audio hardware unavailable or malfunctioning
3. **ValidationError**: Invalid input parameters
4. **StorageError**: Failed to read/write to persistent storage
5. **NotFoundError**: Requested resource (e.g., tuning ID) doesn't exist

### Error Response Format

**Android**: Use Kotlin `Result<T>` type or custom sealed class hierarchy

**iOS**: Use Swift `Result<T, Error>` or `throws` for async functions

**WebApp**: Use Promise rejection or throw Error for async functions

---

## Performance SLAs (Service Level Agreements)

### Response Time Requirements

| Operation | Max Latency | Target Latency | Notes |
|-----------|-------------|----------------|-------|
| `IAudioProcessor.start()` | 500ms | 200ms | Includes permission prompt if needed |
| `IPitchDetector.detectPitch()` | 10ms | 5ms | Must not block audio thread |
| `IFrequencyConverter.frequencyToNote()` | 1ms | <0.5ms | Pure computation, no I/O |
| `ITuningRepository.getAllTunings()` | 50ms | 10ms | First call (cache miss), subsequent calls < 1ms |
| `ISettingsRepository.saveSettings()` | 100ms | 50ms | Disk I/O operation |

### Throughput Requirements

- `observePitchResults()` emission rate: 10-60 Hz (configurable), default 20 Hz
- FFT processing rate: 20-50 Hz (depends on buffer size and sample rate)

---

## Thread Safety Contract

### Thread Usage

**Android**:
- `IAudioProcessor`: Audio capture on background thread (IO Dispatcher), FFT on Default Dispatcher
- Repositories: Main-safe (can call from any thread), suspend functions switch to IO Dispatcher internally
- Observers emit on Main Dispatcher by default

**iOS**:
- `IAudioProcessor`: Audio capture on AVAudioEngine's internal thread, FFT on background queue
- Repositories: Main actor isolated for thread safety
- Publishers deliver on Main queue

**WebApp**:
- `IAudioProcessor`: Audio capture on AudioWorklet thread (audio rendering thread)
- FFT computation in WebAssembly (separate thread via Worker if needed)
- Callbacks invoked on main thread (UI thread)

### Synchronization

- All observable streams are thread-safe
- Repository methods are synchronized internally (no external locking required)
- Audio buffers use lock-free ring buffers or atomic operations

---

## Testing Contracts

### Mock Implementations

Each interface must have a mock/stub implementation for testing:

1. **MockAudioProcessor**: Returns pre-defined `PitchResult` sequence
2. **MockPitchDetector**: Returns deterministic results for known FFT inputs
3. **MockFrequencyConverter**: Validates conversion formulas
4. **MockTuningRepository**: In-memory tuning storage
5. **MockSettingsRepository**: In-memory settings storage

### Test Fixtures

Shared test data located in:
- Android: `src/test/resources/audio-samples/`
- iOS: `MyTuneTests/Fixtures/`
- WebApp: `tests/fixtures/audio/`

---

## Versioning & Compatibility

**Current Version**: 1.0.0

**Breaking Changes Policy**:
- Major version bump (2.0.0): Method signature changes, removed methods
- Minor version bump (1.1.0): New methods added (backward compatible)
- Patch version bump (1.0.1): Implementation bug fixes, no interface changes

**Deprecation Policy**:
- Methods marked `@Deprecated` (Android), `@available(..., deprecated)` (iOS), `@deprecated` (WebApp)
- Deprecated methods supported for 1 major version before removal

---

## Conclusion

These interface contracts define clear boundaries between the audio processing layer and application layer, enabling independent development and testing of each platform while maintaining consistent behavior. All interfaces are designed for testability, performance, and thread safety.

**Next Steps**: Use these contracts to implement concrete classes on each platform, ensuring all implementations conform to the specified behavior, performance SLAs, and error handling requirements.
