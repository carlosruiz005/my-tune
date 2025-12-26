# QuickStart Guide: Guitar Tuner Development

**Feature**: Guitar Tuner (001-guitar-tuner)  
**Version**: 1.0.0  
**Last Updated**: 2025-12-08

## Overview

This guide provides step-by-step instructions for setting up the development environment and implementing the Guitar Tuner application across all three platforms (Android, iOS, WebApp). Follow the platform-specific sections in order: **Android MVP → iOS → WebApp**.

---

## Prerequisites

### All Platforms

- **Git**: Version 2.30 or later
- **Repository Cloned**: `git clone <repo-url> && cd my-tune`
- **Branch Checkout**: `git checkout 001-guitar-tuner`

### Platform-Specific Tools

#### Android
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17 (included with Android Studio)
- **Android SDK**: API 26 (Android 8.0) minimum, API 34 target
- **Gradle**: 8.2+ (via wrapper)
- **Physical Device or Emulator**: With microphone capability

#### iOS
- **macOS**: Ventura (13.0) or later
- **Xcode**: 15.0 or later
- **Swift**: 5.9+ (included with Xcode)
- **CocoaPods** (optional): `sudo gem install cocoapods`
- **Physical Device**: Required for microphone testing (simulator lacks real mic input)

#### WebApp
- **Node.js**: v18 LTS or v20 LTS
- **npm**: 9.0+ (included with Node.js)
- **Browser**: Chrome 115+, Firefox 115+, Safari 16+, or Edge 115+
- **Code Editor**: VS Code recommended with TypeScript/React extensions

---

## Phase 1: Android MVP Implementation

### Step 1: Project Setup

1. **Create Android Project**:
   ```bash
   cd my-tune
   mkdir -p android
   cd android
   ```

2. **Initialize Gradle Project** (Android Studio):
   - Open Android Studio
   - New Project → Empty Activity (Compose)
   - Project name: `MyTune`
   - Package name: `com.mytune`
   - Minimum SDK: API 26
   - Language: Kotlin
   - Save location: `my-tune/android/`

3. **Configure `build.gradle.kts` (Module-level)**:
   ```kotlin
   plugins {
       id("com.android.application")
       id("org.jetbrains.kotlin.android")
       id("com.google.dagger.hilt.android") // Hilt DI
       id("org.jetbrains.kotlin.plugin.serialization") // JSON serialization
   }

   android {
       namespace = "com.mytune"
       compileSdk = 34

       defaultConfig {
           applicationId = "com.mytune"
           minSdk = 26
           targetSdk = 34
           versionCode = 1
           versionName = "1.0.0"
       }

       buildFeatures {
           compose = true
       }

       composeOptions {
           kotlinCompilerExtensionVersion = "1.5.3"
       }

       kotlinOptions {
           jvmTarget = "17"
       }
   }

   dependencies {
       // Jetpack Compose
       implementation("androidx.compose.ui:ui:1.5.4")
       implementation("androidx.compose.material3:material3:1.1.2")
       implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
       implementation("androidx.activity:activity-compose:1.8.1")

       // ViewModel & Lifecycle
       implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
       implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

       // Kotlin Coroutines
       implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

       // Hilt Dependency Injection
       implementation("com.google.dagger:hilt-android:2.48")
       kapt("com.google.dagger:hilt-compiler:2.48")

       // Navigation Compose
       implementation("androidx.navigation:navigation-compose:2.7.5")

       // Kotlinx Serialization (for tuning presets JSON)
       implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

       // Testing
       testImplementation("junit:junit:4.13.2")
       testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
       androidTestImplementation("androidx.test.ext:junit:1.1.5")
       androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
       androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
   }
   ```

4. **Add Permissions** (`AndroidManifest.xml`):
   ```xml
   <uses-permission android:name="android.permission.RECORD_AUDIO" />
   <uses-permission android:name="android.permission.VIBRATE" />
   ```

### Step 2: Implement Data Models

