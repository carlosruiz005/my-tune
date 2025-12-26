# Implementation Plan: Guitar Tuner Application

**Branch**: `001-guitar-tuner` | **Date**: 2025-12-08 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-guitar-tuner/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Building a cross-platform guitar tuner application that detects guitar string frequencies through device microphone input and provides real-time visual feedback for tuning accuracy. The application supports multiple preset tunings for 6-string guitars and operates entirely offline. Implementation follows a phased rollout: Android MVP first, followed by iOS, then WebApp.

Core Technical Approach:
- Native audio frequency detection using FFT (Fast Fourier Transform) implemented from scratch without external libraries
- Real-time pitch-to-note conversion with cent deviation calculation
- Platform-specific UI implementations following native design patterns
- Shared business logic for pitch detection algorithms and tuning presets
- Monorepo structure with platform-specific directories

## Technical Context

### Android (MVP - Priority 1)
**Language/Version**: Kotlin 1.9+ with Jetpack Compose  
**Primary Dependencies**: 
- Jetpack Compose (UI framework)
- AndroidX Architecture Components (ViewModel, LiveData/StateFlow)
- Kotlin Coroutines & Flow (async operations)
- AudioRecord API (microphone access)
- Custom FFT implementation (kissFFT or native Kotlin implementation)

**Architecture**: MVVM + Android Architecture Components  
**Storage**: SharedPreferences (tuning preset persistence), Room Database (optional for future features)  
**Testing**: JUnit 5, Espresso (UI tests), Robolectric (unit tests), AndroidX Test  
**Target Platform**: Android 8.0 (API 26) or superior  
**Minimum SDK**: API 26, Target SDK: Latest stable (API 34)  
**Performance Goals**: 
- Cold start < 2 seconds on mid-range devices
- Audio processing latency < 100ms
- UI updates at 60fps
- Pitch detection accuracy within ±3 cents

**Constraints**: 
- Offline-only operation (no network required)
- Memory footprint < 150MB
- FFT processing must not block UI thread
- Real-time audio analysis with minimal latency

**Scale/Scope**: Single activity, 2-3 composable screens, ~15-20 Kotlin files

### iOS (Priority 2)
**Language/Version**: Swift 5.9+  
**Primary Dependencies**: 
- SwiftUI (UI framework)
- Accelerate Framework (FFT via vDSP)
- AVFoundation (audio capture)
- Combine (reactive patterns)

**Architecture**: MVVM or Swift Composable Architecture  
**Storage**: UserDefaults (tuning preset persistence), CoreData (optional for future features)  
**Testing**: XCTest, XCUITest, Quick/Nimble (BDD-style tests)  
**Target Platform**: iOS 14.0 or superior  
**Performance Goals**: 
- Cold start < 1.5 seconds
- Audio processing latency < 100ms
- UI animations at 60fps
- Pitch detection accuracy within ±3 cents

**Constraints**: 
- Offline-only operation
- Memory footprint < 100MB
- Background audio processing restrictions
- App Store compliance requirements

**Scale/Scope**: 2-3 SwiftUI views, ~15-20 Swift files

### WebApp (Priority 3)
**Language/Version**: TypeScript 5.0+ in strict mode  
**Primary Dependencies**: 
- React 18+ (UI framework)
- Web Audio API (audio capture and processing)
- AudioWorklet (real-time audio processing)
- WebAssembly (optimized FFT implementation)

**Architecture**: Clean Architecture (UI / Core / Audio Engine layers)  
**Storage**: localStorage (tuning preset persistence), IndexedDB (optional for future features)  
**Testing**: Jest, React Testing Library, Playwright or Cypress (E2E tests)  
**Target Platform**: Chrome, Safari, Edge, Firefox (last 2 versions of each)  
**Build System**: Vite with code splitting and tree shaking  
**Performance Goals**: 
- First Contentful Paint < 1.5 seconds on 3G
- Audio processing latency < 100ms
- JavaScript bundle < 50MB after compression
- Lighthouse score > 90

**Constraints**: 
- Browser microphone permission required
- WebAssembly support required
- Service Worker for offline capability
- WCAG 2.1 AA accessibility compliance

**Scale/Scope**: 2-3 React pages, ~20-25 TypeScript files

### Shared Requirements (All Platforms)
**Project Type**: Mobile + Web (Monorepo)  
**Theme Support**: Light and Dark modes (mandatory for all platforms)  
**Audio Processing**: 
- Sample rate: 44100 Hz or 48000 Hz
- FFT size: 4096 or 8192 samples for frequency resolution
- Windowing function: Hann or Hamming window
- Frequency range: 82 Hz (E2) to 330 Hz (E4) for standard 6-string guitar

