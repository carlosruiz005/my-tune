# Research Document: Guitar Tuner Application

**Feature**: Guitar Tuner (001-guitar-tuner)  
**Date**: 2025-12-08  
**Phase**: 0 - Research & Technical Decisions

## Executive Summary

This document captures all technical research, decisions, and rationales for implementing a cross-platform guitar tuner application. The app uses native FFT implementations to detect guitar string frequencies through device microphones, providing real-time visual feedback for tuning accuracy. Implementation follows a phased approach: Android MVP → iOS → WebApp.

---

## 1. Audio Processing Architecture

### 1.1 FFT (Fast Fourier Transform) Implementation

**Decision**: Implement FFT natively on each platform without external DSP libraries (except platform-provided frameworks).

**Platform-Specific Approaches**:

#### Android (Kotlin)
- **Chosen**: Custom Kotlin FFT implementation OR kissFFT C library via JNI
- **Rationale**: 
  - Kotlin can implement Cooley-Tukey FFT algorithm directly for educational transparency
  - kissFFT is lightweight (~2000 LOC), BSD-licensed, easily integrated via NDK
  - Avoids heavy dependencies like FFTW which is GPL-licensed (licensing concerns)
- **Implementation Details**:
  - Use `AudioRecord` class for microphone access (requires RECORD_AUDIO permission)
  - Sample rate: 44100 Hz or 48000 Hz (device-dependent)
  - FFT size: 8192 samples for ~5.4 Hz frequency resolution at 44.1kHz
  - Windowing: Hann window to reduce spectral leakage
  - Processing: Off-thread using Kotlin Coroutines (IO dispatcher)

**Alternatives Considered**:
- FFTW: Rejected due to GPL license incompatibility with mobile app stores
- TarsosDSP: Rejected as external library (requirement to implement natively)
- No FFT (autocorrelation): Rejected due to lower accuracy for guitar tuning

#### iOS (Swift)
- **Chosen**: Accelerate Framework's vDSP library (Apple-provided)
- **Rationale**:
  - Native iOS framework, hardware-optimized for Apple Silicon and Intel
  - Zero external dependencies, already included in iOS SDK
  - Excellent performance with SIMD optimizations
- **Implementation Details**:
  - Use `AVAudioEngine` with `AVAudioInputNode` for microphone access
  - vDSP provides `vDSP_fft_zrip` for real-to-complex FFT
  - Sample rate: 44100 Hz standard
  - FFT size: 8192 samples
  - Windowing: Hann window via `vDSP_hann_window`
  - Processing: Grand Central Dispatch (GCD) background queue

**Alternatives Considered**:
- Custom Swift FFT: Rejected due to unnecessary complexity when Accelerate is available
- Core Audio FFT: Rejected as Accelerate vDSP is more modern and optimized

#### WebApp (TypeScript + WebAssembly)
- **Chosen**: Custom FFT compiled to WebAssembly + AudioWorklet API
- **Rationale**:
  - AudioWorklet runs on audio rendering thread (128-sample chunks at 48kHz)
  - WebAssembly provides near-native performance for FFT computation
  - Keeps processing off main thread, preventing UI blocking
- **Implementation Details**:
  - Use Web Audio API `getUserMedia()` for microphone access
  - AudioWorklet processor written in JavaScript/TypeScript
  - FFT algorithm (Cooley-Tukey) compiled from C/Rust to WASM via Emscripten/wasm-pack
  - Sample rate: 48000 Hz (Web Audio default)
  - FFT size: 8192 samples
  - Windowing: Hann window precomputed in JavaScript
  - Processing: AudioWorklet thread (separate from main and worker threads)

**Alternatives Considered**:
- Pure JavaScript FFT: Rejected due to performance concerns for real-time processing
- Web Workers: Rejected as AudioWorklet is specifically designed for audio processing
- External libraries (e.g., dsp.js): Rejected per requirement to implement natively

---

### 1.2 Pitch Detection Algorithm

**Decision**: Harmonic Product Spectrum (HPS) combined with parabolic interpolation for sub-bin frequency resolution.

**Algorithm Steps**:
1. Capture audio buffer (8192 samples)
2. Apply Hann window to reduce spectral leakage
3. Compute FFT → frequency spectrum (magnitude)
4. Apply Harmonic Product Spectrum:
   - Downsample spectrum by factors of 2, 3, 4, 5
   - Multiply downsampled spectra together
   - Amplifies fundamental frequency, suppresses harmonics
