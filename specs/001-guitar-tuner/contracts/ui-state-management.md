# UI State Management Contract

**Module**: Presentation Layer  
**Version**: 1.0.0  
**Last Updated**: 2025-12-08

## Overview

Defines the state management contract between ViewModels/State Managers and UI components across all platforms. Specifies state structures, state transitions, and update mechanisms for the Guitar Tuner application.

---

## State: TunerViewState

Represents the complete UI state for the main tuner screen.

### Properties

| Property | Type | Description | Default Value |
|----------|------|-------------|---------------|
| `currentPitchResult` | PitchResult? | Most recent pitch detection result | null |
| `selectedTuning` | Tuning | Currently active tuning preset | Standard tuning |
| `tuningState` | TuningState | Derived state for visual indicator | Silent |
| `isListening` | Boolean | Whether audio capture is active | false |
| `error` | AudioError? | Current error state (e.g., permission denied) | null |
| `showInTuneConfirmation` | Boolean | Trigger for "in tune" animation | false |
| `markerPosition` | Float | Position of tuning marker (0.0-1.0, 0.5 = center) | 0.5 |
| `markerColor` | Color | Color of tuning marker (red/yellow/green) | theme default |

### Derived Properties

These are computed from other state properties and not stored directly:

- `displayNote`: String (e.g., "A") from `currentPitchResult.note`
- `displayOctave`: Int (e.g., 2) from `currentPitchResult.octave`
- `displayCents`: Int? (e.g., +15) from `currentPitchResult.cents`
- `targetStringIndicators`: List of 6 indicators showing target notes for current tuning

---

### Platform Implementations

#### Android (Kotlin)

```kotlin
data class TunerViewState(
    val currentPitchResult: PitchResult? = null,
    val selectedTuning: Tuning = TuningPresets.STANDARD,
    val isListening: Boolean = false,
    val error: AudioError? = null,
    val showInTuneConfirmation: Boolean = false
) {
    val tuningState: TuningState
        get() = currentPitchResult?.tuningState ?: TuningState.SILENT
    
    val markerPosition: Float
        get() = when (val cents = currentPitchResult?.cents) {
            null -> 0.5f
            else -> 0.5f + (cents / 100f * 0.5f).coerceIn(-0.5f, 0.5f)
        }
    
    val markerColor: Color
        get() = when (tuningState) {
            TuningState.IN_TUNE -> Color.Green
            TuningState.SLIGHTLY_FLAT, TuningState.SLIGHTLY_SHARP -> Color.Yellow
            TuningState.VERY_FLAT, TuningState.VERY_SHARP -> Color.Red
            else -> Color.Gray
        }
    
    val displayNote: String?
        get() = currentPitchResult?.note
    
    val displayOctave: Int?
        get() = currentPitchResult?.octave
    
    val displayCents: Int?
        get() = currentPitchResult?.cents
}
```

#### iOS (Swift)

```swift
struct TunerViewState {
    var currentPitchResult: PitchResult?
    var selectedTuning: Tuning
    var isListening: Bool
    var error: AudioError?
    var showInTuneConfirmation: Bool
    
    var tuningState: TuningState {
        currentPitchResult?.tuningState ?? .silent
    }
    
    var markerPosition: CGFloat {
        guard let cents = currentPitchResult?.cents else { return 0.5 }
        let offset = CGFloat(cents) / 100.0 * 0.5
        return (0.5 + offset).clamped(to: 0.0...1.0)
    }
    
    var markerColor: Color {
        switch tuningState {
        case .inTune: return .green
        case .slightlyFlat, .slightlySharp: return .yellow
        case .veryFlat, .verySharp: return .red
        default: return .gray
        }
    }
    
    var displayNote: String? {
        currentPitchResult?.note
    }
    
    var displayOctave: Int? {
        currentPitchResult?.octave
    }
    
    var displayCents: Int? {
        currentPitchResult?.cents
    }
    
    init(currentPitchResult: PitchResult? = nil,
         selectedTuning: Tuning = TuningPresets.standard,
         isListening: Bool = false,
         error: AudioError? = nil,
         showInTuneConfirmation: Bool = false) {
        self.currentPitchResult = currentPitchResult
        self.selectedTuning = selectedTuning
        self.isListening = isListening
        self.error = error
        self.showInTuneConfirmation = showInTuneConfirmation
    }
}
```

#### WebApp (TypeScript)