**Tuning Presets** (Minimum Required):
1. Standard (EADGBE)
2. Drop D (DADGBE)
3. Half Step Down (Eb Ab Db Gb Bb Eb)
4. Full Step Down (DGCFAD)
5. Drop C (CGCFAD)

**Accuracy Requirements**:
- Pitch detection within ±3 cents of true pitch
- Visual indicator movement threshold: ±10 cents
- Update frequency: 10-20 times per second

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Code Quality First**: ✅ PASS
  - Android: ktlint/detekt + Android Lint configured
  - iOS: SwiftLint with strict configuration
  - WebApp: ESLint + TypeScript strict mode
  - Complexity limit: max cyclomatic complexity = 10
  - Dependencies: Justified (native audio APIs, UI frameworks, testing libraries)
  - DRY: Shared pitch detection logic in `shared/` module

- [x] **Test-Driven Development**: ✅ PASS
  - Coverage targets: 80% unit, 70% integration defined
  - Android: JUnit 5 + Espresso + Robolectric
  - iOS: XCTest + XCUITest + Quick/Nimble
  - WebApp: Jest + React Testing Library + Playwright/Cypress
  - User approval gate: Test scenarios in spec.md (User Stories 1-3) approved
  - Performance tests: Audio latency < 100ms, UI at 60fps

- [x] **Cross-Platform UX Consistency**: ✅ PASS
  - Design system: Shared design tokens (colors, typography, spacing) in `shared/design-tokens`
  - Android: Material Design 3 components
  - iOS: Human Interface Guidelines + SF Symbols
  - WebApp: Responsive design + WCAG 2.1 AA
  - Theme support: Light + Dark modes (mandatory)
  - Accessibility: Screen readers, 44x44 touch targets, color contrast 4.5:1
  - Offline-first: Core functionality works offline (no network required)

- [x] **Performance Standards**: ✅ PASS
  - Android: Cold start < 2s, memory < 150MB
  - iOS: Cold start < 1.5s, memory < 100MB
  - WebApp: FCP < 1.5s on 3G, bundle < 50MB, Lighthouse > 90
  - Frame rate: 60fps for all animations
  - Audio processing: < 100ms latency, ±3 cents accuracy
  - Monitoring: Firebase Analytics/Crashlytics (Android/iOS), Sentry (WebApp)

- [x] **Platform Requirements**: ✅ PASS
  - Android: API 26+ (Android 8.0+), Kotlin + Jetpack Compose, MVVM
  - iOS: iOS 14.0+, Swift 5.9+, SwiftUI, MVVM/Composable Architecture
  - WebApp: React 18+ + TypeScript 5.0+, Clean Architecture, Vite
  - Security: Native storage (SharedPreferences, UserDefaults, localStorage)

- [x] **Quality Gates**: ✅ PASS
  - CI/CD: Per-platform build pipelines in monorepo (Android Gradle, iOS Xcode, WebApp Vite)
  - Automated: Zero test failures, coverage thresholds, static analysis, security scans
  - Manual: QA sign-off, accessibility audit, localization verification
  - Platform gates: Android APK < 50MB, iOS TestFlight 48h beta, WebApp Lighthouse > 90

- [x] **Monorepo Structure**: ✅ PASS
  - Repository layout: `android/`, `ios/`, `webapp/`, `shared/`, `tools/`
  - Android: Native Kotlin implementation
  - iOS: Native Swift implementation
  - WebApp: Native React/TypeScript implementation
  - Shared: Platform-agnostic pitch detection, tuning presets, design tokens
  - CI/CD: Independent build/test/deploy per platform

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
android/                          # Priority 1: Android MVP
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mytune/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── tuner/           # Main tuner screen composables
│   │   │   │   │   ├── settings/        # Tuning preset selection
│   │   │   │   │   └── theme/           # Light/Dark theme components
│   │   │   │   ├── viewmodel/
│   │   │   │   │   └── TunerViewModel   # MVVM state management
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/           # Tuning, String, PitchResult entities
│   │   │   │   │   └── usecase/         # DetectPitchUseCase, etc.
│   │   │   │   ├── data/
│   │   │   │   │   ├── audio/           # AudioRecorder, FFTProcessor
│   │   │   │   │   └── repository/      # TuningRepository
│   │   │   │   └── di/                  # Hilt dependency injection modules
│   │   │   └── res/                     # Android resources
│   │   ├── androidTest/                 # Espresso UI tests
│   │   └── test/                        # JUnit unit tests, Robolectric
│   └── build.gradle.kts
└── gradle/