5. Find peak bin in HPS result within guitar frequency range (82-330 Hz)
6. Apply parabolic interpolation on peak bin and neighbors for sub-bin accuracy
7. Convert bin index to Hz: `frequency = (bin_index * sample_rate) / fft_size`

**Rationale**:
- HPS is robust against harmonic overtones (common in guitar strings)
- Parabolic interpolation provides accuracy better than FFT bin resolution (~5.4 Hz/bin)
- Achieves ±3 cents accuracy requirement (1 cent = 1/100 of a semitone)

**Alternatives Considered**:
- Simple peak picking: Rejected due to confusion with harmonics (e.g., A string 110Hz vs 220Hz harmonic)
- Autocorrelation (YIN algorithm): Rejected as more complex and not required for monophonic guitar tuning
- Cepstral analysis: Rejected as overkill for single-note detection

---

### 1.3 Frequency to Note Conversion

**Decision**: Logarithmic frequency-to-MIDI note conversion with cent deviation calculation.

**Formula**:
```
MIDI_note = 12 * log2(frequency / 440) + 69
cents_deviation = 100 * (MIDI_note - round(MIDI_note))
note_name = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"][round(MIDI_note) % 12]
```

**Example**:
- Frequency: 110 Hz → MIDI note 45 → Note name "A2" (standard A string)
- Frequency: 112 Hz → 31 cents sharp from A2

**Rationale**:
- MIDI note system is industry standard for pitch representation
- Logarithmic scale matches human pitch perception (equal temperament tuning)
- Cent deviation maps directly to visual indicator position (±50 cents = ±1 semitone)

**Alternatives Considered**:
- Lookup table: Rejected due to lack of precision for cent deviations
- Linear frequency mapping: Rejected as doesn't match musical pitch perception

---

## 2. User Interface Architecture

### 2.1 Visual Tuning Indicator Design

**Decision**: Horizontal bar with animated marker, color-coded feedback, and numeric cent display.

**Component Specifications**:

#### Visual Elements
1. **Tuning Bar**:
   - Horizontal bar spanning 80% screen width
   - Height: 60-80 dp/pt
   - Background: Gradient from red (left) → yellow (center) → green (center) → yellow (right) → red (right)
   - Center marking: Vertical line or tick mark
   
2. **Marker**:
   - Vertical indicator (line or triangle shape)
   - Position: Animated based on cent deviation
   - Range: ±50 cents mapped to bar width (±100 cents = full bar width)
   - Color: 
     - Red when |cents| > 20
     - Yellow when 10 < |cents| ≤ 20
     - Green when |cents| ≤ 10 (with pulse animation)
   - Movement threshold: ±10 cents for visible change (per spec requirement FR-018)

3. **Note Display**:
   - Large text above bar showing detected note (e.g., "A", "E", "D#")
   - Octave number displayed smaller (e.g., "A₂")
   - Font size: 48-64 sp/pt for note, 24 sp/pt for octave

4. **Cent Display**:
   - Numeric cent deviation below bar (e.g., "+12¢" or "-5¢")
   - Font size: 18-24 sp/pt
   - Color matches marker color

5. **Target Note Indicator** (for selected tuning):
   - Shows expected note for each string position
   - Displayed as row of 6 string indicators below tuning bar
   - Highlights current string being tuned (when detected frequency matches string range)

**Rationale**:
- Horizontal bar is intuitive metaphor (string tension)
- Color coding provides instant feedback without reading numbers
- ±50 cent range balances precision with usability (most users tune within ±25 cents)
- Animation smoothness requires 60fps updates (16.67ms frame time)

**Alternatives Considered**:
- Circular dial (analog tuner style): Rejected as less mobile-friendly, harder to implement smoothly
- Waveform display: Rejected as visually complex and unnecessary for tuning task
- LED-style indicator (multiple discrete lights): Rejected as less precise than continuous bar

---

### 2.2 Platform-Specific UI Implementation

#### Android (Jetpack Compose)
**Framework**: Jetpack Compose with Material Design 3

**Key Components**:
```kotlin
@Composable
fun TunerScreen(viewModel: TunerViewModel)
  └─ TuningBar(pitchResult: PitchResult)
      └─ Canvas API for custom drawing
  └─ NoteDisplay(note: String, octave: Int)
  └─ CentDisplay(cents: Int)
  └─ StringIndicators(tuning: Tuning, currentNote: String)
  └─ SettingsButton() → navigates to TuningSelectionScreen
```

**State Management**:
- ViewModel holds `StateFlow<PitchResult>` updated from audio processor
- Compose recomposition triggered on state changes
- `derivedStateOf` for computed UI values (marker position, colors)