```typescript
export interface TunerViewState {
  currentPitchResult: PitchResult | null;
  selectedTuning: Tuning;
  isListening: boolean;
  error: AudioError | null;
  showInTuneConfirmation: boolean;
}

export function getTuningState(state: TunerViewState): TuningState {
  return state.currentPitchResult 
    ? getTuningStateFromResult(state.currentPitchResult)
    : TuningState.Silent;
}

export function getMarkerPosition(state: TunerViewState): number {
  const cents = state.currentPitchResult?.cents;
  if (cents === null || cents === undefined) return 0.5;
  
  const offset = (cents / 100) * 0.5;
  return Math.max(0, Math.min(1, 0.5 + offset));
}

export function getMarkerColor(state: TunerViewState): string {
  const tuningState = getTuningState(state);
  switch (tuningState) {
    case TuningState.InTune: return '#4CAF50'; // green
    case TuningState.SlightlyFlat:
    case TuningState.SlightlySharp: return '#FFC107'; // yellow
    case TuningState.VeryFlat:
    case TuningState.VerySharp: return '#FF5252'; // red
    default: return '#9E9E9E'; // gray
  }
}

export const INITIAL_TUNER_STATE: TunerViewState = {
  currentPitchResult: null,
  selectedTuning: STANDARD_TUNING,
  isListening: false,
  error: null,
  showInTuneConfirmation: false,
};
```

---

## State: SettingsViewState

Represents the UI state for the settings/tuning selection screen.

### Properties

| Property | Type | Description | Default Value |
|----------|------|-------------|---------------|
| `availableTunings` | List\<Tuning\> | All preset tunings | All presets |
| `selectedTuningId` | String | ID of currently selected tuning | "standard" |
| `themeMode` | ThemeMode | Current theme preference | System |
| `enableHapticFeedback` | Boolean | Haptic feedback toggle | true |

---

### Platform Implementations

#### Android (Kotlin)

```kotlin
data class SettingsViewState(
    val availableTunings: List<Tuning> = emptyList(),
    val selectedTuningId: String = "standard",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val enableHapticFeedback: Boolean = true
)
```

#### iOS (Swift)

```swift
struct SettingsViewState {
    var availableTunings: [Tuning]
    var selectedTuningId: String
    var themeMode: ThemeMode
    var enableHapticFeedback: Bool
    
    init(availableTunings: [Tuning] = [],
         selectedTuningId: String = "standard",
         themeMode: ThemeMode = .system,
         enableHapticFeedback: Bool = true) {
        self.availableTunings = availableTunings
        self.selectedTuningId = selectedTuningId
        self.themeMode = themeMode
        self.enableHapticFeedback = enableHapticFeedback
    }
}
```

#### WebApp (TypeScript)

```typescript
export interface SettingsViewState {
  availableTunings: Tuning[];
  selectedTuningId: string;
  themeMode: ThemeMode;
  enableHapticFeedback: boolean;
}

export const INITIAL_SETTINGS_STATE: SettingsViewState = {
  availableTunings: [],
  selectedTuningId: 'standard',
  themeMode: ThemeMode.System,
  enableHapticFeedback: true,
};
```

---

## Interface: ITunerViewModel

Manages state and business logic for the tuner screen.

### Methods

#### `startListening()`

Begins audio capture and pitch detection.

**Parameters**: None

**Returns**: `void` (async on all platforms)

**Side Effects**:
- Calls `IAudioProcessor.start()`
- Updates `isListening` to `true`
- Begins observing pitch results
- Updates `error` state if start fails

**State Transitions**:
- `isListening`: `false` → `true`
- `error`: `null` (on success) or set to error (on failure)

---

#### `stopListening()`

Stops audio capture.

**Parameters**: None

**Returns**: `void`

**Side Effects**:
- Calls `IAudioProcessor.stop()`
- Updates `isListening` to `false`
- Stops observing pitch results

**State Transitions**:
- `isListening`: `true` → `false`
- `currentPitchResult`: remains at last value (not cleared)

---

#### `selectTuning(tuningId: String)`

Changes the active tuning preset.

**Parameters**:
- `tuningId`: ID of tuning to select

**Returns**: `void` (async on all platforms)

**Side Effects**:
- Calls `ITuningRepository.setSelectedTuning(tuningId)`
- Updates `selectedTuning` state
- Persists selection to storage

**State Transitions**:
- `selectedTuning`: old tuning → new tuning
- Triggers UI update to show new target notes

---

#### `acknowledgeInTune()`

Dismisses the "in tune" confirmation animation.

**Parameters**: None

**Returns**: `void`

**Side Effects**:
- Sets `showInTuneConfirmation` to `false`

**State Transitions**:
- `showInTuneConfirmation`: `true` → `false`

---

### Observable State

**Android**: Exposes `StateFlow<TunerViewState>`

**iOS**: Conforms to `ObservableObject` with `@Published` properties