ios/                              # Priority 2: iOS implementation
├── MyTune/
│   ├── MyTuneApp.swift
│   ├── Views/
│   │   ├── TunerView.swift              # Main tuner SwiftUI view
│   │   ├── SettingsView.swift           # Tuning preset selection
│   │   └── Components/                  # Reusable UI components
│   ├── ViewModels/
│   │   └── TunerViewModel.swift         # ObservableObject state
│   ├── Models/
│   │   ├── Tuning.swift                 # Data models
│   │   ├── GuitarString.swift
│   │   └── PitchResult.swift
│   ├── Services/
│   │   ├── AudioEngine.swift            # AVFoundation audio capture
│   │   ├── FFTProcessor.swift           # vDSP Accelerate FFT
│   │   └── PitchDetector.swift          # Hz to note conversion
│   └── Resources/
│       └── Assets.xcassets              # Light/Dark theme assets
├── MyTuneTests/                         # XCTest unit tests
├── MyTuneUITests/                       # XCUITest UI tests
└── MyTune.xcodeproj

webapp/                           # Priority 3: WebApp implementation
├── src/
│   ├── ui/                              # UI Layer (React components)
│   │   ├── components/
│   │   │   ├── TunerDisplay.tsx         # Visual tuning indicator
│   │   │   ├── NoteIndicator.tsx        # Note name display
│   │   │   └── SettingsPanel.tsx        # Tuning preset selector
│   │   ├── pages/
│   │   │   ├── TunerPage.tsx            # Main tuner page
│   │   │   └── SettingsPage.tsx         # Settings page
│   │   └── theme/                       # Light/Dark theme provider
│   ├── core/                            # Core Layer (business logic)
│   │   ├── models/
│   │   │   ├── Tuning.ts
│   │   │   ├── GuitarString.ts
│   │   │   └── PitchResult.ts
│   │   ├── services/
│   │   │   ├── TuningService.ts         # Tuning preset management
│   │   │   └── PitchConverter.ts        # Hz to note/cents logic
│   │   └── constants/
│   │       └── tuningPresets.ts         # Preset definitions
│   ├── audio/                           # Audio Engine Layer
│   │   ├── AudioCapture.ts              # Web Audio API microphone
│   │   ├── AudioWorkletProcessor.ts     # Real-time audio processing
│   │   ├── FFTProcessor.wasm            # WebAssembly FFT implementation
│   │   └── PitchDetector.ts             # Frequency detection
│   └── main.tsx                         # React app entry point
├── public/
│   └── service-worker.js                # Offline support
├── tests/
│   ├── unit/                            # Jest unit tests
│   ├── integration/                     # React Testing Library
│   └── e2e/                             # Playwright/Cypress
└── vite.config.ts

shared/                           # Platform-agnostic modules
├── design-tokens/
│   ├── colors.json                      # Light/Dark theme colors
│   ├── typography.json                  # Font scales
│   └── spacing.json                     # Layout spacing
├── tuning-presets/
│   └── presets.json                     # Standard, Drop D, etc.
└── pitch-detection/
    ├── fft-algorithm.md                 # FFT math documentation
    ├── pitch-conversion.md              # Hz to note conversion formulas
    └── frequency-ranges.md              # Guitar string frequency reference

tools/                            # Build and CI/CD tools
├── ci/
│   ├── android-build.yml                # GitHub Actions Android
│   ├── ios-build.yml                    # GitHub Actions iOS
│   └── webapp-build.yml                 # GitHub Actions WebApp
└── scripts/
    ├── generate-design-tokens.js        # Design token code generation
    └── validate-tuning-presets.js       # Preset validation