**Animation**:
- `animateFloatAsState` for smooth marker position transitions
- `animateColorAsState` for color changes
- Pulse animation: `infiniteRepeatable` with `tween` easing when in-tune

**Theme**:
- Material 3 `colorScheme` with `isSystemInDarkTheme()` detection
- Custom color tokens in `ui/theme/Color.kt`

---

#### iOS (SwiftUI)
**Framework**: SwiftUI with iOS 14+ APIs

**Key Components**:
```swift
struct TunerView: View
  └─ TuningBarView(pitchResult: PitchResult)
      └─ GeometryReader + Path for custom drawing
  └─ NoteDisplayView(note: String, octave: Int)
  └─ CentDisplayView(cents: Int)
  └─ StringIndicatorsView(tuning: Tuning, currentNote: String)
  └─ NavigationLink to SettingsView
```

**State Management**:
- ViewModel as `@ObservableObject` with `@Published` properties
- `@StateObject` ownership in view
- Combine publishers for audio stream

**Animation**:
- `.animation(.spring())` for marker position
- `.transition()` for color changes
- Pulse: `.scaleEffect()` with `withAnimation(.easeInOut.repeatForever())`

**Theme**:
- `@Environment(\.colorScheme)` for Light/Dark detection
- SwiftUI `Color.primary`, `Color.secondary` for system colors
- Custom color assets in Assets.xcassets with Light/Dark variants

---

#### WebApp (React + TypeScript)
**Framework**: React 18 with TypeScript

**Key Components**:
```tsx
function TunerPage()
  └─ <TunerDisplay pitchResult={pitchResult} />
      └─ <svg> or <canvas> for tuning bar rendering
  └─ <NoteIndicator note={note} octave={octave} />
  └─ <CentDisplay cents={cents} />
  └─ <StringIndicators tuning={tuning} currentNote={note} />
  └─ <Link to="/settings">Settings</Link>
```

**State Management**:
- React Context for global audio state
- `useState` + `useEffect` for audio stream updates
- Custom hook `useAudioProcessor()` encapsulating Web Audio logic

**Animation**:
- Framer Motion library for spring animations (`motion.div`)
- CSS transitions for color changes
- Pulse: CSS `@keyframes` animation on in-tune state

**Theme**:
- CSS variables for color tokens (`--color-primary-light`, `--color-primary-dark`)
- `prefers-color-scheme` media query for system theme detection
- React Context provider `ThemeContext` for manual theme toggle

---

### 2.3 Tuning Selection Interface

**Decision**: Modal/Sheet with scrollable list of preset tunings.

**UI Flow**:
1. User taps "Settings" or "Tuning" button on main screen
2. Modal sheet appears with list of tuning presets
3. Each list item shows tuning name and string notes (e.g., "Drop D - DADGBE")
4. User taps to select, modal dismisses, main screen updates to new tuning
5. Selection persisted to local storage (SharedPreferences/UserDefaults/localStorage)

**Preset Tunings** (Minimum 5):
1. **Standard**: E A D G B E (82.41, 110.00, 146.83, 196.00, 246.94, 329.63 Hz)
2. **Drop D**: D A D G B E (73.42, 110.00, 146.83, 196.00, 246.94, 329.63 Hz)
3. **Half Step Down**: Eb Ab Db Gb Bb Eb (77.78, 103.83, 138.59, 185.00, 233.08, 311.13 Hz)
4. **Full Step Down**: D G C F A D (73.42, 98.00, 130.81, 174.61, 220.00, 293.66 Hz)
5. **Drop C**: C G C F A D (65.41, 98.00, 130.81, 174.61, 220.00, 293.66 Hz)

**Additional Presets** (Future):
- Open D, Open G, DADGAD, Drop B, Drop A#, etc.

**Rationale**:
- Modal/sheet is standard mobile pattern for settings selection
- List view scales well for additional presets
- Displaying string notes helps users verify correct tuning selection

---

## 3. Data Persistence

### 3.1 Local Storage Strategy

**Decision**: Use platform-native key-value storage for tuning preset persistence only. No database required for MVP.

**Platform Implementations**:

#### Android
- **Technology**: SharedPreferences (Jetpack Preferences DataStore for future)
- **Storage**:
  - Key: `"selected_tuning_id"` → Value: `"standard"` | `"drop_d"` | etc.
  - Format: String preference
- **Access**: `PreferenceManager.getDefaultSharedPreferences(context)`