1. **Create Package Structure**:
   ```
   com.mytune/
   ├── data/
   │   ├── model/          # Tuning, GuitarString, PitchResult
   │   ├── audio/          # AudioRecorder, FFTProcessor
   │   └── repository/     # TuningRepository, SettingsRepository
   ├── domain/
   │   └── usecase/        # DetectPitchUseCase
   ├── ui/
   │   ├── tuner/          # TunerScreen composable
   │   ├── settings/       # SettingsScreen composable
   │   └── theme/          # Material 3 theme
   └── viewmodel/          # TunerViewModel
   ```

2. **Implement Data Classes** (`data/model/`):
   - Copy entity definitions from `data-model.md` (Tuning, GuitarString, PitchResult, TunerSettings)
   - Add validation logic in `init` blocks

3. **Load Tuning Presets**:
   - Copy `shared/tuning-presets/presets.json` to `app/src/main/assets/`
   - Create `TuningPresetsLoader.kt` to parse JSON using `kotlinx-serialization`

### Step 3: Implement Audio Processing

1. **Create `AndroidAudioProcessor.kt`** (implements `IAudioProcessor`):
   ```kotlin
   class AndroidAudioProcessor(
       private val pitchDetector: IPitchDetector,
       private val context: Context
   ) : IAudioProcessor {
       private val audioRecord: AudioRecord
       override val sampleRate = 44100
       override val bufferSize = 8192
       
       private val _pitchResults = MutableSharedFlow<PitchResult>()
       private var isRecording = false

       // Implementation details: AudioRecord setup, background thread, FFT processing
   }
   ```

2. **Create `FFTProcessor.kt`**:
   - Implement Cooley-Tukey FFT algorithm OR integrate kissFFT via JNI
   - Apply Hann window function
   - Return magnitude spectrum (FloatArray)

3. **Create `HPSPitchDetector.kt`** (implements `IPitchDetector`):
   - Implement Harmonic Product Spectrum algorithm
   - Parabolic interpolation for sub-bin accuracy
   - Return `PitchResult` with frequency and confidence

4. **Create `FrequencyConverter.kt`** (implements `IFrequencyConverter`):
   - Frequency → Note conversion (MIDI formula)
   - Cents deviation calculation

### Step 4: Implement Repository Layer

1. **Create `TuningRepositoryImpl.kt`**:
   - Load tuning presets from assets
   - Expose selected tuning as `StateFlow`
   - Persist selection to SharedPreferences

2. **Create `SettingsRepositoryImpl.kt`**:
   - Read/write TunerSettings to SharedPreferences
   - Use JSON serialization for complex object storage

### Step 5: Implement ViewModel

1. **Create `TunerViewModel.kt`**:
   - Inject `IAudioProcessor`, `ITuningRepository`, `ISettingsRepository` via Hilt
   - Manage `TunerViewState` as `StateFlow`
   - Implement methods: `startListening()`, `stopListening()`, `selectTuning()`

2. **Implement State Updates**:
   - Observe pitch results from `IAudioProcessor`
   - Update `currentPitchResult` at 20 Hz (throttled)
   - Detect in-tune transitions, trigger haptic feedback

### Step 6: Implement UI Layer

1. **Create `TunerScreen.kt` Composable**:
   ```kotlin
   @Composable
   fun TunerScreen(viewModel: TunerViewModel = hiltViewModel()) {
       val state by viewModel.state.collectAsState()
       
       Column(modifier = Modifier.fillMaxSize()) {
           NoteDisplay(note = state.displayNote, octave = state.displayOctave)
           TuningBar(state = state)
           CentDisplay(cents = state.displayCents)
           StringIndicators(tuning = state.selectedTuning, currentNote = state.displayNote)
           
           Button(onClick = { viewModel.startListening() }) {
               Text("Start")
           }
       }
   }
   ```

2. **Create Custom Composables**:
   - `TuningBar`: Canvas-based drawing of horizontal bar + marker
   - `NoteDisplay`: Large text showing current note
   - `StringIndicators`: Row of 6 target notes

3. **Implement Material 3 Theme**:
   - Define light and dark color schemes
   - Use `dynamicColorScheme()` for Material You on Android 12+