```

**Structure Decision**: Monorepo with three independent native platform implementations (Android/iOS/WebApp) plus shared resources. Each platform has its own build system, testing infrastructure, and follows platform-specific architectural patterns. Shared modules contain platform-agnostic data (design tokens, tuning presets, algorithm documentation) but do NOT contain executable code to avoid cross-platform dependencies. Each platform implements its own FFT and pitch detection using native technologies.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**No violations detected.** All constitutional requirements are satisfied.

---

## Phase 0 & Phase 1 Completion Summary

### Phase 0: Research Completed ✅

All technical unknowns have been resolved and documented in `research.md`:

1. **FFT Implementation Strategy**: Platform-specific approaches defined (Kotlin/kissFFT for Android, Accelerate vDSP for iOS, WebAssembly for WebApp)
2. **Pitch Detection Algorithm**: HPS (Harmonic Product Spectrum) with parabolic interpolation selected
3. **UI Architecture**: Platform-native implementations with shared design tokens
4. **Data Persistence**: Key-value storage approach justified (SharedPreferences, UserDefaults, localStorage)
5. **Performance Optimization**: Thread architecture, buffer management, and animation strategies documented
6. **Testing Strategy**: 70/20/10 test pyramid defined with platform-specific frameworks

### Phase 1: Design Completed ✅

All design artifacts have been generated:

1. **Data Model** (`data-model.md`): ✅
   - 4 core entities defined: Tuning, GuitarString, PitchResult, TunerSettings
   - Platform-specific implementations (Kotlin, Swift, TypeScript)
   - Validation rules and state transitions documented
   - Entity relationship diagram included

2. **API Contracts** (`contracts/`): ✅
   - `audio-processing-interface.md`: Defines IAudioProcessor, IPitchDetector, IFrequencyConverter, ITuningRepository, ISettingsRepository
   - `ui-state-management.md`: Defines TunerViewState, SettingsViewState, ITunerViewModel with state transitions
   - Platform-specific implementations for all interfaces
   - Performance SLAs documented (response times, throughput)

3. **QuickStart Guide** (`quickstart.md`): ✅
   - Step-by-step setup instructions for all three platforms
   - Prerequisite tools and versions specified
   - Phase-by-phase implementation walkthrough (Android → iOS → WebApp)
   - Shared resources setup (design tokens, tuning presets)
   - CI/CD pipeline configuration
   - Troubleshooting guide for common issues

4. **Agent Context Updated**: ✅
   - GitHub Copilot context file updated with new technologies (Kotlin, Jetpack Compose, SharedPreferences)

### Constitution Re-Evaluation (Post-Design) ✅

All constitutional principles remain satisfied after design phase:

- ✅ **Code Quality First**: Linting, testing, and DRY principles embedded in architecture
- ✅ **Test-Driven Development**: Test strategies defined for all platforms (unit, integration, UI tests)
- ✅ **Cross-Platform UX Consistency**: Design tokens ensure visual consistency, platform conventions respected
- ✅ **Performance Standards**: Targets defined and optimization strategies documented
- ✅ **Platform Requirements**: All platform-specific requirements met (Android API 26+, iOS 14+, modern browsers)
- ✅ **Quality Gates**: CI/CD pipelines defined, automated and manual verification checklists included
- ✅ **Monorepo Structure**: Repository layout clearly defined with separation of concerns

### Key Artifacts Summary

| Artifact | Location | Status | Size |
|----------|----------|--------|------|
| Implementation Plan | `plan.md` | ✅ Complete | This file |
| Research Document | `research.md` | ✅ Complete | ~10,000 words |
| Data Model | `data-model.md` | ✅ Complete | ~7,000 words |
| Audio Processing Interface | `contracts/audio-processing-interface.md` | ✅ Complete | ~6,000 words |
| UI State Management | `contracts/ui-state-management.md` | ✅ Complete | ~6,000 words |
| QuickStart Guide | `quickstart.md` | ✅ Complete | ~8,000 words |

**Total Documentation**: ~37,000 words across 6 comprehensive documents

---

## Next Steps: Phase 2 (NOT Included in /speckit.plan)

The `/speckit.plan` command ends here. To proceed with implementation:

1. **Run `/speckit.tasks`** to generate `tasks.md` with implementation tickets
2. **Begin Android MVP Development** following `quickstart.md` Phase 1
3. **Set up CI/CD pipelines** as documented in `quickstart.md`
4. **Create `shared/` resources** (design tokens, tuning presets JSON files)
5. **Establish testing infrastructure** (test fixtures, mock implementations)

---

## Branch & Artifacts

- **Branch**: `001-guitar-tuner`
- **Implementation Plan**: `/Users/carlosalbertoruizrobles/Desarrollos/my-tune/specs/001-guitar-tuner/plan.md`
- **Generated Artifacts**:
  - Research: `specs/001-guitar-tuner/research.md`
  - Data Model: `specs/001-guitar-tuner/data-model.md`
  - Contracts: `specs/001-guitar-tuner/contracts/`
  - QuickStart: `specs/001-guitar-tuner/quickstart.md`

**Planning phase complete. Ready for implementation.**
