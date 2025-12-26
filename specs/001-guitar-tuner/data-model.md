# Data Model: Guitar Tuner Application

**Feature**: Guitar Tuner (001-guitar-tuner)  
**Date**: 2025-12-08  
**Phase**: 1 - Design & Contracts

## Overview

This document defines all data entities, their relationships, validation rules, and state transitions for the Guitar Tuner application. These models are implemented consistently across all three platforms (Android, iOS, WebApp) with platform-specific serialization and storage mechanisms.

---

## Entity Definitions

### 1. Tuning

Represents a guitar tuning configuration with target notes for each of the six strings.

#### Properties

| Property | Type | Description | Validation | Required |
|----------|------|-------------|------------|----------|
| `id` | String | Unique identifier for the tuning (e.g., "standard", "drop_d") | Lowercase alphanumeric with underscores, max 50 chars | Yes |
| `name` | String | Display name for the tuning (e.g., "Standard", "Drop D") | Non-empty, max 100 chars | Yes |
| `displayName` | Map<String, String> | Localized display names (key: language code, value: localized name) | Keys must be valid ISO 639-1 codes | No |
| `strings` | List\<GuitarString\> | Ordered list of 6 guitar strings from lowest to highest pitch | Must contain exactly 6 strings, positions 1-6 | Yes |

#### Example (JSON representation)

```json
{
  "id": "standard",
  "name": "Standard",
  "displayName": {
    "en": "Standard",
    "es": "Estándar"
  },
  "strings": [
    {"position": 6, "note": "E", "octave": 2, "frequency": 82.41},
    {"position": 5, "note": "A", "octave": 2, "frequency": 110.00},
    {"position": 4, "note": "D", "octave": 3, "frequency": 146.83},
    {"position": 3, "note": "G", "octave": 3, "frequency": 196.00},
    {"position": 2, "note": "B", "octave": 3, "frequency": 246.94},
    {"position": 1, "note": "E", "octave": 4, "frequency": 329.63}
  ]
}
```

#### Platform Implementations

**Android (Kotlin)**:
```kotlin
data class Tuning(
    val id: String,
    val name: String,
    val displayName: Map<String, String> = emptyMap(),
    val strings: List<GuitarString>
) {
    init {
        require(id.isNotBlank()) { "Tuning ID cannot be blank" }
        require(name.isNotBlank()) { "Tuning name cannot be blank" }
        require(strings.size == 6) { "Tuning must have exactly 6 strings" }
        require(strings.map { it.position }.sorted() == (1..6).toList()) {
            "String positions must be 1-6 without duplicates"
        }
    }
    
    fun getLocalizedName(languageCode: String): String {
        return displayName[languageCode] ?: name
    }
}
```

**iOS (Swift)**:
```swift
struct Tuning: Codable, Identifiable {
    let id: String
    let name: String
    let displayName: [String: String]
    let strings: [GuitarString]
    
    init(id: String, name: String, displayName: [String: String] = [:], strings: [GuitarString]) {
        precondition(!id.isEmpty, "Tuning ID cannot be empty")
        precondition(!name.isEmpty, "Tuning name cannot be empty")
        precondition(strings.count == 6, "Tuning must have exactly 6 strings")
        precondition(Set(strings.map { $0.position }).sorted() == [1, 2, 3, 4, 5, 6],
                     "String positions must be 1-6 without duplicates")
        
        self.id = id
        self.name = name
        self.displayName = displayName
        self.strings = strings.sorted { $0.position > $1.position }
    }
    
    func localizedName(languageCode: String) -> String {
        return displayName[languageCode] ?? name
    }
}
```

**WebApp (TypeScript)**:
```typescript
export interface Tuning {
  id: string;
  name: string;
  displayName?: Record<string, string>;
  strings: GuitarString[];
}

export function validateTuning(tuning: Tuning): void {
  if (!tuning.id || tuning.id.trim() === '') {
    throw new Error('Tuning ID cannot be empty');
  }
  if (!tuning.name || tuning.name.trim() === '') {
    throw new Error('Tuning name cannot be empty');
  }
  if (tuning.strings.length !== 6) {
    throw new Error('Tuning must have exactly 6 strings');
  }
  const positions = tuning.strings.map(s => s.position).sort();
  if (positions.join(',') !== '1,2,3,4,5,6') {
    throw new Error('String positions must be 1-6 without duplicates');
  }
}

export function getLocalizedName(tuning: Tuning, languageCode: string): string {
  return tuning.displayName?.[languageCode] ?? tuning.name;
}
```