### Step 7: Testing

1. **Unit Tests** (`src/test/`):
   - Test `FFTProcessor` with synthetic sine waves
   - Test `HPSPitchDetector` with known frequency inputs
   - Test `FrequencyConverter` formulas (110 Hz → A2)
   - Test `TunerViewModel` state transitions with mocks

2. **Instrumented Tests** (`src/androidTest/`):
   - Test microphone permission flow with Espresso
   - Test UI interactions (start button, tuning selection)
   - Test theme switching

3. **Run Tests**:
   ```bash
   ./gradlew test                      # Unit tests
   ./gradlew connectedAndroidTest      # Instrumented tests (requires device)
   ```

### Step 8: Run & Debug

1. **Connect Device or Start Emulator** (with microphone access)
2. **Run App** from Android Studio (Shift+F10)
3. **Grant Microphone Permission** when prompted
4. **Test with Guitar** or use tone generator app

---

## Phase 2: iOS Implementation

### Step 1: Project Setup

1. **Create Xcode Project**:
   ```bash
   cd my-tune
   mkdir -p ios
   cd ios
   ```

2. **Open Xcode**:
   - New Project → iOS → App
   - Product Name: `MyTune`
   - Team: (Your Apple Developer account)
   - Organization Identifier: `com.mytune`
   - Interface: SwiftUI
   - Language: Swift
   - Save location: `my-tune/ios/`

3. **Configure `Info.plist`**:
   ```xml
   <key>NSMicrophoneUsageDescription</key>
   <string>MyTune needs microphone access to detect guitar string pitch for tuning.</string>
   ```

4. **Set Deployment Target**: iOS 14.0 (Project Settings → Deployment Target)

5. **Add SwiftLint** (optional but recommended):
   ```bash
   brew install swiftlint
   ```
   Add Run Script Phase in Build Phases:
   ```bash
   if which swiftlint >/dev/null; then
       swiftlint
   else
       echo "warning: SwiftLint not installed"
   fi
   ```

### Step 2: Implement Data Models

1. **Create File Structure**:
   ```
   MyTune/
   ├── Models/
   │   ├── Tuning.swift
   │   ├── GuitarString.swift
   │   ├── PitchResult.swift
   │   └── TunerSettings.swift
   ├── Services/
   │   ├── AudioEngine.swift
   │   ├── FFTProcessor.swift
   │   ├── PitchDetector.swift
   │   └── FrequencyConverter.swift
   ├── Repositories/
   │   ├── TuningRepository.swift
   │   └── SettingsRepository.swift
   ├── ViewModels/
   │   └── TunerViewModel.swift
   ├── Views/
   │   ├── TunerView.swift
   │   ├── SettingsView.swift
   │   └── Components/
   │       ├── TuningBarView.swift
   │       ├── NoteDisplayView.swift
   │       └── StringIndicatorsView.swift
   └── Resources/
       └── Assets.xcassets
   ```

2. **Implement Data Structs**:
   - Copy entity definitions from `data-model.md` (Swift versions)
   - Add `Codable` conformance for JSON serialization

3. **Load Tuning Presets**:
   - Copy `shared/tuning-presets/presets.json` to `Resources/`
   - Create `TuningPresetsLoader` to parse JSON using `JSONDecoder`

### Step 3: Implement Audio Processing

1. **Create `IOSAudioProcessor.swift`** (implements `IAudioProcessor` protocol):
   ```swift
   class IOSAudioProcessor: IAudioProcessor {
       private let audioEngine = AVAudioEngine()
       private let fftProcessor: FFTProcessor
       private let pitchDetector: IPitchDetector
       
       let sampleRate: Int = 44100
       let bufferSize: Int = 8192
       
       private let pitchResultSubject = PassthroughSubject<PitchResult, Never>()
       
       // Implementation: AVAudioEngine setup, tap on inputNode, FFT processing
   }
   ```