**WebApp**: Exposes state via React Context or Redux store

---

### Platform Implementations

#### Android (Kotlin)

```kotlin
class TunerViewModel(
    private val audioProcessor: IAudioProcessor,
    private val tuningRepository: ITuningRepository,
    private val settingsRepository: ISettingsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(TunerViewState())
    val state: StateFlow<TunerViewState> = _state.asStateFlow()
    
    private var pitchResultJob: Job? = null
    
    fun startListening() {
        viewModelScope.launch {
            audioProcessor.start().fold(
                onSuccess = {
                    _state.update { it.copy(isListening = true, error = null) }
                    observePitchResults()
                },
                onFailure = { error ->
                    _state.update { it.copy(error = error as? AudioError) }
                }
            )
        }
    }
    
    fun stopListening() {
        audioProcessor.stop()
        pitchResultJob?.cancel()
        _state.update { it.copy(isListening = false) }
    }
    
    fun selectTuning(tuningId: String) {
        viewModelScope.launch {
            tuningRepository.setSelectedTuning(tuningId)
            val newTuning = tuningRepository.getTuningById(tuningId)
            if (newTuning != null) {
                _state.update { it.copy(selectedTuning = newTuning) }
            }
        }
    }
    
    fun acknowledgeInTune() {
        _state.update { it.copy(showInTuneConfirmation = false) }
    }
    
    private fun observePitchResults() {
        pitchResultJob = viewModelScope.launch {
            audioProcessor.observePitchResults().collect { result ->
                _state.update { currentState ->
                    val shouldShowConfirmation = 
                        result.tuningState == TuningState.IN_TUNE &&
                        currentState.currentPitchResult?.tuningState != TuningState.IN_TUNE
                    
                    currentState.copy(
                        currentPitchResult = result,
                        showInTuneConfirmation = shouldShowConfirmation
                    )
                }
                
                // Trigger haptic feedback if enabled
                if (_state.value.showInTuneConfirmation) {
                    val settings = settingsRepository.getSettings()
                    if (settings.enableHapticFeedback) {
                        // Platform-specific haptic trigger
                    }
                }
            }
        }
    }
    
    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}
```

#### iOS (Swift)

```swift
@MainActor
class TunerViewModel: ObservableObject {
    @Published private(set) var state: TunerViewState
    
    private let audioProcessor: IAudioProcessor
    private let tuningRepository: ITuningRepository
    private let settingsRepository: ISettingsRepository
    private var cancellables = Set<AnyCancellable>()
    
    init(audioProcessor: IAudioProcessor,
         tuningRepository: ITuningRepository,
         settingsRepository: ISettingsRepository) {
        self.audioProcessor = audioProcessor
        self.tuningRepository = tuningRepository
        self.settingsRepository = settingsRepository
        self.state = TunerViewState()
        
        // Load initial tuning
        self.state.selectedTuning = tuningRepository.getSelectedTuning()
    }
    
    func startListening() async {
        let result = await audioProcessor.start()
        
        switch result {
        case .success:
            state.isListening = true
            state.error = nil
            observePitchResults()
        case .failure(let error):
            state.error = error
        }
    }
    
    func stopListening() {
        audioProcessor.stop()
        cancellables.removeAll()
        state.isListening = false
    }
    
    func selectTuning(tuningId: String) async {
        _ = await tuningRepository.setSelectedTuning(tuningId: tuningId)
        if let newTuning = tuningRepository.getTuningById(id: tuningId) {
            state.selectedTuning = newTuning
        }
    }
    
    func acknowledgeInTune() {
        state.showInTuneConfirmation = false
    }
    
    private func observePitchResults() {
        audioProcessor.observePitchResults()
            .sink { [weak self] result in
                guard let self = self else { return }
                
                let wasInTune = self.state.currentPitchResult?.tuningState == .inTune
                let isNowInTune = result.tuningState == .inTune
                
                self.state.currentPitchResult = result
                self.state.showInTuneConfirmation = !wasInTune && isNowInTune
                
                // Trigger haptic if needed
                if self.state.showInTuneConfirmation {
                    let settings = self.settingsRepository.getSettings()
                    if settings.enableHapticFeedback {
                        self.triggerHaptic()
                    }
                }
            }
            .store(in: &cancellables)
    }
    
    private func triggerHaptic() {
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.success)
    }
    
    deinit {
        stopListening()
    }
}
```

#### WebApp (TypeScript + React)