#### Relationships
- **Contains**: 6 `GuitarString` entities (composition, cascade delete)
- **Referenced by**: `TunerState` (current active tuning)

---

### 2. GuitarString

Represents a single guitar string with its target pitch information.

#### Properties

| Property | Type | Description | Validation | Required |
|----------|------|-------------|------------|----------|
| `position` | Integer | String position (1 = highest pitch E string, 6 = lowest pitch E string) | Must be 1-6 inclusive | Yes |
| `note` | String | Musical note name (e.g., "E", "A", "D#", "Bb") | Must be valid note name (A-G with optional sharp/flat) | Yes |
| `octave` | Integer | Scientific pitch notation octave number | Must be 0-9 (guitar range typically 2-4) | Yes |
| `frequency` | Double | Target frequency in Hertz (Hz) | Must be > 0, typically 60-350 Hz for guitar | Yes |

#### Example (JSON representation)

```json
{
  "position": 5,
  "note": "A",
  "octave": 2,
  "frequency": 110.00
}
```

#### Platform Implementations

**Android (Kotlin)**:
```kotlin
data class GuitarString(
    val position: Int,
    val note: String,
    val octave: Int,
    val frequency: Double
) {
    init {
        require(position in 1..6) { "String position must be 1-6" }
        require(note.matches(Regex("^[A-G][#b]?$"))) { "Invalid note name: $note" }
        require(octave in 0..9) { "Octave must be 0-9" }
        require(frequency > 0) { "Frequency must be positive" }
    }
    
    val fullNoteName: String
        get() = "$note$octave"
}
```

**iOS (Swift)**:
```swift
struct GuitarString: Codable, Identifiable {
    let position: Int
    let note: String
    let octave: Int
    let frequency: Double
    
    var id: Int { position }
    
    init(position: Int, note: String, octave: Int, frequency: Double) {
        precondition((1...6).contains(position), "String position must be 1-6")
        precondition(note.range(of: "^[A-G][#b]?$", options: .regularExpression) != nil,
                     "Invalid note name: \(note)")
        precondition((0...9).contains(octave), "Octave must be 0-9")
        precondition(frequency > 0, "Frequency must be positive")
        
        self.position = position
        self.note = note
        self.octave = octave
        self.frequency = frequency
    }
    
    var fullNoteName: String {
        return "\(note)\(octave)"
    }
}
```

**WebApp (TypeScript)**:
```typescript
export interface GuitarString {
  position: number;
  note: string;
  octave: number;
  frequency: number;
}

export function validateGuitarString(string: GuitarString): void {
  if (string.position < 1 || string.position > 6) {
    throw new Error('String position must be 1-6');
  }
  if (!/^[A-G][#b]?$/.test(string.note)) {
    throw new Error(`Invalid note name: ${string.note}`);
  }
  if (string.octave < 0 || string.octave > 9) {
    throw new Error('Octave must be 0-9');
  }
  if (string.frequency <= 0) {
    throw new Error('Frequency must be positive');
  }
}

export function getFullNoteName(string: GuitarString): string {
  return `${string.note}${string.octave}`;
}
```

#### Relationships
- **Owned by**: `Tuning` (composition)
- **Referenced by**: `PitchResult` (matched target string, optional)

---

### 3. PitchResult

Represents the result of real-time pitch detection from audio input.

#### Properties

| Property | Type | Description | Validation | Required |
|----------|------|-------------|------------|----------|
| `frequency` | Double | Detected frequency in Hertz (Hz) | Must be > 0, null if no pitch detected | No |
| `note` | String | Closest musical note name (e.g., "A", "D#") | Valid note name, null if no pitch detected | No |
| `octave` | Integer | Octave number for detected note | 0-9, null if no pitch detected | No |
| `cents` | Integer | Deviation from target note in cents (-50 to +50) | -100 to +100 (±1 semitone), null if no pitch detected | No |
| `confidence` | Double | Detection confidence score (0.0 to 1.0) | 0.0-1.0, higher is more confident | Yes |
| `timestamp` | Long | Unix timestamp in milliseconds when pitch was detected | Must be > 0 | Yes |
| `targetString` | GuitarString | Matched target string from current tuning (if detected note is close to a string) | Null if no match within ±50 cents | No |

#### States