#### iOS
- **Technology**: UserDefaults
- **Storage**:
  - Key: `"selectedTuningID"` → Value: `"standard"` | `"dropD"` | etc.
  - Format: String
- **Access**: `UserDefaults.standard.string(forKey: "selectedTuningID")`

#### WebApp
- **Technology**: localStorage
- **Storage**:
  - Key: `"my-tune-selected-tuning"` → Value: `"standard"` | `"drop_d"` | etc.
  - Format: String (JSON for future expansion)
- **Access**: `localStorage.getItem("my-tune-selected-tuning")`

**Rationale**:
- Lightweight solution for single preference
- All platforms provide built-in, reliable key-value storage
- No network sync required (offline-first)
- Fast read/write performance

**Alternatives Considered**:
- SQLite/CoreData/IndexedDB: Rejected as overkill for storing one string value
- JSON file: Rejected as more complex than platform storage APIs
- In-memory only: Rejected as user selection would reset on app restart

---

### 3.2 Tuning Preset Data Structure

**Decision**: Hardcoded tuning presets in shared JSON format, compiled into each platform's codebase.

**Shared JSON Schema** (`shared/tuning-presets/presets.json`):
```json
{
  "tunings": [
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
    },
    {
      "id": "drop_d",
      "name": "Drop D",
      "displayName": {
        "en": "Drop D",
        "es": "Drop D"
      },
      "strings": [
        {"position": 6, "note": "D", "octave": 2, "frequency": 73.42},
        {"position": 5, "note": "A", "octave": 2, "frequency": 110.00},
        {"position": 4, "note": "D", "octave": 3, "frequency": 146.83},
        {"position": 3, "note": "G", "octave": 3, "frequency": 196.00},
        {"position": 2, "note": "B", "octave": 3, "frequency": 246.94},
        {"position": 1, "note": "E", "octave": 4, "frequency": 329.63}
      ]
    }
    // ... additional tunings
  ]
}
```

**Platform Code Generation**:
- Android: Kotlin data classes generated from JSON (sealed class or enum)
- iOS: Swift structs generated from JSON (code generation or manual sync)
- WebApp: TypeScript types imported directly from JSON

**Rationale**:
- Single source of truth prevents platform divergence
- Easy to add new tunings by editing one JSON file
- Localization support via `displayName` dictionary
- Type-safe after code generation

**Alternatives Considered**:
- API endpoint for tunings: Rejected due to offline-first requirement
- Platform-specific hardcoding: Rejected due to duplication and sync issues
- SQLite with seeded data: Rejected as adds complexity for static data

---

## 4. Cross-Platform Consistency

### 4.1 Design Tokens

**Decision**: Shared design token JSON files code-generated to platform-specific formats.

**Design Token Categories**:

#### Colors (`shared/design-tokens/colors.json`)
```json
{
  "tuning-bar-flat": {"light": "#FF5252", "dark": "#FF6B6B"},
  "tuning-bar-close": {"light": "#FFC107", "dark": "#FFD54F"},
  "tuning-bar-intune": {"light": "#4CAF50", "dark": "#66BB6A"},
  "background": {"light": "#FFFFFF", "dark": "#121212"},
  "surface": {"light": "#F5F5F5", "dark": "#1E1E1E"},
  "text-primary": {"light": "#000000", "dark": "#FFFFFF"},
  "text-secondary": {"light": "#666666", "dark": "#AAAAAA"}
}
```

#### Typography (`shared/design-tokens/typography.json`)
```json
{
  "note-display": {"size": 56, "weight": "bold", "family": "system"},
  "cent-display": {"size": 20, "weight": "medium", "family": "system"},
  "string-label": {"size": 14, "weight": "regular", "family": "system"}
}
```

#### Spacing (`shared/design-tokens/spacing.json`)
```json
{
  "xs": 4,
  "sm": 8,
  "md": 16,
  "lg": 24,
  "xl": 32,
  "tuning-bar-height": 64,
  "touch-target-min": 44
}
```

**Code Generation**:
- Script: `tools/scripts/generate-design-tokens.js`
- Output:
  - Android: `android/app/src/main/res/values/colors.xml`, `dimens.xml`
  - iOS: `ios/MyTune/Resources/DesignTokens.swift`
  - WebApp: `webapp/src/ui/theme/tokens.ts`

**Rationale**:
- Ensures pixel-perfect consistency across platforms
- Single-source-of-truth prevents design drift
- Easy to update design system (change JSON, regenerate)
- Type-safe access to design tokens