```typescript
export class TunerViewModel {
  private audioProcessor: IAudioProcessor;
  private tuningRepository: ITuningRepository;
  private settingsRepository: ISettingsRepository;
  private stateUpdater: (state: TunerViewState) => void;
  private unsubscribe?: () => void;
  
  constructor(
    audioProcessor: IAudioProcessor,
    tuningRepository: ITuningRepository,
    settingsRepository: ISettingsRepository,
    stateUpdater: (state: TunerViewState) => void
  ) {
    this.audioProcessor = audioProcessor;
    this.tuningRepository = tuningRepository;
    this.settingsRepository = settingsRepository;
    this.stateUpdater = stateUpdater;
  }
  
  async startListening(currentState: TunerViewState): Promise<TunerViewState> {
    try {
      await this.audioProcessor.start();
      
      this.unsubscribe = this.audioProcessor.observePitchResults((result) => {
        this.handlePitchResult(result, currentState);
      });
      
      return { ...currentState, isListening: true, error: null };
    } catch (error) {
      return { ...currentState, error: error as AudioError };
    }
  }
  
  stopListening(currentState: TunerViewState): TunerViewState {
    this.audioProcessor.stop();
    this.unsubscribe?.();
    return { ...currentState, isListening: false };
  }
  
  async selectTuning(currentState: TunerViewState, tuningId: string): Promise<TunerViewState> {
    await this.tuningRepository.setSelectedTuning(tuningId);
    const newTuning = this.tuningRepository.getTuningById(tuningId);
    
    if (newTuning) {
      return { ...currentState, selectedTuning: newTuning };
    }
    return currentState;
  }
  
  acknowledgeInTune(currentState: TunerViewState): TunerViewState {
    return { ...currentState, showInTuneConfirmation: false };
  }
  
  private handlePitchResult(result: PitchResult, currentState: TunerViewState): void {
    const wasInTune = getTuningState(currentState) === TuningState.InTune;
    const isNowInTune = getTuningStateFromResult(result) === TuningState.InTune;
    
    const newState: TunerViewState = {
      ...currentState,
      currentPitchResult: result,
      showInTuneConfirmation: !wasInTune && isNowInTune,
    };
    
    this.stateUpdater(newState);
    
    // Trigger haptic if needed
    if (newState.showInTuneConfirmation) {
      const settings = this.settingsRepository.getSettings();
      if (settings.enableHapticFeedback && 'vibrate' in navigator) {
        navigator.vibrate(50); // 50ms vibration
      }
    }
  }
  
  dispose(): void {
    this.stopListening(INITIAL_TUNER_STATE);
  }
}

// React Hook usage
export function useTunerViewModel(viewModel: TunerViewModel) {
  const [state, setState] = useState<TunerViewState>(INITIAL_TUNER_STATE);
  
  useEffect(() => {
    // Initialize view model with state updater
    viewModel = new TunerViewModel(
      audioProcessor,
      tuningRepository,
      settingsRepository,
      setState
    );
    
    return () => viewModel.dispose();
  }, []);
  
  return { state, viewModel };
}
```

---

## State Transition Diagrams

### Tuner Screen State Transitions

```
[App Launch]
     │
     ▼
[Initial State]
  isListening: false
  currentPitchResult: null
  error: null
     │
     ├─→ [User taps Start] ──► startListening()
     │                              │
     │                              ├─→ [Permission granted]
     │                              │       │
     │                              │       ▼
     │                              │  [Listening State]
     │                              │    isListening: true
     │                              │    Observing pitch results
     │                              │       │
     │                              │       ├─→ [Pitch detected]
     │                              │       │       │
     │                              │       │       ▼
     │                              │       │  [Active Tuning State]
     │                              │       │    currentPitchResult: {...}
     │                              │       │    tuningState: IN_TUNE | FLAT | SHARP
     │                              │       │       │
     │                              │       │       ├─→ [In Tune]
     │                              │       │       │       │
     │                              │       │       │       ▼
     │                              │       │       │  [Confirmation State]
     │                              │       │       │    showInTuneConfirmation: true
     │                              │       │       │    Trigger animation + haptic
     │                              │       │       │       │
     │                              │       │       │       └─→ [Auto-dismiss after 2s]
     │                              │       │       │               │
     │                              │       │       │               ▼
     │                              │       │       └───────► [Active Tuning State]
     │                              │       │
     │                              │       └─→ [User taps Stop] ──► stopListening()
     │                              │                                     │
     │                              │                                     ▼
     │                              ├─→ [Permission denied]          [Initial State]
     │                              │       │
     │                              │       ▼
     │                              │  [Error State]
     │                              │    error: MicrophonePermissionDenied
     │                              │       │
     │                              │       └─→ [Show permission prompt]
     │                              │
     │                              └─→ [Hardware failure] ──► [Error State]
     │
     └─→ [User opens Settings] ──► Navigate to SettingsView
             │
             └─→ [User selects tuning] ──► selectTuning(id)
                     │
                     └─→ Update selectedTuning in state
```