```
┌─────────────┐
│   Silent    │  confidence < 0.3, frequency = null
└──────┬──────┘
       │ Audio detected
       ▼
┌─────────────┐
│  Detecting  │  confidence 0.3-0.7, frequency present, note may be ambiguous
└──────┬──────┘
       │ Clear pitch identified
       ▼
┌─────────────┐
│   Active    │  confidence > 0.7, frequency/note/cents all present
└──────┬──────┘
       │
       ├─→ InTune (|cents| ≤ 10)      → Green indicator, confirmation animation
       ├─→ SlightlyFlat (-10 > cents ≥ -25) → Yellow indicator, below center
       ├─→ VeryFlat (cents < -25)      → Red indicator, far below center
       ├─→ SlightlySharp (10 < cents ≤ 25)  → Yellow indicator, above center
       └─→ VerySharp (cents > 25)      → Red indicator, far above center
```

#### Example (JSON representation)

```json
{
  "frequency": 112.5,
  "note": "A",
  "octave": 2,
  "cents": 31,
  "confidence": 0.92,
  "timestamp": 1702051234567,
  "targetString": {
    "position": 5,
    "note": "A",
    "octave": 2,
    "frequency": 110.00
  }
}
```

#### Platform Implementations

**Android (Kotlin)**:
```kotlin
data class PitchResult(
    val frequency: Double?,
    val note: String?,
    val octave: Int?,
    val cents: Int?,
    val confidence: Double,
    val timestamp: Long,
    val targetString: GuitarString? = null
) {
    init {
        require(confidence in 0.0..1.0) { "Confidence must be 0.0-1.0" }
        require(timestamp > 0) { "Timestamp must be positive" }
        
        if (frequency != null) {
            require(frequency > 0) { "Frequency must be positive" }
        }
        if (cents != null) {
            require(cents in -100..100) { "Cents must be -100 to +100" }
        }
    }
    
    val tuningState: TuningState
        get() = when {
            frequency == null || confidence < 0.3 -> TuningState.SILENT
            confidence < 0.7 -> TuningState.DETECTING
            cents == null -> TuningState.ACTIVE
            cents in -10..10 -> TuningState.IN_TUNE
            cents in -25..-11 -> TuningState.SLIGHTLY_FLAT
            cents < -25 -> TuningState.VERY_FLAT
            cents in 11..25 -> TuningState.SLIGHTLY_SHARP
            else -> TuningState.VERY_SHARP
        }
}

enum class TuningState {
    SILENT, DETECTING, ACTIVE, IN_TUNE, 
    SLIGHTLY_FLAT, VERY_FLAT, SLIGHTLY_SHARP, VERY_SHARP
}
```

**iOS (Swift)**:
```swift
struct PitchResult {
    let frequency: Double?
    let note: String?
    let octave: Int?
    let cents: Int?
    let confidence: Double
    let timestamp: Int64
    let targetString: GuitarString?
    
    init(frequency: Double?, note: String?, octave: Int?, cents: Int?, 
         confidence: Double, timestamp: Int64, targetString: GuitarString? = nil) {
        precondition((0...1).contains(confidence), "Confidence must be 0.0-1.0")
        precondition(timestamp > 0, "Timestamp must be positive")
        
        if let freq = frequency {
            precondition(freq > 0, "Frequency must be positive")
        }
        if let c = cents {
            precondition((-100...100).contains(c), "Cents must be -100 to +100")
        }
        
        self.frequency = frequency
        self.note = note
        self.octave = octave
        self.cents = cents
        self.confidence = confidence
        self.timestamp = timestamp
        self.targetString = targetString
    }
    
    var tuningState: TuningState {
        switch (frequency, confidence, cents) {
        case (nil, _, _), (_, ..<0.3, _):
            return .silent
        case (_, ..<0.7, _):
            return .detecting
        case (_, _, nil):
            return .active
        case (_, _, let c?) where (-10...10).contains(c):
            return .inTune
        case (_, _, let c?) where (-25...(-11)).contains(c):
            return .slightlyFlat
        case (_, _, let c?) where c < -25:
            return .veryFlat
        case (_, _, let c?) where (11...25).contains(c):
            return .slightlySharp
        default:
            return .verySharp
        }
    }
}

enum TuningState {
    case silent, detecting, active, inTune
    case slightlyFlat, veryFlat, slightlySharp, verySharp
}
```