**Alternatives Considered**:
- Manual syncing: Rejected due to error-prone, high maintenance
- Figma Tokens plugin: Rejected as adds external dependency and complexity
- CSS variables only: Rejected as doesn't solve Android/iOS integration

---

### 4.2 Accessibility Standards

**Decision**: Meet WCAG 2.1 Level AA across all platforms with platform-specific APIs.

**Accessibility Requirements**:

#### Screen Reader Support
- **Android**: TalkBack
  - Content descriptions on all interactive elements (`contentDescription`)
  - Semantic roles (`role = Role.Button`)
  - State descriptions (e.g., "A string, 5 cents sharp")
- **iOS**: VoiceOver
  - Accessibility labels (`.accessibilityLabel`)
  - Accessibility hints (`.accessibilityHint`)
  - Accessibility values for dynamic content (`.accessibilityValue`)
- **WebApp**: NVDA/JAWS/VoiceOver
  - ARIA labels (`aria-label`, `aria-labelledby`)
  - ARIA live regions for pitch updates (`aria-live="polite"`)
  - Semantic HTML (`<button>`, `<nav>`, etc.)

#### Touch Target Sizes
- Minimum: 44x44 dp/pt (per Apple HIG and Material Design)
- Tuner bar marker: 48x48 dp/pt for easy tapping
- Settings button: 48x48 dp/pt minimum

#### Color Contrast
- Text on background: 4.5:1 minimum (WCAG AA)
- Large text (≥18pt): 3:1 minimum
- Tuning indicator colors validated with contrast checker
- Fallback to patterns/textures if color-blind mode requested (future)

#### Keyboard Navigation (WebApp)
- Tab order: Settings button → Tuning preset list items
- Enter/Space to activate buttons
- Escape to close modal
- Focus visible styles (outline)

**Testing**:
- Android: Accessibility Scanner app
- iOS: Accessibility Inspector in Xcode
- WebApp: axe DevTools, Lighthouse audit, manual screen reader testing

**Rationale**:
- Legal compliance (ADA, Section 508)
- Inclusive design benefits all users
- Improved usability in low-light conditions (contrast)
- Future-proofs for accessibility lawsuits

---

## 5. Performance Optimization

### 5.1 Audio Processing Performance

**Decision**: Dedicated background thread/queue for audio processing with optimized buffer management.

**Performance Targets**:
- Audio buffer processing: < 50ms per buffer (8192 samples @ 44.1kHz = 185ms of audio)
- End-to-end latency (sound → screen update): < 100ms total
- CPU usage: < 15% sustained on mid-range devices
- Battery impact: < 5% per hour of active tuning

**Optimization Strategies**:

#### Buffer Management
- Ring buffer for audio samples (avoid memory allocations in hot path)
- Buffer pool for FFT input/output arrays (reuse allocations)
- Double buffering: UI reads from stable buffer while audio thread writes to alternate

#### FFT Optimization
- Pre-compute Hann window coefficients (one-time initialization)
- Use in-place FFT where supported (vDSP on iOS)
- Cache bit-reversal lookup tables (FFT algorithm requirement)

#### Thread Architecture
- **Android**: 
  - AudioRecord on background thread (IO dispatcher)
  - FFT processing on separate thread (Default dispatcher)
  - UI updates via MainScope
- **iOS**:
  - AVAudioEngine runs on separate thread automatically
  - FFT processing on GCD background queue (QoS: .userInitiated)
  - UI updates on main queue via DispatchQueue.main
- **WebApp**:
  - AudioWorklet processor (audio rendering thread)
  - FFT in WebAssembly (near-native speed)
  - UI updates via MessagePort to main thread

#### Throttling
- Update UI at maximum 20 Hz (every 50ms) to avoid excessive recomposition/re-renders
- Debounce "in-tune" state transitions to prevent flicker (100ms stable threshold)

**Rationale**:
- Real-time audio processing is CPU-intensive
- Poor optimization causes UI lag and battery drain
- Background threads prevent ANR (Android Not Responding) / UI freezes

---

### 5.2 App Launch Performance

**Decision**: Lazy initialization and progressive loading for fast cold start.

**Optimization Strategies**:

#### Android
- Application class: Minimal work in `onCreate()`, defer Hilt initialization
- Splash screen: Use Android 12+ SplashScreen API (shows instantly, transitions when ready)
- Baseline profiles: Generate with `androidx.profileinstaller` for ART optimization
- Startup tracing: Identify slow initialization with `androidx.startup`

**Target**: Cold start < 2 seconds on Snapdragon 6-series devices