2. **Create `FFTProcessor.swift`**:
   - Use Accelerate framework's `vDSP_fft_zrip`
   - Apply Hann window via `vDSP_hann_window`
   - Return magnitude spectrum as `[Float]`

3. **Create `HPSPitchDetector.swift`** (implements `IPitchDetector` protocol):
   - Implement HPS algorithm (same logic as Android)
   - Return `PitchResult`

4. **Create `FrequencyConverter.swift`**:
   - Frequency → Note conversion
   - Cents deviation calculation

### Step 4: Implement Repository Layer

1. **Create `TuningRepository.swift`**:
   - Load tuning presets from JSON
   - Use `@Published` property for selected tuning
   - Persist to `UserDefaults`

2. **Create `SettingsRepository.swift`**:
   - Read/write `TunerSettings` to `UserDefaults`
   - Use `Codable` for JSON serialization

### Step 5: Implement ViewModel

1. **Create `TunerViewModel.swift`**:
   - Conform to `ObservableObject`
   - Use `@Published` for `state: TunerViewState`
   - Implement methods: `startListening()`, `stopListening()`, `selectTuning()`

2. **Implement State Updates**:
   - Subscribe to `audioProcessor.observePitchResults()` using Combine
   - Update `state.currentPitchResult` at 20 Hz
   - Trigger haptic via `UINotificationFeedbackGenerator`

### Step 6: Implement UI Layer

1. **Create `TunerView.swift` SwiftUI View**:
   ```swift
   struct TunerView: View {
       @StateObject var viewModel: TunerViewModel
       
       var body: some View {
           VStack {
               NoteDisplayView(note: viewModel.state.displayNote, 
                               octave: viewModel.state.displayOctave)
               TuningBarView(state: viewModel.state)
               CentDisplayView(cents: viewModel.state.displayCents)
               StringIndicatorsView(tuning: viewModel.state.selectedTuning)
               
               Button("Start") {
                   Task { await viewModel.startListening() }
               }
           }
       }
   }
   ```

2. **Create Custom SwiftUI Views**:
   - `TuningBarView`: Use `GeometryReader` + `Path` for custom drawing
   - `NoteDisplayView`: Large `Text` view
   - `StringIndicatorsView`: `HStack` of 6 indicators

3. **Implement Theme Support**:
   - Use `@Environment(\.colorScheme)` to detect light/dark mode
   - Define custom colors in `Assets.xcassets` with light/dark variants

### Step 7: Testing

1. **Unit Tests** (`MyTuneTests/`):
   - Test FFT wrapper (`FFTProcessor`)
   - Test pitch detection algorithm
   - Test frequency conversion
   - Test ViewModel state transitions

2. **UI Tests** (`MyTuneUITests/`):
   - Test navigation to settings
   - Test tuning selection
   - Test microphone permission handling

3. **Run Tests**:
   - Cmd+U in Xcode or `xcodebuild test`

### Step 8: Run & Debug

1. **Connect Physical Device** (simulator microphone is limited)
2. **Run App** (Cmd+R in Xcode)
3. **Grant Microphone Permission**
4. **Test with Guitar**

---

## Phase 3: WebApp Implementation

### Step 1: Project Setup

1. **Create Vite + React + TypeScript Project**:
   ```bash
   cd my-tune
   npm create vite@latest webapp -- --template react-ts
   cd webapp
   npm install
   ```

2. **Install Dependencies**:
   ```bash
   npm install framer-motion          # Animations
   npm install react-router-dom       # Navigation
   npm install @types/webaudio-api    # Web Audio types (dev)
   npm install -D @playwright/test    # E2E testing
   npm install -D @testing-library/react @testing-library/jest-dom jest
   ```

3. **Configure TypeScript** (`tsconfig.json`):
   ```json
   {
     "compilerOptions": {
       "target": "ES2020",
       "lib": ["ES2020", "DOM", "DOM.Iterable"],
       "module": "ESNext",
       "moduleResolution": "bundler",
       "strict": true,
       "jsx": "react-jsx"
     }
   }
   ```