### Settings Screen State Transitions

```
[Settings Screen Opened]
     │
     ▼
[Load State]
  availableTunings: [...] from repository
  selectedTuningId: "standard" from settings
  themeMode: System
  enableHapticFeedback: true
     │
     ├─→ [User taps tuning] ──► selectTuning(id)
     │       │
     │       └─→ Update selectedTuningId
     │           Persist to storage
     │           Return to main screen
     │
     ├─→ [User toggles theme] ──► updateTheme(mode)
     │       │
     │       └─→ Update themeMode
     │           Persist to storage
     │           Apply theme immediately
     │
     └─→ [User toggles haptic] ──► updateHaptic(enabled)
             │
             └─→ Update enableHapticFeedback
                 Persist to storage
```

---

## UI Update Contract

### Update Frequency

- **Pitch Result Updates**: 20 Hz (50ms intervals) by default, configurable 10-60 Hz
- **Marker Position Animation**: Smooth interpolation over 100ms using platform animation APIs
- **In-Tune Confirmation**: Display for 2 seconds, then auto-dismiss

### Animation Specifications

#### Marker Position

**Android (Jetpack Compose)**:
```kotlin
val markerPosition by animateFloatAsState(
    targetValue = state.markerPosition,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
)
```

**iOS (SwiftUI)**:
```swift
.animation(.spring(response: 0.3, dampingFraction: 0.7), value: state.markerPosition)
```

**WebApp (Framer Motion)**:
```tsx
<motion.div
  animate={{ x: markerPosition * 100 + '%' }}
  transition={{ type: 'spring', damping: 20, stiffness: 300 }}
/>
```

#### In-Tune Confirmation

**Android**:
```kotlin
AnimatedVisibility(
    visible = state.showInTuneConfirmation,
    enter = scaleIn() + fadeIn(),
    exit = scaleOut() + fadeOut()
)
```

**iOS**:
```swift
.scaleEffect(state.showInTuneConfirmation ? 1.2 : 1.0)
.animation(.easeInOut(duration: 0.5).repeatCount(2), value: state.showInTuneConfirmation)
```

**WebApp**:
```tsx
<motion.div
  initial={{ scale: 1 }}
  animate={{ scale: state.showInTuneConfirmation ? 1.2 : 1.0 }}
  transition={{ duration: 0.5, repeat: 2 }}
/>
```

---

## Testing Contract

### Unit Test Requirements

Each ViewModel implementation must include tests for:

1. **Initial State**: Verify default state values
2. **Start Listening Success**: Mock audio processor success, verify `isListening = true`
3. **Start Listening Failure**: Mock audio processor failure, verify error state set
4. **Stop Listening**: Verify `isListening = false`, observer unsubscribed
5. **Pitch Result Updates**: Mock pitch results, verify state updates correctly
6. **In-Tune Detection**: Verify `showInTuneConfirmation` triggered only on transition to in-tune
7. **Tuning Selection**: Mock repository, verify state updates and persistence
8. **Marker Position Calculation**: Test edge cases (-100 cents, 0 cents, +100 cents)
9. **Marker Color Logic**: Verify color changes based on tuning state

### Mock Dependencies

```kotlin
// Android example
class MockAudioProcessor : IAudioProcessor {
    private val _pitchResults = MutableSharedFlow<PitchResult>()
    var startResult: Result<Unit> = Result.success(Unit)
    
    override suspend fun start() = startResult
    override fun stop() {}
    override fun isRunning() = true
    override fun observePitchResults() = _pitchResults.asSharedFlow()
    
    suspend fun emitPitchResult(result: PitchResult) {
        _pitchResults.emit(result)
    }
}
```

---

## Performance SLAs

| Operation | Max Latency | Notes |
|-----------|-------------|-------|
| State update (pitch result) | 5ms | From result emission to state object update |
| UI recomposition/re-render | 16ms | Must maintain 60fps (one frame) |
| Marker position animation | 100ms | Smooth spring animation duration |
| Tuning selection | 100ms | Including persistence to storage |

---

## Conclusion

This UI State Management contract ensures consistent state handling across all platforms while respecting platform-specific idioms (StateFlow for Android, ObservableObject for iOS, React Context for WebApp). All state transitions are well-defined, testable, and optimized for performance.

**Next Steps**: Implement ViewModels on each platform conforming to these state structures and transition rules, ensuring consistent user experience across Android, iOS, and WebApp.