#### iOS
- App delegate: Minimal work in `didFinishLaunchingWithOptions`
- Pre-warming: Initialize audio engine on first user interaction, not at launch
- Launch screen: Static storyboard/XIB (fast display)
- Binary size optimization: Bitcode enabled, App Thinning

**Target**: Cold start < 1.5 seconds on iPhone X / iPhone 12 equivalent

#### WebApp
- Code splitting: Separate bundle for settings page (lazy load)
- Critical CSS: Inline above-the-fold styles
- Resource hints: `<link rel="preload">` for Wasm FFT module
- Service worker: Cache shell architecture for instant repeat loads

**Target**: First Contentful Paint < 1.5 seconds on 3G (Fast 3G: 1.6 Mbps down, 750 Kbps up, 150ms RTT)

**Rationale**:
- First impression is critical for user retention
- Users expect instant responsiveness on modern devices
- App store ratings penalize slow apps

---

### 5.3 Memory Management

**Decision**: Proactive memory management with buffer reuse and lifecycle-aware cleanup.

**Memory Budgets**:
- Android: < 150MB total (target mid-range devices with 3-4GB RAM)
- iOS: < 100MB total (avoid memory warnings on devices with 2GB RAM)
- WebApp: < 50MB JavaScript heap (browser-dependent limits)

**Strategies**:

#### Buffer Reuse
- Allocate audio buffers once at initialization
- Reuse same FloatArray/Float32Array for FFT input
- Pool pattern for temporary computation arrays

#### Lifecycle Awareness
- **Android**: 
  - Stop audio recording in `onPause()`, resume in `onResume()`
  - Release audio resources in `onDestroy()`
  - ViewModel clears StateFlow subscribers
- **iOS**:
  - Stop audio engine in `sceneDidEnterBackground`
  - Pause engine in `sceneWillResignActive`
  - Remove observers in `deinit`
- **WebApp**:
  - Close MediaStream on page unload
  - Terminate AudioWorklet processor
  - Clear Web Audio nodes

#### Leak Prevention
- Weak references for callbacks (avoid retain cycles)
- Explicit cleanup of Combine/Coroutine subscriptions
- Memory leak detection in tests (LeakCanary for Android)

**Rationale**:
- Mobile devices have limited RAM compared to desktops
- Memory leaks cause gradual app slowdown and crashes
- Battery efficiency improved by releasing resources when inactive

---

## 6. Testing Strategy

### 6.1 Test Pyramid

**Decision**: 70% unit tests, 20% integration tests, 10% UI/E2E tests.

**Rationale**:
- Unit tests are fast, reliable, and catch most bugs
- Integration tests validate component interactions
- UI tests are slow and flaky, reserved for critical flows

---

### 6.2 Platform-Specific Test Frameworks

#### Android
**Unit Tests** (JUnit 5 + Mockito/MockK):
- Test cases:
  - FFT output correctness (known sine wave input → expected frequency)
  - Frequency-to-note conversion (110 Hz → A2, 440 Hz → A4)
  - Cent deviation calculation (112 Hz from 110 Hz target → +31 cents)
  - Tuning preset selection persistence
  - HPS algorithm accuracy

**Instrumented Tests** (Espresso + AndroidX Test):
- Test cases:
  - Microphone permission flow (deny → show rationale, grant → start audio)
  - Theme switching (light → dark, verify colors)
  - Tuning selection (tap Drop D → verify string notes update)
  - UI state updates (simulate pitch input → verify marker position)

**Robolectric** (Unit tests with Android framework):
- Test ViewModel state transitions
- Test SharedPreferences read/write

#### iOS
**XCTest** (Unit Tests):
- Test cases:
  - FFT wrapper around Accelerate framework (input → output validation)
  - Pitch detection algorithm (recorded guitar sample → expected note)
  - Frequency conversion formulas
  - Tuning model logic

**XCUITest** (UI Tests):
- Test cases:
  - Microphone permission alert handling
  - Navigation to settings and back
  - Theme changes (system dark mode toggle)
  - Tuning preset selection

**Quick/Nimble** (BDD-style unit tests):
- Readable test syntax: `describe("PitchDetector") { it("detects A2 from 110 Hz") { expect(note).to(equal("A")) } }`

#### WebApp
**Jest** (Unit Tests):
- Test cases:
  - FFT algorithm correctness (JavaScript/WASM output validation)
  - React component rendering (snapshot tests)
  - Pitch conversion utilities
  - Tuning service logic