**WebApp (TypeScript)**:
```typescript
export interface PitchResult {
  frequency: number | null;
  note: string | null;
  octave: number | null;
  cents: number | null;
  confidence: number;
  timestamp: number;
  targetString?: GuitarString;
}

export enum TuningState {
  Silent = 'silent',
  Detecting = 'detecting',
  Active = 'active',
  InTune = 'inTune',
  SlightlyFlat = 'slightlyFlat',
  VeryFlat = 'veryFlat',
  SlightlySharp = 'slightlySharp',
  VerySharp = 'verySharp',
}

export function validatePitchResult(result: PitchResult): void {
  if (result.confidence < 0 || result.confidence > 1) {
    throw new Error('Confidence must be 0.0-1.0');
  }
  if (result.timestamp <= 0) {
    throw new Error('Timestamp must be positive');
  }
  if (result.frequency !== null && result.frequency <= 0) {
    throw new Error('Frequency must be positive');
  }
  if (result.cents !== null && (result.cents < -100 || result.cents > 100)) {
    throw new Error('Cents must be -100 to +100');
  }
}

export function getTuningState(result: PitchResult): TuningState {
  if (result.frequency === null || result.confidence < 0.3) {
    return TuningState.Silent;
  }
  if (result.confidence < 0.7) {
    return TuningState.Detecting;
  }
  if (result.cents === null) {
    return TuningState.Active;
  }
  
  const cents = result.cents;
  if (cents >= -10 && cents <= 10) return TuningState.InTune;
  if (cents >= -25 && cents < -10) return TuningState.SlightlyFlat;
  if (cents < -25) return TuningState.VeryFlat;
  if (cents > 10 && cents <= 25) return TuningState.SlightlySharp;
  return TuningState.VerySharp;
}
```

#### Relationships
- **References**: `GuitarString` (optional target string match)
- **Consumed by**: UI layer for visual indicator updates

---

### 4. TunerSettings

Represents user preferences and app configuration.

#### Properties

| Property | Type | Description | Validation | Required |
|----------|------|-------------|------------|----------|
| `selectedTuningId` | String | ID of currently selected tuning preset | Must match existing tuning ID | Yes |
| `themeMode` | Enum | UI theme preference (Light, Dark, System) | One of: "light", "dark", "system" | Yes |
| `enableHapticFeedback` | Boolean | Whether to vibrate on in-tune detection | true/false | Yes |
| `noiseGateThreshold` | Double | Minimum confidence level to display pitch (0.0-1.0) | 0.0-1.0, default 0.3 | Yes |
| `frequencyUpdateRate` | Integer | Hz for UI updates (10-60) | 10-60, default 20 | Yes |

#### Example (JSON representation)

```json
{
  "selectedTuningId": "standard",
  "themeMode": "system",
  "enableHapticFeedback": true,
  "noiseGateThreshold": 0.3,
  "frequencyUpdateRate": 20
}
```

#### Platform Implementations

**Android (Kotlin)**:
```kotlin
data class TunerSettings(
    val selectedTuningId: String,
    val themeMode: ThemeMode,
    val enableHapticFeedback: Boolean,
    val noiseGateThreshold: Double,
    val frequencyUpdateRate: Int
) {
    init {
        require(noiseGateThreshold in 0.0..1.0) { "Noise gate must be 0.0-1.0" }
        require(frequencyUpdateRate in 10..60) { "Update rate must be 10-60 Hz" }
    }
    
    companion object {
        val DEFAULT = TunerSettings(
            selectedTuningId = "standard",
            themeMode = ThemeMode.SYSTEM,
            enableHapticFeedback = true,
            noiseGateThreshold = 0.3,
            frequencyUpdateRate = 20
        )
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
```

**iOS (Swift)**:
```swift
struct TunerSettings: Codable {
    let selectedTuningId: String
    let themeMode: ThemeMode
    let enableHapticFeedback: Bool
    let noiseGateThreshold: Double
    let frequencyUpdateRate: Int
    
    init(selectedTuningId: String, themeMode: ThemeMode, 
         enableHapticFeedback: Bool, noiseGateThreshold: Double, 
         frequencyUpdateRate: Int) {
        precondition((0...1).contains(noiseGateThreshold), "Noise gate must be 0.0-1.0")
        precondition((10...60).contains(frequencyUpdateRate), "Update rate must be 10-60 Hz")
        
        self.selectedTuningId = selectedTuningId
        self.themeMode = themeMode
        self.enableHapticFeedback = enableHapticFeedback
        self.noiseGateThreshold = noiseGateThreshold
        self.frequencyUpdateRate = frequencyUpdateRate
    }
    
    static let `default` = TunerSettings(
        selectedTuningId: "standard",
        themeMode: .system,
        enableHapticFeedback: true,
        noiseGateThreshold: 0.3,
        frequencyUpdateRate: 20
    )
}

enum ThemeMode: String, Codable {
    case light, dark, system
}
```