4. **Configure Vite** (`vite.config.ts`):
   ```typescript
   import { defineConfig } from 'vite';
   import react from '@vitejs/plugin-react';

   export default defineConfig({
     plugins: [react()],
     build: {
       target: 'es2020',
       rollupOptions: {
         output: {
           manualChunks: {
             'audio-engine': ['./src/audio/FFTProcessor.ts']
           }
         }
       }
     },
     worker: {
       format: 'es'
     }
   });
   ```

### Step 2: Implement Data Models

1. **Create File Structure**:
   ```
   src/
   ├── core/                    # Core layer
   │   ├── models/
   │   │   ├── Tuning.ts
   │   │   ├── GuitarString.ts
   │   │   ├── PitchResult.ts
   │   │   └── TunerSettings.ts
   │   ├── services/
   │   │   ├── TuningService.ts
   │   │   └── PitchConverter.ts
   │   └── constants/
   │       └── tuningPresets.ts
   ├── audio/                   # Audio engine layer
   │   ├── AudioProcessor.ts
   │   ├── AudioWorkletProcessor.ts
   │   ├── FFTProcessor.wasm    # WebAssembly FFT (compiled from C/Rust)
   │   └── PitchDetector.ts
   ├── ui/                      # UI layer
   │   ├── components/
   │   │   ├── TunerDisplay.tsx
   │   │   ├── NoteIndicator.tsx
   │   │   └── SettingsPanel.tsx
   │   ├── pages/
   │   │   ├── TunerPage.tsx
   │   │   └── SettingsPage.tsx
   │   └── theme/
   │       ├── ThemeProvider.tsx
   │       └── tokens.ts
   ├── repositories/
   │   ├── TuningRepository.ts
   │   └── SettingsRepository.ts
   └── main.tsx
   ```

2. **Implement Data Interfaces**:
   - Copy entity definitions from `data-model.md` (TypeScript versions)

3. **Load Tuning Presets**:
   - Copy `shared/tuning-presets/presets.json` to `src/core/constants/`
   - Import directly: `import presets from './tuningPresets.json'`

### Step 3: Implement Audio Processing

1. **Create `AudioProcessor.ts`** (implements `IAudioProcessor`):
   ```typescript
   export class WebAudioProcessor implements IAudioProcessor {
     private audioContext?: AudioContext;
     private workletNode?: AudioWorkletNode;
     readonly sampleRate = 48000;
     readonly bufferSize = 8192;
     
     async start(): Promise<void> {
       const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
       this.audioContext = new AudioContext({ sampleRate: this.sampleRate });
       
       await this.audioContext.audioWorklet.addModule('/audio-worklet-processor.js');
       // Setup worklet, connect nodes
     }
     
     // Implementation: MessagePort communication with worklet
   }
   ```

2. **Create `audio-worklet-processor.js`** (runs on audio thread):
   ```javascript
   class TunerAudioWorkletProcessor extends AudioWorkletProcessor {
     process(inputs, outputs, parameters) {
       const input = inputs[0][0]; // Mono channel
       // Send audio buffer to main thread via this.port.postMessage()
       return true;
     }
   }
   registerProcessor('tuner-audio-processor', TunerAudioWorkletProcessor);
   ```

3. **Create WebAssembly FFT**:
   - Option A: Write FFT in C, compile with Emscripten:
     ```bash
     emcc fft.c -o FFTProcessor.wasm -s EXPORTED_FUNCTIONS='["_computeFFT"]'
     ```
   - Option B: Implement FFT in TypeScript (slower but simpler for MVP):
     ```typescript
     export class FFTProcessor {
       computeFFT(inputBuffer: Float32Array): Float32Array {
         // Cooley-Tukey FFT algorithm
       }
     }
     ```

4. **Create `PitchDetector.ts`** (implements `IPitchDetector`):
   - HPS algorithm implementation (same logic as other platforms)

5. **Create `FrequencyConverter.ts`**:
   - Frequency → Note conversion
   - Cents deviation calculation

### Step 4: Implement Repository Layer

1. **Create `TuningRepository.ts`**:
   - Load presets from imported JSON
   - Read/write selected tuning to `localStorage`
   - Use callback-based observers