**React Testing Library** (Integration Tests):
- Test cases:
  - Component interactions (click settings → modal appears)
  - Theme context switching
  - State updates propagate to UI

**Playwright/Cypress** (E2E Tests):
- Test cases:
  - Full user flow: open app → grant mic permission → select tuning → see visual indicator update
  - Cross-browser compatibility (Chrome, Firefox, Safari)
  - Accessibility audit (axe-core integration)

---

### 6.3 Test Data

**Decision**: Pre-recorded guitar audio samples and synthetic sine wave generators.

**Test Audio Assets**:
1. Synthetic pure tones:
   - 110 Hz sine wave (A2 string, perfect tune)
   - 112 Hz sine wave (A2 string, +31 cents sharp)
   - 108 Hz sine wave (A2 string, -31 cents flat)
2. Real guitar recordings:
   - Each of 6 strings in standard tuning (E A D G B E)
   - Each string slightly sharp (+15 cents)
   - Each string slightly flat (-15 cents)
3. Edge cases:
   - Ambient noise only (no guitar sound) → expect no detection
   - Multiple strings played simultaneously → test rejection
   - Very quiet input (near microphone noise floor) → test silence detection

**Storage**:
- Android: `src/androidTest/assets/`
- iOS: `MyTuneTests/TestAudio/`
- WebApp: `tests/fixtures/audio/`

**Rationale**:
- Deterministic audio inputs ensure reproducible tests
- Real recordings validate algorithm on actual guitar timbre
- Edge cases prevent false positives/negatives

---

## 7. Development Workflow & CI/CD

### 7.1 Monorepo Tooling

**Decision**: Independent build pipelines per platform with shared linting/formatting rules.

**Repository Structure**:
```
.github/
  workflows/
    android-ci.yml       # Gradle build, ktlint, unit + instrumented tests
    ios-ci.yml           # xcodebuild, SwiftLint, XCTest
    webapp-ci.yml        # npm build, ESLint, Jest, Playwright
    design-tokens.yml    # Regenerate tokens on shared/ changes
```

**Branch Strategy**:
- Main branch: `main` (always deployable)
- Feature branches: `001-guitar-tuner` (current), `002-metronome`, etc.
- Platform-specific work: `001-guitar-tuner/android`, `001-guitar-tuner/ios`, `001-guitar-tuner/webapp`

**Code Review**:
- PRs require 1 approval from platform expert (Android/iOS/Web)
- Cross-platform PRs (shared/) require all platform experts
- Constitution checklist in PR template

---

### 7.2 CI/CD Pipeline

#### Android
**Tools**: GitHub Actions + Gradle
**Steps**:
1. Checkout code
2. Set up JDK 17
3. Gradle cache restoration
4. Run ktlint (`./gradlew ktlintCheck`)
5. Run unit tests (`./gradlew test`)
6. Run instrumented tests on Firebase Test Lab or emulator (`./gradlew connectedAndroidTest`)
7. Build APK (`./gradlew assembleRelease`)
8. Upload APK artifact

**Deployment**: Google Play Internal Testing track (manual promotion to production)

#### iOS
**Tools**: GitHub Actions + Xcode Cloud (or Fastlane)
**Steps**:
1. Checkout code
2. Set up Xcode (version matrix: 14.3, 15.0)
3. Carthage/CocoaPods dependency resolution (if any)
4. Run SwiftLint (`swiftlint lint --strict`)
5. Build for testing (`xcodebuild build-for-testing`)
6. Run tests (`xcodebuild test-without-building`)
7. Build IPA (`xcodebuild archive`)
8. Upload to TestFlight

**Deployment**: TestFlight beta → App Store (manual release)

#### WebApp
**Tools**: GitHub Actions + Vite
**Steps**:
1. Checkout code
2. Set up Node.js (LTS version)
3. npm cache restoration
4. Install dependencies (`npm ci`)
5. Run ESLint (`npm run lint`)
6. Run Jest tests (`npm run test`)
7. Build production bundle (`npm run build`)
8. Run Playwright E2E tests (`npm run test:e2e`)
9. Run Lighthouse CI (performance audit)
10. Deploy to Vercel/Netlify/Firebase Hosting

**Deployment**: Automatic deployment to staging on merge to main, manual promotion to production

---

## 8. Risk Mitigation