**WebApp (TypeScript)**:
```typescript
export interface TunerSettings {
  selectedTuningId: string;
  themeMode: ThemeMode;
  enableHapticFeedback: boolean;
  noiseGateThreshold: number;
  frequencyUpdateRate: number;
}

export enum ThemeMode {
  Light = 'light',
  Dark = 'dark',
  System = 'system',
}

export function validateTunerSettings(settings: TunerSettings): void {
  if (settings.noiseGateThreshold < 0 || settings.noiseGateThreshold > 1) {
    throw new Error('Noise gate must be 0.0-1.0');
  }
  if (settings.frequencyUpdateRate < 10 || settings.frequencyUpdateRate > 60) {
    throw new Error('Update rate must be 10-60 Hz');
  }
}

export const DEFAULT_TUNER_SETTINGS: TunerSettings = {
  selectedTuningId: 'standard',
  themeMode: ThemeMode.System,
  enableHapticFeedback: true,
  noiseGateThreshold: 0.3,
  frequencyUpdateRate: 20,
};
```

#### Persistence
- **Android**: SharedPreferences (`"tuner_settings"` key, JSON serialization)
- **iOS**: UserDefaults (`"TunerSettings"` key, Codable JSON)
- **WebApp**: localStorage (`"my-tune-settings"` key, JSON.stringify)

---

## Entity Relationship Diagram

```
┌─────────────────┐
│  TunerSettings  │
│─────────────────│
│ selectedTuningId├─────┐ (references)
│ themeMode       │     │
│ enableHaptic... │     │
│ noiseGate...    │     │
│ frequencyUp...  │     │
└─────────────────┘     │
                        ▼
                   ┌─────────────────┐
                   │     Tuning      │
                   │─────────────────│
                   │ id (PK)         │
                   │ name            │
                   │ displayName     │
                   │ strings         │◄──────────┐ (contains)
                   └─────────────────┘           │
                                                 │
                                          ┌──────┴──────┐
                                          │ GuitarString│
                                          │─────────────│
                                          │ position    │
                                          │ note        │
                                          │ octave      │
                                          │ frequency   │
                                          └──────▲──────┘
                                                 │
                                                 │ (references, optional)
                                                 │
                                          ┌──────┴──────┐
                                          │ PitchResult │
                                          │─────────────│
                                          │ frequency   │
                                          │ note        │
                                          │ octave      │
                                          │ cents       │
                                          │ confidence  │
                                          │ timestamp   │
                                          │ targetString│
                                          └─────────────┘
```

**Legend**:
- `─►` : References (foreign key / ID reference)
- `◄──` : Contains (composition / ownership)
- `(PK)` : Primary key / unique identifier

---

## State Transitions

### Application State Machine

```
┌────────────────┐
│  Initializing  │  Loading tuning presets, setting up audio engine
└───────┬────────┘
        │ Audio permission granted
        ▼
┌────────────────┐
│ Ready (Idle)   │  Audio engine initialized, waiting for input
└───────┬────────┘
        │ Audio input detected
        ▼
┌────────────────┐
│   Listening    │  Capturing audio buffers, running FFT
└───────┬────────┘
        │ Pitch detected
        ▼
┌────────────────┐
│    Tuning      │  Displaying pitch result, updating visual indicator
└───────┬────────┘
        │
        ├─→ In Tune (|cents| ≤ 10)   → Trigger confirmation (green + animation + haptic)
        ├─→ Out of Tune              → Continue listening
        └─→ Silence detected          → Return to Listening state
        
        
┌────────────────┐
│     Error      │  Microphone permission denied, audio hardware failure
└────────────────┘
        │ User grants permission / resolves issue
        └─────────► Return to Ready state
```

### Tuning Selection Flow

```
┌─────────────────┐
│  Main Screen    │  Displaying current tuning, pitch detection active
└────────┬────────┘
         │ User taps Settings button
         ▼
┌─────────────────┐
│ Settings Modal  │  List of tuning presets displayed
└────────┬────────┘
         │ User selects new tuning
         ▼
┌─────────────────┐
│ Tuning Changed  │  Update TunerSettings, persist to storage, notify UI
└────────┬────────┘
         │ Modal dismissed
         ▼
┌─────────────────┐
│  Main Screen    │  Visual indicator shows new target notes
└─────────────────┘
```

---

## Validation Rules Summary

### Cross-Entity Validation

