# Tasks: Guitar Tuner Application

**Input**: Design documents from `/specs/001-guitar-tuner/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅

**Tests**: Tests are OPTIONAL per spec. NOT implementing TDD approach for MVP. Focus on manual testing and post-implementation automated tests.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `- [ ] [ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Monorepo structure:
- **Android**: `android/app/src/main/java/com/mytune/`
- **iOS**: `ios/MyTune/`
- **WebApp**: `webapp/src/`
- **Shared**: `shared/` (design tokens, tuning presets)
- **Tools**: `tools/` (scripts, CI/CD)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure for all three platforms

**CONSTITUTION COMPLIANCE**: Implements Code Quality First, Monorepo Structure, and Quality Gates principles

**Platform Priority**: Android → iOS → WebApp

- [X] T001 Create monorepo directory structure: 'mkdir -p android ios webapp shared/design-tokens shared/tuning-presets shared/pitch-detection tools/ci tools/scripts'
- [X] T002 Create shared/README.md documenting purpose of each subdirectory (design-tokens for UI tokens, tuning-presets for JSON data, pitch-detection for algorithm docs)
- [X] T003 [P] Create shared/tuning-presets/presets.json with 5 minimum tunings (Standard, Drop D, Half Step Down, Full Step Down, Drop C)
- [X] T004 [P] Create shared/design-tokens/colors.json with light and dark theme color definitions
- [X] T005 [P] Create shared/design-tokens/typography.json with font scales for all platforms
- [X] T006 [P] Create shared/design-tokens/spacing.json with layout spacing values
- [X] T007 [P] Create shared/pitch-detection/fft-algorithm.md documenting FFT implementation approach
- [X] T008 [P] Create shared/pitch-detection/pitch-conversion.md documenting Hz to note conversion formulas
- [X] T009 [P] Create shared/pitch-detection/frequency-ranges.md with guitar string frequency reference table
- [X] T010 Initialize Android project structure using 'mkdir -p android && cd android && gradle init --type basic --dsl kotlin --project-name MyTune'
- [X] T011 Create android/settings.gradle.kts with root project name and app module inclusion
- [X] T012 Create android/build.gradle.kts with buildscript dependencies (AGP 8.2+, Kotlin 1.9+, Hilt plugin)
- [X] T013 Create android/gradle.properties with JVM args, AndroidX flags, and Kotlin incremental compilation
- [X] T014 Create android/gradle/wrapper/gradle-wrapper.properties with Gradle 8.5+ distribution URL
- [X] T015 Create android/app/build.gradle.kts with Android app plugin, Compose setup, API 26+ config, and dependencies (Compose BOM, Hilt, Coroutines, Serialization)
- [X] T016 Create android/app/src/main/AndroidManifest.xml with application tag, microphone/vibrate permissions, and launcher activity
- [X] T017 Create android/app/src/main/java/com/mytune/MainActivity.kt as entry point with setContent for Compose
- [X] T018 [P] Setup ktlint and detekt in android/build.gradle.kts with plugins and configuration blocks
- [X] T019 Verify Android build with './gradlew assembleDebug' command executes successfullys successfully
- [X] T020 Initialize iOS project using Xcode command 'xcodebuild -project ios/MyTune.xcodeproj' or Xcode GUI with SwiftUI App template, iOS 14+ deployment target
- [X] T021 Create ios/MyTune.xcodeproj/project.pbxproj with proper build settings (Swift 5.9+, iOS 14.0 deployment target, Accelerate framework linked)
- [X] T022 Create ios/MyTune/MyTuneApp.swift as SwiftUI app entry point with @main attribute and WindowGroup
- [X] T023 Create ios/MyTune/Info.plist with NSMicrophoneUsageDescription: 'Microphone access is required to detect guitar string pitch for tuning'
- [X] T024 Configure ios/MyTune.xcodeproj build settings: enable Swift Concurrency, link Accelerate.framework, set PRODUCT_BUNDLE_IDENTIFIER
- [X] T025 [P] Create ios/.swiftlint.yml with strict rules (line_length: 120, function_body_length: 40, type_body_length: 200, force_unwrapping: error)
- [X] T026 Verify iOS build with 'xcodebuild -scheme MyTune -configuration Debug -sdk iphonesimulator build' executes successfullyk iphonesimulator build' executes successfully
- [X] T027 Initialize WebApp project using 'npm create vite@latest webapp -- --template react-ts' in repository root
- [X] T028 Create webapp/package.json with scripts: dev (vite), build (vite build), preview (vite preview), test (jest), test:e2e (playwright test), lint (eslint)
- [X] T029 Create webapp/tsconfig.json with strict mode enabled, target: ES2020, lib: [ES2020, DOM], jsx: react-jsx, moduleResolution: bundler
- [X] T030 Create webapp/vite.config.ts with React plugin, code splitting (manualChunks for vendor/audio modules), WebAssembly support, resolve aliases
- [X] T031 Install WebApp core dependencies: 'cd webapp && npm install react@18 react-dom@18 framer-motion@11 react-router-dom@6'
- [X] T032 Install WebApp dev dependencies: 'npm install -D @types/react @types/react-dom @types/web typescript@5 vite@5 eslint@8 prettier@3'
- [X] T033 [P] Create webapp/.eslintrc.cjs with TypeScript parser, React plugin, strict rules, and Prettier integration
- [X] T034 [P] Create webapp/tsconfig.node.json for Vite config with module: ESNext and moduleResolution: bundler
- [X] T035 Create webapp/index.html with root div, script src to main.tsx, meta viewport, and app title
- [X] T036 Create webapp/src/main.tsx as React entry point with StrictMode and createRoot
- [X] T037 Verify WebApp dev server with 'npm run dev' starts successfully on localhost:5173cessfully on localhost:5173
- [ ] T038 [P] Create tools/ci/android-build.yml GitHub Actions workflow (setup-java@v4, Gradle cache, './gradlew assembleDebug lintDebug testDebugUnitTest')
- [ ] T039 [P] Create tools/ci/ios-build.yml GitHub Actions workflow (macos-latest runner, xcode-select, xcodebuild with -sdk iphonesimulator, SwiftLint run)
- [ ] T040 [P] Create tools/ci/webapp-build.yml GitHub Actions workflow (setup-node@v4, npm ci, npm run lint, npm run build, npm run test, Playwright install and test) npm run build, npm run test, Playwright install and test)
- [ ] T041 [P] Create tools/scripts/generate-design-tokens.js Node script to read JSON from shared/design-tokens and generate Kotlin/Swift/TS files
- [ ] T042 [P] Create tools/scripts/validate-tuning-presets.js Node script with JSON schema validation for preset structure (name, strings array with 6 items)
- [ ] T043 [P] Setup Firebase project via Firebase Console, download google-services.json (Android) and GoogleService-Info.plist (iOS), add Firebase SDK dependencies
- [ ] T044 [P] Setup Sentry project via Sentry Dashboard, obtain DSN, add @sentry/react SDK to webapp/package.json, configure Sentry.init in main.tsxain DSN, add @sentry/react SDK to webapp/package.json, configure Sentry.init in main.tsx