### 8.1 Technical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| FFT implementation bugs cause inaccurate pitch detection | High | Medium | Extensive unit tests with known frequency inputs, pre-recorded guitar audio validation |
| Microphone permission denied by user | High | Medium | Clear explanation UI, fallback to manual note selection mode (future feature) |
| High ambient noise prevents pitch detection | Medium | High | Noise gate threshold, display "Too noisy" message, HPS algorithm is noise-resistant |
| Audio API differences across Android devices (sample rate, buffer size) | Medium | High | Dynamic sample rate detection, adaptive buffer sizing |
| iOS background audio restrictions prevent tuning while app inactive | Medium | Low | Document that app must be foreground, explore background audio modes (future) |
| WebAssembly not supported on older browsers | Medium | Low | Fallback to pure JavaScript FFT (slower but functional), display browser update prompt |
| Performance issues on low-end devices | High | Medium | Profiling on target devices (Snapdragon 6-series, iPhone X), reduce FFT size if needed (4096 samples) |
| Color-blind users cannot distinguish indicator colors | Low | Medium | Add text labels ("FLAT", "IN TUNE", "SHARP") alongside colors |

---

### 8.2 Project Execution Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Android MVP takes longer than estimated, delays iOS/WebApp | High | Medium | Prioritize core tuning feature, defer advanced features (metronome, custom tunings), add buffer time to estimates |
| Platform divergence (features available on one platform but not others) | Medium | Medium | Strict feature parity enforcement in Constitution Check, shared feature checklist |
| Cross-platform design inconsistencies | Low | Medium | Design token code generation, regular design reviews across platforms |
| Lack of iOS/Android device access for testing | Medium | Low | Use emulators/simulators for development, cloud device farms (Firebase Test Lab, AWS Device Farm) for final validation |
| App store rejection (privacy policy, metadata issues) | High | Low | Review app store guidelines early, prepare privacy policy, pre-submission checklist |

---

## 9. Future Enhancements (Out of Scope for MVP)

1. **Additional Instruments**: Bass guitar (4-string, 5-string), ukulele, violin, etc.
2. **Custom Tunings**: User-defined tuning creation with custom string notes
3. **Chromatic Tuner Mode**: Tune any note, not just preset guitar tunings
4. **Tuning History**: Track tuning sessions, frequency over time
5. **Reference Tone Generator**: Play target note through speaker to tune by ear
6. **Strobe Tuner Mode**: Ultra-precise visual tuning with strobe effect
7. **Metronome Integration**: Built-in metronome for practice sessions
8. **Cloud Sync**: Sync tuning preferences across devices (requires backend)
9. **Advanced Audio Processing**: Polyphonic detection (multiple strings at once)
10. **Machine Learning**: Timbre-aware pitch detection trained on guitar audio dataset

---

## 10. References & Resources

### Academic Papers
- "A Smarter Way to Find Pitch" - Miller Puckette (autocorrelation vs FFT comparison)
- "YIN, a fundamental frequency estimator for speech and music" - Alain de Cheveigné & Hideki Kawahara

### Technical Documentation
- Android AudioRecord API: https://developer.android.com/reference/android/media/AudioRecord
- iOS Accelerate Framework vDSP: https://developer.apple.com/documentation/accelerate/vdsp
- Web Audio API Specification: https://www.w3.org/TR/webaudio/
- WebAssembly FFT implementations: Emscripten, wasm-pack

### Best Practices
- Material Design 3 Guidelines: https://m3.material.io/
- Apple Human Interface Guidelines: https://developer.apple.com/design/human-interface-guidelines/
- WCAG 2.1 Accessibility: https://www.w3.org/WAI/WCAG21/quickref/

### Similar Projects (for reference, not dependencies)
- gStrings (Android tuner app)
- Cleartune (iOS tuner app)
- Online Guitar Tuner (web-based)

---

## Conclusion

This research document establishes the technical foundation for implementing the Guitar Tuner application across Android, iOS, and WebApp platforms. All major technical decisions have been documented with rationale, alternatives considered, and platform-specific implementation details. The next phase (Phase 1) will generate concrete data models, API contracts, and a quickstart guide based on this research.

**Key Takeaways**:
1. Native FFT implementations per platform (Kotlin/kissFFT, Accelerate vDSP, WebAssembly)
2. HPS algorithm for robust pitch detection
3. Monorepo structure with shared design tokens and tuning presets
4. MVVM architecture for Android, MVVM/Composable for iOS, Clean Architecture for WebApp
5. Offline-first operation with local storage persistence
6. 70/20/10 test pyramid with platform-specific frameworks
7. Phased rollout: Android MVP → iOS → WebApp

**Next Steps**: Proceed to Phase 1 to generate `data-model.md`, `/contracts/`, and `quickstart.md`.