1. **Tuning Completeness**:
   - Every `Tuning` must have exactly 6 `GuitarString` entries
   - String positions must be unique (1-6)
   - Frequencies must be monotonically increasing from position 6 → 1

2. **Pitch Detection Thresholds**:
   - `PitchResult.confidence` < 0.3 → Treat as silence (do not display pitch)
   - `PitchResult.confidence` 0.3-0.7 → Display note but show "Detecting..." state
   - `PitchResult.confidence` > 0.7 → High confidence, show tuning state

3. **UI Update Throttling**:
   - `PitchResult` emissions limited by `TunerSettings.frequencyUpdateRate` (default 20 Hz = every 50ms)
   - Prevents excessive UI recompositions/re-renders

4. **Haptic Feedback Trigger**:
   - Only trigger when:
     - `TunerSettings.enableHapticFeedback` = true
     - `PitchResult.tuningState` = IN_TUNE
     - Previous state was NOT IN_TUNE (prevent continuous vibration)
     - Device supports haptic feedback (platform capability check)

---

## Data Flow Architecture

### Read Path (Pitch Detection → UI)

```
Microphone Input
    │
    ▼
AudioRecorder / AVAudioEngine / Web Audio API
    │ (Raw PCM samples)
    ▼
FFT Processor (background thread/worklet)
    │ (Frequency spectrum)
    ▼
Pitch Detector (HPS algorithm)
    │ (Frequency + confidence)
    ▼
Frequency Converter (Hz → Note + Cents)
    │ (PitchResult object)
    ▼
String Matcher (find closest target string)
    │ (PitchResult with targetString)
    ▼
ViewModel / State Manager (rate-limited updates)
    │ (StateFlow / @Published / React State)
    ▼
UI Layer (recompose/redraw visual indicator)
```

### Write Path (User Settings → Storage)

```
User Interaction (select tuning, toggle theme)
    │
    ▼
UI Event Handler
    │
    ▼
ViewModel / Settings Manager
    │ (Update in-memory state)
    ▼
Storage Layer (SharedPreferences/UserDefaults/localStorage)
    │ (Persist JSON)
    ▼
Confirmation (UI reflects new setting)
```

---

## Performance Considerations

### Memory Footprint

| Entity | Estimated Size | Quantity | Total |
|--------|----------------|----------|-------|
| `Tuning` (single) | ~300 bytes | 5-10 presets | ~3 KB |
| `GuitarString` | ~50 bytes | 60 (10 tunings × 6 strings) | ~3 KB |
| `PitchResult` (single) | ~100 bytes | 1 (current state only) | ~100 bytes |
| `TunerSettings` | ~200 bytes | 1 | ~200 bytes |
| **Total data model** | | | **~6.3 KB** |

**Note**: Audio buffers (8192 samples × 4 bytes = ~32 KB per buffer) are the primary memory consumers, not data entities.

### Database vs In-Memory

**Decision**: No database required for MVP. All data held in memory with lightweight persistence.

**Rationale**:
- Tuning presets are static and small (<10 presets, ~3 KB total)
- Only one `PitchResult` active at a time
- Settings are a single object (~200 bytes)
- Total data footprint < 10 KB
- SQLite/CoreData/IndexedDB overhead (>100 KB) exceeds benefit

**Future Consideration**: If adding features like tuning history, custom tunings, or session recordings, consider lightweight embedded database (Realm, SQLite).

---

## Localization Support

### Localizable Strings

The following entity properties support localization:

1. **Tuning.displayName**: Map of language codes to localized tuning names
   - Example: `{"en": "Standard", "es": "Estándar", "fr": "Standard", "de": "Standard"}`

2. **Note Names**: Some languages use different note naming conventions
   - English: C D E F G A B
   - German: C D E F G A H (B is "H")
   - Romance languages (Do Re Mi Fa Sol La Si): Optional future support

**Implementation**:
- Platform-specific localization files (Android `strings.xml`, iOS `Localizable.strings`, WebApp `i18n` library)
- `Tuning.getLocalizedName(languageCode)` method retrieves display name for current locale
- Default to English (`name` property) if translation unavailable

---

## Conclusion

This data model provides a complete, validated, and platform-consistent foundation for the Guitar Tuner application. All entities include comprehensive validation, clear relationships, and defined state transitions. The lightweight design (< 10 KB total) enables in-memory storage with simple key-value persistence, meeting performance targets while maintaining data integrity.

**Next Steps**: Generate API contracts (internal interfaces between audio engine and UI layer) in Phase 1.