2. **Create `SettingsRepository.ts`**:
   - Read/write `TunerSettings` to `localStorage`
   - JSON.stringify/parse for serialization

### Step 5: Implement ViewModel (React Hook)

1. **Create `useTunerViewModel.ts` Hook**:
   ```typescript
   export function useTunerViewModel() {
     const [state, setState] = useState<TunerViewState>(INITIAL_TUNER_STATE);
     const audioProcessor = useRef(new WebAudioProcessor(...));
     
     const startListening = async () => {
       try {
         await audioProcessor.current.start();
         setState(prev => ({ ...prev, isListening: true }));
         // Subscribe to pitch results
       } catch (error) {
         setState(prev => ({ ...prev, error: error as AudioError }));
       }
     };
     
     // Other methods: stopListening, selectTuning
     
     return { state, startListening, stopListening, selectTuning };
   }
   ```

### Step 6: Implement UI Layer

1. **Create `TunerPage.tsx` Component**:
   ```tsx
   export function TunerPage() {
     const { state, startListening, stopListening } = useTunerViewModel();
     
     return (
       <div className="tuner-page">
         <NoteIndicator note={state.displayNote} octave={state.displayOctave} />
         <TunerDisplay state={state} />
         <CentDisplay cents={state.displayCents} />
         <button onClick={startListening}>Start</button>
       </div>
     );
   }
   ```

2. **Create Custom Components**:
   - `TunerDisplay`: SVG-based tuning bar with animated marker (Framer Motion)
   - `NoteIndicator`: Large text display
   - `StringIndicators`: Grid of 6 target notes

3. **Implement Theme Support**:
   - Detect system theme: `window.matchMedia('(prefers-color-scheme: dark)')`
   - CSS variables for light/dark colors
   - ThemeContext provider for manual toggle

### Step 7: Service Worker (Offline Support)

1. **Create `service-worker.js`**:
   ```javascript
   const CACHE_NAME = 'my-tune-v1';
   const urlsToCache = ['/', '/index.html', '/main.js', '/assets/*'];
   
   self.addEventListener('install', event => {
     event.waitUntil(
       caches.open(CACHE_NAME).then(cache => cache.addAll(urlsToCache))
     );
   });
   
   self.addEventListener('fetch', event => {
     event.respondWith(
       caches.match(event.request).then(response => response || fetch(event.request))
     );
   });
   ```

2. **Register Service Worker** (`main.tsx`):
   ```typescript
   if ('serviceWorker' in navigator) {
     window.addEventListener('load', () => {
       navigator.serviceWorker.register('/service-worker.js');
     });
   }
   ```

### Step 8: Testing

1. **Unit Tests** (Jest + React Testing Library):
   ```typescript
   test('frequency conversion', () => {
     const result = frequencyToNote(110);
     expect(result.note).toBe('A');
     expect(result.octave).toBe(2);
   });
   ```

2. **Component Tests**:
   ```typescript
   test('TunerDisplay renders marker at correct position', () => {
     render(<TunerDisplay state={mockState} />);
     const marker = screen.getByTestId('tuning-marker');
     expect(marker).toHaveStyle('left: 50%'); // Centered
   });
   ```

3. **E2E Tests** (Playwright):
   ```typescript
   test('full tuning flow', async ({ page, context }) => {
     await context.grantPermissions(['microphone']);
     await page.goto('http://localhost:5173');
     await page.click('button:has-text("Start")');
     await expect(page.locator('.tuner-bar')).toBeVisible();
   });
   ```

4. **Run Tests**:
   ```bash
   npm test                  # Unit tests
   npx playwright test       # E2E tests
   ```

### Step 9: Build & Deploy

1. **Build Production Bundle**:
   ```bash
   npm run build
   ```

2. **Preview Locally**:
   ```bash
   npm run preview
   ```

3. **Deploy** (choose one):
   - **Vercel**: `npx vercel --prod`
   - **Netlify**: `netlify deploy --prod --dir=dist`
   - **Firebase Hosting**: `firebase deploy`