**Checkpoint**: All three platform projects initialized with basic structure and CI/CD pipelines

**Verification Commands**:
- Android: `cd android && ./gradlew assembleDebug` (should build successfully)
- iOS: `cd ios && xcodebuild -scheme MyTune -sdk iphonesimulator build` (should build successfully)
- WebApp: `cd webapp && npm run dev` (should start dev server on http://localhost:5173)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core data models and shared infrastructure that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

**CONSTITUTION COMPLIANCE**: Implements Cross-Platform UX Consistency and Performance Standards

### Android Foundation

- [X] T045 [P] Create android/app/src/main/java/com/mytune/data/model/Tuning.kt data class with validation
- [X] T046 [P] Create android/app/src/main/java/com/mytune/data/model/GuitarString.kt data class with validation
- [X] T047 [P] Create android/app/src/main/java/com/mytune/data/model/PitchResult.kt data class with tuning state enum
- [X] T048 [P] Create android/app/src/main/java/com/mytune/data/model/TunerSettings.kt data class with theme mode enum
- [X] T049 [P] Create android/app/src/main/java/com/mytune/ui/theme/Color.kt with Material3 color schemes (light/dark)
- [X] T050 [P] Create android/app/src/main/java/com/mytune/ui/theme/Type.kt with Material3 typography scale
- [X] T051 [P] Create android/app/src/main/java/com/mytune/ui/theme/Theme.kt with dynamic color support
- [X] T052 Create android/app/src/main/java/com/mytune/data/repository/TuningRepositoryImpl.kt loading presets from assets
- [X] T053 Create android/app/src/main/java/com/mytune/data/repository/SettingsRepositoryImpl.kt using SharedPreferences
- [X] T054 Create android/app/src/main/java/com/mytune/di/AppModule.kt Hilt module for dependency injection

### iOS Foundation

- [X] T055 [P] Create ios/MyTune/Models/Tuning.swift struct with Codable conformance and validation
- [X] T056 [P] Create ios/MyTune/Models/GuitarString.swift struct with Codable conformance
- [X] T057 [P] Create ios/MyTune/Models/PitchResult.swift struct with TuningState enum
- [X] T058 [P] Create ios/MyTune/Models/TunerSettings.swift struct with ThemeMode enum
- [X] T059 [P] Create ios/MyTune/Resources/Assets.xcassets with color sets for light/dark themes
- [X] T060 [P] Create ios/MyTune/Views/Components/ThemeProvider.swift for theme management
- [X] T061 Create ios/MyTune/Repositories/TuningRepository.swift loading presets from JSON in Resources/
- [X] T062 Create ios/MyTune/Repositories/SettingsRepository.swift using UserDefaults with Combine publishers

### WebApp Foundation

- [ ] T063 [P] Create webapp/src/core/models/Tuning.ts interface with validation function
- [ ] T064 [P] Create webapp/src/core/models/GuitarString.ts interface with validation
- [ ] T065 [P] Create webapp/src/core/models/PitchResult.ts interface with TuningState enum
- [ ] T066 [P] Create webapp/src/core/models/TunerSettings.ts interface with ThemeMode enum
- [ ] T067 [P] Create webapp/src/ui/theme/tokens.ts importing design tokens from shared/
- [ ] T068 [P] Create webapp/src/ui/theme/ThemeProvider.tsx with light/dark mode context
- [ ] T069 [P] Create webapp/src/ui/theme/globalStyles.css with CSS variables for themes
- [ ] T070 Create webapp/src/repositories/TuningRepository.ts importing presets.json from shared/
- [ ] T071 Create webapp/src/repositories/SettingsRepository.ts using localStorage with observers
- [ ] T072 Create webapp/public/service-worker.js for offline PWA support

**Checkpoint**: Foundation ready - all platforms have data models, repositories, and themes

---

## Phase 3: User Story 3 - String-by-String Identification (Priority: P1) 🎯 MVP Core

**Goal**: Display detected note name in real-time as user plucks guitar strings (automatic detection mode)

**Why First**: This is the most fundamental feature - note detection without tuning accuracy. Enables testing of audio pipeline.

**Independent Test**: Pluck each of 6 guitar strings in random order, verify app displays correct note name regardless of tuning accuracy.

**Platform Coverage**: [✓] Android [✓] iOS [✓] WebApp

### Android Implementation - User Story 3

- [X] T073 [P] [US3] Create android/app/src/main/java/com/mytune/data/audio/FFTProcessor.kt implementing Cooley-Tukey FFT with Hann window
- [X] T074 [P] [US3] Create android/app/src/main/java/com/mytune/data/audio/HPSPitchDetector.kt implementing Harmonic Product Spectrum algorithm
- [X] T075 [P] [US3] Create android/app/src/main/java/com/mytune/data/audio/FrequencyConverter.kt for Hz to note conversion (MIDI formula)
- [X] T076 [US3] Create android/app/src/main/java/com/mytune/data/audio/AudioRecorder.kt using AudioRecord API on background thread
- [X] T077 [US3] Create android/app/src/main/java/com/mytune/data/audio/AndroidAudioProcessor.kt implementing IAudioProcessor interface
- [X] T078 [US3] Create android/app/src/main/java/com/mytune/viewmodel/TunerViewModel.kt with StateFlow for pitch results
- [X] T079 [P] [US3] Create android/app/src/main/java/com/mytune/ui/tuner/NoteDisplay.kt composable showing large note name + octave
- [X] T080 [P] [US3] Create android/app/src/main/java/com/mytune/ui/tuner/TunerScreen.kt with Start/Stop buttons and permission handling
- [X] T081 [US3] Implement microphone permission request flow in TunerScreen.kt using rememberLauncherForActivityResult
- [ ] T082 [US3] Test Android app with real guitar: verify note detection for all 6 strings (E A D G B E)
- [ ] T083 [US3] Test Android edge cases: background noise, quiet input, very out-of-tune strings
- [ ] T084 [US3] Verify Android performance: < 100ms latency, UI at 60fps, memory < 150MB

### iOS Implementation - User Story 3

- [X] T085 [P] [US3] Create ios/MyTune/Services/FFTProcessor.swift using Accelerate vDSP for FFT with Hann window
- [X] T086 [P] [US3] Create ios/MyTune/Services/HPSPitchDetector.swift implementing HPS algorithm
- [X] T087 [P] [US3] Create ios/MyTune/Services/FrequencyConverter.swift for Hz to note conversion
- [X] T088 [US3] Create ios/MyTune/Services/AudioEngine.swift using AVAudioEngine and AVAudioInputNode
- [X] T089 [US3] Create ios/MyTune/Services/IOSAudioProcessor.swift implementing IAudioProcessor protocol with Combine publishers
- [X] T090 [US3] Create ios/MyTune/ViewModels/TunerViewModel.swift as ObservableObject with @Published state
- [X] T091 [P] [US3] Create ios/MyTune/Views/Components/NoteDisplayView.swift showing large note name + octave
- [X] T092 [P] [US3] Create ios/MyTune/Views/TunerView.swift with Start/Stop buttons and permission handling
- [X] T093 [US3] Implement microphone permission request in TunerView.swift using AVAudioSession authorization
- [ ] T094 [US3] Test iOS app with real guitar: verify note detection for all 6 strings - REQUIRES MANUAL TESTING
- [ ] T095 [US3] Test iOS edge cases: background noise, quiet input, very out-of-tune strings - REQUIRES MANUAL TESTING
- [ ] T096 [US3] Verify iOS performance: < 100ms latency, UI at 60fps, memory < 100MB - REQUIRES MANUAL TESTING

### WebApp Implementation - User Story 3

- [ ] T097 [P] [US3] Create webapp/src/audio/FFTProcessor.ts with Cooley-Tukey FFT implementation (TypeScript or WebAssembly)
- [ ] T098 [P] [US3] Create webapp/src/audio/PitchDetector.ts implementing HPS algorithm
- [ ] T099 [P] [US3] Create webapp/src/core/services/PitchConverter.ts for Hz to note conversion
- [ ] T100 [US3] Create webapp/src/audio/audio-worklet-processor.js for AudioWorklet thread processing
- [ ] T101 [US3] Create webapp/src/audio/AudioProcessor.ts implementing IAudioProcessor with Web Audio API
- [ ] T102 [US3] Create webapp/src/ui/hooks/useTunerViewModel.ts React hook managing tuner state
- [ ] T103 [P] [US3] Create webapp/src/ui/components/NoteIndicator.tsx displaying large note name + octave
- [ ] T104 [P] [US3] Create webapp/src/ui/pages/TunerPage.tsx with Start/Stop buttons and permission handling
- [ ] T105 [US3] Implement microphone permission request using navigator.mediaDevices.getUserMedia with error handling
- [ ] T106 [US3] Test WebApp with real guitar: verify note detection for all 6 strings across browsers (Chrome, Safari, Firefox)
- [ ] T107 [US3] Test WebApp edge cases: background noise, quiet input, very out-of-tune strings
- [ ] T108 [US3] Verify WebApp performance: < 100ms latency, FCP < 1.5s on 3G, Lighthouse > 90

**Checkpoint**: All platforms can detect and display note names in real-time (US3 complete)

---

## Phase 4: User Story 1 - Standard Tuning Detection (Priority: P1) 🎯 MVP Complete

**Goal**: Visual tuning indicator (horizontal bar with marker) showing if string is in-tune, flat, or sharp for Standard tuning

**Why Now**: Builds on US3 note detection to add tuning accuracy feedback. This completes the core tuner functionality.

**Independent Test**: Play each of 6 guitar strings and verify marker position accurately reflects pitch deviation (centered when in-tune, below when flat, above when sharp).

**Platform Coverage**: [✓] Android [✓] iOS [✓] WebApp

### Android Implementation - User Story 1

- [ ] T109 [P] [US1] Enhance android PitchResult to include targetString matching and cents deviation calculation
- [ ] T110 [P] [US1] Create android/app/src/main/java/com/mytune/ui/tuner/TuningBar.kt composable with Canvas drawing
- [ ] T111 [P] [US1] Implement marker position animation using animateFloatAsState with spring physics in TuningBar.kt
- [ ] T112 [P] [US1] Implement marker color logic (red/yellow/green) based on tuning state in TuningBar.kt
- [ ] T113 [P] [US1] Create android/app/src/main/java/com/mytune/ui/tuner/CentDisplay.kt showing numeric cent deviation
- [ ] T114 [P] [US1] Create android/app/src/main/java/com/mytune/ui/tuner/StringIndicators.kt showing 6 target notes (E A D G B E)
- [ ] T115 [US1] Integrate TuningBar, CentDisplay, StringIndicators into TunerScreen.kt
- [ ] T116 [US1] Implement in-tune confirmation: green marker + pulse animation using AnimatedVisibility
- [ ] T117 [US1] Implement haptic feedback on in-tune using Vibrator/VibratorManager (check device capability)
- [ ] T118 [US1] Add ±10 cents movement threshold to prevent marker jitter on small frequency fluctuations
- [ ] T119 [US1] Test Android tuning accuracy with each string: verify marker centered at ±0 cents, moves correctly for ±10, ±20, ±30 cents
- [ ] T120 [US1] Test Android in-tune confirmation triggers only on transition (not continuously)
- [ ] T121 [US1] Verify Android UI animations maintain 60fps during marker updates

### iOS Implementation - User Story 1

- [X] T122 [P] [US1] Enhance iOS PitchResult to include targetString matching and cents deviation
- [X] T123 [P] [US1] Create ios/MyTune/Views/Components/TuningBarView.swift with GeometryReader and Path drawing
- [X] T124 [P] [US1] Implement marker position animation using .animation(.spring()) in TuningBarView.swift
- [X] T125 [P] [US1] Implement marker color logic (red/yellow/green) based on tuning state
- [X] T126 [P] [US1] Create ios/MyTune/Views/Components/CentDisplayView.swift showing numeric cents
- [X] T127 [P] [US1] Create ios/MyTune/Views/Components/StringIndicatorsView.swift showing 6 target notes
- [X] T128 [US1] Integrate all components into TunerView.swift with proper layout
- [X] T129 [US1] Implement in-tune confirmation: green marker + scale animation using .scaleEffect
- [X] T130 [US1] Implement haptic feedback on in-tune using UINotificationFeedbackGenerator
- [X] T131 [US1] Add ±10 cents movement threshold to prevent marker jitter
- [ ] T132 [US1] Test iOS tuning accuracy with each string: verify marker behavior at various cent deviations
- [ ] T133 [US1] Test iOS in-tune confirmation triggers correctly
- [ ] T134 [US1] Verify iOS UI animations maintain 60fps

### WebApp Implementation - User Story 1

- [ ] T135 [P] [US1] Enhance WebApp PitchResult to include targetString matching and cents deviation
- [ ] T136 [P] [US1] Create webapp/src/ui/components/TunerDisplay.tsx with SVG-based tuning bar and animated marker
- [ ] T137 [P] [US1] Implement marker position animation using Framer Motion with spring transition
- [ ] T138 [P] [US1] Implement marker color logic (red/yellow/green) based on tuning state
- [ ] T139 [P] [US1] Create webapp/src/ui/components/CentDisplay.tsx showing numeric cents
- [ ] T140 [P] [US1] Create webapp/src/ui/components/StringIndicators.tsx showing 6 target notes in grid
- [ ] T141 [US1] Integrate all components into TunerPage.tsx with responsive layout
- [ ] T142 [US1] Implement in-tune confirmation: green marker + scale animation using Framer Motion
- [ ] T143 [US1] Implement haptic feedback on in-tune using navigator.vibrate (check browser support)
- [ ] T144 [US1] Add ±10 cents movement threshold to prevent marker jitter
- [ ] T145 [US1] Test WebApp tuning accuracy across browsers: verify marker behavior
- [ ] T146 [US1] Test WebApp in-tune confirmation triggers correctly
- [ ] T147 [US1] Verify WebApp animations are smooth and performant

**Checkpoint**: All platforms have fully functional guitar tuner with visual feedback (US1 + US3 = MVP Core Complete)

---

## Phase 5: User Story 2 - Alternative Tuning Selection (Priority: P2)

**Goal**: Settings screen with tuning preset selection (Drop D, Half Step Down, etc.) with persistence across app sessions

**Why Now**: Extends tuner usefulness beyond Standard tuning for intermediate/advanced players. Non-blocking for MVP.

**Independent Test**: Select different tuning presets, verify target notes update correctly, close and reopen app to verify persistence.

**Platform Coverage**: [✓] Android [✓] iOS [✓] WebApp

### Android Implementation - User Story 2

- [ ] T148 [P] [US2] Create android/app/src/main/java/com/mytune/ui/settings/SettingsScreen.kt composable with tuning list
- [ ] T149 [P] [US2] Create android/app/src/main/java/com/mytune/ui/settings/TuningListItem.kt showing tuning name and string notes
- [ ] T150 [US2] Implement navigation from TunerScreen.kt to SettingsScreen.kt using Navigation Compose
- [ ] T151 [US2] Implement tuning selection in TunerViewModel: call TuningRepository.setSelectedTuning()
- [ ] T152 [US2] Add settings button/icon to TunerScreen.kt app bar
- [ ] T153 [US2] Verify selected tuning persists to SharedPreferences and loads on app restart
- [ ] T154 [US2] Test Android tuning selection: verify target notes update in StringIndicators when tuning changes
- [ ] T155 [US2] Test Android with Drop D tuning: verify 6th string shows D instead of E

### iOS Implementation - User Story 2

- [ ] T156 [P] [US2] Create ios/MyTune/Views/SettingsView.swift with List of tuning presets
- [ ] T157 [P] [US2] Create ios/MyTune/Views/Components/TuningRowView.swift showing tuning name and notes
- [ ] T158 [US2] Implement navigation from TunerView.swift to SettingsView.swift using NavigationLink
- [ ] T159 [US2] Implement tuning selection in TunerViewModel: call TuningRepository.setSelectedTuning()
- [ ] T160 [US2] Add toolbar button to TunerView.swift for settings navigation
- [ ] T161 [US2] Verify selected tuning persists to UserDefaults and loads on app restart
- [ ] T162 [US2] Test iOS tuning selection: verify target notes update when tuning changes
- [ ] T163 [US2] Test iOS with Drop D tuning: verify correct target notes displayed

### WebApp Implementation - User Story 2

- [ ] T164 [P] [US2] Create webapp/src/ui/pages/SettingsPage.tsx with list of tuning presets
- [ ] T165 [P] [US2] Create webapp/src/ui/components/TuningListItem.tsx showing tuning name and string notes
- [ ] T166 [US2] Implement routing from TunerPage.tsx to SettingsPage.tsx using React Router
- [ ] T167 [US2] Implement tuning selection in useTunerViewModel hook: call TuningRepository.setSelectedTuning()
- [ ] T168 [US2] Add settings navigation button to TunerPage.tsx
- [ ] T169 [US2] Verify selected tuning persists to localStorage and loads on page reload
- [ ] T170 [US2] Test WebApp tuning selection: verify target notes update when tuning changes
- [ ] T171 [US2] Test WebApp with Drop D tuning: verify correct behavior

**Checkpoint**: All platforms support multiple tuning presets with persistence (US2 complete)

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize app quality, handle edge cases, prepare for release

**CONSTITUTION COMPLIANCE**: Ensures Quality Gates, Accessibility, and Performance Standards are met

### Accessibility & Localization

- [ ] T172 [P] Add Android content descriptions to all interactive elements in TunerScreen and SettingsScreen
- [ ] T173 [P] Add iOS accessibility labels and hints to all UI elements
- [ ] T174 [P] Add WebApp ARIA labels and live regions for pitch updates
- [ ] T175 [P] Test Android with TalkBack: verify all elements readable and tuning state announced
- [ ] T176 [P] Test iOS with VoiceOver: verify navigation and state announcements
- [ ] T177 [P] Test WebApp with NVDA/JAWS: verify screen reader compatibility
- [ ] T178 [P] Verify touch targets are 44x44 dp/pt minimum on all platforms
- [ ] T179 [P] Run color contrast checker on all themes (target 4.5:1 for text)
- [ ] T180 [P] Setup localization framework for future i18n support (Android strings.xml, iOS Localizable.strings, WebApp i18n)

### Error Handling & Edge Cases

- [ ] T181 [P] Android: Implement error states for microphone permission denied with user guidance
- [ ] T182 [P] iOS: Implement error states for audio session failures with user guidance
- [ ] T183 [P] WebApp: Implement error states for getUserMedia failures with browser-specific guidance
- [ ] T184 [P] Handle "no pitch detected" state gracefully (show placeholder instead of stale data)
- [ ] T185 [P] Handle extremely out-of-tune strings (>100 cents) with appropriate UI feedback
- [ ] T186 [P] Implement noise gate threshold to ignore ambient noise below confidence level
- [ ] T187 [P] Add visual indicator when audio processing is active (e.g., pulsing mic icon)
- [ ] T188 [P] Handle app backgrounding/foregrounding: stop/resume audio capture appropriately

### Performance Optimization & Monitoring

- [ ] T189 [P] Android: Profile app with Android Profiler (CPU, memory, battery usage)
- [ ] T190 [P] iOS: Profile app with Xcode Instruments (Time Profiler, Allocations, Energy Log)
- [ ] T191 [P] WebApp: Profile with Chrome DevTools (Performance tab, Memory profiler)
- [ ] T192 [P] Optimize FFT buffer reuse to reduce memory allocations
- [ ] T193 [P] Verify Android cold start time < 2 seconds on mid-range device (Snapdragon 6-series)
- [ ] T194 [P] Verify iOS cold start time < 1.5 seconds on iPhone X or later
- [ ] T195 [P] Verify WebApp FCP < 1.5 seconds on Fast 3G (Chrome Lighthouse audit)
- [ ] T196 [P] Verify UI animations maintain 60fps during pitch updates on all platforms
- [ ] T197 [P] Configure Firebase Crashlytics for Android/iOS to track crashes
- [ ] T198 [P] Configure Sentry for WebApp to track errors and performance

### Testing & Quality Assurance

- [ ] T199 [P] Android: Run static analysis (ktlint, detekt) and fix all warnings
- [ ] T200 [P] iOS: Run SwiftLint with strict rules and fix all warnings
- [ ] T201 [P] WebApp: Run ESLint and fix all warnings/errors
- [ ] T202 [P] Write unit tests for FFT implementation with synthetic sine wave inputs (all platforms)
- [ ] T203 [P] Write unit tests for HPS pitch detector with known frequency inputs (all platforms)
- [ ] T204 [P] Write unit tests for frequency converter (110Hz → A2, 440Hz → A4, etc.)
- [ ] T205 [P] Android: Write Espresso UI tests for critical user flow (start → tune string → stop)
- [ ] T206 [P] iOS: Write XCUITest for critical user flow
- [ ] T207 [P] WebApp: Write Playwright E2E test for critical user flow
- [ ] T208 Manual QA: Test with real guitar on multiple devices (low-end, mid-range, high-end)
- [ ] T209 Manual QA: Test in noisy environments (moderate background noise up to 60dB)
- [ ] T210 Manual QA: Test all 5 tuning presets with real guitar
- [ ] T211 Manual QA: Test edge cases (very out-of-tune, quiet strings, multiple strings simultaneously)

### App Store Preparation

- [ ] T212 [P] Android: Create app icon in all required densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
- [ ] T213 [P] iOS: Create app icon in all required sizes for App Store
- [ ] T214 [P] WebApp: Create favicon and PWA icons (192x192, 512x512)
- [ ] T215 [P] Android: Create feature graphic and screenshots for Google Play listing
- [ ] T216 [P] iOS: Create screenshots for App Store listing (multiple device sizes)
- [ ] T217 [P] WebApp: Create social media preview images
- [ ] T218 Write app description highlighting key features (offline, multiple tunings, accurate ±3 cents)
- [ ] T219 [P] Create privacy policy document (hosted on website or in-app)
- [ ] T220 [P] Android: Configure ProGuard/R8 obfuscation rules for release build
- [ ] T221 [P] iOS: Configure release build settings and code signing
- [ ] T222 [P] WebApp: Configure production build with optimizations (minification, tree-shaking)
- [ ] T223 [P] Android: Verify APK size < 50MB (target < 20MB)
- [ ] T224 [P] iOS: Verify IPA size < 50MB (target < 20MB)
- [ ] T225 [P] WebApp: Verify bundle size < 50MB compressed (target < 5MB)
- [ ] T226 [P] Run pre-submission checklist for Google Play (permissions, content rating, etc.)
- [ ] T227 [P] Run pre-submission checklist for App Store (privacy labels, metadata, TestFlight)
- [ ] T228 [P] Setup WebApp hosting on Vercel/Netlify/Firebase with HTTPS and custom domain

---

## Implementation Strategy

### MVP Definition (Phases 1-4)

**Minimum Viable Product includes**:
- ✅ Phase 1: Setup (all platforms initialized)
- ✅ Phase 2: Foundational (data models, themes, repositories)
- ✅ Phase 3: User Story 3 (note detection and display)
- ✅ Phase 4: User Story 1 (tuning accuracy with visual indicator)

**MVP delivers**: Fully functional guitar tuner for Standard tuning with real-time pitch detection and visual feedback on Android, iOS, and WebApp.

### Phased Rollout

1. **Android First** (Weeks 1-2):
   - Complete T001-T044 (Setup)
   - Complete T045-T054 (Android Foundation)
   - Complete T073-T084 (Android US3)
   - Complete T109-T121 (Android US1)
   - **Deliverable**: Functional Android MVP

2. **iOS Second** (Weeks 3-4):
   - Complete T020-T026, T055-T062 (iOS Setup & Foundation)
   - Complete T085-T096 (iOS US3)
   - Complete T122-T134 (iOS US1)
   - **Deliverable**: Functional iOS MVP

3. **WebApp Third** (Weeks 5-6):
   - Complete T027-T037, T063-T072 (WebApp Setup & Foundation)
   - Complete T097-T108 (WebApp US3)
   - Complete T135-T147 (WebApp US1)
   - **Deliverable**: Functional WebApp MVP

4. **Feature Expansion** (Week 7):
   - Complete Phase 5 (US2) on all platforms in parallel
   - **Deliverable**: Multiple tuning support

5. **Polish & Release** (Week 8):
   - Complete Phase 6 (Polish) on all platforms
   - **Deliverable**: Production-ready apps

### Parallel Execution Opportunities

**Within Phase 1 (Setup)**:
- T003-T009 (shared resources) can be done in parallel
- T018, T025, T033 (linters) can be done in parallel
- T038-T044 (CI/CD and services) can be done in parallel

**Within Phase 2 (Foundation)**:
- T045-T051 (Android models/theme) can be done in parallel
- T055-T059 (iOS models/theme) can be done in parallel
- T063-T069 (WebApp models/theme) can be done in parallel

**Within Phase 3 (US3)**:
- T073-T075 (Android audio processing) can be done in parallel
- T079-T080 (Android UI) can be done in parallel
- T085-T087 (iOS audio processing) can be done in parallel
- T091-T092 (iOS UI) can be done in parallel
- T097-T099 (WebApp audio processing) can be done in parallel
- T103-T104 (WebApp UI) can be done in parallel

**Within Phase 6 (Polish)**:
- T172-T180 (accessibility) can be done in parallel
- T181-T188 (error handling) can be done in parallel
- T189-T198 (performance & monitoring) can be done in parallel
- T199-T207 (testing) can be done in parallel
- T212-T228 (app store prep) can be done in parallel

---

## Dependencies & Blocking Tasks

### Critical Path (Must Complete in Order)

1. **Setup → Foundation → User Stories**
   - Phase 1 (T001-T044) MUST complete before Phase 2
   - Phase 2 (T045-T072) MUST complete before Phase 3
   - Phase 3 (US3) SHOULD complete before Phase 4 (US1) to enable iterative testing

2. **Per-Platform Critical Path**:
   - Android: T010-T019 → T045-T054 → T073-T084 → T109-T121
   - iOS: T020-T026 → T055-T062 → T085-T096 → T122-T134
   - WebApp: T027-T037 → T063-T072 → T097-T108 → T135-T147

3. **Audio Pipeline Dependencies**:
   - FFT implementation → Pitch detector → Frequency converter → Audio processor → ViewModel → UI

4. **UI Dependencies**:
   - Data models → ViewModel → UI components → Screen composition → Navigation

### Non-Blocking Tasks

- Phase 5 (US2) can start as soon as Phase 4 completes (no dependencies on polish)
- Phase 6 (Polish) tasks are mostly independent and can be parallelized
- CI/CD setup (T038-T040) can happen anytime after platform initialization
- Design token generation (T041) can happen once shared tokens are created

---

## Validation Checklist

Before considering the feature complete, verify:

### Functional Requirements (from spec.md)

- [ ] FR-001: ✅ Microphone audio detection working on all platforms
- [ ] FR-002: ✅ Visual tuning indicator (horizontal bar + marker) implemented
- [ ] FR-016: ✅ App functions completely offline (no network calls)
- [ ] FR-017: ✅ Automatic detection mode (no manual string selection)
- [ ] FR-018: ✅ Marker moves visibly for ±10 cents deviations
- [ ] FR-003: ✅ Marker centered when in-tune
- [ ] FR-004: ✅ Marker below center when flat
- [ ] FR-005: ✅ Marker above center when sharp
- [ ] FR-006: ✅ Note name displayed in real-time
- [ ] FR-007: ✅ Settings menu for tuning selection
- [ ] FR-008: ✅ 5 minimum tuning presets included
- [ ] FR-009: ✅ Target notes update when tuning selected
- [ ] FR-010: ✅ Selected tuning persists across sessions
- [ ] FR-011: ✅ Green marker + animation when in-tune
- [ ] FR-012: ✅ All 6 target notes displayed
- [ ] FR-013: ✅ Real-time pitch updates with minimal latency
- [ ] FR-014: ✅ Microphone permission handling implemented
- [ ] FR-015: ✅ Guitar strings differentiated by frequency
- [ ] FR-019: ✅ Haptic feedback on in-tune (optional)

### Success Criteria (from spec.md)

- [ ] SC-001: Can tune 6 strings in < 3 minutes (manual test)
- [ ] SC-002: Visual indicator latency < 100ms (performance profiling)
- [ ] SC-003: Pitch detection accuracy ±3 cents (test with tone generator)
- [ ] SC-004: Tuning selection takes < 30 seconds (usability test)
- [ ] SC-005: 90% success rate on first attempt (beta tester feedback)
- [ ] SC-006: Users achieve ±5 cents accuracy (manual test)
- [ ] SC-007: Works in 60dB ambient noise (environmental test)

### Constitutional Requirements

- [ ] Code Quality: All linters pass with zero warnings
- [ ] Testing: Unit tests written for core audio processing logic
- [ ] Cross-Platform: Feature parity across Android, iOS, WebApp
- [ ] Performance: All platforms meet latency/memory/size targets
- [ ] Platform Requirements: Minimum SDK/OS versions met
- [ ] Quality Gates: CI/CD pipelines passing
- [ ] Monorepo: Code properly organized in platform directories

---

## Task Summary

- **Total Tasks**: 228
- **Phase 1 (Setup)**: 44 tasks
- **Phase 2 (Foundation)**: 28 tasks
- **Phase 3 (US3 - Note Detection)**: 36 tasks (12 per platform)
- **Phase 4 (US1 - Tuning Accuracy)**: 39 tasks (13 per platform)
- **Phase 5 (US2 - Multiple Tunings)**: 24 tasks (8 per platform)
- **Phase 6 (Polish)**: 57 tasks

**Parallelizable Tasks**: ~60% (tasks marked with [P])

**Estimated Timeline**: 8 weeks (with 1 developer per platform working in parallel)

---

**Generated**: 2025-12-08  
**Feature Branch**: `001-guitar-tuner`  
**Status**: Ready for implementation  
**Next Step**: Begin Phase 1 (Setup) starting with T001