---

## Shared Resources Setup

### Design Tokens

1. **Create `shared/design-tokens/` Directory**:
   ```bash
   mkdir -p shared/design-tokens
   ```

2. **Add Token Files** (colors.json, typography.json, spacing.json)
   - Copy from research.md specifications

3. **Generate Platform Code**:
   ```bash
   cd tools/scripts
   node generate-design-tokens.js
   ```
   - Outputs: `android/app/src/main/res/values/colors.xml`, `ios/MyTune/Resources/DesignTokens.swift`, `webapp/src/ui/theme/tokens.ts`

### Tuning Presets

1. **Create `shared/tuning-presets/presets.json`**:
   - Copy JSON from data-model.md

2. **Sync to Platforms**:
   - Android: Copy to `app/src/main/assets/`
   - iOS: Add to Xcode project in `Resources/`
   - WebApp: Copy to `src/core/constants/` and import

---

## CI/CD Setup

### GitHub Actions Workflows

1. **Create `.github/workflows/` Directory**:
   ```bash
   mkdir -p .github/workflows
   ```

2. **Add `android-ci.yml`**:
   ```yaml
   name: Android CI
   on: [push, pull_request]
   jobs:
     build:
       runs-on: ubuntu-latest
       steps:
         - uses: actions/checkout@v3
         - name: Set up JDK 17
           uses: actions/setup-java@v3
           with:
             java-version: '17'
         - name: Build with Gradle
           run: cd android && ./gradlew build
         - name: Run tests
           run: cd android && ./gradlew test
   ```

3. **Add `ios-ci.yml`** (similar structure for Xcode)

4. **Add `webapp-ci.yml`** (similar structure for npm)

---

## Troubleshooting

### Common Issues

#### Android
- **AudioRecord initialization fails**: Check microphone permission granted
- **FFT producing NaN values**: Ensure Hann window applied, check for zero-division
- **UI freezing during FFT**: Verify processing on background thread (Dispatchers.IO)

#### iOS
- **AVAudioEngine not starting**: Check microphone permission, verify audio session configuration
- **vDSP FFT crashes**: Ensure buffer sizes are powers of 2, check memory alignment

#### WebApp
- **getUserMedia fails**: Ensure HTTPS (required for microphone), check browser permissions
- **AudioWorklet not loading**: Verify module path is correct, check CORS headers
- **WebAssembly not instantiating**: Check `.wasm` file MIME type served correctly

### Performance Issues

- **High CPU usage**: Reduce FFT update rate (increase `frequencyUpdateRate` interval)
- **UI lag**: Ensure FFT processing off main thread, reduce UI update frequency
- **Memory leaks**: Verify audio resources released in lifecycle methods (onPause, deinit, unmount)

---

## Next Steps

After completing the MVP on all platforms:

1. **Internal Testing**: Test on physical devices with real guitars
2. **User Acceptance Testing**: Share with beta testers
3. **Performance Profiling**: Use Android Profiler, Xcode Instruments, Chrome DevTools
4. **Accessibility Audit**: Test with TalkBack, VoiceOver, screen readers
5. **App Store Preparation**: Screenshots, privacy policy, metadata
6. **Deployment**: Google Play Internal Testing, TestFlight, staging deployment for WebApp

---

## Resources

- **Research Document**: `specs/001-guitar-tuner/research.md`
- **Data Model**: `specs/001-guitar-tuner/data-model.md`
- **API Contracts**: `specs/001-guitar-tuner/contracts/`
- **Constitution**: `.specify/memory/constitution.md`
- **Platform Docs**:
  - Android: https://developer.android.com/
  - iOS: https://developer.apple.com/documentation/
  - Web Audio API: https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API

---

## Conclusion

This quickstart guide provides a complete roadmap for implementing the Guitar Tuner application across all three platforms. Follow the phases sequentially (Android MVP → iOS → WebApp) to ensure a robust, tested implementation that meets all constitutional requirements and performance targets.

**Remember**: Test frequently, commit often, and maintain feature parity across platforms!
